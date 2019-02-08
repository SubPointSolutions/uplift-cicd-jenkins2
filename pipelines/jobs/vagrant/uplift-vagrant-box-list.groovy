node('uplift') {

    stage('vagrant box list') {
        sh "vagrant box list"
    }

}