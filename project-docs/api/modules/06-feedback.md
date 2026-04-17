# Feedback (Resident and Admin)

Module key: 06-feedback

Endpoint count: 13

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/feedback | Bearer token (authenticated user) | FeedbackController.submitFeedback |
| GET | /v1/feedback | Bearer token (authenticated user) | FeedbackController.getPublicFeedback |
| GET | /v1/feedback/me | Bearer token (authenticated user) | FeedbackController.getMyFeedback |
| POST | /v1/feedback/{id}/vote | Bearer token (authenticated user) | FeedbackController.voteFeedback |
| PATCH | /v1/feedback/{id}/status | Bearer token (authenticated user) | FeedbackController.updateFeedbackStatus |
| GET | /v1/admin/feedback | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.getDashboard |
| POST | /v1/admin/feedback | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.createFeedback |
| DELETE | /v1/admin/feedback/{feedbackId} | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.deleteFeedback |
| GET | /v1/admin/feedback/{feedbackId}/details | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.getFeedbackDetails |
| PATCH | /v1/admin/feedback/{feedbackId}/respond | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.respondToFeedback |
| PATCH | /v1/admin/feedback/{feedbackId}/status | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.updateFeedbackStatus |
| GET | /v1/admin/feedback/categories | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.getCategoryOptions |
| GET | /v1/admin/feedback/statuses | Admin Bearer token (ROLE_ADMIN) | AdminFeedbackController.getStatusOptions |

## Endpoint Usage

### POST /v1/feedback

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\FeedbackController.java:123`
- Handler: `FeedbackController.submitFeedback()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `SubmitFeedbackRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "title": "Sample title",
  "content": "Sample text",
  "type": "COMPLAINT",
  "category": "OTHER"
}
```

Full body example:

```json
{
  "title": "Sample title",
  "content": "Sample text",
  "type": "COMPLAINT",
  "isPublic": false,
  "category": "OTHER",
  "location": "sample",
  "isAnonymous": false,
  "imageUrl": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/feedback`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/feedback

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\FeedbackController.java:140`
- Handler: `FeedbackController.getPublicFeedback()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<FeedbackResponse>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/feedback`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/feedback/me

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\FeedbackController.java:147`
- Handler: `FeedbackController.getMyFeedback()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `MyFeedbackSummaryResponse`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/feedback/me`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/feedback/{id}/vote

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\FeedbackController.java:160`
- Handler: `FeedbackController.voteFeedback()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `VoteFeedbackRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "voteType": "UP"
}
```

Full body example:

```json
{
  "voteType": "UP"
}
```

How to use:
- Send request to `{{host}}/v1/feedback/{{feedback_id}}/vote`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/feedback/{id}/status

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\FeedbackController.java:167`
- Handler: `FeedbackController.updateFeedbackStatus()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `UpdateFeedbackStatusRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "newStatus": "OPEN"
}
```

Full body example:

```json
{
  "newStatus": "OPEN"
}
```

How to use:
- Send request to `{{host}}/v1/feedback/{{feedback_id}}/status`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/feedback

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:37`
- Handler: `AdminFeedbackController.getDashboard()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminFeedbackDashboardService.AdminFeedbackDashboardResponse`
- Path Params: none
- Query Params:
  - `tab` (AdminFeedbackTab) optional, default `PUBLIC_SUGGESTIONS`
  - `search` (String) optional
  - `status` (AdminFeedbackStatusFilter) optional, default `ALL`
  - `category` (String) optional
  - `page` (int) optional, default `0`
  - `size` (int) optional, default `25`
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/feedback`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/admin/feedback

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:64`
- Handler: `AdminFeedbackController.createFeedback()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `AdminCreateFeedbackRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "authorId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "content": "Sample text",
  "type": "COMPLAINT",
  "category": "OTHER"
}
```

Full body example:

```json
{
  "authorId": "00000000-0000-0000-0000-000000000000",
  "title": "Sample title",
  "content": "Sample text",
  "type": "COMPLAINT",
  "isPublic": false,
  "isPrivate": false,
  "visibility": "sample",
  "category": "OTHER",
  "location": "sample",
  "isAnonymous": false,
  "imageUrl": "sample",
  "imageUrls": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/admin/feedback`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### DELETE /v1/admin/feedback/{feedbackId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:114`
- Handler: `AdminFeedbackController.deleteFeedback()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `feedbackId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/feedback/{{feedback_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/feedback/{feedbackId}/details

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:59`
- Handler: `AdminFeedbackController.getFeedbackDetails()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `AdminFeedbackDashboardService.AdminFeedbackDetailsResponse`
- Path Params:
  - `feedbackId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/feedback/{{feedback_id}}/details`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/admin/feedback/{feedbackId}/respond

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:88`
- Handler: `AdminFeedbackController.respondToFeedback()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `feedbackId` (UUID)
- Query Params: none
- Request Body Type: `AdminRespondFeedbackRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "response": "sample",
  "newStatus": "ALL"
}
```

Full body example:

```json
{
  "response": "sample",
  "newStatus": "ALL"
}
```

How to use:
- Send request to `{{host}}/v1/admin/feedback/{{feedback_id}}/respond`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### PATCH /v1/admin/feedback/{feedbackId}/status

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:101`
- Handler: `AdminFeedbackController.updateFeedbackStatus()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `feedbackId` (UUID)
- Query Params: none
- Request Body Type: `AdminUpdateFeedbackStatusRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "newStatus": "ALL"
}
```

Full body example:

```json
{
  "newStatus": "ALL"
}
```

How to use:
- Send request to `{{host}}/v1/admin/feedback/{{feedback_id}}/status`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/admin/feedback/categories

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:49`
- Handler: `AdminFeedbackController.getCategoryOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/feedback/categories`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/admin/feedback/statuses

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\community\internal\api\controllers\AdminFeedbackController.java:54`
- Handler: `AdminFeedbackController.getStatusOptions()`
- Auth: Admin Bearer token (ROLE_ADMIN)
- Success: 200 OK
- Response Type: `List<String>`
- Path Params: none
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/admin/feedback/statuses`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

