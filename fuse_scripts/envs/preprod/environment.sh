#!/usr/bin/env bash

set -x

# Maven Repo
export MAVEN_REPOSITORY=http://nexus.aws.application/nexus/application/repositories/public@id=repositorypublic


amq_hosts=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-amq*" | grep PRIVATEIPADDRESSES | awk ' { print $4; }' | tr \\n ' '`

app_hosts=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-app*" | grep PRIVATEIPADDRESSES | awk ' { print $4; }' | tr \\n ' '`

fabric1_dyn=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-fabric1*" | grep NETWORKINTERFACES | grep -v az-preprod-fabric1 | grep -oE '((1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\.){3}(1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])'`
fabric1_static=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-fabric1*" | grep NETWORKINTERFACES | grep az-preprod-fabric1 | grep -oE '((1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\.){3}(1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])'`

fabric2_dyn=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-fabric2*" | grep NETWORKINTERFACES | grep -v az-preprod-fabric2 | grep -oE '((1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\.){3}(1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])'`
fabric2_static=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-fabric2*" | grep NETWORKINTERFACES | grep az-preprod-fabric2 | grep -oE '((1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\.){3}(1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])'`

fabric3_dyn=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-fabric3*" | grep NETWORKINTERFACES | grep -v az-preprod-fabric3 | grep -oE '((1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\.){3}(1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])'`
fabric3_static=`aws --region=eu-west-1 --output=text ec2 describe-instances --filter "Name=tag-value,Values=az-preprod-fabric3*" | grep NETWORKINTERFACES | grep az-preprod-fabric3 | grep -oE '((1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])\.){3}(1?[0-9][0-9]?|2[0-4][0-9]|25[0-5])'`

# Host Config
export FUSE_HOSTS=($amq_hosts $app_hosts $fabric1_static $fabric2_static $fabric3_static)
export ROOT_NODE=($fabric1_static)
export FABRIC_HOSTS=($fabric2_static $fabric3_static)

export APP_HOSTS=($app_hosts)
export BROKER_HOSTS=($amq_hosts)

# Host OS paths
export HOST_RH_HOME=/opt/rh
export HOST_FUSE_HOME=/opt/rh/fuse-latest
export FUSE_HOME=/opt/rh/fuse-latest
export TEMP_DIR=/tmp

# Karaf and application user credentials
export APP_ADMIN_USER=hawtio	
export APP_ADMIN_PASSWD=hawtio
export KARAF_USER=admin
export KARAF_PASSWORD=admin

# JVM Options
export JAVA_HOME=/usr/lib/jvm/java-1.7.0
export JVM_GC_OPTS="-XX:+UseG1GC"
export JVM_AGENT_OPTS="-javaagent:/opt/rh/newrelic/newrelic.jar"
export JVM_BROKER_OPTS="--jvm-opts '-Xms4096m -Xmx4096m ${JVM_AGENT_OPTS} ${JVM_GC_OPTS}'" 
export JVM_APP_OPTS="--jvm-opts '-d64 -Xms16384m -Xmx16384m ${JVM_AGENT_OPTS} ${JVM_GC_OPTS}'"
export JVM_FABRIC_OPTS="--jvm-opts '-Xms256m -Xmx512m ${JVM_AGENT_OPTS}'"
