package com.campus.security.aivision.repository;

import com.campus.security.aivision.entity.AiResult;
import com.campus.security.aivision.entity.AiResult.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AI Result operations
 */
@Repository
public interface AiResultRepository extends JpaRepository<AiResult, Long> {

    List<AiResult> findByStatus(AnalysisStatus status);

    List<AiResult> findByCameraId(String cameraId);

    List<AiResult> findByDetectedObject(String detectedObject);

    List<AiResult> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(r) FROM AiResult r WHERE r.status = :status")
    Long countByStatus(@Param("status") AnalysisStatus status);

    @Query("SELECT r.detectedObject, COUNT(r) FROM AiResult r GROUP BY r.detectedObject")
    List<Object[]> countByDetectedObject();

    @Query("SELECT AVG(r.confidence) FROM AiResult r WHERE r.status = 'COMPLETED'")
    Double averageConfidence();

    @Query("SELECT AVG(r.processingTime) FROM AiResult r WHERE r.status = 'COMPLETED'")
    Double averageProcessingTime();
}
