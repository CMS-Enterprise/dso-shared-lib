def gradleBuild(String buildArgs="") {
    logger.info("Gradle build")
    logger.debug("buildArgs: ${buildArgs}")
    sh "chmod +x gradlew; ./gradlew ${buildArgs}"
}

def gradleTest(String testArgs="") {
    logger.info("Gradle test")
    logger.debug("testArgs: ${testArgs}")
    sh "chmod +x gradlew; ./gradlew ${testArgs}"
}

def setGradlePropsFile() {
    logger.debug("Set Gradle Properties...")
    //create gradle.properties
    if (!fileExists('gradle.properties')) {
        def gradleProps = libraryResource "gradle/gradle.properties"
        writeFile file: "./gradle.properties", text: gradleProps
    }
    //inject auth creds into gradle.properties
    withCredentials([usernamePassword(credentialsId: "JfrogArt-SA-ro-user-pass", usernameVariable: 'artifactoryUser', passwordVariable: 'artifactoryPassword')]) {
        sh """
            sed -i 's/\$artifactory_User/'${artifactoryUser}'/g' gradle.properties;
            sed -i 's/\$artifactory_Pwd/'${artifactoryPassword}'/g' gradle.properties;
        """
    }
}

def removeGradlePropsFile() {
    logger.debug("Remove Gradle Properties...")
    if (fileExists('gradle.properties')) {
        sh "rm -f gradle.properties;"
    }
}