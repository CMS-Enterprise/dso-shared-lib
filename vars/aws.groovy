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

          cat > .aws-creds <<EOF
[default]
aws_access_key_id = $(cut -f1 /tmp/role-creds.txt)
aws_secret_access_key = $(cut -f3 /tmp/role-creds.txt)
aws_session_token = $(cut -f4 /tmp/role-creds.txt)
EOF
      '''
    }
  }
}