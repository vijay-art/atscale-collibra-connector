/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.component;

import com.collibra.marketplace.atscale.model.*;
import com.collibra.marketplace.atscale.util.Constants;
import com.collibra.marketplace.atscale.util.Tools;
import com.collibra.marketplace.library.integration.CollibraAsset;
import com.collibra.marketplace.library.integration.CollibraRelation.Direction;
import com.collibra.marketplace.library.integration.constants.CollibraConstants.AttributeType;
import com.collibra.marketplace.atscale.config.ApplicationConfig;
import com.collibra.marketplace.atscale.util.CustomConstants;
import com.collibra.marketplace.atscale.util.CustomConstants.AssetType;
import com.collibra.marketplace.library.integration.model.CollibraAttributeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.collibra.marketplace.atscale.util.Constants.*;

/**
 * A class that contains methods that are used to transform the assets and complex relations that are used by this
 * integration.
 */
@Component
public class CollibraAssetTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollibraAssetTransformer.class);

    private final ApplicationConfig appConfig;

    private Set<String> ignoreHierarchySet;

    private Map<String, String> projectGuidMap = new HashMap<>();

    @Autowired
    public CollibraAssetTransformer(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
        this.ignoreHierarchySet = new HashSet<>();
    }

    public CollibraAsset transformAtScaleInstance(AtScaleInstance instance) {

        LOGGER.info("Transforming AtScale Instance {}", appConfig.getAtscaleApiHost());

        String assetName = AssetNames.prepareAssetName(Arrays.asList(instance.getName()));

        String note = UNIQUE_NAME + assetName + Constants.NOTE_SEPARATOR + "Design Center: " + instance.getDcHost() + ":" + instance.getDcPort() + Constants.NOTE_SEPARATOR + "Engine: " + instance.getApiHost() + ":" + instance.getApiPort() + Constants.NOTE_SEPARATOR + "Authorization: " + instance.getAuthHost() + ":" + instance.getAuthPort();

        // @formatter:off
        CollibraAsset.Builder instanceAssetBuilder = new CollibraAsset.Builder()
                .typeName("Asset")

                .name(assetName)
                .displayName(instance.getApiHost())
                .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                .type(AssetType.ATSCALE_INSTANCE)
                .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note);

        CollibraAsset orgAsset = instanceAssetBuilder.build();

        LOGGER.info("Transformed AtScale Instance {}", instance.getApiHost());

        return orgAsset;
    }

    public CollibraAsset transformOrganization(Organization org, String instanceName) {

        LOGGER.info("Transforming organization '{}'", org.getName());

        String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                instanceName,
                org.getName())));

        String note = UNIQUE_NAME + assetName + ", GUID: " + org.getOrganizationGUID();

        // @formatter:off
        CollibraAsset.Builder orgAssetBuilder = new CollibraAsset.Builder()
                .name(assetName)
                .displayName(org.getName())
                .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                .type(AssetType.ATSCALE_ORGANIZATION)
                .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, org.getOrganizationGUID())
                .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                .addRelationByDomainId(
                        CustomConstants.RelationType.ATSCALE_INSTANCE_CONTAINS_ORG,
                        Direction.SOURCE,
                        AssetNames.prepareAssetName(Arrays.asList(
                                instanceName)),
                        appConfig.getAtscaleBusinessAssetDomainId());

        CollibraAsset orgAsset = orgAssetBuilder.build();

        LOGGER.info("Transformed organization {}", org.getName());

        return orgAsset;
    }

    public List<CollibraAsset> transformProjects(Collection<Project> projects, List<Dataset> datasets, Organization org, String instanceName) {

        LOGGER.info("Transforming {} projects...", projects.size());

        List<CollibraAsset> projectAssets = new ArrayList<>();

        projects.forEach(project -> {

            String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                    instanceName,
                    org.getName(),
                    project.getName())));

            String note = UNIQUE_NAME + assetName;
            projectGuidMap.put(project.getName(), project.getCatalogGUID());

            // @formatter:off
            CollibraAsset.Builder projectAssetBuilder = new CollibraAsset.Builder()
                    .name(assetName)
                    .displayName(project.getName())
                    .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                    .type(AssetType.ATSCALE_PROJECT)
                    .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, project.getCatalogGUID())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION_FROM_SOURCE_SYSTEM, project.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, project.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                    .addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_ORG_CONTAINS_PROJECT,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName()))),
                            appConfig.getAtscaleBusinessAssetDomainId());

            // Get list of connection groups used by datasets in this project
            Set<String> cgs = new HashSet<>();
            for (Dataset dataset: datasets) {
                if (dataset.getCatalogName().equals(project.getName())) {
                    cgs.add(dataset.getConnection());
                }
            }
            for (String cg: cgs) {
                // Add relations to connection groups used
                projectAssetBuilder.addRelationByDomainId(
                        CustomConstants.RelationType.ATSCALE_PROJECT_USES_CONNECTIONGROUP,
                        Direction.TARGET,
                        AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                instanceName,
                                org.getName(),
                                cg))),
                        appConfig.getAtscaleDataAssetDomainId());
            }

            projectAssets.add(projectAssetBuilder.build());

        });

        LOGGER.info("Transformed {} projects", projectAssets.size());

        return projectAssets;

    }

    public List<CollibraAsset> transformCubes(Collection<Cube> cubes, Organization org, String instanceName) {

        LOGGER.info("Transforming {} cubes...", cubes.size());

        List<CollibraAsset> cubeAssets = new ArrayList<>();

        cubes.forEach(cube -> {

            String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                    instanceName,
                    org.getName(),
                    cube.getCatalogName(),
                    cube.getCubeName())));

            String note = UNIQUE_NAME + assetName
                    + (!cube.getLastDataUpdated().isEmpty() ? Constants.NOTE_SEPARATOR+"Last Updated: " + cube.getLastDataUpdated() : "")
                    ;

            // @formatter:off
            CollibraAsset.Builder cubeAssetBuilder = new CollibraAsset.Builder()
                    .name(assetName)
                    .displayName(cube.getCubeCaption())
                    .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                    .type(AssetType.ATSCALE_CUBE)
                    .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, cube.getGuid())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, cube.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, cube.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                    .addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_PROJECT_CONTAINS_CUBE,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    cube.getCatalogName()))),
                            appConfig.getAtscaleBusinessAssetDomainId());

            cubeAssetBuilder.build();

            cubeAssets.add(cubeAssetBuilder.build());

        });

        LOGGER.info("Transformed {} cubes", cubeAssets.size());

        return cubeAssets;

    }

    public List<CollibraAsset> transformMeasures(Map<String, String> typesMap, Collection<Measure> measures, List<Dependency> dependencies, List<Dataset> allDatasets,
                                                 List<Column> allColumns, List<ConnectionGroup> allConnectionGroups,
                                                 Map<String, String> physicalMap, Organization org, String instanceName) {
        LOGGER.info("Transforming {} measures...", measures.size());
        List<CollibraAsset> measureAssets = new ArrayList<>();
        Set<String> missingColumns = new HashSet<>();

        AtomicReference<Boolean> foundColumn = new AtomicReference<>(false);

        measures.stream().forEach(measure -> {

            String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                    instanceName,
                    org.getName(),
                            measure.getCatalogName(),
                            measure.getCubeName(),
                            measure.getMeasureName())));

            String note = UNIQUE_NAME + assetName
                    + (measure.isMetricalAttribute() ? Constants.NOTE_SEPARATOR+"Metrical Attribute": "")
                    + (!measure.getExpression().isEmpty() ? Constants.NOTE_SEPARATOR+"Expression: " + measure.getExpression() : "")
                    + (!measure.getMeasureDisplayFolder().isEmpty() ? Constants.NOTE_SEPARATOR+"Folder: " + measure.getMeasureDisplayFolder() : "")
                    + (measure.isVisible() ? "" : Constants.NOTE_SEPARATOR+"Hidden")
                    + (!Tools.getAggregationByKey(measure.getMeasureAggregator()).isEmpty() ? Constants.NOTE_SEPARATOR+"Aggregation: "+Tools.getAggregationByKey(measure.getMeasureAggregator()) : "")
                    ;

            // @formatter:off
            CollibraAsset.Builder measureAssetBuilder = new CollibraAsset.Builder()
                    .name(assetName)
                    .displayName(measure.getMeasureCaption())
                    .type(AssetType.ATSCALE_MEASURE)
                    .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                    .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, measure.getMeasureGUID())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, measure.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, measure.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                    .addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_CUBE_CONTAINS_MEASURE,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    measure.getCatalogName(),
                                    measure.getCubeName()))),
                            appConfig.getAtscaleBusinessAssetDomainId())
                    ;

            // Regular measure (not calculated)
            if (measure.getExpression().isEmpty()) {
                if (isCalculatedCol (allColumns, measure.getColumnName())
                        || !datasetIsTable(allDatasets, measure.getDatasetName())
                ) {
                    // Uses calculated column so adding to AtScale Columns
                    measureAssetBuilder.addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_MEASURE_USES_ATSCALE_COLUMN,
                            Direction.TARGET,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    measure.getCatalogName(),
                                    measure.getDatasetName(),
                                    measure.getColumnName()))),
                            appConfig.getAtscaleDataAssetDomainId());


                } else {
                    // Now add relationship to physical column if dataset is a table
                    String fullColName = toColumnUniqueName(allDatasets, measure.getDatasetName(), measure.getColumnName());
                    if (fullColName.length() > 0 && datasetIsTable(allDatasets, measure.getDatasetName())) {
                        if (physicalMap.containsKey(fullColName)) {
                            if (!foundColumn.get().booleanValue()) {
                                LOGGER.info("Example related physical column from measure '{}' in domain: {}",
                                        toTableName(allDatasets, measure.getDatasetName()) + Constants.ASSET_NAME_SEPARATOR + measure.getColumnName(),
                                        typesMap.get(physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), measure.getDatasetName()))));
                                foundColumn.set(true);
                            }
                            measureAssetBuilder.addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_MEASURE_USES_PHYSICAL_COLUMN,
                                    Direction.TARGET,
                                    toTableName(allDatasets, measure.getDatasetName()) + Constants.ASSET_NAME_SEPARATOR + measure.getColumnName(),
                                    physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), measure.getDatasetName())));
                        } else {
                            missingColumns.add(fullColName);
                        }
                    }
                }
            } else {
                // Calculated Measure: Add dependent measures. Add hierarchies & levels when populated
                for (Dependency dependency: dependencies) {
                    if ((dependency.getObject().equals(measure.getMeasureName())
                            && dependency.getObjectType().equals("MEASURE")) && dependency.getReferencedObjectType().equals("MEASURE")
                    ) {
                        // Should support other types later
                            // Need to make sure destination exists. Hidden measures are not returned by MEASURES DMV query
                            if (!inMeasureList(measures, dependency.getReferencedObject())) {
                                LOGGER.warn("Destination object not in import so can't create relation from '{}' TO '{}'. Destination measure may not be visible.", assetName, AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                        instanceName,
                                        org.getName(),
                                        dependency.getCatalogName(),
                                        measure.getCubeName(),
                                        dependency.getReferencedObject()))));
                            } else {
                                measureAssetBuilder.addRelationByDomainId(
                                        CustomConstants.RelationType.ATSCALE_MEASURE_USES_MEASURE,
                                        Direction.TARGET,
                                        AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                                instanceName,
                                                org.getName(),
                                                dependency.getCatalogName(),
                                                measure.getCubeName(),
                                                dependency.getReferencedObject()))),
                                        appConfig.getAtscaleBusinessAssetDomainId());
                            }

                    }
                }
            }
            // Now add folders
            if (!measure.getMeasureDisplayFolder().isEmpty()) {
                measureAssetBuilder.addRelationByDomainId(
                        CustomConstants.RelationType.ATSCALE_FOLDER_GROUPS_MEASURE,
                        Direction.SOURCE,
                        AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                instanceName,
                                org.getName(),
                                measure.getCatalogName(),
                                measure.getCubeName(),
                                Constants.FOLDER,
                                measure.getMeasureDisplayFolder()))),
                        appConfig.getAtscaleBusinessAssetDomainId());
            }

            measureAssetBuilder.build();
            measureAssets.add(measureAssetBuilder.build());
        });

        printMissing(missingColumns, "Physical column asset(s)", COLUMN_S, "measures");

        LOGGER.info("Transformed {} measures", measureAssets.size());

        return measureAssets;
    }

    private boolean inMeasureList(Collection<Measure> measures, String referencedObject) {
        for (Measure measure : measures) {
            if (measure.getMeasureName().equals(referencedObject)) {
                return true;
            }
        }
        return false;
    }

    private boolean datasetIsTable(List<Dataset> allDatasets, String datasetName) {
        for (Dataset ds : allDatasets) {
            if (ds.getDatasetName().equals(datasetName)) {
                return !ds.getTable().isEmpty();
            }
        }
        return false;
    }

    private String toTableName(List<Dataset> allDatasets, String datasetName) {
        for (Dataset ds : allDatasets) {
            if (ds.getDatasetName().equals(datasetName)) {
                if (!ds.getTable().isEmpty()) {
                    return ds.getTable();
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    // Returns value like  > ATSCALE_SAMPLE_DATA.atscale > customer_file > customerkey
    private String toColumnUniqueName(List<Dataset> allDatasets, String datasetName, String columnName) {
        for (Dataset ds : allDatasets) {
            if (ds.getDatasetName().equals(datasetName)) {
                if (!ds.getTable().isEmpty()) {
                    if (ds.getDatabase().isEmpty()) {
                        return AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                ds.getSchema(),
                                ds.getTable(),
                                columnName)));
                    } else {
                        return AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                ds.getDatabase() + Constants.ASSET_NAME_SEPARATOR + ds.getSchema(),
                                ds.getTable(),
                                columnName)));
                    }
                }
                return "";
            }
        }
        return "";
    }

    private String toDatabaseSchema(List<Dataset> allDatasets, List<ConnectionGroup> allConnectionGroups, String orgID, String datasetName) {
        for (Dataset ds : allDatasets) {
            if (!ds.getDatasetName().equals(datasetName)) {
                continue;
            }
            if (ds.getTable().isEmpty()) {
                return "";
            }
            if (!ds.getDatabase().isEmpty()) {
                return ds.getDatabase() + Constants.ASSET_NAME_SEPARATOR + ds.getSchema();
            }
            for (ConnectionGroup cg : allConnectionGroups) {
                if (cg.getConnID().equals(ds.getConnection()) && cg.getOrgID().equals(orgID)) {
                    return cg.getDatabase() + Constants.ASSET_NAME_SEPARATOR + ds.getSchema();
                }
            }
            return ds.getSchema();
        }
        return "";
    }


    public List<CollibraAsset> transformFolders(Map<String, Folder> folders, Organization org, String instanceName) {
        LOGGER.info("Transforming {} folders...", folders.size());
        List<CollibraAsset> folderAssets = new ArrayList<>();

        for (Map.Entry<String, Folder> folder: folders.entrySet()) {
            String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                    instanceName,
                    org.getName(),
                    folder.getValue().getCatalogName(),
                    folder.getValue().getCubeName(),
                    FOLDER,
                    folder.getValue().getFolderName())));

            // @formatter:off
            CollibraAsset.Builder folderAssetBuilder = new CollibraAsset.Builder()
                    .name(assetName)
                    .displayName(folder.getValue().getFolderName())
                    .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                    .type(AssetType.ATSCALE_FOLDER)
                    .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, assetName)
