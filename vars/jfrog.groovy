def jfrogPublishBuild(Map properties=[:]) {
    logger.info("Publish build to JFrog")
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh"""
            jf rt bce --project=${properties.artifactoryProjectName} ${properties.artifactName} ${env.GIT_COMMIT}
            jf rt bp --url=https://artifactory.cloud.cms.gov/artifactory --access-token=${TOKEN} --project=${properties.artifactoryProjectName} ${properties.artifactName} ${env.GIT_COMMIT}
        """
    }
}

def jfrogXray(Map properties=[:]) {
    logger.info("JFrog XRay Scan")
    // jfrogPublishBuild(properties)
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        def repoName = properties.build.artifactoryPath.split("/")[0]
        try {
            sh"""
                apk add --no-cache bash jq
                jf c add cms-artifactory --url=https://artifactory.cloud.cms.gov/ --access-token=${TOKEN}
                jf xr curl '/api/v1/artifacts?search=${properties.artifactName}/${env.GIT_COMMIT}/manifest.json&repo=${repoName}' | jq '.data[0].sec_issues' 
            """
            // TODO: INTENTIONAL ERROR 0 BELOW vvv
            def result = sh(script: "jf xr curl '/api/v1/artifacts?search=0${properties.artifactName}/${env.GIT_COMMIT}/manifest.json&repo=${repoName}' | jq '.data[0].sec_issues'", returnStdout: true).trim()
            logger.info("XRay Scan Result: ${result}")
            if (result.equalsIgnoreCase("null")) { 
                error()
            }
        } catch (err) {
            logger.info("No existing XRay scan found")
            jfrogRunXray(properties, repoName)
        }
    }
}

def jfrogRunXray(Map properties=[:], String repoName) {
    if(properties.build.artifactoryPath.contains("amazonaws")) {
        logger.info("Artifact not stored in Artifactory")
        return
    }
    logger.info("Running new XRay Scan")

    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh""" 
            jf c show
            jf xr curl '/api/v1/scanArtifact' --header 'Content-Type: application/json' --data '{ "componentID": "docker://${properties.artifactName}:${env.GIT_COMMIT}"}'
        """
        def status = sh(script: "jf xr curl '/api/v1/artifact/status' --header 'Content-Type: application/json' --data '{ \"repo\": \"${repoName}\", \"path\": \"${properties.artifactName}/${env.GIT_COMMIT}/manifest.json\"}' | jq -r '.overall.status'", returnStdout: true)
        logger.info("Status: ${status}")

        def retry=0
        while(!status.equalsIgnoreCase('DONE') && retry < 10) {
            // Waits 30 seconds before trying again, 30 * 1000
            Thread.sleep(30000)
            status = sh(script: "jf xr curl '/api/v1/artifact/status' --header 'Content-Type: application/json' --data '{ \"repo\": \"${repoName}\", \"path\": \"${properties.artifactName}/${env.GIT_COMMIT}/manifest.json\"}' | jq -r '.overall.status'", returnStdout: true).trim()
            logger.info("Status: ${status}")
            logger.info("Retry count: ${retry}")
            retry+=1
        }
        sh """
            jf xr curl '/api/v1/artifacts?search=${properties.artifactName}/${env.GIT_COMMIT}/manifest.json&repo=${repoName}' | jq '.data[0].sec_issues'
        """

        // While : ; do thing_one; thing_two; sleep 5; done
    }

}

def jfrogRefreshToken(String refreshedToken) {
    logger.info("Create new Jfrog Token")
    def credXml = libraryResource "cloudbees/update-string-cred.xml"
    writeFile(file: "update-string-cred.xml", text: credXml)
    withCredentials([usernamePassword(credentialsId: 'dso-jenkins-sandbox', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh'''
            apk add --no-cache bash jq
            refreshedTokenResponse=$(jf rt atc --url=https://artifactory.cloud.cms.gov/artifactory --user=${USER} --password=${PASS} --groups=readers --expiry=3456000)
            refreshedToken=$(echo ${refreshedTokenResponse} | jq .access_token | sed 's/"//g')
            testVar="<secret>${refreshedToken}<\\/secret>"
            sed -i "s|<secret>.*<\\/secret>|${testVar}|g" ./update-string-cred.xml
            jf rt download --url=https://artifactory.cloud.cms.gov/artifactory --user=${USER} --password=${PASS} tooling-zip-repo/jenkins-jars/jenkins-cli.jar 
        '''
    }
}

def upload(Map properties=[:]) {
    if(properties.dockerFile != ("" || null)) { 
        logger.info("Dockerfile provided")
        return 
    }
    def FILE = properties.artifactPackagePath.split("/")[-1]
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: 'TOKEN')]) {
        // TODO: Need to figure out ${file} ${path}/${file}
        sh "jf rt u --url=https://artifactory.cloud.cms.gov/ --access-token ${TOKEN} $FILE ${properties.artifactPackagePath} --build-name=${properties.artifactName} --build-number=${env.GIT_COMMIT}"
    }
}

