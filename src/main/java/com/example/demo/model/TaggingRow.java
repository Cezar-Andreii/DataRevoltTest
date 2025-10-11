package com.example.demo.model;

public class TaggingRow {
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
    
    public TaggingRow() {}
    
    public TaggingRow(String eventName, String eventCategory, String eventDescription, String eventLocation,
                     String propertyGroup, String propertyLabel, String propertyName, String propertyDefinition,
                     String dataType, String possibleValues) {
        this.eventName = eventName;
        this.eventCategory = eventCategory;
        this.eventDescription = eventDescription;
        this.eventLocation = eventLocation;
        this.propertyGroup = propertyGroup;
        this.propertyLabel = propertyLabel;
        this.propertyName = propertyName;
        this.propertyDefinition = propertyDefinition;
        this.dataType = dataType;
        this.possibleValues = possibleValues;
    }
    
    // Getters and Setters
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
