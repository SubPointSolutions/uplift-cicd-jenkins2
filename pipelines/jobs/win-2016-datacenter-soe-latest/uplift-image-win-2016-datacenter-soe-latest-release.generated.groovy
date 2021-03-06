node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-soe-latest-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-soe-latest-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-soe-latest-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-soe-latest-publish'    
    }
    
}
