def zipBuild(Map properties=[:]) {
    logger.info("Zipping build files")
    sh """
        pwd
        ls
        zip -r ${properties.build.fileName} ${properties.build.zipPath} 
    """
}