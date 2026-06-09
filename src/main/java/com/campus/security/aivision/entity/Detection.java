package com.campus.security.aivision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detection Entity
 * Stores general object detection results from camera images
 */
@Entity
@Table(name = "detections", indexes = {
    @Index(name = "idx_detection_camera_id", columnList = "camera_id"),
    @Index(name = "idx_detection_timestamp", columnList = "detection_timestamp"),
    @Index(name = "idx_detection_zone_id", columnList = "zone_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Detection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "camera_id", nullable = false, length = 50)
    private String cameraId;

    @Column(name = "image_url", nullable = false, length = 1024)
    private String imageUrl;

    @Column(name = "zone_id", length = 50)
    private String zoneId;

    @Column(name = "detection_timestamp", nullable = false)
    private LocalDateTime detectionTimestamp;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DetectionStatus status;

    @OneToMany(mappedBy = "detection", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetectedObject> detectedObjects = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum DetectionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public void addDetectedObject(DetectedObject object) {
        detectedObjects.add(object);
        object.setDetection(this);
    }

    public void removeDetectedObject(DetectedObject object) {
        detectedObjects.remove(object);
        object.setDetection(null);
    }
}
