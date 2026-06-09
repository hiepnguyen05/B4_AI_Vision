package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending notifications to Notification Service (B7)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String title;
    private String message;
    private String severity;
    private Long analysisId;
    private String cameraId;
}
