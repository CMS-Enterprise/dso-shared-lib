def init(Map deployArgs=[:]) {
    sh """
        mkdir -p ~/.aws
        cp -v .aws-creds ~/.aws/credentials
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        apk add --no-cache git gpgv bash curl
        git clone https://github.com/tfutils/tfenv.git ~/.tfenv
        export BASHLOG_COLOURS=0
        ln -s ~/.tfenv/bin/* /usr/local/bin
        echo "trust-tfenv: yes" > ~/.tfenv/use-gpgv
    """

    sh """ 
        terraform -chdir=${deployArgs.deploy.workDir} init -no-color -backend-config=${deployArgs.deploy.backendConfigFile}
    """
}

def plan(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        terraform -chdir=${deployArgs.deploy.workDir} plan -var-file=${deployArgs.deploy.tfVar} -no-color -out=tfplan ${deployArgs.buildArgs}
    """
}

def test(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        terraform -chdir=${deployArgs.deploy.workDir} validate -no-color ${deployArgs.testArgs}
        terraform -chdir=${deployArgs.deploy.workDir} test -no-color ${deployArgs.testArgs}
      """
}

def apply(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        terraform -chdir=${deployArgs.deploy.workDir} apply -no-color tfplan ${deployArgs.buildArgs}
    """
}

def destroy(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        terraform -chdir=${deployArgs.deploy.workDir} destroy -no-color -var-file=${deployArgs.deploy.tfVar} --auto-approve
    """
}

// def init(Map deployArgs=[:]) {
//     logger.info("terraform init ")
//     logger.debug("deployArgs: ${deployArgs}")
//     sh "terraform -chdir=${deployArgs.backendConfigFile} init -backend-config=${deployArgs.backendConfigFile} -no-color"
// }

// def plan(Map deployArgs=[:]) {
//     logger.info("terraform plan")
//     logger.debug("deployArgs: ${deployArgs}")
//     sh "aws sts get-caller-identity"
//     AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
//     AWSCRED = readJSON text: "${AWSCRED}"
//     sh """
//         export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
//         export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
//         export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
//         aws sts get-caller-identity
//         terraform -chdir=${WORKSPACE}/${deployArgs.backendConfigFile} plan -var-file=${WORKSPACE}/${deployArgs.tfVar} -no-color
//     """
// }

// def apply(Map deployArgs=[:]) {
//     logger.info("terraform apply")
//     logger.debug("deployArgs: ${deployArgs}")
//     sh "aws sts get-caller-identity"
//     AWSCRED = sh (script: "aws sts assume-role --role-arn ${deployArgs.awsRoleArn} --role-session-name AWSCLI-Session", returnStdout: true)
//     AWSCRED = readJSON text: "${AWSCRED}"
//     // Current command also contains "sleep" and "destroy" command for testing purposes
//     // TODO: Remove before release
//     sh """
//         export AWS_ACCESS_KEY_ID=${AWSCRED.Credentials.AccessKeyId}
//         export AWS_SECRET_ACCESS_KEY=${AWSCRED.Credentials.SecretAccessKey}
//         export AWS_SESSION_TOKEN=${AWSCRED.Credentials.SessionToken}
//         aws sts get-caller-identity
//         terraform -chdir=${WORKSPACE}/${deployArgs.backendConfigFile} apply -var-file=${WORKSPACE}/${deployArgs.tfVar} --auto-approve -no-color
//         sleep 30
//         terraform -chdir=${WORKSPACE}/${deployArgs.backendConfigFile} destroy -var-file=${WORKSPACE}/${deployArgs.tfVar} --auto-approve -no-color
//     """
// }