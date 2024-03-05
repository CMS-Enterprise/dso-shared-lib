def runSeleniumTests(Map seleniumTestArgs=[:]) {
    logger.info("Starting Selenium Tests")
    logger.debug("seleniumTestArgs: ${seleniumTestArgs.selenium}")
    if (!seleniumTestArgs.containsKey("testUrl")) {
        throw new IllegalArgumentException("testUrl is a mandatory parameter for Selenium testing.")
    }
    
    def testUrl = seleniumTestArgs.get("testUrl")
    def browser = seleniumTestArgs.get("browser", "chrome")
    def video = seleniumTestArgs.get("video", "false")
    // I have questions about the below 2 vars
    def credentialsId = seleniumTestArgs.get("credentialsId", "selenium-access-token")
    def mavenConfigFileId = seleniumTestArgs.get("mavenConfigFileId")

    logger.info("Technology: ${seleniumTestArgs.tech}")
    logger.info("Test URL: ${testUrl}")
    logger.info("Browser: ${browser}")
    logger.info("Video Enabled: ${video}")

    def mvnCommand = "mvn -s ${mavenConfigFileId} clean test -Ds.url=${testUrl} -Ds.token=\$TOKEN -Ds.browser=${browser} -Ds.video=${video}"

    withCredentials([string(credentialsId: "${credentialsId}", variable: "TOKEN")]) {
        sh """
            cd test/maven
            ${mvnCommand}
        """
    }
    
    logger.info("Selenium Tests Completed")
}
