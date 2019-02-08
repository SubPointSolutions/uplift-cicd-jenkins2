$ErrorActionPreference = "Stop"
function Invoke-Jenkins2AgentUtil($options) {

    $commandName   = [String]$options[0]
    
    $systemOptions  = $options | Select -Skip 1 | Where-Object { $_ -like "-*" }
    $commandOptions = $options | Select -Skip 1 | Where-Object { $_ -notlike "-*" }

    if($null -eq $systemOptions) { $systemOptions  = @()}
    if($null -eq $commandOptions) { $commandOptions = @()}

    if($commandOptions.GetType() -eq [String]) { $commandOptions = @($commandOptions) }
    if($systemOptions.GetType()  -eq [String]) { $systemOptions  = @($systemOptions) }

    function Get-DebugStatus() {
        return $systemOptions.Contains("-debug") -or $systemOptions.Contains("-d")
    }

    # log helpers
    function Write-LogMessage {
        [Diagnostics.CodeAnalysis.SuppressMessageAttribute("PSAvoidUsingWriteHost", "", Scope="Function")]
        param(
            $message,
            $level,
            $loggerName = "uplift"
        )


        $level = $level.ToUpper()
        $stamp = $(get-date -f "yyyy-MM-dd HH:mm:ss.fff")

        if((Get-DebugStatus) -eq $False -and ($level -eq "DEBUG")) {
            return;
        }

        $messageColor = "White"

        if($level -eq "INFO")  { $messageColor = "Green" }
        if($level -eq "DEBUG") { $messageColor = "Blue" }
        if($level -eq "ERROR") { $messageColor = "Red" }
        if($level -eq "WARN")  { $messageColor = "Yellow" }

        $level = $level.PadRight(5)

        # use [environment]::UserDomainName / [environment]::UserName
        # $env:USERDOMAIN won't work on non-windows platforms
        $logMessage = "$loggerName : $stamp : $level : $([environment]::UserDomainName)/$([environment]::UserName) : $message"

        Write-Host $logMessage `
            -ForegroundColor $messageColor
    }

    function Write-InfoMessage($message) {
        Write-LogMessage "$message" "INFO"
    }

    function Write-DebugMessage($message) {
        Write-LogMessage "$message" "DEBUG"
    }

    function Write-WarningMessage($message) {
        Write-LogMessage "$message" "WARN"
    }

    function Write-ErrorMessage($message) {
        Write-LogMessage "$message" "ERROR"
    }

    # download helpers

    function New-Folder {
        [Diagnostics.CodeAnalysis.SuppressMessageAttribute("PSShouldProcess", "", Scope="Function")]
        [Diagnostics.CodeAnalysis.SuppressMessageAttribute("PSUseShouldProcessForStateChangingFunctions", "", Scope="Function")]
        param(
            $folder
        )

        if(!(Test-Path $folder))
        {
            New-Item -ItemType Directory -Force -Path $folder | Out-Null
        }
    }

    function Get-Version() {
        return '0.1.0'
    }

    # main commands
    
    function Invoke-ActionHelp {
        [System.ComponentModel.DescriptionAttribute("Shows help")]
        param(
            $options
        ) 

        Invoke-ActionVersion

        $actionFunctions = Get-Command `
            | Where-Object { $_.Name -like "Invoke-Action*" } `
            | Sort-Object -Property Name
            
        Write-Host "Usage: uplift [version] [help] <command> [<args>]"
        Write-Host ''
        Write-Host "Available commands are:"
       
        foreach($actionFunction in $actionFunctions) {
            $action            = Get-Command $actionFunction.Name
            $actionDescription = ([string]$action.ScriptBlock.Attributes[0].Description).ToLower()

            $name = $actionFunction.Name.Replace("Invoke-Action", "").ToLower()
            Write-Host "    $name`t$actionDescription"
        }

        return 0
    }

    function Invoke-ActionVersion {
        [System.ComponentModel.DescriptionAttribute("Shows current version")]
        param(
            $options
        )

        Write-Host "uplift v$(Get-Version)"
    }

    function Get-SwarmAgentUrl() {
        return "https://repo.jenkins-ci.org/releases/org/jenkins-ci/plugins/swarm-client/3.5/swarm-client-3.5.jar"
    }

    function Get-SwarmAgentPath() {
        return (Join-Path $PSScriptRoot "swarm-client-3.5.jar")
    }

    function Get-SwarmAgent() {
        $path = Get-SwarmAgentPath

        if( (Test-Path $path ) -eq $False) {
            Invoke-WebRequest -Uri (Get-SwarmAgentUrl) `
                              -OutFile (Get-SwarmAgentPath) `
                              -UseBasicParsing
        }
    }

    function Require-Cmder() {
        if($null -eq $env:CMDER_ROOT) {
            $errroMessage = [String]::Join([Environment]::NewLine, @(
                "Cannot detect Cmder. Please run jenkins agent under cmder on windows platform: http://cmder.net"
                "You can use choco to install it: choco install -y cmder"
            ))

            Write-ErrorMessage $errroMessage 
            throw $errroMessage 
        }
    }

    function Get-OSName() {
        # $PSVersionTable["OS"] won't work wel;l
        # https://github.com/PowerShell/PowerShell/issues/1635
        $rawOS = [Environment]::OSVersion.VersionString

        Write-InfoMessage "Detected OS.VersionString: $rawOS"

        if($rawOS -ilike "*Darwin*") {

            $parts = $rawOS.Split(' ')
            # Darwin 18.2.0 Darwin Kernel Version 18.2.0: Mon Nov 12 20:24:46 PST 2018; root:xnu-4903.231.4~2/RELEASE_X86_64
            return ($parts[0] + "-" + $parts[1]).ToLower()

        } elseif($rawOS -ilike "*Microsoft*") {
            
            # require cmder under windows
            # it makes Jenkins agents execute sh and other commans without issues
            Require-Cmder

            # Microsoft Windows NT 10.0.17134.0 and so on
            return ( "Windows-" + [Environment]::OSVersion.Version.ToString()).ToLower()

        } else {
            throw ("Unknown OS: $rawOS ")
        }
    }

    function Get-OptionValue($options, $name, $default) {

        $optionTag = "$name" + ":"

        foreach($option in $options) {
            if($option.StartsWith($optionTag) -eq $True) {
                $value = $option.Split($optionTag)[1]

                return  $value
            } 
        }

        return $default
    }

    function Invoke-ActionRegister {
        [System.ComponentModel.DescriptionAttribute("Registers new agent")]
        param(
            $options
        )

        Invoke-ActionVersion

        Write-InfoMessage "Registering new uplift agent"
        Get-SwarmAgent
        
        # agent workdir
        $fsroot = Join-Path ([System.IO.Path]::GetTempPath()) "/uplift-jenkins2/"
        
        if( $null -ne $ENV:JENKINS_FS_ROOT) {
            $fsroot = $ENV:JENKINS_FS_ROOT
            New-Folder $fsroot
        }

        # -sslFingerprints " " is to bypass docker-vagrant setup
        # https://issues.jenkins-ci.org/browse/JENKINS-44210

        $osName      = Get-OSName
        $master      = Get-OptionValue $options "master" "http://localhost:10000"

        $agentName   = "uplift-$osName"
        $agentLabels = @("uplift uplift-$osName")

        $userName     = Get-OptionValue $options "username" "uplift"
        $userPassword = Get-OptionValue $options "password" "uplift"

        $executors    = Get-OptionValue $options "executors" "16"
        
        $agentLabels  += (Get-OptionValue $options "agentLabels" "").Split(",")
        $agentLabels  += $agentName 

        $customAgentName = (Get-OptionValue $options "agentName" "")
        if( [String]::IsNullOrEmpty($customAgentName) -ne $True) {
            $agentName = "$agentName-$customAgentName"
            
            $agentLabels += $agentName
            $agentLabels += $customAgentName
        }

        $fsroot = "$fsroot/$agentName"
        New-Folder $fsroot
        
        $agentLabels = [String]::Join(" ", $agentLabels)

        Write-InfoMessage "`tmaster: $master"
        Write-InfoMessage "`tagentName: $agentName"
        Write-InfoMessage "`tagentLabels: $agentLabels"
        Write-InfoMessage "`tuserName: $userName"
        Write-InfoMessage "`tagentName: $agentName"
        Write-InfoMessage "`tfsroot: $fsroot"
        Write-InfoMessage "`texecutors: $executors"

        $agentPath = Get-SwarmAgentPath

        Write-InfoMessage "Starting java -jar $agentPath"
        & java -jar $agentPath `
            -name $agentName `
            -master $master `
            -username "$userName" `
            -password "$userPassword" `
            -labels "$agentLabels" `
            -fsroot "$fsroot" `
            -sslFingerprints " " `
            -disableSslVerification `
            -executors $executors
    }

    Write-DebugMessage "Running with options:"
    Write-DebugMessage "`toptions: $options"
    Write-DebugMessage "`tcommandName: $commandName"
    Write-DebugMessage "`tsystemOptions: $systemOptions"
    Write-DebugMessage "`tcommandOptions: $commandOptions"
    
    switch($commandName)
    {
        "help"     { Invoke-ActionHelp     $commandOptions }
        "version"  { Invoke-ActionVersion  $commandOptions }
        "register" { Invoke-ActionRegister $commandOptions }

        default    { Invoke-ActionHelp     $commandOptions }
    }
}

exit Invoke-Jenkins2AgentUtil $args