node('uplift') {

    def gitRepo          = "https://github.com/SubPointSolutions/uplift-packer.git"
    def workingDir       = "uplift-packer"
    def upliftWorkingDir =  workingDir;

    def imageName     = "win-2016-datacenter-sp2016latest-sql16-vs17"
    def inputBoxName  = "uplift-local/win-2016-datacenter-app-sql16-master"
    
    stage('checkout') {
        sh "git clone $gitRepo || (cd $workingDir && git pull) "   
    }

    stage('packer rebuild') {
        sh "cd $upliftWorkingDir && pwsh -c 'invoke-build -packerImageName $imageName -Task PackerRebuild -UPLF_INPUT_BOX_NAME $inputBoxName' "
    }
}