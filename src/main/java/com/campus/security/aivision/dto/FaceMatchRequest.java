package com.campus.security.aivision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Face matching request from Core Business Service")
public class FaceMatchRequest {

    @NotNull
    @Schema(description = "Detection ID to match faces for", example = "1")
    private Long detectionId;

    @Schema(description = "Person ID to match against", example = "PERSON_001")
    private String personId;

    @Schema(description = "Base64 encoded face image or face encoding")
    private String faceData;

    @NotNull
    @Schema(description = "Minimum confidence threshold for match", example = "0.85")
    private Double matchThreshold;
}
