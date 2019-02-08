node('uplift') {

    def repoPath = env.UPLF_JENKINS_LOCAL_REPOSITORY_PATH ?: "c:/uplift-local-repository";

    def resources = [
        "7z-",
        "hashicorp-",
        "jetbrains-",
        "jetbrains-",
        "ms-dynamics-",
        "ms-sharepoint2013-",
        "ms-sharepoint2016-",
        "ms-sharepoint-",
        "ms-sql-",
        "ms-visualstudio-2013",
        "ms-visualstudio-2015",
        "ms-visualstudio-2017.ent-installer",
        "ms-win2012r2-kb",
        "ms-win-2016-iso",
        "ms-win2016-lcu",
        "ms-win2016-ssu",
        "oracle-"
    ]

    stage('resource list') {
        sh "pwsh -c 'uplift resource list' "
    }

    resources.each {
        def resourceName = it

        stage("download ${resourceName}") {
            sh "pwsh -c 'uplift resource download ${resourceName} -r $repoPath -d' "
        }
    }
}