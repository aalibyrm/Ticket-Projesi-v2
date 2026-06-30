param(
    [switch]$Restart,
    [string]$SpringProfile = "",
    [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$envPath = if ([System.IO.Path]::IsPathRooted($EnvFile)) {
    $EnvFile
} else {
    Join-Path $repoRoot $EnvFile
}

function Import-EnvFile {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        throw "Environment file not found: $Path. Copy .env.example to .env and fill local values first."
    }

    $loadedKeys = New-Object System.Collections.Generic.List[string]

    Get-Content -LiteralPath $Path | ForEach-Object {
        $line = $_.Trim()
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith("#") -or -not $line.Contains("=")) {
            return
        }

        $parts = $line.Split("=", 2)
        $key = $parts[0].Trim()
        $value = $parts[1].Trim()

        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        if (-not [string]::IsNullOrWhiteSpace($key)) {
            Set-Item -Path "Env:$key" -Value $value
            $loadedKeys.Add($key) | Out-Null
        }
    }

    return $loadedKeys
}

function Assert-RequiredEnv {
    param([string[]]$Keys)

    foreach ($key in $Keys) {
        $value = [Environment]::GetEnvironmentVariable($key)
        if ([string]::IsNullOrWhiteSpace($value)) {
            throw "Required env key is missing after loading .env: $key"
        }
    }
}

function Stop-PortListener {
    param([int]$Port)

    $listeners = netstat -ano | Select-String ":$Port\s+.*LISTENING"
    $processIds = New-Object System.Collections.Generic.HashSet[string]
    foreach ($listener in $listeners) {
        $parts = ($listener.Line -split "\s+") | Where-Object { $_ }
        $processId = $parts[-1]
        if ($processId -match "^\d+$") {
            $processIds.Add($processId) | Out-Null
        }
    }

    foreach ($processId in $processIds) {
        try {
            Write-Host "Stopping process $processId on port $Port"
            Stop-Process -Id ([int]$processId) -Force -ErrorAction Stop
        } catch [Microsoft.PowerShell.Commands.ProcessCommandException] {
            Write-Host "Process $processId is already stopped"
        }
    }
}

Write-Host "Loading local environment from $envPath"
$loadedKeys = Import-EnvFile -Path $envPath
Assert-RequiredEnv -Keys @("TICKET_DB_URL", "TICKET_DB_USER", "TICKET_DB_PASSWORD")
Write-Host "Loaded $($loadedKeys.Count) environment keys. Secret values are not printed."

if ($Restart) {
    Stop-PortListener -Port 8081
}

$logsDir = Join-Path $repoRoot "logs"
New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

Push-Location $repoRoot
try {
    Write-Host "Updating local event contract snapshot"
    mvn -q -pl libs/event-contract install
} finally {
    Pop-Location
}

$profileCommand = ""
$profileArgument = ""
if (-not [string]::IsNullOrWhiteSpace($SpringProfile)) {
    $profileCommand = "`$env:SPRING_PROFILES_ACTIVE = `"$SpringProfile`""
    $profileArgument = " -Dspring-boot.run.profiles=$SpringProfile"
}

$command = @"
$profileCommand
mvn -pl services/ticket-service spring-boot:run$profileArgument
"@

$encodedCommand = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))
$stdoutPath = Join-Path $logsDir "ticket-service.local.out"
$stderrPath = Join-Path $logsDir "ticket-service.local.err"

Write-Host "Starting ticket-service on port 8081"
$process = Start-Process -FilePath "powershell.exe" `
    -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", $encodedCommand) `
    -WorkingDirectory $repoRoot `
    -WindowStyle Hidden `
    -RedirectStandardOutput $stdoutPath `
    -RedirectStandardError $stderrPath `
    -PassThru

Start-Sleep -Seconds 12

try {
    $health = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 5
    Write-Host "ticket-service health: $($health.status)"
    Write-Host "ticket-service PID: $($process.Id)"
    Write-Host "Logs: $stdoutPath, $stderrPath"
} catch {
    Write-Host "ticket-service did not become healthy. PID: $($process.Id)"
    Write-Host "Check logs: $stdoutPath, $stderrPath"
    throw
}
