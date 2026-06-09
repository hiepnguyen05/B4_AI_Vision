package com.campus.security.aivision.repository;

import com.campus.security.aivision.entity.Detection;
import com.campus.security.aivision.entity.Detection.DetectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetectionRepository extends JpaRepository<Detection, Long> {

    List<Detection> findByCameraId(String cameraId);

    List<Detection> findByZoneId(String zoneId);

    List<Detection> findByStatus(DetectionStatus status);

    List<Detection> findByDetectionTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(d) FROM Detection d WHERE d.status = :status")
    Long countByStatus(@Param("status") DetectionStatus status);

    @Query("SELECT AVG(d.processingTimeMs) FROM Detection d WHERE d.status = 'COMPLETED'")
    Double averageProcessingTime();

    @Query("SELECT d FROM Detection d WHERE d.cameraId = :cameraId ORDER BY d.detectionTimestamp DESC")
    List<Detection> findLatestByCameraId(@Param("cameraId") String cameraId);
}
