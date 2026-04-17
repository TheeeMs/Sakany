# Authentication and Users

Module key: 01-auth-users

Endpoint count: 6

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/auth/register | Public (no token) | AuthController.register |
| POST | /v1/auth/send-otp | Public (no token) | AuthController.sendOtp |
| POST | /v1/auth/login/phone | Public (no token) | AuthController.loginWithPhone |
| POST | /v1/auth/login/email | Public (no token) | AuthController.loginWithEmail |
| POST | /v1/auth/refresh | Public (no token) | AuthController.refresh |
| GET | /v1/auth/me | Bearer token (authenticated user) | AuthController.me |

## Endpoint Usage

### POST /v1/auth/register

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AuthController.java:58`
- Handler: `AuthController.register()`
- Auth: Public (no token)
- Success: 201 Created
- Response Type: `AuthResponse`
- Path Params: none
- Query Params: none
- Request Body Type: `RegisterRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "password": "Password123!",
  "type": "PROPERTY_OWNER",
  "unitId": "{{unit_id}}",
  "loginMethod": "PHONE_OTP"
}
```

Full body example:

```json
{
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "password": "Password123!",
  "type": "PROPERTY_OWNER",
  "unitId": "{{unit_id}}",
  "loginMethod": "PHONE_OTP"
}
```

How to use:
- Send request to `{{host}}/v1/auth/register`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/auth/send-otp

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AuthController.java:85`
- Handler: `AuthController.sendOtp()`
- Auth: Public (no token)
- Success: 200 OK
- Response Type: `Map<String, String>`
- Path Params: none
- Query Params: none
- Request Body Type: `Map<String, String>`
- Body Requirement: required

Minimal body example:

```json
{
  "phoneNumber": "+201000000000"
}
```

Full body example:

```json
{
  "phoneNumber": "+201000000000"
}
```

How to use:
- Send request to `{{host}}/v1/auth/send-otp`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/auth/login/phone

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AuthController.java:107`
- Handler: `AuthController.loginWithPhone()`
- Auth: Public (no token)
- Success: 200 OK
- Response Type: `AuthResponse`
- Path Params: none
- Query Params: none
- Request Body Type: `LoginPhoneRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "phoneNumber": "+201000000000",
  "otpCode": "sample"
}
```

Full body example:

```json
{
  "phoneNumber": "+201000000000",
  "otpCode": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/auth/login/phone`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/auth/login/email

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AuthController.java:136`
- Handler: `AuthController.loginWithEmail()`
- Auth: Public (no token)
- Success: 200 OK
- Response Type: `AuthResponse`
- Path Params: none
- Query Params: none
- Request Body Type: `LoginEmailRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

Full body example:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

How to use:
- Send request to `{{host}}/v1/auth/login/email`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/auth/refresh

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AuthController.java:156`
- Handler: `AuthController.refresh()`
- Auth: Public (no token)
- Success: 200 OK
- Response Type: `AuthResponse`
- Path Params: none
- Query Params: none
- Request Body Type: `RefreshRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "refreshToken": "sample"
}
```

Full body example:

```json
{
  "refreshToken": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/auth/refresh`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/auth/me

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AuthController.java:179`
- Handler: `AuthController.me()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Map<String, Object>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/auth/me`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

