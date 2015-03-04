#!/usr/bin/env bash

# This set of library functions depends on the following environment variables being set 
# FUSE_HOME
# TMP_FABRIC_GIT_REPO
# FEATURES_PROJECT

# Set colours

GREEN="\e[32m"
RED="\e[41m\e[37m\e[1m"
YELLOW="\e[33m"
WHITE="\e[0m"


## find the first healthy member of a previously created fabric ensemble.  
function find_fabric_node()
{

	## TODO:  pretty sure this worked at one point but currently the following line only returns the $ROOT_NODE
	## fabric_nodes=($ROOT_NODE ${FABRIC_HOSTS[@]})
	## Replacing it now with:
 
	fabric_nodes[0]=$ROOT_NODE
	fabric_nodes[1]=${FABRIC_HOSTS[0]}
	fabric_nodes[2]=${FABRIC_HOSTS[1]}

	## which is ok it just assumes a fabric of 3 members.  Would like to revert to the previous way as that would work with any number of FABRIC_HOSTS
	
	IP=$(ifconfig eth1 | grep inet | awk '{print $2}' | cut -d':' -f2)

	if [ -z $IP ]; then
		IP=$(ifconfig eth0 | grep inet | awk '{print $2}' | cut -d':' -f2)
	fi

	for node in ${fabric_nodes[@]}
	do
	if [ $IP != $node ]; then
		wait_for_command_available container-list $node 10
	
		if [ $? == "1" ]; then
		    fabric_node=$node
		    return 1;
		fi
	fi
	done
	return 0
}

