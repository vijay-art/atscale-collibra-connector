/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.service;

import com.collibra.marketplace.atscale.api.AtScaleServerClient;
import com.collibra.marketplace.atscale.component.AtScaleApiRequest;
import com.collibra.marketplace.atscale.component.CollibraApiHelper;
import com.collibra.marketplace.atscale.component.CollibraAssetTransformer;
import com.collibra.marketplace.atscale.component.GuidCreator;
import com.collibra.marketplace.atscale.config.ApplicationConfig;
import com.collibra.marketplace.atscale.exception.DataNotFoundException;
import com.collibra.marketplace.atscale.model.*;
import com.collibra.marketplace.atscale.util.Constants;
import com.collibra.marketplace.atscale.util.Helpers;
import com.collibra.marketplace.atscale.util.Tools;
import com.collibra.marketplace.library.generated.core.model.AssetImpl;
import com.collibra.marketplace.library.integration.CollibraAsset;
import com.collibra.marketplace.library.integration.CollibraImportApiHelper;
import com.collibra.marketplace.library.integration.constants.CollibraImportResponseType;
import com.collibra.marketplace.library.integration.model.CollibraDomain;
import com.collibra.marketplace.library.integration.model.CollibraRelationValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.collibra.marketplace.atscale.util.Constants.SOURCE;
import static com.collibra.marketplace.atscale.util.Constants.TARGET;

