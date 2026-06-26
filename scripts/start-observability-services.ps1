param(
    [switch]$Restart
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$logDir = Join-Path $repoRoot "logs"
$agentPath = Join-Path $repoRoot "infra\observability\agent\opentelemetry-javaagent.jar"
$agentConfigPath = Join-Path $repoRoot "infra\observability\opentelemetry-javaagent.properties"
$envFile = Join-Path $repoRoot ".env"

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#") -or -not $line.Contains("=")) {
            return
        }

        $parts = $line -split "=", 2
        Set-Item -Path "Env:$($parts[0].Trim())" -Value $parts[1].Trim()
    }
}

if (-not (Test-Path $agentPath)) {
    Write-Host "OpenTelemetry Java agent not found. Downloading..."
    mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy `
        "-Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.12.0" `
        "-DoutputDirectory=infra/observability/agent" `
        "-Dmdep.stripVersion=true"
}

mvn -q -pl libs/event-contract install

$services = @(
    @{ Name = "api-gateway"; Module = "services/api-gateway"; Port = 8088 },
    @{ Name = "ticket-service"; Module = "services/ticket-service"; Port = 8081 },
    @{ Name = "file-service"; Module = "services/file-service"; Port = 8082 },
    @{ Name = "workflow-sla-service"; Module = "services/workflow-sla-service"; Port = 8083 },
    @{ Name = "notification-service"; Module = "services/notification-service"; Port = 8084 },
    @{ Name = "reporting-service"; Module = "services/reporting-service"; Port = 8085 }
)

function Stop-PortListener {
    param([int]$Port)

    $listeners = netstat -ano | Select-String ":$Port\s+.*LISTENING"
    $processIds = @()
    foreach ($listener in $listeners) {
        if ($listener.Line -match "\s+(\d+)$") {
            $processIds += [int]$Matches[1]
        }
    }

    foreach ($processId in ($processIds | Sort-Object -Unique)) {
        Write-Host "Stopping process $processId on port $Port"
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }
}

function Start-OtelService {
    param(
        [string]$ServiceName,
        [string]$Module
    )

    $stdoutPath = Join-Path $logDir "$ServiceName.otel.out"
    $stderrPath = Join-Path $logDir "$ServiceName.otel.err"
    $javaToolOptions = "-javaagent:$agentPath -Dotel.javaagent.configuration-file=$agentConfigPath -Dapp.log.dir=$logDir"
    $command = @"
`$env:SPRING_PROFILES_ACTIVE = "local"
`$env:JAVA_TOOL_OPTIONS = "$javaToolOptions"
`$env:OTEL_SERVICE_NAME = "$ServiceName"
mvn -pl $Module spring-boot:run "-Dspring-boot.run.profiles=local"
"@
    $encodedCommand = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))

    $process = Start-Process `
        -FilePath "powershell.exe" `
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", $encodedCommand) `
        -WorkingDirectory $repoRoot `
        -WindowStyle Hidden `
        -RedirectStandardOutput $stdoutPath `
        -RedirectStandardError $stderrPath `
        -PassThru

    Write-Host "$ServiceName started with PID $($process.Id). Logs: $stdoutPath"
}

if ($Restart) {
    foreach ($service in $services) {
        Stop-PortListener -Port $service.Port
    }
    Start-Sleep -Seconds 3
}

foreach ($service in $services) {
    Start-OtelService -ServiceName $service.Name -Module $service.Module
}

Write-Host "Waiting for services to start..."
Start-Sleep -Seconds 12

foreach ($service in $services) {
    $healthUrl = "http://localhost:$($service.Port)/actuator/health"
    try {
        $response = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
        Write-Host "$($service.Name) health: $($response.status)"
    } catch {
        Write-Host "$($service.Name) health: not ready yet ($healthUrl)"
    }
}

Write-Host "Generate traffic, then check Jaeger at http://localhost:16686. All six service names should appear after traces are emitted."
