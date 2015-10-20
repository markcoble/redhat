#!/usr/bin/env bash
set +x

karaf_commands

# Kill and clean fuse from all fuse hosts
echo "Cleaning up ${FUSE_HOSTS[@]}"

for i in ${FUSE_HOSTS[@]}; do
	ssh_copy_scripts "${i}"
	ssh_kill_fuse "${i}"
	ssh_clear_karaf_and_containers "${i}"
done

# Seed the root node with the latest version of the scripts, launch the root fuse node, and create the initial fabric
ssh_bootstrap_fuse $RELEASE_VERSION $ROOT_NODE $DEPLOYMENT_ENVIRONMENT

num_fabric_nodes=${#FABRIC_HOSTS[@]}

echo "NUMBER OF FABRIC NODES IS : " $num_fabric_nodes
 
# If we have a 3 or 5 node fabric add nodes to the fabric and create the ensemble
fabric_host_num=1

if [[ $num_fabric_nodes = 2 || $num_fabric_nodes = 4 ]]; then	
	
	for i in ${FABRIC_HOSTS[@]}; do
		printf -v CONTAINER_COMMAND "fabric:container-create-ssh --host ${i} --path /opt/rh/containers --user fuse --profile fabric fabricserver-$i"
		ssh fuse@$ROOT_NODE "set +x;$CLIENT_INVOCATION '$CONTAINER_COMMAND'"
                wait_for_container fabricserver-$i
		((fabric_host_num=fabric_host_num+1))
	done
	
	## add admin user back temporarily to create ensemble
	ssh fuse@$VAR_ROOT_NODE "set +x;$CLIENT_INVOCATION \"jaas:manage --realm karaf --module io.fabric8.jaas.ZookeeperLoginModule;useradd admin $ZOOKEEPER_PASSWD;roleadd admin admin;jaas:update\""

	fabric_ensemble_string="fabric:ensemble-add -f "
	
	fabric_host_num=1	
	
	for i in ${FABRIC_HOSTS[@]}; do
		fabric_ensemble_string+="fabricserver-$i "
		((fabric_host_num=fabric_host_num+1))
	done
	
	echo "Fabric Join String is :" $fabric_ensemble_string
	ssh fuse@$ROOT_NODE "set +x;$CLIENT_INVOCATION '$fabric_ensemble_string'"

	wait_for_ensemble
fi

	## remove temp admin user
	ssh fuse@$VAR_ROOT_NODE "set +x;$CLIENT_INVOCATION \"jaas:manage --realm karaf --module io.fabric8.jaas.ZookeeperLoginModule;userdel admin;jaas:update\""

# Create fabric version for release
ssh fuse@$ROOT_NODE "set -x;$CLIENT_INVOCATION 'fabric:version-create --default $RELEASE_VERSION'"
  
