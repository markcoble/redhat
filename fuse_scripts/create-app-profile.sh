#!/usr/bin/env bash
set +x

### Set the maven repository and create the profiles for the application
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --pid io.fabric8.agent/org.ops4j.pax.url.mvn.repositories=$MAVEN_REPOSITORY default\""

# create profile
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-create --parents jboss-fuse-minimal --parents feature-cxf application\""

# add feature repos
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --repositories mvn:application/application-common-features/$APP_VERSION/xml/features application\""
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --repositories mvn:application/applicaton-features/$CONTENT_VERSION/xml/features application\""

# add features from repos
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --features mq-fabric-camel application\""
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --features appliction-feature application\""

# Create the upgraded application profiles for the release version

ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --repositories mvn:application/application-common-features/$APP_VERSION/xml/features application $RELEASE_VERSION\""
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"fabric:profile-edit --repositories mvn:application/application-features/$APP_VERSION/xml/features application $RELEASE_VERSION\""
ssh fuse@$ROOT_NODE "set +x;$HOST_RH_HOME/scripts/commands/karaf-client.sh $DEPLOYMENT_ENVIRONMENT \"profile-edit --features application-feature application\""
