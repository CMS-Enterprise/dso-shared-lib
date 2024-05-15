def clone(String REPO_URL, String BRANCH) {
    /*
        Function will execute a git clone in current directory
        Input:
            REPO_URL: Repo
            Branch: Repo-branch
    */
    checkout scm: [$class: 'GitSCM', branches: [[name: "*/${BRANCH}"]], userRemoteConfigs: [[credentialsId: "git_token", url: "${REPO_URL}"]]]
}
def push(String REPO, String BRANCH, String MESSAGE) {
    /*
        Function will execute a git commit & push 
        Input:
            REPO_URL: Repo
            Branch: Repo-branch
    */
    dir("${REPO}"){
        sh """
            git config --global user.email "GIT.SVCACCT EMAIL"
            git config --global user.name "GIT SERVCACCT"
            git config --global --add safe.directory $WORKSPACE
            git add .
            git diff --quiet && git diff --staged --quiet || ( git commit -m "${MESSAGE}" && git push origin ${BRANCH} )
        """
    }
}