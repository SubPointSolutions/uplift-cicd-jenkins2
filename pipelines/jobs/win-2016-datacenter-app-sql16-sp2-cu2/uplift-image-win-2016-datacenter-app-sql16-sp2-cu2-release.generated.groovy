node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-sp2-cu2-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-sp2-cu2-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-sp2-cu2-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-app-sql16-sp2-cu2-publish'    
    }
    
}
