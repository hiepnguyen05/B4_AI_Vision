package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for image analysis results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeImageResponse {

    private Long analysisId;
    private String detectedObject;
    private Double confidence;
    private String status;
    private Long processingTime;
    private String message;
}
