package com.campus.security.aivision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detection response with all detected objects")
public class DetectionResponse {

    @Schema(description = "Detection ID", example = "1")
    private Long detectionId;

    @Schema(description = "Camera ID", example = "CAM001")
    private String cameraId;

    @Schema(description = "Zone ID", example = "Zone_Lobby")
    private String zoneId;

    @Schema(description = "Image URL")
    private String imageUrl;

    @Schema(description = "Detection status")
    private String status;

    @Schema(description = "Processing time in milliseconds", example = "150")
    private Long processingTimeMs;

    @Schema(description = "Number of objects detected", example = "3")
    private Integer objectCount;

    @Schema(description = "List of detected objects")
    private List<DetectedObjectDto> detectedObjects;

    @Schema(description = "Detection timestamp")
    private LocalDateTime detectionTimestamp;

    @Schema(description = "Response message")
    private String message;
}
