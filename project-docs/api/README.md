# Sakany API Documentation and Collections

Generated from source controllers under `src/main/java/**/controllers/*Controller.java`.

Total endpoints discovered: 140

## Files

- `endpoint-inventory.csv`: canonical endpoint checklist
- `endpoint-metadata.json`: enriched metadata used for docs/collections generation
- `modules/*.md`: one module document per API area
- `collections/sakany-sequential.http`: sequential REST Client file
- `collections/sakany-sequential.postman_collection.json`: Postman import file

## Modules

| Module | Title | Endpoints | File |
|---|---|---:|---|
| `01-auth-users` | Authentication and Users | 6 | `modules/01-auth-users.md` |
| `02-properties` | Properties (Compound, Building, Unit) | 6 | `modules/02-properties.md` |
| `03-access-qr` | Access Codes and QR Access | 15 | `modules/03-access-qr.md` |
| `04-billing` | Billing (Invoices and Payments) | 6 | `modules/04-billing.md` |
| `05-maintenance` | Maintenance Requests and Admin Command Center | 19 | `modules/05-maintenance.md` |
| `06-feedback` | Feedback (Resident and Admin) | 13 | `modules/06-feedback.md` |
| `07-announcements-alerts` | Announcements and Alerts | 7 | `modules/07-announcements-alerts.md` |
| `08-events` | Events (Resident and Admin) | 19 | `modules/08-events.md` |
| `09-notifications-communications` | Notifications and Communications Center | 15 | `modules/09-notifications-communications.md` |
| `10-admin-missing-found` | Admin Missing and Found | 15 | `modules/10-admin-missing-found.md` |
| `11-admin-residents` | Admin Residents Directory | 10 | `modules/11-admin-residents.md` |
| `12-admin-employees` | Admin Employee Management | 9 | `modules/12-admin-employees.md` |

## Authentication Rules

- Public: `/v1/auth/register`, `/v1/auth/send-otp`, `/v1/auth/login/phone`, `/v1/auth/login/email`, `/v1/auth/refresh`
- Admin token required: all `/v1/admin/**` routes
- Authenticated user token required: all remaining `/v1/**` routes

## Execution Notes

- Start with auth endpoints to obtain `auth_token` and `user_id`.
- Use resident flow modules first, then admin modules.
- For admin endpoints, set `admin_token` to a valid admin JWT.
