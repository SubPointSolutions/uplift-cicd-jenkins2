node('uplift') {

    stage('vagrant global-status') {
        sh "vagrant global-status"
    }

}