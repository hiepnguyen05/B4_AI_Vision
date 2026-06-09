# AI Vision Service - Product B4

## Giới thiệu

AI Vision Service phân tích ảnh từ camera bằng AI để phát hiện đối tượng và nhận diện khuôn mặt trong hệ thống Campus Security.

**Nhóm:** B4 | **Công nghệ:** Java Spring Boot 3.2, PostgreSQL 15, Docker

## Tính năng chính

- ✅ Phát hiện đa đối tượng (Person, Vehicle, Helmet, Face, Fire, Smoke)
- ✅ Nhận diện khuôn mặt với suggestions cho confidence thấp
- ✅ Lưu trữ database PostgreSQL với 6 bảng (detections, detected_objects, face_matches, face_suggestions, ai_results, processing_logs)
- ✅ Tích hợp với B2 (Camera), B3 (Access Gate), B5 (Analytics), B6 (Core), B7 (Notification)
- ✅ REST API + Swagger UI
- ✅ Docker Compose ready

## Kiến trúc Database

```
detections (main table)
├── detected_objects (1-to-many: bounding boxes)
└── face_matches (1-to-many)
    └── face_suggestions (1-to-many)

ai_results (legacy, backward compatibility)
processing_logs (audit logs)
```

## Cài đặt và Chạy

### Chạy với Docker Compose (Khuyến nghị)

```bash
# Clone và khởi động
docker-compose up -d

# Kiểm tra logs
docker-compose logs -f ai-vision-service

# Dừng services
docker-compose down
```

Service: `http://localhost:8084/api`  
Swagger UI: `http://localhost:8084/api/swagger-ui.html`

### Chạy local (Development)

```bash
# Cài PostgreSQL và tạo database
createdb ai_vision_db

# Build và chạy
mvn clean install
mvn spring-boot:run
```

## API Endpoints

### Object Detection (Mới)
- `POST /vision/detect` - Phát hiện đa đối tượng (từ B2)
- `GET /vision/detections/{id}` - Lấy detection theo ID
- `GET /vision/detections` - Lấy tất cả detections

### Face Matching (Mới)
- `POST /vision/face-match` - Khớp khuôn mặt (từ B6)
- `GET /vision/face-matches/{id}` - Lấy face match theo ID
- `GET /vision/face-matches` - Lấy tất cả face matches

### Legacy APIs (Tương thích ngược)
- `POST /vision/analyze` - Phân tích ảnh đơn giản
- `POST /vision/upload` - Upload và phân tích
- `GET /vision/results` - Lấy kết quả
- `GET /vision/statistics` - Thống kê

### System
- `GET /health` - Health check (bao gồm database status)

## Tích hợp với các Service khác

### B2 → B4: Camera Stream gửi ảnh
```json
POST /api/vision/detect
{
  "cameraId": "CAM001",
  "imageUrl": "https://sample.com/image.jpg",
  "timestamp": "2026-06-08T10:00:00"
}
```

### B6 → B4: Core Business yêu cầu face matching
```json
POST /api/vision/face-match
{
  "detectionId": 1,
  "personId": "PERSON_001",
  "matchThreshold": 0.85
}
```

### B4 → B3: Thông báo khi phát hiện người
```json
POST http://localhost:8083/api/access/evaluate
{
  "analysisId": 1,
  "personDetected": true,
  "confidence": 0.94
}
```

### B4 → B7: Gửi cảnh báo khẩn cấp (Fire/Smoke)
```json
POST http://localhost:8087/api/notification/send
{
  "title": "EMERGENCY ALERT",
  "message": "FIRE detected at CAM001",
  "severity": "CRITICAL"
}
```

## Cấu trúc Database Schema

### detections
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary Key |
| camera_id | VARCHAR(50) | ID camera |
| image_url | VARCHAR(1024) | URL ảnh |
| zone_id | VARCHAR(50) | Mã vùng (Zone_Gate_01, Zone_Lobby) |
| detection_timestamp | TIMESTAMP | Thời điểm phát hiện |
| processing_time_ms | BIGINT | Thời gian xử lý (ms) |
| status | VARCHAR(20) | PENDING/PROCESSING/COMPLETED/FAILED |

### detected_objects
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary Key |
| detection_id | BIGINT | FK to detections |
| object_type | VARCHAR(50) | PERSON/VEHICLE/HELMET/FACE/FIRE/SMOKE |
| confidence | DOUBLE | Độ tin cậy (0.0-1.0) |
| bounding_box_x/y/width/height | INTEGER | Tọa độ bounding box |

### face_matches
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary Key |
| detection_id | BIGINT | FK to detections |
| person_id | VARCHAR(100) | ID người được khớp |
| person_name | VARCHAR(255) | Tên người |
| match_confidence | DOUBLE | Độ tin cậy khớp |
| face_matched | BOOLEAN | Kết quả khớp |
| match_threshold | DOUBLE | Ngưỡng khớp |

### face_suggestions
| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary Key |
| face_match_id | BIGINT | FK to face_matches |
| suggested_person_id | VARCHAR(100) | ID người gợi ý |
| suggestion_confidence | DOUBLE | Độ tin cậy gợi ý |
| status | VARCHAR(20) | PENDING/ACCEPTED/REJECTED |

## Testing

### Postman Collection
Import `postman/AI-Vision-Service.postman_collection.json`

### Manual Test
```bash
# Health check
curl http://localhost:8084/api/health

# Detect objects
curl -X POST http://localhost:8084/api/vision/detect \
  -H "Content-Type: application/json" \
  -d '{
    "cameraId": "CAM001",
    "imageUrl": "https://sample.com/image.jpg",
    "timestamp": "2026-06-08T10:00:00"
  }'

# Face matching
curl -X POST http://localhost:8084/api/vision/face-match \
  -H "Content-Type: application/json" \
  -d '{
    "detectionId": 1,
    "matchThreshold": 0.85
  }'
```

## Roadmap Implementation (Kế hoạch triển khai)

✅ **Giai đoạn 1:** Database Schema & JPA Entities (HOÀN THÀNH)  
🔄 **Giai đoạn 2:** Tích hợp với B2 và B6 (ĐANG THỰC HIỆN)  
⏳ **Giai đoạn 3:** Event Broker với RabbitMQ  
⏳ **Giai đoạn 4:** Plug-a-thon (ghép nối toàn khóa)  
⏳ **Giai đoạn 5:** Tài liệu và Bảo vệ BTL

## Environment Variables

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ai_vision_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
SPRING_PROFILES_ACTIVE=prod
```

## Troubleshooting

**Port 8084 đã được sử dụng:**
```bash
# Windows
netstat -ano | findstr :8084
taskkill /PID <pid> /F
```

**Database connection error:**
```bash
docker logs ai-vision-postgres
docker-compose restart postgres
```

## Contributors
**Nhóm B4** - FIT4110 Dịch vụ Kết nối và Công nghệ Nền tảng

## License
MIT License - Campus Security System Project

