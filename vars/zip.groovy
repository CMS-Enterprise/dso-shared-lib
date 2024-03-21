def zipBuild(Map properties=[:]) {
    if(!(properties.build.zipPath?.trim() && properties.build.fileName?.trim())) { 
        logger.info("Missing zip file path or zip file name")
        return 
    }
    logger.info("Zipping build files")
    sh """
        zip -r ${properties.build.fileName} ${properties.build.zipPath}
    """
}