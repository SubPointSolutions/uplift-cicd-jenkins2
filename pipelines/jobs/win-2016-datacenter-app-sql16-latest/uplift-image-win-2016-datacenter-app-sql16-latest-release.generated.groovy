node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-latest-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-latest-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-latest-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-latest-publish'    
    }
    
}
