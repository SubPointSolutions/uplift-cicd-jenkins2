node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-win-2016-datacenter-sp2016latest-sql16-vs17-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-win-2016-datacenter-sp2016latest-sql16-vs17-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-win-2016-datacenter-sp2016latest-sql16-vs17-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-win-2016-datacenter-sp2016latest-sql16-vs17-publish'    
    }
    
}