@Service
public class MainProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainProcessor.class);

    private final CollibraAssetTransformer collibraAssetTransformer;
    private final CollibraImportApiHelper collibraImportApiHelper;
    private final AtScaleApiRequest atScaleApiRequest;
    private final ApplicationConfig appConfig;
    private final CollibraApiHelper collibraApiHelper;
    private final AtScaleServerClient atScaleServerClient;

    @Autowired
    public MainProcessor(ApplicationConfig appConfig, CollibraAssetTransformer collibraAssetTransformer,
                         CollibraImportApiHelper collibraImportApiHelper,
                         CollibraApiHelper collibraApiHelper, AtScaleApiRequest atScaleApiRequest,
                         AtScaleServerClient atScaleServerClient) {

        this.appConfig = appConfig;
        if (appConfig.getAtscaleCommunityId().length() == 0) {
            appConfig.setAtscaleCommunityId(appConfig.getCollibraBusinessAnalystsCommunityId());
        }
        this.collibraAssetTransformer = collibraAssetTransformer;
        this.collibraImportApiHelper = collibraImportApiHelper;
        this.collibraApiHelper = collibraApiHelper;
        this.atScaleApiRequest = atScaleApiRequest;
        this.atScaleServerClient = atScaleServerClient;
    }

    // Initiated by /api/setup endpoint. Manages creation of GUID's in Collibra required by AtScale
    public JsonNode setup() {
        GuidCreator guidCreator = new GuidCreator(appConfig, collibraApiHelper);
        guidCreator.createGUIDsInCollibra(appConfig);
        return null;
    }

    public JsonNode removeAssets() {
        collibraApiHelper.removeAtScaleAssets(appConfig);
        LOGGER.info("Finished removing all AtScale assets");
        return null;
    }

    public JsonNode removeTypes() {
        collibraApiHelper.removeAtScaleTypes(appConfig);
        LOGGER.info("Finished removing all AtScale types");
        return null;
    }

    public JsonNode removeObsolete() {
        collibraApiHelper.removeObsoleteAssets(appConfig);
        LOGGER.info("Finished removing obsolete assets");
        return null;
    }

    public AtScaleInstance retrieveAtScaleInstance() {
        LOGGER.info("Retrieving AtScale instance...");
        AtScaleInstance instance = new AtScaleInstance(appConfig);
        LOGGER.info("Using AtScale engine instance: {}", instance.getName());
        return instance;
    }

    public Map<String, Project> retrieveProjects(AtScaleInstance instance, Organization org) {
        LOGGER.info("Retrieving projects...");
        Map<String, Project> projectsMap = atScaleApiRequest.retrieveAllProjects(instance.getName(), org.getName());
        LOGGER.info("Retrieved {} projects", projectsMap.size());
        return projectsMap;
    }

    public List<Cube> retrieveCubes(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving cubes...");
        List<Cube> allCubes = atScaleApiRequest.retrieveAllCubes(currentProject);
        LOGGER.info("Retrieved {} cubes", allCubes.size());
        return allCubes;
    }

    public List<Measure> retrieveMeasures(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving measures...");
        List<Measure> allMeasures = atScaleApiRequest.retrieveAllMeasures(currentProject);
        LOGGER.info("Retrieved {} measures", allMeasures.size());
        return allMeasures;
    }

    public List<Dimension> retrieveDimensions(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving dimensions...");
        List<Dimension> allDimensions = atScaleApiRequest.retrieveAllDimensions(currentProject);
        LOGGER.info("Retrieved {} dimensions", allDimensions.size());
        return allDimensions;
    }

    public List<Hierarchy> retrieveHierarchies(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving hierarchies...");
        List<Hierarchy> allHierarchies = atScaleApiRequest.retrieveAllHierarchies(currentProject);
        LOGGER.info("Retrieved {} hierarchies", allHierarchies.size());
        return allHierarchies;
    }

    public List<Level> retrieveLevels(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving levels...");
        List<Level> allLevels = atScaleApiRequest.retrieveAllLevels(currentProject);
        LOGGER.info("Retrieved {} levels", allLevels.size());
        return allLevels;
    }

    public Map<String, Folder> retrieveFolders(List<Measure> allMeasures, List<Hierarchy> allHierarchies) {
        LOGGER.info("Retrieving folders...");
        Map<String, Folder> allFolders = atScaleApiRequest.retrieveAllFolders(allMeasures, allHierarchies);
        LOGGER.info("Retrieved {} folders", allFolders.size());
        return allFolders;
    }

    public List<ConnectionGroup> retrieveConnectionGroups() {
        LOGGER.info("Retrieving connection groups...");
        List<ConnectionGroup> allConnectionGroups = atScaleApiRequest.retrieveAllConnectionGroups();
        LOGGER.info("Retrieved {} connection groups", allConnectionGroups.size());
        return allConnectionGroups;
    }

    public List<Connection> retrieveConnections() {
        LOGGER.info("Retrieving connections...");
        List<Connection> allConnections = atScaleApiRequest.retrieveAllConnections();
        LOGGER.info("Retrieved {} connections", allConnections.size());
        return allConnections;
    }

    public List<Dataset> retrieveDatasets(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving datasets...");
        List<Dataset> allDatasets = atScaleApiRequest.retrieveAllDatasets(currentProject);
        LOGGER.info("Retrieved {} datasets", allDatasets.size());
        return allDatasets;
    }

    public List<Column> retrieveColumns(Map<String, Project> currentProject) {
        LOGGER.info("Retrieving columns...");
        List<Column> allColumns = atScaleApiRequest.retrieveAllColumns(currentProject);
        LOGGER.info("Retrieved {} columns", allColumns.size());
        return allColumns;
    }

    public List<Dependency> retrieveDependencies(Map<String, Project> currentProject, List<Column> allColumns) {
        LOGGER.info("Retrieving dependencies...");
        List<Dependency> allDependencies = atScaleApiRequest.retrieveAllDependencies(currentProject, allColumns);
        LOGGER.info("Retrieved {} dependencies", allDependencies.size());
        return allDependencies;
    }

    public void filterMeasureAssetsByCube(CollibraAsset cube, List<CollibraAsset> measureAssets, List<CollibraAsset> assetsToLoad) {
        assetsToLoad.clear();
        for (CollibraAsset meas : measureAssets) {
            if (meas.getName().startsWith(cube.getName() + Constants.ASSET_NAME_SEPARATOR)) {
                assetsToLoad.add(meas);
            }
        }
    }

    public void processDimensionAssets(List<CollibraAsset> assetsToLoad, List<CollibraAsset> allAssets,
                                        List<CollibraAsset> sharedAssets, List<CollibraAsset> cubeFolderAssets,
                                        CollibraAsset cube, Map.Entry<String, Project> currentProjectPair, CollibraAsset dim,
                                        String IN_ONE_IMPORT, Map<String, String> typesMap, Map<String, String> physicalMap,

                                        List<String> failedProjects, List<CollibraAsset> combinedAssets,  List<String> dimList) {
        allAssets.addAll(assetsToLoad);
        if (assetsToLoad.size() >= Constants.MAX_ASSETS_TO_LOAD / 20) {
            assetsToLoad.addAll(sharedAssets);
            assetsToLoad.addAll(cubeFolderAssets);
            assetsToLoad.add(cube);

            LOGGER.info("Loading all assets for dimension '{}.{}.{}' {}",
                    currentProjectPair.getKey(), cube.getDisplayName(), dim.getDisplayName(), IN_ONE_IMPORT);

            validateAllAssets(typesMap, assetsToLoad, physicalMap, dim.getDisplayName());
            importSomeAssets(typesMap, assetsToLoad, failedProjects, currentProjectPair.getKey(), dim.getDisplayName());
        } else {
            // Dim assets go in a combined list for upload together
            combinedAssets.addAll(assetsToLoad);
            dimList.add(dim.getDisplayName());
        }
    }

    public void synchronizeProjectMetadata(AtScaleInstance instance, Organization org, Map.Entry<String, Project> currentProjectPair, List<CollibraAsset> allAssets, List<String> failedProjects, Map<String, String> typesMap) {
        final String IN_ONE_IMPORT = "in one import";
        Map<String, Project> currentProject = new HashMap<>();
        currentProject.put(currentProjectPair.getKey(), currentProjectPair.getValue());

        List<Cube> allCubes = retrieveCubes(currentProject);
        List<Measure> allMeasures = retrieveMeasures(currentProject);
        List<Dimension> allDimensions = retrieveDimensions(currentProject);
        List<Hierarchy> allHierarchies = retrieveHierarchies(currentProject);
        List<Level> allLevels = retrieveLevels(currentProject);
        Map<String, Folder> allFolders = retrieveFolders(allMeasures, allHierarchies);
        List<ConnectionGroup> allConnectionGroups = retrieveConnectionGroups();
        List<Connection> allConnections = retrieveConnections();
        List<Dataset> allDatasets = retrieveDatasets(currentProject);
        List<Column> allColumns = retrieveColumns(currentProject);
        List<Dependency> allDependencies = retrieveDependencies(currentProject, allColumns);

        allHierarchies = removeIfHiddenDimension(allHierarchies, allDimensions);
        allLevels = removeIfHiddenHierarchy(allLevels, allHierarchies);

        Map<String, String> physicalMap = collibraApiHelper.createPhysicalMap(appConfig, allDatasets);

        CollibraAsset atscaleInstanceAsset = collibraAssetTransformer.transformAtScaleInstance(instance);
        CollibraAsset orgAsset = collibraAssetTransformer.transformOrganization(org, instance.getName());
        List<CollibraAsset> projectAssets = collibraAssetTransformer.transformProjects(currentProject.values(), allDatasets, org, instance.getName());
        List<CollibraAsset> cubeAssets = collibraAssetTransformer.transformCubes(allCubes, org, instance.getName());
        List<CollibraAsset> measureAssets = collibraAssetTransformer.transformMeasures(typesMap, allMeasures, allDependencies, allDatasets, allColumns, allConnectionGroups, physicalMap, org, instance.getName());
        List<CollibraAsset> folderAssets = collibraAssetTransformer.transformFolders(allFolders, org, instance.getName());
        List<CollibraAsset> dimensionAssets = collibraAssetTransformer.transformDimensions(allDimensions, org, instance.getName());
        // Need Levels before Hierarchies
        List<CollibraAsset> levelAssets = collibraAssetTransformer.transformLevels(typesMap, allLevels, allColumns, allDatasets, allConnectionGroups, physicalMap, org, instance.getName());
        List<CollibraAsset> hierarchyAssets = collibraAssetTransformer.transformHierarchies(allHierarchies, allLevels, allDatasets, org, instance.getName());

        List<String> connectionAssetNames = new ArrayList<>();
        List<CollibraAsset> connectionGroupAssets = collibraAssetTransformer.transformConnectionGroups(allConnectionGroups, connectionAssetNames, org, instance.getName());
        List<CollibraAsset> connectionAssets = collibraAssetTransformer.transformConnections(allConnections, allConnectionGroups, connectionGroupAssets, connectionAssetNames, org, instance.getName());
        List<CollibraAsset> columnAssets = collibraAssetTransformer.transformColumns(allColumns, allDependencies, allDatasets, allConnectionGroups, physicalMap, org, instance.getName());
        List<CollibraAsset> datasetAssets = collibraAssetTransformer.transformDatasets(allDatasets, allConnectionGroups, physicalMap, org, instance.getName());
        // Remove columns after we have physical columns on which to create relationships

        List<CollibraAsset> assetsToLoad = new ArrayList<>();
        List<CollibraAsset> assetsByCube = new ArrayList<>();
        List<CollibraAsset> sharedAssets = new ArrayList<>();
        List<CollibraAsset> assetsToCheck = new ArrayList<>();

        // Measure and folder assets are managed individually
        sharedAssets.add(atscaleInstanceAsset);
        sharedAssets.add(orgAsset);
        sharedAssets.addAll(projectAssets);
        assetsByCube.addAll(cubeAssets);
        assetsByCube.addAll(dimensionAssets);
        assetsByCube.addAll(levelAssets);
        assetsByCube.addAll(hierarchyAssets);
        sharedAssets.addAll(connectionGroupAssets);
        sharedAssets.addAll(connectionAssets);
        sharedAssets.addAll(datasetAssets);
        sharedAssets.addAll(columnAssets);

        loadAllAssetsForProject(levelAssets, measureAssets, sharedAssets, currentProjectPair, IN_ONE_IMPORT, assetsToLoad,
                assetsByCube, folderAssets, typesMap, physicalMap, failedProjects, allAssets, cubeAssets, assetsToCheck, dimensionAssets);
        allAssets.addAll(sharedAssets);
    }

    public void loadAllAssetsForProject(List<CollibraAsset> levelAssets, List<CollibraAsset> measureAssets, List<CollibraAsset> sharedAssets, Map.Entry<String, Project> currentProjectPair, String IN_ONE_IMPORT, List<CollibraAsset> assetsToLoad, List<CollibraAsset> assetsByCube, List<CollibraAsset> folderAssets, Map<String, String> typesMap, Map<String, String> physicalMap, List<String> failedProjects, List<CollibraAsset> allAssets, List<CollibraAsset> cubeAssets, List<CollibraAsset> assetsToCheck, List<CollibraAsset> dimensionAssets) {
        // If number of assets is too high, split into cubes plus physical assets and import separately
        if (levelAssets.size() + measureAssets.size() + sharedAssets.size() < Constants.MAX_ASSETS_TO_LOAD) {
            LOGGER.info("Loading all assets for project '{}' {}", currentProjectPair.getKey(), IN_ONE_IMPORT);
            assetsToLoad.addAll(sharedAssets);
            assetsToLoad.addAll(assetsByCube);
            assetsToLoad.addAll(folderAssets);
            assetsToLoad.addAll(measureAssets);

            validateAllAssets(typesMap, assetsToLoad, physicalMap, "");
            importSomeAssets(typesMap, assetsToLoad, failedProjects, currentProjectPair.getKey(), "");

            allAssets.addAll(assetsByCube);
            allAssets.addAll(folderAssets);
            allAssets.addAll(measureAssets);
        } else {
            //Can't load full project '{}' because of size constraints so loading by individual cube
            loadProjectByCube(cubeAssets, allAssets, assetsToCheck, assetsByCube, measureAssets, folderAssets, assetsToLoad, sharedAssets, currentProjectPair,
                    typesMap, physicalMap, failedProjects, dimensionAssets, IN_ONE_IMPORT);
        }
    }

    public void loadProjectByCube(List<CollibraAsset> cubeAssets, List<CollibraAsset> allAssets, List<CollibraAsset> assetsToCheck, List<CollibraAsset> assetsByCube, List<CollibraAsset> measureAssets, List<CollibraAsset> folderAssets, List<CollibraAsset> assetsToLoad, List<CollibraAsset> sharedAssets, Map.Entry<String, Project> currentProjectPair, Map<String, String> typesMap, Map<String, String> physicalMap, List<String> failedProjects, List<CollibraAsset> dimensionAssets, String IN_ONE_IMPORT) {
        LOGGER.warn("Can't load full project '{}' because of size constraints so loading by individual cube", currentProjectPair.getKey());
        for (CollibraAsset cube : cubeAssets) {
            allAssets.add(cube);
            assetsToLoad.clear();
            assetsToCheck.clear();
            assetsToCheck.addAll(assetsByCube);
            assetsToCheck.addAll(measureAssets);
            assetsToCheck.addAll(folderAssets);
            filterAssetsToLoad(assetsToCheck, assetsToLoad, cube);
            loadCubeAssets(assetsToLoad,assetsByCube, sharedAssets, measureAssets, currentProjectPair, cube, IN_ONE_IMPORT, allAssets, typesMap, physicalMap,
                    failedProjects, folderAssets, dimensionAssets);
        }
    }

    // private void

    private void filterAssetsToLoad(List<CollibraAsset> assetsToCheck, List<CollibraAsset> assetsToLoad, CollibraAsset cube) {
        for (CollibraAsset asset : assetsToCheck) {
            if (asset.getName().startsWith(cube.getName() + Constants.ASSET_NAME_SEPARATOR)) {
                assetsToLoad.add(asset);
            }
        }
    }

    private void loadCubeAssets(List<CollibraAsset> assetsToLoad, List<CollibraAsset> assetsByCube, List<CollibraAsset> sharedAssets, List<CollibraAsset> measureAssets, Map.Entry<String, Project> currentProjectPair, CollibraAsset cube, String IN_ONE_IMPORT, List<CollibraAsset> allAssets, Map<String, String> typesMap, Map<String, String> physicalMap, List<String> failedProjects, List<CollibraAsset> folderAssets, List<CollibraAsset> dimensionAssets) {
        if (assetsToLoad.size() + sharedAssets.size() < Constants.MAX_ASSETS_TO_LOAD) {
            LOGGER.info("Loading all assets for cube '{}.{}' {}", currentProjectPair.getKey(), cube.getDisplayName(), IN_ONE_IMPORT);
            allAssets.addAll(assetsToLoad);
            assetsToLoad.addAll(sharedAssets);
            assetsToLoad.add(cube);
            validateAllAssets(typesMap, assetsToLoad, physicalMap, cube.getDisplayName());
            importSomeAssets(typesMap, assetsToLoad, failedProjects, currentProjectPair.getKey(), cube.getDisplayName());
        } else {
            LOGGER.warn("Can't load cube '{}.{}' because of size constraints so loading by individual dimension", currentProjectPair.getKey(), cube.getDisplayName());

            // Gather all folders for current cube

            List<CollibraAsset> cubeFolderAssets = filterCubeFolderAssets(folderAssets, cube);

            allAssets.addAll(cubeFolderAssets);

            List<CollibraAsset> combinedAssets = new ArrayList<>();
            List<String> dimList = new ArrayList<>();

            // Have a single very large cube. Need to divide up by dimension
            // If the number of assets in the dim is small, put it in the combined list
            // otherwise process it individually. This is to save time in uploading.
            processDimensions(dimensionAssets, assetsToLoad, assetsByCube, cube, allAssets, sharedAssets, cubeFolderAssets, currentProjectPair, IN_ONE_IMPORT, typesMap, physicalMap,  failedProjects,  combinedAssets,  dimList);

            loadAssetsForSmallerDimensionsInCube(combinedAssets, sharedAssets, cubeFolderAssets, cube, dimList, currentProjectPair, typesMap, physicalMap, failedProjects, IN_ONE_IMPORT);
            // Now gather measure data
            filterMeasureAssetsByCube(cube, measureAssets, assetsToLoad); //will add measured asset, filtered by cube name to assetsToLoad

            allAssets.addAll(assetsToLoad);
            assetsToLoad.addAll(sharedAssets);
            assetsToLoad.addAll(cubeFolderAssets);
            assetsToLoad.add(cube);

            LOGGER.info("Loading all assets for measures in cube '{}.{}' {}", currentProjectPair.getKey(), cube.getDisplayName(), IN_ONE_IMPORT);

            validateAllAssets(typesMap, assetsToLoad, physicalMap, "measures");
            importSomeAssets(typesMap, assetsToLoad, failedProjects, currentProjectPair.getKey(), "measures");
        }
    }

    private List<CollibraAsset> filterCubeFolderAssets(List<CollibraAsset> folderAssets, CollibraAsset cube) {
        List<CollibraAsset> cubeFolderAssets = new ArrayList<>();
        for (CollibraAsset asset : folderAssets) {
            if (asset.getName().startsWith(cube.getName() + Constants.ASSET_NAME_SEPARATOR)) {
                cubeFolderAssets.add(asset);
            }
        }
        return cubeFolderAssets;
    }

    private void processDimensions(List<CollibraAsset> dimensionAssets, List<CollibraAsset> assetsToLoad,
                                   List<CollibraAsset> assetsByCube, CollibraAsset cube, List<CollibraAsset> allAssets,
                                   List<CollibraAsset> sharedAssets, List<CollibraAsset> cubeFolderAssets,
                                   Map.Entry<String, Project> currentProjectPair, String IN_ONE_IMPORT, Map<String, String> typesMap,
                                   Map<String, String> physicalMap, List<String> failedProjects, List<CollibraAsset> combinedAssets, List<String> dimList) {
        for (CollibraAsset dim : dimensionAssets) {
            assetsToLoad.clear();

            for (CollibraAsset asset : assetsByCube) {
                if (asset.getName().startsWith(cube.getName() + Constants.ASSET_NAME_SEPARATOR) && asset.getName().startsWith(dim.getName())) {
                    assetsToLoad.add(asset);
                }
            }
            processDimensionAssets(assetsToLoad, allAssets, sharedAssets, cubeFolderAssets, cube, currentProjectPair, dim, IN_ONE_IMPORT, typesMap, physicalMap, failedProjects, combinedAssets, dimList);
        }
    }

    private void loadAssetsForSmallerDimensionsInCube(List<CollibraAsset> combinedAssets, List<CollibraAsset> sharedAssets,
                                                      List<CollibraAsset> cubeFolderAssets, CollibraAsset cube,
                                                      List<String> dimList, Map.Entry<String, Project> currentProjectPair,
                                                      Map<String, String> typesMap, Map<String, String> physicalMap,
                                                      List<String> failedProjects, String IN_ONE_IMPORT) {
        if (!combinedAssets.isEmpty()) {
            combinedAssets.addAll(sharedAssets);
            combinedAssets.addAll(cubeFolderAssets);
            combinedAssets.add(cube);

            LOGGER.info("Loading all assets for smaller dimensions ({}) in cube '{}.{}' {}", dimList, currentProjectPair.getKey(), cube.getDisplayName(), IN_ONE_IMPORT);

            validateAllAssets(typesMap, combinedAssets, physicalMap, cube.getDisplayName());
            importSomeAssets(typesMap, combinedAssets, failedProjects, currentProjectPair.getKey(), cube.getDisplayName());
        }
    }


    private Map<String, Project> addAllSuccessfulProjects(Map<String, Project> projectsMap, List<String> failedProjects) {
        Map<String, Project> allProjects = new HashMap<>();
        for (Map.Entry<String, Project> projEntry : projectsMap.entrySet()) {
            if (!failedProjects.contains(projEntry.getKey())) {
                allProjects.put(projEntry.getKey(), projEntry.getValue());
            }
        }
        return allProjects;
    }

    /**
         * Main method used to synchronize the metadata.
         */
    // Initiated by /api/sync endpoint
    public JsonNode start(String orgName, String orgId) {
        LOGGER.info("***************** Start of run *****************");

        LOGGER.info("Started synchronizing the metadata");

        AtScaleInstance instance = retrieveAtScaleInstance();
        Organization org = retrieveOrganization(orgName, orgId); // Organization: If values exist as endpoint parameters use those, otherwise use what's in properties.

        Map<String, Project> projectsMap = retrieveProjects(instance, org);

        List<CollibraAsset> allAssets = new ArrayList<>(); // Holds everything being imported for obsolete assets handling
        List<String> failedProjects = new ArrayList<>();

        Map<String, AssetImpl> objectMap = populateObjectMap();
        Map<String, String> typesMap = populateTypesMap(); // Includes domains for databases and schemas, all relationships

        for (Map.Entry<String, Project> currentProjectPair : projectsMap.entrySet()) {
            synchronizeProjectMetadata(instance, org, currentProjectPair, allAssets, failedProjects, typesMap);
        }

        LOGGER.info("Setting obsolete assets...");

        // Include all successfully imported projects for marking as obsolete
        Map<String, Project> allProjects = addAllSuccessfulProjects(projectsMap, failedProjects);

        List<CollibraDomain> domains = new ArrayList<>();
        List<CollibraAsset> assetsToMarkObsolete = collibraApiHelper.getAssetsToMarkAsObsolete(allAssets, orgName, instance.getName(), allProjects, objectMap, appConfig, domains);

        LOGGER.info("Finished synchronizing the metadata for all projects");

        return null;
    }

    private void markAssetsObsolete(List<CollibraAsset> assetsToMarkObsolete, CollibraImportApiHelper collibraImportApiHelper) {
        if (!assetsToMarkObsolete.isEmpty()) {
            UUID obsoleteImportId = collibraImportApiHelper.importAssets(assetsToMarkObsolete, CollibraImportResponseType.COUNTS);
            collibraImportApiHelper.removeImportResponse(obsoleteImportId);
            LOGGER.debug("Obsolete assets updated (Collibra code).");
            LOGGER.info("Set obsolete assets");
        }
    }



    public Map<String, Integer> populateDimSizeMap(List<CollibraAsset> assetsByCube, List<CollibraAsset> dimAassets, String cubeName) {
        Map<String, Integer> dimSizeMap = new HashMap<>();
        for (CollibraAsset dim : dimAassets) {
            for (CollibraAsset asset : assetsByCube) {
                if (asset.getName().startsWith(cubeName + Constants.ASSET_NAME_SEPARATOR) &&
                        asset.getName().startsWith(dim.getName())) {
                    if (dimSizeMap.containsKey(dim.getName())) {
                        dimSizeMap.put(dim.getName(), dimSizeMap.get(dim.getName()).intValue() + 1);
                    } else {
                        dimSizeMap.put(dim.getName(), 1);
                    }
                }
            }
        }

        LOGGER.info("*** Dim Sizes for cube {} ***", cubeName);
        for (Map.Entry<String, Integer> pair : dimSizeMap.entrySet()) {
            LOGGER.info("    {}: {}", pair.getKey(), pair.getValue());
        }
        return dimSizeMap;
    }

    public Boolean importSomeAssets(Map<String, String> typesMap, List<CollibraAsset> assets, List<String> failedProjects, String projectName, String cubeName) {
        try {
            String fileDownloadPath = System.getProperty("user.home") + "/Downloads/";
            saveImportJson(assets, fileDownloadPath + "AtScaleImportToCollibra.json");
            saveImportObjects(typesMap, assets, fileDownloadPath + "AtScaleImportToCollibra.tsv");
        } catch (Exception e) {
            LOGGER.error(String.format("Error saving assets to file: %s", e.getMessage()));
        }

        if (Constants.DO_IMPORTS) {
            if (cubeName.isEmpty()) {
                LOGGER.info("Importing {} assets", assets.size());
            } else {
                LOGGER.info("Importing %d assets for cube '{}'...{}", assets.size(), cubeName);
            }

            UUID importId = UUID.randomUUID();
            try {
                LOGGER.info("Starting import of assets into Collibra with UUID: {}", importId);
                collibraImportApiHelper.importAssets(importId, assets, CollibraImportResponseType.COUNTS);
            } catch (Exception e) {
                failedProjects.add(projectName);
                LOGGER.error(String.format("Import for project '%s' failed so will be skipped", projectName));
                LOGGER.error(e.getMessage());
                return false;
            }

            if (cubeName.isEmpty()) {
                LOGGER.info("Finished importing the metadata for project '{}'", projectName);
            } else {
                LOGGER.info("Finished importing the metadata for cube '{}.{}'", projectName, cubeName);
            }
            return true;
        }
        return true;
    }

    public void saveImportJson(List<CollibraAsset> allProjectAssets, String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writerWithDefaultPrettyPrinter().writeValueAsString(allProjectAssets);
        // <strong>objectMapper.setSerializationInclusion(Include.NON_NULL);</strong>

        LOGGER.info("Writing project assets to JSON file '{}'", path);
        try (FileWriter file = new FileWriter(path)) {
            file.write(jsonString);
            LOGGER.info("Successfully wrote JSON assets to file '{}'", path);
        }
    }

    private void saveImportObjects(Map<String, String> typesMap, List<CollibraAsset> allProjectAssets, String path) throws IOException {
        StringBuilder out = new StringBuilder("NAME\tTYPE\tDOMAIN\tRELATIONS");
        LOGGER.info("Writing project assets to text file '{}'", path);
        for (CollibraAsset asset : allProjectAssets) {
            out.append("\n").append(asset.getName()).append("\t").append(typesMap.get(asset.getType().getId())).append("\t").append(typesMap.get(asset.getDomain().getId()));
            for (Map.Entry<String, List<CollibraRelationValue>> entry : asset.getRelations().entrySet()) {
                out.append(entry.getKey().contains(SOURCE) ? "\tSOURCE" : "")
                        .append(entry.getKey().contains(TARGET) ? "\tTARGET" : "")
                        .append("\t").append(typesMap.get(entry.getKey().replace(":SOURCE", "").replace(":TARGET", "")))
                        .append("\t").append(entry.getValue().get(0).getName())
                        .append("\t").append(typesMap.get(entry.getValue().get(0).getDomain().getId()));
            }
        }
        out.append("\n");
        try (FileWriter file = new FileWriter(path)) {
            file.write(out.toString());
            LOGGER.info("Successfully wrote assets to file '{}'", path);
        }
    }

    public List<Hierarchy> removeIfHiddenDimension(List<Hierarchy> allHierarchies, List<Dimension> allDimensions) {
        List<Hierarchy> newHierarchies = new ArrayList<>();
        for (Hierarchy hier : allHierarchies) {
            boolean found = false;
            for (Dimension dim : allDimensions) {
                if (hier.getDimensionUniqueName().equals(dim.getDimensionUniqueName())) {
                    found = true;
                }
            }
            if (found) {
                newHierarchies.add(hier);
            } else {
                LOGGER.info("Hierarchy {} will not be added since its dimension is not found. It may be marked as invisible or be part of a security dimension", hier.getHierarchyUniqueName());
            }
        }
        return newHierarchies;
    }

    public List<Level> removeIfHiddenHierarchy(List<Level> allLevels, List<Hierarchy> allHierarchies) {
        List<Level> newLevels = new ArrayList<>();
        for (Level level : allLevels) {
            boolean found = false;
            for (Hierarchy hier : allHierarchies) {
                if (level.getHierarchyUniqueName().equals(hier.getHierarchyUniqueName())) {
                    found = true;
                }
            }
            if (found) {
                newLevels.add(level);
            } else {
                LOGGER.info("Level {} will not be added since its hierarchy is not found. It may be marked as invisible or be part of a security dimension", level.getLevelUniqueName());
            }
        }
        return newLevels;
    }

    public Organization retrieveOrganization(String orgName, String orgId) {
        LOGGER.info("Retrieving organization...");
        Organization org = new Organization();
        org.setName(appConfig.getAtscaleOrganizationFilterName());
        org.setOrganizationGUID(appConfig.getAtscaleOrganizationFilterGUID());

        if (orgName != null && orgName.length() > 0 && orgId != null && orgId.length() > 0) {
            org.setName(orgName);
            org.setOrganizationGUID(orgId);
            appConfig.setAtscaleOrganizationFilterName(orgName);
            appConfig.setAtscaleOrganizationFilterGUID(orgId);
            atScaleServerClient.setOrgName(orgName);
            atScaleServerClient.setOrgGUID(orgId);
            atScaleServerClient.buildAuthorizationURL();
            atScaleServerClient.buildQueryURL();
            atScaleApiRequest.setAtScaleServerClient(atScaleServerClient);
        } else if ((orgName != null && orgName.length() > 0) || (orgId != null && orgId.length() > 0)) {
            throw new DataNotFoundException("If passing in parameters for an organization, both the name and UUID must be included");
        }
        LOGGER.info("Using organization '{}' with UUID '{}'", org.getName(), org.getOrganizationGUID());
        return org;
    }

    private Map<String, AssetImpl> populateObjectMap() {
        Map<String, AssetImpl> objectMap = new HashMap<>();
        objectMap = collibraApiHelper.createObjectMap(objectMap, UUID.fromString(appConfig.getAtscaleBusinessAssetDomainId()), appConfig.getAtscaleBusinessAssetDomainName());
        objectMap = collibraApiHelper.createObjectMap(objectMap, UUID.fromString(appConfig.getAtscaleDataAssetDomainId()), appConfig.getAtscaleDataAssetDomainName());
        return objectMap;
    }

    private Map<String, String> populateTypesMap() {
        Map<String, String> typesMap;
        typesMap = collibraApiHelper.createTypesMap();
        return typesMap;
    }

    public void printStatuses(Map<String, AssetImpl> objectMap, String msg) {
        Map<String, Integer> statusCount = new HashMap<>();
        for (Map.Entry<String, AssetImpl> pair : objectMap.entrySet()) {
            if (statusCount.containsKey(pair.getValue().getStatus().getName())) {
                statusCount.put(pair.getValue().getStatus().getName(), statusCount.get(pair.getValue().getStatus().getName()).intValue() + 1);
            } else {
                statusCount.put(pair.getValue().getStatus().getName(), 1);
            }
        }

        LOGGER.info("*** Statuses {} ***", msg);
        for (Map.Entry<String, Integer> pair : statusCount.entrySet()) {
            LOGGER.info(" {}: {}", pair.getKey(), pair.getValue());
        }
    }

    /**
     * We don't want to wait for the Collibra platform to validate then error out if necessary.
     * Instead, catch missing objects in relations ahead, WARN, then error out before sending to Collibra.
     */
    public void validateAllAssets(Map<String, String> typesMap, List<CollibraAsset> allAssets, Map<String, String> physicalMap, String cubeName) {
        Map<String, CollibraAsset> assetsMap = new HashMap<>();
        boolean errorFound = false;
        logMetadataValidationStart(allAssets, cubeName);

        // Check for names that are not unique
        errorFound = isNamesNotUnique(allAssets, errorFound);

        // Populate a hashmap with unique names of objects
        for (CollibraAsset asset : allAssets) {
            assetsMap.put(asset.getName(), asset);
        }

        // Now ensure all referenced objects exist in the map
        // Also check that they're in the right domain and of the right type for the relationship
        errorFound = checkReferencedObjectsExistence(allAssets, assetsMap, errorFound, typesMap, physicalMap);

        if (errorFound) {
            throw new DataNotFoundException("Asset validation errors found so aborting");
        }
        LOGGER.info("Metadata validation successful");
    }

    private boolean checkReferencedObjectsExistence(List<CollibraAsset> allAssets, Map<String, CollibraAsset> assetsMap, boolean errorFound, Map<String, String> typesMap, Map<String, String> physicalMap) {
        for (CollibraAsset asset : allAssets) {
            for (Map.Entry<String, List<CollibraRelationValue>> relation : asset.getRelations().entrySet()) {
                for (CollibraRelationValue destination : relation.getValue()) {
                    if (!domainExists(destination.getDomain())) {
                        LOGGER.warn("Destination domain not found for relation from '{}' ({}) TO '{}' so relation will not be created", asset.getName(), typesMap.get(asset.getType().getId()), destination.getName());
                        errorFound = true;
                        continue;
                    }

                    boolean found = false;

                    if (assetsMap.containsKey(destination.getName())) {
                        found = true;
                        // Check that destination domain or asset matches the asset domain
                        errorFound = isDestinationDomainMismatch(destination, assetsMap, asset, typesMap, errorFound);
                        errorFound = isDestinationAssetTypeMismatch(destination, assetsMap, asset, typesMap, errorFound, relation);
                    } else if (physicalMap.containsKey(physicalMap.get(destination.getDomain().getId()) + Constants.ASSET_NAME_SEPARATOR + destination.getName()) ||
                            physicalMap.containsKey(destination.getName())) {
                        found = true;
                    }

                    if (!found) {
                        errorFound = true;
                        logDestinationNotFoundWarning(relation, typesMap, asset, destination);
                    }
                }
            }
        }
        return errorFound;
    }

    public boolean isNamesNotUnique(List<CollibraAsset> allAssets, Boolean errorFound) {
        for (int i = 0; i < allAssets.size(); i++) {
            for (int j = 0; j < allAssets.size(); j++) {
                if (allAssets.get(i).getName().equals(allAssets.get(j).getName()) && i != j) {
                    LOGGER.warn("Multiple objects with the same unique name found: {} TYPE: {}   <-->   {} TYPE: {}", allAssets.get(i).getName(), allAssets.get(j).getType().getId(), allAssets.get(j).getName(), allAssets.get(j).getType().getId());
                    errorFound = true;
                }
            }
        }
        return errorFound;
    }

    public Boolean isDestinationDomainMismatch(CollibraRelationValue destination, Map<String, CollibraAsset> assetsMap, CollibraAsset asset, Map<String, String> typesMap, boolean errorFound) {
        if (!destination.getDomain().getId().equals(assetsMap.get(destination.getName()).getDomain().getId())) {
            LOGGER.warn("Destination domain in relationship does not match the asset domain for '{}' ({}) TO '{}' so relation will not be created", asset.getName(), typesMap.get(asset.getType().getId()), destination.getName());
            errorFound = true;
        }
        return errorFound;
    }

    public Boolean isDestinationAssetTypeMismatch(CollibraRelationValue destination, Map<String, CollibraAsset> assetsMap, CollibraAsset asset, Map<String, String> typesMap, boolean errorFound, Map.Entry<String, List<CollibraRelationValue>> relation) {
        if (!Helpers.typeIdFromRelationUUID(relation.getKey()).equals(assetsMap.get(destination.getName()).getType().getId())) {
            LOGGER.warn("Destination asset type in relationship does not match the asset type for '{}' ({}) TO '{}' so relation will not be created", asset.getName(), typesMap.get(asset.getType().getId()), destination.getName());
            errorFound = true;
        }
        return errorFound;
    }

    public void logDestinationNotFoundWarning(Map.Entry<String, List<CollibraRelationValue>> relation, Map<String, String> typesMap, CollibraAsset asset, CollibraRelationValue destination) {
        if (relation.getKey().endsWith(SOURCE)) {
            LOGGER.warn("Destination object not found for SOURCE relation {} from '{}' ({}) TO '{}' so relation will not be created", typesMap.get(relation.getKey().substring(0, 36)), asset.getName(), typesMap.get(asset.getType().getId()), destination.getName());
        } else if (relation.getKey().endsWith(TARGET)) {
            LOGGER.warn("Destination object not found for TARGET relation from '{}' ({}) TO '{}' so relation will not be created ", asset.getName(), typesMap.get(asset.getType().getId()), destination.getName());
        } else {
            LOGGER.warn("Destination object not found for relation from '{}' ({}) TO '{}' so relation will not be created", asset.getName(), typesMap.get(asset.getType().getId()), destination.getName());
        }
    }

    public void logMetadataValidationStart(List<CollibraAsset> allAssets, String cubeName) {
        String message = "Starting metadata validation with " + allAssets.size() + " assets";
        if (!cubeName.isEmpty()) {
            message += " for: " + cubeName;
        }
        LOGGER.info(message);
    }

    public boolean domainExists(CollibraDomain domain) {
        return !(domain == null || domain.getId() == null || domain.getId().length() == 0);
    }


    public JsonNode syncCollibraToAtscale() {
        List<Project> collibraProjectList = this.collibraApiHelper.getCollibraAssets(appConfig);
        AtScaleInstance instance = retrieveAtScaleInstance();
        Organization org = retrieveOrganization(null, null); // Organization: If values exist as endpoint parameters use those, otherwise use what's in properties.
        Map<String, Project> projectsMap = retrieveProjects(instance, org);
        for (Map.Entry<String, Project> currentProjectPair : projectsMap.entrySet()) {

            try {
                List<Measure> collibraProjectMeasureList = Tools.getCollibraProjectMeasureList(collibraProjectList, currentProjectPair.getKey());
                Map<String, Project> currentProject = new HashMap<>();
                currentProject.put(currentProjectPair.getKey(), currentProjectPair.getValue());
                List<Measure> allMeasures = retrieveMeasures(currentProject);
                if (!Objects.requireNonNull(collibraProjectMeasureList).isEmpty()) {
                    Map<String, Measure> collibraMeasureMap = Tools.convertCollibraMeasureASMap(collibraProjectMeasureList);
                    Map<String, Measure> updatedMeasureMap = new HashMap<>();
                    boolean flag = Tools.compareMeasureDescription(collibraMeasureMap, allMeasures, updatedMeasureMap);
                    if (flag) {
                        atScaleApiRequest.publishProject(currentProjectPair, updatedMeasureMap);
                    }

                }
            }catch (Exception e){
                LOGGER.error("Error>>>>",e);
            }
        }
        return null;

    }



}
