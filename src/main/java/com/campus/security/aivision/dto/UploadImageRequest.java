package com.campus.security.aivision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for uploading images directly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadImageRequest {

    @NotBlank(message = "Image name is required")
    private String imageName;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Pattern(regexp = "^CAM[0-9]{3,}$", message = "Camera ID must follow format CAM001, CAM002, etc.")
    private String cameraId;
}
