package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.model.Parameter;
import com.example.demo.model.TaggingRow;
import com.example.demo.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/tagging")
public class TaggingController {
    
    private List<TaggingRow> taggingRows = new ArrayList<>();
    
    @Autowired
    private GoogleSheetsService googleSheetsService;
    
    @GetMapping
    public String taggingPlan(Model model) {
        // Evenimente predefinite
        List<Event> events = Arrays.asList(
            new Event("purchase", "Purchase", "Track completed purchases", "User completes a purchase"),
            new Event("add_to_cart", "Add to Cart", "Track items added to cart", "User adds item to cart"),
            new Event("view_item", "View Item", "Track product views", "User views a product"),
            new Event("search", "Search", "Track search queries", "User performs a search"),
            new Event("login", "Login", "Track user logins", "User logs into account")
        );
        
        // Parametri predefiniți
        List<Parameter> parameters = Arrays.asList(
            new Parameter("transaction_id", "TXN_12345", "Unique transaction identifier"),
            new Parameter("value", "29.99", "Purchase value in currency"),
            new Parameter("currency", "USD", "Currency code"),
            new Parameter("item_id", "PROD_001", "Product identifier"),
            new Parameter("item_name", "Laptop", "Product name"),
            new Parameter("category", "Electronics", "Product category"),
            new Parameter("quantity", "1", "Quantity purchased"),
            new Parameter("user_id", "USER_123", "User identifier"),
            new Parameter("session_id", "SESS_456", "Session identifier"),
            new Parameter("timestamp", "1640995200", "Unix timestamp")
        );
        
        model.addAttribute("events", events);
        model.addAttribute("parameters", parameters);
        model.addAttribute("taggingRows", taggingRows);
        
        return "tagging-plan";
    }
    
    @PostMapping("/generate")
    public String generateTaggingPlan(@RequestParam String selectedEvent, 
                                   @RequestParam(required = false) List<String> selectedParameters,
                                   Model model) {
        // Găsim evenimentul selectat
        Event event = getEventByName(selectedEvent);
        
        if (event != null && selectedParameters != null) {
            // Generăm rânduri pentru fiecare parametru selectat
            for (String paramName : selectedParameters) {
                Parameter param = getParameterByName(paramName);
                if (param != null) {
                    TaggingRow row = new TaggingRow(
                        event.getEvent(),
                        event.getEventName(),
                        event.getPurpose(),
                        event.getTrigger(),
                        param.getParameterName(),
                        param.getExampleValue(),
                        param.getParameterDescription()
                    );
                    taggingRows.add(row);
                }
            }
        }
        
        return "redirect:/tagging";
    }
    
    @PostMapping("/update")
    public String updateTaggingRow(@RequestParam int rowIndex,
                                 @RequestParam String event,
                                 @RequestParam String eventName,
                                 @RequestParam String purpose,
                                 @RequestParam String trigger,
                                 @RequestParam String parameterName,
                                 @RequestParam String exampleValue,
                                 @RequestParam String parameterDescription) {
        
        if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
            TaggingRow row = taggingRows.get(rowIndex);
            row.setEvent(event);
            row.setEventName(eventName);
            row.setPurpose(purpose);
            row.setTrigger(trigger);
            row.setParameterName(parameterName);
            row.setExampleValue(exampleValue);
            row.setParameterDescription(parameterDescription);
        }
        
