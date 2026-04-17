# Announcements and Alerts

Module key: 07-announcements-alerts

Endpoint count: 7

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/announcements | Bearer token (authenticated user) | AnnouncementController.createAnnouncement |
| GET | /v1/announcements | Bearer token (authenticated user) | AnnouncementController.getActiveAnnouncements |
| PATCH | /v1/announcements/{id}/deactivate | Bearer token (authenticated user) | AnnouncementController.deactivateAnnouncement |
| POST | /v1/alerts | Bearer token (authenticated user) | AlertController.reportAlert |
| GET | /v1/alerts | Bearer token (authenticated user) | AlertController.getActiveAlerts |
| GET | /v1/alerts/{id} | Bearer token (authenticated user) | AlertController.getAlertById |
| PATCH | /v1/alerts/{id}/resolve | Bearer token (authenticated user) | AlertController.resolveAlert |

## Endpoint Usage

### POST /v1/announcements

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AnnouncementController.java:67`
- Handler: `AnnouncementController.createAnnouncement()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateAnnouncementRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "authorId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "content": "Sample text",
  "priority": "LOW"
}
```

Full body example:

```json
{
  "authorId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "content": "Sample text",
  "priority": "LOW",
  "expiresAt": "2027-01-01T10:00:00Z"
}
```

How to use:
- Send request to `{{host}}/v1/announcements`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/announcements

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AnnouncementController.java:79`
- Handler: `AnnouncementController.getActiveAnnouncements()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<AnnouncementResponse>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/announcements`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/announcements/{id}/deactivate

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AnnouncementController.java:86`
- Handler: `AnnouncementController.deactivateAnnouncement()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `DeactivateAnnouncementRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "requestingUserId": "{{user_id}}"
}
```

Full body example:

```json
{
  "requestingUserId": "{{user_id}}"
}
```

How to use:
- Send request to `{{host}}/v1/announcements/{{announcement_id}}/deactivate`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/alerts

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AlertController.java:49`
- Handler: `AlertController.reportAlert()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `ReportAlertRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "reporterId": "00000000-0000-0000-0000-000000000000",
  "type": "MISSING",
  "category": "PET",
  "title": "Sample title",
  "description": "Sample text"
}
```

Full body example:

```json
{
  "reporterId": "00000000-0000-0000-0000-000000000000",
  "type": "MISSING",
  "category": "PET",
  "title": "Sample title",
  "description": "Sample text",
  "location": "sample",
  "eventTime": "2027-01-01T10:00:00Z",
  "photoUrls": "sample",
  "contactNumber": "+201000000000"
}
```

How to use:
- Send request to `{{host}}/v1/alerts`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/alerts

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AlertController.java:66`
- Handler: `AlertController.getActiveAlerts()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<AlertResponse>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/alerts`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/alerts/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AlertController.java:73`
- Handler: `AlertController.getAlertById()`
- Auth: Bearer token (authenticated user)
- Success: 200/201 depending on handler logic
- Response Type: `AlertResponse`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/alerts/{{alert_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/alerts/{id}/resolve

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AlertController.java:82`
- Handler: `AlertController.resolveAlert()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `ResolveAlertRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "requestingUserId": "{{user_id}}"
}
```

Full body example:

```json
{
  "requestingUserId": "{{user_id}}"
}
```

How to use:
- Send request to `{{host}}/v1/alerts/{{alert_id}}/resolve`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

