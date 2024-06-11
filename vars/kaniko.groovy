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
    def tagList = "${properties.build.artifactHost}/${properties.artifactPackagePath}:${env.GIT_COMMIT}"
    if (properties.build.imageTag) {
        tags = properties.build.imageTag.replaceAll("\\s+", "").split(',')
        for (tag in tags) {
            tagList = "${tagList}" + " -d ${properties.build.artifactHost}/${properties.artifactPackagePath}:${tag}"
        }
        logger.info("Building with Tag from Properties File")
    }
    def baseCommand="/kaniko/executor -f ${properties.build.workDir}/${properties.build.dockerFile} -c ${properties.build.workDir} -d ${tagList} ${ignorePathArg} --image-name-tag-with-digest-file=${env.GIT_COMMIT}-file-details --digest-file=${env.GIT_COMMIT}-image-properties --verbosity=info"
    
    if(properties.adoIAMRole?.trim() && properties.build.artifactHost.contains("amazonaws")) {
        logger.info("Pushing to ECR")
        ecrPush(properties, baseCommand)
    } else if(properties.build.artifactHost.contains("artifactory")) {
        logger.info("Pushing to Artifactory")
        artifactoryPush(properties, baseCommand)
    } else {
        logger.info("Artifact Path URL invalid")
        error("Artifact Path URL invalid")
    }
}

def artifactoryPush(Map properties=[:], String baseCommand) {
    withCredentials([usernamePassword(credentialsId: "kaniko-jfrog-sa-rw", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        /* /kaniko/.docker/config.json is the path where kaniko container assumes authentication exists. */
        sh """
            mkdir -p /kaniko/.docker
            touch /kaniko/.docker/config.json
            cat >> /kaniko/.docker/config.json <<EOF
{"auths": {"artifactory.cloud.cms.gov": {"username":"${USERNAME}","password":"${PASSWORD}"}}, {"artifactory-dev.cloud.cms.gov": {"username":"${USERNAME}","password":"${PASSWORD}"}}}
EOF
            ${baseCommand} --build-arg USER=${USERNAME} --build-arg PASS=${PASSWORD} --build-arg USERARG=${properties.build.dockerargs}
            pwd;ls ${env.GIT_COMMIT}-image-properties
        """
    }
}

def ecrPush(Map properties=[:], String baseCommand) {
    sh """
        mkdir -p ~/.aws
        cp -v .aws-creds ~/.aws/credentials
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        mkdir -p /kaniko/.docker
        echo '{"credsStore":"ecr-login"}' > /kaniko/.docker/config.json
        ${baseCommand} --build-arg USERARG=${properties.build.dockerargs}
        pwd;ls ${env.GIT_COMMIT}-image-properties
    """
}