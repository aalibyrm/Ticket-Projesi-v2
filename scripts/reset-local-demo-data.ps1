[CmdletBinding()]
param(
    [switch] $ConfirmReset,
    [string] $EnvFile = ".env",
    [string] $PostgresContainer = "ticket-postgres",
    [string] $SqlFile = "scripts/demo-data/local-demo-reset.sql"
)

$ErrorActionPreference = "Stop"

function Get-EnvOrDefault {
    param(
        [Parameter(Mandatory = $true)][string] $Name,
        [Parameter(Mandatory = $true)][string] $DefaultValue
    )

    $value = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $DefaultValue
    }
    return $value
}

function Import-DotEnvFile {
    param([Parameter(Mandatory = $true)][string] $Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    foreach ($line in Get-Content -LiteralPath $Path) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0 -or $trimmed.StartsWith("#")) {
            continue
        }
        $separatorIndex = $trimmed.IndexOf("=")
        if ($separatorIndex -lt 1) {
            continue
        }
        $name = $trimmed.Substring(0, $separatorIndex).Trim()
        $value = $trimmed.Substring($separatorIndex + 1).Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

function New-DemoUserProfiles {
    return @(
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000001"; Username = "customer.user"; FirstName = "Ayse"; LastName = "Yilmaz"; Email = "ayse.yilmaz@example.local"; Role = "CUSTOMER" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000004"; Username = "customer.mehmet"; FirstName = "Mehmet"; LastName = "Demir"; Email = "mehmet.demir@example.local"; Role = "CUSTOMER" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000005"; Username = "customer.zeynep"; FirstName = "Zeynep"; LastName = "Kaya"; Email = "zeynep.kaya@example.local"; Role = "CUSTOMER" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000006"; Username = "customer.emre"; FirstName = "Emre"; LastName = "Arslan"; Email = "emre.arslan@example.local"; Role = "CUSTOMER" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000007"; Username = "customer.ceren"; FirstName = "Ceren"; LastName = "Aksoy"; Email = "ceren.aksoy@example.local"; Role = "CUSTOMER" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000008"; Username = "customer.ali"; FirstName = "Ali"; LastName = "Bayram"; Email = "ali.bayram@example.local"; Role = "CUSTOMER" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000001"; Username = "lead.identity"; FirstName = "Irem"; LastName = "Gunes"; Email = "irem.gunes@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000002"; Username = "lead.permission"; FirstName = "Cem"; LastName = "Arslan"; Email = "cem.arslan@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000003"; Username = "lead.web"; FirstName = "Seda"; LastName = "Yildirim"; Email = "seda.yildirim@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000004"; Username = "lead.core"; FirstName = "Okan"; LastName = "Demir"; Email = "okan.demir@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000005"; Username = "lead.network"; FirstName = "Derya"; LastName = "Korkmaz"; Email = "derya.korkmaz@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000006"; Username = "lead.platform"; FirstName = "Alp"; LastName = "Kaya"; Email = "alp.kaya@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000007"; Username = "lead.billing"; FirstName = "Melis"; LastName = "Acar"; Email = "melis.acar@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000008"; Username = "lead.payment1"; FirstName = "Bora"; LastName = "Yalcin"; Email = "bora.yalcin@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "30000000-0000-0000-0000-000000000009"; Username = "lead.payment2"; FirstName = "Eren"; LastName = "Koc"; Email = "eren.koc@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000001"; Username = "agent.identity"; FirstName = "Elif"; LastName = "Aydin"; Email = "elif.aydin@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000002"; Username = "agent.permission"; FirstName = "Mert"; LastName = "Kaya"; Email = "mert.kaya@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000003"; Username = "agent.web"; FirstName = "Deniz"; LastName = "Arslan"; Email = "deniz.arslan@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000004"; Username = "agent.core"; FirstName = "Selin"; LastName = "Demir"; Email = "selin.demir@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000005"; Username = "agent.network"; FirstName = "Baran"; LastName = "Yilmaz"; Email = "baran.yilmaz@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000006"; Username = "agent.platform"; FirstName = "Ece"; LastName = "Sahin"; Email = "ece.sahin@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000007"; Username = "agent.billing"; FirstName = "Onur"; LastName = "Demir"; Email = "onur.demir@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000008"; Username = "agent.payment"; FirstName = "Zeynep"; LastName = "Ozturk"; Email = "zeynep.ozturk@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "40000000-0000-0000-0000-000000000009"; Username = "agent.payment2"; FirstName = "Seda"; LastName = "Erdem"; Email = "seda.erdem@example.local"; Role = "AGENT" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000002"; Username = "manager.user"; FirstName = "Deniz"; LastName = "Karaca"; Email = "deniz.karaca@example.local"; Role = "MANAGER" },
        [pscustomobject]@{ Id = "80000000-0000-0000-0000-000000000003"; Username = "admin.user"; FirstName = "Burak"; LastName = "Ozkan"; Email = "burak.ozkan@example.local"; Role = "ADMIN" }
    )
}

