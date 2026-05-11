# Angel One SmartAPI OAuth Login Architecture via Telegram Bot

## Objective

Build a centralized trading management system where:

- Users do NOT login into the application.
- Users only authenticate their Angel One account.
- Authentication is initiated through a Telegram bot.
- Backend receives and securely stores Angel One tokens.
- Admin manages all connected accounts centrally.
- Spring Boot is used for backend.
- Telegram Bot acts as the user interaction layer.

---

# Core Architecture

```text
Telegram Bot
    ↓
User clicks Login button
    ↓
Backend generates SmartAPI OAuth URL
    ↓
User authenticates on Angel One page
    ↓
Angel One redirects to backend callback
    ↓
Backend receives auth_token + feed_token
    ↓
Backend validates state JWT
    ↓
Backend decodes auth_token JWT
    ↓
Backend securely stores tokens
    ↓
Admin system uses tokens for trading + market data
```

---

# SmartAPI OAuth Login Flow Specification

## Official SmartAPI Login Flow

The login flow starts by navigating to the public SmartAPI login endpoint:

```text
https://smartapi.angelone.in/publisher-login?api_key=xxx&redirect_url=yyy&state=statevariable
```

After successful login:

- User gets redirected to the configured redirect URL.
- SmartAPI appends:
  - auth_token
  - feed_token
  - state

as query parameters.

Example:

```text
https://yourdomain.com/callback?auth_token=xxx&feed_token=yyy&state=zzz
```

---

# Important SmartAPI Concepts

## API Key

Represents the SmartAPI application.

This belongs to the platform/backend.

It is NOT provided by users.

Used in OAuth login URL.

---

## Secret Key

Represents backend ownership of the application.

Must NEVER be exposed publicly.

Store securely.

---

## auth_token

Represents authenticated Angel One session.

Used for:

- Order APIs
- Position APIs
- Holdings APIs
- LTP APIs
- Historical APIs

Usually sent as:

```http
Authorization: Bearer <auth_token>
```

---

## feed_token

Used for SmartAPI WebSocket authentication.

Required for:

- Live market data
- Tick streaming
- Real-time LTP

---

# Recommended System Design

## Tech Stack

### Backend

- Spring Boot
- PostgreSQL
- JJWT (JWT library)
- TelegramBots Spring Boot Starter
- Spring Scheduler
- Spring Security
- JPA/Hibernate

### Telegram Bot

Use:

https://github.com/rubenlagus/TelegramBots

Maven:

```xml
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots-spring-boot-starter</artifactId>
    <version>6.9.7.1</version>
</dependency>
```

---

# Authentication Flow Design

## Key Principle

Users never login into the platform.

Telegram identity becomes the application's primary identity.

The platform only manages:

- Telegram user
- Angel One broker authorization

---

# Database Schema

## telegram_users

