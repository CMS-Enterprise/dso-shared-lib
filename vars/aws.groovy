def roleArn="ADOIAMRole" 
withEnv(["assumeRole=$roleArn"]) {
        sh '''
              aws sts assume-role \
                --role-arn $assumeRole \
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
    
                cp -v .aws-creds $HOME/.aws/credentials
                unset AWS_WEB_IDENTITY_TOKEN_FILE
                
                 aws sts get-caller-identity
             
           '''
}