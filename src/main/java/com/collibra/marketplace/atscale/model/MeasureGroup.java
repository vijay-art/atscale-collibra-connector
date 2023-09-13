package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasureGroup {

  private Integer tableNumber;
  private Integer rowId;
  private String importDate;
  private String catalogName;
  private String schemaName;
  private String cubeName;
  private String measureGroupName;
  private String description;
  private Boolean isWriteEnabled;
  private String measureGroupCaption;

  private String sourceDBServerName;
  private String sourceDBInstanceName;
  private Integer sourceDBID;
}