        return "redirect:/tagging";
    }
    
    @PostMapping("/delete")
    public String deleteTaggingRow(@RequestParam int rowIndex) {
        if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
            taggingRows.remove(rowIndex);
        }
        return "redirect:/tagging";
    }
    
    @PostMapping("/update-cell")
    public String updateCell(@RequestParam int rowIndex, 
                            @RequestParam String field, 
                            @RequestParam String value) {
        if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
            TaggingRow row = taggingRows.get(rowIndex);
            
            switch (field) {
                case "event":
                    row.setEvent(value);
                    break;
                case "eventName":
                    row.setEventName(value);
                    break;
                case "purpose":
                    row.setPurpose(value);
                    break;
                case "trigger":
                    row.setTrigger(value);
                    break;
                case "parameterName":
                    row.setParameterName(value);
                    break;
                case "exampleValue":
                    row.setExampleValue(value);
                    break;
                case "parameterDescription":
                    row.setParameterDescription(value);
                    break;
            }
        }
        
        return "redirect:/tagging";
    }
    
    @GetMapping("/export-google-sheet")
    public String exportToGoogleSheet(Model model) {
        if (taggingRows.isEmpty()) {
            return "redirect:/tagging?error=no-data";
        }
        
        // Generează URL-ul pentru Google Sheets cu datele
        String googleSheetsUrl = googleSheetsService.generateGoogleSheetsUrl(taggingRows);
        
        // Redirecționează către Google Sheets
        return "redirect:" + googleSheetsUrl;
    }
    
    @GetMapping("/export-google-sheet-data")
    public String exportToGoogleSheetWithData(Model model) {
        if (taggingRows.isEmpty()) {
            return "redirect:/tagging?error=no-data";
        }
        
        // Generează CSV data
        String csvData = googleSheetsService.generateCSVData(taggingRows);
        
        // Creează un Google Sheet cu datele
        String googleSheetsUrl = createGoogleSheetWithData(csvData);
        
        // Redirecționează către Google Sheets
        return "redirect:" + googleSheetsUrl;
    }
    
    private String createGoogleSheetWithData(String csvData) {
        // Pentru o implementare completă, ar trebui să folosești Google Sheets API
        // Pentru moment, redirecționez către un Google Sheet nou cu instrucțiuni
        
        // Encode CSV data pentru URL
        String encodedData = java.net.URLEncoder.encode(csvData, java.nio.charset.StandardCharsets.UTF_8);
        
        // Creează URL-ul pentru Google Sheets cu datele
        return "https://docs.google.com/spreadsheets/create?usp=sharing&usp=import&data=" + encodedData;
    }
    
    @GetMapping("/export-csv")
    public ResponseEntity<String> exportToCSV() {
        if (taggingRows.isEmpty()) {
            return ResponseEntity.badRequest().body("No data to export");
        }
        
        String csvData = googleSheetsService.generateCSVData(taggingRows);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tagging-plan.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
    
    private Event getEventByName(String eventName) {
        List<Event> events = Arrays.asList(
            new Event("purchase", "Purchase", "Track completed purchases", "User completes a purchase"),
            new Event("add_to_cart", "Add to Cart", "Track items added to cart", "User adds item to cart"),
            new Event("view_item", "View Item", "Track product views", "User views a product"),
            new Event("search", "Search", "Track search queries", "User performs a search"),
            new Event("login", "Login", "Track user logins", "User logs into account")
        );
        
        return events.stream()
                .filter(e -> e.getEvent().equals(eventName))
                .findFirst()
                .orElse(null);
    }
    
    private Parameter getParameterByName(String paramName) {
        List<Parameter> parameters = Arrays.asList(
            new Parameter("transaction_id", "TXN_12345", "Unique transaction identifier"),
            new Parameter("value", "29.99", "Purchase value in currency"),
            new Parameter("currency", "USD", "Currency code"),
            new Parameter("item_id", "PROD_001", "Product identifier"),
            new Parameter("item_name", "Laptop", "Product name"),
            new Parameter("category", "Electronics", "Product category"),
            new Parameter("quantity", "1", "Quantity purchased"),
            new Parameter("user_id", "USER_123", "User identifier"),
            new Parameter("session_id", "SESS_456", "Session identifier"),
            new Parameter("timestamp", "1640995200", "Unix timestamp")
        );
        
        return parameters.stream()
                .filter(p -> p.getParameterName().equals(paramName))
                .findFirst()
                .orElse(null);
    }
}
