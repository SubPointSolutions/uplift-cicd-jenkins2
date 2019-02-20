node('uplift') {

    def jobName = env.JOB_NAME.toLowerCase();

    // standard uplift configuration
    def localGitRepoPath  = env.UPLF_JENKINS_GIT_REPO_PATH;

    def gitRepo   = env.UPLF_JENKINS_GIT_REPO_URL    ?: "https://github.com/SubPointSolutions/uplift-packer.git";
    def gitBranch = env.UPLF_JENKINS_GIT_REPO_BRANCH ?: "master";

    def envOverridePath = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;
    def upliftWorkingDir = localGitRepoPath == null ? '.' : localGitRepoPath;

    // packer image to build 
    def  imageName = "win-2016-datacenter-app-sql16-latest"
    def  imageTask = "VagrantBoxTest"

    // additional options
    env.UPLF_JENKINS_IMAGE_NAME = imageName;
    env.UPLF_JENKINS_IMAGE_TASK = imageTask;

    env.UPLF_LOG_NO_COLOR = "1"

    // load env override if exists
    if(fileExists(envOverridePath)) {
        echo "Loading environment override file: $envOverridePath"
        load envOverridePath
    }

    stage('Checkout') {
        if(localGitRepoPath == null) {
            echo "Using git repo:  $gitRepo"
            git branch: gitBranch, url: gitRepo
        } else {
            echo "Using local src: $localGitRepoPath"
        }
    }

    stage('Shared Infra') {
        // ensure that shared dc is up for the win-based image testing
        if(jobName.startsWith("uplift-image-win-2016-")) {
            build job: 'uplift-test-win-2016-datacenter-dc-shared-up'              
        } else {
            echo "n/a for the current job: $jobName"
        }
    }
    try {
        stage(imageTask) {
            dir(upliftWorkingDir) {
                sh "pwsh -c 'invoke-build -packerImageName $imageName -Task $imageTask' "    
            }
        }
    } finally {

         stage('Artifacts') {
            dir(upliftWorkingDir) {
                
                def imageArtifactFolder = "build-packer-ci-local/$imageName-$gitBranch"

                archiveArtifacts  artifacts: "$imageArtifactFolder/.build-container.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/Vagrantfile", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/.vagrant-*.ps1", allowEmptyArchive: true

            }
        }
        
    }
}
