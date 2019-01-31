#!/usr/bin/env bash

TAG="subpointsolutions/uplift-jenkins2"

if [ -z "$1" ]; then USER_NAME="uplift"; else USER_NAME=$1; fi
if [ -z "$2" ]; then USER_PASS="uplift"; else USER_PASS=$2; fi
if [ -z "$3" ]; then PORT="9085"; else PORT=$3; fi
if [ -z "$4" ]; then AGENT_PORT="50000"; else AGENT_PORT=$4; fi

echo "Starting container..."
echo "  - TAG: $TAG"
echo "  - PORT: $PORT"
echo "  - AGENT_PORT: $AGENT_PORT"
echo "  - JENKINS_USER_NAME: $USER_NAME"

docker run -it --rm \
    -p $PORT:8080 \
    -p $AGENT_PORT:50000 \
    -e JENKINS_USER_NAME=$USER_NAME \
    -e JENKINS_USER_PASSWORD=$USER_PASS \
    $TAG