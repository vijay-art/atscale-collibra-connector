package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalcDependencies {

    private String databaseName;
    private String objectType;
    private String table;
    private String object;
    private String expression;
    private String referencedObjectType;
    private String referencedTable;
    private Boolean referencedObject;
    private String referencedExpression;

}
