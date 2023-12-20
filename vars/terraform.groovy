def init(Map deployArgs=[:]) {
    // withCredentials([file(credentialsId: "", variable: "FILE")]) {
    //     sh "cp \$FILE /usr/share/maven/conf/settings.xml"
    // }
    logger.info("terraform init ")
    logger.debug("deployArgs: ${deployArgs}")
    sh "terraform init -backend-config=${deployArgs.backendConfigFile}"
}

def plan(Map deployArgs=[:]) {
    logger.info("terraform plan")
    logger.debug("deployArgs: ${deployArgs}")
    sh "terraform plan -var-file=${deployArgs.tfVar}" 
}

def apply(Map deployArgs=[:]) {
    logger.info("terraform apply")
    logger.debug("deployArgs: ${deployArgs}")
    sh "terraform apply -var-file=${deployArgs.tfVar}"
}