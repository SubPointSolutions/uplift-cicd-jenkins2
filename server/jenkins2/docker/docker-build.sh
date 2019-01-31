#!/usr/bin/env bash

[[ ! -z "$BUILD_DIR" ]] && cd $BUILD_DIR

TAG='subpointsolutions/uplift-jenkins2'

echo "BUILD_NO_CACHE: $BUILD_NO_CACHE"

if [[ -z "$BUILD_NO_CACHE" ]]; then 
    echo "Building container"
    echo "  - cmd: docker build $@ --tag $TAG ."

    docker build "$@" --tag $TAG .
else 
    echo "Building container with --no-cache option"
    echo "  - cmd: docker build --no-cache $@ --tag $TAG ."

    docker build --no-cache "$@" --tag $TAG .
fi