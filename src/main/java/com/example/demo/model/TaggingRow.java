package com.example.demo.model;

public class TaggingRow {
    private String event;
    private String eventName;
    private String purpose;
    private String trigger;
    private String parameterName;
    private String exampleValue;
    private String parameterDescription;
    
    public TaggingRow() {}
    
    public TaggingRow(String event, String eventName, String purpose, String trigger, 
                     String parameterName, String exampleValue, String parameterDescription) {
        this.event = event;
        this.eventName = eventName;
        this.purpose = purpose;
        this.trigger = trigger;
        this.parameterName = parameterName;
        this.exampleValue = exampleValue;
        this.parameterDescription = parameterDescription;
    }
    
    // Getters and Setters
    public String getEvent() {
        return event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public String getTrigger() {
        return trigger;
    }
    
    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
    
    public String getParameterName() {
        return parameterName;
    }
    
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
    
    public String getExampleValue() {
        return exampleValue;
    }
    
    public void setExampleValue(String exampleValue) {
        this.exampleValue = exampleValue;
    }
    
    public String getParameterDescription() {
        return parameterDescription;
    }
    
    public void setParameterDescription(String parameterDescription) {
        this.parameterDescription = parameterDescription;
    }
}
