# Access Codes and QR Access

Module key: 03-access-qr

Endpoint count: 15

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/access/codes | Bearer token (authenticated user) | AccessController.createAccessCode |
| GET | /v1/access/codes/my | Bearer token (authenticated user) | AccessController.listMyAccessCodes |
| GET | /v1/access/codes/{id} | Bearer token (authenticated user) | AccessController.getAccessCode |
| POST | /v1/access/codes/{code}/scan | Bearer token (authenticated user) | AccessController.scanAccessCode |
| GET | /v1/access/visit-logs | Bearer token (authenticated user) | AccessController.listVisitLogs |
| PATCH | /v1/access/visit-logs/{id}/exit | Bearer token (authenticated user) | AccessController.logVisitorExit |
| POST | /v1/access/codes/{codeId}/reactivate | Bearer token (authenticated user) | AccessController.reactivateAccessCode |
| DELETE | /v1/access/codes/{id} | Bearer token (authenticated user) | AccessController.revokeAccessCode |
| GET | /v1/admin/access/codes | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.listAdminQrAccessCodes |
| DELETE | /v1/admin/access/codes/{accessCodeId} | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.deleteQrCode |
| GET | /v1/admin/access/codes/{accessCodeId}/details | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.getCodeDetails |
| GET | /v1/admin/access/codes/{accessCodeId}/download | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.downloadQrCode |
| GET | /v1/admin/access/residents/{residentId}/codes | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.listResidentQrCodes |
| GET | /v1/admin/access/statuses | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.listStatusOptions |
| GET | /v1/admin/access/types | Admin Bearer token (ROLE_ADMIN) | AdminQrAccessController.listTypeOptions |

## Endpoint Usage

### POST /v1/access/codes

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:80`
- Handler: `AccessController.createAccessCode()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `AccessCodeResponse`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateAccessCodeRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "visitorName": "Sample name",
  "visitorPhone": "+201000000000",
  "purpose": "GUEST",
  "validFrom": "2027-01-01T10:00:00Z",
  "validUntil": "2027-01-01T10:00:00Z"
}
```

Full body example:

```json
{
  "visitorName": "Sample name",
  "visitorPhone": "+201000000000",
  "purpose": "GUEST",
  "isSingleUse": false,
  "validFrom": "2027-01-01T10:00:00Z",
  "validUntil": "2027-01-01T10:00:00Z"
}
```

How to use:
- Send request to `{{host}}/v1/access/codes`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/access/codes/my

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:115`
- Handler: `AccessController.listMyAccessCodes()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<AccessCodeResponse>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/access/codes/my`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/access/codes/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:104`
- Handler: `AccessController.getAccessCode()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `AccessCodeResponse`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/access/codes/{{access_code_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/access/codes/{code}/scan

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:132`
- Handler: `AccessController.scanAccessCode()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `ScanAccessCodeResponse`
- Path Params:
  - `code` (String)
- Query Params: none
- Request Body Type: `ScanAccessCodeRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "code": "sample",
  "gateNumber": "sample"
}
```

Full body example:

```json
{
  "code": "sample",
  "gateNumber": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/access/codes/{{code}}/scan`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/access/visit-logs

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:179`
- Handler: `AccessController.listVisitLogs()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<VisitLogResponse>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/access/visit-logs`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/access/visit-logs/{id}/exit

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:204`
- Handler: `AccessController.logVisitorExit()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/access/visit-logs/{{visit_log_id}}/exit`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/access/codes/{codeId}/reactivate

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:159`
- Handler: `AccessController.reactivateAccessCode()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `AccessCodeResponse`
- Path Params:
  - `codeId` (UUID)
- Query Params: none
- Request Body Type: `ReactivateAccessCodeRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "validFrom": "2027-01-01T10:00:00Z",
  "validUntil": "2027-01-01T10:00:00Z"
}
```

Full body example:

```json
{
  "validFrom": "2027-01-01T10:00:00Z",
  "validUntil": "2027-01-01T10:00:00Z"
}
```

How to use:
- Send request to `{{host}}/v1/access/codes/{{access_code_id}}/reactivate`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/access/codes/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AccessController.java:149`
- Handler: `AccessController.revokeAccessCode()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/access/codes/{{access_code_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/access/codes

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:30`
- Handler: `AdminQrAccessController.listAdminQrAccessCodes()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminQrAccessDirectoryService.AdminQrAccessPage`
- Path Params: none
- Query Params:
  - `tab` (AdminQrAccessTab) optional, default `ALL`
  - `search` (String) optional
  - `status` (AdminQrAccessStatus) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/codes`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### DELETE /v1/admin/access/codes/{accessCodeId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:78`
- Handler: `AdminQrAccessController.deleteQrCode()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `accessCodeId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/codes/{{access_code_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/access/codes/{accessCodeId}/details

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:62`
- Handler: `AdminQrAccessController.getCodeDetails()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminQrAccessDirectoryService.AdminQrCodeDetailsResponse`
- Path Params:
  - `accessCodeId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/codes/{{access_code_id}}/details`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/access/codes/{accessCodeId}/download

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:69`
- Handler: `AdminQrAccessController.downloadQrCode()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `byte[]`
- Path Params:
  - `accessCodeId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/codes/{{access_code_id}}/download`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/access/residents/{residentId}/codes

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:51`
- Handler: `AdminQrAccessController.listResidentQrCodes()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminQrAccessDirectoryService.ResidentQrCodesResponse`
- Path Params:
  - `residentId` (UUID)
- Query Params:
  - `tab` (AdminQrAccessTab) optional, default `ALL`
  - `status` (AdminQrAccessStatus) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/residents/{{resident_id}}/codes`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/access/statuses

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:41`
- Handler: `AdminQrAccessController.listStatusOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/statuses`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/access/types

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\access\internal\api\controllers\AdminQrAccessController.java:46`
- Handler: `AdminQrAccessController.listTypeOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/access/types`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

