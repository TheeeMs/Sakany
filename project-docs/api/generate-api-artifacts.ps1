$ErrorActionPreference = 'Stop'

$repoRoot = Get-Location
$sourceRoot = Join-Path $repoRoot 'src/main/java'
$apiRoot = Join-Path $repoRoot 'project-docs/api'
$modulesRoot = Join-Path $apiRoot 'modules'
$collectionsRoot = Join-Path $apiRoot 'collections'

New-Item -ItemType Directory -Force -Path $apiRoot | Out-Null
New-Item -ItemType Directory -Force -Path $modulesRoot | Out-Null
New-Item -ItemType Directory -Force -Path $collectionsRoot | Out-Null

function Get-ModuleKey([string]$path) {
    if ($path.StartsWith('/v1/auth')) { return '01-auth-users' }
    if ($path.StartsWith('/v1/compounds') -or $path.StartsWith('/v1/buildings') -or $path.StartsWith('/v1/units')) { return '02-properties' }
    if ($path.StartsWith('/v1/access') -or $path.StartsWith('/v1/admin/access')) { return '03-access-qr' }
    if ($path.StartsWith('/v1/invoices') -or $path.StartsWith('/v1/payments')) { return '04-billing' }
    if ($path.StartsWith('/v1/maintenance-requests') -or $path.StartsWith('/v1/admin/maintenance')) { return '05-maintenance' }
    if ($path.StartsWith('/v1/feedback') -or $path.StartsWith('/v1/admin/feedback')) { return '06-feedback' }
    if ($path.StartsWith('/v1/announcements') -or $path.StartsWith('/v1/alerts')) { return '07-announcements-alerts' }
    if ($path.StartsWith('/v1/events') -or $path.StartsWith('/v1/admin/events')) { return '08-events' }
    if ($path.StartsWith('/v1/notifications') -or $path.StartsWith('/v1/device-tokens') -or $path.StartsWith('/v1/admin/communications')) { return '09-notifications-communications' }
    if ($path.StartsWith('/v1/admin/missing-found')) { return '10-admin-missing-found' }
    if ($path.StartsWith('/v1/admin/residents')) { return '11-admin-residents' }
    if ($path.StartsWith('/v1/admin/employees')) { return '12-admin-employees' }
    return '99-other'
}

$moduleTitles = @{
    '01-auth-users' = 'Authentication and Users'
    '02-properties' = 'Properties (Compound, Building, Unit)'
    '03-access-qr' = 'Access Codes and QR Access'
    '04-billing' = 'Billing (Invoices and Payments)'
    '05-maintenance' = 'Maintenance Requests and Admin Command Center'
    '06-feedback' = 'Feedback (Resident and Admin)'
    '07-announcements-alerts' = 'Announcements and Alerts'
    '08-events' = 'Events (Resident and Admin)'
    '09-notifications-communications' = 'Notifications and Communications Center'
    '10-admin-missing-found' = 'Admin Missing and Found'
    '11-admin-residents' = 'Admin Residents Directory'
    '12-admin-employees' = 'Admin Employee Management'
    '99-other' = 'Other Endpoints'
}

$publicPaths = @(
    '/v1/auth/register',
    '/v1/auth/send-otp',
    '/v1/auth/login/phone',
    '/v1/auth/login/email',
    '/v1/auth/refresh'
)

function Get-AuthScope([string]$path) {
    if ($path.StartsWith('/v1/admin/')) { return 'Admin Bearer token (ROLE_ADMIN)' }
    if ($publicPaths -contains $path) { return 'Public (no token)' }
    return 'Bearer token (authenticated user)'
}

function Parse-RecordFields([string]$fieldsRaw) {
    $fields = @()
    $clean = $fieldsRaw -split "`n"
    foreach ($line in $clean) {
        $l = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($l)) { continue }
        $l = $l.Trim(',')
        if ($l.StartsWith('@')) { continue }
        $l = $l -replace '\s+', ' '
        if ($l -match '^([A-Za-z0-9_<>,\.\[\]\?]+)\s+([A-Za-z0-9_]+)$') {
            $fields += [pscustomobject]@{
                type = $Matches[1]
                name = $Matches[2]
            }
        }
    }
    return $fields
}

function Parse-ClassFields([string]$classBlock) {
    $fields = @()
    $matches = [regex]::Matches($classBlock, '(?m)^\s*(?:public|private|protected)\s+(?!static)(?!final)([A-Za-z0-9_<>,\.\[\]\?]+)\s+([A-Za-z0-9_]+)\s*;')
    foreach ($m in $matches) {
        $fields += [pscustomobject]@{
            type = $m.Groups[1].Value
            name = $m.Groups[2].Value
        }
    }
    return $fields
}

