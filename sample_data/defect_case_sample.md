# Historical Defect Cases

## BUG-2024-001: Login Timeout Under High Concurrency
- **Severity:** P1
- **Found In:** v2.0.3
- **Fixed In:** v2.0.5
- **Symptom:** Users experience login timeout after 30+ concurrent requests
- **Root Cause:** Database connection pool exhausted (default max=10)
- **Fix:** Increased connection pool to 50, added connection timeout of 5s
- **Related API:** POST /api/auth/login
- **Test Suggestion:** Add concurrent login stress test

## BUG-2024-002: Token Not Refreshed After Expiry
- **Severity:** P2
- **Found In:** v2.1.0
- **Symptom:** After token expires, refresh endpoint returns old token
- **Root Cause:** Cache not invalidated on token refresh
- **Fix:** Added cache eviction on token refresh
- **Related API:** POST /api/auth/refresh

## BUG-2024-003: Password Validation Bypass
- **Severity:** P0
- **Found In:** v2.0.0
- **Symptom:** Password with only spaces accepted
- **Root Cause:** trim() applied before validation, empty string passes checks
- **Fix:** Validate before trimming, reject whitespace-only passwords
- **Related API:** POST /api/auth/login, POST /api/user/create
