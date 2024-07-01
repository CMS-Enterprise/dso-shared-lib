def paramValidator(Map properties=[:]) {
    logger.info("Validating parameters...")
    def failure = 0

    logger.info("${properties.build.cacheFlag}")
    if(!properties.build.cacheFlag.equalsIgnoreCase("true") && !properties.build.cacheFlag.equalsIgnoreCase("false")) {
        logger.info("Please enter value \"true\" or \"false\" for the kaniko cache flag")
        failure+=1
    }
    if(properties.slackNotification && !properties.slackNotification.contains("#")) {
        logger.info("Please prepend your slack channel name with a #. Example: #channel-name")
        failure+=1
    }
    if(properties.build.artifactHost && properties.build.artifactHost.contains("amazonaws") && !properties.adoIAMRole?.trim()) {
        logger.info("AWS ECR upload URL provided, but no IAM role provided.")
        logger.info("Please provide IAM Role to be assumed in the account where the artifact will be uploaded.")
        failure+=1
    }
    if(properties.build.artifactHost && properties.build.artifactHost.contains("amazonaws") && (properties.build.zipPath || properties.build.fileName)) {
        logger.info("AWS ECR upload URL, but artifact is a zip file.")
        logger.info("Zip file artifacts cannot be uploaded to ECR. Please provide an image-based artifact.")
        failure+=1
    }
    if(properties.build.dockerFile && (properties.build.zipPath || properties.build.fileName)) {
        logger.info("Dockerfile and Zip file arguments provided. Please choose 1 packaging type for upload.")
        failure+=1
    }
    if(properties.artifactPackagePath && (properties.artifactPackagePath.contains("https://") || properties.build.artifactHost.contains("https://"))) {
        logger.info("Please remove \"https://\" from the parameters")
        failure+=1
    }
    if((!properties.build.zipPath?.trim() && properties.build.fileName?.trim()) || (properties.build.zipPath?.trim() && !properties.build.fileName?.trim())) {
        logger.info("Missing zipPath or zipFileName")
        logger.info("Please provide both zip file parameters for zip artifact upload")
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

def getArtifactName(Map properties=[:]) {
    properties.artifactName = properties.artifactPackagePath.split('/')[-1]
}

def terraformParamValidator (Map properties=[:]) {
    logger.info("Validating parameters...")
    def failure = 0

    if(properties.slackNotification && !properties.slackNotification.contains("#")) {
        logger.info("Please prepend your slack channel name with a #. Example: #channel-name")
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