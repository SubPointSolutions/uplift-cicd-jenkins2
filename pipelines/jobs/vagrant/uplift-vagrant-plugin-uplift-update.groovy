node('uplift') {

    stage('plugin uninstall') {
        sh "vagrant plugin uninstall vagrant-uplift"
    }

    stage('plugin install') {
        sh "vagrant plugin install vagrant-uplift"
    }

    stage('plugin list') {
        sh "vagrant plugin list"
    }

}