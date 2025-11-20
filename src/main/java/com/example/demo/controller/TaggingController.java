package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.Event;
import com.example.demo.model.Parameter;
import com.example.demo.model.TaggingRow;
import com.example.demo.service.GoogleSheetsService;
import java.util.ArrayList;
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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

@Controller
@RequestMapping("/tagging")
public class TaggingController {
    
    private List<TaggingRow> taggingRows = new ArrayList<>();
    private List<String> currentSelectedPlatforms = new ArrayList<>(); // Stochează platformele selectate
    
    @Autowired(required = false)
    private GoogleSheetsService googleSheetsService;
    
    @GetMapping
    public String taggingPlan(Model model) {
        // Evenimente predefinite
        List<Event> events = Arrays.asList(
            new Event("view_item_list", "View Item List", "Track when a user sees a list of items or products", "Product listing pages"),
            new Event("view_item", "View Item", "Track product views", "User views a product"),
            new Event("select_item", "Select Item", "Track when a user selects an item from a list", "User selects an item"),
            new Event("add_to_cart", "Add to Cart", "Track items added to cart", "User adds item to cart"),
            new Event("view_cart", "View Cart", "Track when a user views their cart", "Cart page"),
            new Event("add_to_wishlist", "Add to Wishlist", "Track when a user adds items to wishlist", "User adds item to wishlist"),
            new Event("view_promotion", "View Promotion", "Track when a user views a promotion", "Promotion pages"),
            new Event("select_promotion", "Select Promotion", "Track when a user selects a promotion", "User selects a promotion"),
            new Event("begin_checkout", "Begin Checkout", "Track when a user begins the checkout process", "Checkout start"),
            new Event("add_shipping_info", "Add Shipping Info", "Track when a user adds shipping information", "Shipping form"),
            new Event("add_payment_info", "Add Payment Info", "Track when a user adds payment information", "Payment form"),
            new Event("purchase", "Purchase", "An event that contains data about the purchase made.", "Thank you page"),
            new Event("userData", "User Data", "Track user data information", "User data tracking")
        );
        
        // Parametri predefiniți pentru purchase
        List<Parameter> parameters = Arrays.asList(
            new Parameter("event", "purchase", "The event name."),
            new Parameter("currency", "RON", "Currency of the value. Use three letter ISO 4217 format."),
            new Parameter("value", "1439.00", "The revenue of the event."),
            new Parameter("customer_type", "new", "New or returning user from the last 540-day recommended period"),
            new Parameter("transaction_id", "NL-23435342", "The unique identifier of a transaction."),
            new Parameter("coupon", "Summer Sale", "The coupon name/code associated with the event."),
            new Parameter("shipping", "10.34", "Shipping cost associated with a transaction."),
            new Parameter("tax", "15.23", "Tax cost associated with a transaction."),
            new Parameter("items", "[{itemKey: itemValue}]", "A list with the product (or products) in the shopping cart."),
            new Parameter("creative_name", "Summer Banner", "The name of the creative used in the promotion."),
            new Parameter("creative_slot", "summer_1", "The name of the creative slot."),
            new Parameter("promotion_id", "P_12345", "The ID of the promotion."),
            new Parameter("promotion_name", "Summer Sale", "The name of the promotion.")
        );
        
        model.addAttribute("events", events);
        model.addAttribute("parameters", parameters);
        model.addAttribute("taggingRows", taggingRows);
        
        return "tagging-plan";
    }
    
    @PostMapping("/generate")
    public String generateTaggingPlan(@RequestParam(required = false) List<String> selectedEvents,
                                   @RequestParam(required = false) List<String> selectedItems,
                                   Model model) {
        if (selectedEvents != null && !selectedEvents.isEmpty()) {
            for (String eventName : selectedEvents) {
                Event event = getEventByName(eventName);
                if (event != null) {
                    // Verificăm dacă evenimentul există deja în listă
                    boolean eventExists = taggingRows.stream()
                        .anyMatch(row -> row.getEventName().equals(event.getEvent()));
                    
                    // Obținem parametrii oficiali pentru acest eveniment
                    List<String> officialParameters = getOfficialParametersForEvent(eventName);
                    
                    // Dacă evenimentul nu există, adăugăm un rând cu informațiile despre eveniment
                    if (!eventExists) {
                        // Generează codul în funcție de platformă (momentan doar WEB și iOS)
                        String generatedCode = generateJavaScriptCode(event.getEvent(), officialParameters, selectedItems);
                        
                        TaggingRow eventRow = new TaggingRow(
                            event.getEvent(), // eventName
                            "Ecommerce", // eventCategory
                            event.getPurpose(), // eventDescription
                            event.getTrigger(), // eventLocation
                            "Analytics", // propertyGroup
                            "Dimension", // propertyLabel
                            "event", // propertyName
                            "The event name.", // propertyDefinition
                            "String", // dataType
                            event.getEvent(), // possibleValues
                            generatedCode, // codeExamples - codul generat
                            "yes", // dataLayerStatus
                            "yes" // statusGA4
                        );
                        taggingRows.add(eventRow);
                    }
                    
                    // Adăugăm rânduri pentru fiecare parametru oficial
                    boolean parametersAdded = false;
                    for (String paramName : officialParameters) {
                        Parameter param = getParameterByName(paramName);
                        if (param != null) {
                            // Verificăm dacă parametrul există deja pentru acest eveniment
                            boolean paramExists = taggingRows.stream()
                                .anyMatch(row -> row.getEventName().equals(event.getEvent()) && 
                                               row.getPropertyName().equals(param.getParameterName()));
                            
                            if (!paramExists) {
                                TaggingRow paramRow = new TaggingRow(
                                    "", // eventName gol pentru rândul de parametru
                                    "", // eventCategory gol pentru rândul de parametru
                                    "", // eventDescription gol pentru rândul de parametru
                                    "", // eventLocation gol pentru rândul de parametru
                                    "Analytics", // propertyGroup
                                    getPropertyLabel(param.getParameterName()), // propertyLabel
                                    param.getParameterName(), // propertyName
                                    param.getParameterDescription(), // propertyDefinition
                                    getDataType(param.getParameterName()), // dataType
                                    param.getExampleValue(), // possibleValues
                                    "", // codeExamples - gol pentru parametri
                                    "yes", // dataLayerStatus
                                    "yes" // statusGA4
                                );
                                
                                // Găsim poziția unde să adăugăm parametrul
                                int insertPosition = findInsertPositionForParameter(event.getEvent());
                                taggingRows.add(insertPosition, paramRow);
                                parametersAdded = true;
                            }
                        }
                    }
                    
                    // Dacă evenimentul există deja și am adăugat parametri noi, actualizez codul JavaScript
                    // Folosim "WEB" ca default pentru metodele vechi care nu au platformă
                    if (eventExists && parametersAdded) {
                        updateEventCodeExamplesAfterAdd(event.getEvent(), selectedItems, "WEB");
                    }
                }
            }
        }
        
        return "redirect:/tagging";
    }
    
    @PostMapping("/update")
    public String updateTaggingRow(@RequestParam int rowIndex,
                                 @RequestParam String eventName,
                                 @RequestParam String eventCategory,
                                 @RequestParam String eventDescription,
                                 @RequestParam String eventLocation,
                                 @RequestParam String propertyGroup,
                                 @RequestParam String propertyLabel,
                                 @RequestParam String propertyName,
                                 @RequestParam String propertyDefinition,
                                 @RequestParam String dataType,
                                 @RequestParam String possibleValues) {
        
        if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
            TaggingRow row = taggingRows.get(rowIndex);
            row.setEventName(eventName);
            row.setEventCategory(eventCategory);
            row.setEventDescription(eventDescription);
            row.setEventLocation(eventLocation);
            row.setPropertyGroup(propertyGroup);
            row.setPropertyLabel(propertyLabel);
            row.setPropertyName(propertyName);
            row.setPropertyDefinition(propertyDefinition);
            row.setDataType(dataType);
            row.setPossibleValues(possibleValues);
        }
        
