node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-ubuntu-trusty64-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-ubuntu-trusty64-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-ubuntu-trusty64-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-ubuntu-trusty64-publish'    
    }
    
}
