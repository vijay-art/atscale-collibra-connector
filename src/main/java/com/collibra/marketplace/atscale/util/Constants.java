package com.collibra.marketplace.atscale.util;

import java.util.Arrays;
import java.util.List;

public class Constants {

  public static final String BEFORE_QUERY =
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                  "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                  "  <Body>\n" +
                  "    <Execute xmlns=\"urn:schemas-microsoft-com:xml-analysis\">\n" +
                  "      <Command>\n" +
                  "        <Statement>";
  public static final String AFTER_QUERY =
          "</Statement>\n" +
                  "      </Command>\n" +
                  "      <Properties>\n" +
                  "        <PropertyList>\n" +
                  "          <DbpropMsmdSubqueries>2</DbpropMsmdSubqueries>\n" +
                  "          <LocaleIdentifier>1033</LocaleIdentifier>\n" +
                  "          <Format>Tabular</Format>\n" +
                  "          <Content>SchemaData</Content>\n" +
                  "          <DbpropMsmdActivityID>2305a0fa-ddc8-499c-a815-04f2cfdb8ddc</DbpropMsmdActivityID>\n" +
                  "          <DbpropMsmdRequestID>1659f75d-0e2c-4864-a9b7-b61f92f9bb72</DbpropMsmdRequestID>\n" +
                  "        </PropertyList>\n" +
                  "      </Properties>\n" +
                  "    </Execute>\n" +
                  "  </Body>\n" +
                  "</Envelope>";
  public static final String AFTER_CATALOG_QUERY =
          "</Statement>\n" +
                  "      </Command>\n" +
                  "    </Execute>\n" +
                  "  </Body>\n" +
                  "</Envelope>";

  // TODO: ATSCALE DMV QUERIES DON'T ACCEPT 'ORDER BY' CLAUSE OR 'IN' CLAUSE (CHANGED TO '=' AND REMOVED 'ORDER BY')

