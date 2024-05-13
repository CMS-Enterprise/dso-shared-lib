def assumeRole(String ADOIAMRole)  {
  if(!ADOIAMRole?.trim()) {
    logger.info("No IAM Role defined")
    return
  } else {
    logger.info("Assuming AWS role...")
    withEnv(["iamrole=$ADOIAMRole"]) {
      sh '''
        aws sts assume-role \
          --role-arn $iamrole \
          --role-session-name session \
          --output text \
          --query Credentials \
          > /tmp/role-creds.txt

        [default] >> .aws-creds
        aws_access_key_id=$(cut -f1 /tmp/role-creds.txt)  >> .aws-creds
        aws_secret_access_key=$(cut -f3 /tmp/role-creds.txt)  >> .aws-creds
        aws_session_token=$(cut -f4 /tmp/role-creds.txt)  >> .aws-creds
        
        cp -v .aws-creds $HOME/.aws/credentials
        unset AWS_WEB_IDENTITY_TOKEN_FILE

        aws sts get-caller-identity
      '''
    }
  }
}