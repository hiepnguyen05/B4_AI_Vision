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
@Schema(description = "Detected object data with bounding box")
public class DetectedObjectDto {

    @Schema(description = "Object ID")
    private Long id;

    @Schema(description = "Object type", example = "Person")
    private String objectType;

    @Schema(description = "Detection confidence", example = "0.95")
    private Double confidence;

    @Schema(description = "Bounding box X coordinate", example = "100")
    private Integer boundingBoxX;

    @Schema(description = "Bounding box Y coordinate", example = "200")
    private Integer boundingBoxY;

    @Schema(description = "Bounding box width", example = "300")
    private Integer boundingBoxWidth;

    @Schema(description = "Bounding box height", example = "400")
    private Integer boundingBoxHeight;
}
