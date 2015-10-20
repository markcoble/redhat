#!/usr/bin/env bash

set -x

VAR_ENV=$1

. $HOME/scripts/envs/$VAR_ENV/environment.sh
. $HOME/scripts/lib/helper_functions.sh

kill_karaf_instances