## useful function for initial fabric ensemble creation
function check_fabric_ensemble()
{
	## execute fabric:ensemble list command to find member names
        ENSEMBLE_LIST=$(ssh fuse@${ROOT_NODE[0]} "$CLIENT_INVOCATION 'ensemble-list'")
        ENSEMBLE_LIST_ARRAY=(${ENSEMBLE_LIST//\\n/})

	## ensemble-list command returns [id] header so reduce actual count by one. 
	num_live_nodes=${#ENSEMBLE_LIST_ARRAY[@]}
	((num_live_nodes=num_live_nodes-1))
	((ensemble_size=${#FABRIC_HOSTS[@]}+1))

        if [ $num_live_nodes -eq $ensemble_size ]; then
        	echo "healthy ensemble"
		return $num_live_nodes
        fi

        echo "broken ensemble values are: ENSEMBLE_LIST_ARRAY: ${ENSEMBLE_LIST_ARRAY[@]},num_live_nodes=$num_live_nodes,ensemble_size=$ensemble_size"
        return $num_live_nodes
}


# Kill all running karaf instances
function kill_karaf_instances()
{
	echo "Killing all karaf processes"
	ps -e -opid,command | grep "org.apache.karaf.main.Main" | grep -v grep | awk '{ print $1; }' | xargs kill  -KILL 2> /dev/null
}

function wait_for_command_available() {
	local COMM=$1
	local FABRIC_NODE=$2
	local 
	local _MAX_WAITING=$3
	set +x
	local time_elapsed=0
	local is_command_present=0
	echo -n -e $YELLOW"Waiting (max ${_MAX_WAITING}s) for \"$COMM\" command to be available..."$WHITE
	echo ""
	while (( $is_command_present <= 0 ))
	do
		sleep 0.5
		time_elapsed=$((1 + $time_elapsed))
		printf "\r%-2d sec before forced timeout..." $(( $_MAX_WAITING - ( $time_elapsed / 2 ) ))
 		is_command_present=$(timeout 15s ssh fuse@$2 "$CLIENT_INVOCATION '$COMM --help'  2> /dev/null")
		echo "$is_command_present"
		is_command_present=$(echo "$is_command_present" | grep  -F -c SYNTAX )

		if (( ($time_elapsed / 2 ) >= $_MAX_WAITING )) 
			then 
			echo -e $RED"Waited $(($time_elapsed  / 2)) for command $COMM. Launch aborted. "$WHITE
			return $is_command_present
		fi
	done

	echo ""
	echo -e $GREEN"$COMM became available in $(($time_elapsed  / 2)) seconds"$WHITE
	set -x
	return $is_command_present
}

function wait_for_container() {
	local CONTAINER=$1
	local _MAX_WAITING=180
	set +x
	local time_elapsed=0
	local is_container_available=0
	local is_container_in_error=0
	local container_exists=0

	echo -n -e $YELLOW"Waiting (max ${_MAX_WAITING}s) for \"$CONTAINER\" container to be available..."$WHITE
	echo ""
	while (( $is_container_available <= 0 ))
	do
		sleep 0.5
		time_elapsed=$((1 + $time_elapsed))
		#printf "\r%-2d sec before forced timeout..." $(( $_MAX_WAITING - ( $time_elapsed / 2 ) ))

		if (( $container_exists == 0)) ; then
			#skip if container doesn't exists at all in this fabric
			container_exists=$(ssh fuse@${ROOT_NODE[0]} "$CLIENT_INVOCATION 'container-info $CONTAINER' | grep -c -i 'Container $CONTAINER does not exists!'")
			if (( $container_exists == 1 )) ; then
				echo -e $YELLOW"Container \"$CONTAINER\" not member of this fabric. SKIPPED."$WHITE
				return
			fi
			container_exists=1
		fi

		status_message=$(ssh fuse@${ROOT_NODE[0]} "$CLIENT_INVOCATION 'container-info $CONTAINER | grep  -i status'"  2> /dev/null)
		printf "\r%-2d sec before forced timeout. Current Status: [%-60s] " "$(( $_MAX_WAITING - ( $time_elapsed / 2 ) ))" "$status_message"

		is_container_in_error=$(echo "$status_message" | grep -c -i error )

		if (( $is_container_in_error == 1 )) ; then
			echo ""
			echo -e $RED"!!!!!!!!!!!!!! $CONTAINER is not able to start. Check for errors !!!!!!!!!!!!!!!!!"$WHITE
			echo -e $RED"Current status: $status_message"$WHITE
			exit -1
		 fi

	    is_container_available=$(echo "$status_message" | grep -c -i success )
		if (( ($time_elapsed / 2 ) >= $_MAX_WAITING ))
			then
			echo -e $RED"Waited $(($time_elapsed  / 2)) seconds for container $COMM. Launch aborted. "$WHITE
			exit 1
		fi
	done
	echo ""
	failed_bundles=$(ssh fuse@${ROOT_NODE[0]} "$CLIENT_INVOCATION 'container-connect $CONTAINER list' | grep -F -i Failed" 2> /dev/null)
	if [ "x$failed_bundles" != "x" ]; then
		echo -e $RED"$CONTAINER bundles in status \"Failed\": $failed_bundles"$WHITE
	fi
	echo -e $GREEN"$CONTAINER became available in $(($time_elapsed  / 2)) seconds"$WHITE
	set -x
} # wait_for_container

function wait_for_ensemble() {

	local _MAX_WAITING=180
	set +x
	local time_elapsed=0
	local is_ensemble_available=0
	local ensemble_exists=0
	((ensemble_size=${#FABRIC_HOSTS[@]}+1))

	echo -n -e $YELLOW"Waiting (max ${_MAX_WAITING}s) for fabric ensemble with" $ensemble_size "servers to be available..."$WHITE
	echo ""
	while (( $is_ensemble_available <= 0 ))
	do
		sleep 0.5
		time_elapsed=$((1 + $time_elapsed))
		#printf "\r%-2d sec before forced timeout..." $(( $_MAX_WAITING - ( $time_elapsed / 2 ) ))

		status_message=$(check_fabric_ensemble)
		printf "\r%-2d sec before forced timeout. Current Status: [%-60s] " "$(( $_MAX_WAITING - ( $time_elapsed / 2 ) ))" "$status_message"

	    	is_ensemble_available=$(echo "$status_message" | grep -c -i healthy )
		if (( ($time_elapsed / 2 ) >= $_MAX_WAITING ))
			then
			echo -e $RED"Waited $(($time_elapsed  / 2)) seconds for ensemble. Launch aborted. "$WHITE
			exit 1
		fi
	done
	echo ""

	echo -e $GREEN"Ensemble became available in $(($time_elapsed  / 2)) seconds"$WHITE
	set -x
} # wait_for_ensemble

function ssh_copy_scripts() 
{
	ssh fuse@$1 "rm -fr $HOST_RH_HOME/scripts"
	ssh fuse@$1 "mkdir $HOST_RH_HOME/scripts"
	scp -rp ./ fuse@$1:$HOST_RH_HOME/scripts
} # ssh_copy_scripts

function ssh_kill_fuse() 
{	
	ssh fuse@$1 "export HOST_RH_HOME=$HOST_RH_HOME;$HOST_RH_HOME/scripts/commands/kill-fuse.sh $DEPLOYMENT_ENVIRONMENT"
} # ssh_kill_fuse()

# Kill the karaf container
function ssh_clear_karaf_and_containers()
{
	echo "Clearing down intermediate karaf data directories and containers"
	ssh fuse@$1 "export HOST_RH_HOME=$HOST_RH_HOME;$HOST_RH_HOME/scripts/commands/clear_karaf.sh $FUSE_HOME"
	ssh fuse@$1 "rm -fr $HOST_RH_HOME/containers /opt/rh/kahadb/*"
}


#$RELEASE_VERSION $ROOT_NODE $DEPLOYMENT_ENVIRONMENT
function ssh_bootstrap_fuse()
{
	VAR_RELEASE_VER=$1
	VAR_ROOT_NODE=$2
	VAR_ENV=$3 
	
	ssh fuse@$VAR_ROOT_NODE "set +x;$HOST_RH_HOME/scripts/envs/$VAR_ENV/environment.sh;$HOST_RH_HOME/scripts/commands/launch-fuse.sh $VAR_ENV"

	ssh fuse@$VAR_ROOT_NODE "set +x;$CLIENT_INVOCATION \"wait-for-service -t 300000 io.fabric8.api.BootstrapComplete\""

	## create fabric	
	ssh fuse@$VAR_ROOT_NODE "set +x;$CLIENT_INVOCATION \"fabric:create -b $VAR_ROOT_NODE --clean --generate-zookeeper-password --resolver manualip --manual-ip $VAR_ROOT_NODE  --profile fabric --wait-for-provisioning\""

	wait_for_container root

	## remove old admin user
	ssh fuse@$VAR_ROOT_NODE "set +x;$CLIENT_INVOCATION \"jaas:manage --realm karaf --module io.fabric8.jaas.ZookeeperLoginModule;userdel $KARAF_USER;useradd $APP_ADMIN_USER $APP_ADMIN_PASSWD;roleadd $APP_ADMIN_USER admin;jaas:update\""

	## reset CLIENT_INVOCATION to use new application admin user credentials
	export KARAF_USER=$APP_ADMIN_USER
	export KARAF_PASSWORD=$APP_ADMIN_PASSWD
	karaf_commands

	## set zookeeper password to that generated by fabric:create
	export ZOOKEEPER_PASSWD=$(ssh fuse@$VAR_ROOT_NODE "set +x;$CLIENT_INVOCATION \"fabric:ensemble-password\"")
	
}


function use_new_relic () 
{
	if [ "${WITH_NEW_RELIC:-null}" = null ]; then
	  FUSE_JVM_OPTS=''
	else
	  echo "Adding New Relic agent"
	  FUSE_JVM_OPTS='--jvm-opts \"-javaagent:/opt/rh/new-relic-agent/newrelic.jar\"'
	fi
}


# 3 args [$1 -> SSH_NODE, $2 -> PROJECT_VERSION, $3 -> ENVIRONMENT
function ssh_update_git 
{
	SUG_HOST=$1
	SUG_VERSION=$2
	SUG_ENV=$3
	
	ssh fuse@$SUG_HOST "$HOST_RH_HOME/scripts/commands/update-git.sh $SUG_HOST $SUG_VERSION $SUG_ENV"
}

# 2 args [ $1 -> host, $2 -> version, $3 environment] 
function update_git
{
	ROOT_HOST=$1
	UG_VERSION=$2
	UG_ENV=$3
	
        #Only one member of fabric ensemble will host git repo.  fabric:cluster-list git command returns the elected host.
        UG_HOST=$($FUSE_HOME/bin/client -u $KARAF_USER -p $KARAF_PASSWORD fabric:cluster-list git |grep http://|awk '{print $NF;}'|cut -c 8-)

        echo "UG HOST $UG_HOST"

	pushd $TEMP_DIR
	rm -rf fabric
	
	git clone -b $UG_VERSION http://$KARAF_USER:$KARAF_PASSWORD@$UG_HOST

	# Push the (tmg.broker.config.xml into the base mq profile)
	pushd $HOST_RH_HOME/scripts/envs/$UG_ENV/config
	cp *.* $TEMP_DIR/fabric/fabric/profiles/mq/base.profile
	
	pushd $HOST_RH_HOME/scripts/envs/$UG_ENV	

	pushd $HOST_RH_HOME/scripts/envs/$UG_ENV/properties
	cp *.* $TEMP_DIR/fabric/fabric/profiles/application.profile/


	# Push the result into fabric and create a version
	pushd $TEMP_DIR/fabric

	git config user.email "deployer@telegraph.co.uk"
	git config user.name "Autodeployer"
	git add -v $TEMP_DIR/fabric
	git commit -avm "Added new properties"
	git push origin $UG_VERSION
	
	pushd $TEMP_DIR
	rm -rf fabric
}

function karaf_client
{
	karaf_commands
	
	# Flatten the arguments to a single string to avoid interpretation by the  
	command_string=""
	for a in "${@}"; do 
		command_string+="$a "
	done
	
	echo "KARAF COMMAND: ssh fuse@${ROOT_NODE[0]} $CLIENT_INVOCATION \"$command_string\""
	ssh fuse@${ROOT_NODE[0]} "$CLIENT_INVOCATION \"$command_string\""

}

function start_fuse()
{
	start_and_wait_for_karaf
}


function start_and_wait_for_karaf()
{
	
    echo "Launching Fuse"
    echo "Fuse home: '$FUSE_HOME'"

    karaf_commands

    (ssh fuse@${ROOT_NODE[0]} "$FUSE_HOME/bin/start $1")


    i=0.0
    c=0
    sleeptime=1

    echo -n "Waiting for Fuse to become available..."
    while [ $c -le 0 ]
    do
        sleep $sleeptime
        i=$(echo $sleeptime | bc)
        echo -n .
        echo "$FUSE_HOME/bin/client -u $KARAF_USER -p $KARAF_PASSWORD"
        c=$(ssh fuse@${ROOT_NODE[0]} "$CLIENT_INVOCATION help 2> /dev/null| grep fabric:create | wc -l")
    done
}

function clear_karaf_container() 
{
	rm -fr $1/instances $1/data $1/lock $1/processes
}

function karaf_commands() {
	
	CLIENT_INVOCATION="$FUSE_HOME/bin/client -u $KARAF_USER -p $KARAF_PASSWORD -r 60"

}	
