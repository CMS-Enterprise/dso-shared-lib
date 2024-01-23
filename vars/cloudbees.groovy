def reloadCJOC() {
    println "Starting cloudbees.reloadCJOC"
    final reloadCJOC = libraryResource "python/cloudbees/reload_cjoc.py"
    writeFile(file: "reload_cjoc.py", text: reloadCJOC)
    withCredentials([usernamePassword(credentialsId: 'CloudBees-API-Token', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        try {
            sh "python -V"
            println "python reload_cjoc.py --username '${USERNAME}' --password '${PASSWORD}' --jenkins_url '${JENKINS_URL.split("/")[2]}'"
            sh "python reload_cjoc.py --username '${USERNAME}' --password '${PASSWORD}' --jenkins_url '${JENKINS_URL.split("/")[2]}'"
        } catch (Exception e) {
            error("Error reloading Operation Center.")
        }
    }
}

def jenkinsUpdateToken(String refreshedToken) {
    logger.info("Update Access Token Credentials")
    withCredentials([string(credentialsId: 'jenkins-svc-api-token', variable: 'TOKEN')]) {
        sh """
            java -jar jenkins-jars/jenkins-cli.jar -s https://172.20.0.1:443/cjoc -http -auth dso-jenkins-dev-a:${TOKEN} update-credentials-by-xml system::system::jenkins _ JfrogArt-SA-ro-Token < update-string-cred.xml
            sed -i "s|<secret>.*<\\/secret>|<secret>SECRET<\\/secret>|g" ./update-string-cred.xml
        """
    }
}