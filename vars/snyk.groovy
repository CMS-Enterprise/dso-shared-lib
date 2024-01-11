def snykCodeTest(Map snykCodeTestArgs=[:]) {
    logger.info("Snyk Code Test")
    logger.debug("snykCodeTestArgs: ${snykCodeTestArgs}")
    snykCodeTestParam = snykCodeTestArgs?: ""
    sh "snyk code test ${snykCodeTestParam}"
}

def snykTest1(Map snykTestArgs=[:]) {
    logger.info("Snyk Test1")
    logger.debug("snykTestArgs: ${snykTestArgs}")
    logger.info("Technology: ${snykTestArgs.tech}")
    def projectTech = "${snykTestArgs.tech}"
    switch(snykTestArgs.tech) {
        case "gradle":
            gradleBuildPathParam = snykTestArgs.snyk.gradleBuildPath?: "build.gradle"
            withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
                sh """
                    snyk auth ${TOKEN}
                    snyk test --org=${snykTestArgs.snyk.orgId} --file=${WORKSPACE}/${gradleBuildPathParam} --json
                """
            }
            break;
        default:
            withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
                sh """
                    snyk auth ${TOKEN}
                    snyk test --org=${snykTestArgs.snyk.orgId} --json
                """
            }
            break;
    }
}

def snykTest(Map snykTestArgs=[:]) {
    logger.info("Snyk Test2")
    logger.debug("snykTestArgs: ${snykTestArgs}")
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

def snykMonitor(Map snykMonitorArgs=[:]) {
    logger.info("Snyk Monitor")
    logger.debug("snykMonitorArgs: ${snykMonitorArgs}")
    snykMonitorParam = snykMonitorArgs?: ""
    sh "snyk monitor ${snykMonitorParam}"
}