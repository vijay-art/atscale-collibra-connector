package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dimension {

  private Integer tableNumber;
  private Integer rowId;
  private String importDate;
  private String catalogName;
  private String schemaName;
  private String cubeName;
  private String cubeGUID;
  private String dimensionName;
  private String dimensionUniqueName;
  private String dimensionGUID;
  private String dimensionCaption;
  private Integer dimensionOrdinal;
  private Integer type;
  private Integer dimensionCardinality;
  private String defaultHierarchy;
  private String description;
  private Boolean isVirtual;
  private Boolean isReadWrite;
  private Boolean isVisible;
  private Integer dimensionUniqueSettings;
  private String dimensionMasterName;
  private String sourceDBServerName;
  private String sourceDBInstanceName;
  private Integer sourceDBID;
}
