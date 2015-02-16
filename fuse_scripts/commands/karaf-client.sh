#!/usr/bin/env bash

set -x

VAR_ENV=$1
VAR_COMMAND=$2

. $HOME/scripts/envs/$VAR_ENV/environment.sh
. $HOME/scripts/lib/helper_functions.sh

karaf_client "$VAR_COMMAND"