def jmeterTest(Map testArgs=[:]) {
    logger.info("Running JMeter tests")
    sh """
        jmeter -n -t ${testArgs.pathToTest} -l ${testArgs.pathToTest}.result
        cat ${testArgs.pathToTest}.result
    """
}