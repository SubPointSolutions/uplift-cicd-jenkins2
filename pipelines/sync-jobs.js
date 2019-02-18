const fs = require('fs');
const path = require("path");

// https://github.com/jansepar/node-jenkins-api
const xmlescape = require('xml-escape');
const jenkinsapi = require('jenkins-api');

const user = process.env.JENKINS_USER || "uplift"
const password = process.env.JENKINS_PASSWORD || "uplift"

var jobFolderPath = process.env.JENKINS_JOB_FOLDER || "jobs";

const jenkinsServerUrl = (process.env.JENKINS_URL || 'http://localhost:10000').toLowerCase()
const httpPrefix = (jenkinsServerUrl.indexOf('https') != -1) ? "https" : "http"

const jenkinsUrl = [
    httpPrefix,
    '://',
    user,
    ':',
    password,
    '@',
    jenkinsServerUrl.toString().replace('https://', '').replace('http://', '')
].join('')

// general helpers
function logError(message) {
    logMessage(message, "\x1b[31m")
}

function logInfo(message) {
    logMessage(message, "\x1b[32m")
}

function logWarn(message) {
    logMessage(message, "\x1b[33m")
}

function logMessage(message, color) {
    // https: //stackoverflow.com/questions/9781218/how-to-change-node-jss-console-font-color

    if (color) {
        console.log(color, message);
    } else {
        console.log(message);
    }
}

logInfo(jenkinsUrl);

var jenkins = jenkinsapi.init(jenkinsUrl);

function getAllJobs(jenkins) {
    return new Promise(function(resolve, reject) {
        jenkins.all_jobs( function(err, data) {
            err == null ? resolve(data) :  reject(err);
        });
    });
}

function syncJob(jenkins, jobName, jobFile) {

    if(fs.lstatSync(jobFile).isDirectory()) {
        logWarn(`  - dir: ${jobFile}`)
    } else {
        logInfo(`  - file: ${jobFile}`)

        var template = fs.readFileSync('config/job_config.template.xml', 'utf8')
        var groovyScript = fs.readFileSync(jobFile, 'utf8');

        var jobContent = template.replace('$SCRIPT$', xmlescape(groovyScript));

        var updateJob = function (jenkins, jobName, jobContent) {
            jenkins.update_job(jobName, jobContent, function (err) {
                if (err) {
                    logError("ERROR")
                    logError(err)

                    throw err
                } else {
                    logInfo(" [+] " + jobName)
                }
            });
        }

        var createJob = function (jenkins, jobName, jobContent) {
            jenkins.create_job(jobName, jobContent, function (err,  data) {
                if (err) {
                    logError("ERROR")
                    logError(err)

                    logWarn("Data")
                    logWarn(data)

                    throw err
                } else {
                    logInfo(" [+] " + jobName)
                }
            });
        }

        var res = jenkins.get_config_xml(jobName, function (err, data) {
            if (typeof data == 'undefined') {
                logError("ERROR while performing API call")
                throw err
            }

            if (err && err.indexOf('404') != -1) {
                logWarn("  - creating job: " + jobName)
                createJob(jenkins, jobName, jobContent)
            } else {
                logInfo("  - updating job: " + jobName)
                updateJob(jenkins, jobName, jobContent)
            }
        });
    }
}

function deletePipeline(jenkins, jobName) {
    return new Promise(function(resolve, reject) {
        logWarn("  - deleting job: " + jobName)

        jenkins.delete_job(jobName, function(err, data) {
            err == null ? resolve(data) :  reject(err);
        });
    });
}

function deleteView(jenkins, name) {
    return new Promise(function(resolve, reject) {
        logWarn("  - deleting job: " + name)

        jenkins.delete_view(name, function(err, data) {
            err == null ? resolve(data) :  reject(err);
        });
    });
}

function syncViews(currentViews, viewConfigs) {

    var viewNames = viewConfigs.map( v => v.name);

    logInfo("[~] cleaning views which don't exist locally");
    currentViews.forEach(currentViewName => { 
        if(viewNames.indexOf(currentViewName) == -1) {
            logWarn(`[~] deleting view: ${currentViewName}`);

            deleteView(jenkins, currentViewName);
        }
    });

    logInfo("[~] creating/updating existing views");

    viewConfigs.forEach(viewConfig => {
        
        var viewName = viewConfig.name;
        
        jenkins.view_info(viewName, function (err, data) {
            if (typeof data == 'undefined') {
                logError("ERROR while performing API call")
                throw err
            }
        
            var viewJSONConfig = getViewConfig(viewName, viewConfig.jobs)
        
            if (err && err.indexOf('404') != -1) {
                logWarn("  - creating view: " + viewName)
        
                createView(viewName)
                    .then( function() {
                        updateView(viewName, viewJSONConfig)
                    });
            } else {
                logInfo("  - updating view: " + viewName)
                updateView(viewName, viewJSONConfig);
            }
        });
    }); 
}

function getPipelineFiles(folderPath) {
    var result = [];

    const isDirectory = source => fs.lstatSync(source).isDirectory()
    const getDirectories = source => fs.readdirSync(source).map(name => path.join(source, name)).filter(isDirectory)

    var dirs = getDirectories(folderPath)

    dirs.forEach(dir => {
        result = result.concat(fs.readdirSync(dir).map(f => dir + "/" + f))
    });

    return result;
}

