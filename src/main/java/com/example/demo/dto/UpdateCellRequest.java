package com.example.demo.dto;

public class UpdateCellRequest {
    private int rowIndex;
    private String field;
    private String value;
    
    public UpdateCellRequest() {}
    
    public UpdateCellRequest(int rowIndex, String field, String value) {
        this.rowIndex = rowIndex;
        this.field = field;
        this.value = value;
    }
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}

