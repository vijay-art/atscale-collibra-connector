package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionGroup {

    private String connectionGroupGUID;
    private String connectionGroupName;
    private String platformType;
    private String orgID;
    private String connID;
    private String filesystemURI;
    private String filesystemType;
    private String aggsSchema;
    private String database;
    private Boolean isImpersonationEnabled;
    private Boolean isCanaryEnabled;
    private Boolean isPartialAggHitEnabled;
    private Boolean isReadOnly;

}
