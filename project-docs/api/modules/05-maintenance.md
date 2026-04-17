# Maintenance Requests and Admin Command Center

Module key: 05-maintenance

Endpoint count: 19

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/maintenance-requests | Bearer token (authenticated user) | MaintenanceRequestController.createRequest |
| GET | /v1/maintenance-requests/{id} | Bearer token (authenticated user) | MaintenanceRequestController.getById |
| GET | /v1/maintenance-requests/resident/{residentId} | Bearer token (authenticated user) | MaintenanceRequestController.getByResident |
| GET | /v1/maintenance-requests/status/{status} | Bearer token (authenticated user) | MaintenanceRequestController.getByStatus |
| POST | /v1/maintenance-requests/{id}/assign | Bearer token (authenticated user) | MaintenanceRequestController.assignTechnician |
| POST | /v1/maintenance-requests/{id}/start | Bearer token (authenticated user) | MaintenanceRequestController.startWork |
| POST | /v1/maintenance-requests/{id}/resolve | Bearer token (authenticated user) | MaintenanceRequestController.resolve |
| POST | /v1/maintenance-requests/{id}/reject | Bearer token (authenticated user) | MaintenanceRequestController.reject |
| POST | /v1/maintenance-requests/{id}/cancel | Bearer token (authenticated user) | MaintenanceRequestController.cancel |
| GET | /v1/admin/maintenance/areas | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCommandCenterController.getAreaOptions |
| GET | /v1/admin/maintenance/categories | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCommandCenterController.getCategoryOptions |
| GET | /v1/admin/maintenance/requests | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCommandCenterController.getCommandCenter |
| POST | /v1/admin/maintenance/requests/{requestId}/assign | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCardController.assignTechnician |
| GET | /v1/admin/maintenance/requests/{requestId}/card | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCardController.getMaintenanceCard |
| PATCH | /v1/admin/maintenance/requests/{requestId}/priority | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCardController.updatePriority |
| GET | /v1/admin/maintenance/requests/{requestId}/receipt | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCardController.downloadReceipt |
| POST | /v1/admin/maintenance/requests/{requestId}/viewed | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCardController.markViewed |
| GET | /v1/admin/maintenance/technicians | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCardController.getTechnicians |
| GET | /v1/admin/maintenance/types | Admin Bearer token (ROLE_ADMIN) | AdminMaintenanceCommandCenterController.getTypeOptions |

## Endpoint Usage

