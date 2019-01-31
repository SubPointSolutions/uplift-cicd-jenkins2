
$dirPath = $BuildRoot
$scriptPath = $MyInvocation.MyCommand.Name


# Synopsis: Starts jenkins vagrant box
task StartServer {
    Write-Build Green " [~] Starting jenkins vagrant box..."
    
    exec {
        Set-Location "server/jenkins2"
        vagrant up
    }
}

task StartAgent {
    exec {
        Set-Location "agents/scripts"
        & pwsh -f 'jenkins2-agent.ps1' 'register'
    }
}

task SyncJobs {
    exec {
        Set-Location "pipelines"
        node 'sync-jobs.js'
    }
}

task . StartServer, StartAgent
