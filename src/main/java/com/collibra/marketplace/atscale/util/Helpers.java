package com.collibra.marketplace.atscale.util;

public class Helpers {

    /***
     * utility class pattern
     * The constructor is made private to prevent the class from being instantiated from outside the class.
     */
    private Helpers() {
        throw new IllegalStateException("Helpers utility");
    }

    public static String typeIdFromRelationUUID(String key) {
        if (key.length() != 43) {
            return "";
        }
        String uuid = key.substring(0, 36);
        String relType = key.substring(37);

        if (CustomConstants.RelationType.ATSCALE_LEVEL_CONTAINS_SECONDARY.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_LEVEL.getId() : CustomConstants.AssetType.ATSCALE_SECONDARY.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_HIERARCHY_CONTAINS_LEVEL.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_HIERARCHY.getId() : CustomConstants.AssetType.ATSCALE_LEVEL.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_FOLDER_GROUPS_HIERARCHY.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_FOLDER.getId() : CustomConstants.AssetType.ATSCALE_HIERARCHY.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_FOLDER_GROUPS_SECONDARY.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_FOLDER.getId() : CustomConstants.AssetType.ATSCALE_SECONDARY.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_DIMENSION_CONTAINS_HIERARCHY.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_DIMENSION.getId() : CustomConstants.AssetType.ATSCALE_HIERARCHY.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_CUBE_CONTAINS_DIMENSION.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_CUBE.getId() : CustomConstants.AssetType.ATSCALE_DIMENSION.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_CUBE_CONTAINS_MEASURE.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_CUBE.getId() : CustomConstants.AssetType.ATSCALE_MEASURE.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_CUBE_CONTAINS_FOLDER.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_CUBE.getId() : CustomConstants.AssetType.ATSCALE_FOLDER.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_PROJECT_CONTAINS_CUBE.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_PROJECT.getId() : CustomConstants.AssetType.ATSCALE_CUBE.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_ORG_CONTAINS_PROJECT.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_ORGANIZATION.getId() : CustomConstants.AssetType.ATSCALE_PROJECT.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_INSTANCE_CONTAINS_ORG.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_INSTANCE.getId() : CustomConstants.AssetType.ATSCALE_ORGANIZATION.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_DATASET_CONTAINS_ATSCALE_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_DATASET.getId() : CustomConstants.AssetType.ATSCALE_COLUMN.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_COLUMN_USES_ATSCALE_COLUMN.getId().equals(uuid)) {
            return CustomConstants.AssetType.ATSCALE_COLUMN.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_MEASURE_USES_LEVEL.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_MEASURE.getId() : CustomConstants.AssetType.ATSCALE_LEVEL.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_MEASURE_USES_SECONDARY.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_MEASURE.getId() : CustomConstants.AssetType.ATSCALE_SECONDARY.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_MEASURE_USES_MEASURE.getId().equals(uuid)) {
            return CustomConstants.AssetType.ATSCALE_MEASURE.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_FOLDER_GROUPS_MEASURE.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_FOLDER.getId() : CustomConstants.AssetType.ATSCALE_MEASURE.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_LEVEL_USES_ATSCALE_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_LEVEL.getId() : CustomConstants.AssetType.ATSCALE_COLUMN.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_SECONDARY_USES_ATSCALE_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_SECONDARY.getId() : CustomConstants.AssetType.ATSCALE_COLUMN.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_MEASURE_USES_ATSCALE_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_MEASURE.getId() : CustomConstants.AssetType.ATSCALE_COLUMN.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_LEVEL_USES_PHYSICAL_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_LEVEL.getId() : "";
        }
        if (CustomConstants.RelationType.ATSCALE_SECONDARY_USES_PHYSICAL_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_SECONDARY.getId() : "";
        }
        if (CustomConstants.RelationType.ATSCALE_COLUMN_USES_PHYSICAL_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_COLUMN.getId() : "";
        }
        if (CustomConstants.RelationType.ATSCALE_MEASURE_USES_PHYSICAL_COLUMN.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_MEASURE.getId() : "";
        }
        if (CustomConstants.RelationType.ATSCALE_DATASET_USES_TABLE.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_DATASET.getId() : "";
        }
        if (CustomConstants.RelationType.ATSCALE_DATASET_USES_SCHEMA.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_DATASET.getId() : "";
        }
        if (CustomConstants.RelationType.ATSCALE_PROJECT_USES_CONNECTIONGROUP.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_PROJECT.getId() : CustomConstants.AssetType.ATSCALE_CONNECTION_GROUP.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_HIERARCHY_USES_DATASET.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_HIERARCHY.getId() : CustomConstants.AssetType.ATSCALE_DATASET.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_CONNECTIONGROUP_CONTAINS_CONNECTION.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_CONNECTION_GROUP.getId() : CustomConstants.AssetType.ATSCALE_CONNECTION.getId();
        }
        if (CustomConstants.RelationType.ATSCALE_CONNECTIONGROUP_CONTAINS_DATASET.getId().equals(uuid)) {
            return relType.equals(Constants.SOURCE) ? CustomConstants.AssetType.ATSCALE_CONNECTION_GROUP.getId() : CustomConstants.AssetType.ATSCALE_DATASET.getId();
        }
        return "";
    }
}
