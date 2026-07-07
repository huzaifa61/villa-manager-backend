# Villa Manager Backend - Export & Branding Documentation

## Overview
The Villa Manager Backend now includes comprehensive export functionality for CSV, Excel, and PDF formats, along with branding and app configuration endpoints.

## Table of Contents
1. [Dependencies Added](#dependencies-added)
2. [Configuration](#configuration)
3. [Export Endpoints](#export-endpoints)
4. [Branding & App Endpoints](#branding--app-endpoints)
5. [Utilities](#utilities)
6. [Usage Examples](#usage-examples)

---

## Dependencies Added

### pom.xml
```xml
<!-- Excel Export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.3</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>

<!-- PDF Export -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>

<!-- CSV Export -->
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.8</version>
</dependency>
```

---

## Configuration

### application.yml

```yaml
app:
  branding:
    appName: Villa Manager Pro
    appVersion: 1.0.0
    logoUrl: ${LOGO_URL:https://via.placeholder.com/200x200?text=Villa+Manager}
    companyName: Villa Manager
    supportEmail: support@villamanager.com
    phoneNumber: +971 4 XXX XXXX
    address: Dubai, UAE
  
  export:
    maxRecords: 10000
    chunkSize: 500
    tempDir: ${TEMP_DIR:/tmp/villa-exports}
```

### Configuration Classes

#### BrandingProperties.java
```java
@Component
@ConfigurationProperties(prefix = "app.branding")
@Data
public class BrandingProperties {
    private String appName;
    private String appVersion;
    private String logoUrl;
    private String companyName;
    private String supportEmail;
    private String phoneNumber;
    private String address;
}
```

#### ExportProperties.java
```java
@Component
@ConfigurationProperties(prefix = "app.export")
@Data
public class ExportProperties {
    private Integer maxRecords = 10000;
    private Integer chunkSize = 500;
    private String tempDir = "/tmp/villa-exports";
}
```

---

## Export Endpoints

### Apartments

#### CSV Export
```
GET /api/v1/villas/{villaId}/apartments/export
Content-Type: text/csv
Response: CSV file with apartments data
```

#### Excel Export
```
GET /api/v1/villas/{villaId}/apartments/export-excel
Response: XLSX file with formatted apartments data
Headers: ID, Apartment, Owner, Tenant, Phone, Status, Current Balance, Type
```

#### PDF Export
```
GET /api/v1/villas/{villaId}/apartments/export-pdf
Response: PDF file with apartments report
Includes: Company branding, title, table, and footer with contact info
```

### Expenses

#### CSV Export
```
GET /api/v1/villas/{villaId}/expenses/export
Content-Type: text/csv
Response: CSV file with expenses data
```

#### Excel Export
```
GET /api/v1/villas/{villaId}/expenses/export-excel
Response: XLSX file with formatted expenses data
Headers: ID, Apartment, Category, Description, Amount, Expense Date
```

#### PDF Export
```
GET /api/v1/villas/{villaId}/expenses/export-pdf
Response: PDF file with expenses report
Includes: Company branding, title, table, and footer with contact info
```

### Payments

#### CSV Export
```
GET /api/v1/villas/{villaId}/payments/export
Content-Type: text/csv
Response: CSV file with payments data
```

#### Excel Export
```
GET /api/v1/villas/{villaId}/payments/export-excel
Response: XLSX file with formatted payments data
Headers: ID, Apartment, Amount, Payment Date, Method, Status
```

#### PDF Export
```
GET /api/v1/villas/{villaId}/payments/export-pdf
Response: PDF file with payments report
Includes: Company branding, title, table, and footer with contact info
```

---

## Branding & App Endpoints

### Get Branding Information
```
GET /api/v1/app/branding

Response:
{
  "success": true,
  "message": "Branding information retrieved successfully",
  "data": {
    "appName": "Villa Manager Pro",
    "appVersion": "1.0.0",
    "logoUrl": "https://via.placeholder.com/200x200?text=Villa+Manager",
    "companyName": "Villa Manager",
    "supportEmail": "support@villamanager.com",
    "phoneNumber": "+971 4 XXX XXXX",
    "address": "Dubai, UAE"
  }
}
```

### Get App Information
```
GET /api/v1/app/info

Response:
{
  "success": true,
  "message": "App information retrieved successfully",
  "data": {
    "appName": "Villa Manager Pro",
    "appVersion": "1.0.0",
    "companyName": "Villa Manager",
    "exportInfo": {
      "appName": "Villa Manager Pro",
      "appVersion": "1.0.0",
      "maxRecords": 10000,
      "supportedFormats": ["csv", "xlsx", "pdf"]
    }
  }
}
```

### Get Export Information
```
GET /api/v1/app/export-info

Response:
{
  "success": true,
  "message": "Export information retrieved successfully",
  "data": {
    "appName": "Villa Manager Pro",
    "appVersion": "1.0.0",
    "maxRecords": 10000,
    "supportedFormats": ["csv", "xlsx", "pdf"]
  }
}
```

---

## Utilities

### ExcelExportUtil.java
Handles Excel file generation with:
- Formatted headers (blue background, white text, bold)
- Data styling (borders, alignment)
- Auto-sizing columns
- Type-safe cell value handling (String, Integer, Double, BigDecimal, Date, Boolean)

### PdfExportUtil.java
Handles PDF file generation with:
- Company branding header
- Document title
- Data table with proper formatting
- Footer with contact information
- Font styling (Title, Bold, Regular, Small)

### CsvExportUtil.java (Existing)
Handles CSV file generation with proper escaping and formatting

---

## Usage Examples

### cURL Examples

#### Export Apartments to Excel
```bash
curl -X GET "http://localhost:8080/api/v1/villas/1/apartments/export-excel" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o apartments.xlsx
```

#### Export Expenses to PDF
```bash
curl -X GET "http://localhost:8080/api/v1/villas/1/expenses/export-pdf" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o expenses_report.pdf
```

#### Get App Branding
```bash
curl -X GET "http://localhost:8080/api/v1/app/branding" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Frontend Integration (JavaScript/Fetch)

```javascript
// Export apartments to Excel
async function exportApartmentsExcel(villaId, token) {
  const response = await fetch(
    `/api/v1/villas/${villaId}/apartments/export-excel`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `apartments_${new Date().getTime()}.xlsx`;
  a.click();
}

// Get branding info
async function getBrandingInfo(token) {
  const response = await fetch('/api/v1/app/branding', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
}
```

---

## File Structure

```
villa-manager-backend/
├── src/main/java/com/villamanager/
│   ├── config/
│   │   ├── BrandingProperties.java (NEW)
│   │   └── ExportProperties.java (NEW)
│   ├── controller/
│   │   ├── ApartmentController.java (UPDATED - added export endpoints)
│   │   ├── ExpenseController.java (UPDATED - added export endpoints)
│   │   ├── PaymentController.java (UPDATED - added export endpoints)
│   │   └── BrandingController.java (NEW)
│   ├── service/
│   │   └── ExportService.java (NEW)
│   └── util/
│       ├── ExcelExportUtil.java (NEW)
│       ├── PdfExportUtil.java (NEW)
│       └── CsvExportUtil.java (EXISTING)
├── src/main/resources/
│   └── application.yml (UPDATED - added branding & export config)
└── pom.xml (UPDATED - added dependencies)
```

---

## Response Headers

All export endpoints include proper response headers:
- `Content-Type`: Appropriate MIME type (text/csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/pdf)
- `Content-Disposition`: Attachment with timestamp (e.g., `apartments_20240708_143522.xlsx`)

---

## Error Handling

All export endpoints include:
- Access control validation via `AccessControlService`
- Exception handling with proper error messages
- Logging for debugging

---

## Environment Variables

Configure these in your `.env` or deployment environment:

```
LOGO_URL=https://your-logo-url
TEMP_DIR=/path/to/temp/directory
DB_HOST=your-db-host
DB_PORT=5432
DB_NAME=your-db-name
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
```

---

## Notes

1. **Max Records**: Default is 10,000 records. Adjust `app.export.maxRecords` in `application.yml`
2. **Chunk Size**: Used for batch processing. Default is 500 records
3. **Temp Directory**: Ensure write permissions for the temp directory
4. **PDF Generation**: PDFs include company branding automatically
5. **Excel Formatting**: Automatically formats numbers with 2 decimal places
6. **Date/Time**: Exported as ISO format (YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss)

---

## Future Enhancements

- [ ] Scheduled exports via email
- [ ] Bulk export (multiple reports at once)
- [ ] Custom column selection for exports
- [ ] Export templates
- [ ] Export history tracking
- [ ] Async export for large datasets

