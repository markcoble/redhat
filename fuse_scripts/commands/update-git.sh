#!/usr/bin/env bash

set -x

VAR_HOST=$1
VAR_VERSION=$2
VAR_ENV=$3

#
. $HOME/scripts/envs/$VAR_ENV/environment.sh
. $HOME/scripts/lib/helper_functions.sh
 
echo "Updating fuse git repository" 

update_git "$VAR_HOST" "$VAR_VERSION" "$VAR_ENV"