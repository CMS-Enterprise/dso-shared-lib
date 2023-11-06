def scan () {
    echo"scanning"
}

def digestParameters(Map properties=[:]) {
    // def moduleSettingsBranch
    // if (env.devopsDebugOverridesModuleSettingsBranch){ moduleSettingsBranch = devopsDebugOverridesModuleSettingsBranch }
    // else { moduleSettingsBranch = "develop" }
    // dir('../cloudbees-sonarqube-settings') {
    //     git branch: "${moduleSettingsBranch}",
    //         credentialsId: "${env.bitbucketCredentialId}",
    //         url: "${env.bitbucketUrl}/scm/ce/cloudbees-module-settings.git"
    // }
    def defaultMap = libraryResource "sonarqube/defaults.yaml"
    if(defaultMap.sonarqube."${properties.tech}"){
      logger.debug("Technology specific defaults exist. Adding to default Map")
      logger.debug("Technology: defaultMap.${properties.tech}")
      defaultMap.sonarqube."${properties.tech}".each { entry -> defaultMap.sonarqube["${entry.key}"] = "${entry.value}" }
    }
    // Does this work?? filepath?
    def projectMap = libraryResource "sonarqube/${properties.ghOrg}.yaml"
    if(fileExists(projectMap)) {
      logger.debug("Project file: ${properties.ghOrg} exists proceeding to read file.")
      // def projectMap = readYaml file: "../cloudbees-sonarqube-settings/${properties.ghOrg}.yaml"
      if(projectMap.sonarqube){
        logger.debug("Project level overrides exists, overwritting... ")
        projectMap.sonarqube.each { entry -> defaultMap.sonarqube["${entry.key}"] = "${entry.value}" }
      }
      if(projectMap."${properties.appName}"){
        logger.debug("Repo level overrides exists, overwritting... ")
        projectMap."${properties.appName}".each { entry -> defaultMap.sonarqube["${entry.key}"] = "${entry.value}" }
      }
    }
    else {
      logger.debug("No Project File: So skipping the procject level override")
    }
    //Convert Yaml Maps to map of params that can be passed to CLI
    def paramBuilder = " -Dsonar.host.url=${env.sonarQubeUrl}"
    paramBuilder += defaultMap.sonarqube?.reportPath          ? " ${defaultMap.sonarqube.reportPath}"                                        : ""
    paramBuilder += defaultMap?.javaVersion                   ? " -Dsonar.java.binaries=${defaultMap.sonarqube.javaVersion}"                 : " -Dsonar.java.binaries=."
    paramBuilder += defaultMap.sonarqube?.source              ? " -Dsonar.source='${defaultMap.sonarqube.source}'"                           : ""
    paramBuilder += defaultMap.sonarqube?.exclusions          ? " -Dsonar.exclusions='${defaultMap.sonarqube.exclusions}'"                   : ""
    paramBuilder += defaultMap.sonarqube?.encoding            ? " -Dsonar.sourceEncoding='${defaultMap.sonarqube.encoding}'"                 : ""
    paramBuilder += defaultMap.sonarqube?.tests               ? " -Dsonar.tests='${defaultMap.sonarqube.tests}'"                             : ""
    paramBuilder += defaultMap.sonarqube?.language            ? " -Dsonar.language='${defaultMap.sonarqube.language}'"                       : ""
    paramBuilder += defaultMap.sonarqube?.junitReportsPath    ? " -Dsonar.junit.reportsPath='${defaultMap.sonarqube.junitReportsPath}'"      : ""
    paramBuilder += defaultMap.sonarqube?.junitInclude        ? " -Dsonar.junit.include='${defaultMap.sonarqube.junitInclude}'"              : ""
    paramBuilder += defaultMap.sonarqube?.coverageReportPaths ? " -Dsonar.coverageReportPaths='${defaultMap.sonarqube.coverageReportPaths}'" : ""
    paramBuilder += defaultMap.sonarqube?.swiftSimulator      ? " -Dsonar.swift.simulator='${defaultMap.sonarqube.swiftSimulator}'"          : ""
    paramBuilder += defaultMap.sonarqube?.swiftWorkspace      ? " -Dsonar.swift.workspace='${defaultMap.sonarqube.swiftWorkspace}'"          : ""
    paramBuilder += defaultMap.sonarqube?.swiftAppName        ? " -Dsonar.swift.appName='${defaultMap.sonarqube.swiftAppName}'"              : ""
    paramBuilder += defaultMap.sonarqube?.swiftAppScheme      ? " -Dsonar.swift.appScheme='${defaultMap.sonarqube.swiftAppScheme}'"          : ""
    paramBuilder += defaultMap.sonarqube?.qualityGateWaitTimeout      ? " -Dsonar.qualitygate.timeout='${defaultMap.sonarqube.qualityGateWaitTimeout}'"          : ""
    paramBuilder += defaultMap.sonarqube?.additionalParams    ? "${defaultMap.sonarqube.additionalParams}"                                   : ""
    paramBuilder += " -Dsonar.projectVersion='${env.BUILD_NUMBER}'"
    return paramBuilder
}
def scan(Map properties=[:]) {
  def sonarqubeParams = digestParameters(properties)
  logger.debug("SonarQube Scan Triggered")
  withCredentials([string(credentialsId: "${env.sonarQubeCredentialId}", variable: 'TOKEN')]) {
                sh "sonar-scanner -Dsonar.login=${TOKEN} \
    -Dsonar.projectName=${properties.sonarqube.projectKey} \
    -Dsonar.projectKey=${properties.sonarqube.projectKey} \
    -Dsonar.qualitygate.wait=true \
    ${sonarqubeParams}"
  }
}
def sonarqubeURL() {
  def getURL = readProperties file: './.scannerwork/report-task.txt'
  def sonarqubeURL = "${getURL['dashboardUrl']}"
  echo "SonarQubeURL for Report: ${sonarqubeURL }"
  env.SonarReport = "${sonarqubeURL}" ?: " "
}