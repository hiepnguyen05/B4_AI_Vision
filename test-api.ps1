# PowerShell script to test AI Vision Service APIs
# Usage: .\test-api.ps1

$BASE_URL = 'http://localhost:8084/api'

Write-Host '========================================' -ForegroundColor Cyan
Write-Host 'AI Vision Service - API Test Script' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''

# Function to make HTTP request
function Test-Endpoint {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Body = $null,
        [string]$Description
    )

    Write-Host "[$Method] $Endpoint" -ForegroundColor Yellow
    Write-Host "Description: $Description" -ForegroundColor Gray

    try {
        $headers = @{
            'Content-Type' = 'application/json'
        }

        if ($Body) {
            $response = Invoke-RestMethod -Uri "$BASE_URL$Endpoint" -Method $Method -Headers $headers -Body $Body
        }
        else {
            $response = Invoke-RestMethod -Uri "$BASE_URL$Endpoint" -Method $Method -Headers $headers
        }

        Write-Host 'SUCCESS' -ForegroundColor Green
        $response | ConvertTo-Json -Depth 5 | Write-Host
    }
    catch {
        Write-Host "FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }

    Write-Host ''
    Write-Host '----------------------------------------' -ForegroundColor Gray
    Write-Host ''
}

# Test 1: Health Check
Test-Endpoint -Method 'GET' -Endpoint '/health' -Description 'Check service and database health'

# Test 2: Object Detection
$detectBody = @{
    cameraId  = 'CAM001'
    imageUrl  = 'https://example.com/images/person.jpg'
    timestamp = (Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')
} | ConvertTo-Json

Test-Endpoint -Method 'POST' -Endpoint '/vision/detect' -Body $detectBody -Description 'Detect objects from camera image'

# Test 3: Get All Detections
Test-Endpoint -Method 'GET' -Endpoint '/vision/detections' -Description 'Get all detection results'

# Test 4: Get Detection by ID
Test-Endpoint -Method 'GET' -Endpoint '/vision/detections/1' -Description 'Get specific detection by ID'

# Test 5: Face Matching
$faceMatchBody = @{
    detectionId    = 1
    personId       = 'PERSON_001'
    matchThreshold = 0.85
} | ConvertTo-Json

Test-Endpoint -Method 'POST' -Endpoint '/vision/face-match' -Body $faceMatchBody -Description 'Match face against person database'

# Test 6: Get All Face Matches
Test-Endpoint -Method 'GET' -Endpoint '/vision/face-matches' -Description 'Get all face match results'

# Test 7: Get Face Match by ID
Test-Endpoint -Method 'GET' -Endpoint '/vision/face-matches/1' -Description 'Get specific face match by ID'

# Test 8: Legacy - Analyze Image
$analyzeBody = @{
    cameraId  = 'CAM002'
    imageUrl  = 'https://example.com/images/vehicle.jpg'
    timestamp = (Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')
} | ConvertTo-Json

Test-Endpoint -Method 'POST' -Endpoint '/vision/analyze' -Body $analyzeBody -Description 'Legacy API - Analyze single image'

# Test 9: Get Statistics
Test-Endpoint -Method 'GET' -Endpoint '/vision/statistics' -Description 'Get analysis statistics'

# Test 10: Upload Image
$uploadBody = @{
    imageName = 'test-image.jpg'
    imageUrl  = 'https://example.com/images/test.jpg'
    cameraId  = 'CAM003'
} | ConvertTo-Json

Test-Endpoint -Method 'POST' -Endpoint '/vision/upload' -Body $uploadBody -Description 'Upload and analyze image'

Write-Host '========================================' -ForegroundColor Cyan
Write-Host 'All tests completed!' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
Write-Host 'Next steps:' -ForegroundColor Yellow
Write-Host "1. Check Swagger UI: $BASE_URL/swagger-ui.html" -ForegroundColor White
Write-Host '2. View database: docker exec -it ai-vision-postgres psql -U postgres -d ai_vision_db' -ForegroundColor White
Write-Host '3. Check logs: docker-compose logs -f ai-vision-service' -ForegroundColor White
Write-Host '4. Check RabbitMQ Console: http://localhost:15672 (guest/guest)' -ForegroundColor White
