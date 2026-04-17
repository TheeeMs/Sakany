# Properties (Compound, Building, Unit)

Module key: 02-properties

Endpoint count: 6

## Endpoints Summary

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /v1/compounds | Bearer token (authenticated user) | CompoundController.createCompound |
| GET | /v1/compounds/{id} | Bearer token (authenticated user) | CompoundController.getCompoundById |
| POST | /v1/buildings | Bearer token (authenticated user) | BuildingController.createBuilding |
| GET | /v1/buildings/compound/{compoundId} | Bearer token (authenticated user) | BuildingController.getBuildingsByCompound |
| POST | /v1/units | Bearer token (authenticated user) | UnitController.createUnit |
| GET | /v1/units/building/{buildingId} | Bearer token (authenticated user) | UnitController.getUnitsByBuilding |

## Endpoint Usage

### POST /v1/compounds

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\property\internal\api\controllers\CompoundController.java:28`
- Handler: `CompoundController.createCompound()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateCompoundRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "name": "Sample name",
  "address": "sample"
}
```

Full body example:

```json
{
  "name": "Sample name",
  "address": "sample"
}
```

How to use:
- Send request to `{{host}}/v1/compounds`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/compounds/{id}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\property\internal\api\controllers\CompoundController.java:35`
- Handler: `CompoundController.getCompoundById()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `CompoundResponse`
- Path Params:
  - `id` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/compounds/{{compound_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/buildings

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\property\internal\api\controllers\BuildingController.java:29`
- Handler: `BuildingController.createBuilding()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateBuildingRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "compoundId": "{{compound_id}}",
  "name": "Sample name",
  "numberOfFloors": 1
}
```

Full body example:

```json
{
  "compoundId": "{{compound_id}}",
  "name": "Sample name",
  "numberOfFloors": 1
}
```

How to use:
- Send request to `{{host}}/v1/buildings`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/buildings/compound/{compoundId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\property\internal\api\controllers\BuildingController.java:36`
- Handler: `BuildingController.getBuildingsByCompound()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<BuildingResponse>`
- Path Params:
  - `compoundId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/buildings/compound/{{compound_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

### POST /v1/units

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\property\internal\api\controllers\UnitController.java:29`
- Handler: `UnitController.createUnit()`
- Auth: Bearer token (authenticated user)
- Success: 201 Created
- Response Type: `UUID`
- Path Params: none
- Query Params: none
- Request Body Type: `CreateUnitRequest`
- Body Requirement: required

Minimal body example:

```json
{
  "buildingId": "{{building_id}}",
  "unitNumber": "sample",
  "type": "APARTMENT"
}
```

Full body example:

```json
{
  "buildingId": "{{building_id}}",
  "unitNumber": "sample",
  "floor": 1,
  "type": "APARTMENT"
}
```

How to use:
- Send request to `{{host}}/v1/units`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values
- Start with minimal body, then extend to full body for advanced use cases

### GET /v1/units/building/{buildingId}

- Source: `C:\Users\M16-PC\Projects\Sakany\src\main\java\com\theMs\sakany\property\internal\api\controllers\UnitController.java:36`
- Handler: `UnitController.getUnitsByBuilding()`
- Auth: Bearer token (authenticated user)
- Success: 200 OK
- Response Type: `List<UnitResponse>`
- Path Params:
  - `buildingId` (UUID)
- Query Params: none
- Request Body: none

How to use:
- Send request to `{{host}}/v1/units/building/{{building_id}}`
- Include `Authorization: Bearer <token>` unless endpoint is public
- For path/query params, replace placeholders with real IDs or filter values

