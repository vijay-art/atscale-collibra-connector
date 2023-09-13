# AtScale to Collibra Integration

A Spring Boot integration that extracts AtScale projects and other entities, transforms them and loads them
into Collibra domains.

AtScale Version Required: 2022.2.0 or above

# Instructions for Setting Up Your Environment

1. Download Collibra Spring Boot library: https://marketplace.collibra.com/listings/spring-boot-integration-library/
2. mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=collibra-integration-library-1.1.7.jar
3. mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=collibra-integration-library-1.1.7-javadoc.jar -DgroupId=com.collibra.marketplace -DartifactId=collibra-integration-library -Dversion=1.1.7 -Dpackaging=jar -Dclassifier=javadoc

# Set Up and Run the Listener

All relevant properties need to be updated in `resources/application.properties`.

The listener needs to be run first before hitting the endpoints. If using Intellij, create the following configuration:

* Name: Run Listener
* JDK: Java 1.8
* Main Class: com.collibra.marketplace.atscale.Application
* Parameters: --atscale.api.password=<password> --trigger.api.password=<password> --collibra.password=<password>

Note: If more verbose debug messages are desired add parameter: --atscale.debug=true

# Example Script to Run Listener

Here is an example shell script to run the listener with all the needed parameters.

```
#!/bin/bash

echo "Starting atscale-to-collibra-integration listener"

java -jar atscale-to-collibra-integration-1.0.0.jar \
--server.port=8081 \
--trigger.api.username=<atscale-superuser-user-here> \
--trigger.api.password=<atscale-superuser-password-here> \
--collibra.url=<collibra-url-here> \
--collibra.username=<collibra-user-here> \
--collibra.password=<collibra-password-here> \
--atscale.api.dchost=<atscale-server-url-here> \
--atscale.api.dcport=dummyPort \
--atscale.api.apihost=<atscale-server-url-here> \
--atscale.api.apiport=dummyPort \
--atscale.api.authhost=<atscale-server-url-here> \
--atscale.api.authport=dummyPort \
--atscale.api.username=<atscale-superuser-user-here> \
--atscale.api.password=<atscale-superuser-password-here> \
--atscale.api.disablessl= \
--atscale.debug=false \
--atscale.filter.project= \
```

# Running Setup

Setup is required to be run once before any extractions to create the object types with GUID's that are required by AtScale in the Collibra platform. 

For example: `curl -X POST http://localhost:8081/api/setup -u "<userName>:<password>"` (use basic auth with credentials defined in application.properties file)

# Running a Sync (Extraction)

If running with a single organization use the following:

For example: (use basic auth with credentials defined in application.properties file)

`curl -X POST http://localhost:8081/api/sync -u "<userName>:<password>"`

If running with multiple organizations, each can be hit separately. For example to run on both `<defaultUser>` and `<otherUser>`

`curl -X POST http://localhost:8081/api/sync?orgName=<orgName>&orgId=<orgId> -u "<userName>:<password>"`

`curl -X POST http://localhost:8081/api/sync?orgName=<orgName>&orgId=<00000000-0000-0000-0000-000000000000> -u "<userName>:<password>"`

# Example Script to Run Sync (Extraction) for Multiple Orgs

Here is an example shell script to run a sync for multiple orgs.

```
#!/bin/bash

echo "Syncronizing metadata between AtScale and Collibra"

curl -X POST http://localhost:8081/api/sync?orgName=<defaultUser>&orgId=<defaultUser> -u "<userName>:<password>"
curl -X POST http://localhost:8081/api/sync?orgName=<otherUser>&orgId=<addOrgId>-u "<userName>:<password>"

```

# Endpoints available for testing purposes

Remove AtScale assets with "obsolete" status:  http://localhost:8081/api/removeobsolete

Remove all AtScale assets from both the business and data domains:  http://localhost:8081/api/removeassets

Remove all AtScale types (assets, attributes, relations and domains):  http://localhost:8081/api/removetypes

# Instructions for Packaging the Connector

1. In IntelliJ, Execute Maven Goal: `mvn clean`
2. In IntelliJ, Execute Maven Goal: `mvn compile`
3. In IntelliJ, Execute Maven Goal: `mvn package`

# Instructions for Running the Instance Outside of IntelliJ

`java -jar target/atscale-to-collibra-integration-1.0.0.jar --atscale.api.apihost=engine-stg.int.insights.atscale.com`

NOTE: To override properties, notice the `--parameter.name=value` syntax

# About Naming Data Sources

In order for physical assets (schemas, tables, columns) to be recognized for relationships they must be named as `<database>.<schema>` or just `<schema>` if there is no database. Examples:
* BigQuery:  atscale-data-warehouse.Demo
* Snowflake:  ATSCALE_SAMPLE_DATA.atscale

An "AtScale Column" is defined as either a calculated column or a column on a query dataset. Each are defined by SQL and not a directly physical column on a table. In the case of calculated columns, AtScale columns may have a relationship to physical columns. When on a query dataset, they will not.
