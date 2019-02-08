node('uplift') {

    // standard uplift configuration
    def localGitRepoPath  = env.UPLF_JENKINS_GIT_REPO_PATH;

    def gitRepo   = env.UPLF_JENKINS_GIT_REPO_URL    ?: "https://github.com/SubPointSolutions/uplift-packer.git";
    def gitBranch = env.UPLF_JENKINS_GIT_REPO_BRANCH ?: "master";

    def envOverridePath = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;
    def upliftWorkingDir = localGitRepoPath == null ? '.' : localGitRepoPath;

    // additional options
    env.UPLF_VAGRANT_DC_BOX_NAME = "uplift-local/win-2016-datacenter-soe-latest-$gitBranch"

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

    stage('vagrant dc up') {
        dir("$upliftWorkingDir/tests") {
            sh "vagrant up --provision"    
        }
    }
}