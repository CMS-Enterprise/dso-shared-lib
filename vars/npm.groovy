def test(String testArgs="") {
    logger.info("starting npm test")
    sh """
    npm ${testArgs}
    """
}

def install(String installArgs="") {
    logger.info("starting npm install")
    sh """
    npm ${installArgs}
    """
}

def build(String buildArgs="") {
    logger.info("starting npm build")
    sh """
    npm ${buildArgs}
    """
}