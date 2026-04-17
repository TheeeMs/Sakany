# Admin Residents Directory

Module key: 11-admin-residents

Endpoint count: 10

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| GET | /v1/admin/residents | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.listResidents |
| POST | /v1/admin/residents | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.createResident |
| DELETE | /v1/admin/residents/{residentId} | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.deactivateResident |
| GET | /v1/admin/residents/{residentId} | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.getResident |
| PATCH | /v1/admin/residents/{residentId} | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.updateResident |
| PATCH | /v1/admin/residents/{residentId}/approval | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.updateApprovalStatus |
| GET | /v1/admin/residents/{residentId}/card | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.getResidentCard |
| POST | /v1/admin/residents/{residentId}/reset-password | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.resetResidentPassword |
| GET | /v1/admin/residents/buildings | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.listResidentBuildings |
| GET | /v1/admin/residents/units | Admin Bearer token (ROLE_ADMIN) | AdminResidentsController.listResidentUnits |

## Endpoint Usage

### GET /v1/admin/residents

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:89`
- Handler: `AdminResidentsController.listResidents()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminResidentDirectoryService.AdminResidentDirectoryPage`
- Path Params: none
- Query Params:
  - `search` (String) optional
  - `buildingId` (UUID) optional
  - `status` (ResidentDirectoryStatus) optional
  - `approvalStatus` (ResidentApprovalStatus) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/residents`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/residents

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:166`
- Handler: `AdminResidentsController.createResident()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateResidentRequest`
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
  "loginMethod": "PHONE_OTP",
  "approvalStatus": "PENDING",
  "nationalId": "sample"
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
  "loginMethod": "PHONE_OTP",
  "approvalStatus": "PENDING",
  "moveInDate": "2027-01-01",
  "monthlyFee": 150.0,
  "nationalId": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/admin/residents`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/residents/{residentId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:316`
- Handler: `AdminResidentsController.deactivateResident()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/residents/{{resident_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/residents/{residentId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:109`
- Handler: `AdminResidentsController.getResident()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200/201 depending on handler logic
- Response Type: `AdminResidentDirectoryService.AdminResidentDirectoryItem`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/residents/{{resident_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/residents/{residentId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:232`
- Handler: `AdminResidentsController.updateResident()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body Type: `UpdateResidentRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "unitId": "{{unit_id}}",
  "type": "PROPERTY_OWNER",
  "nationalId": "sample"
}
```

Full body example:

```json
{
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "isActive": false,
  "unitId": "{{unit_id}}",
  "type": "PROPERTY_OWNER",
  "moveInDate": "2027-01-01",
  "monthlyFee": 150.0,
  "nationalId": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/admin/residents/{{resident_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/admin/residents/{residentId}/approval

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:215`
- Handler: `AdminResidentsController.updateApprovalStatus()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body Type: `UpdateResidentApprovalRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "approvalStatus": "PENDING"
}
```

Full body example:

```json
{
  "approvalStatus": "PENDING"
}
```

How to use:
- Send request to `{{host}}/v1/admin/residents/{{resident_id}}/approval`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/residents/{residentId}/card

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:116`
- Handler: `AdminResidentsController.getResidentCard()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminResidentCardService.ResidentCardResponse`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/residents/{{resident_id}}/card`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/residents/{residentId}/reset-password

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:286`
- Handler: `AdminResidentsController.resetResidentPassword()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `ResetPasswordResponse`
- Path Params:
  - `residentId` (UUID)
- Query Params: none
- Request Body Type: `ResetPasswordRequest`
- Body Requirement: optional (`@RequestBody(required = false)`)

Minimal body example:

```json
{
  "newPassword": "Password123!"
}
```

Full body example:

```json
{
  "newPassword": "Password123!"
}
```

How to use:
- Send request to `{{host}}/v1/admin/residents/{{resident_id}}/reset-password`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/residents/buildings

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:121`
- Handler: `AdminResidentsController.listResidentBuildings()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<AdminResidentDirectoryService.AdminResidentBuildingOption>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/residents/buildings`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/residents/units

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminResidentsController.java:126`
- Handler: `AdminResidentsController.listResidentUnits()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<ResidentUnitOptionResponse>`
- Path Params: none
- Query Params:
  - `buildingId` (UUID) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/residents/units`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

