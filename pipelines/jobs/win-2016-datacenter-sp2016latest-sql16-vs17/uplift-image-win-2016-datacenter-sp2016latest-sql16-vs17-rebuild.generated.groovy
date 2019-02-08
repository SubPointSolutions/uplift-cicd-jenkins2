node('uplift') {

    // standard uplift configuration
    def localGitRepoPath  = env.UPLF_JENKINS_GIT_REPO_PATH;

    def gitRepo   = env.UPLF_JENKINS_GIT_REPO_URL    ?: "https://github.com/SubPointSolutions/uplift-packer.git";
    def gitBranch = env.UPLF_JENKINS_GIT_REPO_BRANCH ?: "master";

    def envOverridePath = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;
    def upliftWorkingDir = localGitRepoPath == null ? '.' : localGitRepoPath;

    // packer image to build, fetched from the job parameters
    def imageName    = "win-2016-datacenter-sp2016latest-sql16-vs17"
    def imageTask    = "PackerRebuild"
    def inputBoxName = "uplift-local/win-2016-datacenter-app-sql16-$gitBranch"

    // additional options
    env.UPLF_JENKINS_IMAGE_NAME = imageName;
    env.UPLF_JENKINS_IMAGE_TASK = imageTask;
    env.UPLF_JENKINS_IMAGE_INPUT_BOX = inputBoxName;
    
    // load env override if exists
    if(envOverridePath?.trim() && fileExists(envOverridePath)) {
        echo "Loading environment override file: $envOverridePath"
        load envOverridePath
    }

    // promoting to uplift
    env.UPLF_INPUT_BOX_NAME = env.UPLF_JENKINS_IMAGE_INPUT_BOX; 

    try {
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
    } finally {

         stage('Artifacts') {
            dir(upliftWorkingDir) {
                
                def imageArtifactFolder = "build-packer-ci-local/$imageName-$gitBranch"

                archiveArtifacts  artifacts: "$imageArtifactFolder/box-spec/*.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/.release-notes.md", allowEmptyArchive: true
        
                archiveArtifacts  artifacts: "$imageArtifactFolder/packer.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/variables.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/.build-container.json", allowEmptyArchive: true
        
                archiveArtifacts  artifacts: "$imageArtifactFolder/Vagrantfile", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/.vagrant-*.ps1", allowEmptyArchive: true
        
                archiveArtifacts  artifacts: "$imageArtifactFolder/logs/*.log", allowEmptyArchive: true 

            }
        }
        
    }
}
