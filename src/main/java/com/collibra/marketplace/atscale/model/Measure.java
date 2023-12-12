package com.collibra.marketplace.atscale.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Measure {

  private Integer tableNumber;
  private String importDate;
  private String catalogName;
  private String schemaName;
  private String cubeName;
  private String cubeGUID;
  private String measureName;
  private String measureUniqueName;
  private String measureGUID;
  private Integer rowId;
  private String measureCaption;
  private Integer measureAggregator;
  private String dataType; // For the measure
  private String columnDataType;
  private String columnName;
  private String columnSQL;
  private Integer numericPrecision;
  private Integer numericScale;
  private Integer measureUnits;
  private String description;
  private String expression;
  private boolean isVisible;
  private String levelList;
  private String measureNameSQLColumnName;
  private String measureUnqualifiedCaption;
  private String measureGroupName;
  private String measureDisplayFolder;
  private String defaultFormatString;
  private String datasetName;
  private Dataset dataset;
  private boolean isMetricalAttribute;
  private String parentLevelId;
  private String parentLevelName;
  private List<Attribute> attributeList;

  private static final Logger LOGGER = LoggerFactory.getLogger(Measure.class);
}
