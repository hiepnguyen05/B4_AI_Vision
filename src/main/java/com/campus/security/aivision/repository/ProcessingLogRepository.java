package com.campus.security.aivision.repository;

import com.campus.security.aivision.entity.ProcessingLog;
import com.campus.security.aivision.entity.ProcessingLog.LogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Processing Log operations
 */
@Repository
public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, Long> {

    List<ProcessingLog> findByRequestId(String requestId);

    List<ProcessingLog> findByStatus(LogStatus status);

    List<ProcessingLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByStatus(LogStatus status);
}
