# Admin Employee Management

Module key: 12-admin-employees

Endpoint count: 9

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| GET | /v1/admin/employees | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.listEmployees |
| POST | /v1/admin/employees | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.createEmployee |
| DELETE | /v1/admin/employees/{employeeId} | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.deactivateEmployee |
| GET | /v1/admin/employees/{employeeId} | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.getEmployee |
| PATCH | /v1/admin/employees/{employeeId} | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.updateEmployee |
| POST | /v1/admin/employees/{employeeId}/reset-password | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.resetPassword |
| PATCH | /v1/admin/employees/{employeeId}/status | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.updateStatus |
| GET | /v1/admin/employees/roles | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.getRoleOptions |
| GET | /v1/admin/employees/statuses | Admin Bearer token (ROLE_ADMIN) | AdminEmployeesController.getStatusOptions |

## Endpoint Usage

### GET /v1/admin/employees

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:35`
- Handler: `AdminEmployeesController.listEmployees()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminEmployeeDirectoryService.AdminEmployeesDashboardResponse`
- Path Params: none
- Query Params:
  - `search` (String) optional
  - `role` (String) optional, default `ALL`
  - `status` (String) optional, default `ALL`
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/employees`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/employees

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:67`
- Handler: `AdminEmployeesController.createEmployee()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `AdminEmployeeDirectoryService.CreateEmployeeResult`
- Path Params: none
- Query Params: none
- Request Body Type: `AdminCreateEmployeeRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "fullName": "Sample name",
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!",
  "loginMethod": "PHONE_OTP"
}
```

Full body example:

```json
{
  "fullName": "Sample name",
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!",
  "role": "sample",
  "hireDate": "2027-01-01",
  "department": "sample",
  "isSuperAdmin": false,
  "isActive": false,
  "isPhoneVerified": false,
  "loginMethod": "PHONE_OTP",
  "scopePermissions": "sample",
  "specializations": "sample",
  "technicianAvailable": false
}
```

How to use:
- Send request to `{{host}}/v1/admin/employees`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/employees/{employeeId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:152`
- Handler: `AdminEmployeesController.deactivateEmployee()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `employeeId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/employees/{{employee_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/employees/{employeeId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:52`
- Handler: `AdminEmployeesController.getEmployee()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminEmployeeDirectoryService.AdminEmployeeItem`
- Path Params:
  - `employeeId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/employees/{{employee_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/employees/{employeeId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:96`
- Handler: `AdminEmployeesController.updateEmployee()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `employeeId` (UUID)
- Query Params: none
- Request Body Type: `AdminUpdateEmployeeRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "fullName": "Sample name",
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!",
  "loginMethod": "PHONE_OTP"
}
```

Full body example:

```json
{
  "fullName": "Sample name",
  "firstName": "Sample name",
  "lastName": "Sample name",
  "phoneNumber": "+201000000000",
  "email": "user@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!",
  "role": "sample",
  "hireDate": "2027-01-01",
  "department": "sample",
  "isSuperAdmin": false,
  "isActive": false,
  "isPhoneVerified": false,
  "loginMethod": "PHONE_OTP",
  "scopePermissions": "sample",
  "specializations": "sample",
  "technicianAvailable": false
}
```

How to use:
- Send request to `{{host}}/v1/admin/employees/{{employee_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### POST /v1/admin/employees/{employeeId}/reset-password

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:143`
- Handler: `AdminEmployeesController.resetPassword()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminEmployeeDirectoryService.ResetPasswordResult`
- Path Params:
  - `employeeId` (UUID)
- Query Params: none
- Request Body Type: `AdminResetPasswordRequest`
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
- Send request to `{{host}}/v1/admin/employees/{{employee_id}}/reset-password`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/admin/employees/{employeeId}/status

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:127`
- Handler: `AdminEmployeesController.updateStatus()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `employeeId` (UUID)
- Query Params: none
- Request Body Type: `AdminUpdateEmployeeStatusRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "isActive": false,
  "status": "OPEN"
}
```

Full body example:

```json
{
  "isActive": false,
  "status": "OPEN"
}
```

How to use:
- Send request to `{{host}}/v1/admin/employees/{{employee_id}}/status`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/employees/roles

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:57`
- Handler: `AdminEmployeesController.getRoleOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/employees/roles`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/employees/statuses

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\accounts\internal\api\controllers\AdminEmployeesController.java:62`
- Handler: `AdminEmployeesController.getStatusOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/employees/statuses`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

