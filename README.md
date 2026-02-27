# Instant Wellness Kits — Backend

A Spring Boot REST API that processes wellness kit orders, calculates New York State sales tax by geolocation, and provides order management and statistics.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.3 |
| Database | MongoDB Atlas |
| Auth | JWT + Google OAuth2 |
| Geolocation | JTS (Java Topology Suite) + GeoJSON |
| Tax data | New York State county tax CSV |
| CSV processing | Apache Commons CSV |
| Build | Maven |

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **MongoDB Atlas** cluster (or local MongoDB instance)
- **Google Cloud Console** project with OAuth2 credentials
- A **PKCS12 keystore** for HTTPS (`.p12` file)

---

## Environment Variables

Set these before running the application:

| Variable | Description | Example |
|----------|-------------|---------|
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/iwk?appName=Cluster0` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | `123456789-abc.apps.googleusercontent.com` |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret | `GOCSPX-...` |
| `KEYSTORE_PASSWORD` | Password for the PKCS12 keystore | `changeit` |

### Setting environment variables

**Windows (PowerShell):**
```powershell
$env:MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/iwk"
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
$env:KEYSTORE_PASSWORD="your-keystore-password"
```

**Linux / macOS:**
```bash
export MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/iwk"
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
export KEYSTORE_PASSWORD="your-keystore-password"
```

**.env file (IntelliJ Run Configuration):**  
Go to **Run → Edit Configurations → Environment variables** and add all four variables.

---

## SSL Keystore Setup

The server runs on HTTPS (port `8443`). You need a PKCS12 keystore at `src/main/resources/keystore.p12`.

Generate a self-signed certificate for development:
```bash
keytool -genkeypair -alias mycert -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
  -validity 3650 -storepass your-keystore-password
```

---

## Build & Run

### Clone and build
```bash
git clone <repo-url>
cd instant-wellness-kits-backend
./mvnw clean package -DskipTests
```

### Run with Maven
```bash
./mvnw spring-boot:run
```

### Run the JAR
```bash
java -jar target/instant-wellness-kits-backend-0.0.1-SNAPSHOT.jar
```

The server starts at: **`https://localhost:8443`**

---

## Default Admin User

On first startup, an admin user is automatically created:

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin` |

> ⚠️ Change the password immediately in production.

---

## Initial Data Setup

After the server is running, import the tax jurisdiction data before importing orders.

### 1. Obtain a JWT token
```http
POST https://localhost:8443/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```
Response:
```json
{ "token": "eyJhbGci..." }
```

Use this token as a Bearer header for all subsequent requests:
```
Authorization: Bearer eyJhbGci...
```

### 2. Import New York tax jurisdictions
```http
POST https://localhost:8443/jurisdictions/import
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: New_York_Taxes.csv
```
> The `New_York_Taxes.csv` file is located at `src/main/resources/New_York_Taxes.csv`.

---

## API Endpoints

All endpoints (except auth) require `Authorization: Bearer <token>`.

### Authentication

| Method | Path | Description | Auth required |
|--------|------|-------------|---------------|
| `POST` | `/api/auth/login` | Login with username/password, returns JWT | No |
| `GET` | `/api/auth/login/oauth2/google` | Initiate Google OAuth2 login | No |

### Orders

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/orders/import` | Upload a CSV file of orders, calculates tax and saves to DB |
| `GET` | `/orders/stats` | Returns total orders, total revenue, total tax |
| `GET` | `/orders` | List all orders (Spring Data REST HAL) |
| `GET` | `/orders/{id}` | Get a single order by ID |

#### `POST /orders/import`

- **Content-Type:** `multipart/form-data`
- **Param:** `file` — CSV file with columns: `id, longitude, latitude, timestamp, subtotal`

**Response:**
```json
{
  "importedCount": 11222,
  "unsupportedCount": 0,
  "unsupportedOrders": [],
  "resultCsv": "<base64-encoded CSV with tax columns>"
}
```

The `resultCsv` field is a Base64-encoded CSV with columns:  
`id, longitude, latitude, timestamp, subtotal, tax, total`

**Unsupported orders** (outside supported jurisdictions) are listed in `unsupportedOrders`:
```json
{
  "id": "42",
  "longitude": -75.485,
  "latitude": 44.492,
  "timestamp": "2025-11-04 10:17:04.915",
  "subtotal": 120.0,
  "reason": "No jurisdiction found for lat=44.492, lon=-75.485"
}
```

#### `GET /orders/stats`

**Response:**
```json
{
  "totalOrders": 11222,
  "totalRevenue": 985430.00,
  "totalTax": 86517.50
}
```

### Jurisdictions

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/jurisdictions/import` | Upload `New_York_Taxes.csv` to seed tax data |
| `GET` | `/jurisdictions` | List all jurisdictions (Spring Data REST HAL) |

---

## Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → **APIs & Services** → **Credentials** → **Create OAuth 2.0 Client ID**
3. Application type: **Web application**
4. Add **Authorized redirect URIs**:
   ```
   https://localhost:8443/login/oauth2/code/google
   ```
5. Copy the **Client ID** and **Client Secret** into the `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` env variables

**OAuth2 login flow:**
1. Open in browser: `https://localhost:8443/api/auth/login/oauth2/google`
2. Complete Google sign-in
3. On success, you are redirected back and receive a JWT token

---

## Project Structure

```
src/main/java/ua/trinity/iwk/backend/
├── auth/                          # JWT & OAuth2 authentication
│   ├── AuthController.java        # POST /api/auth/login
│   ├── SecurityConfig.java        # Spring Security filter chain
│   ├── JwtService.java            # JWT generation & validation
│   ├── JwtAuthenticationFilter.java
│   ├── CustomOAuth2UserService.java
│   ├── OAuth2AuthenticationSuccessHandler.java
│   ├── AdminInitializer.java      # Creates default admin on startup
│   └── User / UserRepository
├── tax/
│   ├── OrderTaxController.java    # /orders/import, /orders/stats
│   ├── TaxService.java            # CSV processing & bulk DB insert
│   ├── StatsService.java          # MongoDB aggregation for stats
│   ├── jurisdictions/
│   │   ├── entity/                # Jurisdiction, Breakdown
│   │   ├── ingestion/             # POST /jurisdictions/import
│   │   └── util/                  # GeoJSON point-in-polygon lookup
│   └── order/
│       ├── Order.java
│       ├── TaxDetails.java
│       └── OrderRepository.java
└── InstantWellnessKitsBackendApplication.java

src/main/resources/
├── application.properties
├── Counties.geojson               # NY county polygons for geolocation
├── New_York_Taxes.csv             # NY county tax rates
└── keystore.p12                   # SSL certificate (not committed)
```

---

## Input CSV Format

For `POST /orders/import`, the CSV must have these columns:

```csv
id,longitude,latitude,timestamp,subtotal
1,-78.867,42.012,2025-11-04 10:17:04.915257248,120.0
2,-76.265,42.478,2025-11-04 22:20:08.135761513,120.0
```

| Column | Type | Description |
|--------|------|-------------|
| `id` | string/number | Unique order ID (omit or leave empty for auto-generation) |
| `longitude` | double | WGS84 longitude |
| `latitude` | double | WGS84 latitude |
| `timestamp` | string | Datetime string |
| `subtotal` | double | Order subtotal in USD |

