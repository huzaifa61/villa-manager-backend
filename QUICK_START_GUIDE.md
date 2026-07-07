# Villa Manager Backend - Export & Branding Quick Start Guide

## 🚀 Getting Started

### Step 1: Update Dependencies
```bash
cd villa-manager-backend
mvn clean install
```

### Step 2: Configure Environment
Create or update your environment variables:
```bash
export LOGO_URL="https://your-company-logo-url"
export TEMP_DIR="/tmp/villa-exports"
```

Or add to `.env` file:
```
LOGO_URL=https://your-company-logo-url
TEMP_DIR=/tmp/villa-exports
```

### Step 3: Start the Application
```bash
mvn spring-boot:run
```

Or build and run:
```bash
mvn clean package
java -jar target/villa-manager-backend-1.0.0.jar
```

---

## 📊 Export Examples

### Export Apartments to Excel
```bash
curl -X GET "http://localhost:8080/api/v1/villas/1/apartments/export-excel" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o apartments.xlsx
```

### Export Expenses to PDF
```bash
curl -X GET "http://localhost:8080/api/v1/villas/1/expenses/export-pdf" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o expenses_report.pdf
```

### Export Payments to CSV
```bash
curl -X GET "http://localhost:8080/api/v1/villas/1/payments/export" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -o payments.csv
```

---

## 🎨 Branding Configuration

### Get App Branding
```bash
curl -X GET "http://localhost:8080/api/v1/app/branding" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response**:
```json
{
  "success": true,
  "message": "Branding information retrieved successfully",
  "data": {
    "appName": "Villa Manager Pro",
    "appVersion": "1.0.0",
    "logoUrl": "https://...",
    "companyName": "Villa Manager",
    "supportEmail": "support@villamanager.com",
    "phoneNumber": "+971 4 XXX XXXX",
    "address": "Dubai, UAE"
  }
}
```

### Get Export Info
```bash
curl -X GET "http://localhost:8080/api/v1/app/export-info" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Response**:
```json
{
  "success": true,
  "data": {
    "appName": "Villa Manager Pro",
    "appVersion": "1.0.0",
    "maxRecords": 10000,
    "supportedFormats": ["csv", "xlsx", "pdf"]
  }
}
```

---

## 📝 Frontend Integration

### React/TypeScript Example
```typescript
// exportService.ts
export async function exportApartmentsToExcel(villaId: number, token: string) {
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
  window.URL.revokeObjectURL(url);
}

export async function getBrandingInfo(token: string) {
  const response = await fetch('/api/v1/app/branding', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return response.json();
}
```

### Usage in React Component
```jsx
import { exportApartmentsToExcel, getBrandingInfo } from './exportService';

function ApartmentsScreen() {
  const handleExport = async () => {
    await exportApartmentsToExcel(villaId, token);
  };

  const handleGetBranding = async () => {
    const info = await getBrandingInfo(token);
    console.log(info.data);
  };

  return (
    <div>
      <button onClick={handleExport}>Export to Excel</button>
      <button onClick={handleGetBranding}>Get App Info</button>
    </div>
  );
}
```

---

## 🔧 Configuration Options

### application.yml (All Options)
```yaml
app:
  branding:
    appName: Villa Manager Pro          # App display name
    appVersion: 1.0.0                   # Current version
    logoUrl: https://...                # Company logo URL
    companyName: Villa Manager          # Company name (appears in PDFs)
    supportEmail: support@...           # Support contact email
    phoneNumber: +971 4 XXX XXXX       # Support phone
    address: Dubai, UAE                 # Company address (appears in PDFs)
  
  export:
    maxRecords: 10000                   # Max records to export
    chunkSize: 500                      # Batch processing size
    tempDir: /tmp/villa-exports        # Temporary file storage
```

---

## 📂 File Structure

