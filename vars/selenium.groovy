def runSeleniumTests(Map properties=[:]) {
    logger.info("Tech: ${properties.tech}")
    switch(properties.tech) {
        case "maven3.8":
            runSeleniumTestMaven(properties.selenium)
        case properties.tech.contains("gradle"):
            logger.info("Gradle Selenium")
        case properties.tech.contains("dotnet"):
            logger.info("Dotnet Selenium")
        default:
            logger.info("Technology not found")
    } 
}

def runSeleniumTestMaven(Map seleniumTestArgs=[:]) {
    logger.info("Starting Selenium Tests")
    logger.debug("seleniumTestArgs: ${seleniumTestArgs}")
    if (!seleniumTestArgs.containsKey("testUrl")) {
        throw new IllegalArgumentException("testUrl is a mandatory parameter for Selenium testing.")
    }

    logger.info("Test URL: ${seleniumTestArgs.testUrl}")
    logger.info("Browser: ${seleniumTestArgs.browser}")
    logger.info("Video Enabled: ${seleniumTestArgs.video}")

    withCredentials([string(credentialsId: "${seleniumTestArgs.credentialsId}", variable: "TOKEN")]) {
        sh """
            cd test/maven
            mvn -s ${seleniumTestArgs.mavenConfigFile} clean test -Ds.url=${seleniumTestArgs.testUrl} -Ds.token=${TOKEN} -Ds.browser=${seleniumTestArgs.browser} -Ds.video=${seleniumTestArgs.video}
        """
    }
    
    logger.info("Selenium Tests Completed")
}
