#!/bin/sh

#
# Installs a wildfly instance that can be used for development purposes
#

pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
echo "Running in folder $SCRIPTPATH"

echo "Please enter Wildfly admin user password"
read PWD

WILDFLY=wildfly-9.0.1.Final
WILDFLY_PATH=../$WILDFLY
BASE_DIR=$(pwd)

# NB: We could download from JBoss, but using maven makes it faster because it is fetched from local maven repo...
echo "Installing $WILDFLY"
rm -rf $WILDFLY_PATH
mvn package -f install-wildfly-pom.xml -P install-wildfly -DskipTests
rm -rf  target

echo "Configuring Wildfly"
chmod +x $WILDFLY_PATH/bin/*.sh
$WILDFLY_PATH/bin/jboss-cli.sh --file=configure-wildfly.txt
$WILDFLY_PATH/bin/add-user.sh ci "$PWD" --silent

popd > /dev/null