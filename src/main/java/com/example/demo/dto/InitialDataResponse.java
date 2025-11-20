package com.example.demo.dto;

import com.example.demo.model.Event;
import com.example.demo.model.Parameter;
import com.example.demo.model.TaggingRow;
import java.util.List;

public class InitialDataResponse {
    private List<Event> events;
    private List<Parameter> parameters;
    private List<TaggingRow> taggingRows;
    
    public InitialDataResponse() {}
    
    public InitialDataResponse(List<Event> events, List<Parameter> parameters, List<TaggingRow> taggingRows) {
        this.events = events;
        this.parameters = parameters;
        this.taggingRows = taggingRows;
    }
    
    public List<Event> getEvents() {
        return events;
    }
    
    public void setEvents(List<Event> events) {
        this.events = events;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
    
    public List<TaggingRow> getTaggingRows() {
        return taggingRows;
    }
    
    public void setTaggingRows(List<TaggingRow> taggingRows) {
        this.taggingRows = taggingRows;
    }
}

