def push(Map properties=[:]) {
    if(!properties.build.dockerFile?.trim()) { 
        logger.info("No Dockerfile provided")
        return 
    }
    logger.info("Kaniko Push Started")
    def ignorePaths = ["/var/spool"]
    def ignorePathArg = ""
    // Build the string arg for each path
    for (path in ignorePaths) {
        ignorePathArg = ignorePathArg + "--ignore-path ${path} "
    }
    logger.info("Ignored Paths: ${ignorePathArg}")
    logger.debug("executeKaniko params: ${properties}")
    logger.info("Docker User Args: ${properties.build.dockerargs}")
    // if (properties.build.dockerPath) {
    //     env.DOCKERPATH = "${properties.build.dockerPath}/"
    // } else {
    //     env.DOCKERPATH = ""
    // }
    def tagList = "${properties.build.artifactoryPath}:${env.GIT_COMMIT}"
    if (properties.build.imageTag) {
        tags = properties.build.imageTag.replaceAll("\\s+", "").split(',')
        for (tag in tags) {
            tagList = "${tagList}" + " -d ${properties.build.artifactoryPath}:${tag}"
        }
        logger.info("Building with Tag from Properties File")
    }
    def baseCommand="/kaniko/executor -f ${properties.build.workDir}/${properties.build.dockerFile} -c ${properties.build.workDir} -d ${tagList} ${ignorePathArg} --image-name-tag-with-digest-file=${env.GIT_COMMIT}-file-details --digest-file=${env.GIT_COMMIT}-image-properties --verbosity=info"
    withCredentials([
        file(credentialsId: "JfrogArt-SA-rw-kaniko",variable: 'BUILD_TOKEN'),
        usernamePassword(credentialsId: "JfrogArt-SA-ro-user-pass", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'),
        string(credentialsId: "JfrogArt-SA-ro-Token", variable: 'JfrogArt_TOKEN'),
        string(credentialsId: "JfrogArt-npm-SA-ro-Token", variable: 'NPM_READ_TOKEN')]) {
        /* /kaniko/.docker/config.json is the path where kaniko container assumes authentication exists. */
        if(properties.adoIAMRole?.trim()) { 
            sh """
                mkdir -p ~/.aws
                cp -v .aws-creds ~/.aws/credentials
                unset AWS_WEB_IDENTITY_TOKEN_FILE
            """
        }
        sh """
            mkdir -p /kaniko/.docker
            cp \$BUILD_TOKEN /kaniko/.docker/
            echo '{"credsStore":"ecr-login"}' > /kaniko/.docker/config.json
            ${baseCommand} --build-arg USER=${USERNAME} --build-arg PASS=${PASSWORD} --build-arg TOKEN=${JfrogArt_TOKEN} --build-arg USERARG=${properties.build.dockerargs} --build-arg NPM_READ_TOKEN=${NPM_READ_TOKEN}
            pwd;ls ${env.GIT_COMMIT}-image-properties
        """
    }
}