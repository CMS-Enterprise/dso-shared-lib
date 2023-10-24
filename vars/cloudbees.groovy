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