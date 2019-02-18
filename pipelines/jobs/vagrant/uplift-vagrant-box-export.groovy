node('uplift') {

    def jobName = env.JOB_NAME.toLowerCase();

    // standard uplift configuration
    def envOverridePath   = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;

    // task options
    def buildTask     = "VagrantExportBoxes"

    // additional options
    env.UPLF_LOG_NO_COLOR = "1"

    // load env override if exists
    if(fileExists(envOverridePath)) {
        echo "Loading environment override file: $envOverridePath"
        load envOverridePath
    }

    def localGitRepoPath  = env.UPLF_JENKINS_GIT_REPO_PATH;

    def gitRepo   = env.UPLF_JENKINS_GIT_REPO_URL    ?: "https://github.com/SubPointSolutions/uplift-contrib";
    def gitBranch = env.UPLF_JENKINS_GIT_REPO_BRANCH ?: "master";


    def upliftWorkingDir = localGitRepoPath == null ? 'uplift-vagrant' : localGitRepoPath + "/uplift-vagrant";

    stage('Checkout') {
        if(localGitRepoPath == null) {
            echo "Using git repo:  $gitRepo"
            git branch: gitBranch, url: gitRepo
        } else {
            echo "Using local src: $localGitRepoPath"
        }
    }

    stage("VagrantExportBoxes") {
        dir(upliftWorkingDir) {
            sh "pwsh -c 'invoke-build -Task $buildTask ' "    
        }
    }
}