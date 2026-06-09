package com.campus.security.aivision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Face Match Entity
 * Stores face recognition matching results
 */
@Entity
@Table(name = "face_matches", indexes = {
    @Index(name = "idx_face_match_detection_id", columnList = "detection_id"),
    @Index(name = "idx_face_match_person_id", columnList = "person_id"),
    @Index(name = "idx_face_match_confidence", columnList = "match_confidence")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_id")
    private Detection detection;

    @Column(name = "person_id", length = 100)
    private String personId;

    @Column(name = "person_name", length = 255)
    private String personName;

    @Column(name = "match_confidence", nullable = false)
    private Double matchConfidence;

    @Column(name = "face_matched", nullable = false)
    private Boolean faceMatched;

    @Column(name = "match_threshold")
    private Double matchThreshold;

    @Column(name = "face_encoding", columnDefinition = "TEXT")
    private String faceEncoding;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    public enum MatchStatus {
        MATCHED,
        NOT_MATCHED,
        LOW_CONFIDENCE,
        MULTIPLE_FACES,
        NO_FACE_DETECTED
    }
}
