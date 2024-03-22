def jfrogXray(Map properties=[:]) {
    if(properties.build.artifactoryPath.contains("amazonaws")) {
        logger.info("Artifact not stored in Artifactory")
        return
    } 

    def repoName = properties.artifactPackagePath.split("/")[0]
    def relativeArtifactPath = properties.artifactPackagePath.split("/")[1]
    def searchPath
    if(properties.build.fileName.contains(".zip")) {  
        searchPath = "${properties.build.fileName}&repo=${repoName}"
    } else {
        searchPath = "${properties.artifactName}/${env.GIT_COMMIT}/manifest.json&repo=${repoName}"
    }

    logger.info("JFrog XRay Scan")
    // jfrogPublishBuild(properties)
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        try {
            sh"""
                apk add --no-cache bash jq
                jf c add cms-artifactory --url=https://artifactory.cloud.cms.gov/ --access-token=${TOKEN}
            """
            def result = sh(script: "jf xr curl '/api/v1/artifacts?search=${searchPath}' | jq '.data[0].sec_issues'", returnStdout: true).trim()
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
    if(properties.build.fileName.contains(".zip")) {
        zipScan(properties, repoName)
    } else {
        imageScan(properties, repoName)
    }  
}

def imageScan(Map properties=[:], String repoName) {
    logger.info("Running new XRay Scan")
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh""" 
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
    }
}

def zipScan(Map properties=[:], String repoName) {
    logger.info("On-demand zip file scanning not currently supported")
}

def upload(Map properties=[:]) {
    if(properties.build.dockerFile?.trim()) { 
        logger.info("Dockerfile provided")
        return 
    }
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: 'TOKEN')]) {
        sh "jf rt u --url=https://artifactory.cloud.cms.gov/artifactory --access-token ${TOKEN} ${properties.build.fileName} ${properties.artifactPackagePath}/${properties.build.fileName} --build-name=${properties.artifactName} --build-number=${env.GIT_COMMIT}"
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

def jfrogPublishBuild(Map properties=[:]) {
    logger.info("Publish build to JFrog")
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh"""
            jf rt bce --project=${properties.artifactoryProjectName} ${properties.artifactName} ${env.GIT_COMMIT}
            jf rt bp --url=https://artifactory.cloud.cms.gov/artifactory --access-token=${TOKEN} --project=${properties.artifactoryProjectName} ${properties.artifactName} ${env.GIT_COMMIT}
        """
    }
}