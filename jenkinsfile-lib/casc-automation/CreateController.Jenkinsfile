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
                        git.clone("dso-contrl-casc")
                        git.clone("dso-cjoc-casc")
                        cloudbees.determineController()
                        cloudbees.createNewController()
                        cloudbees.validateControllerCasc()
                        git.push("dso-contrl-casc")
                        cloudbees.appendControllerToCjoc()
                        cloudbees.validateCjocCasc()
                        git.push("dso-cjoc-casc")
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