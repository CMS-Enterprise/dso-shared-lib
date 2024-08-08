def jmeterTest(Map testArgs=[:]) {
    logger.info("Running JMeter tests")
    sh """
        jmeter -n -t ${testArgs.pathToTest} -l ${testArgs.pathToTest}_result.jtl
        cat ${testArgs.pathToTest}_result.jtl
    """
}