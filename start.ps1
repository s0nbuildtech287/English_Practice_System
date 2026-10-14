Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  PRACTICE ENGLISH - CEFR ACADEMIC PLATFORM" -ForegroundColor Yellow
Write-Host "  Backend API: http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "  Web Portal:  http://localhost:8080" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan

# 1. Start Backend if not already running
$backendCheck = Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet
if (-not $backendCheck) {
    Write-Host "Starting Spring Boot Backend..." -ForegroundColor Yellow
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\backend'; .\gradlew.bat bootRun"
    Start-Sleep -Seconds 5
} else {
    Write-Host "Backend is already running on port 8080." -ForegroundColor Green
}

# 2. Start Web Client
Write-Host "Starting Flutter Web App on port 4000..." -ForegroundColor Yellow
Start-Process "http://localhost:4000"
python -m http.server 4000 --directory "$PSScriptRoot\app\build\web"