//                    .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, measure.getDescription())
                    .addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_CUBE_CONTAINS_FOLDER,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    folder.getValue().getCatalogName(),
                                    folder.getValue().getCubeName()))),
                            appConfig.getAtscaleBusinessAssetDomainId())
                    ;

            folderAssetBuilder.build();
            folderAssets.add(folderAssetBuilder.build());
        }

        LOGGER.info("Transformed {} folders", folderAssets.size());

        return folderAssets;
    }

    public List<CollibraAsset> transformDimensions(Collection<Dimension> dimensions, Organization org, String instanceName) {
        LOGGER.info("Transforming {} dimensions...", dimensions.size());
        List<CollibraAsset> dimensionAssets = new ArrayList<>();

        dimensions.forEach(dimension -> {

            String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                    instanceName,
                    org.getName(),
                    dimension.getCatalogName(),
                    dimension.getCubeName(),
                    dimension.getDimensionUniqueName())));

            String note = UNIQUE_NAME + assetName
                    + (dimension.getType().equals(1) ? Constants.NOTE_SEPARATOR+"Dimension is of type Time" : "")
                    ;

            // @formatter:off
            CollibraAsset.Builder dimensionAssetBuilder = new CollibraAsset.Builder()
                    .name(assetName)
                    .displayName(dimension.getDimensionCaption())
                    .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                    .type(AssetType.ATSCALE_DIMENSION)
                    .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, dimension.getDimensionGUID())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, dimension.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, dimension.getDescription())
                    .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                    .addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_CUBE_CONTAINS_DIMENSION,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    dimension.getCatalogName(),
                                    dimension.getCubeName()))),
                            appConfig.getAtscaleBusinessAssetDomainId())
                    ;

            dimensionAssetBuilder.build();
            dimensionAssets.add(dimensionAssetBuilder.build());
        });
        LOGGER.info("Transformed {} dimensions", dimensionAssets.size());

        return dimensionAssets;
    }

    public List<CollibraAsset> transformHierarchies(Collection<Hierarchy> hierarchies, List<Level> levels,
                                                    List<Dataset> datasetList, Organization org, String instanceName) {
        LOGGER.info("Transforming {} hierarchies...", hierarchies.size());
        List<CollibraAsset> hierarchyAssets = new ArrayList<>();

        hierarchies.forEach(hierarchy -> {
                String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        instanceName,
                        org.getName(),
                        hierarchy.getCatalogName(),
                        hierarchy.getCubeName(),
                        hierarchy.getHierarchyUniqueName())));

            String note = UNIQUE_NAME + assetName
                    + ((!hierarchy.getDefaultMember().isEmpty()
                    && !hierarchy.getDefaultMember().toUpperCase(Locale.ROOT).contains("ALL_MEMBER")
                    && !hierarchy.getDefaultMember().toUpperCase(Locale.ROOT).contains("[ALL]")) ?
                    Constants.NOTE_SEPARATOR + "Default Member: " + hierarchy.getDefaultMember() : "");

            if (ignoreHierarchySet == null || ignoreHierarchySet.isEmpty() || !ignoreHierarchySet.contains(assetName)) {

                // @formatter:off
                CollibraAsset.Builder hierarchyAssetBuilder = new CollibraAsset.Builder()
                        .name(assetName)
                        .displayName(hierarchy.getHierarchyCaption())
                        .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                        .type(AssetType.ATSCALE_HIERARCHY)
                        .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, hierarchy.getHierarchyGUID())
                        .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, hierarchy.getDescription())
                        .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, hierarchy.getDescription())
                        .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                        .addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_DIMENSION_CONTAINS_HIERARCHY,
                                Direction.SOURCE,
                                AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                        instanceName,
                                        org.getName(),
                                        hierarchy.getCatalogName(),
                                        hierarchy.getCubeName(),
                                        hierarchy.getDimensionUniqueName()))),
                                appConfig.getAtscaleBusinessAssetDomainId());

                // Get list of datasets used by levels in this hierarchy
                Set<String> datasets = new HashSet<>();
                for (Level level: levels) {
                    if (level.getHierarchyUniqueName().equals(hierarchy.getHierarchyUniqueName()) &&
                            level.getCatalogName().equals(hierarchy.getCatalogName())) {

                        // Need to check if it's a shared degenerate dimension. If so, dataset may look like: Gldetail,qds_for_a0cfb986_e884_482e_ae9,GLACCOUNTBALANCE
                        // First look up dataset. If found, use it. If not, try to break up by commas and look up individual names.
                        if (foundInDatasets(level.getCatalogName(), level.getDatasetName(), datasetList)) {
                            datasets.add(level.getDatasetName());
                        } else {
                            for (String ds: level.getDatasetName().split(",")) {
                                if (foundInDatasets(level.getCatalogName(), ds, datasetList)) {
                                    datasets.add(ds);
                                }
                            }
                        }
                    }
                }
                for (String dataset: datasets) {
                    // Add relations to datasets used
                    hierarchyAssetBuilder.addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_HIERARCHY_USES_DATASET,
                            Direction.TARGET,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    hierarchy.getCatalogName(),
                                    dataset))),
                            appConfig.getAtscaleDataAssetDomainId());
                }

                // Now add folders
                if (!hierarchy.getHierarchyDisplayFolder().isEmpty()) {
                    hierarchyAssetBuilder.addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_FOLDER_GROUPS_HIERARCHY,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    hierarchy.getCatalogName(),
                                    hierarchy.getCubeName(),
                                    FOLDER,
                                    hierarchy.getHierarchyDisplayFolder()))),
                            appConfig.getAtscaleBusinessAssetDomainId());
                }

                hierarchyAssetBuilder.build();
                hierarchyAssets.add(hierarchyAssetBuilder.build());
            }
        });
        LOGGER.info("Transformed {} hierarchies", hierarchyAssets.size());

        return hierarchyAssets;
    }

    private boolean foundInDatasets(String projectName, String dsName, List<Dataset> datasetList) {
        for (Dataset ds: datasetList) {
            if (ds.getDatasetName().equals(dsName) && ds.getCatalogName().equals(projectName)) {
                return true;
            }
        }
        return false;
    }

    // Relations are Dimension -> Hierachy -> Level -> Secondary
    // This breaks when the level that a secondary attribute is associated with is not visible or included in the levels query
    // In this case the keep clean relationships the secondary attribute will be shown as a level
    public List<CollibraAsset> transformLevels(Map<String, String> typesMap, Collection<Level> levels, Collection<Column> columns,
                                               List<Dataset> allDatasets, List<ConnectionGroup> allConnectionGroups,
                                               Map<String, String> physicalMap, Organization org, String instanceName) {
        LOGGER.info("Transforming {} levels...", levels.size());
        List<CollibraAsset> levelAssets = new ArrayList<>();
        Set<String> missingColumns = new HashSet<>();
        AtomicReference<Boolean> foundColumn = new AtomicReference<>(false);

        // First stream all the level attributes, then stream
        // the secondary attributes that use some metadata from the levels

        levels.stream().forEach(level -> {
            CollibraAsset.Builder levelAssetBuilder;

            if (level.getParentLevelGUID().isEmpty()) {
                // Level Attribute
                String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        instanceName,
                        org.getName(),
                        level.getCatalogName(),
                        level.getCubeName(),
                        level.getLevelUniqueName())));

                String note = UNIQUE_NAME + assetName
                        + Constants.NOTE_SEPARATOR+"Level " + level.getLevelNumber()
                        + (!level.getSortColumn().isEmpty() && !level.getSortColumn().equals(level.getNameColumn()) ?
                            Constants.NOTE_SEPARATOR+"Sort by column: " + level.getSortColumn() : "")
                        + (level.getIsVisible() != null && !level.getIsVisible() ? Constants.NOTE_SEPARATOR+"Attribute is Hidden" : "")
                        ;

                // @formatter:off
                levelAssetBuilder = new CollibraAsset.Builder()
                        .name(assetName)
                        .displayName(level.getLevelCaption())
                        .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                        .type(AssetType.ATSCALE_LEVEL)
                        .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, level.getLevelGUID())
                        .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, level.getDescription())
                        .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, level.getDescription())
                        .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                        .addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_HIERARCHY_CONTAINS_LEVEL,
                                Direction.SOURCE,
                                AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                        instanceName,
                                        org.getName(),
                                        level.getCatalogName(),
                                        level.getCubeName(),
                                        level.getHierarchyUniqueName()))),
                                appConfig.getAtscaleBusinessAssetDomainId())
                ;
                // If column is on a shared degenerate dimension DMV query returns missing and invalid data
                // Warn and don't include TARGET relationships
                Set<String> uniqueCalcColumns = level.getUniqueCalcColumns(level.getCatalogName(), columns);
                Set<String> uniquePhysicalColumns = level.getUniquePhysicalColumns(level.getCatalogName(), columns);
                if ((uniqueCalcColumns == null && uniquePhysicalColumns == null) || !foundInDatasets(level.getCatalogName(), level.getDatasetName(), allDatasets)) {
                    LOGGER.warn("Can't add relationships to columns for level '{}' in cube '{} > {}'. It appears to be used for shared degenerate dimensions which are not yet supported by DMV queries", level.getLevelUniqueName(), level.getCatalogName(), level.getCubeName());

                } else {
                    for (String col : uniqueCalcColumns) {
                        // Add relations to columns used
                        levelAssetBuilder.addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_LEVEL_USES_ATSCALE_COLUMN,
                                Direction.TARGET,
                                AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                        instanceName,
                                        org.getName(),
                                        level.getCatalogName(),
                                        level.getDatasetName(),
                                        col))),
                                appConfig.getAtscaleDataAssetDomainId());
                    }
                    // Now add relationship to physical column if dataset is a table
                    for (String col : uniquePhysicalColumns) {
                        String fullColName = toColumnUniqueName(allDatasets, level.getDatasetName(), col);
                        if (fullColName.length() > 0 && datasetIsTable(allDatasets, level.getDatasetName())) {
                            if (physicalMap.containsKey(fullColName)) { // XXX
                                levelAssetBuilder.addRelationByDomainId(
                                        CustomConstants.RelationType.ATSCALE_LEVEL_USES_PHYSICAL_COLUMN,
                                        Direction.TARGET,
                                        toTableName(allDatasets, level.getDatasetName()) + Constants.ASSET_NAME_SEPARATOR + col,
                                        String.valueOf(physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), level.getDatasetName()))));
                                if (!foundColumn.get().booleanValue()) {
                                    LOGGER.info("Example related physical column from level attribute '{}{}' in domain: {}",
                                            toTableName(allDatasets, level.getDatasetName()), Constants.ASSET_NAME_SEPARATOR + col,
                                            typesMap.get(physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), level.getDatasetName()))));

                                    foundColumn.set(true);
                                }
                            } else {
                                missingColumns.add(fullColName);
                            }
                        } else {
                            // Columns are on QDS
                            levelAssetBuilder.addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_LEVEL_USES_ATSCALE_COLUMN,
                                    Direction.TARGET,
                                    AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            level.getCatalogName(),
                                            level.getDatasetName(),
                                            col))),
                                    appConfig.getAtscaleDataAssetDomainId());
                        }
                    }
                }

                levelAssetBuilder.build();
                levelAssets.add(levelAssetBuilder.build());
            }
        });

        Set<String> cubesWithInvisibleLevels = new HashSet<>();

        // Secondary Attributes
        levels.forEach(secondary -> {
            CollibraAsset.Builder secondaryAssetAsLevelBuilder;

            if (!secondary.getParentLevelGUID().isEmpty()) {

                String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        instanceName,
                        org.getName(),
                        secondary.getCatalogName(),
                        secondary.getCubeName(),
                        secondary.getLevelUniqueName())));

                Level associatedLevel = secondary.getAssociatedLevel(levels, cubesWithInvisibleLevels);

                if (associatedLevel != null) {
                    // @formatter:off
                    // Use hierarchy of associated level for secondary attributes
                    ignoreHierarchySet.add(AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                            instanceName,
                            org.getName(),
                            secondary.getCatalogName(),
                            secondary.getCubeName(),
                            secondary.getHierarchyUniqueName()))));

                    String note = UNIQUE_NAME + assetName
                            + Constants.NOTE_SEPARATOR+"Secondary attribute of " + associatedLevel.getLevelCaption()
                            ;

                    secondaryAssetAsLevelBuilder = new CollibraAsset.Builder()
                            .name(assetName)
                            .displayName(secondary.getLevelCaption())
                            .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                            .type(AssetType.ATSCALE_SECONDARY)
                            .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, secondary.getLevelGUID())
                            .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, secondary.getDescription())
                            .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, secondary.getDescription())
                            .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                            .addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_LEVEL_CONTAINS_SECONDARY,
                                    Direction.SOURCE,
                                    AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            secondary.getCatalogName(),
                                            secondary.getCubeName(),
                                            associatedLevel.getLevelUniqueName()))),
                                    appConfig.getAtscaleBusinessAssetDomainId())
                    ;

                    // TODO: Once folders on secondary attributes are supported in DMV queries, add the relationship here

                    Set<String> uniqueCalcColumns = secondary.getUniqueCalcColumns(secondary.getCatalogName(), columns);
                    Set<String> uniquePhysicalColumns = secondary.getUniquePhysicalColumns(secondary.getCatalogName(), columns);
                    String assocLevelDatasetName = associatedLevel.getDatasetName();
                    if ((uniqueCalcColumns == null && uniquePhysicalColumns == null) || !foundInDatasets(associatedLevel.getCatalogName(), assocLevelDatasetName, allDatasets)) {
                        LOGGER.warn("Can't add relationships to columns for secondary attribute '{}' in cube '{} > {}'. It appears to be used for shared degenerate dimensions which are not yet supported by DMV queries", secondary.getLevelUniqueName()
                                , secondary.getCatalogName(), secondary.getCubeName());
                    } else {
                        // Add relations to calculated columns used
                        for (String col : uniqueCalcColumns) {
                                secondaryAssetAsLevelBuilder.addRelationByDomainId(
                                        CustomConstants.RelationType.ATSCALE_SECONDARY_USES_ATSCALE_COLUMN,
                                        Direction.TARGET,
                                        AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                                instanceName,
                                                org.getName(),
                                                associatedLevel.getCatalogName(),
                                                assocLevelDatasetName,
                                                col))),
                                        appConfig.getAtscaleDataAssetDomainId());
                        }

                        // Now add relationships to physical columns
                        for (String col : uniquePhysicalColumns) {
                            String fullColName = toColumnUniqueName(allDatasets, assocLevelDatasetName, col);
                            if (fullColName.length() > 0 && datasetIsTable(allDatasets, assocLevelDatasetName)) {
                                if (physicalMap.containsKey(fullColName)) {
                                    secondaryAssetAsLevelBuilder.addRelationByDomainId(
                                            CustomConstants.RelationType.ATSCALE_SECONDARY_USES_PHYSICAL_COLUMN,
                                            Direction.TARGET,
                                            toTableName(allDatasets, assocLevelDatasetName) + Constants.ASSET_NAME_SEPARATOR + col,
                                            String.valueOf(physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), assocLevelDatasetName))));
                                } else {
                                        missingColumns.add(fullColName);
                                }
                            } else {
                            // Columns are on QDS
                                secondaryAssetAsLevelBuilder.addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_SECONDARY_USES_ATSCALE_COLUMN,
                                    Direction.TARGET,
                                    AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            secondary.getCatalogName(),
                                            secondary.getDatasetName(),
                                            col))),
                                    appConfig.getAtscaleDataAssetDomainId());
                            }
                        }
                    }
                } else {
                    // associated level may be invisible so treat the secondary as a level

                    String note = UNIQUE_NAME + assetName
                            + Constants.NOTE_SEPARATOR+"Secondary attribute of invisible level"
                            ;

                    secondaryAssetAsLevelBuilder = new CollibraAsset.Builder()
                            .name(assetName)
                            .displayName(secondary.getLevelCaption())
                            .domainId(appConfig.getAtscaleBusinessAssetDomainId())
                            .type(AssetType.ATSCALE_LEVEL)
                            .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, secondary.getLevelGUID())
                            .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, secondary.getDescription())
                            .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, secondary.getDescription())
                            .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                            .addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_HIERARCHY_CONTAINS_LEVEL,
                                    Direction.SOURCE,
                                    AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            secondary.getCatalogName(),
                                            secondary.getCubeName(),
                                            secondary.getHierarchyUniqueName()))),
                                    appConfig.getAtscaleBusinessAssetDomainId())
                    ;

                    Set<String> uniqueCalcColumns = secondary.getUniqueCalcColumns(secondary.getCatalogName(), columns);
                    Set<String> uniquePhysicalColumns = secondary.getUniquePhysicalColumns(secondary.getCatalogName(), columns);
                    if ((uniqueCalcColumns == null && uniquePhysicalColumns == null) || !foundInDatasets(secondary.getCatalogName(), secondary.getDatasetName(), allDatasets)) {
                        LOGGER.warn("Can't add relationships to columns for secondary attribute '{}' in cube '{} > {}'. It appears to be used for shared degenerate dimensions which are not yet supported by DMV queries",secondary.getLevelUniqueName()
                                        , secondary.getCatalogName(), secondary.getCubeName());
                    } else {
                        for (String col : uniqueCalcColumns) {

                            // Add relations to columns used
                            secondaryAssetAsLevelBuilder.addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_LEVEL_USES_ATSCALE_COLUMN,
                                    Direction.TARGET,
                                    AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            secondary.getCatalogName(),
                                            secondary.getDatasetName(),
                                            col))),
                                    appConfig.getAtscaleDataAssetDomainId());
                        }



                        // Now add relationships to physical columns
                        for (String col : uniquePhysicalColumns) {
                            String fullColName = toColumnUniqueName(allDatasets, secondary.getDatasetName(), col);
                            if (physicalMap.containsKey(fullColName)) {
                                secondaryAssetAsLevelBuilder.addRelationByDomainId(
                                        CustomConstants.RelationType.ATSCALE_LEVEL_USES_PHYSICAL_COLUMN,
                                        Direction.TARGET,
                                        toTableName(allDatasets, secondary.getDatasetName()) + Constants.ASSET_NAME_SEPARATOR + col,
                                        String.valueOf(physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), secondary.getDatasetName()))));
                            } else {
                                if (fullColName.length() > 0) missingColumns.add(fullColName);
                            }
                        }
                    }
                }
                secondaryAssetAsLevelBuilder.build();
                levelAssets.add(secondaryAssetAsLevelBuilder.build());
            }
        });

        for (String cube : cubesWithInvisibleLevels) {
            LOGGER.warn("Secondary attribute(s) with no associated level attribute found in cube '{}'. The levels could be invisible and not returned by the DMV query", cube);
        }

        printMissing(missingColumns, "Physical column asset(s)", COLUMN_S, "attributes");

        LOGGER.info("Transformed {} attributes (both levels and secondary)", levelAssets.size());

        return levelAssets;
    }

    public List<CollibraAsset> transformConnectionGroups(Collection<ConnectionGroup> connectionGroups, List<String> connectionAssetNames,
                                                         Organization org, String instanceName) {
        LOGGER.info("Transforming {} connectionGroups...", connectionGroups.size());
        List<CollibraAsset> connectionGroupAssets = new ArrayList<>();

        connectionGroups.stream().forEach(connectionGroup -> {
            // The DMV query returns CG's for all orgs, not just the one specified
            if (connectionGroup.getOrgID().equals(org.getOrganizationGUID())) {

                String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        instanceName,
                        org.getName(),
                        connectionGroup.getConnID())));

                String note = UNIQUE_NAME + assetName
                        + (!connectionGroup.getPlatformType().isEmpty() ? Constants.NOTE_SEPARATOR + "Platform Type: " + connectionGroup.getPlatformType() : "")
//                    + (!connectionGroup.getOrgID().isEmpty() ? Constants.NOTE_SEPARATOR+"Organization ID: " + connectionGroup.getOrgID() : "")
                        + (!connectionGroup.getFilesystemURI().isEmpty() ? Constants.NOTE_SEPARATOR + "File System URI: " + connectionGroup.getFilesystemURI() : "")
                        + (!connectionGroup.getAggsSchema().isEmpty() ? Constants.NOTE_SEPARATOR + "Aggregate Schema: " + connectionGroup.getAggsSchema() : "")
                        + (!connectionGroup.getDatabase().isEmpty() ? Constants.NOTE_SEPARATOR + "Database: " + connectionGroup.getDatabase() : "");

                // @formatter:off
                CollibraAsset.Builder connectionGroupAssetBuilder = new CollibraAsset.Builder()
                        .name(assetName)
                        .displayName(connectionGroup.getConnectionGroupName())
                        .domainId(appConfig.getAtscaleDataAssetDomainId())
                        .type(AssetType.ATSCALE_CONNECTION_GROUP)
                        .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, connectionGroup.getConnectionGroupGUID())
                        .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, "A " + connectionGroup.getPlatformType() + " connection")
                        .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note);

                connectionGroupAssetBuilder.build();
                connectionGroupAssets.add(connectionGroupAssetBuilder.build());
                connectionAssetNames.add(assetName);
            }
        });
        LOGGER.info("Transformed {} connectionGroups", connectionGroupAssets.size());

        return connectionGroupAssets;
    }

    private String getConnIdFromGUID(List<ConnectionGroup> connectionGroups, String guid) {
        for (ConnectionGroup cg: connectionGroups) {
            if (guid.equals(cg.getConnectionGroupGUID())) {
                return cg.getConnID();
            }
        }
        LOGGER.warn("Connection Group ID not found for {} so using GUID instead", guid);
        return guid;
    }

    private String getCGGuidFromCG(CollibraAsset cg) {
        for (Map.Entry<String, List<CollibraAttributeValue>> pair : cg.getAttributes().entrySet()) {
            if (pair.getKey().equals(CustomConstants.AttributeType.IDENTIFIER.getId())) {
                return pair.getValue().get(0).getValue().toString();
            }
        }
        LOGGER.warn("Connection Group GUID not found for '{}'", cg.getName());
        return "";
    }

    public List<CollibraAsset> transformConnections(List<Connection> connections, List<ConnectionGroup> allConnectionGroups,
                                                    List<CollibraAsset> connectionGroups,
                                                    List<String> connectionAssetNames, Organization org, String instanceName) {

        LOGGER.info("Transforming {} connections...", connections.size());

        List<CollibraAsset> connectionAssets = new ArrayList<>();

        connections.stream().forEach(connection -> {

            // Make sure its group is being imported. Connections are returned for all orgs
            // so we need to remove those that aren't in the current org
            for (CollibraAsset cg : connectionGroups) {
                if (getCGGuidFromCG(cg).equals(connection.getGroupGUID())) {
                    String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                            instanceName,
                            org.getName(),
                            getConnIdFromGUID(allConnectionGroups, connection.getGroupGUID()),
                            connection.getConnectionName())));

                    String note = UNIQUE_NAME + assetName
                            + (!connection.getHost().isEmpty() ? Constants.NOTE_SEPARATOR+"Host: " + connection.getHost() : "")
                            + (!connection.getPort().isEmpty() ? Constants.NOTE_SEPARATOR+"Port: " + connection.getPort() : "")
                            + (!connection.getUser().isEmpty() ? Constants.NOTE_SEPARATOR+"User: " + connection.getUser() : "")
                            ;

                    // @formatter:off
                    CollibraAsset.Builder connectionAssetBuilder = new CollibraAsset.Builder()
                            .name(assetName)
                            .displayName(connection.getConnectionName())
                            .domainId(appConfig.getAtscaleDataAssetDomainId())
                            .type(AssetType.ATSCALE_CONNECTION)
                            .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, connection.getConnectionGUID())
                            .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, "A "+connection.getConnectorType()+
                                    " connection with query roles: "+connection.getQueryRoles())
                            .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                            .addRelationByDomainId(
                                    CustomConstants.RelationType.ATSCALE_CONNECTIONGROUP_CONTAINS_CONNECTION,
                                    Direction.SOURCE,
                                    AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            getConnIdFromGUID(allConnectionGroups, connection.getGroupGUID())))),
                                    appConfig.getAtscaleDataAssetDomainId())
                            ;
                    connectionAssetBuilder.build();

                    connectionAssets.add(connectionAssetBuilder.build());
                    connectionAssetNames.add(assetName);
                    break;
                }
            }
        });

        LOGGER.info("Transformed {} connections", connectionAssets.size());

        return connectionAssets;

    }

    public List<CollibraAsset> transformDatasets(List<Dataset> datasets, List<ConnectionGroup> allConnectionGroups, Map<String, String> physicalMap, Organization org, String instanceName) {

        LOGGER.info("Transforming {} datasets...", datasets.size());

        List<CollibraAsset> datasetAssets = new ArrayList<>();
        Set<String> missingSchemas = new HashSet<>();
        Set<String> missingTables = new HashSet<>();

        datasets.forEach(dataset -> {

            if (CollectionUtils.isEmpty(appConfig.getFilterProjects()) || appConfig.getFilterProjects().contains(dataset.getCatalogName())) {

                String assetName = AssetNames.prepareAssetName(Arrays.asList(
                        instanceName,
                        org.getName(),
                        dataset.getCatalogName(),
                        dataset.getDatasetName()));

                // Create definition for dataset
                String note =UNIQUE_NAME + assetName;
                String description;
                if (!dataset.getExpression().isEmpty()) {
                    note += Constants.NOTE_SEPARATOR +
                            (dataset.getExpression().length() > 99 ?
                                    "Query dataset with expression (truncated) as: " + dataset.getExpression().substring(0, 100)
                                    : "Query dataset with expression as: " + dataset.getExpression());
                    description = "Query dataset";
                } else {
                    note += Constants.NOTE_SEPARATOR + "Dataset based on table: " + dataset.getSchema() + "." + dataset.getTable();
                    description = "Dataset is a table";
                }

                // @formatter:off
                CollibraAsset.Builder datasetAssetBuilder = new CollibraAsset.Builder()
                        .name(assetName)
                        .displayName(dataset.getDatasetName())
                        .domainId(appConfig.getAtscaleDataAssetDomainId())
                        .type(AssetType.ATSCALE_DATASET)
                        .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, assetName) // There's no GUID in the DMV datasets query so using name
                        .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, description)
                        .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note)
                        .addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_CONNECTIONGROUP_CONTAINS_DATASET,
                                Direction.SOURCE,
                                AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                        instanceName,
                                        org.getName(),
                                        dataset.getConnection()))),
                                appConfig.getAtscaleDataAssetDomainId());

                // Now add relationship to physical column if dataset is a table
                if (!dataset.getTable().isEmpty() && !dataset.getSchema().isEmpty()) {
                    String fullSchema = dataset.getSchema();
                    if (!dataset.getDatabase().isEmpty()) {
                        fullSchema = dataset.getDatabase() + Constants.ASSET_NAME_SEPARATOR + fullSchema;
                    } else {
                        // Look up database from connection groups
                        for (ConnectionGroup cg: allConnectionGroups) {
                            if (cg.getConnID().equals(dataset.getConnection()) && cg.getOrgID().equals(org.getName())) {
                                fullSchema = (!cg.getDatabase().isEmpty() ? Constants.ASSET_NAME_SEPARATOR + cg.getDatabase()+Constants.ASSET_NAME_SEPARATOR : "") + fullSchema;
                            }
                        }
                    }

                    if (physicalMap.containsKey(fullSchema)) {
                        datasetAssetBuilder.addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_DATASET_USES_SCHEMA,
                                Direction.TARGET,
                                dataset.getSchema(),
                                physicalMap.get(fullSchema)); // Gets the UUID of the schema which is the domain
                    } else {
                        missingSchemas.add(fullSchema);
                    }

                    String tableUniqueName = fullSchema+Constants.ASSET_NAME_SEPARATOR+dataset.getTable();
                    if (physicalMap.containsKey(tableUniqueName)) {
                        datasetAssetBuilder.addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_DATASET_USES_TABLE,
                                Direction.TARGET,
                                dataset.getTable(),
                                physicalMap.get(fullSchema));
                    } else {
                        missingTables.add(fullSchema+Constants.ASSET_NAME_SEPARATOR+dataset.getTable());
                    }
                }

                datasetAssetBuilder.build();

                datasetAssets.add(datasetAssetBuilder.build());
            }

        });

        printMissing(missingSchemas, "Physical asset(s)", "schema(s)", ATSCALE_DATASETS);
        printMissing(missingTables, "Physical asset(s)", "table(s)", ATSCALE_DATASETS);

        LOGGER.info("Transformed {} datasets", datasetAssets.size());

        return datasetAssets;

    }

    // If it's a calculated column or on a QDS then add AtScale Column. Otherwise, will use physical columns for relationships
    public List<CollibraAsset> transformColumns(List<Column> columns, List<Dependency> dependencies,
                                                List<Dataset> allDatasets, List<ConnectionGroup> allConnectionGroups,
                                                Map<String, String> physicalMap, Organization org, String instanceName) {

        LOGGER.info("Transforming {} columns...", columns.size());

        List<CollibraAsset> columnAssets = new ArrayList<>();
        Set<String> missingDependencies = new HashSet<>();
        Map<String, List<String>> atscaleColumnsMap = new HashMap<>();

        columns.forEach(column -> {

            if ((CollectionUtils.isEmpty(appConfig.getFilterProjects()) || appConfig.getFilterProjects().contains(column.getCatalogName())) &&
                    !column.getExpression().isEmpty()
                    || !datasetIsTable(allDatasets, column.getDatasetName())) {

                // Make sure it's a calculated column or on a QDS


                String assetName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        instanceName,
                        org.getName(),
                        column.getCatalogName(),
                        column.getDatasetName(),
                        column.getColumnName())));

                String datasetUniqueName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        instanceName,
                        org.getName(),
                        column.getCatalogName(),
                        column.getDatasetName())));

                    // Create definition for column
                String note = UNIQUE_NAME + assetName;
                String description;
                if (!column.getExpression().isEmpty()) {
                    // Calculated column
                    note += Constants.NOTE_SEPARATOR +
                            (column.getExpression().length() > 99 ?
                                    "Calculated column with sql (truncated): " + column.getExpression().substring(0, 100)
                                    : "Calculated column with expression: " + column.getExpression());
                    description = "SQL column";
                } else {
                    note += Constants.NOTE_SEPARATOR + "Column '" + column.getColumnName() + "' with data type '" + column.getDataType() + "' on query dataset '" + column.getDatasetName() + "' in project '" + column.getCatalogName() + "'";
                    description = Constants.AT_SCALE_COLUMN;
                }
                description = description + Constants.NOTE_SEPARATOR + note;

                // @formatter:off
                CollibraAsset.Builder columnAssetBuilder = new CollibraAsset.Builder()
                        .name(assetName)
                        .displayName(column.getColumnName())
                        .domainId(appConfig.getAtscaleDataAssetDomainId())
                        .type(AssetType.ATSCALE_COLUMN)
                        .addAttributeValue(CustomConstants.AttributeType.IDENTIFIER, assetName) // There's no GUID for columns
                        .addAttributeValueDefaultToEmptyString(AttributeType.DESCRIPTION, description)
                        .addAttributeValueDefaultToEmptyString(AttributeType.DEFINITION, description)
                        .addAttributeValueDefaultToEmptyString(AttributeType.NOTE, note);

                Tools.addToMapList(atscaleColumnsMap, datasetUniqueName, assetName);

                // Add relationship from AtScale dataset to calculated columns and all columns if QDS
                if (!column.getExpression().isEmpty() || !datasetIsTable(allDatasets, column.getDatasetName())) {
                    columnAssetBuilder.addRelationByDomainId(
                            CustomConstants.RelationType.ATSCALE_DATASET_CONTAINS_ATSCALE_COLUMN,
                            Direction.SOURCE,
                            AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                    instanceName,
                                    org.getName(),
                                    column.getCatalogName(),
                                    column.getDatasetName()))),
                            appConfig.getAtscaleDataAssetDomainId());
                }

                    Set<String> logicalColsToCreate = new HashSet<>();
                    Set<String> physicalColsToCreate = new HashSet<>();

                    // Add relationship to physical column if dataset is a table and column is not calculated,
                    // and AtScale column otherwise
                    for (Dependency dependency : dependencies) {

                        if (dependency.getObject().equals(column.getColumnName()) && dependency.getObjectType().equals("CALC_COLUMN") &&
                                dependency.getCatalogName().equals(column.getCatalogName())) {
                            // If referenced is calculated column create relationship to AtScale column
                            // Otherwise make it to physical column
                            if (dependency.getReferencedObjectType().equals(Constants.COLUMN)) {
                                if (isCalculatedCol(columns, dependency.getReferencedObject()) || !datasetIsTable(allDatasets, column.getDatasetName())) {
                                    String colUniqueName = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                            instanceName,
                                            org.getName(),
                                            dependency.getCatalogName(),
                                            column.getDatasetName(),
                                            dependency.getReferencedObject())));

                                    if (inColumns(columns, dependency.getCatalogName(), dependency.getReferencedTable(), dependency.getReferencedObject())) {
                                        logicalColsToCreate.add(colUniqueName);
                                    } else {
                                        missingDependencies.add(colUniqueName);
                                    }
                                } else {
                                    // Calculated column -> physical column
                                    String fullColName = toColumnUniqueName(allDatasets, toTableName(allDatasets, dependency.getReferencedTable()), dependency.getReferencedObject());
                                    if (physicalMap.containsKey(fullColName)) {
                                        physicalColsToCreate.add(dependency.getReferencedTable()+Constants.ASSET_NAME_SEPARATOR+dependency.getReferencedObject());
                                    } else {
                                        missingDependencies.add(fullColName);
                                    }
                                }
                            } else if (dependency.getReferencedObjectType().equals("ERROR")) {
                                missingDependencies.add(toTableName(allDatasets, column.getDatasetName()) + Constants.ASSET_NAME_SEPARATOR + Tools.coalesce(dependency.getReferencedObject(), dependency.getObject()));
                            }
                        }
                    }

                    for (String logicalCol : logicalColsToCreate) {
                        columnAssetBuilder.addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_COLUMN_USES_ATSCALE_COLUMN,
                                Direction.TARGET,
                                logicalCol,
                                appConfig.getAtscaleDataAssetDomainId());
                    }
                    for (String physicalCol : physicalColsToCreate) {
                        // Need to use the specific schema where the physical data resides
                        columnAssetBuilder.addRelationByDomainId(
                                CustomConstants.RelationType.ATSCALE_COLUMN_USES_PHYSICAL_COLUMN,
                                Direction.TARGET,
                                physicalCol,
                                physicalMap.get(toDatabaseSchema(allDatasets, allConnectionGroups, org.getName(), column.getDatasetName())));
                    }

                    columnAssetBuilder.build();

                    columnAssets.add(columnAssetBuilder.build());
                }

        });

        if (!missingDependencies.isEmpty())
            printMissing(missingDependencies, "Dependent column(s)", "calculated column(s)", ATSCALE_DATASETS);

        LOGGER.info("Transformed {} columns", columnAssets.size());

        return columnAssets;

    }

    // Checks if logical column exists in full list of attributes
    private boolean inColumns(List<Column> columns, String catalogName, String datasetName, String columnName) {
        for (Column col : columns) {
            if (col.getCatalogName().equals(catalogName)
                    && col.getDatasetName().equals(datasetName)
                    && col.getColumnName().equals(columnName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCalculatedCol(Collection<Column> columns, String colName) {
        for (Column column : columns) {
            if (column.getColumnName().equals(colName)) {
                return !column.getExpression().isEmpty();
            }
        }
        return false;
    }

    private void printMissing(Set<String> missingObjectsIn, String missing, String usedObjects, String consumers) {
        // Remove fields from map columns associated with Sales Insights
        List<String> missingObjects = filterColumns(missingObjectsIn, missing);

        if (!missingObjects.isEmpty()) {
            StringBuilder msg = new StringBuilder();
            msg.append(missing).append(" not found for ").append(missingObjects.size()).append(" ").append(usedObjects).append(" used by ").append(consumers).append(" so relationships not created: ");

            // DMV queries don't distinguish between physical columns and map-based columns
            if (isMappedColumnsWarning(usedObjects, consumers)) {
                msg.append(missing).append(" not found for ").append(missingObjects.size()).append(" ").append(usedObjects).append(" used by ").append(consumers).append(" so relationships not created (if mapped column it won't be found so warning can be ignored): ");
            }

            appendColumns(missingObjects, msg);

            if (missingObjects.size() > 3) msg.append("...");
            LOGGER.warn("msg {}", msg);
        }
    }

    private List<String> filterColumns(Set<String> missingObjectsIn, String missing) {
        List<String> missingObjects = new ArrayList<>();
        for (String col : missingObjectsIn) {
            if (!missing.contains(Constants.COLUMN) || (missing.contains(Constants.COLUMN) && !Tools.inListSubset(col, Arrays.asList("style", "color", "size", "weight")))) {
                missingObjects.add(col);
            }
        }
        return missingObjects;
    }

    private boolean isMappedColumnsWarning(String usedObjects, String consumers) {
        return COLUMN_S.equals(usedObjects) && consumers.equals("attributes");
    }

    private void appendColumns(List<String> columns, StringBuilder msg) {
        for (int i = 0; i < columns.size() && i < 3; i++) {
            if (i == 0) {
                msg.append(columns.get(i));
            } else {
                msg.append(", ").append(columns.get(i));
            }
        }
    }
}

