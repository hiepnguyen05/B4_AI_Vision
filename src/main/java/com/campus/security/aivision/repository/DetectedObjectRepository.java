package com.campus.security.aivision.repository;

import com.campus.security.aivision.entity.DetectedObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetectedObjectRepository extends JpaRepository<DetectedObject, Long> {

    List<DetectedObject> findByDetectionId(Long detectionId);

    List<DetectedObject> findByObjectType(String objectType);

    List<DetectedObject> findByConfidenceGreaterThanEqual(Double confidence);

    @Query("SELECT o.objectType, COUNT(o) FROM DetectedObject o GROUP BY o.objectType")
    List<Object[]> countByObjectType();

    @Query("SELECT AVG(o.confidence) FROM DetectedObject o WHERE o.objectType = :objectType")
    Double averageConfidenceByType(@Param("objectType") String objectType);

    @Query("SELECT o FROM DetectedObject o WHERE o.detection.cameraId = :cameraId")
    List<DetectedObject> findByCameraId(@Param("cameraId") String cameraId);
}
