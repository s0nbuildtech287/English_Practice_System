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

# 2. Open Academic Web Portal
Write-Host "Opening Academic Web Portal at http://localhost:8080..." -ForegroundColor Cyan
Start-Process "http://localhost:8080"
Write-Host "Done! Web portal is live at http://localhost:8080" -ForegroundColor Green
