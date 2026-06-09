# AI Vision Service - API Usage Guide

## Mục lục
1. [Giới thiệu](#giới-thiệu)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [Endpoints](#endpoints)
5. [Request/Response Examples](#requestresponse-examples)
6. [Error Handling](#error-handling)
7. [Integration Guide](#integration-guide)

## Giới thiệu

AI Vision Service cung cấp REST API để phân tích ảnh bằng AI, phát hiện các đối tượng như người, xe, mũ bảo hiểm và khuôn mặt. Service này là phần của hệ thống Campus Security (Product B4).

## Authentication

Hiện tại service không yêu cầu authentication (phiên bản development). Trong production, nên thêm JWT token hoặc API key.

## Base URL

- **Development:** `http://localhost:8084/api`
- **Docker:** `http://ai-vision-service:8084/api`
- **Production:** `https://ai-vision.campus.edu/api`

## Endpoints

### 1. Health Check

**GET** `/health`

Kiểm tra trạng thái service.

**Response:**
```json
{
  "status": "UP",
  "service": "AI Vision Service",
  "version": "1.0.0",
  "timestamp": "2026-01-01T10:00:00"
}
```

### 2. Analyze Image from Camera

**POST** `/vision/analyze`

Nhận ảnh từ Camera Stream Service và thực hiện phân tích AI.

**Request Body:**
```json
{
  "cameraId": "CAM001",
  "imageUrl": "https://sample.com/images/person.jpg",
  "timestamp": "2026-01-01T10:00:00"
}
```

**Validation Rules:**
- `cameraId`: Bắt buộc, format `CAM[0-9]{3,}` (ví dụ: CAM001, CAM002)
- `imageUrl`: Bắt buộc, phải là URL hợp lệ kết thúc bằng .jpg, .jpeg, .png, hoặc .gif
- `timestamp`: Bắt buộc, format `yyyy-MM-ddTHH:mm:ss`

**Response (200 OK):**
```json
{
  "analysisId": 1,
  "detectedObject": "Person",
  "confidence": 0.94,
  "status": "COMPLETED",
  "processingTime": 150,
  "message": "Image analyzed successfully"
}
```

**Response Fields:**
- `analysisId`: ID duy nhất của kết quả phân tích
- `detectedObject`: Đối tượng phát hiện (Person, Vehicle, Helmet, Face)
- `confidence`: Độ tin cậy (0.0 - 1.0)
- `status`: PENDING, PROCESSING, COMPLETED, FAILED
- `processingTime`: Thời gian xử lý (milliseconds)

### 3. Upload and Analyze Image

**POST** `/vision/upload`

Upload ảnh trực tiếp và thực hiện phân tích.

**Request Body:**
```json
{
  "imageName": "person_detected.jpg",
  "imageUrl": "https://sample.com/images/person_detected.jpg",
  "cameraId": "CAM001"
}
```

**Response (201 Created):**
```json
{
  "analysisId": 2,
  "detectedObject": "Vehicle",
  "confidence": 0.87,
  "status": "COMPLETED",
  "processingTime": 180,
  "message": "Image uploaded and analyzed successfully"
}
```

### 4. Get All Analysis Results

**GET** `/vision/results`

Lấy tất cả kết quả phân tích.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "imageName": "person_detected.jpg",
    "imageUrl": "https://sample.com/images/person_detected.jpg",
    "cameraId": "CAM001",
    "detectedObject": "Person",
    "confidence": 0.94,
    "status": "COMPLETED",
    "processingTime": 150,
    "additionalInfo": null,
    "createdAt": "2026-01-01T10:00:00",
    "analyzedAt": "2026-01-01T10:00:01"
  },
  {
    "id": 2,
    "imageName": "vehicle_check.jpg",
    "imageUrl": "https://sample.com/images/vehicle_check.jpg",
    "cameraId": "CAM002",
    "detectedObject": "Vehicle",
    "confidence": 0.87,
    "status": "COMPLETED",
    "processingTime": 180,
    "additionalInfo": null,
    "createdAt": "2026-01-01T10:05:00",
    "analyzedAt": "2026-01-01T10:05:01"
  }
]
```

### 5. Get Result by ID

**GET** `/vision/results/{id}`

Lấy kết quả phân tích theo ID.

**Path Parameters:**
- `id`: Analysis result ID (Long)

**Response (200 OK):**
```json
{
  "id": 1,
  "imageName": "person_detected.jpg",
  "imageUrl": "https://sample.com/images/person_detected.jpg",
  "cameraId": "CAM001",
  "detectedObject": "Person",
  "confidence": 0.94,
  "status": "COMPLETED",
  "processingTime": 150,
  "additionalInfo": null,
  "createdAt": "2026-01-01T10:00:00",
  "analyzedAt": "2026-01-01T10:00:01"
}
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Result not found with ID: 999",
  "path": "/api/vision/results/999",
  "timestamp": "2026-01-01T10:00:00"
}
```

### 6. Delete Result

**DELETE** `/vision/results/{id}`

Xóa kết quả phân tích theo ID.

**Path Parameters:**
- `id`: Analysis result ID (Long)

**Response (204 No Content):**
```
(Empty response body)
```

**Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Result not found with ID: 999",
  "path": "/api/vision/results/999",
  "timestamp": "2026-01-01T10:00:00"
}
```

### 7. Get Statistics

**GET** `/vision/statistics`

Lấy thống kê tổng quan về các phân tích.

**Response (200 OK):**
```json
{
  "totalAnalyses": 100,
  "completedAnalyses": 95,
  "failedAnalyses": 2,
  "pendingAnalyses": 3,
  "averageConfidence": 0.89,
  "averageProcessingTime": 145.5,
  "detectedObjectCounts": {
    "Person": 50,
    "Vehicle": 30,
    "Helmet": 15,
    "Face": 5
  },
  "message": "Statistics retrieved successfully"
}
```

## Request/Response Examples

### Example 1: Successful Person Detection

**Request:**
```bash
curl -X POST http://localhost:8084/api/vision/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "cameraId": "CAM001",
    "imageUrl": "https://sample.com/images/person.jpg",
    "timestamp": "2026-01-01T10:00:00"
  }'
```

**Response:**
```json
{
  "analysisId": 1,
  "detectedObject": "Person",
  "confidence": 0.94,
  "status": "COMPLETED",
  "processingTime": 150,
  "message": "Image analyzed successfully"
}
```

### Example 2: Validation Error

**Request:**
```bash
curl -X POST http://localhost:8084/api/vision/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "cameraId": "INVALID",
    "imageUrl": "https://sample.com/images/test.jpg",
    "timestamp": "2026-01-01T10:00:00"
  }'
```

**Response (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input parameters",
  "details": [
    "cameraId: Camera ID must follow format CAM001, CAM002, etc."
  ],
  "path": "/api/vision/analyze",
  "timestamp": "2026-01-01T10:00:00"
}
```

### Example 3: Get Statistics

**Request:**
```bash
curl http://localhost:8084/api/vision/statistics
```

**Response:**
```json
{
  "totalAnalyses": 100,
  "completedAnalyses": 95,
  "failedAnalyses": 2,
  "pendingAnalyses": 3,
  "averageConfidence": 0.89,
  "averageProcessingTime": 145.5,
  "detectedObjectCounts": {
    "Person": 50,
    "Vehicle": 30,
    "Helmet": 15,
    "Face": 5
  },
  "message": "Statistics retrieved successfully"
}
```

## Error Handling

Service sử dụng HTTP status codes chuẩn và trả về error response có cấu trúc nhất quán.

### Error Response Format

```json
{
  "status": 400,
  "error": "Error Type",
  "message": "Error description",
  "details": ["Additional error details"],
  "path": "/api/endpoint",
  "timestamp": "2026-01-01T10:00:00"
}
```

### HTTP Status Codes

| Code | Description | When it occurs |
|------|-------------|----------------|
| 200 | OK | Successful request |
| 201 | Created | Resource created successfully |
| 204 | No Content | Resource deleted successfully |
| 400 | Bad Request | Invalid input / validation error |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server error |

### Common Validation Errors

1. **Invalid Camera ID:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "details": ["cameraId: Camera ID must follow format CAM001, CAM002, etc."]
}
```

2. **Missing Required Field:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "details": ["imageUrl: Image URL is required"]
}
```

3. **Invalid URL Format:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "details": ["imageUrl: Image URL must be a valid HTTP(S) URL ending with jpg, jpeg, png, or gif"]
}
```

