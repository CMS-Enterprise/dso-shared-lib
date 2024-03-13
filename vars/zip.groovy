def zipBuild(Map properties=[:]) {
    logger.info("Zipping build files")
    sh """
        zip -r ${properties.build.fileName} ${properties.build.zipPath} 
    """
}