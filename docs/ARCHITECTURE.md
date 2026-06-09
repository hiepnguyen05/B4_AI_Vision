# AI Vision Service - Architecture Documentation

## Mục lục
1. [Tổng quan kiến trúc](#tổng-quan-kiến-trúc)
2. [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
3. [Các lớp ứng dụng](#các-lớp-ứng-dụng)
4. [Luồng xử lý dữ liệu](#luồng-xử-lý-dữ-liệu)
5. [Database Design](#database-design)
6. [Communication Patterns](#communication-patterns)
7. [Security & Reliability](#security--reliability)

## Tổng quan kiến trúc

AI Vision Service được thiết kế theo kiến trúc Microservices, tuân thủ các nguyên tắc:

- **Single Responsibility:** Service chỉ tập trung vào AI image analysis
- **Loose Coupling:** Giao tiếp với services khác qua REST API
- **High Cohesion:** Các components liên quan chặt chẽ được nhóm lại
- **Independent Deployment:** Có thể deploy độc lập không ảnh hưởng services khác

### Technology Stack

```
┌────────────────────────────────────────────┐
│         Presentation Layer                 │
│    SpringDoc OpenAPI + Swagger UI          │
├────────────────────────────────────────────┤
│         Application Layer                  │
│         Spring Boot 3.2.0                  │
│         Spring Web MVC                     │
├────────────────────────────────────────────┤
│         Business Logic Layer               │
│         Service Components                 │
│         AI Inference Engine (Simulated)    │
├────────────────────────────────────────────┤
│         Data Access Layer                  │
│         Spring Data JPA                    │
│         Hibernate                          │
├────────────────────────────────────────────┤
│         Database Layer                     │
│         PostgreSQL 15                      │
└────────────────────────────────────────────┘
```

## Kiến trúc hệ thống

### High-Level Architecture

```
┌─────────────┐
│  Camera (B2)│──────┐
└─────────────┘      │
                     │ POST /analyze
                     ↓
            ┌────────────────────┐
            │  AI Vision (B4)    │
            │  - Controller      │
            │  - Service         │
            │  - Repository      │
            │  - Database        │
            └────────────────────┘
                     │
      ┌──────────────┼──────────────┬──────────────┐
      │              │              │              │
      ↓              ↓              ↓              ↓
┌──────────┐  ┌───────────┐  ┌──────────┐  ┌──────────────┐
│Access(B3)│  │Analytics  │  │Core (B6) │  │Notification  │
│          │  │  (B5)     │  │          │  │   (B7)       │
└──────────┘  └───────────┘  └──────────┘  └──────────────┘
```

### Component Architecture

```
AI Vision Service
│
├── API Gateway Layer
│   └── Spring Web MVC + Filters
│
├── Controller Layer
│   ├── AiVisionController
│   │   ├── analyzeImage()
│   │   ├── uploadImage()
│   │   ├── getAllResults()
│   │   ├── getResultById()
│   │   ├── deleteResult()
│   │   └── getStatistics()
│   └── HealthController
│       └── health()
│
├── Service Layer
│   ├── AiVisionService
│   │   ├── Image Analysis
│   │   ├── AI Inference
│   │   ├── Result Processing
│   │   └── Statistics
│   └── ExternalServiceClient
│       ├── notifyAccessGate()
│       ├── notifyAnalytics()
│       ├── notifyCoreService()
│       └── notifyNotificationService()
│
├── Repository Layer
│   ├── AiResultRepository
│   └── ProcessingLogRepository
│
├── Entity Layer
│   ├── AiResult
│   └── ProcessingLog
│
└── Cross-Cutting Concerns
    ├── Exception Handling
    ├── Validation
    ├── Logging
    └── Configuration
```

## Các lớp ứng dụng

### 1. Controller Layer

**Trách nhiệm:**
- Nhận HTTP requests
- Validate input
- Delegate đến Service layer
- Format responses
- Handle HTTP errors

**Key Components:**
- `AiVisionController`: Main REST endpoints
- `HealthController`: Health check endpoint

**Design Patterns:**
- RESTful API design
- Request/Response DTOs
- HTTP status code standardization

### 2. Service Layer

**Trách nhiệm:**
- Business logic
- Transaction management
- AI inference simulation
- Orchestrate repository calls
- External service communication

**Key Components:**
- `AiVisionService`: Core business logic
- `ExternalServiceClient`: Service-to-service communication

**Design Patterns:**
- Service pattern
- Facade pattern (ExternalServiceClient)
- Strategy pattern (AI inference)

### 3. Repository Layer

**Trách nhiệm:**
- Data persistence
- Query execution
- Transaction management
- Database abstraction

**Key Components:**
- `AiResultRepository`: CRUD + custom queries
- `ProcessingLogRepository`: Logging operations

**Design Patterns:**
- Repository pattern
- Data Access Object (DAO) pattern

### 4. Entity Layer

**Trách nhiệm:**
- Domain model representation
- JPA mappings
- Database schema definition

**Key Components:**
- `AiResult`: Main domain entity
- `ProcessingLog`: Audit logging entity

**Design Patterns:**
- Entity pattern
- Value Object pattern (enums)

### 5. DTO Layer

**Trách nhiệm:**
- Data transfer between layers
- Input validation
- Response formatting

**Key Components:**
- Request DTOs: `AnalyzeImageRequest`, `UploadImageRequest`
- Response DTOs: `AnalyzeImageResponse`, `AiResultResponse`, `StatisticsResponse`
- Error DTOs: `ErrorResponse`

**Design Patterns:**
- Data Transfer Object (DTO) pattern
- Builder pattern

### 6. Exception Handling

**Trách nhiệm:**
- Global exception handling
- Error response formatting
- Logging errors

**Key Components:**
- `GlobalExceptionHandler`: @RestControllerAdvice
- Custom exceptions: `ResourceNotFoundException`

**Design Patterns:**
- Exception handling pattern
- Template method pattern

## Luồng xử lý dữ liệu

### Analyze Image Flow

```
1. Camera Service (B2)
   │
   ├─> POST /api/vision/analyze
   │   Body: { cameraId, imageUrl, timestamp }
   │
2. AiVisionController
   │
   ├─> Validate Request (@Valid)
   │
3. AiVisionService
   │
   ├─> Generate Request ID
   ├─> Create Processing Log (STARTED)
   │
   ├─> Save AiResult (status: PROCESSING)
   │   └─> AiResultRepository.save()
   │       └─> PostgreSQL INSERT
   │
   ├─> Perform AI Inference
   │   └─> performAiInference()
   │       ├─> Simulate processing delay
   │       ├─> Random object detection
   │       └─> Generate confidence score
   │
   ├─> Update AiResult (status: COMPLETED)
   │   └─> AiResultRepository.save()
   │       └─> PostgreSQL UPDATE
   │
   ├─> Create Processing Log (SUCCESS)
   │
   ├─> Notify External Services (async)
   │   │
   │   ├─> if (Person detected)
   │   │   └─> POST to Access Gate (B3)
   │   │
   │   ├─> POST to Analytics (B5)
   │   │
   │   ├─> POST to Core Business (B6)
   │   │
   │   └─> if (confidence >= 0.90)
   │       └─> POST to Notification (B7)
   │
4. Return Response
   │
   └─> AnalyzeImageResponse
       { analysisId, detectedObject, confidence, ... }
```

### Error Handling Flow

```
Exception occurs
   │
   ├─> GlobalExceptionHandler
   │   │
   │   ├─> ResourceNotFoundException
   │   │   └─> 404 NOT FOUND
   │   │
   │   ├─> MethodArgumentNotValidException
   │   │   └─> 400 BAD REQUEST
   │   │       └─> ErrorResponse with validation details
   │   │
   │   └─> Exception (catch-all)
   │       └─> 500 INTERNAL SERVER ERROR
   │
   └─> Log error
       └─> Return ErrorResponse
```

## Database Design

### Schema Overview

```sql
-- ai_results table
CREATE TABLE ai_results (
    id BIGSERIAL PRIMARY KEY,
    image_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    camera_id VARCHAR(50),
    detected_object VARCHAR(100) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    processing_time BIGINT,
    additional_info TEXT,
    created_at TIMESTAMP NOT NULL,
    analyzed_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_image_name ON ai_results(image_name);
CREATE INDEX idx_status ON ai_results(status);
CREATE INDEX idx_created_at ON ai_results(created_at);

-- processing_logs table
CREATE TABLE processing_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message TEXT,
    error_details TEXT,
    created_at TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_request_id ON processing_logs(request_id);
CREATE INDEX idx_processing_created_at ON processing_logs(created_at);
```

### Entity Relationships

```
ai_results (1) ─────────── (N) processing_logs
                (conceptual, via request_id)
```

### Database Access Patterns

1. **Write Operations:**
   - INSERT new analysis result
   - UPDATE result status
   - INSERT processing log

2. **Read Operations:**
   - SELECT all results
   - SELECT by ID
   - SELECT by status
   - SELECT by camera ID
   - Aggregate queries (statistics)

3. **Performance Optimization:**
   - Indexed columns: image_name, status, created_at, request_id
   - Connection pooling: HikariCP (max 10 connections)
   - Query optimization: JPA Criteria API

## Communication Patterns

### Synchronous Communication

**Pattern:** REST API (HTTP/JSON)

**When:** Request/Response operations

```
Camera (B2) ───> AI Vision (B4)
                      │
                      └───> Response immediately
```

### Asynchronous Communication

**Pattern:** Fire-and-forget notifications

**When:** Notifying other services

```
AI Vision (B4) ───┐
                  ├───> Access Gate (B3)
                  ├───> Analytics (B5)
                  ├───> Core (B6)
                  └───> Notification (B7)
```

**Implementation:**
- Try-catch blocks
- Continue even if notification fails
- Log failures for monitoring

### Service Contracts

#### B2 → B4: Analyze Image

```json
Request:
POST /api/vision/analyze
{
  "cameraId": "CAM001",
  "imageUrl": "https://...",
  "timestamp": "2026-01-01T10:00:00"
}

Response:
{
  "analysisId": 1,
  "detectedObject": "Person",
  "confidence": 0.94,
  "status": "COMPLETED",
  "processingTime": 150,
  "message": "Image analyzed successfully"
}
```

#### B4 → B3: Access Evaluation

```json
POST /api/access/evaluate
{
  "analysisId": 1,
  "personDetected": true,
  "confidence": 0.94,
  "cameraId": "CAM001",
  "timestamp": "2026-01-01T10:00:00"
}
```

#### B4 → B5: Analytics Event

```json
POST /api/analytics/events
{
  "analysisId": 1,
  "eventType": "VISION_DETECTION",
  "confidence": 0.94,
  "detectedObject": "Person",
  "cameraId": "CAM001",
  "timestamp": "2026-01-01T10:00:00"
}
```

#### B4 → B6: Core Event

```json
POST /api/core/events
{
  "analysisId": 1,
  "eventType": "VISION_RESULT",
  "status": "DETECTED",
  "detectedObject": "Person",
  "confidence": 0.94,
  "timestamp": "2026-01-01T10:00:00"
}
```

#### B4 → B7: Notification

```json
POST /api/notification/send
{
  "title": "AI Vision Alert",
  "message": "Person detected with 94% confidence at camera CAM001",
  "severity": "HIGH",
  "analysisId": 1,
  "cameraId": "CAM001"
}
```

## Security & Reliability

### Security Considerations

1. **Input Validation:**
   - Jakarta Validation annotations
   - Regex patterns for IDs and URLs
   - Size limits for requests

2. **SQL Injection Prevention:**
   - JPA/Hibernate parameterized queries
   - No raw SQL in application code

3. **Error Handling:**
   - No sensitive data in error messages
   - Structured error responses
   - Detailed logging for debugging

4. **Future Enhancements:**
   - JWT authentication
   - API rate limiting
   - HTTPS/TLS encryption

### Reliability Patterns

1. **Health Checks:**
   - `/health` endpoint
   - Database connection check
   - Service status monitoring

2. **Logging:**
   - SLF4J + Logback
   - Request/Response logging
   - Error tracking
   - Processing logs in database

3. **Transaction Management:**
   - @Transactional annotations
   - ACID compliance
   - Rollback on errors

4. **Error Recovery:**
   - Try-catch blocks
   - Graceful degradation
   - Retry logic for external calls (future)

### Performance Considerations

1. **Database:**
   - Connection pooling (HikariCP)
   - Indexes on frequently queried columns
   - Pagination for large result sets

2. **Application:**
   - Async external notifications
   - Caching (future: Redis)
   - Lazy loading for entities

3. **Scalability:**
   - Stateless design
   - Horizontal scaling ready
   - Docker containerization

## Deployment Architecture

### Docker Deployment

```
┌─────────────────────────────────────────┐
│         Docker Host                     │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │  ai-vision-network (bridge)       │  │
│  │                                   │  │
│  │  ┌──────────────────────────┐    │  │
│  │  │ ai-vision-service        │    │  │
│  │  │ Port: 8084               │    │  │
│  │  │ Image: Built from source │    │  │
│  │  └──────────────────────────┘    │  │
│  │           │                       │  │
│  │           ↓ JDBC                  │  │
│  │  ┌──────────────────────────┐    │  │
│  │  │ ai-vision-postgres       │    │  │
│  │  │ Port: 5432               │    │  │
│  │  │ Image: postgres:15-alpine│    │  │
│  │  │ Volume: postgres_data    │    │  │
│  │  └──────────────────────────┘    │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

### Environment Configuration

**Development:**
```yaml
spring:
  profiles:
    active: dev
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop
```

**Production:**
```yaml
spring:
  profiles:
    active: prod
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
```

## Monitoring & Observability

### Metrics (Future Enhancement)

- Request rate
- Response time
- Error rate
- Database connection pool
- AI inference time

### Logging Levels

- **DEBUG:** Development detailed logs
- **INFO:** Normal operations
- **WARN:** Potential issues
- **ERROR:** Actual errors

### Health Monitoring

```
GET /health
Response:
{
  "status": "UP",
  "service": "AI Vision Service",
  "version": "1.0.0",
  "timestamp": "2026-01-01T10:00:00"
}
```

## Future Enhancements

1. **Real AI Integration:**
   - TensorFlow/PyTorch models
   - YOLO for object detection
   - Real-time inference

2. **Message Queue:**
   - Kafka/RabbitMQ for async communication
   - Event-driven architecture

3. **Caching:**
   - Redis for frequently accessed data
   - Image result caching

4. **Advanced Features:**
   - Batch processing
   - Real-time video stream analysis
   - AI model versioning

5. **Security:**
   - OAuth 2.0 / JWT
   - API Gateway integration
   - Rate limiting

6. **Monitoring:**
   - Prometheus metrics
   - Grafana dashboards
   - Distributed tracing (Jaeger)
