package com.example.demo.dto;

import java.util.List;

public class GenerateRequest {
    private List<String> selectedEvents;
    private List<String> selectedItems;
    private List<String> selectedPlatforms;
    
    public GenerateRequest() {}
    
    public GenerateRequest(List<String> selectedEvents, List<String> selectedItems) {
        this.selectedEvents = selectedEvents;
        this.selectedItems = selectedItems;
    }
    
    public List<String> getSelectedEvents() {
        return selectedEvents;
    }
    
    public void setSelectedEvents(List<String> selectedEvents) {
        this.selectedEvents = selectedEvents;
    }
    
    public List<String> getSelectedItems() {
        return selectedItems;
    }
    
    public void setSelectedItems(List<String> selectedItems) {
        this.selectedItems = selectedItems;
    }
    
    public List<String> getSelectedPlatforms() {
        return selectedPlatforms;
    }
    
    public void setSelectedPlatforms(List<String> selectedPlatforms) {
        this.selectedPlatforms = selectedPlatforms;
    }
}

