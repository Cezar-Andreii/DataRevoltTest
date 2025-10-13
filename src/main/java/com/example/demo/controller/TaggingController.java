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
                    event.getEvent() // possibleValues
                );
                taggingRows.add(eventRow);
            }
            
            // Adăugăm rânduri pentru fiecare parametru selectat
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
                            param.getExampleValue() // possibleValues
                        );
                        taggingRows.add(paramRow);
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
}
