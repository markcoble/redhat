#!/usr/bin/env bash

VAR_ENV=$1

. $HOME/scripts/envs/$VAR_ENV/environment.sh
. $HOME/scripts/lib/helper_functions.sh

echo "Starting fuse with version" "$@"

start_fuse "$@"