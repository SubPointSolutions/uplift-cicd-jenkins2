node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-publish'    
    }
    
}
