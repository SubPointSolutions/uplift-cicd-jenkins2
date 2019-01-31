import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import hudson.security.*

def userName     = System.getenv()["JENKINS_USER_NAME"]    
def userPassword = System.getenv()["JENKINS_USER_PASSWORD"] 

if(userName == null)     { userName     = "uplift"; }
if(userPassword == null) { userPassword = "uplift"; }

def log(msg) {
   println "\u001B[32mjenkins2 setup: user setup: ${msg}\u001B[0m"
}

Thread.start {
    sleep 10000
    
    def instance = Jenkins.getInstance();

    log "creating new realm for user: ${userName}"
    def hudsonRealm = new HudsonPrivateSecurityRealm(false);
    hudsonRealm.createAccount(userName, userPassword);

    instance.setSecurityRealm(hudsonRealm);
    instance.save();
    def strategy = new GlobalMatrixAuthorizationStrategy();

    log "adding new user..."
    strategy.add(Jenkins.ADMINISTER, userName);
    strategy.add(Jenkins.ADMINISTER, "admin");

    log "updating security..."
    instance.setAuthorizationStrategy(strategy);
    
    log "setup complete"
}