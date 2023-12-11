def failure(Map properties) {
  /*
        Function to validate if email and MS Teams webhook are provided, and frame Email subject and body
  */
    // if(!(env.EMAIL_NOTIFICATION || env.EMAIL_NOTIFICATION ==~ /(?i)(false)/)) {
    if (properties.email) {
        def subject = "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' encountered a problem"
        // def template = '''${SCRIPT, template="failure.template"}'''
        def template = "Your Job Failed"
        sendEmail(properties.email,subject,template)
    }
    // if(!(env.MSTEAMS_WEBHOOK ==~ /(?i)(false)/)){
    if (properties.MSTEAMS_WEBHOOK) {
        def colorCode = '#FF0000'
        def subject = "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
        sendMSTeams(subject, "FAILURE", colorCode)
    }
}
def success(Map properties) {
  /*
        Function to validate if email and MS Teams webhook are provided, and frame Email subject and body
  */
    // if(!(env.EMAIL_NOTIFICATION || env.EMAIL_NOTIFICATION ==~ /(?i)(false)/)) {
    if (properties.email) {
        def subject = "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' "
        // def template = '''${SCRIPT, template="success.template"}'''
        def template = "Your Job was Successful"
        sendEmail(subject,template)
    }
    // if(!(env.MSTEAMS_WEBHOOK ==~ /(?i)(false)/)){
    if (properties.MSTEAMS_WEBHOOK) {
        def colorCode = '#FF0000'
        def subject = "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
        sendMSTeams(subject, "SUCCESS", colorCode)
    }
}

def jiraFailure() {
  /*
        Function to validate if email and MS Teams webhook are provided, and frame Email subject and body for jira failure
        Input:
            properties: metadata from cloudbees.properties
            msg: Jira transition error message
  */
    if(!(env.EMAIL_NOTIFICATION ==~ /(?i)(false)/)) {
        def subject = "ACTION: Update ${env.JIRA_STORIES} workflow for ${env.JOB_NAME}"
        def template = '''${SCRIPT, template="jiraFailure.template"}'''
        sendEmail(subject,template)
    }
    // if(!(env.MSTEAMS_WEBHOOK ==~ /(?i)(false)/)) {
    if (properties.MSTEAMS_WEBHOOK) {
        def colorCode = '#000000'
        def subject = "ACTION: Update ${env.JIRA_STORIES} workflow for ${env.JOB_NAME}"
        sendMSTeams(subject, "ACTION REQUIRED", colorCode)
    }
}
def sendEmail(def email, def subject, def details) {
  /*
        Function to send email
        Input:
            subject: Email subject
            details: Email body
            email: Recipient
  */
    emailext (
        to: "${email}",
        mimeType: 'text/html',
        subject: subject,
        body: details
    )
}
def sendMSTeams(def msTeamsMsg, def bStatus, def colorCode) {
  /*
        Function to send MSTeams notification
        Input:
            colorCode: colour to be displayed in message
            msTeamsMsg: Message to be displayed
            bStatus: Build Status
            teamsWebHook: Webhook url to send the notification
  */
  logger.info("sending MSTeam notification")
//   office365ConnectorSend (
//     color: colorCode,
//                                 message: msTeamsMsg,
//                                 status: bStatus,
//                                 webhookUrl: "${env.MSTEAMS_WEBHOOK}"
//                 )
}