function Build-TypeMaps {
    $javaFiles = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java
    $typeMap = @{}
    $enumMap = @{}

    foreach ($file in $javaFiles) {
        $raw = Get-Content -Raw -Path $file.FullName

        $enumMatches = [regex]::Matches($raw, 'enum\s+([A-Za-z0-9_]+)\s*\{([\s\S]*?)\}', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        foreach ($em in $enumMatches) {
            $enumName = $em.Groups[1].Value
            $body = $em.Groups[2].Value
            $firstConst = ($body -split ',|;')[0].Trim()
            if (-not [string]::IsNullOrWhiteSpace($firstConst) -and -not $enumMap.ContainsKey($enumName)) {
                $enumMap[$enumName] = $firstConst
            }
        }

        $recordMatches = [regex]::Matches($raw, 'record\s+([A-Za-z0-9_]+)\s*\((?<fields>[\s\S]*?)\)\s*(?:implements[^{]+)?\{', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        foreach ($rm in $recordMatches) {
            $typeName = $rm.Groups[1].Value
            if (-not $typeMap.ContainsKey($typeName)) {
                $typeMap[$typeName] = [pscustomobject]@{
                    kind = 'record'
                    file = $file.FullName.Replace($repoRoot.Path + '\\', '').Replace('\\', '/')
                    fields = Parse-RecordFields $rm.Groups['fields'].Value
                }
            }
        }

        $classMatches = [regex]::Matches($raw, 'class\s+([A-Za-z0-9_]+)\s*\{')
        foreach ($cm in $classMatches) {
            $className = $cm.Groups[1].Value
            if ($className.EndsWith('Controller')) { continue }
            if ($typeMap.ContainsKey($className)) { continue }

            $start = $cm.Index + $cm.Length - 1
            $depth = 0
            $end = -1
            for ($i = $start; $i -lt $raw.Length; $i++) {
                $ch = $raw[$i]
                if ($ch -eq '{') { $depth++ }
                elseif ($ch -eq '}') {
                    $depth--
                    if ($depth -eq 0) {
                        $end = $i
                        break
                    }
                }
            }
            if ($end -gt $start) {
                $block = $raw.Substring($start, $end - $start + 1)
                $fields = Parse-ClassFields $block
                if ($fields.Count -gt 0) {
                    $typeMap[$className] = [pscustomobject]@{
                        kind = 'class'
                        file = $file.FullName.Replace($repoRoot.Path + '\\', '').Replace('\\', '/')
                        fields = $fields
                    }
                }
            }
        }
    }

    return [pscustomobject]@{ typeMap = $typeMap; enumMap = $enumMap }
}

function Normalize-Type([string]$t) {
    if ([string]::IsNullOrWhiteSpace($t)) { return '' }
    $r = $t.Trim()
    $r = $r -replace '\s+', ' '
    return $r
}

function Get-SampleValue([string]$type, [string]$name, $enumMap) {
    $type = Normalize-Type $type
    $nameLower = $name.ToLowerInvariant()

    if ($type -match '^List<(.+)>$') {
        $inner = $Matches[1].Trim()
        return @((Get-SampleValue $inner $name $enumMap))
    }

    if ($type -eq 'UUID') {
        if ($nameLower -like '*user*') { return '{{user_id}}' }
        if ($nameLower -like '*resident*') { return '{{user_id}}' }
        if ($nameLower -like '*unit*') { return '{{unit_id}}' }
        if ($nameLower -like '*building*') { return '{{building_id}}' }
        if ($nameLower -like '*compound*') { return '{{compound_id}}' }
        if ($nameLower -like '*invoice*') { return '{{invoice_id}}' }
        if ($nameLower -like '*feedback*') { return '{{feedback_id}}' }
        if ($nameLower -like '*event*') { return '{{event_id}}' }
        if ($nameLower -like '*alert*') { return '{{alert_id}}' }
        if ($nameLower -like '*request*') { return '{{maintenance_id}}' }
        return '00000000-0000-0000-0000-000000000000'
    }

    if ($type -in @('String', 'CharSequence')) {
        if ($nameLower -like '*email*') { return 'user@example.com' }
        if ($nameLower -like '*password*') { return 'Password123!' }
        if ($nameLower -like '*phone*' -or $nameLower -like '*contact*') { return '+201000000000' }
        if ($nameLower -like '*title*') { return 'Sample title' }
        if ($nameLower -like '*name*') { return 'Sample name' }
        if ($nameLower -like '*description*' -or $nameLower -like '*content*' -or $nameLower -like '*message*' -or $nameLower -like '*reason*') { return 'Sample text' }
        if ($nameLower -like '*status*') { return 'OPEN' }
        if ($nameLower -like '*category*') { return 'OTHER' }
        if ($nameLower -like '*priority*') { return 'NORMAL' }
        if ($nameLower -like '*platform*') { return 'WEB' }
        if ($nameLower -like '*currency*') { return 'USD' }
        if ($nameLower -like '*transaction*') { return 'tx-123456' }
        return 'sample'
    }

    if ($type -in @('int', 'Integer', 'long', 'Long', 'short', 'Short')) { return 1 }
    if ($type -in @('double', 'Double', 'float', 'Float', 'BigDecimal')) { return 150.0 }
    if ($type -in @('boolean', 'Boolean')) { return $false }
    if ($type -in @('Instant', 'OffsetDateTime', 'ZonedDateTime')) { return '2027-01-01T10:00:00Z' }
    if ($type -in @('LocalDateTime')) { return '2027-01-01T10:00:00' }
    if ($type -in @('LocalDate')) { return '2027-01-01' }

    if ($enumMap.ContainsKey($type)) {
        return $enumMap[$type]
    }

    return 'VALUE'
}

function Build-FullBody($fields, $enumMap) {
    $obj = [ordered]@{}
    foreach ($f in $fields) {
        $obj[$f.name] = Get-SampleValue $f.type $f.name $enumMap
    }
    return $obj
}

function Build-MinimalBody($fields, $enumMap) {
    if ($fields.Count -le 3) { return Build-FullBody $fields $enumMap }

    $required = @()
    foreach ($f in $fields) {
        $n = $f.name.ToLowerInvariant()
        if ($n -match '(^id$|id$|title|content|description|name|email|password|phone|resident|unit|organizer|author|reporter|recipient|purpose|status|type|category|priority|amount|method|transaction|validfrom|validuntil)') {
            if ($n -notmatch '(image|photo|url|latitude|longitude|tags|expires|schedule|duration|anonymous|public|active|verified|permissions|specializations)') {
                $required += $f
            }
        }
    }

    if ($required.Count -eq 0) {
        $required = $fields | Select-Object -First 3
    }

    return Build-FullBody $required $enumMap
}

function Convert-PathToHttp([string]$path) {
    $specificMap = [ordered]@{
        '/v1/compounds/{id}' = '/v1/compounds/{{compound_id}}'
        '/v1/buildings/compound/{compoundId}' = '/v1/buildings/compound/{{compound_id}}'
        '/v1/units/building/{buildingId}' = '/v1/units/building/{{building_id}}'
        '/v1/access/codes/{id}' = '/v1/access/codes/{{access_code_id}}'
        '/v1/access/codes/{code}' = '/v1/access/codes/{{access_code}}'
        '/v1/access/codes/{codeId}/reactivate' = '/v1/access/codes/{{access_code_id}}/reactivate'
        '/v1/access/visit-logs/{id}/exit' = '/v1/access/visit-logs/{{visit_log_id}}/exit'
        '/v1/invoices/{id}' = '/v1/invoices/{{invoice_id}}'
        '/v1/invoices/{id}/pay' = '/v1/invoices/{{invoice_id}}/pay'
        '/v1/invoices/{id}/cancel' = '/v1/invoices/{{invoice_id}}/cancel'
        '/v1/maintenance-requests/{id}' = '/v1/maintenance-requests/{{maintenance_id}}'
        '/v1/maintenance-requests/{id}/assign' = '/v1/maintenance-requests/{{maintenance_id}}/assign'
        '/v1/maintenance-requests/{id}/start' = '/v1/maintenance-requests/{{maintenance_id}}/start'
        '/v1/maintenance-requests/{id}/resolve' = '/v1/maintenance-requests/{{maintenance_id}}/resolve'
        '/v1/maintenance-requests/{id}/cancel' = '/v1/maintenance-requests/{{maintenance_id}}/cancel'
        '/v1/maintenance-requests/{id}/reject' = '/v1/maintenance-requests/{{maintenance_id}}/reject'
        '/v1/feedback/{id}/vote' = '/v1/feedback/{{feedback_id}}/vote'
        '/v1/feedback/{id}/status' = '/v1/feedback/{{feedback_id}}/status'
        '/v1/announcements/{id}/deactivate' = '/v1/announcements/{{announcement_id}}/deactivate'
        '/v1/alerts/{id}' = '/v1/alerts/{{alert_id}}'
        '/v1/alerts/{id}/resolve' = '/v1/alerts/{{alert_id}}/resolve'
        '/v1/events/{id}' = '/v1/events/{{event_id}}'
        '/v1/events/{id}/approve' = '/v1/events/{{event_id}}/approve'
        '/v1/events/{id}/reject' = '/v1/events/{{event_id}}/reject'
        '/v1/events/{id}/register' = '/v1/events/{{event_id}}/register'
        '/v1/notifications/{id}/read' = '/v1/notifications/{{notification_id}}/read'
        '/v1/admin/access/codes/{accessCodeId}' = '/v1/admin/access/codes/{{access_code_id}}'
        '/v1/admin/access/codes/{accessCodeId}/details' = '/v1/admin/access/codes/{{access_code_id}}/details'
        '/v1/admin/access/codes/{accessCodeId}/download' = '/v1/admin/access/codes/{{access_code_id}}/download'
        '/v1/admin/access/residents/{residentId}/codes' = '/v1/admin/access/residents/{{resident_id}}/codes'
        '/v1/admin/feedback/{feedbackId}/details' = '/v1/admin/feedback/{{feedback_id}}/details'
        '/v1/admin/feedback/{feedbackId}/respond' = '/v1/admin/feedback/{{feedback_id}}/respond'
        '/v1/admin/feedback/{feedbackId}/status' = '/v1/admin/feedback/{{feedback_id}}/status'
        '/v1/admin/feedback/{feedbackId}' = '/v1/admin/feedback/{{feedback_id}}'
        '/v1/admin/missing-found/reports/{reportId}' = '/v1/admin/missing-found/reports/{{admin_report_id}}'
        '/v1/admin/missing-found/reports/{reportId}/details' = '/v1/admin/missing-found/reports/{{admin_report_id}}/details'
        '/v1/admin/missing-found/reports/{reportId}/status' = '/v1/admin/missing-found/reports/{{admin_report_id}}/status'
        '/v1/admin/missing-found/reports/{reportId}/mark-matched' = '/v1/admin/missing-found/reports/{{admin_report_id}}/mark-matched'
        '/v1/admin/missing-found/reports/{reportId}/mark-resolved' = '/v1/admin/missing-found/reports/{{admin_report_id}}/mark-resolved'
        '/v1/admin/missing-found/reports/{reportId}/notify-user' = '/v1/admin/missing-found/reports/{{admin_report_id}}/notify-user'
        '/v1/admin/events/{eventId}/approve' = '/v1/admin/events/{{event_id}}/approve'
        '/v1/admin/events/{eventId}/reject' = '/v1/admin/events/{{event_id}}/reject'
        '/v1/admin/events/{eventId}/complete' = '/v1/admin/events/{{event_id}}/complete'
        '/v1/admin/events/{eventId}/details' = '/v1/admin/events/{{event_id}}/details'
        '/v1/admin/events/{eventId}' = '/v1/admin/events/{{event_id}}'
        '/v1/admin/events/{eventId}/notify-residents' = '/v1/admin/events/{{event_id}}/notify-residents'
        '/v1/admin/events/{eventId}/attendees/export' = '/v1/admin/events/{{event_id}}/attendees/export'
        '/v1/admin/maintenance/requests/{requestId}/card' = '/v1/admin/maintenance/requests/{{maintenance_id}}/card'
        '/v1/admin/maintenance/requests/{requestId}/receipt' = '/v1/admin/maintenance/requests/{{maintenance_id}}/receipt'
        '/v1/admin/maintenance/requests/{requestId}/priority' = '/v1/admin/maintenance/requests/{{maintenance_id}}/priority'
        '/v1/admin/maintenance/requests/{requestId}/assign' = '/v1/admin/maintenance/requests/{{maintenance_id}}/assign'
        '/v1/admin/maintenance/requests/{requestId}/viewed' = '/v1/admin/maintenance/requests/{{maintenance_id}}/viewed'
        '/v1/admin/communications/notifications/{itemId}' = '/v1/admin/communications/notifications/{{notification_id}}'
        '/v1/admin/communications/announcements/{announcementId}' = '/v1/admin/communications/announcements/{{announcement_id}}'
        '/v1/admin/residents/{residentId}' = '/v1/admin/residents/{{resident_id}}'
        '/v1/admin/residents/{residentId}/card' = '/v1/admin/residents/{{resident_id}}/card'
        '/v1/admin/residents/{residentId}/approval' = '/v1/admin/residents/{{resident_id}}/approval'
        '/v1/admin/residents/{residentId}/reset-password' = '/v1/admin/residents/{{resident_id}}/reset-password'
        '/v1/admin/employees/{employeeId}' = '/v1/admin/employees/{{employee_id}}'
        '/v1/admin/employees/{employeeId}/status' = '/v1/admin/employees/{{employee_id}}/status'
        '/v1/admin/employees/{employeeId}/reset-password' = '/v1/admin/employees/{{employee_id}}/reset-password'
    }

    if ($specificMap.Contains($path)) {
        return $specificMap[$path]
    }

    return ([regex]::Replace($path, '\{([^}]+)\}', '{{$1}}'))
}

function Get-SequenceRank($ep) {
    $key = $ep.method + ' ' + $ep.path
    $map = @{
        'POST /v1/auth/register' = 10
        'POST /v1/auth/send-otp' = 20
        'POST /v1/auth/login/phone' = 30
        'POST /v1/auth/login/email' = 40
        'POST /v1/auth/refresh' = 50
        'GET /v1/auth/me' = 60

        'POST /v1/compounds' = 110
        'GET /v1/compounds/{id}' = 120
        'POST /v1/buildings' = 130
        'GET /v1/buildings/compound/{compoundId}' = 140
        'POST /v1/units' = 150
        'GET /v1/units/building/{buildingId}' = 160

        'POST /v1/access/codes' = 210
        'GET /v1/access/codes/my' = 220
        'GET /v1/access/codes/{id}' = 230
        'POST /v1/access/codes/{code}/scan' = 240
        'GET /v1/access/visit-logs' = 250
        'PATCH /v1/access/visit-logs/{id}/exit' = 260
        'POST /v1/access/codes/{codeId}/reactivate' = 270
        'DELETE /v1/access/codes/{id}' = 280

        'POST /v1/invoices' = 310
        'GET /v1/invoices' = 320
        'GET /v1/invoices/{id}' = 330
        'POST /v1/invoices/{id}/pay' = 340
        'GET /v1/payments' = 350
        'PATCH /v1/invoices/{id}/cancel' = 360

        'POST /v1/maintenance-requests' = 410
        'GET /v1/maintenance-requests/{id}' = 420
        'GET /v1/maintenance-requests/resident/{residentId}' = 430
        'GET /v1/maintenance-requests/status/{status}' = 440
        'POST /v1/maintenance-requests/{id}/assign' = 450
        'POST /v1/maintenance-requests/{id}/start' = 460
        'POST /v1/maintenance-requests/{id}/resolve' = 470
        'POST /v1/maintenance-requests/{id}/reject' = 480
        'POST /v1/maintenance-requests/{id}/cancel' = 490

        'POST /v1/feedback' = 510
        'GET /v1/feedback' = 520
        'GET /v1/feedback/me' = 530
        'POST /v1/feedback/{id}/vote' = 540
        'PATCH /v1/feedback/{id}/status' = 550

        'POST /v1/announcements' = 610
        'GET /v1/announcements' = 620
        'PATCH /v1/announcements/{id}/deactivate' = 630
        'POST /v1/alerts' = 640
        'GET /v1/alerts' = 650
        'GET /v1/alerts/{id}' = 660
        'PATCH /v1/alerts/{id}/resolve' = 670

        'POST /v1/events' = 710
        'GET /v1/events' = 720
        'GET /v1/events/{id}' = 730
        'PATCH /v1/events/{id}/approve' = 740
        'PATCH /v1/events/{id}/reject' = 750
        'POST /v1/events/{id}/register' = 760
        'DELETE /v1/events/{id}/register' = 770

        'POST /v1/device-tokens' = 810
        'GET /v1/notifications' = 820
        'POST /v1/notifications/send' = 830
        'PATCH /v1/notifications/{id}/read' = 840
        'PATCH /v1/notifications/read-all' = 850
        'DELETE /v1/device-tokens/{id}' = 860
    }
    if ($map.ContainsKey($key)) {
        return $map[$key]
    }
    return 10000
}

function Parse-EndpointMetadata($typeMap, $enumMap) {
    $controllers = Get-ChildItem -Path $sourceRoot -Recurse -Filter *Controller.java
    $endpoints = @()

    foreach ($file in $controllers) {
        $raw = Get-Content -Raw -Path $file.FullName
        $relPath = $file.FullName.Replace($repoRoot.Path + '\\', '').Replace('\\', '/')

        $classMatch = [regex]::Match($raw, '@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]+)"[^\)]*\)\s*public\s+class\s+([A-Za-z0-9_]+)', [System.Text.RegularExpressions.RegexOptions]::Singleline)
        if (-not $classMatch.Success) { continue }

        $base = $classMatch.Groups[1].Value
        $className = $classMatch.Groups[2].Value

        $pattern = '@(?<verb>Get|Post|Put|Patch|Delete)Mapping(?:\(\s*"(?<sub>[^"]*)"\s*\))?[\s\r\n]*public\s+(?<ret>[^\(\r\n]+?)\s+(?<method>[A-Za-z0-9_]+)\s*\((?<params>[\s\S]*?)\)\s*\{'
        $matches = [regex]::Matches($raw, $pattern, [System.Text.RegularExpressions.RegexOptions]::Singleline)

        foreach ($m in $matches) {
            $verb = $m.Groups['verb'].Value.ToUpper()
            $sub = $m.Groups['sub'].Value
            $methodName = $m.Groups['method'].Value
            $ret = Normalize-Type $m.Groups['ret'].Value
            $params = $m.Groups['params'].Value -replace '\s+', ' '

            $path = $base
            if (-not [string]::IsNullOrWhiteSpace($sub)) {
                if ($sub.StartsWith('/')) { $path = $base + $sub } else { $path = $base + '/' + $sub }
            }

            $line = ($raw.Substring(0, $m.Index).Split("`n").Count)

            $pathParams = @()
            $ppMatches = [regex]::Matches($params, '@PathVariable(?:\((?<meta>[^\)]*)\))?\s+(?<type>(?:[A-Za-z0-9_\.]+(?:<[^>]+>)?))\s+(?<name>[A-Za-z0-9_]+)')
            foreach ($ppm in $ppMatches) {
                $pname = $ppm.Groups['name'].Value
                $meta = $ppm.Groups['meta'].Value
                if ($meta -match '"([^"]+)"') { $pname = $Matches[1] }
                $pathParams += [pscustomobject]@{ name = $pname; type = $ppm.Groups['type'].Value }
            }

            $queryParams = @()
            $qpMatches = [regex]::Matches($params, '@RequestParam(?:\((?<meta>[^\)]*)\))?\s+(?<type>(?:[A-Za-z0-9_\.]+(?:<[^>]+>)?))\s+(?<name>[A-Za-z0-9_]+)')
            foreach ($qpm in $qpMatches) {
                $qname = $qpm.Groups['name'].Value
                $qmeta = $qpm.Groups['meta'].Value
                $required = $true
                $defaultValue = $null
                if ($qmeta -match 'required\s*=\s*false') { $required = $false }
                if ($qmeta -match 'defaultValue\s*=\s*"([^"]*)"') { $defaultValue = $Matches[1]; $required = $false }
                if ($qmeta -match '(?:^|,)\s*(?:name|value)\s*=\s*"([^"]+)"') {
                    $qname = $Matches[1]
                } elseif (($qmeta.Trim()) -match '^"([^"]+)"$') {
                    $qname = $Matches[1]
                }

                $queryParams += [pscustomobject]@{
                    name = $qname
                    type = $qpm.Groups['type'].Value
                    required = $required
                    defaultValue = $defaultValue
                }
            }

            $bodyType = $null
            $bodyOptional = $false
            $bodyParam = [regex]::Match($params, '@RequestBody(?<meta>\([^\)]*\))?\s+(?<type>(?:[A-Za-z0-9_\.]+(?:<[^>]+>)?))\s+(?<name>[A-Za-z0-9_]+)')
            if ($bodyParam.Success) {
                $bodyType = $bodyParam.Groups['type'].Value
                if ($bodyParam.Groups['meta'].Value -match 'required\s*=\s*false') { $bodyOptional = $true }
            }

            $responseType = $ret
            if ($ret -match 'ResponseEntity<(.+)>') {
                $responseType = $Matches[1].Trim()
            }

            $methodStart = $m.Index + $m.Length - 1
            $depth = 0
            $methodEnd = -1
            for ($i = $methodStart; $i -lt $raw.Length; $i++) {
                $ch = $raw[$i]
                if ($ch -eq '{') { $depth++ }
                elseif ($ch -eq '}') {
                    $depth--
                    if ($depth -eq 0) { $methodEnd = $i; break }
                }
            }
            $statusHint = '200/201 depending on handler logic'
            if ($methodEnd -gt $methodStart) {
                $methodBody = $raw.Substring($methodStart, $methodEnd - $methodStart + 1)
                if ($methodBody -match 'HttpStatus\.CREATED|status\(HttpStatus\.CREATED\)') { $statusHint = '201 Created' }
                elseif ($methodBody -match 'noContent\(') { $statusHint = '204 No Content' }
                elseif ($methodBody -match 'accepted\(') { $statusHint = '202 Accepted' }
                elseif ($methodBody -match 'ok\(') { $statusHint = '200 OK' }
            }

            $bodyFields = @()
            if ($bodyType) {
                $cleanType = ($bodyType -replace '<.*', '').Trim()
                if ($typeMap.ContainsKey($cleanType)) {
                    $bodyFields = $typeMap[$cleanType].fields
                }
            }

            $minimalBody = $null
            $fullBody = $null
            if ($bodyType) {
                if ($path -eq '/v1/auth/send-otp') {
                    $minimalBody = [ordered]@{ phoneNumber = '+201000000000' }
                    $fullBody = [ordered]@{ phoneNumber = '+201000000000' }
                } elseif ($bodyFields.Count -gt 0) {
                    $minimalBody = Build-MinimalBody $bodyFields $enumMap
                    $fullBody = Build-FullBody $bodyFields $enumMap
                } else {
                    $minimalBody = [ordered]@{ sample = 'value' }
                    $fullBody = [ordered]@{ sample = 'value' }
                }
            }

            $endpoints += [pscustomobject]@{
                file = $relPath
                class = $className
                handler = $methodName
                line = $line
                method = $verb
                path = $path
                module = Get-ModuleKey $path
                auth = Get-AuthScope $path
                pathParams = $pathParams
                queryParams = $queryParams
                requestBodyType = $bodyType
                requestBodyOptional = $bodyOptional
                responseType = $responseType
                statusHint = $statusHint
                minimalBody = $minimalBody
                fullBody = $fullBody
            }
        }
    }

    return $endpoints | Sort-Object module, path, method
}

function Write-ModuleDocs($endpoints) {
    $groups = $endpoints | Group-Object module | Sort-Object Name

    foreach ($group in $groups) {
        $module = $group.Name
        $title = $moduleTitles[$module]
        if (-not $title) { $title = $module }
        $filePath = Join-Path $modulesRoot ($module + '.md')
        $lines = @()
        $lines += '# ' + $title
        $lines += ''
        $lines += 'Module key: ' + $module
        $lines += ''
        $lines += 'Endpoint count: ' + $group.Count
        $lines += ''
        $lines += '## Endpoints Summary'
        $lines += ''
        $lines += '| Method | Path | Auth | Handler |'
        $lines += '|---|---|---|---|'
        foreach ($ep in ($group.Group | Sort-Object @{ Expression = { Get-SequenceRank $_ } }, path, method)) {
            $lines += '| ' + $ep.method + ' | ' + $ep.path + ' | ' + $ep.auth + ' | ' + $ep.class + '.' + $ep.handler + ' |'
        }
        $lines += ''

        $lines += '## Endpoint Usage'
        $lines += ''

        foreach ($ep in ($group.Group | Sort-Object @{ Expression = { Get-SequenceRank $_ } }, path, method)) {
            $lines += '### ' + $ep.method + ' ' + $ep.path
            $lines += ''
            $lines += '- Source: `' + $ep.file + ':' + $ep.line + '`'
            $lines += '- Handler: `' + $ep.class + '.' + $ep.handler + '()`'
            $lines += '- Auth: ' + $ep.auth
            $lines += '- Success: ' + $ep.statusHint
            $lines += '- Response Type: `' + $ep.responseType + '`'

            if ($ep.pathParams.Count -gt 0) {
                $lines += '- Path Params:'
                foreach ($p in $ep.pathParams) {
                    $lines += '  - `' + $p.name + '` (' + $p.type + ')'
                }
            } else {
                $lines += '- Path Params: none'
            }

            if ($ep.queryParams.Count -gt 0) {
                $lines += '- Query Params:'
                foreach ($q in $ep.queryParams) {
                    $entry = '  - `' + $q.name + '` (' + $q.type + ')'
                    if (-not $q.required) { $entry += ' optional' }
                    if ($q.defaultValue) { $entry += ', default `' + $q.defaultValue + '`' }
                    $lines += $entry
                }
            } else {
                $lines += '- Query Params: none'
            }

            if ($ep.requestBodyType) {
                $lines += '- Request Body Type: `' + $ep.requestBodyType + '`'
                if ($ep.requestBodyOptional) {
                    $lines += '- Body Requirement: optional (`@RequestBody(required = false)`)'
                } else {
                    $lines += '- Body Requirement: required'
                }

                $lines += ''
                $lines += 'Minimal body example:'
                $lines += ''
                $lines += '```json'
                $lines += ($ep.minimalBody | ConvertTo-Json -Depth 10)
                $lines += '```'
                $lines += ''
                $lines += 'Full body example:'
                $lines += ''
                $lines += '```json'
                $lines += ($ep.fullBody | ConvertTo-Json -Depth 10)
                $lines += '```'
            } else {
                $lines += '- Request Body: none'
            }

            $lines += ''
            $lines += 'How to use:'
            $lines += '- Send request to `{{host}}' + (Convert-PathToHttp $ep.path) + '`'
            $lines += '- Include `Authorization: Bearer <token>` unless endpoint is public'
            $lines += '- For path/query params, replace placeholders with real IDs or filter values'
            if ($ep.requestBodyType) {
                $lines += '- Start with minimal body, then extend to full body for advanced use cases'
            }
            $lines += ''
        }

        Set-Content -Path $filePath -Value $lines
    }
}

function Write-MasterReadme($endpoints) {
    $readmePath = Join-Path $apiRoot 'README.md'
    $lines = @()
    $lines += '# Sakany API Documentation and Collections'
    $lines += ''
    $lines += 'Generated from source controllers under `src/main/java/**/controllers/*Controller.java`.'
    $lines += ''
    $lines += 'Total endpoints discovered: ' + $endpoints.Count
    $lines += ''
    $lines += '## Files'
    $lines += ''
    $lines += '- `endpoint-inventory.csv`: canonical endpoint checklist'
    $lines += '- `endpoint-metadata.json`: enriched metadata used for docs/collections generation'
    $lines += '- `modules/*.md`: one module document per API area'
    $lines += '- `collections/sakany-sequential.http`: sequential REST Client file'
    $lines += '- `collections/sakany-sequential.postman_collection.json`: Postman import file'
    $lines += ''
    $lines += '## Modules'
    $lines += ''
    $lines += '| Module | Title | Endpoints | File |'
    $lines += '|---|---|---:|---|'

    $groups = $endpoints | Group-Object module | Sort-Object Name
    foreach ($g in $groups) {
        $m = $g.Name
        $title = $moduleTitles[$m]
        if (-not $title) { $title = $m }
        $lines += '| `' + $m + '` | ' + $title + ' | ' + $g.Count + ' | `modules/' + $m + '.md` |'
    }

    $lines += ''
    $lines += '## Authentication Rules'
    $lines += ''
    $lines += '- Public: `/v1/auth/register`, `/v1/auth/send-otp`, `/v1/auth/login/phone`, `/v1/auth/login/email`, `/v1/auth/refresh`'
    $lines += '- Admin token required: all `/v1/admin/**` routes'
    $lines += '- Authenticated user token required: all remaining `/v1/**` routes'
    $lines += ''
    $lines += '## Execution Notes'
    $lines += ''
    $lines += '- Start with auth endpoints to obtain `auth_token` and `user_id`.'
    $lines += '- Use resident flow modules first, then admin modules.'
    $lines += '- For admin endpoints, set `admin_token` to a valid admin JWT.'

    Set-Content -Path $readmePath -Value $lines
}

function Write-HttpCollection($endpoints) {
    $httpPath = Join-Path $collectionsRoot 'sakany-sequential.http'
    $lines = @()
    $lines += '### Sakany Sequential API Collection (Generated)'
    $lines += '# Generated from controller source. Update by rerunning generate-api-artifacts.ps1'
    $lines += ''
    $lines += '@host = http://localhost:8080'
    $lines += '@auth_token = '
    $lines += '@admin_token = '
    $lines += '@refresh_token = '
    $lines += '@user_id = '
    $lines += '@compound_id = '
    $lines += '@building_id = '
    $lines += '@unit_id = '
    $lines += '@access_code_id = '
    $lines += '@access_code = '
    $lines += '@visit_log_id = '
    $lines += '@invoice_id = '
    $lines += '@maintenance_id = '
    $lines += '@feedback_id = '
    $lines += '@announcement_id = '
    $lines += '@alert_id = '
    $lines += '@event_id = '
    $lines += '@notification_id = '
    $lines += '@admin_report_id = '
    $lines += '@resident_id = '
    $lines += '@employee_id = '
    $lines += ''

    $orderedModules = @(
        '01-auth-users',
        '02-properties',
        '03-access-qr',
        '04-billing',
        '05-maintenance',
        '06-feedback',
        '07-announcements-alerts',
        '08-events',
        '09-notifications-communications',
        '10-admin-missing-found',
        '11-admin-residents',
        '12-admin-employees',
        '99-other'
    )

    foreach ($module in $orderedModules) {
        $moduleEndpoints = $endpoints | Where-Object { $_.module -eq $module } | Sort-Object @{ Expression = { Get-SequenceRank $_ } }, path, method
        if ($moduleEndpoints.Count -eq 0) { continue }

        $title = $moduleTitles[$module]
        if (-not $title) { $title = $module }
        $lines += '# =========================================='
        $lines += '# ' + $title
        $lines += '# =========================================='
        $lines += ''

        foreach ($ep in $moduleEndpoints) {
            $safeName = ($ep.method + ' ' + $ep.path) -replace '[^A-Za-z0-9]+', '_'
            $lines += '### ' + $ep.method + ' ' + $ep.path
            $lines += '# @name ' + $safeName

            $path = Convert-PathToHttp $ep.path
            $qs = @()
            foreach ($q in $ep.queryParams) {
                if ($q.defaultValue) { $qs += ($q.name + '=' + $q.defaultValue) }
                elseif ($q.required) { $qs += ($q.name + '=') }
            }
            $fullUrl = '{{host}}' + $path
            if ($qs.Count -gt 0) {
                $fullUrl += '?' + ($qs -join '&')
            }

            $lines += $ep.method + ' ' + $fullUrl
            if ($ep.auth -eq 'Bearer token (authenticated user)') {
                $lines += 'Authorization: Bearer {{auth_token}}'
            } elseif ($ep.auth -eq 'Admin Bearer token (ROLE_ADMIN)') {
                $lines += 'Authorization: Bearer {{admin_token}}'
            }
            if ($ep.requestBodyType) {
                $lines += 'Content-Type: application/json'
                $lines += ''
                $lines += ($ep.minimalBody | ConvertTo-Json -Depth 10)
            }
            $lines += ''

            if ($ep.path -eq '/v1/auth/register') {
                $lines += '> {%'
                $lines += '    if (response.body && response.body.accessToken) client.global.set("auth_token", response.body.accessToken);'
                $lines += '    if (response.body && response.body.refreshToken) client.global.set("refresh_token", response.body.refreshToken);'
                $lines += '    if (response.body && response.body.userId) client.global.set("user_id", response.body.userId);'
                $lines += '%}'
                $lines += ''
            }
            if ($ep.path -eq '/v1/auth/login/email' -or $ep.path -eq '/v1/auth/login/phone') {
                $lines += '> {%'
                $lines += '    if (response.body && response.body.accessToken) client.global.set("auth_token", response.body.accessToken);'
                $lines += '    if (response.body && response.body.refreshToken) client.global.set("refresh_token", response.body.refreshToken);'
                $lines += '%}'
                $lines += ''
            }
            if ($ep.path -eq '/v1/compounds') {
                $lines += '> {% if (response.body) client.global.set("compound_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.path -eq '/v1/buildings') {
                $lines += '> {% if (response.body) client.global.set("building_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.path -eq '/v1/units') {
                $lines += '> {% if (response.body) client.global.set("unit_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.path -eq '/v1/access/codes') {
                $lines += '> {% '
                $lines += '    if (response.body && response.body.id) client.global.set("access_code_id", response.body.id);'
                $lines += '    if (response.body && response.body.code) client.global.set("access_code", response.body.code);'
                $lines += '%}'
                $lines += ''
            }
            if ($ep.path -eq '/v1/access/codes/{code}/scan') {
                $lines += '> {% if (response.body && response.body.visitLogId) client.global.set("visit_log_id", response.body.visitLogId); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/invoices') {
                $lines += '> {% if (response.body) client.global.set("invoice_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/maintenance-requests') {
                $lines += '> {% if (response.body) client.global.set("maintenance_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/feedback') {
                $lines += '> {% if (response.body) client.global.set("feedback_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/announcements') {
                $lines += '> {% if (response.body) client.global.set("announcement_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/alerts') {
                $lines += '> {% if (response.body) client.global.set("alert_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/events') {
                $lines += '> {% '
                $lines += '    const location = response.headers["location"] || response.headers["Location"];'
                $lines += '    if (location) client.global.set("event_id", location.split("/").pop());'
                $lines += '%}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/notifications/send') {
                $lines += '> {% if (response.body) client.global.set("notification_id", response.body.id || response.body); %}'
                $lines += ''
            }
            if ($ep.method -eq 'POST' -and $ep.path -eq '/v1/admin/missing-found/reports') {
                $lines += '> {% if (response.body) client.global.set("admin_report_id", response.body.id || response.body); %}'
                $lines += ''
            }
        }
    }

    Set-Content -Path $httpPath -Value $lines
}

function Write-PostmanCollection($endpoints) {
    $postmanPath = Join-Path $collectionsRoot 'sakany-sequential.postman_collection.json'

    $orderedModules = @(
        '01-auth-users',
        '02-properties',
        '03-access-qr',
        '04-billing',
        '05-maintenance',
        '06-feedback',
        '07-announcements-alerts',
        '08-events',
        '09-notifications-communications',
        '10-admin-missing-found',
        '11-admin-residents',
        '12-admin-employees',
        '99-other'
    )

    $collection = [ordered]@{
        info = [ordered]@{
            name = 'Sakany Sequential API Collection (Generated)'
            schema = 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'
        }
        variable = @(
            @{ key = 'host'; value = 'http://localhost:8080' },
            @{ key = 'auth_token'; value = '' },
            @{ key = 'admin_token'; value = '' },
            @{ key = 'refresh_token'; value = '' },
            @{ key = 'user_id'; value = '' },
            @{ key = 'compound_id'; value = '' },
            @{ key = 'building_id'; value = '' },
            @{ key = 'unit_id'; value = '' },
            @{ key = 'access_code_id'; value = '' },
            @{ key = 'access_code'; value = '' },
            @{ key = 'visit_log_id'; value = '' },
            @{ key = 'invoice_id'; value = '' },
            @{ key = 'maintenance_id'; value = '' },
            @{ key = 'feedback_id'; value = '' },
            @{ key = 'announcement_id'; value = '' },
            @{ key = 'alert_id'; value = '' },
            @{ key = 'event_id'; value = '' },
            @{ key = 'notification_id'; value = '' },
            @{ key = 'admin_report_id'; value = '' }
        )
        item = @()
    }

    foreach ($module in $orderedModules) {
        $moduleEndpoints = $endpoints | Where-Object { $_.module -eq $module } | Sort-Object @{ Expression = { Get-SequenceRank $_ } }, path, method
        if ($moduleEndpoints.Count -eq 0) { continue }

        $folder = [ordered]@{
            name = $moduleTitles[$module]
            item = @()
        }

        foreach ($ep in $moduleEndpoints) {
            $urlPath = Convert-PathToHttp $ep.path
            $rawUrl = '{{host}}' + $urlPath
            $qsArray = @()
            foreach ($q in $ep.queryParams) {
                $qv = ''
                if ($q.defaultValue) { $qv = $q.defaultValue }
                $qsArray += @{ key = $q.name; value = $qv }
            }
            if ($qsArray.Count -gt 0) {
                $rawUrl += '?' + (($qsArray | ForEach-Object { $_.key + '=' + $_.value }) -join '&')
            }

            $headers = @()
            if ($ep.auth -eq 'Bearer token (authenticated user)') {
                $headers += @{ key = 'Authorization'; value = 'Bearer {{auth_token}}' }
            } elseif ($ep.auth -eq 'Admin Bearer token (ROLE_ADMIN)') {
                $headers += @{ key = 'Authorization'; value = 'Bearer {{admin_token}}' }
            }

            $request = [ordered]@{
                method = $ep.method
                header = $headers
                url = [ordered]@{
                    raw = $rawUrl
                }
            }

            if ($ep.requestBodyType) {
                $headers += @{ key = 'Content-Type'; value = 'application/json' }
                $request.body = [ordered]@{
                    mode = 'raw'
                    raw = ($ep.minimalBody | ConvertTo-Json -Depth 10)
                    options = @{ raw = @{ language = 'json' } }
                }
            }

            $folder.item += [ordered]@{
                name = $ep.method + ' ' + $ep.path
                request = $request
            }
        }

        $collection.item += $folder
    }

    $json = $collection | ConvertTo-Json -Depth 25
    Set-Content -Path $postmanPath -Value $json
}

$typeData = Build-TypeMaps
$endpoints = Parse-EndpointMetadata $typeData.typeMap $typeData.enumMap

$inventoryCsv = Join-Path $apiRoot 'endpoint-inventory.csv'
$inventoryMd = Join-Path $apiRoot 'endpoint-inventory.md'
$metadataJson = Join-Path $apiRoot 'endpoint-metadata.json'

$endpoints | Select-Object module, method, path, auth, class, handler, file, line, requestBodyType, responseType, statusHint | Export-Csv -NoTypeInformation -Path $inventoryCsv

$lines = @()
$lines += '# Endpoint Inventory (Generated)'
$lines += ''
$lines += 'Total endpoints: ' + $endpoints.Count
$lines += ''
$lines += '| Module | Method | Path | Auth | Handler | Source |'
$lines += '|---|---|---|---|---|---|'
foreach ($ep in $endpoints) {
    $source = $ep.file + ':' + $ep.line
    $lines += '| ' + $ep.module + ' | ' + $ep.method + ' | ' + $ep.path + ' | ' + $ep.auth + ' | ' + $ep.class + '.' + $ep.handler + ' | ' + $source + ' |'
}
Set-Content -Path $inventoryMd -Value $lines

$endpoints | ConvertTo-Json -Depth 20 | Set-Content -Path $metadataJson

Write-ModuleDocs $endpoints
Write-MasterReadme $endpoints
Write-HttpCollection $endpoints
Write-PostmanCollection $endpoints

Write-Output ('Generated API artifacts for ' + $endpoints.Count + ' endpoints in project-docs/api')