function Sync-KeycloakDemoUsers {
    param([Parameter(Mandatory = $true)][array] $Profiles)

    $port = Get-EnvOrDefault -Name "KEYCLOAK_HTTP_PORT" -DefaultValue "8080"
    $realm = Get-EnvOrDefault -Name "KEYCLOAK_REALM" -DefaultValue "ticket-management"
    $adminUser = Get-EnvOrDefault -Name "KEYCLOAK_ADMIN" -DefaultValue "admin"
    $adminPassword = Get-EnvOrDefault -Name "KEYCLOAK_ADMIN_PASSWORD" -DefaultValue "admin"
    $baseUrl = "http://localhost:$port"

    try {
        Invoke-RestMethod -Method Get -Uri "$baseUrl/realms/master" -TimeoutSec 3 | Out-Null
    }
    catch {
        Write-Warning "Keycloak is not reachable on $($baseUrl). Realm export was updated; running users were not synced."
        return
    }

    try {
        $token = Invoke-RestMethod `
            -Method Post `
            -Uri "$baseUrl/realms/master/protocol/openid-connect/token" `
            -ContentType "application/x-www-form-urlencoded" `
            -Body @{
                grant_type = "password"
                client_id = "admin-cli"
                username = $adminUser
                password = $adminPassword
            }

        $headers = @{ Authorization = "Bearer $($token.access_token)" }
        $roleCache = @{}

        foreach ($profile in $Profiles) {
            $encodedUsername = [Uri]::EscapeDataString($profile.Username)
            $existingUsers = Invoke-RestMethod `
                -Method Get `
                -Uri "$baseUrl/admin/realms/$realm/users?username=$encodedUsername&exact=true" `
                -Headers $headers

            $userBody = @{
                id = $profile.Id
                username = $profile.Username
                enabled = $true
                emailVerified = $true
                firstName = $profile.FirstName
                lastName = $profile.LastName
                email = $profile.Email
            }

            if ($existingUsers.Count -eq 0) {
                $createBody = $userBody.Clone()
                $createBody.credentials = @(
                    @{
                        type = "password"
                        value = "Password123!"
                        temporary = $false
                    }
                )
                Invoke-WebRequest `
                    -Method Post `
                    -Uri "$baseUrl/admin/realms/$realm/users" `
                    -Headers $headers `
                    -ContentType "application/json" `
                    -Body ($createBody | ConvertTo-Json -Depth 8) | Out-Null
                $existingUsers = Invoke-RestMethod `
                    -Method Get `
                    -Uri "$baseUrl/admin/realms/$realm/users?username=$encodedUsername&exact=true" `
                    -Headers $headers
            }

            $userId = $existingUsers[0].id
            if ($userId -ne $profile.Id) {
                Write-Warning "Keycloak user '$($profile.Username)' has id $userId, expected $($profile.Id). Recreate the local Keycloak container to avoid broken ticket ownership."
                continue
            }

            Invoke-WebRequest `
                -Method Put `
                -Uri "$baseUrl/admin/realms/$realm/users/$userId" `
                -Headers $headers `
                -ContentType "application/json" `
                -Body ($userBody | ConvertTo-Json -Depth 8) | Out-Null

            if (-not $roleCache.ContainsKey($profile.Role)) {
                $roleCache[$profile.Role] = Invoke-RestMethod `
                    -Method Get `
                    -Uri "$baseUrl/admin/realms/$realm/roles/$($profile.Role)" `
                    -Headers $headers
            }

            $currentRoles = Invoke-RestMethod `
                -Method Get `
                -Uri "$baseUrl/admin/realms/$realm/users/$userId/role-mappings/realm" `
                -Headers $headers

            if (-not ($currentRoles | Where-Object { $_.name -eq $profile.Role })) {
                Invoke-WebRequest `
                    -Method Post `
                    -Uri "$baseUrl/admin/realms/$realm/users/$userId/role-mappings/realm" `
                    -Headers $headers `
                    -ContentType "application/json" `
                    -Body (@($roleCache[$profile.Role]) | ConvertTo-Json -Depth 8) | Out-Null
            }
        }

        Write-Host "Keycloak demo users synced."
    }
    catch {
        Write-Warning "Keycloak user sync failed: $($_.Exception.Message)"
    }
}

function Clear-MailpitMessages {
    $port = Get-EnvOrDefault -Name "MAILPIT_WEB_PORT" -DefaultValue "8025"
    $uri = "http://localhost:$port/api/v1/messages"

    try {
        Invoke-WebRequest -Method Delete -Uri $uri -TimeoutSec 3 | Out-Null
        Write-Host "Mailpit messages cleared."
    }
    catch {
        Write-Warning "Mailpit is not reachable or did not accept delete-all at $uri."
    }
}

if (-not $ConfirmReset) {
    throw "Refusing to reset local demo data. Re-run with -ConfirmReset to delete existing local ticket data."
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$envPath = Join-Path $repoRoot $EnvFile
$sqlPath = Join-Path $repoRoot $SqlFile

if (-not (Test-Path -LiteralPath $sqlPath)) {
    throw "SQL seed file not found: $sqlPath"
}

Import-DotEnvFile -Path $envPath

$dbName = Get-EnvOrDefault -Name "POSTGRES_DB" -DefaultValue "ticket_platform"
$dbUser = Get-EnvOrDefault -Name "POSTGRES_ADMIN_USER" -DefaultValue "postgres"
$runningContainers = docker ps --filter "name=^/$($PostgresContainer)$" --filter "status=running" --format "{{.Names}}"

if ($LASTEXITCODE -ne 0) {
    throw "Docker is not available or Docker Desktop is not running."
}

if ($runningContainers -notcontains $PostgresContainer) {
    throw "PostgreSQL container '$PostgresContainer' is not running. Start the local Docker stack first."
}

Write-Host "Resetting local demo data in PostgreSQL container '$PostgresContainer'..."
Get-Content -Raw -LiteralPath $sqlPath | docker exec -i $PostgresContainer psql -U $dbUser -d $dbName -v ON_ERROR_STOP=1
if ($LASTEXITCODE -ne 0) {
    throw "PostgreSQL demo reset failed."
}

$profiles = New-DemoUserProfiles
Sync-KeycloakDemoUsers -Profiles $profiles
Clear-MailpitMessages

Write-Host "Local demo data reset complete."