```sql
CREATE TABLE telegram_users (
    id BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT UNIQUE NOT NULL,
    telegram_username VARCHAR(255),
    full_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## broker_sessions

```sql
CREATE TABLE broker_sessions (
    id BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT NOT NULL,
    broker_name VARCHAR(50) NOT NULL,
    broker_client_id VARCHAR(100) NOT NULL,
    auth_token TEXT NOT NULL,
    feed_token TEXT NOT NULL,
    token_expiry TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

---

# Security Design

## IMPORTANT

Never expose:

- auth_token
- feed_token
- API key
- Secret key

To:

- React frontend
- Telegram users
- Browser localStorage
- Logs

All sensitive operations must remain backend-only.

---

# State Parameter Architecture

## Best Practice

Use signed JWT as the OAuth state parameter.

This avoids:

- Redis temporary storage
- Session correlation issues
- Stateful architecture

The state itself carries identity information.

---

# Recommended state JWT Payload

```json
{
  "telegramUserId": 987654321,
  "purpose": "ANGEL_LOGIN",
  "iat": 1715234567,
  "exp": 1715235167
}
```

---

# Why JWT State Is Best

Advantages:

- Stateless
- Secure
- Signed
- Horizontally scalable
- No temporary DB needed
- Self-contained identity

---

# State JWT Generation

## Maven Dependency

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

## State Generation Example

```java
String state = Jwts.builder()
    .claim("telegramUserId", telegramUserId)
    .claim("purpose", "ANGEL_LOGIN")
    .setIssuedAt(new Date())
    .setExpiration(new Date(System.currentTimeMillis() + 300000))
    .signWith(secretKey)
    .compact();
```

State expiry:

- 5 minutes recommended.

---

# Telegram Bot Flow

## 1. User Starts Bot

Bot checks whether Telegram user exists in allowlist.

If not authorized:

```text
Access denied.
```

---

## 2. 9 AM Login Reminder

Use Spring Scheduler.

```java
@EnableScheduling
```

Example:

```java
@Scheduled(cron = "0 0 9 * * MON-FRI")
public void sendLoginReminders() {

}
```

---

# Reminder Message

```text
Please login to Angel One for today's trading session.
```

With Telegram inline button:

```text
[ Login to Angel One ]
```

---

# OAuth URL Generation

## Backend Generates OAuth URL

```java
String loginUrl =
    "https://smartapi.angelone.in/publisher-login" +
    "?api_key=" + apiKey +
    "&redirect_url=" + redirectUrl +
    "&state=" + state;
```

---

# Telegram Button

```java
InlineKeyboardButton loginButton =
    new InlineKeyboardButton();

loginButton.setText("Login to Angel One");
loginButton.setUrl(loginUrl);
```

---

# SmartAPI OAuth Callback

## Callback Endpoint

```java
@GetMapping("/broker/angel/callback")
public ResponseEntity<?> callback(
        @RequestParam("auth_token") String authToken,
        @RequestParam("feed_token") String feedToken,
        @RequestParam("state") String state
) {

}
```

---

# Callback Processing Steps

## Step 1 — Validate state JWT

Verify:

- Signature
- Expiry
- Purpose

Extract:

- telegramUserId

Example:

```java
Claims claims = Jwts.parserBuilder()
    .setSigningKey(secretKey)
    .build()
    .parseClaimsJws(state)
    .getBody();

Long telegramUserId =
    claims.get("telegramUserId", Long.class);
```

---

# Step 2 — Decode auth_token JWT

The SmartAPI auth_token is usually JWT-like.

Decode it to extract Angel client code.

---

# JWT Decode Dependency

```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>
```

---

# Decode SmartAPI auth_token

```java
DecodedJWT jwt = JWT.decode(authToken);

String angelClientId =
    jwt.getClaim("username").asString();
```

Potential claims:

- username
- clientcode
- sub

Inspect actual payload during development.

---

# Step 3 — Store Broker Session

Persist:

| Field | Value |
|---|---|
| telegram_user_id | Internal Telegram identity |
| broker_client_id | Angel client ID |
| auth_token | SmartAPI auth token |
| feed_token | SmartAPI websocket token |

---

# Token Storage Security

## CRITICAL REQUIREMENTS

Tokens must be encrypted before storage.

Never store plaintext tokens in production.

---

# Recommended Encryption

Use:

- AES encryption
- Jasypt
- AWS KMS (future)
- Vault (future)

---

# Recommended Minimal Approach

Encrypt before DB save.

Example service:

```java
public class EncryptionService {

    public String encrypt(String plainText) {

    }

    public String decrypt(String cipherText) {

    }
}
```

---

# IMPORTANT TOKEN RULES

Never:

- Log tokens
- Send tokens to frontend
- Send tokens to Telegram
- Store in browser
- Expose in APIs

Backend-only access.

---

# Token Expiry Strategy

SmartAPI sessions may expire daily.

Recommended operational flow:

- Daily 9 AM reminder
- User re-authenticates
- Tokens replaced
- Previous sessions invalidated

---

# Future WebSocket Architecture

Each authenticated Angel account will require:

- Separate feed_token
- Separate websocket session

Recommended design:

```text
1 websocket connection per Angel account
```

---

# Recommended Backend Modules

## TelegramBotService

Responsibilities:

- Handle Telegram commands
- Send reminders
- Send OAuth login buttons

---

## OAuthService

Responsibilities:

- Generate signed state JWT
- Generate OAuth URL
- Validate callback state

---

## SmartApiTokenService

Responsibilities:

- Decode auth_token
- Extract Angel client ID
- Store encrypted tokens

---

## BrokerSessionService

Responsibilities:

- Create/update broker sessions
- Manage token lifecycle
- Manage active broker accounts

---

## MarketDataService

Responsibilities:

- SmartAPI WebSocket
- Live tick subscriptions
- Market data streaming

---

# Recommended Environment Variables

```env
SMARTAPI_API_KEY=xxx
SMARTAPI_SECRET_KEY=yyy
SMARTAPI_REDIRECT_URL=https://yourdomain.com/broker/angel/callback
JWT_SIGNING_SECRET=zzz
TELEGRAM_BOT_TOKEN=aaa
```

---

# Recommended Security Practices

## HTTPS Mandatory

Production callback URL must use HTTPS.

---

## Validate state Purpose

Prevent misuse.

Verify:

```json
"purpose": "ANGEL_LOGIN"
```

---

## Short state Expiry

Recommended:

- 5 minutes
- max 10 minutes

---

## Verify Telegram Allowlist

Only approved Telegram users should receive login links.

---

# Recommended Final Flow

```text
Telegram user receives reminder
    ↓
Clicks Login button
    ↓
Backend generates signed state JWT
    ↓
Backend generates SmartAPI OAuth URL
    ↓
User authenticates with Angel One
    ↓
Angel redirects to backend callback
    ↓
Backend validates state JWT
    ↓
Backend decodes auth_token
    ↓
Backend extracts Angel client ID
    ↓
Backend encrypts + stores tokens
    ↓
Admin system manages account
```

---

# Final Architectural Notes

This architecture intentionally avoids:

- User accounts
- Frontend authentication
- Password handling
- TOTP handling
- Broker credential storage

The platform only:

- receives OAuth-authorized broker sessions
- stores tokens securely
- centrally manages trading

This is the recommended architecture for:

- centralized trade management
- advisory systems
- copy trading systems
- multi-account trading systems
- Telegram-driven trading platforms

