# Build the Vue frontend and copy dist into Spring Boot static resources.
# Usage: pwsh -File scripts/build-all.ps1
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$webDir = Join-Path $root "campusevent-web"
$staticDir = Join-Path $root "campusevent-server\src\main\resources\static"

Push-Location $webDir
try {
    Write-Host "==> npm run build ($webDir)"
    npm run build
} finally {
    Pop-Location
}

if (-not (Test-Path (Join-Path $webDir "dist\index.html"))) {
    throw "Frontend build failed: missing $webDir\dist\index.html"
}

if (Test-Path $staticDir) {
    Get-ChildItem -LiteralPath $staticDir -Force | Remove-Item -Recurse -Force
} else {
    New-Item -ItemType Directory -Path $staticDir | Out-Null
}

Write-Host "==> copy dist -> $staticDir"
Copy-Item -Path (Join-Path $webDir "dist\*") -Destination $staticDir -Recurse -Force
Write-Host "Done: Spring Boot static resources updated."
