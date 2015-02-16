#!/usr/bin/env bash
set +x

host_num=0
karaf_commands

# spawn the brokers
for i in ${BROKER_HOSTS[@]}; do
	host_num=$(($host_num + 1))
	printf -v CONTAINER_COMMAND "fabric:container-create-ssh $JVM_BROKER_OPTS --host ${i} --path /opt/rh/containers --user fuse application-broker-$host_num"
	ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"$CONTAINER_COMMAND\""
        wait_for_container application-broker-$host_num
done

printf -v CONTAINER_COMMAND "mq-create --config application.broker.config.xml --assign-container application-broker-1,application-broker-2 --group application-brokers application-broker"

# TODO: optimize this to use fabric client commands to determing availability of nitro broker.
echo "Sleeping for 90 sec to allow for broker creation..."
sleep 90

ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"$CONTAINER_COMMAND\""


