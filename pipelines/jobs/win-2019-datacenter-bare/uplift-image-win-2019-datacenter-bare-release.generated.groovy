node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2019-datacenter-bare-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2019-datacenter-bare-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2019-datacenter-bare-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2019-datacenter-bare-publish'    
    }
    
}
