def jfrogXray(Map properties=[:]) {
    logger.info("JFrog XRay Scan")
    withCredentials([string(credentialsId: "JfrogArt-SA-ro-Token", variable: "TOKEN")]) {
        // Need to ask for their artifactory project and separate the artfactory rerpo and path names in {properties.build.artifactoryPath}?
        sh"""
            jf build-scan --url=https://artifactory.cloud.cms.gov/ --access-token=${TOKEN} --project=${properties.artifactoryProjectName} --fail=false ${properties.artifactName} ${env.GIT_COMMIT}
        """
    }
}