# ExecuteMe

Spring Boot backend for a modular trading execution platform.

## What It Does

- Approved Telegram users authenticate with Angel One through the Telegram login flow.
- The backend stores encrypted broker session tokens and never exposes them through APIs.
- Admin APIs monitor users, broker sessions, positions, and trade execution jobs.
- Trade requests create one execution job per broker-connected user.
- Async workers claim jobs with an atomic database update before calling the broker abstraction.
- Angel One orders are sent with raw SmartAPI HTTP requests from the broker module, not through the Angel One SDK.

## Architecture

The backend is a modular monolith under `com.executeme`:

```text
admin
auth
broker
execution
notifications
positions
telegram
users
common
```

The execution module depends on the broker abstraction, not Angel One implementation classes. Angel One-specific login/token code remains inside `broker.angelone`.

## Configuration

The app imports a root `.env` file automatically through Spring Boot config import:

```yaml
spring.config.import: optional:file:.env[.properties]
```

Important variables:

```env
SMARTAPI_API_KEY=...
SMARTAPI_REDIRECT_URL=https://yourdomain.com/broker/angel/callback
SMARTAPI_CLIENT_LOCAL_IP=...
SMARTAPI_CLIENT_PUBLIC_IP=...
SMARTAPI_MAC_ADDRESS=...
JWT_SIGNING_SECRET=...
TOKEN_ENCRYPTION_SECRET=...
TELEGRAM_BOT_USERNAME=...
TELEGRAM_BOT_TOKEN=...
TELEGRAM_ALLOWED_USER_IDS=987654321,123456789
ADMIN_USERNAME=...
ADMIN_PASSWORD=...
```

Use PostgreSQL in production:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/executeme
DATABASE_USERNAME=executeme
DATABASE_PASSWORD=...
```

The app falls back to an in-memory H2 database for local smoke testing.

## Run

```bash
mvn spring-boot:run
```

## Current Admin APIs

Admin endpoints use HTTP Basic authentication when `ADMIN_USERNAME` and `ADMIN_PASSWORD` are configured.

```text
GET  /admin/broker-sessions
GET  /admin/telegram-users
GET  /admin/positions
POST /admin/trades/execute
GET  /admin/trades/{tradeRequestId}
GET  /admin/trades/{tradeRequestId}/executions
GET  /admin/trades/{tradeRequestId}/summary
```

Example trade request:

```json
{
  "symbol": "RELIANCE-EQ",
  "symbolToken": "2885",
  "exchange": "NSE",
  "variety": "NORMAL",
  "transactionType": "BUY",
  "orderType": "MARKET",
  "quantity": 1,
  "duration": "DAY",
  "price": 0,
  "squareOff": 0,
  "stopLoss": 0,
  "productType": "INTRADAY",
  "userIds": [1],
  "createdBy": "admin"
}
```

The Angel One adapter loads the selected `broker_session_id`, decrypts the access token inside the broker module, and sends the SmartAPI order request with explicit SmartAPI headers.

## Telegram Commands

- `/start`: verifies allowlist and sends the Angel One login button.
- `/login`: sends the Angel One login button.

Every weekday at 9:00 AM Asia/Kolkata, the scheduler sends login reminders to `TELEGRAM_ALLOWED_USER_IDS`.

## Security Notes

- Tokens are encrypted using AES-GCM before database storage.
- Broker tokens, API keys, and secrets are never returned from APIs.
- The OAuth `state` JWT expires after 5 minutes by default.
- Admin endpoints are denied when admin credentials are not configured.
