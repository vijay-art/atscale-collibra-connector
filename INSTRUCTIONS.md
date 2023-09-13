# AtScale to Collibra Integration

A Spring Boot java integration that extracts AtScale projects and other entities, transforms them and loads them
into Collibra domains.

AtScale Version Required: 2022.2.0 or above

# Set Up and Run the Listener

The command for running the listener is in `update_metadata.sh`. To run it:

`sh run_listener.sh`

It includes the following relevant properties that need to be updated.
* server.port=dummyPort
* trigger.api.username=dummy
* trigger.api.password=dummy
* collibra.url=https://atscale.collibra.com
* collibra.username=dummy
* collibra.password=dummy
* atscale.api.dchost=dummyHost
* atscale.api.dcport=dummyPort
* atscale.api.apihost=dummyHost
* atscale.api.apiport=dummyPort
* atscale.api.authhost=dummyHost
* atscale.api.authport=dummyPort
* atscale.api.username=dummy
* atscale.api.password=dummy
* atscale.api.disablessl=

The following additional properties may need to be overwritten in the script if the values are different for you.
* collibra.business.analysts.community.id=00000000-0000-0000-0000-000000000000
* collibra.schemas.community.id=00000000-0000-0000-0000-000000000000
* collibra.asset.type.Schema=00000000-0000-0000-0000-000000000000
* collibra.asset.type.Table=00000000-0000-0000-0000-000000000000
* collibra.asset.type.column=00000000-0000-0000-0000-000000000000
* atscale.api.raylight-path=/raylight/v1
* atscale.api.page.limit=50
* atscale.community.id=   (if community other than default Business Analysts Community is to be used)
* collibra.business.asset.domaintype=00000000-0000-0000-0000-000000000000
* collibra.data.asset.domaintype=00000000-0000-0000-0000-000000000000
* collibra.physical.data.domaintype=00000000-0000-0000-0000-000000000000
* atscale.debug=true  (by default verbose messages are not logged)

A simple script is also included to run the setup process then sync 2 organizations (details following). It needs to be update for the organization(s) to be included. Details are included in the next 2 sections.

`sh update_metadata.sh`

# Running Setup

Setup is required to be run once before any extractions to create the object types with GUID's that are required by AtScale in the Collibra platform. 

http://localhost:8081/api/setup

# Running an Extraction

If running with multiple organizations, each needs to be hit separately. For example to run on both `<addDefaultUser>` and `<addOrgId>` with UUID `<addUUID>`

http://localhost:8081/api/sync?orgName=<orgName>&orgId=<orgId>
http://localhost:8081/api/sync?orgName=<orgName>&orgId=<00000000-0000-0000-0000-000000000000>

If more than one AtScale instance is to be imported, restarting the listener is required to update the settings.

# Endpoints available for testing purposes

Remove AtScale assets with "obsolete" status:  http://localhost:8081/api/removeobsolete

Remove all AtScale assets from both the business and data domains:  http://localhost:8081/api/removeassets

Remove all AtScale types (assets, attributes, relations and domains):  http://localhost:8081/api/removetypes

# Naming Data Sources

In order for physical assets (schemas, tables, columns) to be recognized for relationships they must be named as `<database>.<schema>` or just `<schema>` if there is no database. Examples:
* BigQuery:  atscale-data-warehouse.Demo
* Snowflake:  ATSCALE_SAMPLE_DATA.atscale

# Metadata Model

![Metadata Model](https://github.com/AtScaleInc/atscale-collibra-connector/blob/main/AtScaleMetadataModelInCollibra.png?raw=true)

The following relationships are created in the connector.
* ATSCALE_LEVEL_CONTAINS_SECONDARY
* ATSCALE_HIERARCHY_CONTAINS_LEVEL
* ATSCALE_FOLDER_GROUPS_HIERARCHY
* ATSCALE_DIMENSION_CONTAINS_HIERARCHY
* ATSCALE_CUBE_CONTAINS_DIMENSION
* ATSCALE_CUBE_CONTAINS_MEASURE
* ATSCALE_CUBE_CONTAINS_FOLDER
* ATSCALE_PROJECT_CONTAINS_CUBE
* ATSCALE_ORG_CONTAINS_PROJECT
* ATSCALE_INSTANCE_CONTAINS_ORG
* ATSCALE_DATASET_CONTAINS_ATSCALE_COLUMN
* ATSCALE_MEASURE_USES_LEVEL
* ATSCALE_MEASURE_USES_SECONDARY
* ATSCALE_MEASURE_USES_MEASURE
* ATSCALE_FOLDER_GROUPS_MEASURE
* ATSCALE_LEVEL_USES_ATSCALE_COLUMN
* ATSCALE_SECONDARY_USES_ATSCALE_COLUMN
* ATSCALE_MEASURE_USES_ATSCALE_COLUMN
* ATSCALE_LEVEL_USES_PHYSICAL_COLUMN
* ATSCALE_SECONDARY_USES_PHYSICAL_COLUMN
* ATSCALE_COLUMN_USES_PHYSICAL_COLUMN
* ATSCALE_MEASURE_USES_PHYSICAL_COLUMN
* ATSCALE_DATASET_USES_TABLE
* ATSCALE_DATASET_USES_SCHEMA
* ATSCALE_PROJECT_USES_CONNECTIONGROUP
* ATSCALE_HIERARCHY_USES_DATASET
* ATSCALE_CONNECTIONGROUP_CONTAINS_CONNECTION
* ATSCALE_CONNECTIONGROUP_CONTAINS_DATASET

An "AtScale Column" is defined as either a calculated column or a column on a query dataset. Each are defined by SQL and not a directly physical column on a table. In the case of calculated columns, AtScale columns may have a relationship to physical columns. When on a query dataset, they will not.

# What's Missing

* Mapping from columns on a query dataset to physical columns. Need to investigate using Collibra lineage for this. Or enhance the DMV query for dependencies to add this which be complicated.
