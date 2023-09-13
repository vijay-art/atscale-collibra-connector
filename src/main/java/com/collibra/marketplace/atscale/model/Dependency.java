package com.collibra.marketplace.atscale.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dependency {
    private String databaseName;
    private String objectType;
    private String table;
    private String object;
    private String expression;
    private String referencedObjectType;
    private String referencedTable;
    private String referencedObject;
    private String referencedExpression;
    private String catalogName;
    private String cubeName;
}
