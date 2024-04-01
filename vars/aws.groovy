def assumeRole(String ADOIAMRole)  {
  if(!ADOIAMRole?.trim()) {
    logger.info("No IAM Role defined")
    return
  } else {
    withEnv(["iamrole=$ADOIAMRole"]) {
      sh '''
        aws sts assume-role \
          --role-arn $iamrole \
          --role-session-name session \
          --output text \
          --query Credentials \
          > /tmp/role-creds.txt

        echo [default] >> .aws-creds
        echo  aws_access_key_id = $(cut -f1 /tmp/role-creds.txt)  >> .aws-creds
        echo  aws_secret_access_key = $(cut -f3 /tmp/role-creds.txt)  >> .aws-creds
        echo  aws_session_token = $(cut -f4 /tmp/role-creds.txt)  >> .aws-creds
        
        cp -v .aws-creds $HOME/.aws/credentials
        unset AWS_WEB_IDENTITY_TOKEN_FILE

        aws sts get-caller-identity
      '''
    }
  }
}