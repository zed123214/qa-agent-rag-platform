# PRD: User Login Module v2.1

## Overview
The login module handles user authentication for the API platform.

## Functional Requirements

### FR-001: Standard Login
- Users can log in with username + password
- Successful login returns a JWT token (valid for 24 hours)
- Failed login returns error code and message

### FR-002: Password Policy
- Minimum 8 characters, maximum 128
- Must contain at least 1 uppercase, 1 lowercase, 1 digit
- Passwords are stored as bcrypt hashes

### FR-003: Account Lockout
- After 5 consecutive failed attempts, account is locked for 15 minutes
- Admin can manually unlock accounts

### FR-004: Token Management
- Token is included in Authorization header as "Bearer {token}"
- Token refresh endpoint: POST /api/auth/refresh
- Expired tokens return 401 with error code TOKEN_EXPIRED

## Error Codes
| Code | Description |
|------|-------------|
| AUTH_001 | Invalid credentials |
| AUTH_002 | Account disabled |
| AUTH_003 | Account locked |
| AUTH_004 | Token expired |
| AUTH_005 | Token invalid |

## Non-Functional Requirements
- Login response time < 500ms (P95)
- Support 1000 concurrent login requests
- Rate limiting: max 10 attempts per IP per minute
