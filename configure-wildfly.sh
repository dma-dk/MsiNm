#!/bin/sh

WILDFLY=wildfly-8.2.0.Final

cp conf/standalone.xml $WILDFLY/standalone/configuration/
mkdir -p $WILDFLY/modules/com/mysql/main
cp conf/module.xml $WILDFLY/modules/com/mysql/main/
cp conf/mysql-connector-java-5.1.30-bin.jar $WILDFLY/modules/com/mysql/main/
chmod +x $WILDFLY/bin/*.sh


