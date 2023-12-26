package com.collibra.marketplace.atscale.component;

import com.collibra.marketplace.atscale.api.AtScaleAPI;
import com.collibra.marketplace.atscale.api.AtScaleServerClient;
import com.collibra.marketplace.atscale.api.SOAPQuery;
import com.collibra.marketplace.atscale.api.SOAPResultSet;
import com.collibra.marketplace.atscale.config.ApplicationConfig;
import com.collibra.marketplace.atscale.model.*;
import com.collibra.marketplace.atscale.util.Constants;
import com.collibra.marketplace.atscale.util.Tools;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.*;

import static com.collibra.marketplace.atscale.util.Constants.*;

@Component
public class AtScaleApiRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtScaleApiRequest.class);
    private final ApplicationConfig appConfig;
    private final ObjectMapper mapper;
    private AtScaleServerClient atScaleServerClient;
    private final Map<Integer, String> dataTypeVsNameMap = new HashMap<>();

    @Autowired
    public AtScaleApiRequest(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
        this.mapper = new ObjectMapper();
        this.atScaleServerClient = new AtScaleServerClient(appConfig);
    }

    public void setAtScaleServerClient(AtScaleServerClient atScaleServerClient) {
        this.atScaleServerClient = atScaleServerClient;
    }

    public Map<String, Project> retrieveAllProjects(String instanceName, String orgName) {

        LOGGER.debug("Retrieving all projects...");

        String query = Constants.PROJECT_QUERY;
        LOGGER.info("SQL Query for CATALOGS = {}", query);
        Map<String, Project> projectMap = new HashMap<>();

        try {
            SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());
            List<String> projectsToLoad = new ArrayList<>();

            LOGGER.info("Consolidating projects that have been published multiple times");
            while (resultSet.next()) {
                // Need to add filtering out of projects published twice with different names
                // Only want 1 instance for now. Just have the unique name with which project was published
                String projectName = resultSet.getString(CATALOG_NAME);

                if (CollectionUtils.isEmpty(appConfig.getFilterProjects())  || appConfig.getFilterProjects().contains(projectName)) {

                    List<String> publishedProjectList = AtScaleAPI.getSinglePublishedProjectNames(atScaleServerClient);
                    if (publishedProjectList.contains(resultSet.getString(Constants.PROJECT_NAME))) {
                        Project project = createProject(resultSet);
                        projectMap.put(resultSet.getString(CATALOG_NAME), project);
                        projectsToLoad.add(projectName + " (" + project.getCatalogGUID() + ")");
                    }
                } else {
                    LOGGER.info("Skipping project '{}' since atscale.filter.project setting is '{}'", projectName, appConfig.getFilterProjectString());
                }
            }
            LOGGER.info("Populated list of {} published and deduplicated project(s): {}", projectMap.size(), projectsToLoad);
        } catch (Exception ex) {
            LOGGER.error("Error while getting project names for extraction.");
            LOGGER.error(ex.getMessage(), ex);
        }

        LOGGER.debug("Retrieved {} projects", projectMap.size());

        return projectMap;
    }

    public Project createProject(SOAPResultSet resultSet) throws SQLException {
        Project project = new Project();
        try {
            project = new Project();
            project.setRowId(resultSet.getRow());
            project.setTableNumber(resultSet.getRow());
            project.setName(resultSet.getString(CATALOG_NAME));
            project.setRole(resultSet.getString("ROLES"));
            project.setImportDate(new Date().toString());
            project.setLastModified(resultSet.getString("DATE_MODIFIED"));
            project.setCompatibilityLevel(resultSet.getInt("COMPATIBILITY_LEVEL"));
            project.setType(resultSet.getInt("TYPE"));
            project.setVersion(resultSet.getInt("VERSION"));
            project.setDatabaseId(resultSet.getString("DATABASE_ID"));
            project.setDateQueried(resultSet.getString("DATE_QUERIED"));
            project.setIsCurrentlyUsed(resultSet.getBoolean("CURRENTLY_USED"));
            project.setCatalogGUID(resultSet.getString("CATALOG_GUID"));
        } catch (Exception e) {
            LOGGER.error("Error while creating Project object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return project;
    }

    public List<Cube> retrieveAllCubes(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all cubes...");

        List<Cube> cubeList = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.CUBE_NAME_FROM_CATALOG_SELECTIVE_QUERY, pair.getKey());
            LOGGER.info("SQL Query for CUBES on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());
                while (resultSet.next()) {
                    Cube cube = createCube(resultSet);
                    cubeList.add(cube);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LOGGER.info("Populated list of {} cube(s)", cubeList.size());
        }
        LOGGER.debug("Retrieved {} cubes", cubeList.size());
        return cubeList;
    }

    public Cube createCube(SOAPResultSet resultSet) throws SQLException {
        Cube cube = null;
        try {
            cube = new Cube();
            cube.setCubeName(resultSet.getString(CUBE_NAME));
            cube.setCubeCaption(resultSet.getString("CUBE_CAPTION"));
            cube.setGuid(resultSet.getString(CUBE_GUID));
            cube.setCubeSource(resultSet.getInt("CUBE_SOURCE"));
            cube.setCubeType(resultSet.getString("CUBE_TYPE"));
            cube.setCatalogName(resultSet.getString(CATALOG_NAME));
            cube.setBaseCubeName(resultSet.getString("BASE_CUBE_NAME"));
            cube.setLastDataUpdated(resultSet.getString("LAST_DATA_UPDATE"));
            cube.setDescription(resultSet.getString(DESCRIPTION));
            cube.setIsDrillThroughEnabled(resultSet.getBoolean("IS_DRILLTHROUGH_ENABLED"));
            cube.setPreferredQueryPatterns(resultSet.getString("PREFERRED_QUERY_PATTERNS"));
        } catch (Exception e) {
            LOGGER.error("Error while creating Cube object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return cube;
    }

    public List<Dimension> retrieveAllDimensions(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all dimensions...");

        List<Dimension> dimensionList = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.DIMENSION_SELECTIVE_QUERY, pair.getKey());
            LOGGER.info("SQL Query for DIMENSIONS on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());
                while (resultSet.next()) {
                    Dimension dimension = createDimension(resultSet);
                    dimensionList.add(dimension);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LOGGER.info("Populated list of {} dimension(s)", dimensionList.size());
        }
        LOGGER.debug("Retrieved {} dimensions", dimensionList.size());
        return dimensionList;
    }

    public Dimension createDimension(SOAPResultSet resultSet) throws SQLException {
        Dimension dimension = null;
        try {
            dimension = new Dimension();
            dimension.setRowId(resultSet.getRow());
            dimension.setTableNumber(resultSet.getRow());
            dimension.setImportDate(new Date().toString());
            dimension.setCatalogName(resultSet.getString(CATALOG_NAME));
            dimension.setSchemaName(resultSet.getString(SCHEMA_NAME));
            dimension.setCubeName(resultSet.getString(CUBE_NAME));
            dimension.setCubeGUID(resultSet.getString(CUBE_GUID));
            dimension.setDimensionName(resultSet.getString("DIMENSION_NAME"));
            dimension.setDimensionUniqueName(resultSet.getString(DIMENSION_UNIQUE_NAME));
            dimension.setDimensionGUID(resultSet.getString("DIMENSION_GUID"));
            dimension.setDimensionCaption(resultSet.getString("DIMENSION_CAPTION"));
            dimension.setDimensionOrdinal(resultSet.getInt("DIMENSION_ORDINAL"));
            dimension.setType(resultSet.getInt("DIMENSION_TYPE"));
            dimension.setDimensionCardinality(resultSet.getInt("DIMENSION_CARDINALITY"));
            dimension.setDefaultHierarchy(resultSet.getString("DEFAULT_HIERARCHY"));
            dimension.setDescription(resultSet.getString(DESCRIPTION));
            dimension.setIsVirtual(resultSet.getBoolean("IS_VIRTUAL"));
            dimension.setIsReadWrite(resultSet.getBoolean("IS_READWRITE"));
            dimension.setDimensionUniqueSettings(resultSet.getInt("DIMENSION_UNIQUE_SETTINGS"));
            dimension.setDimensionMasterName(resultSet.getString("DIMENSION_MASTER_NAME"));
            dimension.setIsVisible(resultSet.getBoolean("DIMENSION_IS_VISIBLE"));
        } catch (Exception e) {
            LOGGER.error("Error while creating Dimension object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return dimension;
    }

    public List<Measure> retrieveAllMeasures(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all measures...");

        List<Measure> measureList = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.MEASURE_SELECTIVE_QUERY, pair.getKey());
            LOGGER.info("SQL Query for MEASURES on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());

                while (resultSet.next()) {
                    Measure measure = createMeasure(resultSet);
                    measureList.add(measure);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LOGGER.info("Populated list of {} measure(s)", measureList.size());
        }
        LOGGER.debug("Retrieved {} measures", measureList.size());
        return measureList;
    }

    public Measure createMeasure(SOAPResultSet resultSet) throws SQLException {
        Measure measure = null;
        try {
            measure = new Measure();
            measure.setRowId(resultSet.getRow());
            measure.setTableNumber(resultSet.getRow());
            measure.setImportDate(new Date().toString());
            measure.setCatalogName(resultSet.getString(CATALOG_NAME));
            measure.setSchemaName(resultSet.getString(SCHEMA_NAME));
            measure.setCubeName(resultSet.getString(CUBE_NAME));
            measure.setCubeGUID(resultSet.getString(CUBE_GUID));
            measure.setMeasureName(resultSet.getString("MEASURE_NAME"));
            measure.setMeasureUniqueName(resultSet.getString("MEASURE_UNIQUE_NAME"));
            measure.setMeasureGUID(resultSet.getString("MEASURE_GUID"));
            measure.setMeasureCaption(resultSet.getString("MEASURE_CAPTION"));
            measure.setMeasureAggregator(resultSet.getInt("MEASURE_AGGREGATOR"));
            measure.setNumericPrecision(resultSet.getInt("NUMERIC_PRECISION"));
            measure.setNumericScale(resultSet.getInt("NUMERIC_SCALE"));
            measure.setMeasureUnits(resultSet.getInt("MEASURE_UNITS"));
            measure.setDescription(resultSet.getString(DESCRIPTION));
            measure.setExpression(resultSet.getString(EXPRESSION));
            measure.setVisible(resultSet.getBoolean("MEASURE_IS_VISIBLE"));
            measure.setLevelList(resultSet.getString("MEASURE_IS_VISIBLE"));
            measure.setMeasureNameSQLColumnName(resultSet.getString("MEASURE_NAME_SQL_COLUMN_NAME"));
            measure.setMeasureUnqualifiedCaption(resultSet.getString("MEASURE_UNQUALIFIED_CAPTION"));
            measure.setMeasureGroupName(resultSet.getString("MEASUREGROUP_NAME"));
            measure.setMeasureDisplayFolder(resultSet.getString("MEASURE_DISPLAY_FOLDER"));
            measure.setDefaultFormatString(resultSet.getString("DEFAULT_FORMAT_STRING"));
            measure.setDatasetName(resultSet.getString(DATASET_NAME));
            measure.setColumnName(resultSet.getString("COLUMN_NAME"));
            measure.setMetricalAttribute(resultSet.getBoolean("IS_METRICAL_ATTRIBUTE"));
            measure.setParentLevelId(resultSet.getString("PARENT_LEVEL_ID"));
            measure.setParentLevelName(resultSet.getString("PARENT_LEVEL_NAME"));
        } catch (Exception e) {
            LOGGER.error("Error while creating Measure object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return measure;
    }

    public List<Hierarchy> retrieveAllHierarchies(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all hierarchies...");

        List<Hierarchy> hierarchyList = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.HIERARCHY_SELECTIVE_QUERY, pair.getKey());
            LOGGER.info("SQL Query for HIERARCHIES on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());

                while (resultSet.next()) {
                    Hierarchy hierarchy = createHierarchy(resultSet);
                    hierarchyList.add(hierarchy);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LOGGER.info("Populated list of {}  hierarchy(s)", hierarchyList.size());
        }
        LOGGER.debug("Retrieved {} hierarchies", hierarchyList.size());
        return hierarchyList;
    }

    public Hierarchy createHierarchy(SOAPResultSet resultSet) throws SQLException {
        Hierarchy hierarchy = null;
        try {
            hierarchy = new Hierarchy();
            hierarchy.setRowId(resultSet.getRow());
            hierarchy.setTableNumber(resultSet.getRow());
            hierarchy.setImportDate(new Date().toString());
            hierarchy.setCatalogName(resultSet.getString(CATALOG_NAME));
            hierarchy.setSchemaName(resultSet.getString(SCHEMA_NAME));
            hierarchy.setCubeName(resultSet.getString(CUBE_NAME));
            hierarchy.setDimensionUniqueName(resultSet.getString(DIMENSION_UNIQUE_NAME));
            hierarchy.setHierarchyName(resultSet.getString("HIERARCHY_NAME"));
            hierarchy.setHierarchyUniqueName(resultSet.getString("HIERARCHY_UNIQUE_NAME"));
            hierarchy.setHierarchyGUID(resultSet.getString("HIERARCHY_GUID"));
            hierarchy.setHierarchyCaption(resultSet.getString("HIERARCHY_CAPTION"));
            hierarchy.setDimensionType(resultSet.getInt("DIMENSION_TYPE"));
            hierarchy.setHierarchyCardinality(resultSet.getInt("HIERARCHY_CARDINALITY"));
            hierarchy.setDefaultMember(resultSet.getString("DEFAULT_MEMBER"));
            hierarchy.setAllMember(resultSet.getString("ALL_MEMBER"));
            hierarchy.setDescription(resultSet.getString(DESCRIPTION));
            hierarchy.setStructure(resultSet.getInt("STRUCTURE"));
            hierarchy.setIsVirtual(resultSet.getBoolean("IS_VIRTUAL"));
            hierarchy.setIsReadWrite(resultSet.getBoolean("IS_READWRITE"));
            hierarchy.setDimensionUniqueSettings(resultSet.getInt("DIMENSION_UNIQUE_SETTINGS"));
            hierarchy.setDimensionMasterName(resultSet.getString("DIMENSION_MASTER_UNIQUE_NAME"));
            hierarchy.setDimensionIsVisible(resultSet.getBoolean("DIMENSION_IS_VISIBLE"));
            hierarchy.setHierarchyOrigin(resultSet.getInt("HIERARCHY_ORIGIN"));
            hierarchy.setHierarchyDisplayFolder(resultSet.getString("HIERARCHY_DISPLAY_FOLDER"));
            hierarchy.setInstanceSelection(resultSet.getInt("INSTANCE_SELECTION"));
            hierarchy.setGroupingBehaviour(resultSet.getInt("GROUPING_BEHAVIOR"));
            hierarchy.setStructureType(resultSet.getString("STRUCTURE_TYPE"));
            hierarchy.setDimensionIsShared(resultSet.getBoolean("DIMENSION_IS_SHARED"));
            hierarchy.setHierarchyIsVisible(resultSet.getBoolean("HIERARCHY_IS_VISIBLE"));
            hierarchy.setHierarchyOrdinal(resultSet.getInt("HIERARCHY_ORDINAL"));
        } catch (Exception e) {
            LOGGER.error("Error while creating Hierarchy object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return hierarchy;
    }

    public List<Level> retrieveAllLevels(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all levels...");

        List<Level> levelList = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.LEVEL_SELECTIVE_QUERY, pair.getKey());
            LOGGER.info("SQL Query for LEVELS on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());

                while (resultSet.next()) {
                        Level level = createLevel(resultSet);
                        levelList.add(level);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            LOGGER.info("Populated list of {} level(s)", levelList.size());
        }
        LOGGER.debug("Retrieved {} levels", levelList.size());
        return levelList;
    }

    public Level createLevel(SOAPResultSet resultSet) throws SQLException {
        Level level = null;
        try {
            level = new Level();
            level.setRowId(resultSet.getRow());
            level.setImportDate(new Date().toString());
            level.setCatalogName(resultSet.getString(CATALOG_NAME));
            level.setSchemaName(resultSet.getString(SCHEMA_NAME));
            level.setCubeName(resultSet.getString(CUBE_NAME));
            level.setCubeGUID(resultSet.getString(CUBE_GUID));
            level.setDatasetName(resultSet.getString(DATASET_NAME));
            level.setDimensionUniqueName(resultSet.getString(DIMENSION_UNIQUE_NAME));
            level.setHierarchyUniqueName(resultSet.getString("HIERARCHY_UNIQUE_NAME"));
            level.setLevelUniqueName(resultSet.getString("LEVEL_UNIQUE_NAME"));
            level.setLevelGUID(resultSet.getString("LEVEL_GUID"));
            if (level.getLevelGUID().contains("+")) { // To work around issue with GUID's in DMV query
                level.setLevelGUID(level.getLevelGUID().substring(0,level.getLevelGUID().indexOf("+")));
            }
            level.setLevelCaption(resultSet.getString("LEVEL_CAPTION"));
            level.setLevelName(resultSet.getString("LEVEL_NAME"));
            level.setLevelNumber(resultSet.getInt("LEVEL_NUMBER"));
            level.setNameColumn(resultSet.getString("LEVEL_NAME_SQL_COLUMN_NAME"));
            level.setKeyColumns(resultSet.getString("LEVEL_KEY_SQL_COLUMN_NAME"));
            level.setSortColumn(resultSet.getString("LEVEL_SORT_SQL_COLUMN_NAME"));
            level.setNameDataType(resultSet.getString("LEVEL_DBTYPE_NAME_COLUMN"));
            level.setSortDataType(resultSet.getString("LEVEL_DBTYPE_SORT_COLUMN"));
            level.setParentLevelGUID(resultSet.getString("PARENT_LEVEL_ID"));
            level.setIsPrimary(resultSet.getBoolean("IS_PRIMARY"));
            level.setDescription(resultSet.getString(DESCRIPTION));
        } catch (Exception e) {
            LOGGER.error("Error while creating Level object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return level;
    }

    public List<ConnectionGroup> retrieveAllConnectionGroups() {
        LOGGER.debug("Retrieving all connection groups...");

        List<ConnectionGroup> connectionGroupList = new ArrayList<>();

        String query = Constants.CONNECTION_GROUPS_QUERY;
        LOGGER.info("SQL Query for CONNECTION_GROUPS = {}", query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());

                while (resultSet.next()) {
                        ConnectionGroup connectionGroup = createConnectionGroup(resultSet);
                        connectionGroupList.add(connectionGroup);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        LOGGER.debug("Retrieved {} connectionGroups", connectionGroupList.size());
        return connectionGroupList;
    }

    public ConnectionGroup createConnectionGroup(SOAPResultSet resultSet) throws SQLException {
        ConnectionGroup connectionGroup = null;
        try {
            connectionGroup = new ConnectionGroup();
            connectionGroup.setConnectionGroupGUID(resultSet.getString("ID"));
            connectionGroup.setConnectionGroupName(resultSet.getString("NAME"));
            connectionGroup.setPlatformType(resultSet.getString("PLATFORM_TYPE"));
            connectionGroup.setOrgID(resultSet.getString("ORGANIZATION_ID"));
            connectionGroup.setConnID(resultSet.getString(CONNECTION_ID));
            connectionGroup.setFilesystemURI(resultSet.getString("FILESYSTEM_URI"));
            connectionGroup.setFilesystemType(resultSet.getString("FILESYSTEM_TYPE"));
            connectionGroup.setAggsSchema(resultSet.getString("AGGREGATE_SCHEMA"));
            connectionGroup.setDatabase(resultSet.getString(DATABASE));
            connectionGroup.setIsImpersonationEnabled(resultSet.getBoolean("IS_IMPERSONATION_ENABLED"));
            connectionGroup.setIsCanaryEnabled(resultSet.getBoolean("IS_CANARY_ALWAYS_ENABLED"));
            connectionGroup.setIsPartialAggHitEnabled(resultSet.getBoolean("IS_PARTIAL_AGG_HIT_ENABLED"));
            connectionGroup.setIsReadOnly(resultSet.getBoolean("READ_ONLY"));
        } catch (Exception e) {
            LOGGER.error("Error while creating ConnectionGroup object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return connectionGroup;
    }

    public List<Connection> retrieveAllConnections() {
        LOGGER.debug("Retrieving all connections...");

        List<Connection> connectionList = new ArrayList<>();

        String query = Constants.CONNECTIONS_QUERY;
        LOGGER.info("SQL Query for CONNECTIONS = {}", query);

        try {
            SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());

            while (resultSet.next()) {
                Connection connection = createConnection(resultSet);
                connectionList.add(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOGGER.debug("Retrieved {} connections", connectionList.size());
        return connectionList;
    }

    public Connection createConnection(SOAPResultSet resultSet) throws SQLException {
        Connection connection = null;
        try {
            connection = new Connection();
            connection.setConnectionName(resultSet.getString("NAME"));
            connection.setGroupGUID(resultSet.getString("GROUP_ID"));
            connection.setConnectionGUID(resultSet.getString("ID"));
            connection.setHost(resultSet.getString("HOSTS"));
            connection.setPort(resultSet.getString("PORT"));
            connection.setConnectorType(resultSet.getString("CONNECTOR_TYPE"));
            connection.setUser(resultSet.getString("USERNAME"));
            connection.setKerberosEnabled(resultSet.getBoolean("IS_KERBEROS_CLIENT_ENABLED"));
            connection.setJdbcFlags(resultSet.getString("EXTRA_JDBC_FLAGS"));
            connection.setMgtConsoleURL(resultSet.getString("MANAGEMENT_CONSOLE_URL"));
            connection.setDatabase(resultSet.getString(DATABASE));
            connection.setQueryRoles(resultSet.getString("QUERY_ROLES"));
        } catch (Exception e) {
            LOGGER.error("Error while creating Connection object for row id : {}",resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return connection;
    }

    public List<Dataset> retrieveAllDatasets(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all datasets...");

        List<Dataset> datasetList = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.DATASET_SELECTIVE_QUERY, pair.getKey());

            LOGGER.info("SQL Query for DATASETS on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());
                while (resultSet.next()) {
                    Dataset dataset = createDataSet(resultSet);
                    datasetList.add(dataset);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LOGGER.debug("Retrieved {} datasets", datasetList.size());
        return datasetList;
    }

    public Dataset createDataSet(SOAPResultSet resultSet) throws SQLException {
        Dataset dataset = null;
        try {
            dataset = new Dataset();
            dataset.setDatasetName(resultSet.getString(DATASET_NAME));
            dataset.setCatalogName(resultSet.getString(CATALOG_NAME));
            dataset.setCubeGUID(resultSet.getString(CUBE_GUID));
            dataset.setDatabase(resultSet.getString(DATABASE));
            dataset.setTable(resultSet.getString("TABLE"));
            dataset.setSchema(resultSet.getString("SCHEMA"));
            dataset.setExpression(resultSet.getString(EXPRESSION));
            dataset.setConnection(resultSet.getString(CONNECTION_ID));
        } catch (Exception e) {
            LOGGER.error("Error while creating Dataset object for row id : {}",resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return dataset;
    }

    public List<Column> retrieveAllColumns(Map<String, Project> allProjects) {
        LOGGER.debug("Retrieving all columns...");

        List<Column> columnList = new ArrayList<>();

        populateDataTypeVsNameMap();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            String query = String.format(Constants.COLUMN_SELECTIVE_QUERY, pair.getKey());

            LOGGER.info("SQL Query for COLUMNS on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, appConfig.getAtscaleDebug());
                getColumnList(resultSet, columnList);
            } catch (Exception e) {
                LOGGER.error("Error while retrieving all column", e);
            }
        }
        LOGGER.debug("Retrieved {} columns", columnList.size());
        return columnList;
    }

    public List<Column> getColumnList(SOAPResultSet resultSet, List<Column> columnList) throws SQLException {
        while (resultSet.next()) {
            try {
                Column column = new Column();
                column.setCatalogName(resultSet.getString(CATALOG_NAME));
                column.setDatasetName(resultSet.getString(DATASET_NAME));
                column.setColumnName(resultSet.getString("COLUMN_NAME"));
                column.setDataType(dataTypeVsNameMap.get(resultSet.getInt("DATA_TYPE")));
                column.setExpression(resultSet.getString(EXPRESSION));
                column.setConnectionID(resultSet.getString(CONNECTION_ID));
                columnList.add(column);
            } catch (Exception e) {
                LOGGER.error("Error while creating Column object for row id : {}", resultSet.getRow());
                LOGGER.error(e.getMessage(), e);
            }
        }
        return columnList;
    }

    public List<Dependency> retrieveAllDependencies(Map<String, Project> allProjects, List<Column> allColumns) {
        LOGGER.debug("Retrieving all dependencies...");

        List<Dependency> dependencies = new ArrayList<>();

        for (Map.Entry<String, Project> pair : allProjects.entrySet()) {
            // In DEPENDENCIES we can't filter using 'WHERE catalog_name ='
            // [TABLE] is actually the dataset so get db/schema/table from it
            String query = Constants.DEPENDENCIES_QUERY;  // DEPENDENCIES_SELECTIVE_QUERY
            LOGGER.info("SQL Query for DEPENDENCIES on catalog '{}' = {}", pair.getKey(), query);

            try {
                SOAPResultSet resultSet = SOAPQuery.runSOAPQuery(atScaleServerClient, query, "<Catalog>" + pair.getKey() + "</Catalog>", appConfig.getAtscaleDebug());

                while (resultSet.next()) {
                        Dependency dependency = createDependency(resultSet, pair);
                        // Check to make sure column exists, else warn and don't add dependency
                        if (isDependencyValid(dependency, allColumns)) {
                            dependencies.add(dependency);
                        } else {
                            LOGGER.warn("Dependency from '{}' in project '{}' not added because column '{}{}{}' not found",
                                    dependency.getObject(), pair.getKey(), dependency.getTable(), Constants.ASSET_NAME_SEPARATOR, dependency.getObject());
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LOGGER.debug("Retrieved {} dependencies", dependencies.size());
        return dependencies;
    }

    public boolean isDependencyValid(Dependency dependency, List<Column> allColumns) {
        if (dependency.getReferencedObjectType().equals(Constants.COLUMN)) {
            for (Column col : allColumns) {
                if (col.getColumnName().equals(dependency.getReferencedObject()) && col.getDatasetName().equals(dependency.getTable())) {
                    return true;
                }
            }
            return false;
        } else {
            // It's a MEASURE
            return true;
        }
    }

    public Dependency createDependency(SOAPResultSet resultSet, Map.Entry<String, Project> pair) throws SQLException {
        Dependency dependency = null;
        try {
            dependency = new Dependency();
            dependency.setDatabaseName(resultSet.getString("DATABASE_NAME"));
            dependency.setObjectType(resultSet.getString("OBJECT_TYPE"));
            dependency.setTable(resultSet.getString("TABLE"));
            dependency.setObject(resultSet.getString("OBJECT"));
            dependency.setExpression(resultSet.getString(EXPRESSION));
            dependency.setReferencedObjectType(resultSet.getString("REFERENCED_OBJECT_TYPE"));
            dependency.setReferencedTable(resultSet.getString("REFERENCED_TABLE"));
            dependency.setReferencedObject(resultSet.getString("REFERENCED_OBJECT"));
            dependency.setReferencedExpression(resultSet.getString("REFERENCED_EXPRESSION"));
            dependency.setCatalogName(pair.getKey());
            dependency.setCubeName(resultSet.getString(CUBE_NAME));

        } catch (Exception e) {
            LOGGER.error("Error while creating Dependency object for row id : {}", resultSet.getRow());
            LOGGER.error(e.getMessage(), e);
        }
        return dependency;
    }

    private void populateDataTypeVsNameMap() {
        dataTypeVsNameMap.put(2,"Int");
        dataTypeVsNameMap.put(3,"Int");
        dataTypeVsNameMap.put(4,FLOAT);
        dataTypeVsNameMap.put(5,FLOAT);
        dataTypeVsNameMap.put(6,FLOAT); // Currency
        dataTypeVsNameMap.put(7,"Double"); // Date values are stored as Double, the whole part of which is the number of days since December 30, 1899, and the fractional part of which is the fraction of a day.
        dataTypeVsNameMap.put(8,STRING); // A pointer to a BSTR, which is a null-terminated character string in which the string length is stored with the string.
        dataTypeVsNameMap.put(11,"Boolean");
        dataTypeVsNameMap.put(14,FLOAT);
        dataTypeVsNameMap.put(16,"Int");
        dataTypeVsNameMap.put(17,"Int");
        dataTypeVsNameMap.put(18,"Int");
        dataTypeVsNameMap.put(19,"Int");
        dataTypeVsNameMap.put(20,"Int");
        dataTypeVsNameMap.put(21,"Int");
        dataTypeVsNameMap.put(72,STRING); // GUID
        dataTypeVsNameMap.put(128,"Int"); // Binary
        dataTypeVsNameMap.put(129,STRING);
        dataTypeVsNameMap.put(130,STRING);
        dataTypeVsNameMap.put(131,FLOAT);
        dataTypeVsNameMap.put(133,"Int"); // Date as yyyymmdd
        dataTypeVsNameMap.put(134,"Int"); // Time as hhmmss
        dataTypeVsNameMap.put(135,"Int"); // date-time stamp (yyyymmddhhmmss plus a fraction in billionths).
    }

    public Map<String, Folder> retrieveAllFolders(List<Measure> allMeasures, List<Hierarchy> allHierarchies) {
        Map<String, Folder> allFolders = new HashMap<>();

        for (Measure measure: allMeasures) {
            if (!measure.getMeasureDisplayFolder().isEmpty()) {
                allFolders.put(measure.getCatalogName() + "." + measure.getCubeName() + "." + measure.getMeasureDisplayFolder(),
                        new Folder(measure.getCatalogName(), measure.getCubeName(), measure.getMeasureDisplayFolder()));
            }
        }
        for (Hierarchy hierarchy: allHierarchies) {
            if (!hierarchy.getHierarchyDisplayFolder().isEmpty()) {
                allFolders.put(hierarchy.getCatalogName() + "." + hierarchy.getCubeName() + "." + hierarchy.getHierarchyDisplayFolder(),
                        new Folder(hierarchy.getCatalogName(), hierarchy.getCubeName(), hierarchy.getHierarchyDisplayFolder()));
            }
        }
        return allFolders;
    }

    public void publishProject(Map.Entry<String, Project> currentProjectPair, Map<String, Measure> updatedMeasureMap) {
        String requestBody = getProjetSchema(currentProjectPair, updatedMeasureMap);
        atScaleServerClient.connect();
        String token = atScaleServerClient.getConnection();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BEARER + SPACE + token);
        headers.add(CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE);
        HttpEntity<?> httpEntity = new HttpEntity<Object>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> responseEntity = null;
        String publishProjectUrl = atScaleServerClient.buildPublishProjectURL(currentProjectPair.getValue().getCatalogGUID(), "normal_publish", "system", currentProjectPair.getValue().getCatalogGUID(), "project");
        responseEntity = restTemplate.exchange(
                publishProjectUrl,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> responseBody = responseEntity.getBody();
    }

    private String getProjetSchema(Map.Entry<String, Project> currentProjectPair, Map<String, Measure> updatedMeasureMap) {
        atScaleServerClient.connect();
        String token = atScaleServerClient.getConnection();
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, BEARER + SPACE + token);
        headers.add(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<?> httpEntity = new HttpEntity<Object>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> responseEntity = null;
        Project project = currentProjectPair.getValue();
        String url = atScaleServerClient.buildGetProjectSchemaURL(project.getCatalogGUID());
        try {
            responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    httpEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Map<String, Object> responseBody = responseEntity.getBody();
            Map responseMap = (Map) responseBody.get(JSON_RESPONSE_PROPERTY);
            String projectSchema = String.valueOf(responseMap.get(JSON_RESPONSE_PROPERTY));
            projectSchema = projectSchema.replace(XML_VERSION, EMPTY_STRING);
            projectSchema = PROJECT_SCHEMA_START_TAG + projectSchema + PROJECT_SCHEMA_END_TAG;
            DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder1 = null;
            builder1 = factory1.newDocumentBuilder();
            Document document1 = null;
            document1 = builder1.parse(new InputSource(new StringReader(projectSchema)));
            String originalXml = Tools.formatdocumentToXml(document1);
            document1 = builder1.parse(new InputSource(new StringReader(originalXml)));
            Element cubesElement = (Element) document1.getElementsByTagName(CUBES).item(0);
            NodeList attributeNodes = cubesElement.getElementsByTagName(ATTRIBUTE);
            Tools.modifySchema(attributeNodes, document1, updatedMeasureMap);
            NodeList calculatedMembersNodes = document1.getElementsByTagName(CALCULATED_MEMBER);
            Tools.modifySchema(calculatedMembersNodes, document1, updatedMeasureMap);
            return Tools.formatdocumentToXml(document1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
