# Angel One Telegram OAuth Manager

Spring Boot backend for Telegram-initiated Angel One SmartAPI OAuth login.

## What It Does

- Telegram users do not log in to this application.
- Approved Telegram users receive an Angel One login button.
- The backend generates a signed short-lived JWT `state`.
- Angel One redirects to `/broker/angel/callback`.
- The backend validates `state`, decodes the Angel `auth_token`, encrypts tokens, and stores them.
- Admin endpoints show connected accounts without exposing tokens.

## Configuration

Copy `.env.example` into your deployment environment and set real values. Important variables:

```env
SMARTAPI_API_KEY=...
SMARTAPI_REDIRECT_URL=https://yourdomain.com/broker/angel/callback
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
DATABASE_URL=jdbc:postgresql://localhost:5432/angel_oauth
DATABASE_USERNAME=angel_oauth
DATABASE_PASSWORD=...
```

The app falls back to an in-memory H2 database for local smoke testing.

## Run

```bash
mvn spring-boot:run
```

The callback endpoint is:

```text
GET /broker/angel/callback?auth_token=...&feed_token=...&state=...
```

Admin endpoints use HTTP Basic authentication when `ADMIN_USERNAME` and `ADMIN_PASSWORD` are configured:

```text
GET /admin/broker-sessions
GET /admin/telegram-users
```

## Telegram Commands

- `/start`: verifies allowlist and sends the Angel One login button.
- `/login`: sends the Angel One login button.

Every weekday at 9:00 AM Asia/Kolkata, the scheduler sends login reminders to `TELEGRAM_ALLOWED_USER_IDS`.

## Security Notes

- Tokens are encrypted using AES-GCM before database storage.
- `auth_token`, `feed_token`, API key, and secret key are never returned from APIs.
- The OAuth `state` JWT expires after 5 minutes by default.
- Admin endpoints are denied when admin credentials are not configured.
