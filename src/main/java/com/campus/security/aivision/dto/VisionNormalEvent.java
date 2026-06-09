package com.campus.security.aivision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisionNormalEvent {
    private Long detectionId;
    private String cameraId;
    private String zoneId;
    private String imageUrl;
    private List<DetectedObjectDto> detectedObjects;
    private String timestamp;
}
