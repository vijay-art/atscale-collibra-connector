package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

  private Integer rowId;
  private Integer tableNumber;
  private String importDate;
  private String name;
  private String catalogGUID;
  private String description;
  private String role;
  private String lastModified;
  private Integer compatibilityLevel;
  private Integer type;
  private Integer version;
  private String databaseId;
  private String dateQueried;
  private Boolean isCurrentlyUsed;
  private Integer popularity;
}
