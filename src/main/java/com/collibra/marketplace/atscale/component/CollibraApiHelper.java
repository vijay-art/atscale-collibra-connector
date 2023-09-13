package com.collibra.marketplace.atscale.component;

import com.collibra.marketplace.atscale.config.ApplicationConfig;
import com.collibra.marketplace.atscale.exception.DataNotFoundException;
import com.collibra.marketplace.atscale.model.Dataset;
import com.collibra.marketplace.atscale.model.Project;
import com.collibra.marketplace.atscale.util.Constants;
import com.collibra.marketplace.atscale.util.CustomConstants;
import com.collibra.marketplace.atscale.util.Tools;
import com.collibra.marketplace.library.generated.core.ApiClient;
import com.collibra.marketplace.library.generated.core.api.*;
import com.collibra.marketplace.library.generated.core.model.*;
import com.collibra.marketplace.library.integration.CollibraAsset;
import com.collibra.marketplace.library.integration.constants.CollibraConstants;
import com.collibra.marketplace.library.integration.model.CollibraDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.collibra.marketplace.atscale.util.Constants.OBSOLETE;

@Service
public class CollibraApiHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollibraApiHelper.class);
    private final ApiClient collibraCoreApi;

    @Autowired
    public CollibraApiHelper(ApiClient collibraCoreApi) {
        this.collibraCoreApi = collibraCoreApi;
    }

    public AssetTypeImpl addAssetType(AddAssetTypeRequest addAssetTypeRequest) {
        AssetTypesApi assetTypesApi = collibraCoreApi.buildClient(AssetTypesApi.class);
        return assetTypesApi.addAssetType(addAssetTypeRequest);
    }

    public RelationTypeImpl addRelationType(AddRelationTypeRequest addRelationTypeRequest) {
        RelationTypesApi relationTypesApi = collibraCoreApi.buildClient(RelationTypesApi.class);
        return relationTypesApi.addRelationType(addRelationTypeRequest);
    }

    public DomainImpl addDomain(AddDomainRequest addDomainRequest) {
        DomainsApi domainsApi = collibraCoreApi.buildClient(DomainsApi.class);
        return domainsApi.addDomain(addDomainRequest);
    }

    public boolean relationTypeExists(UUID uuid) throws DataNotFoundException {
        RelationTypesApi relationTypesApi = null;
        boolean isRelationTypeExist = true;
        try {
            relationTypesApi = collibraCoreApi.buildClient(RelationTypesApi.class);
            relationTypesApi.getRelationType(uuid);
        } catch (Exception e) {
            if (e.getMessage().contains("relationTypeNotFoundId")) {
                isRelationTypeExist = false;
            } else {
                throw e;
            }
        }
        return isRelationTypeExist;
    }

    public void deleteAllAtScaleTypes(ApplicationConfig appConfig) {
        LOGGER.info("Removing all AtScale resource types from Collibra platform");
        deleteAtScaleRelations();
        deleteAtScaleAttributes();
        deleteAtScaleAssetTypes();
        deleteAtScaleDomains(appConfig);
        LOGGER.info("Finished removing all AtScale resource types from Collibra platform");
    }

    public void deleteAtScaleRelations() {
        List<UUID> uuids = new ArrayList<>();

        RelationTypesApi relationTypesApi = collibraCoreApi.buildClient(RelationTypesApi.class);
        for (CustomConstants.RelationType atscaleRelationType : CustomConstants.RelationType.values()) {
            uuids.add(UUID.fromString(atscaleRelationType.getId()));
        }

        try {
            relationTypesApi.removeRelationTypes(uuids);
            LOGGER.info("Removed {} AtScale relation type(s) from Collibra platform", uuids.size());
        } catch (Exception e) {
            LOGGER.error("Error received deleting AtScale relation type(s)", e);
        }
    }

    public void deleteAtScaleAttributes() {
        List<UUID> uuids = new ArrayList<>();

        AttributeTypesApi attributeTypesApi = collibraCoreApi.buildClient(AttributeTypesApi.class);
        for (CustomConstants.AttributeType atscaleAttributeType : CustomConstants.AttributeType.values()) {
            uuids.add(UUID.fromString(atscaleAttributeType.getId()));
        }

        try {
            attributeTypesApi.removeAttributeTypes(uuids);
            LOGGER.info("Removed {} AtScale attribute(s) from Collibra platform", uuids.size());
        } catch (Exception e) {
            LOGGER.error("Error received deleting AtScale attribute(s)", e);
        }
    }

    public void deleteAtScaleAssetTypes() {
        List<UUID> uuids = new ArrayList<>();

        AssetTypesApi assetTypesApi = collibraCoreApi.buildClient(AssetTypesApi.class);
        for (CustomConstants.AssetType atscaleAssetType : CustomConstants.AssetType.values()) {
            uuids.add(UUID.fromString(atscaleAssetType.getId()));
        }

        try {
            assetTypesApi.removeAssetTypes(uuids);
            LOGGER.info("Removed {} AtScale asset type(s) from Collibra platform", uuids.size());

        } catch (Exception e) {
            LOGGER.warn("Error received deleting AtScale asset type(s): {}", e.getMessage());
        }
    }

    public void deleteAtScaleAssets(ApplicationConfig appConfig) {
        deleteAssetsFromDomain(UUID.fromString(appConfig.getAtscaleBusinessAssetDomainId()), appConfig.getAtscaleBusinessAssetDomainName());
        deleteAssetsFromDomain(UUID.fromString(appConfig.getAtscaleDataAssetDomainId()), appConfig.getAtscaleDataAssetDomainName());
    }

    // The API only removes 1000 assets at a time so need to loop it until empty
    public void deleteAssetsFromDomain(UUID domainUUID, String domainName) {
        List<UUID> uuids = new ArrayList<>();
        Integer count = 0;

        AssetsApi assetsApi = collibraCoreApi.buildClient(AssetsApi.class);
        AssetsApi.FindAssetsQueryParams params = new AssetsApi.FindAssetsQueryParams();
        params.domainId(domainUUID);

        LOGGER.info("Removing all AtScale assets from domain '{}'", domainName);

        do {
            uuids.clear();

            AssetPagedResponse apr = assetsApi.findAssets(params);
            if (apr.getResults() != null) {
                for (AssetImpl asset : apr.getResults()) {
                    uuids.add(asset.getId());
                }
            }

            try {
                assetsApi.removeAssets(uuids);
                LOGGER.info("Removed {} asset(s)", uuids.size());
                count += uuids.size();
            } catch (Exception e) {
                LOGGER.error("Error received deleting AtScale asset(s): {}", e.getMessage());
            }
        } while (uuids.size() >= 1000);
        LOGGER.info("Removed a total of {} asset(s) from domain '{}'", count, domainName);
    }

    public void removeObsoleteAssets(ApplicationConfig appConfig) {
        deleteObsoleteAssetsFromDomain(UUID.fromString(appConfig.getAtscaleBusinessAssetDomainId()), appConfig.getAtscaleBusinessAssetDomainName());
        deleteObsoleteAssetsFromDomain(UUID.fromString(appConfig.getAtscaleDataAssetDomainId()), appConfig.getAtscaleDataAssetDomainName());
    }

    public void removeAtScaleAssets(ApplicationConfig appConfig) {
        this.deleteAtScaleAssets(appConfig);
    }

    public void removeAtScaleTypes(ApplicationConfig appConfig) {
        this.deleteAllAtScaleTypes(appConfig);
    }

    public void deleteObsoleteAssetsFromDomain(UUID domainUUID, String domainName) {
        List<UUID> uuids = new ArrayList<>();
        AssetsApi assetsApi = this.collibraCoreApi.buildClient(AssetsApi.class);
        AssetsApi.FindAssetsQueryParams params = new AssetsApi.FindAssetsQueryParams();
        params.domainId(domainUUID);
        AssetPagedResponse apr = assetsApi.findAssets(params);
        if (apr.getResults() != null) {
            for (AssetImpl asset : apr.getResults()) {
                if (asset.getStatus().getName().toLowerCase(Locale.ROOT).equals(OBSOLETE)) {
                    uuids.add(asset.getId());
                }
            }
        }

        try {
            assetsApi.removeAssets(uuids);
            LOGGER.info("Removed {} obsolete AtScale asset(s) from domain '{}'", uuids.size(), domainName);
        } catch (Exception e) {
            LOGGER.warn("Error received deleting obsolete AtScale asset(s): {}", e.getMessage());
        }
    }

    public void deleteAtScaleDomains(ApplicationConfig appConfig) {
        List<UUID> uuids = new ArrayList<>();

        DomainsApi domainApi = this.collibraCoreApi.buildClient(DomainsApi.class);
        uuids.add(UUID.fromString(appConfig.getAtscaleBusinessAssetDomainId()));
        uuids.add(UUID.fromString(appConfig.getAtscaleDataAssetDomainId()));

        try {
            domainApi.removeDomains(uuids);
            LOGGER.info("Removed {} AtScale domain(s) from Collibra platform", uuids.size());
        } catch (Exception e) {
            LOGGER.warn("Error received deleting AtScale domain(s)");
        }
    }

    public boolean domainExists(UUID uuid) throws DataNotFoundException {
        DomainsApi domainApi = this.collibraCoreApi.buildClient(DomainsApi.class);
        try {
            domainApi.getDomain(uuid);
            return true;
        } catch (Exception e) {
            if (e.getMessage().contains("vocNotFoundId")) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public Map<String, String> createTypesMap() {
        Map<String, String> typesMap = new HashMap<>();

        AssetTypesApi assetTypesApi = this.collibraCoreApi.buildClient(AssetTypesApi.class);
        AssetTypesApi.FindAssetTypesQueryParams params = new AssetTypesApi.FindAssetTypesQueryParams();
        params.limit(100000);

        AssetTypePagedResponse apr = assetTypesApi.findAssetTypes(params);
        if (apr.getResults() != null) {
            for (AssetTypeImpl assetType : apr.getResults()) {
                typesMap.put(assetType.getId().toString(), assetType.getName());
            }
        }

        DomainsApi domainsApi = this.collibraCoreApi.buildClient(DomainsApi.class);
        DomainsApi.FindDomainsQueryParams domainParams = new DomainsApi.FindDomainsQueryParams();
        domainParams.limit(1000);
        DomainPagedResponse domainAPR = domainsApi.findDomains(domainParams);
        if (domainAPR.getResults() != null) {
            for (DomainImpl domain : domainAPR.getResults()) {
                typesMap.put(domain.getId().toString(), domain.getName());
            }
        }

        RelationTypesApi relationTypesApi = this.collibraCoreApi.buildClient(RelationTypesApi.class);
        RelationTypesApi.FindRelationTypesQueryParams relationParams = new RelationTypesApi.FindRelationTypesQueryParams();
        relationParams.limit(100000);

        RelationTypePagedResponse relationAPR = relationTypesApi.findRelationTypes(relationParams);
        if (relationAPR.getResults() != null) {
            for (RelationTypeImpl relationType : relationAPR.getResults()) {
                typesMap.put(relationType.getId().toString(), relationType.getSourceType().getName() + " " + relationType.getRole() + " " + relationType.getTargetType().getName());
            }
        }

        return typesMap;
    }

    public Map<String, AssetImpl> createObjectMap(Map<String, AssetImpl> objectMap, UUID domainUUID, String domainName) {
        AssetsApi assetsApi = this.collibraCoreApi.buildClient(AssetsApi.class);
        AssetsApi.FindAssetsQueryParams params = new AssetsApi.FindAssetsQueryParams();
        params.domainId(domainUUID);
        params.limit(100000);
        int initSize = objectMap.size();

        AssetPagedResponse apr = assetsApi.findAssets(params);
        if (apr.getResults() != null) {
            for (AssetImpl asset : apr.getResults()) {
                objectMap.put(asset.getName(), asset);
            }
        }
        LOGGER.info("Added {} AtScale assets to objectMap for domain: {}", (objectMap.size() - initSize), domainName);
        return objectMap;
    }

    // Only want to map tables that are used otherwise map could get too large
    public Map<String, String> createPhysicalMap(ApplicationConfig appConfig, List<Dataset> allDatasets) {
        Map<String, String> physicalMap = new HashMap<>();

        LOGGER.info("Creating a map of physical objects for linking");

        AssetsApi assetsApi = this.collibraCoreApi.buildClient(AssetsApi.class);

        // Make a set of db.schema.table and db.schema for quick lookup
        Set<String> datasetsSet = new HashSet<>();
        for (Dataset ds : allDatasets) {
            if (ds.getSchema() != null && ds.getSchema().length() > 0) {
                datasetsSet.add(
                        (Tools.hasStringValue(ds.getDatabase()) ? ds.getDatabase() + Constants.ASSET_NAME_SEPARATOR + ds.getSchema() : ds.getSchema()));
                datasetsSet.add(AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                        (Tools.hasStringValue(ds.getDatabase()) ? ds.getDatabase() + Constants.ASSET_NAME_SEPARATOR + ds.getSchema() : ds.getSchema()),
                        ds.getTable()))));
            }
        }

        RelationsApi relationsApi = this.collibraCoreApi.buildClient(RelationsApi.class);
        RelationsApi.FindRelationsQueryParams relationsParams = new RelationsApi.FindRelationsQueryParams();

        AssetsApi.FindAssetsQueryParams assetsParams = new AssetsApi.FindAssetsQueryParams();
        assetsParams.typeId(Stream.of(UUID.fromString("00000000-0000-0000-0000-000000000000")).collect(Collectors.toList())); // Database type
        AssetPagedResponse apr = assetsApi.findAssets(assetsParams);

        if (apr.getResults() != null) {
            for (AssetImpl dbAsset : apr.getResults()) {
                relationsParams.sourceId(dbAsset.getId());
                RelationPagedResponse relationAPR = relationsApi.findRelations(relationsParams);
                if (relationAPR.getResults() != null) {
                    for (RelationImpl relation : relationAPR.getResults()) {
                        // Add <db> > <schema>  as well as just <schema>
                        if (relation.getTarget() != null) {
                            // Now get the schema's domain
                            AssetsApi.FindAssetsQueryParams assetParams = new AssetsApi.FindAssetsQueryParams();
                            assetParams.name(relation.getTarget().getName());
                            AssetPagedResponse schemaAPR = assetsApi.findAssets(assetParams);
                            Integer count = 0;
                            for (AssetImpl schema : schemaAPR.getResults()) {
                                if (schema.getId().equals(relation.getTarget().getId())) {
                                    physicalMap.put(dbAsset.getName() + Constants.ASSET_NAME_SEPARATOR + relation.getTarget().getName(), schema.getDomain().getId().toString());
                                    physicalMap.put(relation.getTarget().getName(), schema.getDomain().getId().toString());
                                    physicalMap.put(schema.getDomain().getId().toString(), dbAsset.getName() + Constants.ASSET_NAME_SEPARATOR + relation.getTarget().getName());

                                    // Within the schema domain find all tables used in our model and add tables and columns
                                    AssetsApi.FindAssetsQueryParams tableParams = new AssetsApi.FindAssetsQueryParams();
                                    tableParams.domainId(schema.getDomain().getId()); //
                                    AssetPagedResponse tablesAPR = assetsApi.findAssets(tableParams);
                                    boolean foundTable = false;
                                    boolean foundColumn = false;

                                    for (AssetImpl tableOrCol : tablesAPR.getResults()) {
                                        String assetID = AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                                                (dbAsset != null ? dbAsset.getName() + Constants.ASSET_NAME_SEPARATOR + schema.getName() : schema.getName()),
                                                tableOrCol.getName())));

                                        if (!foundTable && tableOrCol.getType().getName().equals("Table") && datasetsSet.contains(assetID)) {
                                            LOGGER.info("Example Table asset: {} ({})", assetID, tableOrCol.getId());
                                            foundTable = true;
                                        }
                                        if (!foundColumn && tableOrCol.getType().getName().equals(Constants.COLUMN) && datasetsSet.contains(removeColumn(assetID, true))) {
                                            LOGGER.info("Example Column asset: {} ({})", assetID, tableOrCol.getId());
                                            foundColumn = true;
                                        }

                                        if ((tableOrCol.getType().getName().equals("Table") && datasetsSet.contains(assetID)) ||
                                                (tableOrCol.getType().getName().equals(Constants.COLUMN) && datasetsSet.contains(removeColumn(assetID, true)))) {
                                            // Only field that has the table is the unique name for the column, and need to combine with
                                            // the schema for physicalMap
                                            physicalMap.put(assetID, tableOrCol.getName());
                                            count++;
                                        }
                                    }
                                }
                            }
                            if (count > 0)
                                LOGGER.info("Added {} assets to physical map for schema: {}", count, relation.getTarget().getName());
                        }
                    }
                } else LOGGER.info("No related schemas found for database: {}", dbAsset.getName());
            }
        } else LOGGER.warn("No assets of type database (00000000-0000-0000-0000-000000031006) found");

        return physicalMap;
    }

    public String removeColumn(String assetID, Boolean hasDatabase) { // TODO: Remove hard-coded true in calls
        if ((assetID.split(" > ").length == 4)
                || (assetID.split(" > ").length == 3 && !hasDatabase))
            return assetID.substring(0, assetID.lastIndexOf(" > "));
        return assetID;
    }

    // Add all assets from all organizations and projects then remove those from current import
    public List<CollibraAsset> getAssetsToMarkAsObsolete(
            List<CollibraAsset> importedAssets,
            String orgName,
            String instance,
            Map<String, Project> allProjects,
            Map<String, AssetImpl> prevObjectsMap,
            ApplicationConfig appConfig,
            List<CollibraDomain> domains) {
        List<CollibraAsset> obsoleteAssets = new ArrayList<>();
        Map<String, Boolean> importedAssetsMap = new HashMap<>();
        Integer newAssetsCount = 0;

        // Create a map of updated assets for fast lookups
        importedAssetsMap = importedAssets.stream()
                .collect(Collectors.toMap(CollibraAsset::getName, asset -> true));

        AssetsApi assetsApi = this.collibraCoreApi.buildClient(AssetsApi.class);
        AssetsApi.FindAssetsQueryParams params = new AssetsApi.FindAssetsQueryParams();

        // Logic for business domain includes org, while data domain does not
        params.domainId(UUID.fromString(appConfig.getAtscaleBusinessAssetDomainId()));

        AssetPagedResponse apr = assetsApi.findAssets(params);

        if (apr.getResults() != null) {
            for (AssetImpl asset : apr.getResults()) {
                // Unique name starts with instance, org and a filtered project
                if (!asset.getStatus().getName().toLowerCase(Locale.ROOT).equals(OBSOLETE) // not already obsolete
                        // In current instance, org and projects
                        && (assetStartsWithInstanceOrgProject(asset.getName(), instance, orgName, allProjects)
                        // Not in current import
                        && !importedAssetsMap.containsKey(asset.getName()))
                        // Not the instance or org object
                        && !asset.getName().equals(AssetNames.prepareAssetName(Arrays.asList(instance, orgName)))
                        && !asset.getName().equals(AssetNames.prepareAssetName(Arrays.asList(instance)))) {
                    obsoleteAssets.add(new CollibraAsset().getBuilder()
                            .name(asset.getName())
                            .domainId(asset.getDomain().getId().toString())
                            .status(CollibraConstants.Status.OBSOLETE)
                            .build());
                }
            }
        }
        int count = obsoleteAssets.size();
        if (count > 0) {
            domains.add(new CollibraDomain.Builder().id(appConfig.getAtscaleBusinessAssetDomainId()).build());
            LOGGER.info("Setting {} asset(s) to status obsolete in the business domain:", count);
            obsoleteAssets.forEach(a -> LOGGER.info("   {}", a.getName()));
        }

        // Now add data domain. Doesn't include project.
        params.domainId(UUID.fromString(appConfig.getAtscaleDataAssetDomainId()));

        apr = assetsApi.findAssets(params);
        if (apr.getResults() != null) {
            for (AssetImpl asset : apr.getResults()) {
                if (!asset.getStatus().getName().toLowerCase(Locale.ROOT).equals(OBSOLETE) // not already obsolete
                        // Unique name starts with instance, org and project
                        && (assetStartsWithInstanceOrgProject(asset.getName(), instance, orgName, allProjects))
                        // Not in current import
                        && !importedAssetsMap.containsKey(asset.getName())) {
                    obsoleteAssets.add(new CollibraAsset().getBuilder()
                            .name(asset.getName())
                            .domainId(asset.getDomain().getId().toString())
                            .status(CollibraConstants.Status.OBSOLETE)
                            .build());
                }
            }
        }

        // Count # assets added
        for (CollibraAsset asset : importedAssets) {
            if (!prevObjectsMap.containsKey(asset.getName())) {
                newAssetsCount++;
            }
        }
        LOGGER.info("Added {} new AtScale asset(s)", newAssetsCount);

        if (obsoleteAssets.size() > count) {
            domains.add(new CollibraDomain.Builder().id(appConfig.getAtscaleDataAssetDomainId()).build());

            Integer dataSize = obsoleteAssets.size() - count;
            LOGGER.info("Setting {} asset(s) to status obsolete in the data domain:", dataSize);
            for (int i = count; i < obsoleteAssets.size(); i++) {
                LOGGER.info("   {}", obsoleteAssets.get(i).getName());
            }
        }

        if (obsoleteAssets.isEmpty()) {
            LOGGER.info("No assets being set to obsolete");
        }
        return obsoleteAssets;
    }

    public boolean assetStartsWithInstanceOrgProject(String assetName, String instance, String orgName, Map<String, Project> allProjects) {
        for (String projectName : allProjects.keySet()) {
            if (assetName.startsWith(AssetNames.prepareAssetName(new ArrayList<>(Arrays.asList(
                    instance, orgName, projectName))))) {
                return true;
            }
        }
        return false;
    }

}
