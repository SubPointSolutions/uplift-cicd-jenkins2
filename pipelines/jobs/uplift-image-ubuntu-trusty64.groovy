node('uplift') {

    def gitRepo          = "https://github.com/SubPointSolutions/uplift-packer.git"
    def workingDir       = "uplift-packer"
    def upliftWorkingDir =  workingDir;

    def imageName  = "ubuntu-trusty64"
    
    stage('checkout') {
        sh "git clone $gitRepo || (cd $workingDir && git pull) "   
    }

    stage('packer rebuild') {
       sh "cd $upliftWorkingDir && pwsh -c 'invoke-build -packerImageName $imageName -Task PackerRebuild' "
    }
}