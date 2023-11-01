@Library("dso-shared-lib@main") _
def podYaml = libraryResource "podTemplates/base.yaml"

pipeline{
    agent{
        kubernetes {
            yaml podYaml
        }
    }
    stages {
        stage("init"){
            steps{
                conatiner ("base-agent"){
                    script{
                        init()
                    }
                }
            }
        }
        stage("create new controller"){
            steps{
                conatiner ("base-agent"){
                    script{
                        git.clone("https://github.com/CMS-Enterprise/dso-contrl-casc.git", "main")
                        git.clone("https://github.com/CMS-Enterprise/dso-cjoc-casc.git", "main")
                        cloudbees.determineController()
                        cloudbees.createNewController()
                        cloudbees.validateControllerCasc()
                        git.push("dso-contrl-casc", "main", "creating new controller")
                        cloudbees.appendControllerToCjoc()
                        cloudbees.validateCjocCasc()
                        git.push("dso-cjoc-casc", "main", "creating new controller on cjoc")
                        cloudbees.cjocConfigMapUpdate()
                        cloudbees.reloadCjoc()
                    }
                }
            }
        }
    }
    post{
        success (
            conatiner ("base-agent"){
                script{
                    notification.success()
                }
            }
        )
        failure (
            conatiner ("base-agent"){
                script{
                    notification.failure()
                }
            }
        )
    }
}