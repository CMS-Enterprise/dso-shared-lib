def upload() {
    echo "kaniko upload"
}

def push(Map properties=[:]) {
    def ignorePaths = ["/var/spool"]
    def ignorePathArg = ""
    // Build the string arg for each path
    for (path in ignorePaths) {
        ignorePathArg = ignorePathArg + "--ignore-path ${path} "
    }
    logger.info("Ignored Paths: ${ignorePathArg}")
    // logger.debug("executeKaniko params: ${properties}")
    logger.info("Docker User Args: ${properties.build.dockerargs}")
    // if (properties.build.dockerPath) {
    //     env.DOCKERPATH = "${properties.build.dockerPath}/"
    // } else {
    //     env.DOCKERPATH = ""
    // }
    // def tagList = "${env.nexusUrl}/${properties.build.repo.toLowerCase()}/${env.DOCKERPATH}${properties.appName.toLowerCase()}:${env.GIT_COMMIT}"
    def tagList = "artifactory.cloud.cms.gov/${properties.build.artifactoryPath}:${env.GIT_COMMIT}"
    // if (properties.build.imageTag) {
    //     tags = properties.build.imageTag.replaceAll(\\s+, "").split(',')
    //     for (tag in tags) {
    //         tagList = "${tagList}" + " -d artifactory.cloud.cms.gov/${properties.build.artifactoryPath}:${tag}"
    //     }
    //     logger.info("Building with Tag from Properties File")
    // }
    def baseCommand="/kaniko/executor -f ${properties.build.workDir}/${properties.build.dockerFile} -c ${properties.build.workDir} -d ${tagList} ${ignorePathArg} --image-name-tag-with-digest-file=${properties.appName}-file-details --digest-file=${properties.appName}-image-properties --verbosity=info"
    withCredentials([
        file(credentialsId: "JfrogArt-SA-rw-kaniko",variable: 'BUILD_TOKEN'),
        usernamePassword(credentialsId: "JfrogArt-SA-ro-user-pass", usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD'),
        string(credentialsId: "JfrogArt-SA-ro-Token", variable: 'JfrogArt_TOKEN'),
        string(credentialsId: "JfrogArt-npm-SA-ro-Token", variable: 'NPM_READ_TOKEN')]) {
        /* /kaniko/.docker/config.json is the path where kaniko container assumes authentication exists. */
        sh """
            mkdir -p /kaniko/.docker
            cp \$BUILD_TOKEN /kaniko/.docker/
            ${baseCommand} --build-arg USER=${USERNAME} --build-arg PASS=${PASSWORD} --build-arg TOKEN=${JfrogArt_TOKEN} --build-arg USERARG=${properties.build.dockerargs} --build-arg NPM_READ_TOKEN=${NPM_READ_TOKEN}
            pwd;ls ${properties.appName}-image-properties
        """
    }
}