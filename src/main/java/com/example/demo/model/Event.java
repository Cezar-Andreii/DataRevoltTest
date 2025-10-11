package com.example.demo.model;

public class Event {
    private String event;
    private String eventName;
    private String purpose;
    private String trigger;
    
    public Event() {}
    
    public Event(String event, String eventName, String purpose, String trigger) {
        this.event = event;
        this.eventName = eventName;
        this.purpose = purpose;
        this.trigger = trigger;
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
}
