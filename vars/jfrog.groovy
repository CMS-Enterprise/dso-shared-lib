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
            logger.info("Result: ${result}")
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
    logger.info("Running new XRay Scan")
    // Need to add project build path and file type to template and properties?? 
    // Or since we build the application in our pipeline how do we use that? Can we store the output of the build somehow?

    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh""" 
            jf c show
            jf xr curl '/api/v1/scanArtifact' --header 'Content-Type: application/json' --data '{ "componentID": "docker://${properties.artifactName}:${env.GIT_COMMIT}"}'
            jf xr curl '/api/v1/artifact/status' --header 'Content-Type: application/json' --data '{ "repo": "${repoName}", "path": "${properties.artifactName}/${env.GIT_COMMIT}/manifest.json"}' | jq '.overall.status'
        """
        def status = sh(script: "jf xr curl '/api/v1/artifact/status' --header 'Content-Type: application/json' --data '{ \"repo\": \"${repoName}\", \"path\": \"${properties.artifactName}/${env.GIT_COMMIT}/manifest.json\"}' | jq -r '.overall.status', returnStdout: true.trim()")
        logger.info("Status: ${status}")
        while(status.equalsIgnoreCase('SCANNING')) {
            // Waits 30 seconds before trying again, 30 * 1000
            Thread.sleep(30000)
            status = sh(script: "jf xr curl '/api/v1/artifact/status' --header 'Content-Type: application/json' --data '{ \"repo\": \"${repoName}\", \"path\": \"${properties.artifactName}/${env.GIT_COMMIT}/manifest.json\"}' | jq -r '.overall.status', returnStdout: true.trim()")
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