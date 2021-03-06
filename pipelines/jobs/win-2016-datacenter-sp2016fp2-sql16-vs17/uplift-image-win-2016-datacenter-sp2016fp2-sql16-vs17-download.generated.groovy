node('uplift') {

    // standard uplift configuration
    def defaultRepoPath = isUnix() ? "~/uplift-local-repository" : "c:/uplift-local-repository"
    def repoPath        = env.UPLF_JENKINS_LOCAL_REPOSITORY_PATH ?: defaultRepoPath;
    
    def envOverridePath = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;
    def invokeUpliftOptions = '-d'

    def resources = [
        "ms-visualstudio-2017.ent-installer",
		"ms-visualstudio-2017.ent-dist-office-dev",
		"ms-sharepoint2016-rtm",
		"ms-sharepoint2016-lang-pack",
		"ms-sharepoint2016-fp2"
    ]

    // packer image to build, fetched from the job parameters
    def imageName    = "win-2016-datacenter-sp2016fp2-sql16-vs17"
    def imageTask    = ""
   
    // additional options
    env.UPLF_JENKINS_IMAGE_NAME = imageName;
    env.UPLF_JENKINS_IMAGE_TASK = imageTask;

    // load env override if exists
    def envOverrideExists = fileExists "$envOverridePath"
    if(envOverrideExists) {
        echo "Loading environment override file: $envOverridePath"
        load envOverridePath

        repoPath = env.UPLF_JENKINS_LOCAL_REPOSITORY_PATH ?: repoPath;
        invokeUpliftOptions = env.UPLF_JENKINS_INVOKE_UPLIFT_OPTIONS ?: invokeUpliftOptions;
    }

    stage('resource validate-uri') {
        resources.each {
            def resourceName = it

            task resourceName
            sh "pwsh -c 'uplift resource validate-uri $resourceName' "
        }
    }

    stage("resource download") {
        resources.each {
            def resourceName = it

            task resourceName
            sh "pwsh -c 'uplift resource download ${resourceName} -repository $repoPath $invokeUpliftOptions' "
        }
    }
}
