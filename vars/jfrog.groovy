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
    jfrogPublishBuild(properties)
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        sh"""
            jf build-scan --url=https://artifactory.cloud.cms.gov/xray --access-token=${TOKEN} --project=${properties.artifactoryProjectName} --fail=false ${properties.artifactName} ${env.GIT_COMMIT}
        """
    }
}

def jfrogRefreshToken(String refreshedToken) {
    logger.info("Create new Jfrog Token")
    def credXml = libraryResource "cloudbees/update-string-cred.xml"
    writeFile(file: "update-string-cred.xml", text: credXml)
    withCredentials([usernamePassword(credentialsId: 'jfrog-prod-sa', passwordVariable: 'PASS', usernameVariable: 'USER')]) {
        sh'''
            apk add --no-cache bash jq
            jq --version
            refreshedTokenResponse=$(jf rt access-token-create --url=https://artifactory.cloud.cms.gov/artifactory --user=${USER} --password=${PASS} --groups=Admins --expiry=3456000)
            refreshedToken=$(jq .access_token ${refreshedTokenResponse})
            testVar="<secret>${refreshedToken}<\\/secret>"
            sed -i "" 's|<secret>.*<\\/secret>|'${testVar}'|g;' update-string-cred.xml
        '''
    }
}