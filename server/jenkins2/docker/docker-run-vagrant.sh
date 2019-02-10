#!/usr/bin/env bash

[[ ! -z "$BUILD_DIR" ]] && cd $BUILD_DIR

TAG="subpointsolutions/uplift-jenkins2"

if [ -z "$1" ]; then USER_NAME="uplift"; else USER_NAME=$1; fi
if [ -z "$2" ]; then USER_PASS="uplift"; else USER_PASS=$2; fi
if [ -z "$3" ]; then PORT="9085"; else PORT=$3; fi

JENKINS_HOME_PATH='/opt/uplift-jenkins_home'
CONTAINER_NAME='uplift-jenkins2-container'

echo "Starting container..."
echo "  - TAG: $TAG"
echo "  - PORT: $PORT"
echo "  - JENKINS_USER_NAME: $USER_NAME"
echo "  - JENKINS_HOME_PATH: $JENKINS_HOME_PATH"

mkdir -p  $JENKINS_HOME_PATH
chmod 777 $JENKINS_HOME_PATH

CONTAINER_STATUS="$(docker ps -a | grep Up | grep $CONTAINER_NAME)"

# if running, check the latest
echo "CONTAINER_STATUS: $CONTAINER_STATUS"

if [ ! -z "$CONTAINER_STATUS" ]; then
    echo "Container is running: $CONTAINER_NAME"

    LATEST_IMAGE_ID=$(docker images --format='{{ .ID }}' subpointsolutions/uplift-jenkins2)
    RUN_IMAGE_ID=$(docker inspect  --format='{{ .Image }}' $CONTAINER_NAME)

    echo "LATEST_IMAGE_ID: $LATEST_IMAGE_ID"
    echo "RUN_IMAGE_ID   : $RUN_IMAGE_ID"

    if [[ $RUN_IMAGE_ID == *"$LATEST_IMAGE_ID"* ]]; then
        echo "[+] Running latest image, all good"
    else 
        echo "[-] Running older image, should restart"

        echo " - stoppig..."
        docker stop $CONTAINER_NAME

        echo " - deleting..."
        docker rm $CONTAINER_NAME

        CONTAINER_STATUS="$(docker ps -a | grep Up | grep $CONTAINER_NAME)"
        SHOULD_RUN='1'
    fi
else 
    SHOULD_RUN='1'
fi

if [ -z "$CONTAINER_STATUS" ]; then
    if [[ "$SHOULD_RUN" == '1' ]]; then 
        echo "Running container: $CONTAINER_NAME"
        
        docker start $CONTAINER_NAME ||  docker run -d \
            --name $CONTAINER_NAME \
            -p $PORT:8080 \
            -p 50000:50000 \
            -v $JENKINS_HOME_PATH:/var/jenkins_home \
            -e JENKINS_USER_NAME=$USER_NAME \
            -e JENKINS_USER_PASSWORD=$USER_PASS \
            -e UPLF_GIT_BRANCH=$UPLF_GIT_BRANCH \
            -e UPLF_GIT_COMMIT=$UPLF_GIT_COMMIT \
            $TAG    
    else 
        echo "Starting container: $CONTAINER_NAME"
        docker start $CONTAINER_NAME
    fi
    
else 
    echo "Container is already running"
fi