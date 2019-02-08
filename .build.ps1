
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

# Synopsis: Updates jenkins server as per the latest docker file
task UpdateServer {
    Write-Build Green " [~] Starting jenkins vagrant box..."
    
    exec {
        Set-Location "server/jenkins2"
        vagrant up --provision
    }
}

# Synopsis: Stops jenkins server
task HaltServer {
    Write-Build Green " [~] Halting jenkins vagrant box..."
    
    exec {
        Set-Location "server/jenkins2"
        vagrant halt
    }
}

# Synopsis: Starts jenkins agent
task StartAgent {
    exec {
        Set-Location "agents/scripts"
        & pwsh -f 'jenkins2-agent.ps1' 'register'
    }
}

# Synopsis: Sync jenkins configuration - jobs, views, etc
task SyncJobs {
    exec {
        Set-Location "pipelines"
        node 'sync-jobs.js'
    }
}

task . StartServer, StartAgent
