node('uplift') {

    // standard uplift configuration
    def localGitRepoPath  = env.UPLF_JENKINS_GIT_REPO_PATH;

    def gitRepo   = env.UPLF_JENKINS_GIT_REPO_URL    ?: "https://github.com/SubPointSolutions/uplift-packer.git";
    def gitBranch = env.UPLF_JENKINS_GIT_REPO_BRANCH ?: "master";

    def envOverridePath = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;
    def upliftWorkingDir = localGitRepoPath == null ? '.' : localGitRepoPath;

    // packer image to build 
    def  imageName = "win-2016-datacenter-sp2016fp1-sql16-vs17"
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

    stage(imageTask) {
        dir(upliftWorkingDir) {
            sh "pwsh -c 'invoke-build -packerImageName $imageName -Task $imageTask' "    
        }
    }
}
