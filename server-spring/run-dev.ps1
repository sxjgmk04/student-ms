# Run from this folder: .\run-dev.ps1
# If MySQL password is not "root": .\run-dev.ps1 -MysqlPassword 123456
param(
  [string]$MysqlPassword = $env:MYSQL_PASSWORD
)

Set-Location $PSScriptRoot

# 可选：本地 AI 配置（复制 local-ai.env.example → local-ai.env 并填写 Key）
# $PSScriptRoot 在少数调用方式下可能为空，Join-Path $null 会得到 $null，导致 Test-Path 报错
$aiRoot = $PSScriptRoot
if (-not $aiRoot) {
  $aiRoot = (Get-Location).Path
}
$aiEnvFile = Join-Path $aiRoot "local-ai.env"
if ($aiEnvFile -and (Test-Path -LiteralPath $aiEnvFile)) {
  Get-Content $aiEnvFile -Encoding UTF8 | ForEach-Object {
    $line = $_.Trim()
    if ($line -match '^\s*#' -or $line -eq '') { return }
    $idx = $line.IndexOf('=')
    if ($idx -gt 0) {
      $k = $line.Substring(0, $idx).Trim()
      $v = $line.Substring($idx + 1).Trim()
      if ($k) { Set-Item -Path "Env:$k" -Value $v }
    }
  }
  Write-Host "Loaded local-ai.env (AI keys)" -ForegroundColor DarkGray
}

$env:MYSQL_USER = "root"
if (-not $MysqlPassword) {
  $MysqlPassword = "root"
}
$env:MYSQL_PASSWORD = $MysqlPassword
if (-not $env:JWT_SECRET) {
  $env:JWT_SECRET = "please-use-a-long-random-secret-key-ok-for-thesis-demo"
}

Write-Host "Starting backend: http://localhost:3000 (MYSQL_USER=root)" -ForegroundColor Cyan
mvn spring-boot:run
