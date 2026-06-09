package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {

    private Long totalAnalyses;
    private Long completedAnalyses;
    private Long failedAnalyses;
    private Long pendingAnalyses;
    private Double averageConfidence;
    private Double averageProcessingTime;
    private Map<String, Long> detectedObjectCounts;
    private String message;
}
