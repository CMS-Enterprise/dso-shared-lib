def build(){
    logger.info("starting dotnet build")
    sh """
    dotnet build
    """
}
def test(){
    logger.info("starting dotnet test")
    sh """
    dotnet test
    """
}
