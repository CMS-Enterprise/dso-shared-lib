def build(String buildArgs=""){
    logger.info("starting dotnet build")
    sh """
    dotnet ${buildArgs)}
    """
}
def test(String testArgs=""){
    logger.info("starting dotnet test")
    sh """
    dotnet ${testArgs}
    """
}
