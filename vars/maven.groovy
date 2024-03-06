def compile(String buildArgs="") {
    // withCredentials([file(credentialsId: "", variable: "FILE")]) {
    //     sh "cp \$FILE /usr/share/maven/conf/settings.xml"
    // }
    logger.info("Maven compiling")
    logger.debug("buildArgs: ${buildArgs}")
    sh "mvn ${buildParam}"
}

def test(String testArgs="") {
    logger.info("Maven testing")
    logger.debug("testArgs: ${testArgs}")
    sh "mvn ${testParam}"
}

def mvnPackage(String packageArgs="") {
    logger.info("Maven packaging")
    logger.debug("packageArgs: ${packageArgs}")
    sh "mvn ${packageParam}"
}