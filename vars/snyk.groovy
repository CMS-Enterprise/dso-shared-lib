def snykCodeTest(Map snykCodeTestArgs=[:]) {
    logger.info("Snyk Code Test")
    logger.debug("snykCodeTestArgs: ${snykCodeTestArgs}")
    snykCodeTestParam = snykCodeTestArgs?: ""
    sh "snyk code test ${snykCodeTestParam}"
}

def snykTest(Map snykTestArgs=[:]) {
    logger.info("Snyk Test")
    logger.debug("snykTestArgs: ${snykTestArgs}")
    gradleBuildPathParam = snykTestArgs.snyk.gradleBuildPath?: ""
    withCredentials([string(credentialsId: "snyk-sa-token", variable: "TOKEN")]) {
        sh """
            snyk auth ${TOKEN}
            snyk test --org=${snykTestArgs.snyk.orgId} --file=${WORKSPACE}/${gradleBuildPathParam} --json
        """
    }
}

def snykMonitor(Map snykMonitorArgs=[:]) {
    logger.info("Snyk Monitor")
    logger.debug("snykMonitorArgs: ${snykMonitorArgs}")
    snykMonitorParam = snykMonitorArgs?: ""
    sh "snyk monitor ${snykMonitorParam}"
}