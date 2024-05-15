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
        cd ${deployArgs.appName}
        terraform -chdir=${deployArgs.deploy.workDir} init -no-color -backend-config=${deployArgs.deploy.backendConfigFile}
    """
}

def plan(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE

        cd ${deployArgs.appName}
        terraform -chdir=${deployArgs.deploy.workDir} plan -var-file=${deployArgs.deploy.tfVar} -no-color -out=tfplan ${deployArgs.buildArgs}
    """
}

def test(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        
        cd ${deployArgs.appName}
        terraform -chdir=${deployArgs.deploy.workDir} validate -no-color ${deployArgs.testArgs}
        terraform -chdir=${deployArgs.deploy.workDir} test -no-color  ${deployArgs.testArgs}
      """
}

def apply(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        cd ${deployArgs.appName}
        terraform -chdir=${deployArgs.deploy.workDir} apply -no-color  tfplan ${deployArgs.buildArgs}
    """
}

def destroy(Map deployArgs=[:]) {
    sh """
        unset AWS_WEB_IDENTITY_TOKEN_FILE
        cd ${deployArgs.appName}
        terraform -chdir=${deployArgs.deploy.workDir} destroy -no-color -var-file=${deployArgs.deploy.tfVar} --auto-approve
    """
}