  public static final String PROJECT_QUERY = "SELECT [CATALOG_NAME], [CATALOG_GUID], [ROLES], [DESCRIPTION], [DATE_MODIFIED], [COMPATIBILITY_LEVEL], [TYPE], [VERSION], [DATABASE_ID], [DATE_QUERIED], [CURRENTLY_USED] from $system.DBSCHEMA_CATALOGS";
  public static final String CUBE_SELECTIVE_QUERY =
          "SELECT [CUBE_NAME], [CUBE_GUID], [CATALOG_NAME], [BASE_CUBE_NAME], [SCHEMA_NAME], [CUBE_TYPE], [CREATED_ON], [LAST_SCHEMA_UPDATE], [LAST_DATA_UPDATE], [DATA_UPDATED_BY], [DESCRIPTION], [CUBE_CAPTION], [CUBE_SOURCE], [IS_DRILLTHROUGH_ENABLED], [PREFERRED_QUERY_PATTERNS] from $system.MDSCHEMA_CUBES WHERE [CUBE_NAME] = '%s'";
  public static final String CUBE_NAME_FROM_CATALOG_SELECTIVE_QUERY =
          "SELECT [CUBE_NAME], [CATALOG_NAME], [BASE_CUBE_NAME], [SCHEMA_NAME], [CUBE_TYPE], [CUBE_GUID], [CREATED_ON], [LAST_SCHEMA_UPDATE], [LAST_DATA_UPDATE], [DATA_UPDATED_BY], [DESCRIPTION], [CUBE_CAPTION], [CUBE_SOURCE], [IS_DRILLTHROUGH_ENABLED], [PREFERRED_QUERY_PATTERNS] from $system.MDSCHEMA_CUBES WHERE [CATALOG_NAME] = '%s'";
  public static final String COLUMN_SELECTIVE_QUERY =
          "SELECT [CATALOG_NAME], [DATASET_NAME], [COLUMN_NAME], [DATA_TYPE], [EXPRESSION], [CONNECTION_ID] FROM $system.DBSCHEMA_COLUMNS WHERE [CATALOG_NAME] = '%s'";
  public static final String DATASET_SELECTIVE_QUERY =
          "SELECT [CATALOG_NAME], [CUBE_GUID], [DATASET_NAME], [DATABASE], [TABLE], [SCHEMA], [EXPRESSION], [CONNECTION_ID] FROM $system.DBSCHEMA_TABLES WHERE [CATALOG_NAME] = '%s'";
  public static final String DIMENSION_SELECTIVE_QUERY =
          "SELECT [CATALOG_NAME], [SCHEMA_NAME], [CUBE_NAME], [CUBE_GUID], [DIMENSION_NAME], [DIMENSION_GUID], [DIMENSION_UNIQUE_NAME], [DIMENSION_CAPTION], [DIMENSION_ORDINAL], [DIMENSION_TYPE], [DIMENSION_CARDINALITY], [DEFAULT_HIERARCHY], [DESCRIPTION], [IS_VIRTUAL], [IS_READWRITE], [DIMENSION_UNIQUE_SETTINGS], [DIMENSION_MASTER_NAME], [DIMENSION_IS_VISIBLE] FROM $system.MDSCHEMA_DIMENSIONS WHERE [CATALOG_NAME] = '%s' AND [DIMENSION_UNIQUE_NAME] &lt;&gt; '[Measures]'";
  public static final String HIERARCHY_SELECTIVE_QUERY =
          "SELECT [CATALOG_NAME], [SCHEMA_NAME], [CUBE_NAME], [CUBE_GUID], [DIMENSION_UNIQUE_NAME], [HIERARCHY_NAME], [HIERARCHY_UNIQUE_NAME], [HIERARCHY_GUID], [HIERARCHY_CAPTION], [DIMENSION_TYPE], [HIERARCHY_CARDINALITY], [DEFAULT_MEMBER], [ALL_MEMBER], [DESCRIPTION], [STRUCTURE], [IS_VIRTUAL], [IS_READWRITE], [DIMENSION_UNIQUE_SETTINGS], [DIMENSION_MASTER_UNIQUE_NAME], [DIMENSION_IS_VISIBLE], [HIERARCHY_ORIGIN], [HIERARCHY_DISPLAY_FOLDER], [INSTANCE_SELECTION], [GROUPING_BEHAVIOR], [STRUCTURE_TYPE], [DIMENSION_IS_SHARED], [HIERARCHY_IS_VISIBLE], [HIERARCHY_ORDINAL]  FROM $system.MDSCHEMA_HIERARCHIES WHERE [CATALOG_NAME] = '%s' AND [DIMENSION_UNIQUE_NAME] &lt;&gt; '[Measures]'";
  public static final String LEVEL_SELECTIVE_QUERY =
          "SELECT [CATALOG_NAME], [SCHEMA_NAME], [CUBE_NAME], [CUBE_GUID], [DIMENSION_UNIQUE_NAME], [HIERARCHY_UNIQUE_NAME], [LEVEL_NAME], [LEVEL_UNIQUE_NAME], [LEVEL_GUID], [LEVEL_CAPTION], [LEVEL_NUMBER], [LEVEL_CARDINALITY], [LEVEL_TYPE], [DESCRIPTION], [CUSTOM_ROLLUP_SETTINGS], [LEVEL_UNIQUE_SETTINGS], [LEVEL_IS_VISIBLE], [LEVEL_ORDERING_PROPERTY], [LEVEL_DBTYPE], [LEVEL_MASTER_UNIQUE_NAME], [LEVEL_NAME_SQL_COLUMN_NAME], [LEVEL_KEY_SQL_COLUMN_NAME], [LEVEL_UNIQUE_NAME_SQL_COLUMN_NAME], [LEVEL_ATTRIBUTE_HIERARCHY_NAME], [LEVEL_KEY_CARDINALITY], [LEVEL_ORIGIN], [DATASET_NAME], [LEVEL_NAME_SQL_COLUMN_NAME], [LEVEL_KEY_SQL_COLUMN_NAME], [LEVEL_SORT_SQL_COLUMN_NAME], [LEVEL_DBTYPE_NAME_COLUMN], [LEVEL_DBTYPE_SORT_COLUMN], [IS_PRIMARY], [PARENT_LEVEL_ID] FROM $system.MDSCHEMA_LEVELS WHERE [CATALOG_NAME] = '%s' AND [DIMENSION_UNIQUE_NAME] &lt;&gt; '[Measures]' AND [LEVEL_NAME] &lt;&gt; '(All)'";
  public static final String MEASURE_SELECTIVE_QUERY =
          "SELECT [CATALOG_NAME], [SCHEMA_NAME], [CUBE_NAME], [CUBE_GUID], [MEASURE_NAME], [MEASURE_UNIQUE_NAME], [MEASURE_GUID], [MEASURE_CAPTION], [MEASURE_AGGREGATOR], [DATA_TYPE], [COLUMN_NAME], [COLUMN_DATA_TYPE], [NUMERIC_PRECISION], [NUMERIC_SCALE], [MEASURE_UNITS], [DESCRIPTION], [EXPRESSION], [MEASURE_IS_VISIBLE], [MEASURE_IS_VISIBLE], [MEASURE_NAME_SQL_COLUMN_NAME], [MEASURE_UNQUALIFIED_CAPTION], [MEASUREGROUP_NAME], [MEASURE_DISPLAY_FOLDER], [DEFAULT_FORMAT_STRING], [DATASET_NAME], [IS_METRICAL_ATTRIBUTE], [PARENT_LEVEL_ID], [PARENT_LEVEL_NAME] FROM $system.MDSCHEMA_MEASURES WHERE [CATALOG_NAME] = '%s'";
  public static final String CONNECTION_GROUPS_QUERY =
        "SELECT [ID], [NAME], [PLATFORM_TYPE], [ORGANIZATION_ID],[CONNECTION_ID], [FILESYSTEM_URI], [FILESYSTEM_TYPE], [AGGREGATE_SCHEMA], [DATABASE], [IS_IMPERSONATION_ENABLED], [IS_CANARY_ALWAYS_ENABLED], [IS_PARTIAL_AGG_HIT_ENABLED], [READ_ONLY] FROM $system.DBSCHEMA_CONNECTION_GROUPS"; //  WHERE [CATALOG_NAME] = '%s'
  public static final String CONNECTIONS_QUERY =
          "SELECT [GROUP_ID], [ID], [NAME], [HOSTS],[PORT], [CONNECTOR_TYPE], [USERNAME], [IS_KERBEROS_CLIENT_ENABLED], [EXTRA_JDBC_FLAGS], [MANAGEMENT_CONSOLE_URL], [DATABASE], [QUERY_ROLES] FROM $system.DBSCHEMA_CONNECTION_SUBGROUPS";
  public static final String DEPENDENCIES_QUERY =
          "SELECT [DATABASE_NAME], [OBJECT_TYPE], [TABLE], [OBJECT], [EXPRESSION], [REFERENCED_OBJECT_TYPE], [REFERENCED_TABLE], [REFERENCED_OBJECT], [REFERENCED_EXPRESSION], [CATALOG_NAME], [CUBE_NAME] FROM $system.DISCOVER_CALC_DEPENDENCY";
  public static final String DEPENDENCIES_SELECTIVE_QUERY =
          "SELECT [DATABASE_NAME], [OBJECT_TYPE], [TABLE], [OBJECT], [EXPRESSION], [REFERENCED_OBJECT_TYPE], [REFERENCED_TABLE], [REFERENCED_OBJECT], [REFERENCED_EXPRESSION], [CATALOG_NAME], [CUBE_NAME] FROM $system.DISCOVER_CALC_DEPENDENCY WHERE [CATALOG_NAME] = '%s'";

