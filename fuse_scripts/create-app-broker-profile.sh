#!/usr/bin/env bash
set +x

### create mq profile for nitro broker containers

printf -v CONTAINER_COMMAND "mq-create --config app.broker.config.xml --group app-brokers app-broker"
ssh fuse@$ROOT_NODE "set +x;$CLIENT_INVOCATION \"$CONTAINER_COMMAND\""

### profile mq-broker-app-brokers.app-broker created
