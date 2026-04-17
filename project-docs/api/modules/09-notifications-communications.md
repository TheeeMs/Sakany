# Notifications and Communications Center

Module key: 09-notifications-communications

Endpoint count: 15

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/device-tokens | Bearer token (authenticated user) | DeviceTokenController.registerDeviceToken |
| GET | /v1/notifications | Bearer token (authenticated user) | NotificationController.listNotifications |
| POST | /v1/notifications/send | Bearer token (authenticated user) | NotificationController.sendNotification |
| PATCH | /v1/notifications/{id}/read | Bearer token (authenticated user) | NotificationController.markAsRead |
| PATCH | /v1/notifications/read-all | Bearer token (authenticated user) | NotificationController.markAllAsRead |
| DELETE | /v1/device-tokens/{id} | Bearer token (authenticated user) | DeviceTokenController.deactivateDeviceToken |
| POST | /v1/admin/communications/announcements | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.createAnnouncement |
| DELETE | /v1/admin/communications/announcements/{announcementId} | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.deleteAnnouncementItem |
| GET | /v1/admin/communications/announcements/{announcementId} | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.getAnnouncementItem |
| PATCH | /v1/admin/communications/announcements/{announcementId} | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.updateAnnouncementItem |
| GET | /v1/admin/communications/center | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.getCenter |
| POST | /v1/admin/communications/notifications | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.createPushNotification |
| DELETE | /v1/admin/communications/notifications/{itemId} | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.deleteNotificationItem |
| GET | /v1/admin/communications/notifications/{itemId} | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.getNotificationItem |
| PATCH | /v1/admin/communications/notifications/{itemId} | Admin Bearer token (ROLE_ADMIN) | AdminCommunicationsController.updateNotificationItem |

## Endpoint Usage

### POST /v1/device-tokens

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\DeviceTokenController.java:34`
- Handler: `DeviceTokenController.registerDeviceToken()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `RegisterDeviceTokenRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "userId": "{{user_id}}",
  "token": "sample",
  "platform": "ANDROID"
}
```

Full body example:

```json
{
  "userId": "{{user_id}}",
  "token": "sample",
  "platform": "ANDROID"
}
```

How to use:
- Send request to `{{host}}/v1/device-tokens`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/notifications

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\NotificationController.java:50`
- Handler: `NotificationController.listNotifications()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<NotificationResponse>`
- Path Params: none
- Query Params:
  - `recipientId` (UUID)
  - `status` (NotificationStatus) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/notifications`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/notifications/send

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\NotificationController.java:74`
- Handler: `NotificationController.sendNotification()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `SendNotificationRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "recipientId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "type": "MAINTENANCE_UPDATE",
  "referenceId": "00000000-0000-0000-0000-000000000000"
}
```

Full body example:

```json
{
  "recipientId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "body": "sample",
  "type": "MAINTENANCE_UPDATE",
  "referenceId": "00000000-0000-0000-0000-000000000000",
  "channel": "PUSH"
}
```

How to use:
- Send request to `{{host}}/v1/notifications/send`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/notifications/{id}/read

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\NotificationController.java:62`
- Handler: `NotificationController.markAsRead()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params:
  - `recipientId` (UUID)
- Request Body: none

How to use:
- Send request to `{{host}}/v1/notifications/{{notification_id}}/read`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/notifications/read-all

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\NotificationController.java:68`
- Handler: `NotificationController.markAllAsRead()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params: none
- Query Params:
  - `recipientId` (UUID)
- Request Body: none

How to use:
- Send request to `{{host}}/v1/notifications/read-all`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### DELETE /v1/device-tokens/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\DeviceTokenController.java:42`
- Handler: `DeviceTokenController.deactivateDeviceToken()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/device-tokens/{{id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/communications/announcements

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:96`
- Handler: `AdminCommunicationsController.createAnnouncement()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `AdminCreateAnnouncementRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "authorId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "content": "Sample text",
  "priority": "NORMAL"
}
```

Full body example:

```json
{
  "authorId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "content": "Sample text",
  "priority": "NORMAL",
  "expiresAt": "2027-01-01T10:00:00Z"
}
```

How to use:
- Send request to `{{host}}/v1/admin/communications/announcements`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/communications/announcements/{announcementId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:130`
- Handler: `AdminCommunicationsController.deleteAnnouncementItem()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `announcementId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/communications/announcements/{{announcement_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/communications/announcements/{announcementId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:109`
- Handler: `AdminCommunicationsController.getAnnouncementItem()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminCommunicationsCenterService.CommunicationCardItem`
- Path Params:
  - `announcementId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/communications/announcements/{{announcement_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/communications/announcements/{announcementId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:114`
- Handler: `AdminCommunicationsController.updateAnnouncementItem()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `announcementId` (UUID)
- Query Params: none
- Request Body Type: `AdminUpdateAnnouncementRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "title": "Sample title",
  "content": "Sample text",
  "priority": "NORMAL"
}
```

Full body example:

```json
{
  "title": "Sample title",
  "content": "Sample text",
  "priority": "NORMAL",
  "expiresAt": "2027-01-01T10:00:00Z",
  "active": false
}
```

How to use:
- Send request to `{{host}}/v1/admin/communications/announcements/{{announcement_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/communications/center

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:34`
- Handler: `AdminCommunicationsController.getCenter()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminCommunicationsCenterService.AdminCommunicationsCenterResponse`
- Path Params: none
- Query Params:
  - `tab` (String) optional, default `PUSH_NOTIFICATIONS`
  - `status` (String) optional, default `ALL`
  - `search` (String) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/communications/center`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/communications/notifications

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:43`
- Handler: `AdminCommunicationsController.createPushNotification()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `AdminCommunicationsCenterService.CreatePushNotificationResult`
- Path Params: none
- Query Params: none
- Request Body Type: `AdminCreatePushNotificationRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000",
  "recipientIds": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "priority": "NORMAL"
}
```

Full body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000",
  "recipientIds": "00000000-0000-0000-0000-000000000000",
  "sendToAll": false,
  "title": "Sample title",
  "message": "Sample text",
  "priority": "NORMAL",
  "scheduleAt": "2027-01-01T10:00:00Z"
}
```

How to use:
- Send request to `{{host}}/v1/admin/communications/notifications`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/communications/notifications/{itemId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:87`
- Handler: `AdminCommunicationsController.deleteNotificationItem()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `itemId` (UUID)
- Query Params:
  - `tab` (String) optional, default `PUSH_NOTIFICATIONS`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/communications/notifications/{{notification_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/communications/notifications/{itemId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:62`
- Handler: `AdminCommunicationsController.getNotificationItem()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminCommunicationsCenterService.CommunicationCardItem`
- Path Params:
  - `itemId` (UUID)
- Query Params:
  - `tab` (String) optional, default `PUSH_NOTIFICATIONS`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/communications/notifications/{{notification_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/communications/notifications/{itemId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\notifications\internal\api\controllers\AdminCommunicationsController.java:70`
- Handler: `AdminCommunicationsController.updateNotificationItem()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `itemId` (UUID)
- Query Params:
  - `tab` (String) optional, default `PUSH_NOTIFICATIONS`
- Request Body Type: `AdminUpdateNotificationRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "title": "Sample title",
  "priority": "NORMAL"
}
```

Full body example:

```json
{
  "title": "Sample title",
  "message": "Sample text",
  "priority": "NORMAL",
  "sendNow": false
}
```

How to use:
- Send request to `{{host}}/v1/admin/communications/notifications/{{notification_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

