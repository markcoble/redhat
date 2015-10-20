#!/usr/bin/env bash

set -x

# Maven Repo setup locally with python -m SimpleHTTPServer
export MAVEN_REPOSITORY=http://192.168.0.36:8000/m2repo@snapshots@id=localvagrantrepo

# Host Config
export FUSE_HOSTS=(192.168.50.21 192.168.50.22 192.168.50.23)
export ROOT_NODE=(192.168.50.21)
export FABRIC_HOSTS=()
export APP_HOSTS=(192.168.50.21)
export BROKER_HOSTS=(192.168.50.22 192.168.50.23)

# Host OS paths
export HOST_RH_HOME=/opt/rh
export HOST_FUSE_HOME=/opt/rh/fuse-latest
export FUSE_HOME=/opt/rh/fuse-latest
export KARAF_USER=admin
export KARAF_PASSWORD=admin
export TEMP_DIR=/tmp

# JVM Options
export JAVA_HOME=/usr/lib/jvm/java-1.7.0
export JVM_GC_OPTS="-XX:+UseG1GC"
export JVM_BROKER_OPTS="--jvm-opts '-Xms256m -Xmx512m ${JVM_AGENT_OPTS} ${JVM_GC_OPTS}'" 
export JVM_APP_OPTS="--jvm-opts '-Xms256m -Xmx512m ${JVM_AGENT_OPTS} ${JVM_GC_OPTS}'"
export JVM_FABRIC_OPTS="--jvm-opts '-Xms256m -Xmx512m ${JVM_AGENT_OPTS}'"
