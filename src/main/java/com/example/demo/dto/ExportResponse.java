package com.example.demo.dto;

public class ExportResponse {
    private String url;
    private String message;
    private boolean success;
    
    public ExportResponse() {}
    
    public ExportResponse(boolean success, String message, String url) {
        this.success = success;
        this.message = message;
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}

