node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-soe-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-soe-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-soe-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-soe-publish'    
    }
    
}
