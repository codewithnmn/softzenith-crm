<#
.SYNOPSIS
  Local development environment: Postgres, Mailpit, backend API, staff UI and the Western World site.

.DESCRIPTION
  Run it through dev.cmd in the repository root:
    .\dev            stop the app services if they are running, then start everything (default)
    .\dev stop       stop the backend and both UIs
    .\dev stop -All  also stop Postgres and Mailpit
    .\dev status     show what is running

  The backend and the UIs each open in their own window (title "CRM - ..."), so their logs stay visible. Only
  processes started from this repository are stopped: if another program holds one of the ports, it is left alone.
#>
param(
    [ValidateSet('start', 'stop', 'status')] [string] $Action = 'start',
    [switch] $All
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$PgBin = Join-Path $env:LOCALAPPDATA 'Programs\pgsql\bin'
$PgData = Join-Path $env:LOCALAPPDATA 'crm-pgdata'
$Mailpit = Join-Path $env:LOCALAPPDATA 'Programs\mailpit\mailpit.exe'
$StateFile = Join-Path $Root '.dev-pids.json'

$Apps = @(
    @{ Name = 'Backend API'; Port = 8081; Dir = 'backend'; Wait = 240
       Cmd = '.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"'
       Check = 'http://localhost:8081/actuator/health'; Open = 'http://localhost:8081/swagger-ui.html' },
    @{ Name = 'Staff UI'; Port = 3000; Dir = 'frontend'; Wait = 120
       Cmd = 'npm run dev'
       Check = 'http://localhost:3000/login'; Open = 'http://localhost:3000' },
    @{ Name = 'Western World site'; Port = 3001; Dir = 'sites\westernworld'; Wait = 120
       Cmd = 'npm run dev -- -p 3001'
       Check = 'http://localhost:3001/'; Open = 'http://localhost:3001' }
)

function Get-ListenerPid([int] $Port) {
    $c = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($c) { return [int] $c.OwningProcess }
    return $null
}

function Test-FromThisRepo([int] $ProcessId) {
    $p = Get-CimInstance Win32_Process -Filter "ProcessId=$ProcessId" -ErrorAction SilentlyContinue
    if (-not $p -or -not $p.CommandLine) { return $false }
    $cmd = $p.CommandLine.Replace('/', '\').ToLowerInvariant()
    # spring-boot:run starts the API with its classpath in a temp file, so it is recognised by its main class.
    return $cmd.Contains($Root.ToLowerInvariant()) -or $cmd.Contains('com.softzenith.crm.crmapplication')
}

function Stop-Tree([int] $ProcessId) {
    & taskkill.exe /PID $ProcessId /T /F 2>&1 | Out-Null
}

function Test-Up([string] $Url) {
    try {
        $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
        return $r.StatusCode -lt 500
    } catch {
        return $false
    }
}

function Test-Postgres {
    & (Join-Path $PgBin 'pg_isready.exe') -h localhost -p 5432 2>&1 | Out-Null
    return $LASTEXITCODE -eq 0
}

function Stop-Apps {
    # The windows this script opened last time (closing them stops what runs inside).
    if (Test-Path $StateFile) {
        foreach ($id in (Get-Content $StateFile -Raw | ConvertFrom-Json)) {
            if (Get-Process -Id $id -ErrorAction SilentlyContinue) { Stop-Tree $id }
        }
        Remove-Item $StateFile -Force
    }
    # Anything of ours still holding a port (e.g. started by hand in a terminal).
    foreach ($app in $Apps) {
        $id = Get-ListenerPid $app.Port
        if (-not $id) { continue }
        if (Test-FromThisRepo $id) {
            Stop-Tree $id
        } else {
            $name = (Get-Process -Id $id -ErrorAction SilentlyContinue).ProcessName
            Write-Warning "Port $($app.Port) is used by '$name' (PID $id), which is not from this repository; left running."
        }
    }
    # Give the ports a moment to be released.
    for ($i = 0; $i -lt 30; $i++) {
        $busy = $Apps | Where-Object { $id = Get-ListenerPid $_.Port; $id -and (Test-FromThisRepo $id) }
        if (-not $busy) { break }
        Start-Sleep -Milliseconds 500
    }
    Write-Host 'App services stopped.' -ForegroundColor DarkGray
}

function Stop-Infra {
    if (Test-Postgres) {
        & (Join-Path $PgBin 'pg_ctl.exe') -D $PgData stop -m fast | Out-Null
        Write-Host 'Postgres stopped.' -ForegroundColor DarkGray
    }
    Get-Process mailpit -ErrorAction SilentlyContinue | Stop-Process -Force
    Write-Host 'Mailpit stopped.' -ForegroundColor DarkGray
}

function Start-Infra {
    if (Test-Postgres) {
        Write-Host 'Postgres already running.' -ForegroundColor DarkGray
    } else {
        Write-Host 'Starting Postgres...'
        & (Join-Path $PgBin 'pg_ctl.exe') -D $PgData -l (Join-Path $PgData 'server.log') -w start | Out-Null
        if (-not (Test-Postgres)) { throw "Postgres did not start; see $PgData\server.log" }
    }
    if (Get-ListenerPid 8025) {
        Write-Host 'Mailpit already running.' -ForegroundColor DarkGray
    } elseif (Test-Path $Mailpit) {
        Start-Process -FilePath $Mailpit -WindowStyle Hidden
    } else {
        Write-Warning "Mailpit not found at $Mailpit; emails will fail to send (the app keeps working)."
    }
}

function Start-Apps {
    $ids = @()
    foreach ($app in $Apps) {
        Write-Host "Starting $($app.Name)..."
        $dir = Join-Path $Root $app.Dir
        $command = "`$Host.UI.RawUI.WindowTitle = 'CRM - $($app.Name)'; Set-Location -LiteralPath '$dir'; $($app.Cmd)"
        # Encoded, because Start-Process drops the inner quotes (and PowerShell 5.1 would then split -Dspring-boot...=dev).
        $encoded = [Convert]::ToBase64String([Text.Encoding]::Unicode.GetBytes($command))
        $p = Start-Process powershell.exe -PassThru -ArgumentList @(
            '-NoProfile', '-ExecutionPolicy', 'Bypass', '-NoExit', '-EncodedCommand', $encoded)
        $ids += $p.Id
    }
    ConvertTo-Json -InputObject $ids | Set-Content -Path $StateFile -Encoding UTF8
}

function Wait-Apps {
    foreach ($app in $Apps) {
        Write-Host -NoNewline "Waiting for $($app.Name)"
        $deadline = (Get-Date).AddSeconds($app.Wait)
        $up = $false
        while ((Get-Date) -lt $deadline) {
            if (Test-Up $app.Check) { $up = $true; break }
            Write-Host -NoNewline '.'
            Start-Sleep -Seconds 2
        }
        if ($up) { Write-Host ' up' -ForegroundColor Green }
        else { Write-Host " not up after $($app.Wait) s: check the 'CRM - $($app.Name)' window" -ForegroundColor Yellow }
    }
}

function Show-Status {
    $rows = @(
        [pscustomobject]@{ Service = 'Postgres'; Status = $(if (Test-Postgres) { 'running' } else { 'stopped' }); Url = 'localhost:5432' },
        [pscustomobject]@{ Service = 'Mailpit (email inbox)'; Status = $(if (Get-ListenerPid 8025) { 'running' } else { 'stopped' }); Url = 'http://localhost:8025' }
    )
    foreach ($app in $Apps) {
        $state = if (Test-Up $app.Check) { 'running' } elseif (Get-ListenerPid $app.Port) { 'starting / not responding' } else { 'stopped' }
        $rows += [pscustomobject]@{ Service = $app.Name; Status = $state; Url = $app.Open }
    }
    $rows | Format-Table -AutoSize | Out-String | Write-Host
}

switch ($Action) {
    'stop' {
        Stop-Apps
        if ($All) { Stop-Infra }
    }
    'status' {
        Show-Status
    }
    'start' {
        Stop-Apps
        Start-Infra
        Start-Apps
        Wait-Apps
        Show-Status
        Write-Host 'Sign in at http://localhost:3000 with 9000000001 (Admin), 9000000002 (Branch Manager),'
        Write-Host '9000000003 (Counsellor) or 9000000004 (Receptionist). Stop everything with: .\dev stop'
    }
}
