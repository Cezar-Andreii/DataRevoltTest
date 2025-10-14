package com.example.demo.service;

import com.example.demo.model.TaggingRow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

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
        csv.append("Event Name,Event Category,Event Description,Event Location,Property Group,Property Label,Property Name,Property Definition,Data Type,Possible Values,Code Examples,DATA LAYER STATUS,STATUS GA4\n");
        
        // Date
        for (TaggingRow row : taggingRows) {
            csv.append("\"").append(escapeCSV(row.getEventName())).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventCategory())).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventDescription())).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventLocation())).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyGroup())).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyLabel())).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyName())).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyDefinition())).append("\",");
            csv.append("\"").append(escapeCSV(row.getDataType())).append("\",");
            csv.append("\"").append(escapeCSV(row.getPossibleValues())).append("\",");
            
            // Pentru rândurile de evenimente, generează codul JavaScript pe o singură linie
            String codeExamples = row.getCodeExamples();
            if (row.getEventName() != null && !row.getEventName().trim().isEmpty() && 
                row.getPropertyName() != null && row.getPropertyName().equals("event")) {
                // Este un rând de eveniment, generează codul pe o singură linie
                List<String> parameters = getParametersForEvent(taggingRows, row.getEventName());
                codeExamples = generateJavaScriptCodeForExport(row.getEventName(), parameters);
            }
            
            csv.append("\"").append(escapeCSV(codeExamples)).append("\",");
            csv.append("\"").append(escapeCSV(row.getDataLayerStatus())).append("\",");
            csv.append("\"").append(escapeCSV(row.getStatusGA4())).append("\"\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Găsește parametrii pentru un eveniment specific
     */
    private List<String> getParametersForEvent(List<TaggingRow> taggingRows, String eventName) {
        List<String> parameters = new ArrayList<>();
        
        for (TaggingRow row : taggingRows) {
            if (row.getEventName() == null || row.getEventName().trim().isEmpty()) {
                // Este un rând de parametru, verifică dacă aparține evenimentului curent
                if (row.getPropertyName() != null && 
                    !row.getPropertyName().trim().isEmpty() &&
                    !row.getPropertyName().equals("event")) {
                    // Verifică dacă acest parametru aparține evenimentului curent
                    if (belongsToEvent(taggingRows, row, eventName)) {
                        parameters.add(row.getPropertyName());
                    }
                }
            }
        }
        
        return parameters;
    }
    
    /**
     * Verifică dacă un parametru aparține unui eveniment specific
     */
    private boolean belongsToEvent(List<TaggingRow> taggingRows, TaggingRow paramRow, String eventName) {
        int paramIndex = taggingRows.indexOf(paramRow);
        if (paramIndex == -1) return false;
        
        // Caută înapoi pentru a găsi evenimentul părinte
        for (int i = paramIndex - 1; i >= 0; i--) {
            TaggingRow row = taggingRows.get(i);
            if (row.getEventName() != null && !row.getEventName().trim().isEmpty()) {
                return row.getEventName().equals(eventName);
            }
        }
        
        return false;
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
     * Generează codul JavaScript pentru export (o singură linie)
     */
    private String generateJavaScriptCodeForExport(String eventName, List<String> selectedParameters) {
        StringBuilder code = new StringBuilder();
        
        // Header standard - pe o singură linie
        code.append("window.dataLayer = window.dataLayer || []; ");
        code.append("dataLayer.push({ 'ecommerce': null }); ");
        code.append("dataLayer.push({ ");
        code.append("'event': '").append(eventName).append("'");
        
        // Adaugă parametrii selectați doar dacă există
        if (selectedParameters != null && !selectedParameters.isEmpty()) {
            code.append(", 'ecommerce': { ");
            
            boolean firstParam = true;
            for (String paramName : selectedParameters) {
                if (!firstParam) {
                    code.append(", ");
                }
                
                switch (paramName) {
                    case "currency":
                        code.append("'currency': $value");
                        break;
                    case "value":
                        code.append("'value': $value");
                        break;
                    case "transaction_id":
                        code.append("'transaction_id': $value");
                        break;
                    case "coupon":
                        code.append("'coupon': $value");
                        break;
                    case "shipping":
                        code.append("'shipping': $value");
                        break;
                    case "tax":
                        code.append("'tax': $value");
                        break;
                    case "affiliation":
                        code.append("'affiliation': $value");
                        break;
                    case "items":
                        code.append("'items': [{ item_object_1 }]");
                        break;
                    default:
                        code.append("'").append(paramName).append("': $value");
                        break;
                }
                firstParam = false;
            }
            
            code.append(" }");
        }
        
        // Footer - pe o singură linie
        code.append(" });");
        
        return code.toString();
    }
    
    /**
     * Escapă caracterele speciale pentru CSV
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        // Înlocuiește newline-urile cu spații pentru a rămâne pe o singură linie în Google Sheets
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }
}
