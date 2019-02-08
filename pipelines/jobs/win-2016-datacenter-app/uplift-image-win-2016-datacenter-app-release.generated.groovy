node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-app-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-app-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-app-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-app-publish'    
    }
    
}
