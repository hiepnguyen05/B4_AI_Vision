# PowerShell Build Script for AI Vision Service
# Usage: .\build.ps1 [command]
# Commands: build, start, stop, restart, logs, clean, test

param(
    [string]$Command = "help"
)

$SERVICE_NAME = "ai-vision-service"
$IMAGE_NAME = "ai-vision-service:latest"

function Show-Help {
    Write-Host ""
    Write-Host "AI Vision Service - Build & Deploy Script" -ForegroundColor Cyan
    Write-Host "=========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: .\build.ps1 [command]" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Green
    Write-Host "  build       Build Docker image" -ForegroundColor White
    Write-Host "  start       Start services with docker-compose" -ForegroundColor White
    Write-Host "  stop        Stop services" -ForegroundColor White
    Write-Host "  restart     Restart services" -ForegroundColor White
    Write-Host "  logs        View service logs" -ForegroundColor White
    Write-Host "  clean       Clean up containers and volumes" -ForegroundColor White
    Write-Host "  test        Run API tests" -ForegroundColor White
    Write-Host "  status      Check service status" -ForegroundColor White
    Write-Host "  db          Connect to database" -ForegroundColor White
    Write-Host "  help        Show this help message" -ForegroundColor White
    Write-Host ""
}

function Build-Image {
    Write-Host "Building Docker image..." -ForegroundColor Yellow
    docker-compose build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Build successful!" -ForegroundColor Green
    } else {
        Write-Host "✗ Build failed!" -ForegroundColor Red
        exit 1
    }
}

function Start-Services {
    Write-Host "Starting services..." -ForegroundColor Yellow
    docker-compose up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Services started!" -ForegroundColor Green
        Start-Sleep -Seconds 5
        Show-Status
        Write-Host ""
        Write-Host "Service URLs:" -ForegroundColor Cyan
        Write-Host "  API: http://localhost:8084/api" -ForegroundColor White
        Write-Host "  Swagger: http://localhost:8084/api/swagger-ui.html" -ForegroundColor White
        Write-Host "  Health: http://localhost:8084/api/health" -ForegroundColor White
        Write-Host "  RabbitMQ Console: http://localhost:15672 (guest/guest)" -ForegroundColor White
    } else {
        Write-Host "✗ Failed to start services!" -ForegroundColor Red
        exit 1
    }
}

function Stop-Services {
    Write-Host "Stopping services..." -ForegroundColor Yellow
    docker-compose down
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Services stopped!" -ForegroundColor Green
    } else {
        Write-Host "✗ Failed to stop services!" -ForegroundColor Red
        exit 1
    }
}

function Restart-Services {
    Write-Host "Restarting services..." -ForegroundColor Yellow
    Stop-Services
    Start-Sleep -Seconds 2
    Start-Services
}

function Show-Logs {
    Write-Host "Showing service logs (Ctrl+C to exit)..." -ForegroundColor Yellow
    docker-compose logs -f $SERVICE_NAME
}

function Clean-All {
    Write-Host "Cleaning up containers, volumes, and images..." -ForegroundColor Yellow
    Write-Host "WARNING: This will delete all data!" -ForegroundColor Red
    $confirm = Read-Host "Are you sure? (yes/no)"
    
    if ($confirm -eq "yes") {
        docker-compose down -v
        Write-Host "✓ Cleanup completed!" -ForegroundColor Green
    } else {
        Write-Host "Cleanup cancelled." -ForegroundColor Yellow
    }
}

function Run-Tests {
    Write-Host "Running API tests..." -ForegroundColor Yellow
    Write-Host ""
    
    # Check if service is running
    $health = $null
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8084/api/health" -Method GET -ErrorAction SilentlyContinue
    } catch {
        Write-Host "✗ Service is not running! Please start it first with: .\build.ps1 start" -ForegroundColor Red
        exit 1
    }
    
    if ($health) {
        Write-Host "✓ Service is running" -ForegroundColor Green
        Write-Host ""
        Write-Host "Running test script..." -ForegroundColor Yellow
        & .\test-api.ps1
    }
}

function Show-Status {
    Write-Host "Service Status:" -ForegroundColor Cyan
    docker-compose ps
    Write-Host ""
    
    # Check health endpoint
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8084/api/health" -Method GET -ErrorAction SilentlyContinue
        Write-Host "Health Status:" -ForegroundColor Cyan
        Write-Host "  Service: $($health.service)" -ForegroundColor White
        Write-Host "  Status: $($health.status)" -ForegroundColor Green
        Write-Host "  Database: $($health.database)" -ForegroundColor Green
        Write-Host "  Version: $($health.version)" -ForegroundColor White
    } catch {
        Write-Host "Health Status: Service not responding" -ForegroundColor Red
    }
}

function Connect-Database {
    Write-Host "Connecting to PostgreSQL database..." -ForegroundColor Yellow
    Write-Host "Default password: postgres" -ForegroundColor Gray
    Write-Host ""
    docker exec -it ai-vision-postgres psql -U postgres -d ai_vision_db
}

# Main script logic
switch ($Command.ToLower()) {
    "build" {
        Build-Image
    }
    "start" {
        Start-Services
    }
    "stop" {
        Stop-Services
    }
    "restart" {
        Restart-Services
    }
    "logs" {
        Show-Logs
    }
    "clean" {
        Clean-All
    }
    "test" {
        Run-Tests
    }
    "status" {
        Show-Status
    }
    "db" {
        Connect-Database
    }
    "help" {
        Show-Help
    }
    default {
        Write-Host "Unknown command: $Command" -ForegroundColor Red
        Show-Help
        exit 1
    }
}
