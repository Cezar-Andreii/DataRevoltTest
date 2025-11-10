# Configurarea Google Sheets API

## Pas cu Pas pentru Obținerea Credențialelor

### 1. Creează un Proiect Google Cloud

1. **Accesează Google Cloud Console**: https://console.cloud.google.com/
2. **Creează un proiect nou** sau selectează unul existent
3. **Notează Project ID-ul** - îl vei avea nevoie mai târziu

### 2. Activează Google Sheets API și Google Drive API

**IMPORTANT:** Trebuie să activezi AMBELE API-uri!

#### Metoda 1: Prin Google Cloud Console (RECOMANDAT)

1. **Mergi la Google Cloud Console**: https://console.cloud.google.com/
2. **Selectează proiectul tău** (în dropdown-ul de sus)
3. **Mergi la "APIs & Services" → "Library"** (sau "Bibliotecă API")
4. **Activează Google Sheets API**:
   - Caută "Google Sheets API"
   - Click pe rezultatul respectiv
   - Click butonul **"ENABLE"** (sau "ACTIVEAZĂ")
5. **Activează Google Drive API**:
   - Caută "Google Drive API"
   - Click pe rezultatul respectiv
   - Click butonul **"ENABLE"** (sau "ACTIVEAZĂ")

#### Metoda 2: Prin gcloud CLI (dacă ai instalat Google Cloud SDK)

```bash
# Autentifică-te
gcloud auth login

# Selectează proiectul
gcloud config set project YOUR_PROJECT_ID

# Activează API-urile
gcloud services enable sheets.googleapis.com
gcloud services enable drive.googleapis.com
```

**⚠️ Dacă nu activezi aceste API-uri, vei primi eroarea "PERMISSION_DENIED"!**
Vezi `FIX_PERMISSION_DENIED.md` pentru detalii complete.

### 3. Creează Service Account

1. **Mergi la "IAM & Admin" → "Service Accounts"** (sau "APIs & Services" → "Credentials" → "Create Credentials" → "Service Account")
2. **Click "Create Service Account"**
3. **Completează detaliile**:
   - Service account name: `tagging-plan-generator`
   - Service account ID: `tagging-plan-generator`
   - Description: `Service account for Tagging Plan Google Sheets integration`
4. **Click "Create and Continue"**
5. **Grant access (OPTIONAL):** Poți să dai rolul "Editor" aici, SAU îl vei adăuga mai târziu
6. **Click "Done"**

**IMPORTANT - După creare, verifică permisiunile:**
1. **Mergi la "IAM & Admin" → "IAM"**
2. **Caută Service Account-ul creat** (email-ul arată ca: `tagging-plan-generator@your-project.iam.gserviceaccount.com`)
3. **Dacă nu are rolul "Editor":**
   - Click pe iconița de edit (✏️)
   - Adaugă rolul **"Editor"**
   - Click "Save"

**⚠️ Fără rolul "Editor", Service Account-ul nu va putea crea sheet-uri!**

### 4. Generează Cheia JSON

1. **În lista de Service Accounts**, click pe cel creat
2. **Mergi la tab-ul "Keys"**
3. **Click "Add Key" → "Create new key"**
4. **Selectează "JSON"** și click "Create"
5. **Fișierul JSON se va descărca automat**

### 5. Plasează Fișierul în Proiect

1. **Redenumește fișierul** descărcat în `credentials.json`
2. **Mută fișierul** în `src/main/resources/credentials.json`
3. **Verifică că structura** arată ca în `credentials.json.example`

### 6. Testează Implementarea

1. **Pornește aplicația**: `mvn spring-boot:run`
2. **Accesează**: `http://localhost:8080/tagging`
3. **Generează datele** selectând evenimente
4. **Click "Creează Google Sheet"**
5. **Ar trebui să te redirecționeze** către un Google Sheet nou cu datele tale!

## Structura Fișierului credentials.json

```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "your-private-key-id",
  "private_key": "-----BEGIN PRIVATE KEY-----\nYOUR_PRIVATE_KEY_HERE\n-----END PRIVATE KEY-----\n",
  "client_email": "your-service-account@your-project-id.iam.gserviceaccount.com",
  "client_id": "your-client-id",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/your-service-account%40your-project-id.iam.gserviceaccount.com"
}
```

## Funcționalități Implementate

✅ **Creare automată Google Sheets** cu datele din tagging plan  
✅ **Formatare profesională** - header-uri colorate și bold  
✅ **Gestionare erori** - mesaje clare în caz de probleme  
✅ **Integrare completă** - un click și ai sheet-ul gata  

## Depanare

### Eroare: "File not found: credentials.json"
- Verifică că fișierul `credentials.json` este în `src/main/resources/`
- Verifică că numele fișierului este exact `credentials.json`

### Eroare: "Invalid credentials"
- Verifică că JSON-ul este valid
- Verifică că toate câmpurile sunt completate corect
- Verifică că Google Sheets API este activat în Google Cloud Console

### Eroare: "Permission denied" sau "PERMISSION_DENIED"

**Aceasta este cea mai comună eroare!** Vezi ghidul complet: `FIX_PERMISSION_DENIED.md`

**Soluții rapide:**
1. **Verifică că API-urile sunt activate:**
   - Google Cloud Console → APIs & Services → Enabled APIs
   - Ar trebui să vezi: ✅ Google Sheets API și ✅ Google Drive API

2. **Verifică permisiunile Service Account-ului:**
   - IAM & Admin → IAM
   - Service Account-ul trebuie să aibă rolul **"Editor"**

3. **Verifică că credentials.json este valid:**
   - Conține `"type": "service_account"`
   - `client_email` corespunde cu Service Account-ul din Google Cloud

**Pas cu pas complet:** Vezi `FIX_PERMISSION_DENIED.md`

