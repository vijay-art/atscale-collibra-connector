package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hierarchy {

  private Integer tableNumber;
  private String importDate;
  private String catalogName;
  private Integer rowId;
  private String schemaName;
  private String cubeName;
  private String cubeGUID;

  private String dimensionUniqueName;
  private String hierarchyName;
  private String hierarchyUniqueName;
  private String hierarchyGUID;
  private String hierarchyCaption;
  private Integer dimensionType;
  private Integer hierarchyCardinality;
  private String defaultMember;
  private String allMember;
  private String description;
  private Integer structure;
  private Boolean isVirtual;
  private Boolean isReadWrite;
  private Boolean dimensionIsVisible;
  private Integer dimensionUniqueSettings;
  private String dimensionMasterName;
  private Integer hierarchyOrigin;
  private String hierarchyDisplayFolder;
  private Integer instanceSelection;
  private Integer groupingBehaviour;
  private String structureType;
  private Boolean dimensionIsShared;
  private Boolean hierarchyIsVisible;
  private Integer hierarchyOrdinal;
}
