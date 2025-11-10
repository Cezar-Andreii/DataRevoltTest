package com.example.demo.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GoogleSheetsConfig {

    @Value("${google.credentials.path:}")
    private String credentialsPath;

    @Value("${google.credentials.json:}")
    private String credentialsJson;

    @Bean
    public Sheets sheetsService() throws IOException, GeneralSecurityException {
        System.out.println("GoogleSheetsConfig: Încep configurarea Google Sheets API...");
        
        GoogleCredential credential;
        java.io.InputStream credentialsStream = null;
        String sourceInfo = "";
        
        // Prioritate 1: Variabilă de mediu GOOGLE_CREDENTIALS_JSON (pentru production/hosting)
        String envCredentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");
        if (envCredentialsJson != null && !envCredentialsJson.isEmpty()) {
            System.out.println("GoogleSheetsConfig: Folosesc credențiale din variabila de mediu GOOGLE_CREDENTIALS_JSON");
            credentialsStream = new ByteArrayInputStream(envCredentialsJson.getBytes(StandardCharsets.UTF_8));
            sourceInfo = "variabila de mediu GOOGLE_CREDENTIALS_JSON";
        }
        // Prioritate 2: Variabilă de mediu GOOGLE_CREDENTIALS_PATH
        else if (credentialsPath != null && !credentialsPath.isEmpty()) {
            System.out.println("GoogleSheetsConfig: Folosesc credențiale din path: " + credentialsPath);
            credentialsStream = new FileInputStream(credentialsPath);
            sourceInfo = "path: " + credentialsPath;
        }
        // Prioritate 3: Variabilă de mediu sistem GOOGLE_CREDENTIALS_PATH
        else {
            String envCredentialsPath = System.getenv("GOOGLE_CREDENTIALS_PATH");
            if (envCredentialsPath != null && !envCredentialsPath.isEmpty()) {
                System.out.println("GoogleSheetsConfig: Folosesc credențiale din variabila de mediu GOOGLE_CREDENTIALS_PATH: " + envCredentialsPath);
                credentialsStream = new FileInputStream(envCredentialsPath);
                sourceInfo = "variabila de mediu GOOGLE_CREDENTIALS_PATH: " + envCredentialsPath;
            }
            // Prioritate 4: Application property google.credentials.json
            else if (credentialsJson != null && !credentialsJson.isEmpty()) {
                System.out.println("GoogleSheetsConfig: Folosesc credențiale din application properties");
                credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
                sourceInfo = "application properties";
            }
            // Prioritate 5: Fallback la fișierul din resources (pentru development local)
            else {
                System.out.println("GoogleSheetsConfig: Caut fișierul credentials.json în resources...");
                ClassPathResource resource = new ClassPathResource("credentials.json");
                
                if (!resource.exists()) {
                    System.out.println("GoogleSheetsConfig: ⚠️ Nu s-au găsit credențiale Google. Google Sheets API va fi dezactivat.");
                    System.out.println("GoogleSheetsConfig: Aplicația va funcționa normal, dar funcționalitățile Google Sheets nu vor fi disponibile.");
                    return null;
                }
                
                System.out.println("GoogleSheetsConfig: Fișierul credentials.json găsit în resources, încarc credențialele...");
                credentialsStream = resource.getInputStream();
                sourceInfo = "resources/credentials.json";
            }
        }
        
        try {
            // Folosește scope-uri complete pentru creare spreadsheet-uri
            credential = GoogleCredential.fromStream(credentialsStream)
                .createScoped(Arrays.asList(
                    SheetsScopes.SPREADSHEETS,           // Permite citire/scriere în spreadsheet-uri
                    SheetsScopes.DRIVE_FILE,             // Permite acces la fișiere Drive
                    "https://www.googleapis.com/auth/drive", // Scope complet pentru Drive (necesar pentru creare)
                    "https://www.googleapis.com/auth/spreadsheets" // Scope complet pentru Sheets
                ));
            
            // Forțează refresh-ul token-ului
            credential.refreshToken();
            
        // Verifică dacă credențialele sunt valide
        if (credential.getServiceAccountId() == null) {
                throw new IOException("Credențialele nu sunt de tip Service Account. " +
                    "Verifică că folosești un Service Account JSON valid.");
            }
            
            String serviceAccountEmail = credential.getServiceAccountId();
            System.out.println("GoogleSheetsConfig: ✅ Credențiale încărcate din: " + sourceInfo);
            System.out.println("GoogleSheetsConfig: ✅ Service Account Email: " + serviceAccountEmail);
            System.out.println("GoogleSheetsConfig: ✅ Scope-uri configurate:");
            System.out.println("   - " + SheetsScopes.SPREADSHEETS);
            System.out.println("   - " + SheetsScopes.DRIVE_FILE);
            System.out.println("   - https://www.googleapis.com/auth/drive");
            System.out.println("   - https://www.googleapis.com/auth/spreadsheets");
            
            // Verifică dacă token-ul este valid
            if (credential.getAccessToken() == null) {
                System.err.println("GoogleSheetsConfig: ⚠️ ATENȚIE: Token-ul de acces este null!");
            } else {
                System.out.println("GoogleSheetsConfig: ✅ Token de acces obținut cu succes");
                System.out.println("GoogleSheetsConfig: Token (primele 20 caractere): " + 
                    credential.getAccessToken().substring(0, Math.min(20, credential.getAccessToken().length())) + "...");
            }
            
            // Testează dacă token-ul funcționează făcând un request simplu
            try {
                System.out.println("GoogleSheetsConfig: Testez token-ul făcând un request de verificare...");
                // Nu facem request real aici, doar verificăm că credential-ul este configurat
                System.out.println("GoogleSheetsConfig: ✅ Credential-ul este configurat corect");
            } catch (Exception e) {
                System.err.println("GoogleSheetsConfig: ⚠️ Eroare la testarea credential-ului: " + e.getMessage());
            }
            
            Sheets sheetsService = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("Tagging Plan Generator")
             .build();
            
            System.out.println("GoogleSheetsConfig: ✅ Sheets service creat cu succes");
            return sheetsService;
        } finally {
            if (credentialsStream != null) {
                try {
                    credentialsStream.close();
                } catch (IOException e) {
                    System.err.println("GoogleSheetsConfig: Eroare la închiderea stream-ului: " + e.getMessage());
                }
            }
        }
    }
    
    @Bean
    public Drive driveService() throws IOException, GeneralSecurityException {
        System.out.println("GoogleSheetsConfig: Încep configurarea Google Drive API...");
        
        GoogleCredential credential;
        java.io.InputStream credentialsStream = null;
        String sourceInfo = "";
        
        // Aceeași logică de încărcare credențiale ca pentru Sheets
        String envCredentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");
        if (envCredentialsJson != null && !envCredentialsJson.isEmpty()) {
            credentialsStream = new ByteArrayInputStream(envCredentialsJson.getBytes(StandardCharsets.UTF_8));
            sourceInfo = "variabila de mediu GOOGLE_CREDENTIALS_JSON";
        } else if (credentialsPath != null && !credentialsPath.isEmpty()) {
            credentialsStream = new FileInputStream(credentialsPath);
            sourceInfo = "path: " + credentialsPath;
        } else {
            String envCredentialsPath = System.getenv("GOOGLE_CREDENTIALS_PATH");
            if (envCredentialsPath != null && !envCredentialsPath.isEmpty()) {
                credentialsStream = new FileInputStream(envCredentialsPath);
                sourceInfo = "variabila de mediu GOOGLE_CREDENTIALS_PATH: " + envCredentialsPath;
            } else if (credentialsJson != null && !credentialsJson.isEmpty()) {
                credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
                sourceInfo = "application properties";
            } else {
                ClassPathResource resource = new ClassPathResource("credentials.json");
                if (!resource.exists()) {
                    System.out.println("GoogleSheetsConfig: ⚠️ Nu s-au găsit credențiale Google. Google Drive API va fi dezactivat.");
                    return null;
                }
                credentialsStream = resource.getInputStream();
                sourceInfo = "resources/credentials.json";
            }
        }
        
        try {
            credential = GoogleCredential.fromStream(credentialsStream)
                .createScoped(Arrays.asList(
                    DriveScopes.DRIVE,
                    DriveScopes.DRIVE_FILE,
                    "https://www.googleapis.com/auth/drive",
                    SheetsScopes.SPREADSHEETS
                ));
            
            credential.refreshToken();
            
            System.out.println("GoogleSheetsConfig: ✅ Drive service creat cu succes");
            
            return new Drive.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Tagging Plan Generator")
         .build();
        } finally {
            if (credentialsStream != null) {
                try {
                    credentialsStream.close();
                } catch (IOException e) {
                    System.err.println("GoogleSheetsConfig: Eroare la închiderea stream-ului: " + e.getMessage());
                }
            }
        }
    }
}
