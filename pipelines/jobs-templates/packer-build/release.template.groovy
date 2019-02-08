node('uplift') {
    
    stage('Download binaries') {
        build job: 'uplift-image-#IMAGE_NAME#-download'
    }

    stage('Rebuild') {
        build job: 'uplift-image-#IMAGE_NAME#-rebuild'
    }
    
    stage('Test') {
        build job: 'uplift-image-#IMAGE_NAME#-test'    
    }
    
    stage('Publish') {
        build job: 'uplift-image-#IMAGE_NAME#-publish'    
    }
    
}