def snykCodeTest(Map snykCodeTestArgs=[:]) {
    logger.info("Snyk Code Test")
    withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
        try {
            sh """
                snyk auth ${TOKEN}
                snyk code test --org=${snykCodeTestArgs.snyk.orgId} --json --report --project-name="${snykCodeTestArgs.artifactPackagePath}"
            """
        } catch(Exception ex) {
            logger.info("Snyk Code not enabled in Organization")
        }
    }
}

def snykTest(Map snykTestArgs=[:]) {
    logger.info("Snyk Test")
    logger.info("Technology: ${snykTestArgs.tech}")
    def snykTestCommand = "snyk test --org=${snykTestArgs.snyk.orgId} --json"
    def projectTech = "${snykTestArgs.tech}"
    switch(snykTestArgs.tech) {
        case "gradle":
            gradleBuildPathParam = snykTestArgs.snyk.gradleBuildPath?: "build.gradle"
            snykTestCommand += " --file=${WORKSPACE}/${gradleBuildPathParam}"
            break;
        default:
            break;
    }
    withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
        sh """
            snyk auth ${TOKEN}
            ${snykTestCommand}
        """
    }
}

def snykIac(Map snykTestArgs=[:]) {
    logger.info("Snyk IAC")
    logger.info("Technology: ${snykTestArgs.tech}")
    logger.info("Testing terraform files...")
    withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
        sh """
            snyk auth ${TOKEN}
            snyk iac test ${snykTestArgs.deploy.workDir} --org=${snykTestArgs.snyk.orgId} --report || true
        """
    }
}

def snykMonitor(Map snykMonitorArgs=[:]) {
    logger.info("Snyk Monitor")
    logger.debug("snykMonitorArgs: ${snykMonitorArgs}")
    snykMonitorParam = snykMonitorArgs?: ""
    sh "snyk monitor ${snykMonitorParam}"
}