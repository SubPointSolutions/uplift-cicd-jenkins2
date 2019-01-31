const fs = require('fs');
const path = require("path");

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

console.log(jenkinsUrl)

var jenkins = jenkinsapi.init(jenkinsUrl);

function logMessage(message, color) {
    // https: //stackoverflow.com/questions/9781218/how-to-change-node-jss-console-font-color

    if (color) {
        console.log(color, message);
    } else {
        console.log(message);
    }
}

function logError(message) {
    logMessage(message, "\x1b[31m")
}

function logInfo(message) {
    logMessage(message, "\x1b[32m")
}

function logWarn(message) {
    logMessage(message, "\x1b[33m")
}

function syncJob(jenkins, jobName, jobFile) {

    var template = fs.readFileSync('config/job_config.template.xml', 'utf8')
    var groovyScript = fs.readFileSync(jobFile, 'utf8');

    var jobContent = template.replace('$SCRIPT$', xmlescape(groovyScript));
    var jobExists = false

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
        jenkins.create_job(jobName, jobContent, function (err) {
            if (err) {
                logError("ERROR")
                logError(err)

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
            logWarn(" - creating job: " + jobName)
            createJob(jenkins, jobName, jobContent)
        } else {
            logInfo(" - updating job: " + jobName)
            updateJob(jenkins, jobName, jobContent)
        }
    });
}

function syncFolder(folderPath) {

    logInfo("[~] syncing folder: " + folderPath)

    var files = fs.readdirSync(folderPath)

    files.forEach(file => {
        var jobName = path.parse(file).name;
        var jobFilePath = folderPath + "/" + file

        logInfo(" job: " + jobName);
        logInfo("  - src file: " + jobFilePath);

        syncJob(jenkins, jobName, jobFilePath);
    });
}

syncFolder(jobFolderPath)