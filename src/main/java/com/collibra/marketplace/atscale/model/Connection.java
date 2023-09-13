package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Connection {

    private String connectionGUID;
    private String groupGUID;
    private String connectionName;
    private String host;
    private String port;
    private String user;
    private String connectorType;
    private Boolean kerberosEnabled;
    private String jdbcFlags;
    private String mgtConsoleURL;
    private String database;
    private String queryRoles;

}
