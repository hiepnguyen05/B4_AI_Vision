package com.campus.security.aivision.repository;

import com.campus.security.aivision.entity.FaceMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaceMatchRepository extends JpaRepository<FaceMatch, Long> {

    List<FaceMatch> findByPersonId(String personId);

    List<FaceMatch> findByFaceMatched(Boolean faceMatched);

    List<FaceMatch> findByDetectionId(Long detectionId);

    Optional<FaceMatch> findTopByPersonIdOrderByMatchConfidenceDesc(String personId);

    List<FaceMatch> findByMatchConfidenceGreaterThanEqual(Double confidence);

    @Query("SELECT COUNT(f) FROM FaceMatch f WHERE f.faceMatched = true")
    Long countSuccessfulMatches();

    @Query("SELECT COUNT(f) FROM FaceMatch f WHERE f.faceMatched = false")
    Long countFailedMatches();

    @Query("SELECT AVG(f.matchConfidence) FROM FaceMatch f WHERE f.faceMatched = true")
    Double averageMatchConfidence();

    @Query("SELECT f FROM FaceMatch f WHERE f.createdAt BETWEEN :start AND :end")
    List<FaceMatch> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
