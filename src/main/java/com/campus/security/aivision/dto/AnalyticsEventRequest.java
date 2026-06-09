package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending events to Analytics Service (B5)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEventRequest {

    private Long analysisId;
    private String eventType;
    private Double confidence;
    private String detectedObject;
    private String cameraId;
    private String timestamp;
}
