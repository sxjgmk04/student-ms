param(
  [string]$MysqlPassword = $env:MYSQL_PASSWORD
)

$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

Write-Host "== student-ms local dev starter ==" -ForegroundColor Cyan

if (-not $MysqlPassword) { $MysqlPassword = "root" }

# Backend (new PowerShell window)
$backendCmd = "cd `"$PSScriptRoot\server-spring`"; .\run-dev.ps1 -MysqlPassword `"$MysqlPassword`""
Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendCmd | Out-Null
Write-Host "Backend starting in a new window: http://localhost:3000" -ForegroundColor Green

Start-Sleep -Seconds 2

# Frontend (new PowerShell window)
$frontendCmd = "cd `"$PSScriptRoot`"; npm install; npm run dev"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $frontendCmd | Out-Null
Write-Host "Frontend starting in a new window: http://localhost:5173" -ForegroundColor Green

Write-Host ""
Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "- Ensure MySQL is running and database 'student_ms' exists."
Write-Host "- Default MySQL user is root; password default is 'root' (change via -MysqlPassword)."
Write-Host "- AI: copy server-spring\local-ai.env.example to local-ai.env, set AI_API_KEY; run-dev.ps1 loads it."

