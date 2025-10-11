package com.example.demo.model;

public class Parameter {
    private String parameterName;
    private String exampleValue;
    private String parameterDescription;
    private boolean selected;
    
    public Parameter() {}
    
    public Parameter(String parameterName, String exampleValue, String parameterDescription) {
        this.parameterName = parameterName;
        this.exampleValue = exampleValue;
        this.parameterDescription = parameterDescription;
        this.selected = false;
    }
    
    // Getters and Setters
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
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
