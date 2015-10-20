 #!/usr/bin/env bash
set +x

host_num=0
karaf_commands

# spawn the nitro [content] containers
for i in ${APP_HOSTS[@]}; do
	host_num=$(($host_num + 1))
	printf -v CONTAINER_COMMAND "fabric:container-create-ssh $JVM_APP_OPTS --host ${i} --path /opt/rh/containers --user fuse --profile application app-container-$host_num"
	ssh fuse@$ROOT_NODE "set -x;$HOST_RH_HOME/scripts/envs/$DEPLOYMENT_ENVIRONMENT/environment.sh;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"$CONTAINER_COMMAND\""
        wait_for_container app-container-$host_num
done
