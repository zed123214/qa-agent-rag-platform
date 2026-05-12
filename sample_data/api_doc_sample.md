# API Documentation: User Management

## POST /api/auth/login
Authenticate user and return JWT token.

**Request:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Response (200):**
```json
{
  "code": 200,
  "msg": "Login successful",
  "data": {
    "token": "eyJhbG...",
    "expiresIn": 86400
  }
}
```

**Response (401):**
```json
{
  "code": 401,
  "msg": "Invalid credentials",
  "errorCode": "AUTH_001"
}
```

## GET /api/user/profile
Get current user profile. Requires Bearer token.

**Response (200):**
```json
{
  "code": 200,
  "data": {
    "userId": "U-10001",
    "username": "demo_user",
    "role": "developer",
    "status": "active"
  }
}
```

## POST /api/user/create
Create new user (admin only). Requires Bearer token.

**Request:**
```json
{
  "username": "string (3-32 chars, required)",
  "password": "string (8-128 chars, required)",
  "email": "string (required)",
  "role": "developer|viewer (required)"
}
```
