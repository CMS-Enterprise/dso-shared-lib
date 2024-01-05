def gradleBuild(String buildArgs="") {
    logger.info("Gradle build")
    logger.debug("buildArgs: ${buildArgs}")
    buildParam = buildArgs?: "clean build"
    sh "./gradlew ${buildArgs}"
}