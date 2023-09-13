package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cube {

  private Integer rowId;
  private Integer tableNumber;
  private String importDate;
  private String catalogName;
  private String schemaName;
  private String cubeName;
  private String cubeType;
  private String guid;
  private String createdOn;
  private String lastSchemaUpdate;
  private String lastSchemaUpdatedBy;
  private String schemaUpdatedBy;
  private String lastDataUpdated;
  private String dataUpdatedBy;
  private String description;
  private String cubeCaption;
  private String baseCubeName;
  private Integer cubeSource;
  private String preferredQueryPatterns;
  private Boolean isDrillThroughEnabled;
  private Boolean isLinkable;
  private Boolean isWriteEnabled;
  private Boolean isSQLEnabled;
}
