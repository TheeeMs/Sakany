# Admin Missing and Found

Module key: 10-admin-missing-found

Endpoint count: 15

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| GET | /v1/admin/missing-found/categories | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getCategoryOptions |
| GET | /v1/admin/missing-found/reports | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getReports |
| POST | /v1/admin/missing-found/reports | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.createReport |
| DELETE | /v1/admin/missing-found/reports/{reportId} | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.deleteReport |
| GET | /v1/admin/missing-found/reports/{reportId} | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getReport |
| PATCH | /v1/admin/missing-found/reports/{reportId} | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.updateReport |
| GET | /v1/admin/missing-found/reports/{reportId}/details | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getReportDetails |
| PATCH | /v1/admin/missing-found/reports/{reportId}/mark-matched | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.markMatched |
| PATCH | /v1/admin/missing-found/reports/{reportId}/mark-resolved | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.markResolved |
| POST | /v1/admin/missing-found/reports/{reportId}/notify-user | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.notifyUser |
| PATCH | /v1/admin/missing-found/reports/{reportId}/status | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.updateStatus |
| GET | /v1/admin/missing-found/residents/options | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getResidentOptions |
| GET | /v1/admin/missing-found/statuses | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getStatusOptions |
| GET | /v1/admin/missing-found/summary | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getSummary |
| GET | /v1/admin/missing-found/types | Admin Bearer token (ROLE_ADMIN) | AdminMissingFoundController.getTypeOptions |

## Endpoint Usage

### GET /v1/admin/missing-found/categories

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:176`
- Handler: `AdminMissingFoundController.getCategoryOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/categories`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/missing-found/reports

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:57`
- Handler: `AdminMissingFoundController.getReports()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMissingFoundService.MissingFoundReportsPage`
- Path Params: none
- Query Params:
  - `search` (String) optional
  - `type` (String) optional, default `ALL`
  - `status` (String) optional, default `ALL`
  - `category` (String) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/missing-found/reports

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:101`
- Handler: `AdminMissingFoundController.createReport()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `AdminMissingFoundCreateRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "reporterId": "00000000-0000-0000-0000-000000000000",
  "residentId": "{{user_id}}",
  "type": "sample",
  "category": "OTHER",
  "description": "Sample text"
}
```

Full body example:

```json
{
  "reporterId": "00000000-0000-0000-0000-000000000000",
  "residentId": "{{user_id}}",
  "type": "sample",
  "category": "OTHER",
  "description": "Sample text",
  "location": "sample",
  "eventTime": "2027-01-01T10:00:00Z",
  "photoUrls": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/missing-found/reports/{reportId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:160`
- Handler: `AdminMissingFoundController.deleteReport()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/missing-found/reports/{reportId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:91`
- Handler: `AdminMissingFoundController.getReport()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMissingFoundService.MissingFoundReportDetails`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/missing-found/reports/{reportId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:118`
- Handler: `AdminMissingFoundController.updateReport()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body Type: `AdminMissingFoundUpdateRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "type": "sample",
  "category": "OTHER",
  "description": "Sample text"
}
```

Full body example:

```json
{
  "type": "sample",
  "category": "OTHER",
  "description": "Sample text",
  "location": "sample",
  "eventTime": "2027-01-01T10:00:00Z",
  "photoUrls": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/missing-found/reports/{reportId}/details

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:96`
- Handler: `AdminMissingFoundController.getReportDetails()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMissingFoundService.MissingFoundReportDetails`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}/details`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/missing-found/reports/{reportId}/mark-matched

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:136`
- Handler: `AdminMissingFoundController.markMatched()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}/mark-matched`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/missing-found/reports/{reportId}/mark-resolved

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:142`
- Handler: `AdminMissingFoundController.markResolved()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}/mark-resolved`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/missing-found/reports/{reportId}/notify-user

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:148`
- Handler: `AdminMissingFoundController.notifyUser()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMissingFoundService.NotifyUserResult`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body Type: `AdminMissingFoundNotifyUserRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "title": "Sample title"
}
```

Full body example:

```json
{
  "title": "Sample title"
}
```

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}/notify-user`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/admin/missing-found/reports/{reportId}/status

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:127`
- Handler: `AdminMissingFoundController.updateStatus()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `reportId` (UUID)
- Query Params: none
- Request Body Type: `AdminMissingFoundStatusRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "status": "OPEN"
}
```

Full body example:

```json
{
  "status": "OPEN"
}
```

How to use:
- Send request to `{{host}}/v1/admin/missing-found/reports/{{admin_report_id}}/status`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/missing-found/residents/options

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:181`
- Handler: `AdminMissingFoundController.getResidentOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminResidentOptionsPage`
- Path Params: none
- Query Params:
  - `search` (String) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/residents/options`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/missing-found/statuses

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:171`
- Handler: `AdminMissingFoundController.getStatusOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/statuses`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/missing-found/summary

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:76`
- Handler: `AdminMissingFoundController.getSummary()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMissingFoundService.MissingFoundSummary`
- Path Params: none
- Query Params:
  - `search` (String) optional
  - `type` (String) optional, default `ALL`
  - `status` (String) optional, default `ALL`
  - `category` (String) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/summary`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/missing-found/types

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminMissingFoundController.java:166`
- Handler: `AdminMissingFoundController.getTypeOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/missing-found/types`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

