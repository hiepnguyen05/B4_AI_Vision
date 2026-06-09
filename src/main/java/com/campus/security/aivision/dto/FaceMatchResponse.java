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
@Schema(description = "Face matching response")
public class FaceMatchResponse {

    @Schema(description = "Face match ID", example = "1")
    private Long matchId;

    @Schema(description = "Detection ID", example = "1")
    private Long detectionId;

    @Schema(description = "Whether face was successfully matched")
    private Boolean faceMatched;

    @Schema(description = "Matched person ID", example = "PERSON_001")
    private String personId;

    @Schema(description = "Matched person name", example = "John Doe")
    private String personName;

    @Schema(description = "Match confidence score", example = "0.95")
    private Double matchConfidence;

    @Schema(description = "Match threshold used", example = "0.85")
    private Double matchThreshold;

    @Schema(description = "Match status")
    private String status;

    @Schema(description = "List of suggestions if confidence is low")
    private List<FaceSuggestionDto> suggestions;

    @Schema(description = "Match timestamp")
    private LocalDateTime matchedAt;

    @Schema(description = "Response message")
    private String message;
}