  public static final String PROJECT_NAME = "CATALOG_NAME";
  public static final boolean PRINT_MSGS = true;
  public static final boolean PRINT_HEADERS = true;
  public static final boolean DO_IMPORTS = true;
  public static final int MAX_ASSETS_TO_LOAD = 6000;
  protected static final List<String> FILTER = Arrays.asList("d_orderdate");
  public static final String NOTE_SEPARATOR = "  ||  ";

  //Shifted constant from classes
  public static final String ASSET_NAME_SEPARATOR = " > ";
  public static final String PROJECTS_ENDPOINT = "{API_PATH_TO_GET_PROJECT_USING_DC_HOST}"; // Using DC host
  public static final String PROJECT_XML_ENDPOINT = "{API_PATH_TO_GET_PROJECT_USING_XML}";
  public static final String PUBLISHED_PROJECTS_ENDPOINT = "{API_PATH_TO_PUBLISH_PROJECT_USING_API_HOST}"; // Using api host
  public static final String CONNECTIONS_ENDPOINT = "{API_PATH_TO_CONNECTIONS_PROJECT_USING_API_HOST}"; // Using api host

  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER = "Bearer";
  public static final String CONTENT_TYPE ="Content-Type";
  public static final String APPLICATION_XML = "application/xml";
  public static final String HTTPS = "https";
  public static final String SOURCE = "SOURCE";
  public static final String NEW_LINE_WITH_ASTERISK = "\n    * ";
  public static final String ALREADY_EXITS = "already exists";
  public static final String TARGET = "TARGET";
  public static final String UNIQUE_NAME = "Unique Name: ";
  public static final String ATSCALE_DATASETS = "AtScale datasets";
  public static final String FOLDER = "folder";
  public static final String CATALOG_NAME = "CATALOG_NAME";
  public static final String CUBE_NAME = "CUBE_NAME";
  public static final String CUBE_GUID = "CUBE_GUID";
  public static final String DESCRIPTION = "DESCRIPTION";
  public static final String DATASET_NAME = "DATASET_NAME";
  public static final String SCHEMA_NAME = "SCHEMA_NAME";
  public static final String EXPRESSION = "EXPRESSION";
  public static final String FLOAT = "Float";
  public static final String STRING = "String";
  public static final String DIMENSION_UNIQUE_NAME = "DIMENSION_UNIQUE_NAME";
  public static final String CONNECTION_ID = "CONNECTION_ID";
  public static final String DATABASE = "DATABASE";
  public static final String OBSOLETE = "obsolete";
  public static final String AT_SCALE_ORGANIZATION = "AtScale Organization";
  public static final String AT_SCALE_PROJECT = "AtScale Project";
  public static final String AT_SCALE_CUBE = "AtScale_Cube";
  public static final String AT_SCALE_DIMENSION = "AtScale Dimension";
  public static final String AT_SCALE_HIERARCHY = "AtScale Hierarchy";
  public static final String AT_SCALE_LEVEL = "AtScale Level";
  public static final String AT_SCALE_SECONDARY_ATTRIBUTE = "AtScale Secondary Attribute";
  public static final String AT_SCALE_MEASURE = "AtScale Measure";
  public static final String AT_SCALE_FOLDER = "AtScale Folder";
  public static final String AT_SCALE_CONNECTION_GROUP = "AtScale Connection Group";
  public static final String AT_SCALE_COLUMN = "AtScale Column";
  public static final String CONTAINS = "contains";
  public static final String IS_PART_OF = "is part of";
  public static final String GROUPS = "groups";
  public static final String IS_GROUPED_BY = "is grouped by";
  public static final String IS_USED_BY = "is used by";
  public static final String COLUMN = "Column";
  public static final String AT_SCALE_DATASET = "AtScale Dataset";
  public static final String COLUMN_S = "column(s)";
  public static final String EMPTY_STRING = "";
  public static final String HTTP = "http";
  public static final String BASIC = "Basic";
  public static final String SPACE = " ";
  public static final String ATSCALE_SERVER_DC_HOST_NAME = "dummyHost";
  public static final String ATSCALE_SERVER_API_HOST_NAME = "dummyHost";
  public static final String ATSCALE_SERVER_AUTH_HOST_NAME = "dummyHost";
  public static final String ATSCALE_SERVER_DC_PORT = "dummyPort";
  public static final String ATSCALE_SERVER_API_PORT = "dummyPort";
  public static final String ATSCALE_SERVER_AUTH_PORT = "dummyPort";
  public static final String ATSCALE_SERVER_USERNAME = "dummy";
  public static final String ATSCALE_SERVER_PASSWORD = "dummy";
  public static final String ATSCALE_ORGANIZATION_FILTER_NAME = "dummyUser";
  public static final String ATSCALE_ORGANIZATION_FILTER_GUID = "dummyUser";

  private Constants() {}
}
