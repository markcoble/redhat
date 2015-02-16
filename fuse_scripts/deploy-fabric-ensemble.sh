#!/usr/bin/env bash
set +x


export APP_VERSION="$1"
export RELEASE_VERSION="$2"
export DEPLOYMENT_ENVIRONMENT="$3"
export WITH_NEW_RELIC="$4"

# Set the environment variables for the selected environment
. ./envs/$DEPLOYMENT_ENVIRONMENT/environment.sh
. ./lib/helper_functions.sh

karaf_commands


CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Output the session ID for debugging purposes
echo &id

FUSE_HOSTS[0]=$ROOT_NODE
FUSE_HOSTS[1]=${FABRIC_HOSTS[0]}
FUSE_HOSTS[2]=${FABRIC_HOSTS[1]}
export FUSE_HOSTS

. ./destroy-and-deploy-clean-fabric.sh "$@"
. ./create-app-profile.sh $APP_VERSION

ssh_update_git $ROOT_NODE $RELEASE_VERSION $DEPLOYMENT_ENVIRONMENT

. ./deploy-brokers.sh "$@"

exit 0
