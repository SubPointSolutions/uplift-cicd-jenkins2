node('uplift') {

    stage('plugin list') {
        sh "vagrant plugin list"
    }

}