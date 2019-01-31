node('uplift') {

    stage('initial check') {
        sh 'whoami'
    }

    stage('build tools') {
        sh 'git version'
        sh 'pwsh -version'

        sh 'wget --version'
        sh 'curl --version'
    }

    stage('hashicorp tools') {
        sh 'packer version'
        sh 'vagrant version'
    }

    stage('virtualbox tooling') {
        sh 'vboxmanage --version'
    }
}