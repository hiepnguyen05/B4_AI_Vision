-- Database initialization script for AI Vision Service
-- Creates tables for all entities

-- Enable UUID extension (if needed)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===== LEGACY TABLES (Keep for backward compatibility) =====

-- Create ai_results table
CREATE TABLE IF NOT EXISTS ai_results (
    id BIGSERIAL PRIMARY KEY,
    image_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    camera_id VARCHAR(50),
    detected_object VARCHAR(100) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    processing_time BIGINT,
    additional_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    analyzed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_image_name ON ai_results(image_name);
CREATE INDEX IF NOT EXISTS idx_status ON ai_results(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON ai_results(created_at);

-- Create processing_logs table
CREATE TABLE IF NOT EXISTS processing_logs (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    message TEXT,
    error_details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_request_id ON processing_logs(request_id);
CREATE INDEX IF NOT EXISTS idx_processing_created_at ON processing_logs(created_at);

-- ===== NEW TABLES (As per deployment plan) =====

-- Create detections table (main object detection table)
CREATE TABLE IF NOT EXISTS detections (
    id BIGSERIAL PRIMARY KEY,
    camera_id VARCHAR(50) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    zone_id VARCHAR(50),
    detection_timestamp TIMESTAMP NOT NULL,
    processing_time_ms BIGINT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_detection_camera_id ON detections(camera_id);
CREATE INDEX IF NOT EXISTS idx_detection_timestamp ON detections(detection_timestamp);
CREATE INDEX IF NOT EXISTS idx_detection_zone_id ON detections(zone_id);

-- Create detected_objects table (stores individual objects with bounding boxes)
CREATE TABLE IF NOT EXISTS detected_objects (
    id BIGSERIAL PRIMARY KEY,
    detection_id BIGINT NOT NULL REFERENCES detections(id) ON DELETE CASCADE,
    object_type VARCHAR(50) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    bounding_box_x INTEGER,
    bounding_box_y INTEGER,
    bounding_box_width INTEGER,
    bounding_box_height INTEGER,
    additional_attributes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_object_type ON detected_objects(object_type);
CREATE INDEX IF NOT EXISTS idx_confidence ON detected_objects(confidence);

-- Create face_matches table (stores face recognition results)
CREATE TABLE IF NOT EXISTS face_matches (
    id BIGSERIAL PRIMARY KEY,
    detection_id BIGINT REFERENCES detections(id) ON DELETE CASCADE,
    person_id VARCHAR(100),
    person_name VARCHAR(255),
    match_confidence DOUBLE PRECISION NOT NULL,
    face_matched BOOLEAN NOT NULL,
    match_threshold DOUBLE PRECISION,
    face_encoding TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    matched_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_face_match_detection_id ON face_matches(detection_id);
CREATE INDEX IF NOT EXISTS idx_face_match_person_id ON face_matches(person_id);
CREATE INDEX IF NOT EXISTS idx_face_match_confidence ON face_matches(match_confidence);

-- Create face_suggestions table (stores suggestions for low confidence matches)
CREATE TABLE IF NOT EXISTS face_suggestions (
    id BIGSERIAL PRIMARY KEY,
    face_match_id BIGINT NOT NULL REFERENCES face_matches(id) ON DELETE CASCADE,
    suggested_person_id VARCHAR(100),
    suggested_person_name VARCHAR(255),
    suggestion_confidence DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    review_notes TEXT,
    reviewed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_face_suggestion_match_id ON face_suggestions(face_match_id);
CREATE INDEX IF NOT EXISTS idx_suggestion_confidence ON face_suggestions(suggestion_confidence);
CREATE INDEX IF NOT EXISTS idx_suggestion_status ON face_suggestions(status);

-- ===== SAMPLE DATA (For testing) =====

-- Sample data for ai_results (legacy)
INSERT INTO ai_results (image_name, image_url, camera_id, detected_object, confidence, status, processing_time, created_at, analyzed_at)
VALUES 
    ('sample01.jpg', 'https://example.com/images/sample01.jpg', 'CAM001', 'Person', 0.94, 'COMPLETED', 150, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('sample02.jpg', 'https://example.com/images/sample02.jpg', 'CAM002', 'Vehicle', 0.87, 'COMPLETED', 180, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Sample data for detections
INSERT INTO detections (camera_id, image_url, zone_id, detection_timestamp, processing_time_ms, status, created_at)
VALUES 
    ('CAM001', 'https://example.com/images/det01.jpg', 'Zone_Gate_01', CURRENT_TIMESTAMP, 150, 'COMPLETED', CURRENT_TIMESTAMP),
    ('CAM002', 'https://example.com/images/det02.jpg', 'Zone_Lobby', CURRENT_TIMESTAMP, 180, 'COMPLETED', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Note: Foreign key data for detected_objects, face_matches, and face_suggestions
-- will be created by the application when detections are processed

COMMENT ON TABLE detections IS 'Main table for object detection results from cameras';
COMMENT ON TABLE detected_objects IS 'Individual objects detected with bounding box coordinates';
COMMENT ON TABLE face_matches IS 'Face recognition matching results';
COMMENT ON TABLE face_suggestions IS 'Suggestions for face matches with low confidence';

