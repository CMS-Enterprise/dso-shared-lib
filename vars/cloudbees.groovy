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
    withCredentials(string[credentialsId: 'jenkins-svc-api-token', variable:'TOKEN'])
    sh """
        yum install -y wget
        wget -i https://jenkins-dev-west.cloud.cms.gov/cjoc/jnlpJars/jenkins-cli.jar
        java -jar jenkins-cli.jar -s https://jenkins-dev-west.cloud.cms.gov/ -http -auth dso-jenkins-dev-a:${TOKEN} update-credentials-by-xml system::system::jenkins _ JfrogArt-SA-ro-Token < updated_jfrog_token.xml
        sed -i "s|<secret>.*<\\/secret>|<secret>SECRET<\\/secret>|g" ./update-string-cred.xml
    """
}