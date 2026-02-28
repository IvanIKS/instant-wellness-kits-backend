# Instant Wellness Kits — Backend

Spring Boot REST API for processing wellness kit orders with New York State sales tax calculation by geolocation.

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MongoDB Atlas account (or local MongoDB)
- Google Cloud OAuth2 credentials
- A PKCS12 keystore for HTTPS

---

## Environment Variables

| Variable | Description |
|----------|-------------|
| `MONGODB_URI` | MongoDB connection string, e.g. `mongodb+srv://user:pass@cluster.mongodb.net/iwk` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `FRONTEND_URL` | Frontend origin, e.g. `https://wellness-kit-frontend.vercel.app` |

**PowerShell:**
```powershell
$env:MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/iwk"
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
$env:FRONTEND_URL="http://localhost:5173"
```

**Linux / macOS:**
```bash
export MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/iwk"
export GOOGLE_CLIENT_ID="your-client-id"
export GOOGLE_CLIENT_SECRET="your-client-secret"
export FRONTEND_URL="http://localhost:5173"
```

---

## SSL Keystore

The server runs on HTTPS (port `8443`). Generate a self-signed certificate for local development:

```bash
keytool -genkeypair -alias mycert -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
  -validity 3650 -storepass your-password
```

---

## Run
Java 17 is required
```bash
./mvnw spring-boot:run
```

The server starts at **`https://localhost:8443`**.

---

## Google OAuth2 Setup

1. Open [Google Cloud Console](https://console.cloud.google.com/) → **APIs & Services** → **Credentials** → **Create OAuth 2.0 Client ID**
2. Application type: **Web application**
3. Add **Authorized redirect URI**: `http://localhost:5173/login`
4. Copy **Client ID** and **Client Secret** to the env variables above

---

## Initial Data Setup

### 1. Get a JWT token

```http
POST https://localhost:8443/api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "admin" }
```

Use the returned token as `Authorization: Bearer <token>` in all subsequent requests.

### 2. Import tax jurisdictions

```http
POST https://localhost:8443/jurisdictions/import
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: New_York_Taxes.csv   (located in src/main/resources/)
```

### 3. Import orders

```http
POST https://localhost:8443/orders/import
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: your-orders.csv
```

CSV format:
```
id,longitude,latitude,timestamp,subtotal
1,-78.867,42.012,2025-11-04 10:17:04,120.0
```

`id` is optional — leave it empty or omit it for auto-generation.

---

## API Reference

### Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/auth/login` | No | Login, returns JWT |
| `GET` | `/api/auth/login/oauth2/google` | No | Start Google OAuth2 flow |

### Orders

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/orders/import` | Upload CSV, calculate tax, save to DB |
| `GET` | `/orders/stats` | Total orders, revenue, tax |
| `GET` | `/orders` | List all orders |
| `GET` | `/orders/{id}` | Get order by ID |

### Jurisdictions

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/jurisdictions/import` | Import `New_York_Taxes.csv` |
| `DELETE` | `/jurisdictions` | Delete all jurisdictions |
| `GET` | `/jurisdictions` | List all jurisdictions |
