def assumeRole(String ADOIAMRole) {
    env.ADOIAMRole = ADOIAMRole

    sh """
        echo "Role ARN: ${env.ADOIAMRole}"
        aws sts assume-role \\
        --role-arn "${env.ADOIAMRole}" \\
        --role-session-name session \\
        --output text \\
        --query Credentials \\
        > /tmp/role-creds.txt

        aws sts get-caller-identity

        cat > .aws-creds <<EOF
[default]
aws_access_key_id = $(cut -f1 /tmp/role-creds.txt)
aws_secret_access_key = $(cut -f3 /tmp/role-creds.txt)
aws_session_token = $(cut -f4 /tmp/role-creds.txt)
EOF
    """
}