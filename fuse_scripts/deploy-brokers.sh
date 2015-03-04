#!/usr/bin/env bash
set +x

host_num=0
karaf_commands

# spawn the brokers
for i in ${BROKER_HOSTS[@]}; do
	host_num=$(($host_num + 1))
	printf -v CONTAINER_COMMAND "fabric:container-create-ssh $JVM_BROKER_OPTS --host ${i} --path /opt/rh/containers --user fuse --profile mq-broker-app-brokers.app-broker app-broker-$host_num"
	ssh fuse@$ROOT_NODE "set +x;$CLIENT_INVOCATION \"$CONTAINER_COMMAND\""
        wait_for_container app-broker-$host_num
done
