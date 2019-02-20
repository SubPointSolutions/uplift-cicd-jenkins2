node('uplift') {

    // standard uplift configuration
    def localGitRepoPath  = env.UPLF_JENKINS_GIT_REPO_PATH;

    def gitRepo   = env.UPLF_JENKINS_GIT_REPO_URL    ?: "https://github.com/SubPointSolutions/uplift-packer.git";
    def gitBranch = env.UPLF_JENKINS_GIT_REPO_BRANCH ?: "master";

    def envOverridePath  = env.UPLF_JENKINS_ENV_OVERRIDE_PATH;
    def upliftWorkingDir = localGitRepoPath == null ? '.' : localGitRepoPath;

    // packer image to build 
    def  imageName = "win-2016-datacenter-app-sql16-sp2-cu2"
    def  imageTask = "VagrantCloudPublish"

    // additional options
    def imageBuildJob = "uplift-image-$imageName-rebuild"
   
    env.UPLF_JENKINS_IMAGE_NAME = imageName;
    env.UPLF_JENKINS_IMAGE_TASK = imageTask;

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

    stage('Artifacts') {
         dir(upliftWorkingDir) {
            // copy release notes file from the latest successful
            // https://jenkins.io/doc/pipeline/steps/copyartifact/
            copyArtifacts projectName: imageBuildJob,
                selector: lastSuccessful(),
                flatten: true,
                filter: '**/.release-notes.md',
                target: "build-packer-ci-local/$imageName-$gitBranch"
        }
    }

    try {
        stage(imageTask) {
            dir(upliftWorkingDir) {
                withCredentials([string(credentialsId: 'uplift-vagrant-cloud-auth-token', variable: 'vagrantCloudAuthToken')]) {
                    sh "pwsh -c 'invoke-build -packerImageName $imageName -Task $imageTask' -VAGRANT_CLOUD_AUTH_TOKEN $vagrantCloudAuthToken"    
                }
            }
        }

    } finally {

         stage('Artifacts') {
            dir(upliftWorkingDir) {
                
                def imageArtifactFolder = "build-packer-ci-local/$imageName-$gitBranch"

                // archiveArtifacts  artifacts: "$imageArtifactFolder/box-spec/*.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/.release-notes.md", allowEmptyArchive: true
        
                archiveArtifacts  artifacts: "$imageArtifactFolder/packer.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/variables.json", allowEmptyArchive: true
                archiveArtifacts  artifacts: "$imageArtifactFolder/.build-container.json", allowEmptyArchive: true
        
                // archiveArtifacts  artifacts: "$imageArtifactFolder/Vagrantfile", allowEmptyArchive: true
                // archiveArtifacts  artifacts: "$imageArtifactFolder/.vagrant-*.ps1", allowEmptyArchive: true
        
                // archiveArtifacts  artifacts: "$imageArtifactFolder/logs/*.log", allowEmptyArchive: true 

            }
        }
        
    }
}
