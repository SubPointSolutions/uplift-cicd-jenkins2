
// CSRF Protection
// https://wiki.jenkins.io/display/JENKINS/CSRF+Protection

// disable, need this so that node-jenkins-api will work

// https://github.com/jansepar/node-jenkins-api/issues/48
// https://github.com/jansepar/node-jenkins-api/issues/58

// import hudson.security.csrf.DefaultCrumbIssuer
// import jenkins.model.Jenkins
 
// def instance = Jenkins.instance

// instance.setCrumbIssuer(new DefaultCrumbIssuer(true))
// instance.save()