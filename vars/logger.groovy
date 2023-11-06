def info(String msg) {
    /*
        Function to print pipeline info messages in a digestable way for splunk. Retrieves env
        variables JIRA_STORIES and GIT_COMMIT
        Input:
            msg: Some type of process indicator; Build Started, Build Completed, etc..
    */
    def infoMsg = logMsgBuilder(msg)
    echo "[INFO] - ${infoMsg}"
}
def error(String msg) {
    /*
        Function to print pipeline error messages in a digestable way for splunk. Retrieves env
        variables JIRA_STORIES and GIT_COMMIT
        Input:
            msg: Some type of process indicator; Build Failed, checkout failed, etc..
    */
    def errorMsg = logMsgBuilder(msg)
    echo "[ERROR] - ${errorMsg}"
}
def debug(String msg) {
    /*
        Function to print pipeline DEBUG messages in a digestable way for splunk. Retrieves global
        variable DEBUG and check if DEBUG set to true or false. If true log debug message.
        Input:
            msg: Some type of process indicator
    */
    if (env.DEBUG.toUpperCase() == 'TRUE') {
        echo "[DEBUG] - ${msg}"
    }
}
def warning(String msg) {
    /*
        Function to print pipeline WARNING messages in a digestable way for splunk. Retrieves env
        variables JIRA_STORIES and GIT_COMMIT
        Input:
            msg: Some type of process indicator
    */
    def warningMsg = logMsgBuilder(msg)
    echo "[WARNING] - ${warningMsg}"
}
def stage(String msg=null) {
    /*
        Function to print new pipeline process separator for console output readability.
        Input:
            msg: Some type of process indicator; Source Checkout, BUILD, etc..
            A stage log can be a custom message or default to Stage Name/Title
    */
    def logMsg = msg ?: env.STAGE_NAME
    echo "--------------------------- ${logMsg} ---------------------------"
}
def logMsgBuilder(String msg){
    /*
        Function to build log message. Retrieves env variables JIRA_STORIES and GIT_COMMIT,
        if exists will be included in log message.
        Input:
            msg: Some type of process indicator
    */
    //String logMessage = env.JIRA_STORIES ? "${env.JIRA_STORIES} - " : '';
    //logMessage       += env.GIT_COMMIT   ? "${env.GIT_COMMIT} - "   : '';
    logMessage       = "${msg}"
    return logMessage
}