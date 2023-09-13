package com.collibra.marketplace.atscale.model;

import com.collibra.marketplace.atscale.util.Tools;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {
    private String catalogName;
    private String datasetName;
    private String columnName;
    private String dataType;
    private String expression;
    private String connectionID;
    private Dataset dataset;

    @Override
    public int hashCode() {
        return Tools.hashStringToInt(connectionID + "." + catalogName + "." + datasetName + "." + columnName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Column column = (Column) o;
        return Objects.equals(catalogName, column.catalogName) && Objects.equals(datasetName, column.datasetName) && Objects.equals(columnName, column.columnName) && Objects.equals(connectionID, column.connectionID);
    }
}
