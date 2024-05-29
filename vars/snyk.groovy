def snykCodeTest(Map snykCodeTestArgs=[:]) {
    logger.info("Snyk Code Test")
    withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
        try {
            sh """
                snyk auth ${TOKEN}
                snyk code test --org=${snykCodeTestArgs.snyk.orgId} --json --report
            """
        } catch(Exception ex) {
            logger.info("Snyk Code not enabled in Organization")
        }
    }
}

def snykTest(Map snykTestArgs=[:]) {
    logger.info("Snyk Test")
    logger.info("Technology: ${snykTestArgs.tech}")
    def xrayCommand = "snyk test --org=${snykTestArgs.snyk.orgId} --json"
    def projectTech = "${snykTestArgs.tech}"
    switch(snykTestArgs.tech) {
        case "gradle":
            gradleBuildPathParam = snykTestArgs.snyk.gradleBuildPath?: "build.gradle"
            xrayCommand += " --file=${WORKSPACE}/${gradleBuildPathParam}"
            break;
        default:
            break;
    }
    withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
    sh """
        snyk auth ${TOKEN}
        ${xrayCommand}
    """
    }
}

def snykIac(Map snykMonitorArgs=[:]) {
    logger.info("Snyk IAC")
    logger.info("Technology: ${snykTestArgs.tech}")
    logger.info("Testing terraform files...")
    sh """
        snyk auth ${TOKEN}
        snyk iac test . --json --report
    """
    logger.info("Testing terraform plan output...")
    sh """ 
        snyk iac test tfplan.json --report
    """
}

def snykMonitor(Map snykMonitorArgs=[:]) {
    logger.info("Snyk Monitor")
    logger.debug("snykMonitorArgs: ${snykMonitorArgs}")
    snykMonitorParam = snykMonitorArgs?: ""
    sh "snyk monitor ${snykMonitorParam}"
}