## Integration Guide

### Integrating as Camera Stream Service (B2)

```java
// Java example
RestTemplate restTemplate = new RestTemplate();
String url = "http://ai-vision-service:8084/api/vision/analyze";

AnalyzeImageRequest request = new AnalyzeImageRequest();
request.setCameraId("CAM001");
request.setImageUrl("https://sample.com/images/person.jpg");
request.setTimestamp("2026-01-01T10:00:00");

ResponseEntity<AnalyzeImageResponse> response = 
    restTemplate.postForEntity(url, request, AnalyzeImageResponse.class);

System.out.println("Analysis ID: " + response.getBody().getAnalysisId());
System.out.println("Detected: " + response.getBody().getDetectedObject());
System.out.println("Confidence: " + response.getBody().getConfidence());
```

### Integrating with JavaScript/Node.js

```javascript
const axios = require('axios');

const analyzeImage = async () => {
  try {
    const response = await axios.post(
      'http://localhost:8084/api/vision/analyze',
      {
        cameraId: 'CAM001',
        imageUrl: 'https://sample.com/images/person.jpg',
        timestamp: '2026-01-01T10:00:00'
      }
    );
    
    console.log('Analysis ID:', response.data.analysisId);
    console.log('Detected:', response.data.detectedObject);
    console.log('Confidence:', response.data.confidence);
  } catch (error) {
    console.error('Error:', error.response.data);
  }
};

analyzeImage();
```

### Integrating with Python

```python
import requests

url = 'http://localhost:8084/api/vision/analyze'
payload = {
    'cameraId': 'CAM001',
    'imageUrl': 'https://sample.com/images/person.jpg',
    'timestamp': '2026-01-01T10:00:00'
}

response = requests.post(url, json=payload)

if response.status_code == 200:
    data = response.json()
    print(f"Analysis ID: {data['analysisId']}")
    print(f"Detected: {data['detectedObject']}")
    print(f"Confidence: {data['confidence']}")
else:
    print(f"Error: {response.json()}")
```

## Rate Limiting & Best Practices

### Best Practices

1. **Handle timeouts:** Set appropriate timeout values (5-10 seconds)
2. **Retry logic:** Implement exponential backoff for failed requests
3. **Error handling:** Always handle 4xx and 5xx errors properly
4. **Logging:** Log all API calls for debugging and monitoring
5. **Testing:** Test with various image types and error scenarios

### Performance Considerations

- Average processing time: 100-200ms per image
- Recommended max concurrent requests: 100
- Database connection pool: 10 connections
- Image URL should be accessible within 3 seconds

## Support & Documentation

- **Swagger UI:** http://localhost:8084/api/swagger-ui.html
- **OpenAPI Spec:** `/openapi.yaml`
- **Source Code:** [GitHub Repository]
- **Contact:** groupb4@campus.edu
