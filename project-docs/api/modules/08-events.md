# Events (Resident and Admin)

Module key: 08-events

Endpoint count: 19

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/events | Bearer token (authenticated user) | EventController.proposeEvent |
| GET | /v1/events | Bearer token (authenticated user) | EventController.listEvents |
| GET | /v1/events/{id} | Bearer token (authenticated user) | EventController.getEventDetails |
| PATCH | /v1/events/{id}/approve | Bearer token (authenticated user) | EventController.approveEvent |
| PATCH | /v1/events/{id}/reject | Bearer token (authenticated user) | EventController.rejectEvent |
| POST | /v1/events/{id}/register | Bearer token (authenticated user) | EventController.registerForEvent |
| DELETE | /v1/events/{id}/register | Bearer token (authenticated user) | EventController.cancelRegistration |
| GET | /v1/admin/events | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.getDashboard |
| POST | /v1/admin/events | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.createEvent |
| DELETE | /v1/admin/events/{eventId} | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.deleteEvent |
| PATCH | /v1/admin/events/{eventId} | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.editEvent |
| PATCH | /v1/admin/events/{eventId}/approve | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.approveEvent |
| GET | /v1/admin/events/{eventId}/attendees/export | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.exportAttendees |
| PATCH | /v1/admin/events/{eventId}/complete | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.completeEvent |
| GET | /v1/admin/events/{eventId}/details | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.getEventCardDetails |
| POST | /v1/admin/events/{eventId}/notify-residents | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.notifyResidents |
| PATCH | /v1/admin/events/{eventId}/reject | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.rejectEvent |
| GET | /v1/admin/events/categories | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.getCategoryOptions |
| GET | /v1/admin/events/statuses | Admin Bearer token (ROLE_ADMIN) | AdminEventsController.getStatusOptions |

## Endpoint Usage

### POST /v1/events

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:47`
- Handler: `EventController.proposeEvent()`
- Auth: Bearer token (authenticated user)
- Success: 200/201 depending on handler logic
- Response Type: `Void`
- Path Params: none
- Query Params: none
- Request Body Type: `ProposeEventCommand`
- Body Requirement: required

Minimal body example:

```json
{
  "organizerId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "description": "Sample text",
  "hostName": "Sample name",
  "category": "OTHER",
  "contactPhone": "+201000000000"
}
```

Full body example:

```json
{
  "organizerId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "description": "Sample text",
  "location": "sample",
  "startDate": "2027-01-01T10:00:00Z",
  "endDate": "2027-01-01T10:00:00Z",
  "imageUrl": "sample",
  "hostName": "Sample name",
  "price": 150.0,
  "maxAttendees": 1,
  "category": "OTHER",
  "hostRole": "sample",
  "contactPhone": "+201000000000",
  "latitude": 150.0,
  "longitude": 150.0,
  "tags": "sample",
  "recurringEvent": false
}
```

How to use:
- Send request to `{{host}}/v1/events`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/events

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:58`
- Handler: `EventController.listEvents()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<EventDto>`
- Path Params: none
- Query Params:
  - `status` (EventStatus) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/events`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/events/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:64`
- Handler: `EventController.getEventDetails()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `EventDto`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/events/{{event_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/events/{id}/approve

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:71`
- Handler: `EventController.approveEvent()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params:
  - `adminId` (UUID)
- Request Body: none

How to use:
- Send request to `{{host}}/v1/events/{{event_id}}/approve`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/events/{id}/reject

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:77`
- Handler: `EventController.rejectEvent()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params:
  - `adminId` (UUID)
- Request Body: none

How to use:
- Send request to `{{host}}/v1/events/{{event_id}}/reject`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/events/{id}/register

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:83`
- Handler: `EventController.registerForEvent()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params:
  - `residentId` (UUID)
- Request Body: none

How to use:
- Send request to `{{host}}/v1/events/{{event_id}}/register`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### DELETE /v1/events/{id}/register

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\EventController.java:89`
- Handler: `EventController.cancelRegistration()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params:
  - `residentId` (UUID)
- Request Body: none

How to use:
- Send request to `{{host}}/v1/events/{{event_id}}/register`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/events

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:78`
- Handler: `AdminEventsController.getDashboard()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminEventsManagerService.AdminEventsDashboardResponse`
- Path Params: none
- Query Params:
  - `search` (String) optional
  - `status` (AdminEventStatusFilter) optional, default `ALL`
  - `category` (String) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `9`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/events`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/events

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:99`
- Handler: `AdminEventsController.createEvent()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `AdminCreateEventRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "organizerId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "description": "Sample text",
  "hostName": "Sample name",
  "category": "OTHER",
  "contactPhone": "+201000000000"
}
```

Full body example:

```json
{
  "organizerId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "description": "Sample text",
  "location": "sample",
  "startDate": "2027-01-01T10:00:00Z",
  "endDate": "2027-01-01T10:00:00Z",
  "imageUrl": "sample",
  "hostName": "Sample name",
  "price": 150.0,
  "maxAttendees": 1,
  "category": "OTHER",
  "hostRole": "sample",
  "contactPhone": "+201000000000",
  "latitude": 150.0,
  "longitude": 150.0,
  "date": "sample",
  "time": "sample",
  "duration": 1,
  "durationUnit": "sample",
  "tags": "VALUE",
  "recurringEvent": false
}
```

How to use:
- Send request to `{{host}}/v1/admin/events`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/events/{eventId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:185`
- Handler: `AdminEventsController.deleteEvent()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `eventId` (UUID)
- Query Params:
  - `adminId` (UUID) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/events/{eventId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:154`
- Handler: `AdminEventsController.editEvent()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body Type: `AdminEventCardMenuService.AdminUpdateEventRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "sample": "value"
}
```

Full body example:

```json
{
  "sample": "value"
}
```

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/admin/events/{eventId}/approve

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:128`
- Handler: `AdminEventsController.approveEvent()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body Type: `AdminActorRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000"
}
```

Full body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000"
}
```

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}/approve`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/events/{eventId}/attendees/export

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:176`
- Handler: `AdminEventsController.exportAttendees()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `byte[]`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}/attendees/export`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/events/{eventId}/complete

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:142`
- Handler: `AdminEventsController.completeEvent()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body Type: `AdminActorRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000"
}
```

Full body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000"
}
```

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}/complete`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/events/{eventId}/details

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:149`
- Handler: `AdminEventsController.getEventCardDetails()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminEventCardMenuService.AdminEventCardDetailsResponse`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}/details`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/events/{eventId}/notify-residents

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:163`
- Handler: `AdminEventsController.notifyResidents()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminEventCardMenuService.NotifyResidentsResult`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body Type: `AdminNotifyResidentsRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "title": "Sample title",
  "message": "Sample text",
  "channel": "PUSH"
}
```

Full body example:

```json
{
  "title": "Sample title",
  "message": "Sample text",
  "channel": "PUSH"
}
```

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}/notify-residents`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/admin/events/{eventId}/reject

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:135`
- Handler: `AdminEventsController.rejectEvent()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `eventId` (UUID)
- Query Params: none
- Request Body Type: `AdminActorRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000"
}
```

Full body example:

```json
{
  "adminId": "00000000-0000-0000-0000-000000000000"
}
```

How to use:
- Send request to `{{host}}/v1/admin/events/{{event_id}}/reject`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/events/categories

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:89`
- Handler: `AdminEventsController.getCategoryOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/events/categories`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/events/statuses

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\events\internal\api\controllers\AdminEventsController.java:94`
- Handler: `AdminEventsController.getStatusOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/events/statuses`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

