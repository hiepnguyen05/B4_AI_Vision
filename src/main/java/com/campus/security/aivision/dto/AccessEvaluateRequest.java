package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending data to Access Gate Service (B3)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessEvaluateRequest {

    private Long analysisId;
    private Boolean personDetected;
    private Double confidence;
    private String cameraId;
    private String timestamp;
}
