def paramValidator(Map properties=[:]) {
    logger.info("Validating parameters...")
    def failure = 0

    if(properties.artifactPackagePath && properties.build.artifactoryPath) {
        logger.info("Artifact Package Path provided AND Artifactory Package URL provided, these are mutually exclusive.")
        logger.info("Please only provide \"Path to your artifact\" if your artifact is zip/binary based")
        logger.info("Please only provide \"URL to Artifact in Jfrog Artifactory or ECR\" if your artifact is image based")
        failure+=1
    }
    if(properties.build.artifactoryPath.contains("amazonaws") && !properties.adoIAMRole?.trim()) {
        logger.info("AWS ECR upload URL provided, but no IAM role provided.")
        logger.info("Please provide IAM Role to be assumed in the account where the artifact will be uploaded.")
        failure+=1
    }
    if(properties.build.artifactoryPath.contains("amazonaws") && properties.build.zipPath) {
        logger.info("AWS ECR upload URL, but artifact is a zip file.")
        logger.info("Zip file artifacts cannot be uploaded to ECR. Please provide an image-based artifact.")
        failure+=1
    }
    if (!properties) {
        logger.info("How did you even do that? There's literally no properties.")
        error("BOOOOOOOO TOMATO TOMATO TOMATO")
    }
    if(failure > 0) {
        error("Parameter validation failed")
    }

    logger.info("Parameters validated. Proceeding with pipeline.")
}