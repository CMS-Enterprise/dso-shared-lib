def gradleBuild(String buildArgs="") {
    logger.info("Gradle build")
    logger.debug("buildArgs: ${buildArgs}")
    sh "./gradlew ${buildArgs}"
}