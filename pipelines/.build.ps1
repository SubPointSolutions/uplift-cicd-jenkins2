
function Confirm-NpmModule($name) {
    npm list $name 

    if ($LASTEXITCODE -ne 0 ) {
        Write-Build Green "installing npm module: $name"
        npm install $name
    }
    else {
        Write-Build Green "npm module installed: $name"
    }
} 

Enter-Build {
    Write-Build Green "Preparing env..."
    
    Confirm-NpmModule xml-escape 
    Confirm-NpmModule jenkins-api
}

task SyncPipelines {
    Write-Build Green "Sync jenkins2 pipelines..."

    exec {
        node sync-jobs.js
    }
}

task . SyncPipelines