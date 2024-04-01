def zipBuild(Map properties=[:]) {
    if(!(properties.build.zipPath?.trim() && properties.build.fileName?.trim())) { 
        logger.info("No zip file path or zip file name provided, skipping zip stage")
        return 
    }
    logger.info("Zipping build files")
    sh """
        zip -r ${properties.build.fileName} ${properties.build.zipPath}
    """
}