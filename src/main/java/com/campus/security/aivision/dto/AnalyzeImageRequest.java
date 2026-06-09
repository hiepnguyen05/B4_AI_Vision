package com.campus.security.aivision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for analyzing images from Camera Stream Service (B2)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeImageRequest {

    @NotBlank(message = "Camera ID is required")
    @Pattern(regexp = "^CAM[0-9]{3,}$", message = "Camera ID must follow format CAM001, CAM002, etc.")
    private String cameraId;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif)$", 
             message = "Image URL must be a valid HTTP(S) URL ending with jpg, jpeg, png, or gif")
    private String imageUrl;

    @NotBlank(message = "Timestamp is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$",
             message = "Timestamp must follow format yyyy-MM-ddTHH:mm:ss")
    private String timestamp;
}
