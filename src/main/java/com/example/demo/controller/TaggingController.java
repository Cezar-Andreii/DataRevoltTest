package com.example.demo.controller;

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
            new Event("search", "Search", "Track search queries", "User performs a search"),
            new Event("login", "Login", "Track user logins", "User logs into account")
        );
        
        // Parametri predefiniți pentru purchase
        List<Parameter> parameters = Arrays.asList(
            new Parameter("event", "purchase", "The event name."),
            new Parameter("value", "1439.00", "The revenue of the event."),
            new Parameter("coupon", "Summer Sale", "The coupon name/code associated with the event."),
            new Parameter("transaction_id", "NL-23435342", "The unique identifier of a transaction."),
            new Parameter("shipping", "10.34", "Shipping cost associated with a transaction."),
            new Parameter("currency", "RON", "Currency of the value. Use three letter ISO 4217 format."),
            new Parameter("affiliation", "Store Name", "The store or affiliation from which this transaction occurred."),
            new Parameter("tax", "15.23", "Tax cost associated with a transaction."),
            new Parameter("items", "[{itemKey: itemValue}]", "A list with the product (or products) in the shopping cart.")
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
            // Verificăm dacă evenimentul există deja în listă
            boolean eventExists = taggingRows.stream()
                .anyMatch(row -> row.getEventName().equals(event.getEvent()));
            
            // Dacă evenimentul nu există, adăugăm un rând cu informațiile despre eveniment
            if (!eventExists) {
                // Generează codul JavaScript pentru eveniment
                String generatedCode = generateJavaScriptCode(event.getEvent(), selectedParameters);
                
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
            
            // Adăugăm rânduri pentru fiecare parametru selectat
            boolean parametersAdded = false;
            for (String paramName : selectedParameters) {
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
            if (eventExists && parametersAdded) {
                updateEventCodeExamplesAfterAdd(event.getEvent());
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
            new Parameter("event", "purchase", "The event name."),
            new Parameter("value", "1439.00", "The revenue of the event."),
            new Parameter("coupon", "Summer Sale", "The coupon name/code associated with the event."),
            new Parameter("transaction_id", "NL-23435342", "The unique identifier of a transaction."),
            new Parameter("shipping", "10.34", "Shipping cost associated with a transaction."),
            new Parameter("currency", "RON", "Currency of the value. Use three letter ISO 4217 format."),
            new Parameter("affiliation", "Store Name", "The store or affiliation from which this transaction occurred."),
            new Parameter("tax", "15.23", "Tax cost associated with a transaction."),
            new Parameter("items", "[{itemKey: itemValue}]", "A list with the product (or products) in the shopping cart.")
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
            case "affiliation":
            case "items":
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
    private String generateJavaScriptCode(String eventName, List<String> selectedParameters) {
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
                    case "items":
                        code.append("    'items': [\n");
                        code.append("      {\n");
                        code.append("        item_object_1\n");
                        code.append("      }\n");
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
     * Găsește poziția unde să adăugăm un parametru pentru un eveniment specific
     */
    private int findInsertPositionForParameter(String eventName) {
        // Găsim poziția evenimentului
        int eventIndex = -1;
        for (int i = 0; i < taggingRows.size(); i++) {
            if (eventName.equals(taggingRows.get(i).getEventName())) {
                eventIndex = i;
                break;
            }
        }
        
        if (eventIndex == -1) {
            // Dacă nu găsim evenimentul, adăugăm la sfârșit
            return taggingRows.size();
        }
        
        // Găsim poziția unde se termină parametrii acestui eveniment
        int insertPosition = eventIndex + 1;
        
        // Parcurgem rândurile după eveniment până găsim un alt eveniment sau sfârșitul listei
        for (int i = eventIndex + 1; i < taggingRows.size(); i++) {
            TaggingRow row = taggingRows.get(i);
            // Dacă rândul are eventName gol, este un parametru al evenimentului curent
            if (row.getEventName() == null || row.getEventName().trim().isEmpty()) {
                insertPosition = i + 1; // Adăugăm după acest parametru
            } else {
                // Găsim un alt eveniment, ne oprim aici
                break;
            }
        }
        
        return insertPosition;
    }
    
    /**
     * Actualizează codul JavaScript pentru un eveniment după adăugarea parametrilor noi
     */
    private void updateEventCodeExamplesAfterAdd(String eventName) {
        // Găsim toți parametrii existenți pentru acest eveniment din tabel
        List<String> allParameters = new ArrayList<>();
        
        System.out.println("Updating code after add for event: " + eventName);
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
                        allParameters.add(row.getPropertyName());
                        System.out.println("Added parameter: " + row.getPropertyName());
                    }
                }
            }
        }
        
        System.out.println("All parameters: " + allParameters);
        
        // Generează codul JavaScript cu toți parametrii din tabel
        String updatedCode = generateJavaScriptCode(eventName, allParameters);
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
        
        // Generează codul JavaScript cu parametrii rămași
        String updatedCode = generateJavaScriptCode(eventName, remainingParameters);
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
        // Găsim poziția rândului de parametru în listă
        int paramIndex = taggingRows.indexOf(paramRow);
        
        // Căutăm înapoi până găsim un eveniment
        for (int i = paramIndex - 1; i >= 0; i--) {
            TaggingRow row = taggingRows.get(i);
            if (row.getEventName() != null && !row.getEventName().trim().isEmpty()) {
                // Am găsit un eveniment, verifică dacă este cel căutat
                return row.getEventName().equals(eventName);
            }
        }
        
        return false;
    }
    
}
