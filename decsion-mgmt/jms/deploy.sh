#!/usr/bin/env bash
set +x

. ./lib/helper_functions.sh

export APP_VERSION="$1"
export RELEASE_VERSION="$2"
export DEPLOYMENT_ENVIRONMENT="$3"
export WITH_NEW_RELIC="$4"

CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Output the session ID for debugging purposes
echo &id

# Set the environment variables for the selected environment 
. ./envs/$DEPLOYMENT_ENVIRONMENT/environment.sh

karaf_commands

## create profiles 
. ./destroy-and-deploy-clean-fabric.sh "$@"
. ./create-app-profile.sh $APP_VERSION
. ./create-app-broker-profile.sh "$@"

ssh_update_git $ROOT_NODE $RELEASE_VERSION $DEPLOYMENT_ENVIRONMENT

## deploy containers with profiles
. ./deploy-brokers.sh "$@"
. ./deploy-app.sh $APP_VERSION

exit 0
