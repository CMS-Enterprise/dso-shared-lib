def jfrogPublishBuild(Map properties=[:]) {
    logger.info("Publish build to JFrog")
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh"""
            jf rt bce --project=${properties.artifactoryProjectName} ${properties.artifactName} ${env.GIT_COMMIT}
            jf rt bp --url=https://artifactory.cloud.cms.gov/artifactory --access-token=${TOKEN} --project=${properties.artifactoryProjectName} ${properties.artifactName} ${env.GIT_COMMIT}
        """
    }
}

// TODO: catch block in the XRay scan to where if it returns with "nothing found" then it goes to another function to run "scan" command
def jfrogXray(Map properties=[:]) {
    logger.info("JFrog XRay Scan")
    // jfrogPublishBuild(properties)
    // withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
    withCredentials([usernamePassword(credentialsId: "jfrog-prod-sa", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')])
        // jf build-scan --url=https://artifactory.cloud.cms.gov/xray --access-token=${TOKEN} --project=${properties.artifactoryProjectName} --fail=false ${properties.artifactName} ${env.GIT_COMMIT}
        sh"""
            jf c add --url=artifactory.cloud.cms.gov --user=${USERNAME} --password=${PASSWORD}
            jf c show
            jf xr curl '/api/v1/artifacts?search=${properties.artifactName}/${env.GIT_COMMIT}/manifest.json&repo=${properties.build.repoName}'
        """
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