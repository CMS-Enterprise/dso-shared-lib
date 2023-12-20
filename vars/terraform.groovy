def init(Map deployArgs=[:]) {
    logger.info("terraform init ")
    logger.debug("deployArgs: ${deployArgs}")
    sh "terraform init -backend-config=${deployArgs.backendConfigFile}"
}

def plan(Map deployArgs=[:]) {
    logger.info("terraform plan")
    logger.debug("deployArgs: ${deployArgs}")
    sh "aws sts get-caller-identity"
    AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
    AWSCRED = readJSON text: "${AWSCRED}"
    sh """
        export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
        export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
        export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
        aws sts get-caller-identity
        terraform plan -var-file=${deployArgs.tfVar}
    """
}

def apply(Map deployArgs=[:]) {
    logger.info("terraform apply")
    logger.debug("deployArgs: ${deployArgs}")
    sh "aws sts get-caller-identity"
    AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
    AWSCRED = readJSON text: "${AWSCRED}"
    sh """
        export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
        export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
        export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
        aws sts get-caller-identity
        terraform apply -var-file=${deployArgs.tfVar} --auto-approve
        sleep 300
        terraform destroy -var-file=${deployArgs.tfVar} --auto-approve
    """
}