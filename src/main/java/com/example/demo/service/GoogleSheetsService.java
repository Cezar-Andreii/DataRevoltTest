package com.example.demo.service;

import com.example.demo.model.TaggingRow;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoogleSheetsService {
    
    /**
     * Generează un URL pentru Google Sheets cu datele din tagging plan
     */
    public String generateGoogleSheetsUrl(List<TaggingRow> taggingRows) {
        if (taggingRows == null || taggingRows.isEmpty()) {
            return "https://docs.google.com/spreadsheets/create";
        }
        
        // Generează CSV data
        String csvData = generateCSVData(taggingRows);
        
        // Creează URL-ul pentru Google Sheets cu datele
        StringBuilder url = new StringBuilder("https://docs.google.com/spreadsheets/create");
        url.append("?usp=sharing");
        
        // Pentru o implementare mai avansată, poți folosi Google Sheets API
        // pentru a crea direct sheet-ul cu datele
        
        return url.toString();
    }
    
    /**
     * Generează CSV data din lista de rânduri
     */
    public String generateCSVData(List<TaggingRow> taggingRows) {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("Event,Event Name,Purpose,Trigger,Parameter Name,Example Value,Parameter Description\n");
        
        // Date
        for (TaggingRow row : taggingRows) {
            csv.append("\"").append(escapeCSV(row.getEvent())).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventName())).append("\",");
            csv.append("\"").append(escapeCSV(row.getPurpose())).append("\",");
            csv.append("\"").append(escapeCSV(row.getTrigger())).append("\",");
            csv.append("\"").append(escapeCSV(row.getParameterName())).append("\",");
            csv.append("\"").append(escapeCSV(row.getExampleValue())).append("\",");
            csv.append("\"").append(escapeCSV(row.getParameterDescription())).append("\"\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Generează un link de import pentru Google Sheets
     */
    public String generateImportUrl(List<TaggingRow> taggingRows) {
        String csvData = generateCSVData(taggingRows);
        
        // Encode CSV data pentru URL
        String encodedData = java.net.URLEncoder.encode(csvData, java.nio.charset.StandardCharsets.UTF_8);
        
        // Generează URL-ul pentru import
        return "https://docs.google.com/spreadsheets/create?usp=sharing&usp=import&data=" + encodedData;
    }
    
    /**
     * Escapă caracterele speciale pentru CSV
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
