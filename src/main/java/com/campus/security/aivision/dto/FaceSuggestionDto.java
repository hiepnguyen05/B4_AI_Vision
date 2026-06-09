package com.campus.security.aivision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Face suggestion data")
public class FaceSuggestionDto {

    @Schema(description = "Suggestion ID")
    private Long id;

    @Schema(description = "Suggested person ID")
    private String suggestedPersonId;

    @Schema(description = "Suggested person name")
    private String suggestedPersonName;

    @Schema(description = "Suggestion confidence")
    private Double suggestionConfidence;

    @Schema(description = "Suggestion status")
    private String status;
}
