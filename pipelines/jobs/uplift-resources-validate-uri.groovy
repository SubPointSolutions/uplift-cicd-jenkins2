node('uplift') {

    stage('resource validate-uri') {
        sh "pwsh -c 'uplift resource validate-uri'"
    }

}