function syncPipelines(currentPipelines, folderPath) {

    logInfo("[~] syncing folder: " + folderPath)

    var files = getPipelineFiles(folderPath)
    var fileNames = files.map( file => path.parse(file).name.replace('.generated', '') );

    logInfo("[~] cleaning pipelines which don't exist locally");
    currentPipelines.forEach(currentPipelineName => { 
        if(fileNames.indexOf(currentPipelineName) == -1) {
            logWarn(`[~] deleting pipeline: ${currentPipelineName}`);

            deletePipeline(jenkins, currentPipelineName);
        }
    });

    logInfo("[~] creating/updating existing pipelines");
    var filesCount   = files.length;
    var currentIndex = 1;

    files.forEach(file => {
        
        var jobName = path.parse(file).name.replace('.generated', '');
        var jobFilePath = file

        logWarn(`[${currentIndex}/${filesCount}] job: ${jobName}`);
        logInfo(`  - src file: ${jobFilePath}`);

        syncJob(jenkins, jobName, jobFilePath);

        currentIndex = currentIndex + 1;
    });
}

function createView(name) {
    return new Promise(function(resolve, reject) {
        jenkins.create_view(name, 'se.diabol.jenkins.workflow.WorkflowPipelineView', function(err, data) {
            err == null ? resolve(data) :  reject(err);
        });
    });
}

function updateView(name, viewConfig) {
    return new Promise(function(resolve, reject) {
        jenkins.update_view(name, viewConfig,  function(err, data) {
            err == null ? resolve(data) :  reject(err);
        });
    });
    
}

function getViewConfig(name, jobs) {
    var result =  {
        "name": name,
        "description": "",
        "filterExecutors": "false",
        "filterQueue": "false",

        "noOfPipelines": "2",
        "noOfColumns": "2",
        "updateInterval": "5",

        "allowPipelineStart": true,
        "allowAbort": true,

        "showChanges": true,
        "showAbsoluteDateTime": true,

        "linkToConsoleLog": true,
        "theme": "contrast",

        "componentSpecs": [
           
        ]
    };

    result.componentSpecs = jobs.map(job => {
        return {
            "name": job.name,
            "job": job.job
        }
    });

    return result;
}

function getAllViews(jenkins) {
    return new Promise(function(resolve, reject) {
        jenkins.all_views( function(err, data) {
            err == null ? resolve(data) :  reject(err);
        });
    });
}

function getUpliftJobNames(jobs)  {
    return jobs.filter(p => p.name.startsWith('uplift-'))
                .map(p => p.name);
}

function getUpliftViewNames(jobs)  {
    return jobs.filter(p => p.name.startsWith('uplf-'))
                .map(p => p.name);
}

getAllJobs(jenkins)
    .then( function(data) {
        var upliftPipelines = getUpliftJobNames(data);

        syncPipelines(upliftPipelines, jobFolderPath)
    });

function getVewsConfiguration() {
    var imageNames = [
        'ubuntu-trusty64',

        'win-2016-datacenter-bare',

        'win-2016-datacenter-soe',
        'win-2016-datacenter-soe-latest',

        'win-2016-datacenter-app',
        'win-2016-datacenter-app-sql16',

        'win-2016-datacenter-sp2016rtm-sql16-vs17',
        'win-2016-datacenter-sp2016fp1-sql16-vs17',
        'win-2016-datacenter-sp2016fp2-sql16-vs17',
        'win-2016-datacenter-sp2016latest-sql16-vs17'
    ];

    var result = imageNames.map(name => {
        return {
            'name': `uplf-${name}`,
            'jobs': [ 
                {
                    name: 'Rebuild/Test/Publish image to Vagrant Cloud',
                    job: `uplift-image-${name}-release`
                },
                {
                    name: 'Rebuild image',
                    job: `uplift-image-${name}-rebuild`,
                },
                {
                    name: 'Build image',
                    job: `uplift-image-${name}-build`,
                },
                {
                    name: 'Test image',
                    job: `uplift-image-${name}-test`,
                },
                {
                    name: 'Download binaries',
                    job: `uplift-image-${name}-download`,
                },
                {
                    name: 'Publish image to Vagrant Cloud',
                    job: `uplift-image-${name}-publish`,
                }
            ]
        }
    });

    // test views
    result.push({
        'name': `uplf-win-2016-tests`,
        'jobs': [ 
            {
                name: 'Shared dc: vagrant up',
                job: `uplift-test-win-2016-datacenter-dc-shared-up`
            },
            {
                name: 'Shared dc: vagrant up --provision',
                job: `uplift-test-win-2016-datacenter-dc-shared-up-provision`
            },
            {
                name: 'Shared dc: vagrant destroy -f',
                job: `uplift-test-win-2016-datacenter-dc-shared-destroy`
            },
        ]
    });

    // contrib views
    result.push({
        'name': `uplf-contrib`,
        'jobs': [ 
            {
                name: 'Test: sp16lts-dev',
                job: `uplift-contrib-sp16lts-dev-test`
            },
            {
                name: 'Test: sp16lts-dev-spmeta2',
                job: `uplift-contrib-sp16lts-dev-spmeta2-test`
            },
            {
                name: 'Test: sp16lts-sql-dev',
                job: `uplift-contrib-sp16lts-sql-dev-test`
            }
        ]
    });

    // vagrant views
    result.push({
        'name': `uplf-vagrant`,
        'jobs': [ 
            {
                name: 'vagrant box list',
                job: `uplift-vagrant-box-list`
            },
            {
                name: 'vagrant global-status',
                job: `uplift-vagrant-global-status`
            },
            {
                name: 'vagrant plugin list',
                job: `uplift-vagrant-plugin-list`
            },

            {
                name: 'vagrant plugin update',
                job: `uplift-vagrant-plugin-uplift-update`
            },

            {
                name: 'vagrant box export',
                job: `uplift-vagrant-box-export`
            },


            
        ]
    });

    return result;
}

getAllViews(jenkins)    
    .then(function (data) {
        var upliftViews = getUpliftViewNames(data);

        syncViews(upliftViews, getVewsConfiguration());
    });
