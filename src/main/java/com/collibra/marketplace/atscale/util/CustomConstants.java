/*
 * (c) 2022 Collibra Inc. This software is protected under international copyright law.
 * You may only install and use this software subject to the license agreement available at https://marketplace.collibra.com/binary-code-license-agreement/.
 * If such an agreement is not in place, you may not use the software.
 */
package com.collibra.marketplace.atscale.util;

import com.collibra.marketplace.library.integration.interfaces.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

public enum CustomConstants {

    ;

    @AllArgsConstructor
    public enum AssetType implements CollibraAssetTypeInterface {

        ATSCALE_INSTANCE("AtScale Instance", "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "An AtScale instance is an installation of an AtScale engine"),
        ATSCALE_ORGANIZATION(Constants.AT_SCALE_ORGANIZATION, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "An AtScale instance can have 1 or more organizations, each with their own settings"),
        ATSCALE_PROJECT(Constants.AT_SCALE_PROJECT, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "The highest level of an AtScale model that contains cubes, dimensions, and datasets"),
        ATSCALE_CUBE(Constants.AT_SCALE_CUBE, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A cube contains relationships, measures, and dimensions. BI users connect to a cube"),
        ATSCALE_DIMENSION(Constants.AT_SCALE_DIMENSION, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                "A dimension is made up of hierarchies. It contains a subject area like products or customers"),
        ATSCALE_HIERARCHY(Constants.AT_SCALE_HIERARCHY, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A hierarchy is a specific roll-up of levels within a dimension"),
        ATSCALE_LEVEL(Constants.AT_SCALE_LEVEL, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A level is a dimension attribute that uniquely rolls up in a hierarchy"),
        ATSCALE_SECONDARY(Constants.AT_SCALE_SECONDARY_ATTRIBUTE, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A secondary attribute hangs off a level and holds a property of the level"),
        ATSCALE_MEASURE(Constants.AT_SCALE_MEASURE, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                "A measure is an aggregated value such as a sum or average. It can be an MDX calculation or a base measure"),
        ATSCALE_FOLDER(Constants.AT_SCALE_FOLDER, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A grouping of measures"),
        ATSCALE_CONNECTION_GROUP(Constants.AT_SCALE_CONNECTION_GROUP, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "Group of connections on a single platform used by a dataset"),
        ATSCALE_CONNECTION("AtScale Connection", "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A connection to a database"),
        ATSCALE_DATASET(Constants.AT_SCALE_DATASET, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A dataset is a logical representation of a table or view"),
        ATSCALE_COLUMN(Constants.AT_SCALE_COLUMN, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", "A logical column on a logical dataset (calculated column or column on a query dataset");

        @Getter
        private final String name;

        @Getter
        private final String id;

        @Getter
        private final String parentID;

        @Getter
        private final String description;
    }

    @AllArgsConstructor
    public enum AttributeType implements CollibraAttributeTypeInterface {

        // TODO: Add the custom attribute types here as required.
        IDENTIFIER("Identifier", "00000000-0000-0000-0000-000000000000", "GUID for object", "Text");

        @Getter
        private final String name;

        @Getter
        private final String id;

        @Getter
        private final String description;

        @Getter
        private final String kind;

    }

    @AllArgsConstructor
    public enum RelationType implements CollibraRelationTypeInterface {

        ATSCALE_LEVEL_CONTAINS_SECONDARY("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_LEVEL, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_SECONDARY_ATTRIBUTE,""),
        ATSCALE_HIERARCHY_CONTAINS_LEVEL("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_HIERARCHY, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_LEVEL,""),
        ATSCALE_FOLDER_GROUPS_HIERARCHY("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_FOLDER, Constants.GROUPS, Constants.IS_GROUPED_BY,Constants.AT_SCALE_HIERARCHY,""),
        ATSCALE_FOLDER_GROUPS_SECONDARY("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_FOLDER, Constants.GROUPS, Constants.IS_GROUPED_BY,Constants.AT_SCALE_SECONDARY_ATTRIBUTE,""),
        ATSCALE_DIMENSION_CONTAINS_HIERARCHY("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_DIMENSION, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_HIERARCHY,""),
        ATSCALE_CUBE_CONTAINS_DIMENSION("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_CUBE, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_DIMENSION,""),
        ATSCALE_CUBE_CONTAINS_MEASURE("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_CUBE, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_MEASURE,""),
        ATSCALE_CUBE_CONTAINS_FOLDER("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_CUBE, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_FOLDER,""),
        ATSCALE_PROJECT_CONTAINS_CUBE("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_PROJECT, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_CUBE,""),
        ATSCALE_ORG_CONTAINS_PROJECT("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_ORGANIZATION, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_PROJECT,""),
        ATSCALE_INSTANCE_CONTAINS_ORG("00000000-0000-0000-0000-000000000000", "AtScale Instance", Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_ORGANIZATION,""),

        ATSCALE_DATASET_CONTAINS_ATSCALE_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_DATASET, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_COLUMN,""),
        // Case where a QDS has a calculated column defined on it
        ATSCALE_COLUMN_USES_ATSCALE_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_COLUMN, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_COLUMN,""), // Calculated column, from CALC_DEPENDENCY
        ATSCALE_MEASURE_USES_LEVEL("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_MEASURE, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_LEVEL,""), // Calculated measures, from CALC_DEPENDENCY, missing in DMV
        ATSCALE_MEASURE_USES_SECONDARY("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_MEASURE, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_SECONDARY_ATTRIBUTE,""), // Calculated measures, from CALC_DEPENDENCY, missing in DMV
        ATSCALE_MEASURE_USES_MEASURE("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_MEASURE, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_MEASURE,""), // Calculated measures, from CALC_DEPENDENCY
        ATSCALE_FOLDER_GROUPS_MEASURE("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_FOLDER, Constants.GROUPS, Constants.IS_GROUPED_BY,Constants.AT_SCALE_MEASURE,""),

        ATSCALE_LEVEL_USES_ATSCALE_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_LEVEL, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_COLUMN,""),
        ATSCALE_SECONDARY_USES_ATSCALE_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_SECONDARY_ATTRIBUTE, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_COLUMN,""),
        ATSCALE_MEASURE_USES_ATSCALE_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_MEASURE, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_COLUMN,""),

        ATSCALE_LEVEL_USES_PHYSICAL_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_LEVEL, "uses", Constants.IS_USED_BY,Constants.COLUMN,""),
        ATSCALE_SECONDARY_USES_PHYSICAL_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_SECONDARY_ATTRIBUTE, "uses", Constants.IS_USED_BY,Constants.COLUMN,""),
        ATSCALE_COLUMN_USES_PHYSICAL_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_COLUMN, "uses", Constants.IS_USED_BY,Constants.COLUMN,""),
        ATSCALE_MEASURE_USES_PHYSICAL_COLUMN("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_MEASURE, "uses", Constants.IS_USED_BY,Constants.COLUMN,""),
        ATSCALE_DATASET_USES_TABLE("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_DATASET, "uses", Constants.IS_USED_BY,"Table",""),
        ATSCALE_DATASET_USES_SCHEMA("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_DATASET, "uses", Constants.IS_USED_BY,"Schema",""),

        ATSCALE_PROJECT_USES_CONNECTIONGROUP("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_PROJECT, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_CONNECTION_GROUP,""),
        ATSCALE_HIERARCHY_USES_DATASET("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_HIERARCHY, "uses", Constants.IS_USED_BY,Constants.AT_SCALE_DATASET,""), // Get from levels
        ATSCALE_CONNECTIONGROUP_CONTAINS_CONNECTION("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_CONNECTION_GROUP, Constants.CONTAINS, Constants.IS_PART_OF,"AtScale Connection",""),
        ATSCALE_CONNECTIONGROUP_CONTAINS_DATASET("00000000-0000-0000-0000-000000000000", Constants.AT_SCALE_CONNECTION_GROUP, Constants.CONTAINS, Constants.IS_PART_OF,Constants.AT_SCALE_DATASET,""),
        ;

        @Getter
        private final String id;

        @Getter
        private final String head;

        @Getter
        private final String role;

        @Getter
        private final String coRole;

        @Getter
        private final String tail;

        @Getter
        private final String description;
    }

    @AllArgsConstructor
    public enum Status implements CollibraStatusInterface {

        // TODO: Add the custom attribute types here as required.

        ;

        @Getter
        private final String name;

        @Getter
        private final String id;

    }

    @AllArgsConstructor
    public enum ComplexRelationType implements CollibraComplexRelationTypeInterface {

        // TODO: Add the custom complex relation types here as required.

        ;

        @Getter
        private final String name;

        @Getter
        private final String id;

    }
}
