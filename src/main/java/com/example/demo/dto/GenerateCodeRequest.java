package com.example.demo.dto;

import java.util.List;

public class GenerateCodeRequest {
    private String eventName;
    private List<String> parameters;
    private List<String> items;
    
    public GenerateCodeRequest() {}
    
    public GenerateCodeRequest(String eventName, List<String> parameters, List<String> items) {
        this.eventName = eventName;
        this.parameters = parameters;
        this.items = items;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    
    public List<String> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public void setItems(List<String> items) {
        this.items = items;
    }
}