        return "redirect:/tagging";
    }
    
    @PostMapping("/delete")
    public String deleteTaggingRow(@RequestParam int rowIndex) {
        if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
            TaggingRow rowToDelete = taggingRows.get(rowIndex);
            String eventName = rowToDelete.getEventName();
            
            System.out.println("Deleting row at index: " + rowIndex);
            System.out.println("Event name: '" + eventName + "'");
            System.out.println("Total rows before delete: " + taggingRows.size());
            
            // Dacă este un rând de eveniment (are eventName și nu este gol), șterge și toți parametrii săi
            if (eventName != null && !eventName.trim().isEmpty()) {
                System.out.println("Deleting event and all its parameters for: " + eventName);
                
                // Găsește toate rândurile care trebuie șterse
                List<TaggingRow> rowsToDelete = new ArrayList<>();
                
                // Adaugă rândul de eveniment
                rowsToDelete.add(rowToDelete);
                
                // Adaugă toți parametrii acestui eveniment (rândurile cu eventName gol care aparțin acestui eveniment)
                // Pentru a identifica parametrii unui eveniment, trebuie să găsim rândurile care au eventName gol
                // și care sunt în poziția după rândul de eveniment
                for (int i = rowIndex + 1; i < taggingRows.size(); i++) {
                    TaggingRow row = taggingRows.get(i);
                    // Dacă rândul are eventName gol, este un parametru al evenimentului anterior
                    if (row.getEventName() == null || row.getEventName().trim().isEmpty()) {
                        rowsToDelete.add(row);
                    } else {
                        // Dacă găsim un alt eveniment, ne oprim
                        break;
                    }
                }
                
                // Șterge toate rândurile găsite
                taggingRows.removeAll(rowsToDelete);
                System.out.println("Removed " + rowsToDelete.size() + " rows");
                
            } else {
                System.out.println("Deleting single parameter row");
                // Dacă este un rând de parametru (eventName gol), șterge doar acel rând
                String parentEventName = findParentEventName(rowIndex);
            taggingRows.remove(rowIndex);
                
                // Actualizez codul JavaScript pentru evenimentul părinte după ștergere
                if (parentEventName != null) {
                    updateEventCodeExamplesAfterDelete(parentEventName);
                }
            }
            
            System.out.println("Total rows after delete: " + taggingRows.size());
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
                case "eventName":
                    row.setEventName(value);
                    break;
                case "eventCategory":
                    row.setEventCategory(value);
                    break;
                case "eventDescription":
                    row.setEventDescription(value);
                    break;
                case "eventLocation":
                    row.setEventLocation(value);
                    break;
                case "propertyGroup":
                    row.setPropertyGroup(value);
                    break;
                case "propertyLabel":
                    row.setPropertyLabel(value);
                    break;
                case "propertyName":
                    row.setPropertyName(value);
                    break;
                case "propertyDefinition":
                    row.setPropertyDefinition(value);
                    break;
                case "dataType":
                    row.setDataType(value);
                    break;
                case "possibleValues":
                    row.setPossibleValues(value);
                    break;
                case "codeExamples":
                    row.setCodeExamples(value);
                    break;
                case "dataLayerStatus":
                    row.setDataLayerStatus(value);
                    break;
                case "statusGA4":
                    row.setStatusGA4(value);
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
        
        if (googleSheetsService == null) {
            return "redirect:/tagging?error=google-sheets-not-configured";
        }
        
        try {
            System.out.println("Încep să creez Google Sheet cu " + taggingRows.size() + " rânduri...");
            
            // Verifică fișierele din Drive înainte de creare (pentru debugging)
            googleSheetsService.listAllFilesInDrive();
            
            // Creează Google Sheet cu datele folosind API-ul real
            String googleSheetsUrl = googleSheetsService.createGoogleSheet(taggingRows);
            
            System.out.println("Google Sheet creat cu succes: " + googleSheetsUrl);
            
            // Redirecționează către Google Sheet-ul creat
            return "redirect:" + googleSheetsUrl;
        } catch (Exception e) {
            // Log eroarea pentru debugging
            System.err.println("Eroare la crearea Google Sheet: " + e.getMessage());
            e.printStackTrace();
            
            // În caz de eroare, redirecționează cu mesaj de eroare
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Eroare necunoscută";
            return "redirect:/tagging?error=google-sheets-error&message=" + 
                   java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8);
        }
    }
    
    @GetMapping("/export-google-sheet-data")
    public String exportToGoogleSheetWithData(Model model) {
        if (taggingRows.isEmpty()) {
            return "redirect:/tagging?error=no-data";
        }
        
        if (googleSheetsService == null) {
            return "redirect:/tagging?error=google-sheets-not-configured";
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
    
    @GetMapping("/test-google-sheets")
    @ResponseBody
    public ResponseEntity<String> testGoogleSheets() {
        try {
            // Test simplu - doar verifică dacă serviciul se poate injecta
            if (googleSheetsService != null) {
                return ResponseEntity.ok("Google Sheets Service este configurat corect!");
            } else {
                return ResponseEntity.badRequest().body("Google Sheets Service nu este configurat!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Eroare la testarea Google Sheets: " + e.getMessage());
        }
    }
    
    @GetMapping("/export-csv")
    public ResponseEntity<String> exportToCSV() {
        if (taggingRows.isEmpty()) {
            return ResponseEntity.badRequest().body("No data to export");
        }
        
        String csvData;
        if (googleSheetsService != null) {
            csvData = googleSheetsService.generateCSVData(taggingRows);
        } else {
            // Generează CSV manual dacă serviciul nu este disponibil
            csvData = generateCSVDataManually(taggingRows);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "tagging-plan.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
    
    private String generateCSVDataManually(List<TaggingRow> rows) {
        StringBuilder csv = new StringBuilder();
        csv.append("Event Name,Event Category,Event Description,Event Location,Property Group,Property Label,Property Name,Property Definition,Data Type,Possible Values,Code Examples,DATA LAYER STATUS,STATUS GA4\n");
        
        for (TaggingRow row : rows) {
            csv.append("\"").append(escapeCSV(row.getEventName() != null ? row.getEventName() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventCategory() != null ? row.getEventCategory() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventDescription() != null ? row.getEventDescription() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getEventLocation() != null ? row.getEventLocation() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyGroup() != null ? row.getPropertyGroup() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyLabel() != null ? row.getPropertyLabel() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyName() != null ? row.getPropertyName() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getPropertyDefinition() != null ? row.getPropertyDefinition() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getDataType() != null ? row.getDataType() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getPossibleValues() != null ? row.getPossibleValues() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getCodeExamples() != null ? row.getCodeExamples() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getDataLayerStatus() != null ? row.getDataLayerStatus() : "")).append("\",");
            csv.append("\"").append(escapeCSV(row.getStatusGA4() != null ? row.getStatusGA4() : "")).append("\"\n");
        }
        
        return csv.toString();
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }
    
    private Event getEventByName(String eventName) {
        List<Event> events = Arrays.asList(
            new Event("view_item_list", "View Item List", "Track when a user sees a list of items or products", "Product listing pages"),
            new Event("view_item", "View Item", "Track product views", "User views a product"),
            new Event("select_item", "Select Item", "Track when a user selects an item from a list", "User selects an item"),
            new Event("add_to_cart", "Add to Cart", "Track items added to cart", "User adds item to cart"),
            new Event("view_cart", "View Cart", "Track when a user views their cart", "Cart page"),
            new Event("add_to_wishlist", "Add to Wishlist", "Track when a user adds items to wishlist", "User adds item to wishlist"),
            new Event("view_promotion", "View Promotion", "Track when a user views a promotion", "Promotion pages"),
            new Event("select_promotion", "Select Promotion", "Track when a user selects a promotion", "User selects a promotion"),
            new Event("begin_checkout", "Begin Checkout", "Track when a user begins the checkout process", "Checkout start"),
            new Event("add_shipping_info", "Add Shipping Info", "Track when a user adds shipping information", "Shipping form"),
            new Event("add_payment_info", "Add Payment Info", "Track when a user adds payment information", "Payment form"),
            new Event("purchase", "Purchase", "An event that contains data about the purchase made.", "Thank you page"),
            new Event("userData", "User Data", "Track user data information", "User data tracking")
        );
        
        return events.stream()
                .filter(e -> e.getEvent().equals(eventName))
                .findFirst()
                .orElse(null);
    }
    
    private Parameter getParameterByName(String paramName) {
        List<Parameter> parameters = Arrays.asList(
            new Parameter("event", "purchase", "The event name."),
            new Parameter("currency", "RON", "Currency of the value. Use three letter ISO 4217 format."),
            new Parameter("value", "1439.00", "The revenue of the event."),
            new Parameter("customer_type", "new", "Is the conversion from a `new` or `returning` customer?"),
            new Parameter("transaction_id", "NL-23435342", "The unique identifier of a transaction."),
            new Parameter("coupon", "Summer Sale", "The coupon name/code associated with the event."),
            new Parameter("shipping", "10.34", "Shipping cost associated with a transaction."),
            new Parameter("tax", "15.23", "Tax cost associated with a transaction."),
            new Parameter("items", "[{itemKey: itemValue}]", "A list with the product (or products) in the shopping cart."),
            new Parameter("search_term", "winter jacket", "The term that was searched for."),
            new Parameter("method", "email", "The method used to log in."),
            new Parameter("creative_name", "Summer Banner", "The name of the creative used in the promotion."),
            new Parameter("creative_slot", "summer_1", "The name of the creative slot."),
            new Parameter("promotion_id", "P_12345", "The ID of the promotion."),
            new Parameter("promotion_name", "Summer Sale", "The name of the promotion.")
        );
        
        return parameters.stream()
                .filter(p -> p.getParameterName().equals(paramName))
                .findFirst()
                .orElse(null);
    }
    
    private String getPropertyLabel(String paramName) {
        switch (paramName) {
            case "value":
            case "shipping":
            case "tax":
                return "Metric";
            case "event":
            case "coupon":
            case "transaction_id":
            case "currency":
            case "customer_type":
            case "affiliation":
            case "items":
            case "search_term":
            case "method":
            case "creative_name":
            case "creative_slot":
            case "promotion_id":
            case "promotion_name":
                return "Dimension";
            default:
                return "Dimension";
        }
    }
    
    private String getDataType(String paramName) {
        switch (paramName) {
            case "value":
            case "shipping":
            case "tax":
                return "Numeric";
            case "items":
                return "List of Objects";
            case "event":
            case "coupon":
            case "transaction_id":
            case "currency":
            case "affiliation":
            case "search_term":
            case "method":
            case "creative_name":
            case "creative_slot":
            case "promotion_id":
            case "promotion_name":
                return "String";
            default:
                return "String";
        }
    }
    
    @PostMapping("/add-custom-row")
    @ResponseBody
    public ResponseEntity<String> addCustomRow(@RequestParam String eventName,
                                             @RequestParam String eventCategory,
                                             @RequestParam String eventDescription,
                                             @RequestParam String eventLocation,
                                             @RequestParam String propertyGroup,
                                             @RequestParam String propertyLabel,
                                             @RequestParam String propertyName,
                                             @RequestParam String propertyDefinition,
                                             @RequestParam String dataType,
                                             @RequestParam String possibleValues,
                                             @RequestParam String codeExamples,
                                             @RequestParam String dataLayerStatus,
                                             @RequestParam String statusGA4) {
        
        try {
            // Verifică dacă rândul custom are cel puțin un câmp completat
            boolean hasContent = !eventName.trim().isEmpty() || 
                               !eventCategory.trim().isEmpty() || 
                               !eventDescription.trim().isEmpty() || 
                               !propertyName.trim().isEmpty();
            
            if (!hasContent) {
                return ResponseEntity.ok("No content to save");
            }
            
            // Verifică dacă rândul custom există deja (pentru a evita duplicatele)
            // Verifică doar dacă există un rând cu același eventName și propertyName
            boolean customRowExists = taggingRows.stream()
                .anyMatch(row -> row.getEventName().equals(eventName) && 
                               row.getPropertyName().equals(propertyName) &&
                               !eventName.isEmpty() && 
                               !propertyName.isEmpty());
            
            if (!customRowExists) {
                TaggingRow customRow = new TaggingRow(
                    eventName,
                    eventCategory,
                    eventDescription,
                    eventLocation,
                    propertyGroup,
                    propertyLabel,
                    propertyName,
                    propertyDefinition,
                    dataType,
                    possibleValues,
                    codeExamples,
                    dataLayerStatus,
                    statusGA4
                );
                
                taggingRows.add(customRow);
                return ResponseEntity.ok("Custom row added successfully");
            } else {
                return ResponseEntity.ok("Custom row already exists");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding custom row: " + e.getMessage());
        }
    }
    
    @PostMapping("/reset")
    @ResponseBody
    public ResponseEntity<String> resetTable() {
        try {
            // Șterge toate rândurile din listă
            taggingRows.clear();
            
            return ResponseEntity.ok("Table reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resetting table: " + e.getMessage());
        }
    }
    
    /**
     * Generează codul JavaScript pentru un eveniment și parametrii săi
     */
    private String generateJavaScriptCode(String eventName, List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        
        // Header standard - formatat frumos
        code.append("window.dataLayer = window.dataLayer || [];\n");
        code.append("dataLayer.push({ 'ecommerce': null });\n");
        code.append("dataLayer.push({\n");
        code.append("  'event': '").append(eventName).append("'");
        
        // Adaugă parametrii selectați doar dacă există
        if (selectedParameters != null && !selectedParameters.isEmpty()) {
            code.append(",\n  'ecommerce': {\n");
            
            boolean firstParam = true;
            for (String paramName : selectedParameters) {
                if (!firstParam) {
                    code.append(",\n");
                }
                
                switch (paramName) {
                    case "currency":
                        code.append("    'currency': $value");
                        break;
                    case "value":
                        code.append("    'value': $value");
                        break;
                    case "customer_type":
                        code.append("    'customer_type': $value");
                        break;
                    case "transaction_id":
                        code.append("    'transaction_id': $value");
                        break;
                    case "coupon":
                        code.append("    'coupon': $value");
                        break;
                    case "shipping":
                        code.append("    'shipping': $value");
                        break;
                    case "tax":
                        code.append("    'tax': $value");
                        break;
                    case "affiliation":
                        code.append("    'affiliation': $value");
                        break;
                    case "search_term":
                        code.append("    'search_term': $value");
                        break;
                    case "method":
                        code.append("    'method': $value");
                        break;
                    case "creative_name":
                        code.append("    'creative_name': $value");
                        break;
                    case "creative_slot":
                        code.append("    'creative_slot': $value");
                        break;
                    case "promotion_id":
                        code.append("    'promotion_id': $value");
                        break;
                    case "promotion_name":
                        code.append("    'promotion_name': $value");
                        break;
                    case "items":
                        code.append("    'items': [\n");
                        code.append("      {\n");
                        
                        // Adaugă opțiunile selectate pentru items
                        if (selectedItems != null && !selectedItems.isEmpty()) {
                            boolean firstItem = true;
                            for (String item : selectedItems) {
                                if (!firstItem) {
                                    code.append(",\n");
                                }
                                switch (item) {
                                    case "item_id":
                                        code.append("        'item_id': $value");
                                        break;
                                    case "item_name":
                                        code.append("        'item_name': $value");
                                        break;
                                    case "item_category":
                                        code.append("        'item_category': $value");
                                        break;
                                    case "item_category2":
                                        code.append("        'item_category2': $value");
                                        break;
                                    case "item_category3":
                                        code.append("        'item_category3': $value");
                                        break;
                                    case "item_brand":
                                        code.append("        'item_brand': $value");
                                        break;
                                    case "item_variant":
                                        code.append("        'item_variant': $value");
                                        break;
                                    case "item_list_id":
                                        code.append("        'item_list_id': $value");
                                        break;
                                    case "item_list_name":
                                        code.append("        'item_list_name': $value");
                                        break;
                                    case "price":
                                        code.append("        'price': $value");
                                        break;
                                    case "quantity":
                                        code.append("        'quantity': $value");
                                        break;
                                    case "discount":
                                        code.append("        'discount': $value");
                                        break;
                                    case "affiliation":
                                        code.append("        'affiliation': $value");
                                        break;
                                    case "coupon":
                                        code.append("        'coupon': $value");
                                        break;
                                    case "index":
                                        code.append("        'index': $value");
                                        break;
                                    default:
                                        // Pentru parametri custom sau alți parametri
                                        code.append("        '").append(item).append("': $value");
                                        break;
                                }
                                firstItem = false;
                            }
                        } else {
                            // Default: item_id, item_name, item_category
                            code.append("        'item_id': $value,\n");
                            code.append("        'item_name': $value,\n");
                            code.append("        'item_category': $value");
                        }
                        
                        code.append("\n      }\n");
                        code.append("    ]");
                        break;
                    default:
                        code.append("    '").append(paramName).append("': $value");
                        break;
                }
                firstParam = false;
            }
            
            code.append("\n  }");
        }
        
        // Footer - formatat frumos
        code.append("\n});");
        
        return code.toString();
    }
    
    /**
     * Generează codul Swift pentru iOS conform documentației Firebase Analytics
     */
    private String generateSwiftCode(String eventName, List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        
        // Import statement
        code.append("import FirebaseAnalytics\n\n");
        
        // Generează codul în funcție de tipul de eveniment
        switch (eventName) {
            case "view_item_list":
                code.append(generateSwiftViewItemList(selectedItems));
                break;
            case "view_item":
                code.append(generateSwiftViewItem(selectedItems));
                break;
            case "select_item":
                code.append(generateSwiftSelectItem(selectedItems));
                break;
            case "add_to_cart":
                code.append(generateSwiftAddToCart(selectedItems));
                break;
            case "view_cart":
                code.append(generateSwiftViewCart(selectedItems));
                break;
            case "add_to_wishlist":
                code.append(generateSwiftAddToWishlist(selectedItems));
                break;
            case "begin_checkout":
                code.append(generateSwiftBeginCheckout(selectedParameters, selectedItems));
                break;
            case "add_shipping_info":
                code.append(generateSwiftAddShippingInfo(selectedParameters, selectedItems));
                break;
            case "add_payment_info":
                code.append(generateSwiftAddPaymentInfo(selectedParameters, selectedItems));
                break;
            case "purchase":
                code.append(generateSwiftPurchase(selectedParameters, selectedItems));
                break;
            case "view_promotion":
            case "select_promotion":
                code.append(generateSwiftPromotion(eventName, selectedItems));
                break;
            default:
                // Eveniment generic
                code.append(generateSwiftGenericEvent(eventName, selectedParameters, selectedItems));
                break;
        }
        
        return code.toString();
    }
    
    private String generateSwiftViewItemList(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// View Item List\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterItemListID: \"L001\",\n");
        code.append("  AnalyticsParameterItemListName: \"Related products\"\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventViewItemList, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftViewItem(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// View Item\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventViewItem, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftSelectItem(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Select Item\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterItemListID: \"L001\",\n");
        code.append("  AnalyticsParameterItemListName: \"Related products\"\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventSelectItem, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftAddToCart(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add to Cart\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventAddToCart, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftViewCart(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// View Cart\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventViewCart, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftAddToWishlist(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add to Wishlist\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventAddToWishlist, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftBeginCheckout(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Begin Checkout\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("  AnalyticsParameterCoupon: \"SUMMER_FUN\"\n");
        }
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventBeginCheckout, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftAddShippingInfo(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add Shipping Info\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99,\n");
        code.append("  AnalyticsParameterShippingTier: \"Ground\"\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("  AnalyticsParameterCoupon: \"SUMMER_FUN\"\n");
        }
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventAddShippingInfo, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftAddPaymentInfo(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add Payment Info\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 9.99,\n");
        code.append("  AnalyticsParameterPaymentType: \"Credit Card\"\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("  AnalyticsParameterCoupon: \"SUMMER_FUN\"\n");
        }
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventAddPaymentInfo, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftPurchase(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Purchase\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterTransactionID: \"T12345\",\n");
        code.append("  AnalyticsParameterAffiliation: \"Google Store\",\n");
        code.append("  AnalyticsParameterCurrency: \"USD\",\n");
        code.append("  AnalyticsParameterValue: 14.98,\n");
        code.append("  AnalyticsParameterTax: 2.58,\n");
        code.append("  AnalyticsParameterShipping: 5.34\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("  AnalyticsParameterCoupon: \"SUMMER_FUN\"\n");
        }
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(AnalyticsEventPurchase, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftSearch(List<String> selectedParameters) {
        StringBuilder code = new StringBuilder();
        code.append("// Search\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterSearchTerm: \"t-shirts\"\n");
        code.append("]\n\n");
        code.append("Analytics.logEvent(AnalyticsEventSearch, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftLogin(List<String> selectedParameters) {
        StringBuilder code = new StringBuilder();
        code.append("// Login\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterMethod: \"email\"\n");
        code.append("]\n\n");
        code.append("Analytics.logEvent(AnalyticsEventLogin, parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftPromotion(String eventName, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        String eventType = eventName.equals("view_promotion") ? "View Promotion" : "Select Promotion";
        String analyticsEvent = eventName.equals("view_promotion") ? "AnalyticsEventViewPromotion" : "AnalyticsEventSelectPromotion";
        
        code.append("// ").append(eventType).append("\n");
        code.append("var params: [String: Any] = [\n");
        code.append("  AnalyticsParameterPromotionID: \"SUMMER_FUN\",\n");
        code.append("  AnalyticsParameterPromotionName: \"Summer Sale\",\n");
        code.append("  AnalyticsParameterCreativeName: \"summer2020_promo.jpg\",\n");
        code.append("  AnalyticsParameterCreativeSlot: \"featured_app_1\",\n");
        code.append("  AnalyticsParameterLocationID: \"HERO_BANNER\"\n");
        code.append("]\n\n");
        code.append("var items: [[String: Any]] = []\n");
        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
        code.append("\nparams[AnalyticsParameterItems] = items\n\n");
        code.append("Analytics.logEvent(").append(analyticsEvent).append(", parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftGenericEvent(String eventName, List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// ").append(eventName).append("\n");
        code.append("var params: [String: Any] = [:]\n\n");
        
        if (selectedParameters != null && !selectedParameters.isEmpty()) {
            for (String param : selectedParameters) {
                switch (param) {
                    case "currency":
                        code.append("params[AnalyticsParameterCurrency] = \"USD\"\n");
                        break;
                    case "value":
                        code.append("params[AnalyticsParameterValue] = 9.99\n");
                        break;
                    case "items":
                        code.append("var items: [[String: Any]] = []\n");
                        code.append(generateSwiftItems(selectedItems, "item_jeggings"));
                        code.append("params[AnalyticsParameterItems] = items\n");
                        break;
                }
            }
            code.append("\n");
        }
        
        code.append("Analytics.logEvent(\"").append(eventName).append("\", parameters: params)\n");
        return code.toString();
    }
    
    private String generateSwiftItems(List<String> selectedItems, String itemName) {
        StringBuilder code = new StringBuilder();
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            // Default items
            code.append("var ").append(itemName).append(": [String: Any] = [\n");
            code.append("  AnalyticsParameterItemID: \"SKU_123\",\n");
            code.append("  AnalyticsParameterItemName: \"jeggings\",\n");
            code.append("  AnalyticsParameterItemCategory: \"pants\"\n");
            code.append("]\n");
            code.append("items.append(").append(itemName).append(")\n");
        } else {
            code.append("var ").append(itemName).append(": [String: Any] = [\n");
            boolean first = true;
            for (String item : selectedItems) {
                if (!first) {
                    code.append(",\n");
                }
                switch (item) {
                    case "item_id":
                        code.append("  AnalyticsParameterItemID: \"SKU_123\"");
                        break;
                    case "item_name":
                        code.append("  AnalyticsParameterItemName: \"jeggings\"");
                        break;
                    case "item_category":
                        code.append("  AnalyticsParameterItemCategory: \"pants\"");
                        break;
                    case "item_category2":
                        code.append("  AnalyticsParameterItemCategory2: \"pants\"");
                        break;
                    case "item_category3":
                        code.append("  AnalyticsParameterItemCategory3: \"pants\"");
                        break;
                    case "item_brand":
                        code.append("  AnalyticsParameterItemBrand: \"Google\"");
                        break;
                    case "item_variant":
                        code.append("  AnalyticsParameterItemVariant: \"black\"");
                        break;
                    case "item_list_id":
                        code.append("  AnalyticsParameterItemListID: \"L001\"");
                        break;
                    case "item_list_name":
                        code.append("  AnalyticsParameterItemListName: \"Related products\"");
                        break;
                    case "price":
                        code.append("  AnalyticsParameterPrice: 9.99");
                        break;
                    case "quantity":
                        code.append("  AnalyticsParameterQuantity: 1");
                        break;
                    case "discount":
                        code.append("  AnalyticsParameterDiscount: 0.0");
                        break;
                    case "index":
                        code.append("  AnalyticsParameterIndex: 0");
                        break;
                    default:
                        code.append("  \"").append(item).append("\": $value");
                        break;
                }
                first = false;
            }
            code.append("\n]\n");
            code.append("items.append(").append(itemName).append(")\n");
        }
        
        return code.toString();
    }
    
    /**
     * Generează codul Kotlin pentru Android conform documentației Firebase Analytics
     */
    private String generateKotlinCode(String eventName, List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        
        // Import statement
        code.append("import com.google.firebase.analytics.FirebaseAnalytics\n");
        code.append("import android.os.Bundle\n\n");
        
        // Generează codul în funcție de tipul de eveniment
        switch (eventName) {
            case "view_item_list":
                code.append(generateKotlinViewItemList(selectedItems));
                break;
            case "view_item":
                code.append(generateKotlinViewItem(selectedItems));
                break;
            case "select_item":
                code.append(generateKotlinSelectItem(selectedItems));
                break;
            case "add_to_cart":
                code.append(generateKotlinAddToCart(selectedItems));
                break;
            case "view_cart":
                code.append(generateKotlinViewCart(selectedItems));
                break;
            case "add_to_wishlist":
                code.append(generateKotlinAddToWishlist(selectedItems));
                break;
            case "begin_checkout":
                code.append(generateKotlinBeginCheckout(selectedParameters, selectedItems));
                break;
            case "add_shipping_info":
                code.append(generateKotlinAddShippingInfo(selectedParameters, selectedItems));
                break;
            case "add_payment_info":
                code.append(generateKotlinAddPaymentInfo(selectedParameters, selectedItems));
                break;
            case "purchase":
                code.append(generateKotlinPurchase(selectedParameters, selectedItems));
                break;
            case "view_promotion":
            case "select_promotion":
                code.append(generateKotlinPromotion(eventName, selectedItems));
                break;
            default:
                // Eveniment generic
                code.append(generateKotlinGenericEvent(eventName, selectedParameters, selectedItems));
                break;
        }
        
        return code.toString();
    }
    
    private String generateKotlinViewItemList(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// View Item List\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.ITEM_LIST_ID, \"L001\")\n");
        code.append("    putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, \"Related products\")\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM_LIST, params)\n");
        return code.toString();
    }
    
    private String generateKotlinViewItem(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// View Item\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params)\n");
        return code.toString();
    }
    
    private String generateKotlinSelectItem(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Select Item\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.ITEM_LIST_ID, \"L001\")\n");
        code.append("    putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, \"Related products\")\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, params)\n");
        return code.toString();
    }
    
    private String generateKotlinAddToCart(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add to Cart\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, params)\n");
        return code.toString();
    }
    
    private String generateKotlinViewCart(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// View Cart\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_CART, params)\n");
        return code.toString();
    }
    
    private String generateKotlinAddToWishlist(List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add to Wishlist\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_WISHLIST, params)\n");
        return code.toString();
    }
    
    private String generateKotlinBeginCheckout(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Begin Checkout\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("    putString(FirebaseAnalytics.Param.COUPON, \"SUMMER_FUN\")\n");
        }
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, params)\n");
        return code.toString();
    }
    
    private String generateKotlinAddShippingInfo(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add Shipping Info\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        code.append("    putString(FirebaseAnalytics.Param.SHIPPING_TIER, \"Ground\")\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("    putString(FirebaseAnalytics.Param.COUPON, \"SUMMER_FUN\")\n");
        }
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_SHIPPING_INFO, params)\n");
        return code.toString();
    }
    
    private String generateKotlinAddPaymentInfo(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Add Payment Info\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
        code.append("    putString(FirebaseAnalytics.Param.PAYMENT_TYPE, \"Credit Card\")\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("    putString(FirebaseAnalytics.Param.COUPON, \"SUMMER_FUN\")\n");
        }
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_PAYMENT_INFO, params)\n");
        return code.toString();
    }
    
    private String generateKotlinPurchase(List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// Purchase\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.TRANSACTION_ID, \"T12345\")\n");
        code.append("    putString(FirebaseAnalytics.Param.AFFILIATION, \"Google Store\")\n");
        code.append("    putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
        code.append("    putDouble(FirebaseAnalytics.Param.VALUE, 14.98)\n");
        code.append("    putDouble(FirebaseAnalytics.Param.TAX, 2.58)\n");
        code.append("    putDouble(FirebaseAnalytics.Param.SHIPPING, 5.34)\n");
        if (selectedParameters != null && selectedParameters.contains("coupon")) {
            code.append("    putString(FirebaseAnalytics.Param.COUPON, \"SUMMER_FUN\")\n");
        }
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, params)\n");
        return code.toString();
    }
    
    private String generateKotlinSearch(List<String> selectedParameters) {
        StringBuilder code = new StringBuilder();
        code.append("// Search\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.SEARCH_TERM, \"t-shirts\")\n");
        code.append("}\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params)\n");
        return code.toString();
    }
    
    private String generateKotlinLogin(List<String> selectedParameters) {
        StringBuilder code = new StringBuilder();
        code.append("// Login\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.METHOD, \"email\")\n");
        code.append("}\n\n");
        code.append("firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, params)\n");
        return code.toString();
    }
    
    private String generateKotlinPromotion(String eventName, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        String eventType = eventName.equals("view_promotion") ? "View Promotion" : "Select Promotion";
        String analyticsEvent = eventName.equals("view_promotion") ? "FirebaseAnalytics.Event.VIEW_PROMOTION" : "FirebaseAnalytics.Event.SELECT_PROMOTION";
        
        code.append("// ").append(eventType).append("\n");
        code.append("val params = Bundle().apply {\n");
        code.append("    putString(FirebaseAnalytics.Param.PROMOTION_ID, \"SUMMER_FUN\")\n");
        code.append("    putString(FirebaseAnalytics.Param.PROMOTION_NAME, \"Summer Sale\")\n");
        code.append("    putString(FirebaseAnalytics.Param.CREATIVE_NAME, \"summer2020_promo.jpg\")\n");
        code.append("    putString(FirebaseAnalytics.Param.CREATIVE_SLOT, \"featured_app_1\")\n");
        code.append("    putString(FirebaseAnalytics.Param.LOCATION_ID, \"HERO_BANNER\")\n");
        code.append("}\n\n");
        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n\n");
        code.append("firebaseAnalytics.logEvent(").append(analyticsEvent).append(", params)\n");
        return code.toString();
    }
    
    private String generateKotlinGenericEvent(String eventName, List<String> selectedParameters, List<String> selectedItems) {
        StringBuilder code = new StringBuilder();
        code.append("// ").append(eventName).append("\n");
        code.append("val params = Bundle()\n\n");
        
        if (selectedParameters != null && !selectedParameters.isEmpty()) {
            for (String param : selectedParameters) {
                switch (param) {
                    case "currency":
                        code.append("params.putString(FirebaseAnalytics.Param.CURRENCY, \"USD\")\n");
                        break;
                    case "value":
                        code.append("params.putDouble(FirebaseAnalytics.Param.VALUE, 9.99)\n");
                        break;
                    case "items":
                        code.append("val items = arrayOf<Bundle>(").append(generateKotlinItems(selectedItems, "itemJeggings")).append(")\n");
                        code.append("params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, items)\n");
                        break;
                }
            }
            code.append("\n");
        }
        
        code.append("firebaseAnalytics.logEvent(\"").append(eventName).append("\", params)\n");
        return code.toString();
    }
    
    private String generateKotlinItems(List<String> selectedItems, String itemName) {
        StringBuilder code = new StringBuilder();
        
        if (selectedItems == null || selectedItems.isEmpty()) {
            // Default items
            code.append("Bundle().apply {\n");
            code.append("    putString(FirebaseAnalytics.Param.ITEM_ID, \"SKU_123\")\n");
            code.append("    putString(FirebaseAnalytics.Param.ITEM_NAME, \"jeggings\")\n");
            code.append("    putString(FirebaseAnalytics.Param.ITEM_CATEGORY, \"pants\")\n");
            code.append("}\n");
        } else {
            code.append("Bundle().apply {\n");
            for (String item : selectedItems) {
                switch (item) {
                    case "item_id":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_ID, \"SKU_123\")\n");
                        break;
                    case "item_name":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_NAME, \"jeggings\")\n");
                        break;
                    case "item_category":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_CATEGORY, \"pants\")\n");
                        break;
                    case "item_category2":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_CATEGORY2, \"pants\")\n");
                        break;
                    case "item_category3":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_CATEGORY3, \"pants\")\n");
                        break;
                    case "item_brand":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_BRAND, \"Google\")\n");
                        break;
                    case "item_variant":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_VARIANT, \"black\")\n");
                        break;
                    case "item_list_id":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_LIST_ID, \"L001\")\n");
                        break;
                    case "item_list_name":
                        code.append("    putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, \"Related products\")\n");
                        break;
                    case "price":
                        code.append("    putDouble(FirebaseAnalytics.Param.PRICE, 9.99)\n");
                        break;
                    case "quantity":
                        code.append("    putDouble(FirebaseAnalytics.Param.QUANTITY, 1.0)\n");
                        break;
                    case "discount":
                        code.append("    putDouble(FirebaseAnalytics.Param.DISCOUNT, 0.0)\n");
                        break;
                    case "index":
                        code.append("    putInt(FirebaseAnalytics.Param.INDEX, 0)\n");
                        break;
                    default:
                        code.append("    putString(\"").append(item).append("\", $value)\n");
                        break;
                }
            }
            code.append("}\n");
        }
        
        return code.toString();
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
                    case "customer_type":
                        code.append("'customer_type': $value");
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
                    case "search_term":
                        code.append("'search_term': $value");
                        break;
                    case "method":
                        code.append("'method': $value");
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
     * Găsește poziția unde să adăugăm un parametru pentru un eveniment specific
     */
    private int findInsertPositionForParameter(String eventName, String platformCategory) {
        // Găsim poziția evenimentului pentru platforma specificată
        int eventIndex = -1;
        for (int i = 0; i < taggingRows.size(); i++) {
            TaggingRow row = taggingRows.get(i);
            if (eventName.equals(row.getEventName()) && 
                platformCategory != null && platformCategory.equals(row.getEventCategory()) &&
                row.getPropertyName() != null && "event".equals(row.getPropertyName())) {
                eventIndex = i;
                break;
            }
        }
        
        if (eventIndex == -1) {
            // Dacă nu găsim evenimentul, adăugăm la sfârșit
            return taggingRows.size();
        }
        
        // Găsim poziția unde se termină parametrii acestui eveniment pentru această platformă
        int insertPosition = eventIndex + 1;
        
        // Parcurgem rândurile după eveniment până găsim un alt eveniment sau sfârșitul listei
        for (int i = eventIndex + 1; i < taggingRows.size(); i++) {
            TaggingRow row = taggingRows.get(i);
            // Dacă rândul are eventName gol și aceeași platformă, este un parametru al evenimentului curent
            if ((row.getEventName() == null || row.getEventName().trim().isEmpty()) &&
                platformCategory != null && platformCategory.equals(row.getEventCategory())) {
                insertPosition = i + 1; // Adăugăm după acest parametru
            } else if (row.getEventName() != null && !row.getEventName().trim().isEmpty()) {
                // Găsim un alt eveniment, ne oprim aici
                break;
            }
        }
        
        return insertPosition;
    }
    
    /**
     * Găsește poziția unde trebuie inserat un parametru pentru un eveniment (metodă veche pentru compatibilitate)
     */
    private int findInsertPositionForParameter(String eventName) {
        return findInsertPositionForParameter(eventName, "Ecommerce"); // Default pentru compatibilitate
    }
    
    /**
     * Actualizează codul pentru un eveniment după adăugarea parametrilor noi
     * Folosește platforma specificată
     */
    private void updateEventCodeExamplesAfterAdd(String eventName, List<String> selectedItems, String platformCategory) {
        // Găsim toți parametrii existenți pentru acest eveniment din tabel
        List<String> allParameters = new ArrayList<>();
        
        System.out.println("Updating code after add for event: " + eventName);
        System.out.println("Total rows in table: " + taggingRows.size());
        
        for (TaggingRow row : taggingRows) {
            System.out.println("Row - EventName: '" + row.getEventName() + "', PropertyName: '" + row.getPropertyName() + "'");
            
            // Pentru rândurile de parametri (EventName gol), trebuie să găsim evenimentul părinte
            if (row.getEventName() == null || row.getEventName().trim().isEmpty()) {
                // Este un rând de parametru, verifică dacă aparține evenimentului curent și platformei
                if (row.getPropertyName() != null && 
                    !row.getPropertyName().trim().isEmpty() &&
                    !row.getPropertyName().equals("event") &&
                    platformCategory != null && platformCategory.equals(row.getEventCategory())) {
                    // Verifică dacă acest parametru aparține evenimentului curent și platformei
                    if (belongsToEvent(row, eventName, platformCategory)) {
                        allParameters.add(row.getPropertyName());
                        System.out.println("Added parameter: " + row.getPropertyName());
                    }
                }
            }
        }
        
        System.out.println("All parameters: " + allParameters);
        System.out.println("Platform for update: " + platformCategory);
        
        // Generează codul în funcție de platformă
        String updatedCode;
        if ("iOS".equals(platformCategory)) {
            System.out.println("Updating Swift code for event: " + eventName);
            updatedCode = generateSwiftCode(eventName, allParameters, selectedItems);
        } else if ("Android".equals(platformCategory)) {
            System.out.println("Updating Kotlin code for event: " + eventName);
            updatedCode = generateKotlinCode(eventName, allParameters, selectedItems);
        } else {
            System.out.println("Updating JavaScript code for WEB");
            updatedCode = generateJavaScriptCode(eventName, allParameters, selectedItems);
        }
        System.out.println("Generated code length: " + updatedCode.length());
        
        // Actualizez codul în rândul de eveniment pentru platforma specificată
        for (TaggingRow row : taggingRows) {
            if (row.getEventName() != null && row.getEventName().equals(eventName) && 
                row.getPropertyName() != null && 
                row.getPropertyName().equals("event") &&
                platformCategory != null && platformCategory.equals(row.getEventCategory())) {
                row.setCodeExamples(updatedCode);
                System.out.println("Updated code in event row for platform: " + platformCategory);
                break;
            }
        }
    }
    
    /**
     * Găsește numele evenimentului părinte pentru un parametru
     */
    private String findParentEventName(int paramRowIndex) {
        // Căutăm înapoi până găsim un eveniment (rând cu eventName completat)
        for (int i = paramRowIndex - 1; i >= 0; i--) {
            TaggingRow row = taggingRows.get(i);
            if (row.getEventName() != null && !row.getEventName().trim().isEmpty()) {
                return row.getEventName();
            }
        }
        return null;
    }
    
    /**
     * Actualizează codul JavaScript pentru un eveniment după ștergerea unui parametru
     */
    private void updateEventCodeExamplesAfterDelete(String eventName) {
        // Găsim toți parametrii rămași pentru acest eveniment
        List<String> remainingParameters = new ArrayList<>();
        
        System.out.println("Updating code for event: " + eventName);
        System.out.println("Total rows in table: " + taggingRows.size());
        
        for (TaggingRow row : taggingRows) {
            System.out.println("Row - EventName: '" + row.getEventName() + "', PropertyName: '" + row.getPropertyName() + "'");
            
            // Pentru rândurile de parametri (EventName gol), trebuie să găsim evenimentul părinte
            if (row.getEventName() == null || row.getEventName().trim().isEmpty()) {
                // Este un rând de parametru, verifică dacă aparține evenimentului curent
                if (row.getPropertyName() != null && 
                    !row.getPropertyName().trim().isEmpty() &&
                    !row.getPropertyName().equals("event")) {
                    // Verifică dacă acest parametru aparține evenimentului curent
                    if (belongsToEvent(row, eventName)) {
                        remainingParameters.add(row.getPropertyName());
                        System.out.println("Added parameter: " + row.getPropertyName());
                    }
                }
            }
        }
        
        System.out.println("Remaining parameters: " + remainingParameters);
        
        // Generează codul JavaScript cu parametrii rămași (folosim null pentru selectedItems la ștergere)
        String updatedCode = generateJavaScriptCode(eventName, remainingParameters, null);
        System.out.println("Generated code: " + updatedCode);
        
        // Actualizez codul în rândul de eveniment
        for (TaggingRow row : taggingRows) {
            if (row.getEventName() != null && row.getEventName().equals(eventName) && 
                row.getPropertyName() != null && 
                row.getPropertyName().equals("event")) {
                row.setCodeExamples(updatedCode);
                System.out.println("Updated code in event row");
                break;
            }
        }
    }
    
    /**
     * Verifică dacă un rând de parametru aparține unui eveniment specific
     */
    private boolean belongsToEvent(TaggingRow paramRow, String eventName) {
        return belongsToEvent(paramRow, eventName, null);
    }
    
    private boolean belongsToEvent(TaggingRow paramRow, String eventName, String platformCategory) {
        // Găsim poziția rândului de parametru în listă
        int paramIndex = taggingRows.indexOf(paramRow);
        
        // Căutăm înapoi până găsim un eveniment
        for (int i = paramIndex - 1; i >= 0; i--) {
            TaggingRow row = taggingRows.get(i);
            if (row.getEventName() != null && !row.getEventName().trim().isEmpty()) {
                // Am găsit un eveniment, verifică dacă este cel căutat și dacă platforma se potrivește
                boolean eventMatches = row.getEventName().equals(eventName);
                boolean platformMatches = platformCategory == null || 
                                         (row.getEventCategory() != null && platformCategory.equals(row.getEventCategory()));
                return eventMatches && platformMatches;
            }
        }
        
        return false;
    }
    
    /**
     * Obține parametrii oficiali pentru un eveniment specific
     */
    private List<String> getOfficialParametersForEvent(String eventName) {
        List<String> parameters = new ArrayList<>();
        
        switch (eventName) {
            case "view_item_list":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items"));
                break;
            case "view_item":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items"));
                break;
            case "select_item":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items"));
                break;
            case "add_to_cart":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items"));
                break;
            case "view_cart":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items"));
                break;
            case "add_to_wishlist":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items"));
                break;
            case "view_promotion":
                parameters.addAll(Arrays.asList("event", "creative_name", "creative_slot", "promotion_id", "promotion_name", "items"));
                break;
            case "select_promotion":
                parameters.addAll(Arrays.asList("event", "creative_name", "creative_slot", "promotion_id", "promotion_name", "items"));
                break;
            case "begin_checkout":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items", "coupon"));
                break;
            case "add_shipping_info":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items", "coupon"));
                break;
            case "add_payment_info":
                parameters.addAll(Arrays.asList("event", "currency", "value", "items", "coupon"));
                break;
            case "purchase":
                parameters.addAll(Arrays.asList("event", "currency", "value", "customer_type", 
                                               "transaction_id", "coupon", "shipping", "tax", "items"));
                break;
            default:
                // Parametri de bază pentru evenimente necunoscute
                parameters.addAll(Arrays.asList("event", "currency", "value"));
                break;
        }
        
        return parameters;
    }
    
    // ========== REST API ENDPOINTS ==========
    
    /**
     * Endpoint REST pentru obținerea datelor inițiale
     * GET /api/tagging/init
     */
    @GetMapping("/api/init")
    @ResponseBody
    public ResponseEntity<InitialDataResponse> getInitialData() {
        List<Event> events = Arrays.asList(
            new Event("view_item_list", "View Item List", "Track when a user sees a list of items or products", "Product listing pages"),
            new Event("view_item", "View Item", "Track product views", "User views a product"),
            new Event("select_item", "Select Item", "Track when a user selects an item from a list", "User selects an item"),
            new Event("add_to_cart", "Add to Cart", "Track items added to cart", "User adds item to cart"),
            new Event("view_cart", "View Cart", "Track when a user views their cart", "Cart page"),
            new Event("add_to_wishlist", "Add to Wishlist", "Track when a user adds items to wishlist", "User adds item to wishlist"),
            new Event("view_promotion", "View Promotion", "Track when a user views a promotion", "Promotion pages"),
            new Event("select_promotion", "Select Promotion", "Track when a user selects a promotion", "User selects a promotion"),
            new Event("begin_checkout", "Begin Checkout", "Track when a user begins the checkout process", "Checkout start"),
            new Event("add_shipping_info", "Add Shipping Info", "Track when a user adds shipping information", "Shipping form"),
            new Event("add_payment_info", "Add Payment Info", "Track when a user adds payment information", "Payment form"),
            new Event("purchase", "Purchase", "An event that contains data about the purchase made.", "Thank you page"),
            new Event("userData", "User Data", "Track user data information", "User data tracking")
        );
        
        List<Parameter> parameters = Arrays.asList(
            new Parameter("event", "purchase", "The event name."),
            new Parameter("currency", "RON", "Currency of the value. Use three letter ISO 4217 format."),
            new Parameter("value", "1439.00", "The revenue of the event."),
            new Parameter("customer_type", "new", "New or returning user from the last 540-day recommended period"),
            new Parameter("transaction_id", "NL-23435342", "The unique identifier of a transaction."),
            new Parameter("coupon", "Summer Sale", "The coupon name/code associated with the event."),
            new Parameter("shipping", "10.34", "Shipping cost associated with a transaction."),
            new Parameter("tax", "15.23", "Tax cost associated with a transaction."),
            new Parameter("items", "[{itemKey: itemValue}]", "A list with the product (or products) in the shopping cart."),
            new Parameter("search_term", "winter jacket", "The term that was searched for."),
            new Parameter("method", "email", "The method used to log in.")
        );
        
        InitialDataResponse response = new InitialDataResponse(events, parameters, new ArrayList<>(taggingRows));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint REST pentru generarea tagging plan
     * POST /api/tagging/generate
     */
    @PostMapping("/api/generate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateTaggingPlanApi(@RequestBody GenerateRequest request) {
        System.out.println("=== BACKEND: generateTaggingPlanApi CALLED ===");
        System.out.println("Request object: " + request);
        System.out.println("Request class: " + (request != null ? request.getClass().getName() : "null"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> selectedEvents = request != null ? request.getSelectedEvents() : null;
            List<String> selectedItems = request != null ? request.getSelectedItems() : null;
            List<String> selectedPlatforms = request != null ? request.getSelectedPlatforms() : null;
            
            System.out.println("Extracted from request:");
            System.out.println("  selectedEvents: " + selectedEvents);
            System.out.println("  selectedItems: " + selectedItems);
            System.out.println("  selectedPlatforms: " + selectedPlatforms);
            System.out.println("  selectedPlatforms type: " + (selectedPlatforms != null ? selectedPlatforms.getClass().getName() : "null"));
            if (selectedPlatforms != null) {
                System.out.println("  selectedPlatforms size: " + selectedPlatforms.size());
                for (int i = 0; i < selectedPlatforms.size(); i++) {
                    System.out.println("    [" + i + "] = '" + selectedPlatforms.get(i) + "'");
                }
            }
            
            // Salvează platformele selectate pentru a le folosi în actualizări ulterioare
            currentSelectedPlatforms = selectedPlatforms != null ? new ArrayList<>(selectedPlatforms) : new ArrayList<>();
            System.out.println("Stored currentSelectedPlatforms: " + currentSelectedPlatforms);
            
            // Debug logging
            System.out.println("=== GENERATE TAGging PLAN ===");
            System.out.println("Selected Events: " + selectedEvents);
            System.out.println("Selected Items: " + selectedItems);
            System.out.println("Selected Platforms: " + selectedPlatforms);
            System.out.println("Current Selected Platforms stored: " + currentSelectedPlatforms);
            
            // Determină ce platforme trebuie generate
            List<String> platformsToGenerate = new ArrayList<>();
            if (selectedPlatforms != null && !selectedPlatforms.isEmpty()) {
                for (String platform : selectedPlatforms) {
                    if (platform != null) {
                        String platformTrimmed = platform.trim();
                        if ("iOS".equals(platformTrimmed) || "Android".equals(platformTrimmed) || "WEB".equals(platformTrimmed)) {
                            platformsToGenerate.add(platformTrimmed);
                        }
                    }
                }
            }
            // Dacă nu este selectată nicio platformă, folosește WEB ca default
            if (platformsToGenerate.isEmpty()) {
                platformsToGenerate.add("WEB");
            }
            
            System.out.println("Platforms to generate: " + platformsToGenerate);
            
            if (selectedEvents != null && !selectedEvents.isEmpty()) {
                for (String eventName : selectedEvents) {
                    // Tratare specială pentru userData
                    if ("userData".equals(eventName)) {
                        generateUserDataRows(selectedItems != null ? selectedItems : new ArrayList<>());
                        continue;
                    }
                    
                    Event event = getEventByName(eventName);
                    if (event != null) {
                        List<String> officialParameters = getOfficialParametersForEvent(eventName);
                        
                        // Generează rânduri pentru fiecare platformă selectată
                        for (String platform : platformsToGenerate) {
                            // Verifică dacă există deja un rând pentru acest eveniment și platformă
                            boolean eventExists = taggingRows.stream()
                                .anyMatch(row -> row.getEventName().equals(event.getEvent()) && 
                                               platform.equals(row.getEventCategory()));
                            
                            if (!eventExists) {
                                String generatedCode;
                                String platformCategory = platform; // Folosim eventCategory pentru platformă
                                
                                System.out.println("=== GENERATING CODE FOR PLATFORM ===");
                                System.out.println("Platform string: '" + platform + "'");
                                System.out.println("Platform equals iOS: " + "iOS".equals(platform));
                                System.out.println("Platform equals Android: " + "Android".equals(platform));
                                System.out.println("Platform equals WEB: " + "WEB".equals(platform));
                                
                                if ("iOS".equals(platform)) {
                                    System.out.println(">>> Generating Swift code for event: " + eventName + " on platform: " + platform);
                                    generatedCode = generateSwiftCode(event.getEvent(), officialParameters, selectedItems);
                                    System.out.println(">>> Generated Swift code length: " + generatedCode.length());
                                    System.out.println(">>> First 100 chars: " + (generatedCode.length() > 100 ? generatedCode.substring(0, 100) : generatedCode));
                                } else if ("Android".equals(platform)) {
                                    System.out.println(">>> Generating Kotlin code for event: " + eventName + " on platform: " + platform);
                                    generatedCode = generateKotlinCode(event.getEvent(), officialParameters, selectedItems);
                                    System.out.println(">>> Generated Kotlin code length: " + generatedCode.length());
                                    System.out.println(">>> First 100 chars: " + (generatedCode.length() > 100 ? generatedCode.substring(0, 100) : generatedCode));
                                } else {
                                    System.out.println(">>> Generating JavaScript code for event: " + eventName + " on platform: " + platform);
                                    generatedCode = generateJavaScriptCode(event.getEvent(), officialParameters, selectedItems);
                                    System.out.println(">>> Generated JavaScript code length: " + generatedCode.length());
                                    System.out.println(">>> First 100 chars: " + (generatedCode.length() > 100 ? generatedCode.substring(0, 100) : generatedCode));
                                }
                                
                                System.out.println(">>> Creating TaggingRow with platformCategory: '" + platformCategory + "'");
                                TaggingRow eventRow = new TaggingRow(
                                    event.getEvent(),
                                    platformCategory, // Platforma în eventCategory
                                    event.getPurpose(),
                                    event.getTrigger(),
                                    "Analytics",
                                    "Dimension",
                                    "event",
                                    "The event name.",
                                    "String",
                                    event.getEvent(),
                                    generatedCode,
                                    "yes",
                                    "yes"
                                );
                                System.out.println(">>> TaggingRow created - EventName: '" + eventRow.getEventName() + "', Category: '" + eventRow.getEventCategory() + "'");
                                taggingRows.add(eventRow);
                                System.out.println(">>> TaggingRow added to list. Total rows: " + taggingRows.size());
                                
                                // Adaugă parametrii pentru această platformă
                                boolean parametersAdded = false;
                                for (String paramName : officialParameters) {
                                    Parameter param = getParameterByName(paramName);
                                    if (param != null) {
                                        boolean paramExists = taggingRows.stream()
                                            .anyMatch(row -> row.getEventName().equals(event.getEvent()) && 
                                                           platformCategory.equals(row.getEventCategory()) &&
                                                           row.getPropertyName().equals(param.getParameterName()));
                                        
                                        if (!paramExists) {
                                            TaggingRow paramRow = new TaggingRow(
                                                "",
                                                platformCategory, // Platforma în eventCategory
                                                "",
                                                "",
                                                "Analytics",
                                                getPropertyLabel(param.getParameterName()),
                                                param.getParameterName(),
                                                param.getParameterDescription(),
                                                getDataType(param.getParameterName()),
                                                param.getExampleValue(),
                                                "",
                                                "yes",
                                                "yes"
                                            );
                                            
                                            int insertPosition = findInsertPositionForParameter(event.getEvent(), platformCategory);
                                            taggingRows.add(insertPosition, paramRow);
                                            parametersAdded = true;
                                        }
                                    }
                                }
                                
                                if (parametersAdded) {
                                    updateEventCodeExamplesAfterAdd(event.getEvent(), selectedItems, platformCategory);
                                }
                            }
                        }
                    }
                }
            }
            
            response.put("success", true);
            response.put("message", "Tagging plan generated successfully");
            response.put("taggingRows", taggingRows);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating tagging plan: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint REST pentru generarea codului JavaScript
     * POST /api/tagging/generate-code
     */
    @PostMapping("/api/generate-code")
    @ResponseBody
    public ResponseEntity<CodeResponse> generateCodeApi(@RequestBody GenerateCodeRequest request) {
        try {
            String code = generateJavaScriptCode(
                request.getEventName(),
                request.getParameters(),
                request.getItems()
            );
            return ResponseEntity.ok(new CodeResponse(code));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new CodeResponse("Error generating code: " + e.getMessage()));
        }
    }
    
    /**
     * Endpoint REST pentru actualizarea unui rând
     * PUT /api/tagging/update
     */
    @PutMapping("/api/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTaggingRowApi(@RequestBody UpdateRowRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int rowIndex = request.getRowIndex();
            if (taggingRows.isEmpty()) {
                response.put("success", false);
                response.put("message", "Table is empty. Please generate some rows first.");
                response.put("totalRows", 0);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
                TaggingRow row = taggingRows.get(rowIndex);
                row.setEventName(request.getEventName());
                row.setEventCategory(request.getEventCategory());
                row.setEventDescription(request.getEventDescription());
                row.setEventLocation(request.getEventLocation());
                row.setPropertyGroup(request.getPropertyGroup());
                row.setPropertyLabel(request.getPropertyLabel());
                row.setPropertyName(request.getPropertyName());
                row.setPropertyDefinition(request.getPropertyDefinition());
                row.setDataType(request.getDataType());
                row.setPossibleValues(request.getPossibleValues());
                
                response.put("success", true);
                response.put("message", "Row updated successfully");
                response.put("row", row);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid row index: " + rowIndex + ". Valid range is 0 to " + (taggingRows.size() - 1));
                response.put("rowIndex", rowIndex);
                response.put("totalRows", taggingRows.size());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating row: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint REST pentru ștergerea unui rând
     * DELETE /api/tagging/delete/{rowIndex}
     */
    @DeleteMapping("/api/delete/{rowIndex}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTaggingRowApi(@PathVariable int rowIndex) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (taggingRows.isEmpty()) {
                response.put("success", false);
                response.put("message", "Table is empty. Nothing to delete.");
                response.put("totalRows", 0);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
                TaggingRow rowToDelete = taggingRows.get(rowIndex);
                String eventName = rowToDelete.getEventName();
                
                if (eventName != null && !eventName.trim().isEmpty()) {
                    // Șterge evenimentul și toți parametrii săi
                    List<TaggingRow> rowsToDelete = new ArrayList<>();
                    rowsToDelete.add(rowToDelete);
                    
                    for (int i = rowIndex + 1; i < taggingRows.size(); i++) {
                        TaggingRow row = taggingRows.get(i);
                        if (row.getEventName() == null || row.getEventName().trim().isEmpty()) {
                            rowsToDelete.add(row);
                        } else {
                            break;
                        }
                    }
                    
                    taggingRows.removeAll(rowsToDelete);
                    response.put("success", true);
                    response.put("message", "Event and parameters deleted successfully");
                    response.put("deletedCount", rowsToDelete.size());
                } else {
                    // Șterge doar parametrul
                    String parentEventName = findParentEventName(rowIndex);
                    taggingRows.remove(rowIndex);
                    
                    if (parentEventName != null) {
                        updateEventCodeExamplesAfterDelete(parentEventName);
                    }
                    
                    response.put("success", true);
                    response.put("message", "Parameter deleted successfully");
                }
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid row index: " + rowIndex + ". Valid range is 0 to " + (taggingRows.size() - 1));
                response.put("rowIndex", rowIndex);
                response.put("totalRows", taggingRows.size());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting row: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint REST pentru actualizarea unei celule
     * PATCH /api/tagging/update-cell
     */
    @PatchMapping("/api/update-cell")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCellApi(@RequestBody UpdateCellRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int rowIndex = request.getRowIndex();
            String field = request.getField();
            String value = request.getValue();
            
            if (taggingRows.isEmpty()) {
                response.put("success", false);
                response.put("message", "Table is empty. Please generate some rows first.");
                response.put("totalRows", 0);
                return ResponseEntity.badRequest().body(response);
            }
            
            if (rowIndex >= 0 && rowIndex < taggingRows.size()) {
                TaggingRow row = taggingRows.get(rowIndex);
                
                switch (field) {
                    case "eventName":
                        row.setEventName(value);
                        break;
                    case "eventCategory":
                        row.setEventCategory(value);
                        break;
                    case "eventDescription":
                        row.setEventDescription(value);
                        break;
                    case "eventLocation":
                        row.setEventLocation(value);
                        break;
                    case "propertyGroup":
                        row.setPropertyGroup(value);
                        break;
                    case "propertyLabel":
                        row.setPropertyLabel(value);
                        break;
                    case "propertyName":
                        row.setPropertyName(value);
                        break;
                    case "propertyDefinition":
                        row.setPropertyDefinition(value);
                        break;
                    case "dataType":
                        row.setDataType(value);
                        break;
                    case "possibleValues":
                        row.setPossibleValues(value);
                        break;
                    case "codeExamples":
                        row.setCodeExamples(value);
                        break;
                    case "dataLayerStatus":
                        row.setDataLayerStatus(value);
                        break;
                    case "statusGA4":
                        row.setStatusGA4(value);
                        break;
                    default:
                        response.put("success", false);
                        response.put("message", "Invalid field name: " + field);
                        return ResponseEntity.badRequest().body(response);
                }
                
                response.put("success", true);
                response.put("message", "Cell updated successfully");
                response.put("row", row);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid row index: " + rowIndex + ". Valid range is 0 to " + (taggingRows.size() - 1));
                response.put("rowIndex", rowIndex);
                response.put("totalRows", taggingRows.size());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating cell: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint REST pentru adăugarea unui rând custom
     * POST /api/tagging/add-custom-row
     */
    @PostMapping("/api/add-custom-row")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addCustomRowApi(@RequestBody AddCustomRowRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean hasContent = (request.getEventName() != null && !request.getEventName().trim().isEmpty()) ||
                               (request.getEventCategory() != null && !request.getEventCategory().trim().isEmpty()) ||
                               (request.getEventDescription() != null && !request.getEventDescription().trim().isEmpty()) ||
                               (request.getPropertyName() != null && !request.getPropertyName().trim().isEmpty());
            
            if (!hasContent) {
                response.put("success", false);
                response.put("message", "No content to save");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean customRowExists = taggingRows.stream()
                .anyMatch(row -> {
                    String rowEventName = row.getEventName() != null ? row.getEventName() : "";
                    String rowPropertyName = row.getPropertyName() != null ? row.getPropertyName() : "";
                    String reqEventName = request.getEventName() != null ? request.getEventName() : "";
                    String reqPropertyName = request.getPropertyName() != null ? request.getPropertyName() : "";
                    return rowEventName.equals(reqEventName) && 
                           rowPropertyName.equals(reqPropertyName) &&
                           !reqEventName.isEmpty() && 
                           !reqPropertyName.isEmpty();
                });
            
            if (!customRowExists) {
                TaggingRow customRow = new TaggingRow(
                    request.getEventName(),
                    request.getEventCategory(),
                    request.getEventDescription(),
                    request.getEventLocation(),
                    request.getPropertyGroup(),
                    request.getPropertyLabel(),
                    request.getPropertyName(),
                    request.getPropertyDefinition(),
                    request.getDataType(),
                    request.getPossibleValues(),
                    request.getCodeExamples(),
                    request.getDataLayerStatus(),
                    request.getStatusGA4()
                );
                
                taggingRows.add(customRow);
                response.put("success", true);
                response.put("message", "Custom row added successfully");
                response.put("row", customRow);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Custom row already exists");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding custom row: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint REST pentru resetarea tabelului
     * POST /api/tagging/reset
     */
    @PostMapping("/api/reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetTableApi() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            taggingRows.clear();
            response.put("success", true);
            response.put("message", "Table reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting table: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint REST pentru export Google Sheets
     * GET /api/tagging/export-google-sheet
     */
    @GetMapping("/api/export-google-sheet")
    @ResponseBody
    public ResponseEntity<ExportResponse> exportGoogleSheetApi() {
        if (taggingRows.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ExportResponse(false, "No data to export", null));
        }
        
        if (googleSheetsService == null) {
            return ResponseEntity.badRequest()
                .body(new ExportResponse(false, "Google Sheets Service is not configured", null));
        }
        
        try {
            String googleSheetsUrl = googleSheetsService.createGoogleSheet(taggingRows);
            return ResponseEntity.ok(new ExportResponse(true, "Google Sheet created successfully", googleSheetsUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ExportResponse(false, "Error creating Google Sheet: " + e.getMessage(), null));
        }
    }
    
    /**
     * Endpoint REST pentru export JSON
     * GET /api/tagging/export-json
     */
    @GetMapping("/api/export-json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportJsonApi() {
        Map<String, Object> response = new HashMap<>();
        
        // Returnează array gol dacă nu sunt date, nu eroare
        response.put("success", true);
        response.put("data", taggingRows);
        response.put("count", taggingRows.size());
        response.put("message", taggingRows.isEmpty() ? "No data to export. Table is empty." : "Export successful");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint REST pentru export GTM JSON
     * GET /api/tagging/export-gtm-json
     */
    @GetMapping("/api/export-gtm-json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportGtmJsonApi() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (taggingRows.isEmpty()) {
                response.put("success", false);
                response.put("message", "No data to export. Table is empty.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> gtmJson = generateGtmJson();
            response.put("success", true);
            response.put("data", gtmJson);
            response.put("message", "GTM JSON generated successfully");
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=gtm-container.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating GTM JSON: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Generează structura JSON pentru Google Tag Manager
     */
    private Map<String, Object> generateGtmJson() {
        Map<String, Object> gtmJson = new HashMap<>();
        
        // Metadata
        gtmJson.put("exportFormatVersion", 2);
        gtmJson.put("exportTime", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Container Version
        Map<String, Object> containerVersion = new HashMap<>();
        containerVersion.put("path", "accounts/6274223855/containers/209159890/versions/0");
        containerVersion.put("accountId", "6274223855");
        containerVersion.put("containerId", "209159890");
        containerVersion.put("containerVersionId", "0");
        
        // Container info
        Map<String, Object> container = new HashMap<>();
        container.put("path", "accounts/6274223855/containers/209159890");
        container.put("accountId", "6274223855");
        container.put("containerId", "209159890");
        container.put("name", "www.MyCompany.ro");
        container.put("publicId", "GTM-5CF8GSB6");
        container.put("usageContext", Arrays.asList("WEB"));
        container.put("fingerprint", String.valueOf(System.currentTimeMillis()));
        containerVersion.put("container", container);
        
        // Extrage evenimentele unice (doar pentru WEB)
        List<String> uniqueEvents = taggingRows.stream()
            .filter(row -> row.getEventCategory() != null && row.getEventCategory().equals("WEB"))
            .filter(row -> row.getEventName() != null && !row.getEventName().trim().isEmpty())
            .filter(row -> row.getPropertyName() != null && row.getPropertyName().equals("event"))
            .map(TaggingRow::getEventName)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        // Generează tags, triggers și variables
        List<Map<String, Object>> tags = new ArrayList<>();
        List<Map<String, Object>> triggers = new ArrayList<>();
        List<Map<String, Object>> variables = new ArrayList<>();
        Set<String> createdVariables = new HashSet<>();
        int tagIdCounter = 32;
        int triggerIdCounter = 26;
        int variableIdCounter = 22;
        
        for (String eventName : uniqueEvents) {
            // Generează trigger-ul mai întâi pentru a obține ID-ul
            Map<String, Object> trigger = generateGtmTrigger(eventName, triggerIdCounter);
            triggers.add(trigger);
            
            // Generează tag-ul GA4 cu triggerId-ul corect
            Map<String, Object> tag = generateGtmTag(eventName, tagIdCounter++, triggerIdCounter);
            tags.add(tag);
            
            triggerIdCounter++;
            
            // Generează variabilele pentru parametrii evenimentului
            List<String> eventParameters = getParametersForEvent(eventName, "WEB");
            for (String paramName : eventParameters) {
                if (!paramName.equals("event") && !createdVariables.contains(paramName)) {
                    Map<String, Object> variable = generateGtmVariable(paramName, variableIdCounter++);
                    variables.add(variable);
                    createdVariables.add(paramName);
                }
            }
        }
        
        containerVersion.put("tag", tags);
        containerVersion.put("trigger", triggers);
        containerVersion.put("variable", variables);
        containerVersion.put("fingerprint", String.valueOf(System.currentTimeMillis()));
        
        gtmJson.put("containerVersion", containerVersion);
        
        return gtmJson;
    }
    
    /**
     * Generează un tag GA4 pentru un eveniment
     */
    private Map<String, Object> generateGtmTag(String eventName, int tagId, int triggerId) {
        Map<String, Object> tag = new HashMap<>();
        tag.put("accountId", "6274223855");
        tag.put("containerId", "209159890");
        tag.put("tagId", String.valueOf(tagId));
        tag.put("name", "GA4 - " + eventName);
        tag.put("type", "gaawe");
        
        List<Map<String, Object>> parameters = new ArrayList<>();
        
        // sendEcommerceData
        Map<String, Object> sendEcommerceData = new HashMap<>();
        sendEcommerceData.put("type", "BOOLEAN");
        sendEcommerceData.put("key", "sendEcommerceData");
        sendEcommerceData.put("value", "false");
        parameters.add(sendEcommerceData);
        
        // eventSettingsTable
        Map<String, Object> eventSettingsTable = new HashMap<>();
        eventSettingsTable.put("type", "LIST");
        eventSettingsTable.put("key", "eventSettingsTable");
        
        List<Map<String, Object>> eventSettingsList = new ArrayList<>();
        List<String> eventParameters = getParametersForEvent(eventName, "WEB");
        
        for (String paramName : eventParameters) {
            if (paramName.equals("event")) continue; // Skip event parameter
            
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("type", "MAP");
            
            List<Map<String, Object>> mapList = new ArrayList<>();
            
            // parameter key
            Map<String, Object> paramKey = new HashMap<>();
            paramKey.put("type", "TEMPLATE");
            paramKey.put("key", "parameter");
            paramKey.put("value", paramName);
            mapList.add(paramKey);
            
            // parameterValue
            Map<String, Object> paramValue = new HashMap<>();
            paramValue.put("type", "TEMPLATE");
            paramValue.put("key", "parameterValue");
            
            // Pentru toți parametrii, folosește formatul dlv - ecommerce.paramName
            String dataLayerPath = getDataLayerPathForParameter(paramName);
            paramValue.put("value", "{{dlv - " + dataLayerPath + "}}");
            mapList.add(paramValue);
            
            paramMap.put("map", mapList);
            eventSettingsList.add(paramMap);
        }
        
        eventSettingsTable.put("list", eventSettingsList);
        parameters.add(eventSettingsTable);
        
        // eventName
        Map<String, Object> eventNameParam = new HashMap<>();
        eventNameParam.put("type", "TEMPLATE");
        eventNameParam.put("key", "eventName");
        eventNameParam.put("value", eventName);
        parameters.add(eventNameParam);
        
        // measurementIdOverride
        Map<String, Object> measurementId = new HashMap<>();
        measurementId.put("type", "TEMPLATE");
        measurementId.put("key", "measurementIdOverride");
        measurementId.put("value", "{{GA4 - Measurement ID}}");
        parameters.add(measurementId);
        
        tag.put("parameter", parameters);
        tag.put("fingerprint", String.valueOf(System.currentTimeMillis()));
        tag.put("firingTriggerId", Arrays.asList(String.valueOf(triggerId)));
        tag.put("tagFiringOption", "ONCE_PER_EVENT");
        
        Map<String, Object> monitoringMetadata = new HashMap<>();
        monitoringMetadata.put("type", "MAP");
        tag.put("monitoringMetadata", monitoringMetadata);
        
        Map<String, Object> consentSettings = new HashMap<>();
        consentSettings.put("consentStatus", "NOT_SET");
        tag.put("consentSettings", consentSettings);
        
        return tag;
    }
    
    /**
     * Generează un trigger custom event pentru un eveniment
     */
    private Map<String, Object> generateGtmTrigger(String eventName, int triggerId) {
        Map<String, Object> trigger = new HashMap<>();
        trigger.put("accountId", "6274223855");
        trigger.put("containerId", "209159890");
        trigger.put("triggerId", String.valueOf(triggerId));
        trigger.put("name", "ce - " + eventName);
        trigger.put("type", "CUSTOM_EVENT");
        
        List<Map<String, Object>> customEventFilter = new ArrayList<>();
        Map<String, Object> filter = new HashMap<>();
        filter.put("type", "EQUALS");
        
        List<Map<String, Object>> filterParams = new ArrayList<>();
        
        Map<String, Object> arg0 = new HashMap<>();
        arg0.put("type", "TEMPLATE");
        arg0.put("key", "arg0");
        arg0.put("value", "{{_event}}");
        filterParams.add(arg0);
        
        Map<String, Object> arg1 = new HashMap<>();
        arg1.put("type", "TEMPLATE");
        arg1.put("key", "arg1");
        arg1.put("value", eventName);
        filterParams.add(arg1);
        
        filter.put("parameter", filterParams);
        customEventFilter.add(filter);
        
        trigger.put("customEventFilter", customEventFilter);
        trigger.put("fingerprint", String.valueOf(System.currentTimeMillis()));
        
        return trigger;
    }
    
    /**
     * Generează o variabilă dataLayer pentru un parametru
     */
    private Map<String, Object> generateGtmVariable(String paramName, int variableId) {
        Map<String, Object> variable = new HashMap<>();
        variable.put("accountId", "6274223855");
        variable.put("containerId", "209159890");
        variable.put("variableId", String.valueOf(variableId));
        variable.put("name", "dlv - " + getDataLayerPathForParameter(paramName));
        variable.put("type", "v");
        
        List<Map<String, Object>> parameters = new ArrayList<>();
        
        Map<String, Object> dataLayerVersion = new HashMap<>();
        dataLayerVersion.put("type", "INTEGER");
        dataLayerVersion.put("key", "dataLayerVersion");
        dataLayerVersion.put("value", "2");
        parameters.add(dataLayerVersion);
        
        Map<String, Object> setDefaultValue = new HashMap<>();
        setDefaultValue.put("type", "BOOLEAN");
        setDefaultValue.put("key", "setDefaultValue");
        setDefaultValue.put("value", "false");
        parameters.add(setDefaultValue);
        
        Map<String, Object> name = new HashMap<>();
        name.put("type", "TEMPLATE");
        name.put("key", "name");
        name.put("value", getDataLayerPathForParameter(paramName));
        parameters.add(name);
        
        variable.put("parameter", parameters);
        variable.put("fingerprint", String.valueOf(System.currentTimeMillis()));
        
        Map<String, Object> formatValue = new HashMap<>();
        variable.put("formatValue", formatValue);
        
        return variable;
    }
    
    /**
     * Returnează path-ul dataLayer pentru un parametru
     */
    private String getDataLayerPathForParameter(String paramName) {
        // Parametrii ecommerce folosesc formatul ecommerce.paramName
        List<String> ecommerceParams = Arrays.asList(
            "currency", "value", "transaction_id", "coupon", "shipping", "tax",
            "items", "customer_type", "item_id", "item_name", "item_category", "item_category2",
            "item_category3", "item_brand", "item_variant", "item_list_id",
            "item_list_name", "price", "quantity", "discount", "affiliation", "index",
            "creative_name", "creative_slot", "promotion_id", "promotion_name"
        );
        
        if (ecommerceParams.contains(paramName)) {
            return "ecommerce." + paramName;
        }
        
        // Pentru alți parametri, folosește direct numele
        return paramName;
    }
    
    /**
     * Obține parametrii pentru un eveniment specific (doar pentru WEB)
     */
    private List<String> getParametersForEvent(String eventName, String platform) {
        List<String> parameters = new ArrayList<>();
        
        // Găsește rândul de eveniment
        TaggingRow eventRow = taggingRows.stream()
            .filter(row -> row.getEventName() != null && row.getEventName().equals(eventName))
            .filter(row -> row.getEventCategory() != null && row.getEventCategory().equals(platform))
            .filter(row -> row.getPropertyName() != null && row.getPropertyName().equals("event"))
            .findFirst()
            .orElse(null);
        
        if (eventRow == null) {
            // Fallback: folosește parametrii oficiali
            return getOfficialParametersForEvent(eventName);
        }
        
        // Găsește index-ul evenimentului
        int eventIndex = taggingRows.indexOf(eventRow);
        
        // Adaugă "event" ca prim parametru
        parameters.add("event");
        
        // Caută parametrii după eveniment
        for (int i = eventIndex + 1; i < taggingRows.size(); i++) {
            TaggingRow row = taggingRows.get(i);
            
            // Dacă găsim un alt eveniment, ne oprim
            if (row.getEventName() != null && !row.getEventName().trim().isEmpty() && 
                !row.getEventName().equals(eventName)) {
                break;
            }
            
            // Dacă rândul este un parametru pentru acest eveniment
            if ((row.getEventName() == null || row.getEventName().trim().isEmpty()) &&
                row.getEventCategory() != null && row.getEventCategory().equals(platform) &&
                row.getPropertyName() != null && !row.getPropertyName().trim().isEmpty() &&
                !row.getPropertyName().equals("event")) {
                parameters.add(row.getPropertyName());
            }
        }
        
        return parameters;
    }
    
    /**
     * Generează rândurile pentru evenimentul userData
     */
    private void generateUserDataRows(List<String> selectedParams) {
        // Verifică dacă evenimentul userData există deja
        boolean eventExists = taggingRows.stream()
            .anyMatch(row -> "userData".equals(row.getEventName()) && "WEB".equals(row.getEventCategory()));
        
        if (!eventExists) {
            // Generează codul pentru userData (include loggedStatus + parametrii selectați)
            List<String> allParams = new ArrayList<>();
            allParams.add("loggedStatus"); // Adaugă loggedStatus ca prim parametru
            if (selectedParams != null) {
                allParams.addAll(selectedParams);
            }
            String generatedCode = generateUserDataCode(allParams);
            
            // Primul rând - event (ca la celelalte evenimente)
            TaggingRow eventRow = new TaggingRow(
                "userData", // eventName
                "WEB", // eventCategory
                "an event about user properties", // eventDescription
                "on every page", // eventLocation
                "user", // propertyGroup
                "Dimension", // propertyLabel
                "event", // propertyName
                "", // propertyDefinition - gol
                "String", // dataType - cu prima literă mare
                "userData", // possibleValues
                generatedCode, // codeExamples
                "yes", // dataLayerStatus
                "yes" // statusGA4
            );
            taggingRows.add(eventRow);
            
            // Adaugă loggedStatus ca parametru
            TaggingRow loggedStatusRow = new TaggingRow(
                "", // eventName gol pentru rândul de parametru
                "WEB", // eventCategory
                getUserDataDescription("loggedStatus"), // eventDescription
                "", // eventLocation
                "user", // propertyGroup
                getUserDataPropertyLabel("loggedStatus"), // propertyLabel
                "loggedStatus", // propertyName
                "", // propertyDefinition - gol
                getUserDataDataType("loggedStatus"), // dataType
                getUserDataPossibleValue("loggedStatus"), // possibleValues
                "", // codeExamples
                "yes", // dataLayerStatus
                "yes" // statusGA4
            );
            taggingRows.add(loggedStatusRow);
            
            // Adaugă parametrii selectați
            for (String paramName : selectedParams) {
                TaggingRow paramRow = new TaggingRow(
                    "", // eventName gol pentru rândul de parametru
                    "WEB", // eventCategory
                    getUserDataDescription(paramName), // eventDescription
                    "", // eventLocation
                    "user", // propertyGroup
                    getUserDataPropertyLabel(paramName), // propertyLabel
                    paramName, // propertyName
                    "", // propertyDefinition - gol
                    getUserDataDataType(paramName), // dataType
                    getUserDataPossibleValue(paramName), // possibleValues
                    "", // codeExamples
                    "yes", // dataLayerStatus
                    "yes" // statusGA4
                );
                taggingRows.add(paramRow);
            }
        }
    }
    
    /**
     * Returnează descrierea pentru un parametru userData
     */
    private String getUserDataDescription(String paramName) {
        switch (paramName) {
            case "loggedStatus":
                return "User authentication status: \"logged\" for authenticated users and \"guest\" for non-logged users";
            case "userID":
                return "Unique identifier for the user";
            case "email":
                return "User's email address";
            case "md5":
                return "MD5 hash of the user's email address";
            case "sha256":
                return "SHA256 hash of the user's email address";
            case "firstPurchase":
                return "Date of the user's first purchase";
            case "lastPurchase":
                return "Date of the user's last purchase";
            case "accountAge":
                return "Number of days since account creation";
            case "Orders":
                return "Total number of orders placed by the user";
            case "OrdersValue":
                return "Total value of all orders placed by the user";
            case "OrdersCanceled":
                return "Number of canceled orders";
            case "catOrdered":
                return "Categories of products the user has ordered";
            case "catWishlisted":
                return "Categories of products in the user's wishlist";
            case "lastPaymentType":
                return "Payment method used in the last order";
            case "lastShippingMethod":
                return "Shipping method used in the last order";
            case "newsletter_subscriber":
                return "Whether the user is subscribed to the newsletter";
            case "lastPurchasedProducts":
                return "List of products purchased in the last order";
            case "lastProductsCompare":
                return "List of products the user compared";
            case "savedCard":
                return "Whether the user has a saved payment card";
            case "savedAddress":
                return "Whether the user has a saved shipping address";
            case "client_type":
                return "Type of client: new, returning, VIP, etc.";
            default:
                return "User data parameter: " + paramName;
        }
    }
    
    /**
     * Returnează property label (Dimension sau Metric) pentru un parametru userData
     */
    private String getUserDataPropertyLabel(String paramName) {
        switch (paramName) {
            case "Orders":
            case "OrdersValue":
            case "OrdersCanceled":
            case "accountAge":
                return "Metric";
            default:
                return "Dimension";
        }
    }
    
    /**
     * Returnează data type (String, Numeric, List) pentru un parametru userData
     */
    private String getUserDataDataType(String paramName) {
        switch (paramName) {
            case "Orders":
            case "OrdersValue":
            case "OrdersCanceled":
            case "accountAge":
                return "Numeric";
            case "catOrdered":
            case "catWishlisted":
            case "lastPurchasedProducts":
            case "lastProductsCompare":
                return "List";
            default:
                return "String";
        }
    }
    
    /**
     * Returnează o valoare reală pentru un parametru userData
     */
    private String getUserDataPossibleValue(String paramName) {
        switch (paramName) {
            case "loggedStatus":
                return "logged";
            case "userID":
                return "12345";
            case "email":
                return "user@example.com";
            case "md5":
                return "5d41402abc4b2a76b9719d911017c592";
            case "sha256":
                return "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae";
            case "firstPurchase":
                return "2024-01-15";
            case "lastPurchase":
                return "2024-11-19";
            case "accountAge":
                return "365";
            case "Orders":
                return "10";
            case "OrdersValue":
                return "1500.50";
            case "OrdersCanceled":
                return "2";
            case "catOrdered":
                return "Electronics, Clothing";
            case "catWishlisted":
                return "Books, Home";
            case "lastPaymentType":
                return "credit_card";
            case "lastShippingMethod":
                return "standard";
            case "newsletter_subscriber":
                return "true";
            case "lastPurchasedProducts":
                return "Product A, Product B";
            case "lastProductsCompare":
                return "Product X, Product Y";
            case "savedCard":
                return "true";
            case "savedAddress":
                return "true";
            case "client_type":
                return "returning";
            default:
                return "value";
        }
    }
    
    /**
     * Generează codul JavaScript pentru userData
     * selectedParams include loggedStatus + parametrii selectați
     */
    private String generateUserDataCode(List<String> selectedParams) {
        StringBuilder code = new StringBuilder();
        code.append("window.dataLayer = window.dataLayer || [];\n");
        code.append("dataLayer.push({\n");
        code.append("  'userData': {\n");
        
        // Adaugă toți parametrii (inclusiv loggedStatus)
        if (selectedParams != null && !selectedParams.isEmpty()) {
            for (int i = 0; i < selectedParams.size(); i++) {
                String param = selectedParams.get(i);
                if (i == 0) {
                    code.append("    '").append(param).append("': 'logged'");
                } else {
                    code.append("    '").append(param).append("': $value");
                }
                if (i < selectedParams.size() - 1) {
                    code.append(",\n");
                } else {
                    code.append("\n");
                }
            }
        } else {
            // Dacă nu sunt parametri, adaugă doar loggedStatus
            code.append("    'loggedStatus': 'logged'\n");
        }
        
        code.append("  }\n");
        code.append("});\n");
        
        return code.toString();
    }
    
}
