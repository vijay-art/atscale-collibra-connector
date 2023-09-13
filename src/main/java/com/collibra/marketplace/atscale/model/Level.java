package com.collibra.marketplace.atscale.model;

import com.collibra.marketplace.atscale.util.Tools;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Level {

    private static final Logger LOGGER = LoggerFactory.getLogger(Level.class);

    private Integer tableNumber;
    private String importDate;
    private String catalogName;
    private Integer rowId;
    private String schemaName;
    private String cubeName;
    private String cubeGUID;
    private String datasetName;
    private String dimensionUniqueName;
    private String hierarchyUniqueName;
    private String levelName;
    private Integer levelNumber;
    private String levelUniqueName;
    private String levelGUID;
    private String levelCaption;
    private String description;
    private Boolean isVisible;
    private String nameColumn;
    private String keyColumns;
    private String sortColumn;
    private String nameDataType;
    private String sortDataType;
    private Boolean isPrimary;
    private String parentLevelGUID;

    public Level getAssociatedLevel(Collection<Level> levels, Set<String> cubesWithInvisibleLevels) {
        if (Tools.isEmpty(this.getParentLevelGUID())) {
            return null;
        }
        for (Level level : levels) {
            if (this.getParentLevelGUID().equals(level.getLevelGUID())
                    && this.getCatalogName().equals(level.getCatalogName())
                    && this.getCubeName().equals(level.getCubeName())
                    && this.getDimensionUniqueName().equals(level.getDimensionUniqueName())
            ) {
                return level;
            }
        }
        cubesWithInvisibleLevels.add(this.getCatalogName() + " > " + this.getCubeName());
        return null; // If level attribute is invisible, won't be included dmv results
    }

    public Boolean isCalculatedCol(Collection<Column> columns, String colName) {
        for (Column column : columns) {
            if (column.getColumnName().equals(colName)) {
                return !(column.getExpression().isEmpty());
            }
        }
        return false;
    }

    public Set<String> getUniqueCalcColumns(String project, Collection<Column> columns) {
        Set<String> colSet = new HashSet<>();

        for (Column column : columns) {
            if (column.getCatalogName().equals(project) && column.getColumnName().equals(this.nameColumn) && !column.getExpression().isEmpty()) {
                colSet.add(this.nameColumn);
            }
            if (!this.sortColumn.isEmpty() && column.getColumnName().equals(this.sortColumn)) {
                if (!column.getExpression().isEmpty()) {
                    colSet.add(this.sortColumn);
                }
                break;
            }
            for (String kc : this.keyColumns.split(",")) {
                if (!kc.isEmpty() && kc.equals(column.getColumnName())) {
                    if (!column.getExpression().isEmpty()) {
                        colSet.add(kc);
                    }
                    break;
                }
            }
        }
        if (this.nameColumn.contains(",")) {
            return Collections.emptySet();
        }
        return colSet;
    }

    public Set<String> getUniquePhysicalColumns(String project, Collection<Column> columns) {
        Set<String> colSet = new HashSet<>();

        for (Column column : columns) {
            if (column.getCatalogName().equals(project)) {
                if (column.getColumnName().equals(this.nameColumn) && column.getExpression().isEmpty()) {
                    colSet.add(this.nameColumn);
                }
                if (!this.sortColumn.isEmpty() && column.getColumnName().equals(this.sortColumn)) {
                    if (column.getExpression().isEmpty()) {
                        colSet.add(this.sortColumn);
                    }
                    break;
                }
                for (String kc : this.keyColumns.split(",")) {
                    if (!kc.isEmpty() && kc.equals(column.getColumnName())) {
                        if (column.getExpression().isEmpty()) {
                            colSet.add(kc);
                        }
                        break;
                    }
                }
            }
        }
        if (this.nameColumn.contains(",")) {
            return Collections.emptySet();
        }
        return colSet;
    }
}

