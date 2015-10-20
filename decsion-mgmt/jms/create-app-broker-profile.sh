#!/usr/bin/env bash
set +x

### create mq profile for batch broker containers

printf -v CONTAINER_COMMAND "mq-create --config decision.service.broker.config.xml --group dec-service-brokers batch-broker"
ssh fuse@$ROOT_NODE "set +x;$CLIENT_INVOCATION \"$CONTAINER_COMMAND\""

### profile mq-broker-dec-service-brokers.batch-broker created