// def upload(Map properties=[:]) {
//     if(properties.dockerFile != ("" || null)) { 
//         logger.info("Dockerfile provided")
//         return 
//     }
//     logger.info("Artifactory upload Initiated")
//     switch (type) {
//         case 'awsCf':
//             path = path_builder(properties, true)
//             for (entry in properties?.build.aws.packages) {
//                 file = entry.packageName
//                 upload_call(path, file)
//             }
//         default:
//             file = properties?.build?.packageName
//             path = path_builder(properties, false)
//             upload_call(path, file)
//     }
// }

// def upload_call(String path, String file) {
//     def Map defaults = digestParameters()
//     withCredentials([string(credentialsId: "${env.artifactoryCredentialId}", variable: 'TOKEN')]) {
//         sh "jfrog rt u --url ${defaults.url} --access-token ${TOKEN} ${file} ${path}/${file} --build-name=${env.JOB_NAME} --build-number=${env.BUILD_NUMBER}"
//         //we capture the build info for an artifact when we upload it
//         //Assumed information will be relevant during this CI Build process, so using job name and build number as variables to store
//     }
// }

// def digestParameters(Map parameters=[:]) {
//     def defaultMap = [
//         url:"https://${env.artifactoryUrl}/artifactory",
//         org: "com/customer",
//     ]
//     logger.debug("parameters: ${parameters}")
//     return defaultMap
// }

def buildPublish(Map parameters=[:]) {
    //Optional functionality. Return if non-container build
    if(CONTAINER_BUILD ==~ /(?i)(false|FALSE)/) { return }
    logger.debug("Debug buildpublish:${parameters}")
    logger.info("buildPublish Initiated")
    def Map defaults = digestParameters()
    withCredentials([string(credentialsId: "${env.artifactoryCredentialId}", variable: 'TOKEN')]) {
        sh "jfrog rt bp --url ${defaults.url} --access-token ${TOKEN} --build-url ${env.JOB_URL} ${env.JOB_NAME} ${env.BUILD_NUMBER}"
        //Publish the captured build info
        //Likely run after a Kaniko build
        //https://github.com/jfrogrog/project-examples/tree/master/docker-oci-examples/kaniko-example
        //Leave this here to be available, but call in Kaniko?
    }
}

def buildDockerCreate(Map parameters=[:], multiTags = false, tagFile = ""){ //multiTag and tagFile used for buildDockerCreateAndPublish
    //Optional functionality. Return if non-container build
    if(CONTAINER_BUILD ==~ /(?i)(false|FALSE)/) { return }
    logger.info("buildDockerCreate Initiated")
    def Map defaults = digestParameters()
    logger.info("env.JOB_NAME: ${env.JOB_NAME}")
    logger.info("env.BUILD_NUMBER: ${env.BUILD_NUMBER}")
    logger.info("parameters.appName: ${parameters.appName}")
    logger.info("parameters.build.repo: ${parameters.build.repo}")
    withCredentials([string(credentialsId: "${env.artifactoryCredentialId}", variable: 'TOKEN')]) {
        //If calling from buildDockerCreateAndPublish, we're using multiple tags, so we want to pass a different image-file each time.
        if (multiTags == false) {
            sh "jfrog rt bdc --url ${defaults.url} --access-token ${TOKEN} ${parameters.build.repo} --image-file ${parameters.appName}-file-details --build-name=${env.JOB_NAME} --build-number=${env.BUILD_NUMBER}"
        }
        else {
            sh "jfrog rt bdc --url ${defaults.url} --access-token ${TOKEN} ${parameters.build.repo} --image-file ${tagFile} --build-name=${env.JOB_NAME} --build-number=${env.BUILD_NUMBER}"
        }
    }
}
def buildDockerCreateAndPublish(Map parameters=[:]){
    //Optional functionality. Return if non-container build
    if(CONTAINER_BUILD ==~ /(?i)(false|FALSE)/) { return }
    logger.info("buildDockerCreateAndPublish Initiated")
    // Split the file-details file into multiple files one for each tag, then call buildDockerCreate for each one.
    sh "split -l 1 ${parameters.appName}-file-details tag-"
    tagFiles = sh(returnStdout: true, script: "ls -1 tag-*").trim()
    for (file in tagFiles.split('\n')) {
        buildDockerCreate(parameters, true, file)
        buildPublish(parameters)
    }
}