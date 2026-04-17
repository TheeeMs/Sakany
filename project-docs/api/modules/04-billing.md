# Billing (Invoices and Payments)

Module key: 04-billing

Endpoint count: 6

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/invoices | Bearer token (authenticated user) | InvoiceController.issueInvoice |
| GET | /v1/invoices | Bearer token (authenticated user) | InvoiceController.listInvoices |
| GET | /v1/invoices/{id} | Bearer token (authenticated user) | InvoiceController.getInvoiceById |
| POST | /v1/invoices/{id}/pay | Bearer token (authenticated user) | InvoiceController.payInvoice |
| GET | /v1/payments | Bearer token (authenticated user) | PaymentController.listPayments |
| PATCH | /v1/invoices/{id}/cancel | Bearer token (authenticated user) | InvoiceController.cancelInvoice |

## Endpoint Usage

### POST /v1/invoices

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\billing\internal\api\controllers\InvoiceController.java:56`
- Handler: `InvoiceController.issueInvoice()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `IssueInvoiceRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "residentId": "{{user_id}}",
  "unitId": "{{unit_id}}",
  "type": "MONTHLY_FEE",
  "amount": 150.0,
  "description": "Sample text"
}
```

Full body example:

```json
{
  "residentId": "{{user_id}}",
  "unitId": "{{unit_id}}",
  "type": "MONTHLY_FEE",
  "amount": 150.0,
  "currency": "USD",
  "description": "Sample text",
  "dueDate": "2027-01-01"
}
```

How to use:
- Send request to `{{host}}/v1/invoices`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/invoices

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\billing\internal\api\controllers\InvoiceController.java:71`
- Handler: `InvoiceController.listInvoices()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<InvoiceResponse>`
- Path Params: none
- Query Params:
  - `residentId` (UUID) optional
  - `status` (InvoiceStatus) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/invoices`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### GET /v1/invoices/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\billing\internal\api\controllers\InvoiceController.java:83`
- Handler: `InvoiceController.getInvoiceById()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `InvoiceResponse`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/invoices/{{invoice_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/invoices/{id}/pay

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\billing\internal\api\controllers\InvoiceController.java:89`
- Handler: `InvoiceController.payInvoice()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body Type: `PayInvoiceRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "residentId": "{{user_id}}",
  "paymentMethod": "CASH",
  "transactionReference": "tx-123456"
}
```

Full body example:

```json
{
  "residentId": "{{user_id}}",
  "paymentMethod": "CASH",
  "transactionReference": "tx-123456"
}
```

How to use:
- Send request to `{{host}}/v1/invoices/{{invoice_id}}/pay`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/payments

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\billing\internal\api\controllers\PaymentController.java:27`
- Handler: `PaymentController.listPayments()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<PaymentResponse>`
- Path Params: none
- Query Params:
  - `residentId` (UUID) optional
  - `status` (PaymentStatus) optional
- Request Body: none

How to use:
- Send request to `{{host}}/v1/payments`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### PATCH /v1/invoices/{id}/cancel

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\billing\internal\api\controllers\InvoiceController.java:101`
- Handler: `InvoiceController.cancelInvoice()`
- Auth: Bearer token (authenticated user)
- Success: 204 No Content
- Response Type: `Void`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/invoices/{{invoice_id}}/cancel`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

