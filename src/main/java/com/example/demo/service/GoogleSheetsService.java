package com.example.demo.service;

import com.example.demo.model.TaggingRow;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class GoogleSheetsService {
    
    @Autowired
    private Sheets sheetsService;
    
    @Autowired(required = false)
    private Drive driveService;
    
    /**
     * Verifică și listează toate fișierele din Drive-ul Service Account-ului
     */
    public void listAllFilesInDrive() {
        if (driveService == null) {
            System.out.println("GoogleSheetsService: Drive service nu este disponibil pentru verificare");
            return;
        }
        
        try {
            System.out.println("GoogleSheetsService: ========================================");
            System.out.println("GoogleSheetsService: VERIFICARE FIȘIERE ÎN DRIVE");
            System.out.println("GoogleSheetsService: ========================================");
            
            // Listă TOATE fișierele (nu doar spreadsheet-urile)
            // Folosește supportsAllDrives pentru a include și Shared Drives
            FileList fileList = driveService.files().list()
                .setFields("files(id, name, mimeType, createdTime, size, owners)")
                .setPageSize(100)
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute();
            
            List<File> files = fileList.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("GoogleSheetsService: ✅ Drive-ul este GOL - nu există fișiere");
                System.out.println("GoogleSheetsService: ========================================");
                return;
            }
            
            System.out.println("GoogleSheetsService: Găsit " + files.size() + " fișiere în Drive:");
            System.out.println("GoogleSheetsService: ----------------------------------------");
            
            int spreadsheetCount = 0;
            long totalSize = 0;
            
            for (File file : files) {
                String mimeType = file.getMimeType() != null ? file.getMimeType() : "unknown";
                String size = file.getSize() != null ? String.valueOf(file.getSize()) + " bytes" : "N/A";
                String created = file.getCreatedTime() != null ? file.getCreatedTime().toString() : "N/A";
                
                System.out.println("GoogleSheetsService: - " + file.getName());
                System.out.println("GoogleSheetsService:   ID: " + file.getId());
                System.out.println("GoogleSheetsService:   Type: " + mimeType);
                System.out.println("GoogleSheetsService:   Size: " + size);
                System.out.println("GoogleSheetsService:   Created: " + created);
                
                if (mimeType.contains("spreadsheet")) {
                    spreadsheetCount++;
                }
                if (file.getSize() != null) {
                    totalSize += file.getSize();
                }
                System.out.println("GoogleSheetsService: ----------------------------------------");
            }
            
            System.out.println("GoogleSheetsService: REZUMAT:");
            System.out.println("GoogleSheetsService:   Total fișiere: " + files.size());
            System.out.println("GoogleSheetsService:   Spreadsheet-uri: " + spreadsheetCount);
            System.out.println("GoogleSheetsService:   Total size: " + (totalSize / 1024 / 1024) + " MB");
            System.out.println("GoogleSheetsService: ========================================");
            
        } catch (Exception e) {
            System.err.println("GoogleSheetsService: ❌ EROARE la verificarea fișierelor:");
            System.err.println("GoogleSheetsService:   Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Curăță fișierele vechi din Drive-ul Service Account-ului
     * (pentru a evita storageQuotaExceeded)
     */
    private void cleanupOldFiles() {
        if (driveService == null) {
            return;
        }
        
        try {
            System.out.println("GoogleSheetsService: Curăț fișierele vechi din Drive...");
            
            // Listă toate fișierele spreadsheet
            // Folosește supportsAllDrives pentru a include și Shared Drives
            FileList fileList = driveService.files().list()
                .setQ("mimeType='application/vnd.google-apps.spreadsheet' and name contains 'Tagging Plan'")
                .setFields("files(id, name, createdTime)")
                .setOrderBy("createdTime desc")
                .setPageSize(100)
                .setSupportsAllDrives(true)
                .setIncludeItemsFromAllDrives(true)
                .execute();
            
            List<File> files = fileList.getFiles();
            if (files == null || files.isEmpty()) {
                System.out.println("GoogleSheetsService: Nu există fișiere vechi de șters");
                return;
            }
            
            System.out.println("GoogleSheetsService: Găsit " + files.size() + " fișiere. Păstrez doar cele mai recente 5...");
            
            // Șterge toate în afară de cele mai recente 5
            int kept = 0;
            int deleted = 0;
            for (File file : files) {
                if (kept < 5) {
                    kept++;
                    System.out.println("GoogleSheetsService: Păstrez: " + file.getName() + " (ID: " + file.getId() + ")");
                } else {
                    try {
                        driveService.files().delete(file.getId())
                            .setSupportsAllDrives(true)
                            .execute();
                        deleted++;
                        System.out.println("GoogleSheetsService: Șters: " + file.getName());
                    } catch (Exception e) {
                        System.err.println("GoogleSheetsService: Eroare la ștergerea " + file.getName() + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("GoogleSheetsService: ✅ Cleanup complet: " + deleted + " fișiere șterse, " + kept + " păstrate");
            
        } catch (Exception e) {
            System.err.println("GoogleSheetsService: ⚠️ Eroare la cleanup (continuă oricum): " + e.getMessage());
        }
    }
    
    /**
     * Creează un Google Sheet nou cu datele din tagging plan
     */
    public String createGoogleSheet(List<TaggingRow> taggingRows) throws IOException {
        if (taggingRows == null || taggingRows.isEmpty()) {
            throw new IllegalArgumentException("Nu există date pentru export");
        }
        
        System.out.println("GoogleSheetsService: Pregătesc să creez Google Sheet...");
        
        // 1. Creează spreadsheet-ul nou
        Spreadsheet spreadsheet = new Spreadsheet()
            .setProperties(new SpreadsheetProperties()
                .setTitle("Tagging Plan - " + java.time.LocalDateTime.now().toString()));
        
        System.out.println("GoogleSheetsService: Creez spreadsheet-ul...");
        System.out.println("GoogleSheetsService: Sheets service: " + 
            (sheetsService != null ? "✅ Inițializat" : "❌ NULL"));
        System.out.println("GoogleSheetsService: Drive service: " + 
            (driveService != null ? "✅ Inițializat" : "⚠️ NULL (va încerca doar Sheets API)"));
        
        // Curăță fișierele vechi înainte de a crea unul nou (pentru a evita storageQuotaExceeded)
        if (driveService != null) {
            cleanupOldFiles();
        }
        
        String spreadsheetId;
        
        try {
            // Încearcă mai întâi cu Sheets API direct (metoda standard)
            System.out.println("GoogleSheetsService: Încerc crearea prin Sheets API...");
        Spreadsheet createdSpreadsheet = sheetsService.spreadsheets()
            .create(spreadsheet)
            .execute();
        
            spreadsheetId = createdSpreadsheet.getSpreadsheetId();
            System.out.println("GoogleSheetsService: ✅ Spreadsheet creat cu ID: " + spreadsheetId + " (prin Sheets API)");
        
        // 2. Pregătește datele pentru inserare
        List<List<Object>> values = prepareDataForSheets(taggingRows);
        
        // 3. Inserează datele în sheet
            System.out.println("GoogleSheetsService: Populez datele în spreadsheet...");
        ValueRange body = new ValueRange()
            .setValues(values);
        
        sheetsService.spreadsheets().values()
            .update(spreadsheetId, "A1", body)
            .setValueInputOption("RAW")
            .execute();
        
            System.out.println("GoogleSheetsService: ✅ Datele au fost inserate");
            
        // 4. Formatează header-ul
            System.out.println("GoogleSheetsService: Formatez header-ul...");
        formatHeader(spreadsheetId);
        
        // 5. Returnează URL-ul către sheet
            String url = "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
            System.out.println("GoogleSheetsService: ✅ Spreadsheet gata: " + url);
            return url;
        
        } catch (com.google.api.client.googleapis.json.GoogleJsonResponseException e) {
            if (e.getStatusCode() == 403 && driveService != null) {
                // Dacă Sheets API nu funcționează, încearcă prin Drive API
                System.err.println("GoogleSheetsService: ⚠️ Sheets API a returnat 403, încearcă prin Drive API...");
                try {
                    // Creează spreadsheet prin Drive API
                    File driveFile = new File();
                    driveFile.setName("Tagging Plan - " + java.time.LocalDateTime.now().toString());
                    driveFile.setMimeType("application/vnd.google-apps.spreadsheet");
                    
                    // Încearcă cu supportsAllDrives și supportsTeamDrives pentru Shared Drive support
                    File createdFile = driveService.files().create(driveFile)
                        .setFields("id, name")
                        .setSupportsAllDrives(true)
                        .setSupportsTeamDrives(true)
                        .execute();
                    
                    spreadsheetId = createdFile.getId();
                    System.out.println("GoogleSheetsService: ✅ Spreadsheet creat cu ID: " + spreadsheetId + " (prin Drive API)");
                    
                    // Continuă cu popularea datelor (folosind Sheets API pentru un fișier existent)
                    List<List<Object>> values = prepareDataForSheets(taggingRows);
                    System.out.println("GoogleSheetsService: Populez datele în spreadsheet...");
                    ValueRange body = new ValueRange()
                        .setValues(values);
                    
                    sheetsService.spreadsheets().values()
                        .update(spreadsheetId, "A1", body)
                        .setValueInputOption("RAW")
                        .execute();
                    
                    System.out.println("GoogleSheetsService: ✅ Datele au fost inserate");
                    
                    // Formatează header-ul
                    System.out.println("GoogleSheetsService: Formatez header-ul...");
                    formatHeader(spreadsheetId);
                    
                    String url = "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
                    System.out.println("GoogleSheetsService: ✅ Spreadsheet gata: " + url);
                    return url;
                    
                } catch (Exception driveException) {
                    System.err.println("GoogleSheetsService: ❌ EROARE la crearea prin Drive API:");
                    System.err.println("  Message: " + driveException.getMessage());
                    System.err.println("GoogleSheetsService: ❌ EROARE ORIGINALĂ Google Sheets API:");
                    System.err.println("  Code: " + e.getStatusCode());
                    System.err.println("  Message: " + e.getMessage());
                    System.err.println("GoogleSheetsService: ⚠️ VERIFICĂRI NECESARE:");
                    System.err.println("  1. Service Account-ul trebuie să aibă rolul 'Editor' în IAM & Admin → IAM");
                    System.err.println("  2. Google Sheets API trebuie să fie activat în APIs & Services → Enabled APIs");
                    System.err.println("  3. Google Drive API trebuie să fie activat în APIs & Services → Enabled APIs");
                    System.err.println("  4. Verifică că proiectul din credentials.json este corect");
                    throw e;
                }
            } else {
                System.err.println("GoogleSheetsService: ❌ EROARE Google API:");
                System.err.println("  Code: " + e.getStatusCode());
                System.err.println("  Message: " + e.getMessage());
                if (e.getDetails() != null) {
                    System.err.println("  Details: " + e.getDetails().toString());
                }
                System.err.println("GoogleSheetsService: ⚠️ VERIFICĂRI NECESARE:");
                System.err.println("  1. Service Account-ul trebuie să aibă rolul 'Editor' în IAM & Admin → IAM");
                System.err.println("  2. Google Sheets API trebuie să fie activat în APIs & Services → Enabled APIs");
                System.err.println("  3. Google Drive API trebuie să fie activat în APIs & Services → Enabled APIs");
                System.err.println("  4. Verifică că proiectul din credentials.json este corect");
                throw e;
            }
        } catch (Exception e) {
            System.err.println("GoogleSheetsService: ❌ EROARE NEAȘTEPTATĂ: " + e.getClass().getName());
            System.err.println("  Message: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Eroare la crearea Google Sheet: " + e.getMessage(), e);
        }
    }
    
    /**
     * Pregătește datele pentru inserare în Google Sheets
     */
    private List<List<Object>> prepareDataForSheets(List<TaggingRow> taggingRows) {
        List<List<Object>> values = new ArrayList<>();
        
        // Header
        List<Object> header = new ArrayList<>();
        header.add("Event Name");
        header.add("Event Category");
        header.add("Event Description");
        header.add("Event Location");
        header.add("Property Group");
        header.add("Property Label");
        header.add("Property Name");
        header.add("Property Definition");
        header.add("Data Type");
        header.add("Possible Values");
        header.add("Code Examples");
        header.add("DATA LAYER STATUS");
        header.add("STATUS GA4");
        values.add(header);
        
        // Date
        for (TaggingRow row : taggingRows) {
            List<Object> rowData = new ArrayList<>();
            rowData.add(row.getEventName() != null ? row.getEventName() : "");
            rowData.add(row.getEventCategory() != null ? row.getEventCategory() : "");
            rowData.add(row.getEventDescription() != null ? row.getEventDescription() : "");
            rowData.add(row.getEventLocation() != null ? row.getEventLocation() : "");
            rowData.add(row.getPropertyGroup() != null ? row.getPropertyGroup() : "");
            rowData.add(row.getPropertyLabel() != null ? row.getPropertyLabel() : "");
            rowData.add(row.getPropertyName() != null ? row.getPropertyName() : "");
            rowData.add(row.getPropertyDefinition() != null ? row.getPropertyDefinition() : "");
            rowData.add(row.getDataType() != null ? row.getDataType() : "");
            rowData.add(row.getPossibleValues() != null ? row.getPossibleValues() : "");
            rowData.add(row.getCodeExamples() != null ? row.getCodeExamples() : "");
            rowData.add(row.getDataLayerStatus() != null ? row.getDataLayerStatus() : "");
            rowData.add(row.getStatusGA4() != null ? row.getStatusGA4() : "");
            values.add(rowData);
        }
        
        return values;
    }
    
    /**
     * Formatează header-ul pentru a arăta mai bine
     */
    private void formatHeader(String spreadsheetId) throws IOException {
        // Creează request pentru formatare
        List<Request> requests = new ArrayList<>();
        
        // Formatare header (primul rând)
        Request headerFormatRequest = new Request()
            .setRepeatCell(new RepeatCellRequest()
                .setRange(new GridRange()
                    .setSheetId(0)
                    .setStartRowIndex(0)
                    .setEndRowIndex(1))
                .setCell(new CellData()
                    .setUserEnteredFormat(new CellFormat()
                        .setBackgroundColor(new Color()
                            .setRed(0.2f)
                            .setGreen(0.4f)
                            .setBlue(0.8f))
                        .setTextFormat(new TextFormat()
                            .setForegroundColor(new Color()
                                .setRed(1.0f)
                                .setGreen(1.0f)
                                .setBlue(1.0f))
                            .setBold(true))))
                .setFields("userEnteredFormat(backgroundColor,textFormat)"));
        
        requests.add(headerFormatRequest);
        
        // Aplică formatarea
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
            .setRequests(requests);
        
        sheetsService.spreadsheets()
            .batchUpdate(spreadsheetId, batchUpdateRequest)
            .execute();
    }
    
    /**
     * Generează un URL pentru Google Sheets cu datele din tagging plan (metodă veche - păstrată pentru compatibilitate)
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
     * Escapă caracterele speciale pentru CSV
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        // Înlocuiește newline-urile cu spații pentru a rămâne pe o singură linie în Google Sheets
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }
}
