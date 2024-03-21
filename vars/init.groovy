def paramValidator(Map properties=[:]) {
    logger.info("Validating parameters...")

    if(properties.artifactPackagePath?.trim() && properties.build.artifactoryPath?.trim()) {
        logger.info("Artifact Package Path provided AND Artifactory Package URL provided, these are mutually exclusive.")
        logger.info("Please only provide \"Path to your artifact\" if your artifact is zip/binary based")
        logger.info("Please only provide \"URL to Artifact in Jfrog Artifactory or ECR\" if your artifact is image based")
        error("Parameter validation failed")
    } else if(properties.build.artifactoryPath.contains("amazonaws") && !properties.adoIAMRole?.trim()) {
        logger.info("AWS ECR upload URL provided, but no IAM role provided.")
        logger.info("Please provide IAM Role to be assumed in the account where the artifact will be upploaded.")
        error("Parameter validation failed")
    } else if(properties.build.artifactoryPath.contains("amazonaws") && properties.build.zipPath?.trim()) {
        logger.info("AWS ECR upload URL, but artifact is a zip file.")
        logger.info("Zip file artifacts cannot be uploaded to ECR. Please provide an image-based artifact.")
        error("Parameter validation failed")
    }

    if (!properties?.trim()) {
        error("BOOOOOOOO TOMATO TOMATO TOMATO")
    }
}