### POST /v1/maintenance-requests

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:59`
- Handler: `MaintenanceRequestController.createRequest()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateMaintenanceRequestDto`
- Body Requirement: required

Minimal body example:

```json
{
  "residentId": "{{user_id}}",
  "unitId": "{{unit_id}}",
  "title": "Sample title",
  "description": "Sample text",
  "category": "PLUMBING",
  "priority": "LOW"
}
```

Full body example:

```json
{
  "residentId": "{{user_id}}",
  "unitId": "{{unit_id}}",
  "title": "Sample title",
  "description": "Sample text",
  "locationLabel": "sample",
  "category": "PLUMBING",
  "priority": "LOW",
  "isPublic": false,
  "photoUrls": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/maintenance-requests`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/maintenance-requests/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:84`
- Handler: `MaintenanceRequestController.getById()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `MaintenanceRequestResponseDto`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/maintenance-requests/{{maintenance_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/maintenance-requests/resident/{residentId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:90`
- Handler: `MaintenanceRequestController.getByResident()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<MaintenanceRequestResponseDto>`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/maintenance-requests/resident/{{residentId}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/maintenance-requests/status/{status}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:98`
- Handler: `MaintenanceRequestController.getByStatus()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<MaintenanceRequestResponseDto>`
- Path Params:
  - `status` (MaintenanceStatus)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/maintenance-requests/status/{{status}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/maintenance-requests/{id}/assign

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:106`
- Handler: `MaintenanceRequestController.assignTechnician()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `AssignTechnicianDto`
- Body Requirement: required

Minimal body example:

```json
{
  "technicianId": "00000000-0000-0000-0000-000000000000"
}
```

Full body example:

```json
{
  "technicianId": "00000000-0000-0000-0000-000000000000"
}
```

How to use:
- Send request to `{{host}}/v1/maintenance-requests/{{maintenance_id}}/assign`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/maintenance-requests/{id}/start

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:120`
- Handler: `MaintenanceRequestController.startWork()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/maintenance-requests/{{maintenance_id}}/start`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/maintenance-requests/{id}/resolve

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:134`
- Handler: `MaintenanceRequestController.resolve()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `ResolveMaintenanceRequestDto`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "resolution": "sample",
  "totalCost": 150.0
}
```

Full body example:

```json
{
  "resolution": "sample",
  "totalCost": 150.0
}
```

How to use:
- Send request to `{{host}}/v1/maintenance-requests/{{maintenance_id}}/resolve`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/maintenance-requests/{id}/reject

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:168`
- Handler: `MaintenanceRequestController.reject()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `RejectRequestDto`
- Body Requirement: required

Minimal body example:

```json
{
  "reason": "Sample text"
}
```

Full body example:

```json
{
  "reason": "Sample text"
}
```

How to use:
- Send request to `{{host}}/v1/maintenance-requests/{{maintenance_id}}/reject`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/maintenance-requests/{id}/cancel

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\MaintenanceRequestController.java:154`
- Handler: `MaintenanceRequestController.cancel()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/maintenance-requests/{{maintenance_id}}/cancel`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/maintenance/areas

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCommandCenterController.java:40`
- Handler: `AdminMaintenanceCommandCenterController.getAreaOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/areas`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/maintenance/categories

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCommandCenterController.java:50`
- Handler: `AdminMaintenanceCommandCenterController.getCategoryOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/categories`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/maintenance/requests

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCommandCenterController.java:27`
- Handler: `AdminMaintenanceCommandCenterController.getCommandCenter()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMaintenanceCommandCenterService.MaintenanceCommandCenterPage`
- Path Params: none
- Query Params:
  - `tab` (AdminMaintenanceTab) optional, default `ALL`
  - `area` (String) optional
  - `type` (AdminMaintenanceRequestType) optional
  - `category` (MaintenanceCategory) optional
  - `sortBy` (AdminMaintenanceSortBy) optional, default `NEWEST`
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/requests`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/maintenance/requests/{requestId}/assign

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCardController.java:114`
- Handler: `AdminMaintenanceCardController.assignTechnician()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `requestId` (UUID)
- Query Params: none
- Request Body Type: `AssignMaintenanceTechnicianRequest`
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
- Send request to `{{host}}/v1/admin/maintenance/requests/{{maintenance_id}}/assign`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/maintenance/requests/{requestId}/card

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCardController.java:48`
- Handler: `AdminMaintenanceCardController.getMaintenanceCard()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminMaintenanceCardService.MaintenanceManagementCardResponse`
- Path Params:
  - `requestId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/requests/{{maintenance_id}}/card`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/maintenance/requests/{requestId}/priority

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCardController.java:96`
- Handler: `AdminMaintenanceCardController.updatePriority()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `requestId` (UUID)
- Query Params: none
- Request Body Type: `UpdatePriorityRequest`
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
- Send request to `{{host}}/v1/admin/maintenance/requests/{{maintenance_id}}/priority`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/maintenance/requests/{requestId}/receipt

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCardController.java:55`
- Handler: `AdminMaintenanceCardController.downloadReceipt()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `byte[]`
- Path Params:
  - `requestId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/requests/{{maintenance_id}}/receipt`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/maintenance/requests/{requestId}/viewed

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCardController.java:135`
- Handler: `AdminMaintenanceCardController.markViewed()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `requestId` (UUID)
- Query Params: none
- Request Body Type: `MarkMaintenanceViewedRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "actorId": "00000000-0000-0000-0000-000000000000"
}
```

Full body example:

```json
{
  "actorId": "00000000-0000-0000-0000-000000000000"
}
```

How to use:
- Send request to `{{host}}/v1/admin/maintenance/requests/{{maintenance_id}}/viewed`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/maintenance/technicians

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCardController.java:89`
- Handler: `AdminMaintenanceCardController.getTechnicians()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<AdminMaintenanceCardService.TechnicianOption>`
- Path Params: none
- Query Params:
  - `availableOnly` (boolean) optional, default `true`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/technicians`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/maintenance/types

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\maintenance\internal\api\controllers\AdminMaintenanceCommandCenterController.java:45`
- Handler: `AdminMaintenanceCommandCenterController.getTypeOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/maintenance/types`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

