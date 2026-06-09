package com.campus.security.aivision.mapper;

import com.campus.security.aivision.dto.AiResultResponse;
import com.campus.security.aivision.entity.AiResult;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AiResult entity and DTOs
 */
@Component
public class AiResultMapper {

    public AiResultResponse toResponse(AiResult entity) {
        if (entity == null) {
            return null;
        }

        return AiResultResponse.builder()
                .id(entity.getId())
                .imageName(entity.getImageName())
                .imageUrl(entity.getImageUrl())
                .cameraId(entity.getCameraId())
                .detectedObject(entity.getDetectedObject())
                .confidence(entity.getConfidence())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .processingTime(entity.getProcessingTime())
                .additionalInfo(entity.getAdditionalInfo())
                .createdAt(entity.getCreatedAt())
                .analyzedAt(entity.getAnalyzedAt())
                .build();
    }
}
