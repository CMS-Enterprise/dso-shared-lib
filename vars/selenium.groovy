def runSeleniumTests(Map seleniumTestArgs=[:]) {
    logger.info("Starting Selenium Tests")
    logger.debug("seleniumTestArgs: ${seleniumTestArgs}")
    if (!seleniumTestArgs.containsKey('testUrl')) {
        throw new IllegalArgumentException("testUrl is a mandatory parameter for runSeleniumTests.")
    }
    
    def testUrl = seleniumTestArgs.get('testUrl')
    def browser = seleniumTestArgs.get('browser', 'chrome')
    def video = seleniumTestArgs.get('video', 'false')
    def credentialsId = seleniumTestArgs.get('credentialsId', 'sbox')
    def mavenSettingsFileId = seleniumTestArgs.get('mavenSettingsFileId', 'b14b5104-8739-4627-9fd8-69b8b323fb6e')

    logger.info("Technology: Maven")
    logger.info("Test URL: ${testUrl}")
    logger.info("Browser: ${browser}")
    logger.info("Video Enabled: ${video}")

    def mvnCommand = "mvn -s \$MAVEN_SETTINGS clean test -Ds.url=${testUrl} -Ds.token=\$TOKEN -Ds.browser=${browser} -Ds.video=${video}"

    
    withCredentials([string(credentialsId: credentialsId, variable: 'TOKEN')]) {
        configFileProvider([configFile(fileId: mavenSettingsFileId, variable: 'MAVEN_SETTINGS')]) {
    sh """
        cd test/maven
        ${mvnCommand}
    """
        }
    }
    
    
    logger.info("Selenium Tests Completed")
}