```
villa-manager-backend/
├── EXPORT_API_DOCUMENTATION.md      ← Full API documentation
├── IMPLEMENTATION_SUMMARY.md         ← Implementation details
├── QUICK_START_GUIDE.md             ← This file
├── pom.xml                          ← Updated dependencies
├── src/main/resources/
│   └── application.yml              ← Updated configuration
└── src/main/java/com/villamanager/
    ├── config/
    │   ├── BrandingProperties.java  (NEW)
    │   └── ExportProperties.java    (NEW)
    ├── service/
    │   └── ExportService.java       (NEW)
    ├── controller/
    │   ├── BrandingController.java  (NEW)
    │   ├── ApartmentController.java (UPDATED)
    │   ├── ExpenseController.java   (UPDATED)
    │   └── PaymentController.java   (UPDATED)
    └── util/
        ├── ExcelExportUtil.java     (NEW)
        ├── PdfExportUtil.java       (NEW)
        └── CsvExportUtil.java       (existing)
```

---

## ✅ Verification Checklist

- [ ] Maven build successful: `mvn clean package`
- [ ] All dependencies resolved
- [ ] Application starts without errors
- [ ] BrandingController accessible
- [ ] Branding endpoint returns data: `GET /v1/app/branding`
- [ ] Export endpoints accessible: `GET /v1/villas/1/apartments/export-excel`
- [ ] Temp directory exists and is writable
- [ ] Logs show no errors

---

## 🐛 Troubleshooting

### Build Failures
```bash
# Clear Maven cache and rebuild
mvn clean -DskipTests install
```

### Permission Denied (Temp Directory)
```bash
# Ensure temp directory exists and is writable
mkdir -p /tmp/villa-exports
chmod 777 /tmp/villa-exports
```

### Excel Export Not Working
```bash
# Check if POI libraries are loaded
mvn dependency:tree | grep poi
```

### PDF Generation Errors
```bash
# Verify iText dependency
mvn dependency:tree | grep itextpdf
```

### 403 Forbidden on Exports
```bash
# Check token validity and villa access permissions
# Verify user has required permissions
```

---

## 📚 Additional Documentation

- **Full API Documentation**: See `EXPORT_API_DOCUMENTATION.md`
- **Implementation Details**: See `IMPLEMENTATION_SUMMARY.md`
- **Backend README**: See `README.md`

---

## 🎯 Common Tasks

### Export All Apartments for a Villa
```bash
curl "http://localhost:8080/api/v1/villas/1/apartments/export-excel" \
  -H "Authorization: Bearer TOKEN" -o apartments.xlsx
```

### Get Branding for Frontend
```javascript
fetch('/api/v1/app/branding', {
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(r => r.json())
.then(data => console.log(data.data.logoUrl))
```

### Configure Custom Logo
```bash
export LOGO_URL="https://my-cdn.com/villa-manager-logo.png"
```

### Change Max Export Records
Update `application.yml`:
```yaml
app:
  export:
    maxRecords: 50000  # Increased limit
```

---

## 🚀 Production Deployment

### Docker Environment Variables
```dockerfile
ENV LOGO_URL=https://production-cdn.com/logo.png
ENV TEMP_DIR=/var/tmp/villa-exports
ENV DB_HOST=production-db.example.com
```

### Kubernetes ConfigMap
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: villa-manager-config
data:
  LOGO_URL: "https://cdn.example.com/logo.png"
  TEMP_DIR: "/tmp/villa-exports"
  APP_EXPORT_MAX_RECORDS: "10000"
```

---

## 📞 Support

For issues or questions:
1. Check `EXPORT_API_DOCUMENTATION.md`
2. Review logs: `tail -f logs/villa-manager.log`
3. Verify configuration in `application.yml`
4. Check Maven dependencies: `mvn dependency:tree`

---

## ⚡ Performance Tips

- **Large Exports**: Use chunking for > 5,000 records
- **PDF Generation**: Takes longer than Excel, consider async
- **Concurrent Requests**: Each export uses separate memory
- **File Storage**: Ensure `/tmp` has sufficient disk space

---

Last Updated: 2026-07-08
Version: 1.0.0
