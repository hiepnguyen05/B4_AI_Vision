package com.campus.security.aivision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Face Suggestion Entity
 * Stores suggestions for face matches with low confidence
 */
@Entity
@Table(name = "face_suggestions", indexes = {
    @Index(name = "idx_face_suggestion_match_id", columnList = "face_match_id"),
    @Index(name = "idx_suggestion_confidence", columnList = "suggestion_confidence"),
    @Index(name = "idx_suggestion_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "face_match_id", nullable = false)
    private FaceMatch faceMatch;

    @Column(name = "suggested_person_id", length = 100)
    private String suggestedPersonId;

    @Column(name = "suggested_person_name", length = 255)
    private String suggestedPersonName;

    @Column(name = "suggestion_confidence", nullable = false)
    private Double suggestionConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SuggestionStatus status;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum SuggestionStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        REQUIRES_REVIEW
    }
}
