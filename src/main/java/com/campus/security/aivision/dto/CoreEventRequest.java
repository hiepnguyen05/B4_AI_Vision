package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending events to Core Business Service (B6)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreEventRequest {

    private Long analysisId;
    private String eventType;
    private String status;
    private String detectedObject;
    private Double confidence;
    private String timestamp;
}
