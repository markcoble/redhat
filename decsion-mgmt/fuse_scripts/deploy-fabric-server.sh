#!/usr/bin/env bash
set +x

if [ "$#" -lt 1 ]; then
    echo "Illegal number of parameters."
    echo "Usage: deploy-fabric-server.sh (environment)"
    echo "Example: deploy-fabric-server.sh staging"
    exit 1
fi
export DEPLOYMENT_ENVIRONMENT="$1"

# Set the environment variables for the selected environment
. ./envs/$DEPLOYMENT_ENVIRONMENT/environment.sh
. ./lib/helper_functions.sh
CLIENT_INVOCATION="$FUSE_HOME/bin/client -u $APP_ADMIN_USER -p $APP_ADMIN_PASSWD -r 60"

IP=$(ifconfig eth1 | grep inet | awk '{print $2}' | cut -d':' -f2)
echo "Rejoining fabric IP: $IP to ensemble."

find_fabric_node 
if [ $? == "0" ]; then
    exit 255;
fi

zookeeper_urls=$(ssh fuse@$fabric_node "$CLIENT_INVOCATION 'config:edit io.fabric8.zookeeper;config:proplist|grep zookeeper.url'")
zookeeper_ips=${zookeeper_urls//-/.}
zookeeper_ips=${zookeeper_ips//,/$'\n'}
ZOOKEEPER_PASSWD=$(ssh fuse@$fabric_node "$CLIENT_INVOCATION 'fabric:ensemble-password'")

## Retrieving ensemble password from root container has a return feed for some reason. 
## Below are attempts to get a correct format.

#ZOOKEEPER_PASSWD=`echo $ZOOKEEPER_PASSWD_TEMP | tr -d $'\r'| tr -d " "`
#ZOOKEEPER_PASSWD=`echo $ZOOKEEPER_PASSWD_TEMP | tr -d $'\r'`

for url in ${zookeeper_ips[@]}
do

if [[ $url == *"$fabric_node"* ]]; then
  zookeeper_port=$(echo $url | sed 's/.*\://')
  zookeeper_url="$fabric_node:$zookeeper_port"
  echo "Zookeeper url: $zookeeper_url"
fi
done

$FUSE_HOME/bin/start
CLIENT_INVOCATION="$FUSE_HOME/bin/client -u $KARAF_USER -p $KARAF_PASSWORD -r 60"
wait_for_command_available join $IP 30
if [ $? == "0" ]; then 
	exit 255;
fi

sleep 20

if [ $IP == $ROOT_NODE ]; then
        $CLIENT_INVOCATION "join --zookeeper-password $ZOOKEEPER_PASSWD --force $fabric_node:2181 root"
else
    if [ $fabric_node == $ROOT_NODE ]; then
        $CLIENT_INVOCATION "join --zookeeper-password $ZOOKEEPER_PASSWD --force $fabric_node:2182 fabricserver-$IP"
    else
        $CLIENT_INVOCATION "join --zookeeper-password $ZOOKEEPER_PASSWD --force $fabric_node:2181 fabricserver-$IP"
    fi

fi

## restart 
$FUSE_HOME/bin/start
