param(
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5173
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$serverDir = Join-Path $repoRoot "campus-catering-server"
$webDir = Join-Path $repoRoot "campus-catering-web"

function Test-PortListening {
    param([int]$Port)
    return $null -ne (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1)
}

function Resolve-JavaHome {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
        return $env:JAVA_HOME
    }

    $candidateHomes = @()

    $ideaJdks = Join-Path $env:USERPROFILE ".jdks"
    if (Test-Path $ideaJdks) {
        $candidateHomes += Get-ChildItem $ideaJdks -Directory | Sort-Object LastWriteTime -Descending | ForEach-Object { $_.FullName }
    }

    if (Test-Path "C:\Program Files\Java") {
        $candidateHomes += Get-ChildItem "C:\Program Files\Java" -Directory | Sort-Object LastWriteTime -Descending | ForEach-Object { $_.FullName }
    }

    if (Test-Path "C:\Program Files\Eclipse Adoptium") {
        $candidateHomes += Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory | Sort-Object LastWriteTime -Descending | ForEach-Object { $_.FullName }
    }

    foreach ($home in $candidateHomes) {
        if (Test-Path (Join-Path $home "bin\java.exe")) {
            return $home
        }
    }

    return $null
}

function Start-Backend {
    if (Test-PortListening -Port $BackendPort) {
        Write-Host "[backend] already running on port $BackendPort"
        return
    }

    $javaHome = Resolve-JavaHome
    if (-not $javaHome) {
        Write-Warning "[backend] skipped: no valid JDK found. Please install JDK and set JAVA_HOME."
        return
    }

    $backendCommand = "`$env:JAVA_HOME='$javaHome'; `$env:Path='$javaHome\bin;' + `$env:Path; mvn spring-boot:run"
    Start-Process -FilePath "powershell.exe" -WorkingDirectory $serverDir -ArgumentList "-NoExit", "-Command", $backendCommand | Out-Null
    Write-Host "[backend] start command sent (port $BackendPort)"
}

function Start-Frontend {
    if (Test-PortListening -Port $FrontendPort) {
        Write-Host "[frontend] already running on port $FrontendPort"
        return
    }

    Start-Process -FilePath "powershell.exe" -WorkingDirectory $webDir -ArgumentList "-NoExit", "-Command", "npm run dev" | Out-Null
    Write-Host "[frontend] start command sent (port $FrontendPort)"
}

Start-Backend
Start-Frontend

Write-Host ""
Write-Host "Start script finished. Check new terminal windows for logs."
Write-Host "Frontend: http://localhost:$FrontendPort"
Write-Host "Backend:  http://localhost:$BackendPort"
