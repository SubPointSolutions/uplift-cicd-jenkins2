# tend to run on the latest with frequent instance recreation
# jenkins/jenkins:lts tag can be used for latest LTS
# FROM jenkins/jenkins:lts

FROM jenkins/jenkins

# install plugins, check base doco on how it works
# https://hub.docker.com/r/jenkins/jenkins/
COPY plugins/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# copy configuration as code settings
# https://github.com/jenkinsci/configuration-as-code-plugin
COPY casc_configs/* /usr/share/jenkins/casc_configs/

# copy all init.groovy.d scripts
COPY init.groovy.d/* /usr/share/jenkins/ref/init.groovy.d/
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/casc_configs/

# Skip initial setup
# search on github for more details
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

# COPY pipelines/* /app/jenkins2/pipelines/