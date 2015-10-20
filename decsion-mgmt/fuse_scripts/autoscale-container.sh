#!/usr/bin/env bash

set +x

if [ "$#" -lt 3 ]; then
    echo "Illegal number of parameters."
    echo "Usage: autoscale-container.sh (container-name prefix) (environment) (space separated profiles names to be attached to the container)"
    echo "Example: autoscale-container.sh application-container preprod mq-amq app-profile "
    exit 1
fi

. ./lib/helper_functions.sh

set +x

PREFIX=$1
DEPLOYMENT_ENVIRONMENT="$2"

args=( "$@" )
profiles=""

for ((i=2; i<${#args[@]}; i++));
do
    profiles="$profiles --profile ${args[$i]}"
done

IP=$(ifconfig eth0 | grep inet | awk '{print $2}' | cut -d':' -f2)
echo "My IP is: $IP"

container_name="$PREFIX-$IP"
echo "Selected container name: $container_name"

. ./envs/$DEPLOYMENT_ENVIRONMENT/environment.sh
CLIENT_INVOCATION="$FUSE_HOME/bin/client -u $APP_ADMIN_USER -p $APP_ADMIN_PASSWD -r 60"

find_fabric_node 
if [ $? == "0" ]; then
    exit 255;
fi

echo "Selected fabric node: $fabric_node"
export ROOT_NODE=($fabric_node)
SSH="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no fuse@$fabric_node"

echo $FUSE_HOME

arg='$1'

containers_info=$(timeout 15s $SSH $CLIENT_INVOCATION "container-list | grep -i $PREFIX | awk '{print $arg}; END {print empty}'")

array=(${containers_info})

for ((i=0; i<${#array[@]}; i++));
do
  if [[ ${array[$i]} == *"$container_name"* ]]; then
     echo "Container with the name: ${array[$i]} already exists."
     echo "Deleteing..."
     $($SSH "$CLIENT_INVOCATION container-delete ${array[$i]}")
  fi
done

printf -v CONTAINER_COMMAND "fabric:container-create-ssh $JVM_CONTAINER_OPTS --host $IP --path /opt/rh/containers --user fuse $profiles $container_name"
echo $($SSH "$CLIENT_INVOCATION \"$CONTAINER_COMMAND\"")

wait_for_container $container_name

sleep 10

set -x
exit 0
