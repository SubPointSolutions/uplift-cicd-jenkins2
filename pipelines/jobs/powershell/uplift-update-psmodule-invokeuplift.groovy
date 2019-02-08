
def name       = "InvokeUplift"
def version    = "0.1.20190126.124510"
def repository = "subpointsolutions-staging"

node('uplift') {

    stage('update module') {
        sh "pwsh -c  \"Install-Module -Name $name -RequiredVersion \"$version\" -Repository $repository \" "   
    }
    stage('get installed module') {
        sh "pwsh -c  \"Get-InstalledModule -Name $name \" "   
    }

}