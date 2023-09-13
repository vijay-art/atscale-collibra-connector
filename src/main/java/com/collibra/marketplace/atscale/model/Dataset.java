package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {
    // Need database too but not yet populated in DMV query. Get from parsing for now.
    private String catalogName;
    private String cubeGUID;
    private String datasetName;
    private String database;
    private String table;
    private String schema;
    private String expression;
    private String connection;
}
