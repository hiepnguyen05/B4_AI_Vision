package com.campus.security.aivision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Detected Object Entity
 * Stores detailed information about individual objects detected in images
 */
@Entity
@Table(name = "detected_objects", indexes = {
    @Index(name = "idx_object_type", columnList = "object_type"),
    @Index(name = "idx_confidence", columnList = "confidence")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_id", nullable = false)
    private Detection detection;

    @Column(name = "object_type", nullable = false, length = 50)
    private String objectType;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "bounding_box_x")
    private Integer boundingBoxX;

    @Column(name = "bounding_box_y")
    private Integer boundingBoxY;

    @Column(name = "bounding_box_width")
    private Integer boundingBoxWidth;

    @Column(name = "bounding_box_height")
    private Integer boundingBoxHeight;

    @Column(name = "additional_attributes", columnDefinition = "TEXT")
    private String additionalAttributes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum ObjectType {
        PERSON,
        VEHICLE,
        HELMET,
        FACE,
        FIRE,
        SMOKE,
        WEAPON,
        UNKNOWN
    }
}
