package com.collibra.marketplace.atscale.api;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class SOAPResultSet extends AbstractResultSet {

    public class Field {
        private String fieldName;
        private String fieldType;

        public Field(String name, String type) {
            fieldName = name;
            fieldType = type;
        }

        public void setFieldName(String name) {
            fieldName = name;
        }

        public void setFieldType(String type) {
            fieldType = type;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getFieldType() {
            return fieldType;
        }
    }

    int currentRow = 0;
    List<Field> dataCols = new ArrayList<>();
    List<List<String>> dataRows = new ArrayList<>();
    List<String> columnNames = new ArrayList<>();

    public boolean insertColumn(String fieldName, String fieldType) {
        Field colField = new Field(fieldName, fieldType);
        dataCols.add(colField);
        columnNames.add(fieldName);
        return true;
    }

    public boolean insertRow(List<String> row) {
        dataRows.add(row);
        return true;
    }

    @Override
    public int getRow() throws SQLException {
        return currentRow;
    }

    @Override
    public boolean next() throws SQLException {
        if (currentRow < dataRows.size()) {
            currentRow++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean previous() {
        if (currentRow > 0) {
            currentRow--;
            return true;
        }
            return false;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        return val != null && (val.equalsIgnoreCase("true") || val.equals("1"));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        return val != null && (val.equalsIgnoreCase("true") || val.equals("1"));
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return null;
        } else {
            return Date.valueOf(val);
        }
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return null;
        } else {
            return Date.valueOf(val);
        }
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return 0.0;
        } else {
            return Double.parseDouble(val);
        }
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return 0.0;
        } else {
            return Double.parseDouble(val);
        }
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return 0.0f;
        } else {
            return Float.parseFloat(val);
        }
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return 0.0f;
        } else {
            return Float.parseFloat(val);
        }
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(val);
        }
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(val);
        }
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return 0;
        } else {
            return Long.parseLong(val);
        }
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return 0;
        } else {
            return Long.parseLong(val);
        }
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return 0;
        } else {
            return Short.parseShort(val);
        }
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return 0;
        } else {
            return Short.parseShort(val);
        }
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return dataRows.get(currentRow - 1).get(columnIndex);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        int columnIndex = columnNames.indexOf(columnLabel);
        return dataRows.get(currentRow - 1).get(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        String val = getString(columnIndex);
        if (val.isEmpty()) {
            return null;
        } else {
            return Time.valueOf(val);
        }
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        String val = getString(columnLabel);
        if (val.isEmpty()) {
            return null;
        } else {
            return Time.valueOf(val);
        }
    }
}
