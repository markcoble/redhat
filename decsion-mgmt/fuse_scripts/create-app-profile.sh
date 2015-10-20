#!/usr/bin/env bash
set +x

### Set the maven repository and create the profiles for the application
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --pid io.fabric8.agent/org.ops4j.pax.url.mvn.repositories=$MAVEN_REPOSITORY default\""

# create profile
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-create --parents jboss-fuse-full --parents feature-cxf --parents feature-camel-jms decision-service\""

# add feature repos
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --repositories mvn:org.coble.core.odm/batch-decision-service/$APP_VERSION/xml/features decision-service\""

## add features from repos
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --features batch-decision-service decision-service\""

# Create the upgraded application profiles for the release version
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --repositories mvn:batch-decision-service/$APP_VERSION/xml/features decision-service decision-service $RELEASE_VERSION\""

ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --features batch-decision-service decision-service $RELEASE_VERSION\""

