package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for AI Result details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResultResponse {

    private Long id;
    private String imageName;
    private String imageUrl;
    private String cameraId;
    private String detectedObject;
    private Double confidence;
    private String status;
    private Long processingTime;
    private String additionalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime analyzedAt;
}
