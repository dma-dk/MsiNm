# MSI-NM #

A demonstration site for managing and publishing MSI and NM messages.

## Prerequisites
* Java JDK 1.8
* Maven 3.x
* JBoss Wildfly 8.1.0.Final or later
* MySQL

## Initial setup

### MySQL
Set up an "msi" database with an msi/msi user (in test).

Alternatively, in the root of MsiNm, run:

    mysql -u root -p < db/create-database.sql

### JBoss Wildfly
Install and configure the Wildfly application server by running:

    ./install-widlfly.sh
    ./configure-widlfly.sh

You can now run the Wildfly app server using:

    ./wildfly-8.1.0.Final/bin/standalone.sh


## Deploying to Wildfly

Build the project and deploy e.g. the Danish flavoured MSI-NM web application:

    mvn clean install
    cd msinm-web-dk
    mvn wildfly:deploy

You can now access the web application on: http://localhost:8080/

### Remote deployment

Configure management user

    ./wildfly-8.1.0.Final/bin/add-user.sh 
    
Make wildfly listen on public interfaces

    ./wildfly-8.1.0.Final/bin/standalone.sh -b=0.0.0.0 -bmanagement=0.0.0.0
    
To deploy

    mvn wildfly:deploy -Dwildfly.hostname=remote-server
    
Available properties

    wildfly.hostname
    wildfly.port
    wildfly.username
    wildfly.password

## Idea Intellij setup

* Setup JDK 1.8 if not already present.
* Import the MsiNm pom.xml in Intellij.
* Configure a new application server pointing to the Wildfly installation, and with msinm-web:war as the deployment unit.
* Run the server
* To improve the development experience, define an msi database and set up the msinm-web persistence unit to use it.
* If you want to be able to use the JPA-QL console, use the test-persistence.xml file of the msinm-web module.


