package com.example.demo.dto;

public class UpdateRowRequest {
    private int rowIndex;
    private String eventName;
    private String eventCategory;
    private String eventDescription;
    private String eventLocation;
    private String propertyGroup;
    private String propertyLabel;
    private String propertyName;
    private String propertyDefinition;
    private String dataType;
    private String possibleValues;
    
    public UpdateRowRequest() {}
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getEventCategory() {
        return eventCategory;
    }
    
    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }
    
    public String getEventDescription() {
        return eventDescription;
    }
    
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }
    
    public String getEventLocation() {
        return eventLocation;
    }
    
    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }
    
    public String getPropertyGroup() {
        return propertyGroup;
    }
    
    public void setPropertyGroup(String propertyGroup) {
        this.propertyGroup = propertyGroup;
    }
    
    public String getPropertyLabel() {
        return propertyLabel;
    }
    
    public void setPropertyLabel(String propertyLabel) {
        this.propertyLabel = propertyLabel;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    
    public String getPropertyDefinition() {
        return propertyDefinition;
    }
    
    public void setPropertyDefinition(String propertyDefinition) {
        this.propertyDefinition = propertyDefinition;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public String getPossibleValues() {
        return possibleValues;
    }
    
    public void setPossibleValues(String possibleValues) {
        this.possibleValues = possibleValues;
    }
}

