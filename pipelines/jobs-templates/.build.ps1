$dirPath = $BuildRoot

. "$dirPath/.build-helpers.ps1"

$pipelinesFolder = "$dirPath/../jobs"

$images = @{
    # test box
    'ubuntu-trusty64' = @{
        'FileResources' = @(

        )
        'ImageInputBoxName' = 'ubuntu/trusty64'
    };

    # soe boxes
    'win-2016-datacenter-bare' = @{
        'FileResources' = @(
            
        )
    };

    # soe boxes
    'win-2016-datacenter-soe' = @{
        'FileResources' = @(
            
        )
    };

    'win-2016-datacenter-soe-latest' = @{
        'FileResources' = @(
            'ms-win2016-ssu-2018.05.17-KB4132216'
            'ms-win2016-lcu-2019.01.17-KB4480977'
        )
    };

    # app boxes
    'win-2016-datacenter-app' = @{
        'FileResources' = @(
        
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-soe-latest-$gitBranch'
    };

    'win-2016-datacenter-app-sql16' = @{
        'FileResources' = @(
            'ms-sql-server2016-rtm'
            'ms-sql-server-management-studio-17.04'
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-soe-latest-$gitBranch'
    };

    'win-2016-datacenter-app-sql16-vs17' = @{
        'FileResources' = @(
            'ms-sql-server2016-rtm'
            'ms-sql-server-management-studio-17.04'
            'ms-visualstudio-2017.ent-installer'
            'ms-visualstudio-2017.ent-dist-office-dev'
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-soe-latest-$gitBranch'
    };

    # sharepoint boxes
    'win-2016-datacenter-sp2016rtm-sql16-vs17' = @{
        'FileResources' = @(
            'ms-visualstudio-2017.ent-installer'
            'ms-visualstudio-2017.ent-dist-office-dev'
            'ms-sharepoint2016-rtm'
            'ms-sharepoint2016-lang-pack'
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-app-sql16-$gitBranch'
    };

    'win-2016-datacenter-sp2016fp1-sql16-vs17' = @{
        'FileResources' = @(
            'ms-visualstudio-2017.ent-installer'
            'ms-visualstudio-2017.ent-dist-office-dev'
            'ms-sharepoint2016-rtm'
            'ms-sharepoint2016-lang-pack'
            'ms-sharepoint2016-update-2016.11.08'
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-app-sql16-$gitBranch'
    };

    'win-2016-datacenter-sp2016fp2-sql16-vs17' = @{
        'FileResources' = @(
            'ms-visualstudio-2017.ent-installer'
            'ms-visualstudio-2017.ent-dist-office-dev'
            'ms-sharepoint2016-rtm'
            'ms-sharepoint2016-lang-pack'
            'ms-sharepoint2016-fp2'
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-app-sql16-$gitBranch'
    };
    
    'win-2016-datacenter-sp2016latest-sql16-vs17' = @{
        'FileResources' = @(
            'ms-visualstudio-2017.ent-installer'
            'ms-visualstudio-2017.ent-dist-office-dev'
            'ms-sharepoint2016-rtm'
            'ms-sharepoint2016-lang-pack'
            'ms-sharepoint2016-update-2019.01.08'
        )
        'ImageInputBoxName' = 'uplift-local/win-2016-datacenter-app-sql16-$gitBranch'
    };
}

Enter-Build {
    Write-Build Green "Building pipelines..."
}

function Get-PackerBuildTemplates() {
    return Get-ChildItem packer-build  -Recurse 
}

function Build-PackerPipelines($imageName, $imageSpec, $pipelinesFolder) {
    $templates = Get-PackerBuildTemplates

    foreach($templateFile in $templates) {

        $templateName = [System.IO.Path]::GetFileName($templateFile.FullName)
        $pipelineAction =  $templateName.Replace('.template.groovy', '').Split('-')[0]

        $pipelineName = "uplift-image-$imageName-$pipelineAction"

        Write-Build Green "  $pipelineName"

        $content = Get-Content -Raw $templateFile.FullName -Encoding UTF8

        $imageAction = ''
 
        switch( $pipelineAction ) {
            "download" {  }

            "build"    {  $imageAction = 'PackerBuild' }
            "rebuild"  {  $imageAction = 'PackerRebuild' }
            "test"     {  $imageAction = 'VagrantBoxTest' }
            
            "publish"  {  $imageAction = 'VagrantCloudPublish' }

            "release"  {  $imageAction = '' }
            
            default { throw ("Unknown action:  $pipelineAction") }
        }

        $imageFileResources = "";
        
        if( $imageSpec.FileResources.Count -gt 0) {
            $imageFileResources = $imageFileResources + '"'
            $imageFileResources = $imageFileResources + [String]::Join(""",`n`t`t""", $imageSpec.FileResources)
            $imageFileResources = $imageFileResources + '"'
        }
        
        $contentTokens = @{
            '#IMAGE_NAME#' = $imageName
            '#IMAGE_TASK#' = $imageAction 
            
            '#IMAGE_INPUT_BOX#' = [String]$imageSpec.ImageInputBoxName

            '#FILE-RESOURCES#' = $imageFileResources
        }

        foreach ($token in $contentTokens.Keys) {
            $value =  $contentTokens[$token]
            $content = $content.Replace($token, $value)
        }
        
        $fileFolder = "$pipelinesFolder/$imageName"
        [System.IO.Directory]::CreateDirectory($fileFolder) | Out-Null        

        $filePath   = " $fileFolder/$pipelineName.generated.groovy" 

        $utf8NoBomEncoding = New-Object System.Text.UTF8Encoding($False) 
        [System.IO.File]::WriteAllLines($filePath, $content, $utf8NoBomEncoding) 
    }
}

task BuildPipelines {
    foreach($imageName in $images.Keys) {
        Write-Build Yellow "[~] image: $imageName"

        Build-PackerPipelines $imageName $images[$imageName] $pipelinesFolder 
    }
}

task . BuildPipelines