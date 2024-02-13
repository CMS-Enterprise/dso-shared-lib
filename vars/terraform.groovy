def init(Map deployArgs=[:]) {
    logger.info("terraform init ")
    logger.debug("deployArgs: ${deployArgs}")
    sh """
        apk add --no-cache git gpg awscli
        git clone https://github.com/tfutils/tfenv.git ~/.tfenv
        echo "export PATH="$HOME/.tfenv/bin:$PATH" >> ~/.profile
        echo "trust-tfenv: yes" > ~/.tfenv/use-gpgv
        terraform -chdir=${deployArgs.deploy.backendConfigFile} init -backend-config=${deployArgs.deploy.backendConfigFile}
    """
}

def plan(Map deployArgs=[:]) {
    logger.info("terraform plan")
    logger.debug("deployArgs: ${deployArgs}")
    sh "aws sts get-caller-identity"
    AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.deploy.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
    AWSCRED = readJSON text: "${AWSCRED}"
    sh """
        export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
        export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
        export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        aws sts get-caller-identity
        terraform -chdir=${WORKSPACE}/${deployArgs.deploy.backendConfigFile} plan -var-file=${WORKSPACE}/${deployArgs.deploy.tfVar} ${deployArgs.buildArgs}
    """
}

def test(Map deployArgs=[:]) {
    logger.info("terraform test")
    logger.debug("deployArgs: ${deployArgs}")
    sh "aws sts get-caller-identity"
    AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.deploy.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
    AWSCRED = readJSON text: "${AWSCRED}"
    sh """
        export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
        export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
        export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        aws sts get-caller-identity
        terraform -chdir=${WORKSPACE}/${deployArgs.deploy.backendConfigFile} fmt -check -var-file=${WORKSPACE}/${deployArgs.deploy.tfVar} ${deployArgs.testArgs}
        terraform -chdir=${WORKSPACE}/${deployArgs.deploy.backendConfigFile} validate -var-file=${WORKSPACE}/${deployArgs.deploy.tfVar} ${deployArgs.testArgs}
        terraform -chdir=${WORKSPACE}/${deployArgs.deploy.backendConfigFile} test -var-file=${WORKSPACE}/${deployArgs.deploy.tfVar} ${deployArgs.testArgs}
      """
}

def apply(Map deployArgs=[:]) {
    logger.info("terraform apply")
    logger.debug("deployArgs: ${deployArgs}")
    sh "aws sts get-caller-identity"
    AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.deploy.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
    AWSCRED = readJSON text: "${AWSCRED}"
    // Current command also contains "sleep" and "destroy" command for testing purposes
    // TODO: Remove before release
    sh """
        export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
        export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
        export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        aws sts get-caller-identity
        terraform -chdir=${WORKSPACE}/${deployArgs.deploy.backendConfigFile} apply -var-file=${WORKSPACE}/${deployArgs.deploy.tfVar} ${deployArgs.buildArgs}--auto-approve
        sleep 30
        terraform -chdir=${WORKSPACE}/${deployArgs.deploy.backendConfigFile} destroy -var-file=${WORKSPACE}/${deployArgs.deploy.tfVar} --auto-approve
    """
}
