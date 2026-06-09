package com.campus.security.aivision.service;

import com.campus.security.aivision.dto.*;
import com.campus.security.aivision.entity.AiResult;
import com.campus.security.aivision.entity.AiResult.AnalysisStatus;
import com.campus.security.aivision.entity.ProcessingLog;
import com.campus.security.aivision.entity.ProcessingLog.LogStatus;
import com.campus.security.aivision.exception.ResourceNotFoundException;
import com.campus.security.aivision.mapper.AiResultMapper;
import com.campus.security.aivision.repository.AiResultRepository;
import com.campus.security.aivision.repository.ProcessingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for AI Vision operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiVisionService {

    private final AiResultRepository aiResultRepository;
    private final ProcessingLogRepository processingLogRepository;
    private final AiResultMapper aiResultMapper;
    private final ExternalServiceClient externalServiceClient;

    @Value("${ai.vision.simulation.enabled:true}")
    private boolean simulationEnabled;

    @Value("${ai.vision.simulation.min-confidence:0.75}")
    private double minConfidence;

    @Value("${ai.vision.simulation.max-confidence:0.99}")
    private double maxConfidence;

    private final List<String> detectedObjects = Arrays.asList("Person", "Vehicle", "Helmet", "Face");

    /**
     * Analyze image from Camera Stream Service (B2)
     */
    @Transactional
    public AnalyzeImageResponse analyzeImage(AnalyzeImageRequest request) {
        log.info("Analyzing image from camera: {}", request.getCameraId());
        
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        // Create processing log
        createLog(requestId, "IMAGE_ANALYSIS_STARTED", LogStatus.STARTED, "Image analysis started");

        try {
            // Extract image name from URL
            String imageName = extractImageName(request.getImageUrl());

            // Create AI Result entity
            AiResult aiResult = AiResult.builder()
                    .imageName(imageName)
                    .imageUrl(request.getImageUrl())
                    .cameraId(request.getCameraId())
                    .status(AnalysisStatus.PROCESSING)
                    .build();

            aiResult = aiResultRepository.save(aiResult);
            createLog(requestId, "IMAGE_SAVED", LogStatus.IN_PROGRESS, "Image saved to database");

            // Perform AI inference (simulated)
            Map<String, Object> inferenceResult = performAiInference(request.getImageUrl());
            
            String detectedObject = (String) inferenceResult.get("object");
            Double confidence = (Double) inferenceResult.get("confidence");

            // Update AI Result with inference results
            long processingTime = System.currentTimeMillis() - startTime;
            aiResult.setDetectedObject(detectedObject);
            aiResult.setConfidence(confidence);
            aiResult.setStatus(AnalysisStatus.COMPLETED);
            aiResult.setProcessingTime(processingTime);
            aiResult.setAnalyzedAt(LocalDateTime.now());

            aiResult = aiResultRepository.save(aiResult);
            createLog(requestId, "ANALYSIS_COMPLETED", LogStatus.SUCCESS, 
                     "Analysis completed: " + detectedObject + " with confidence " + confidence);

            // Notify other services asynchronously
            notifyOtherServices(aiResult);

            // Build response
            return AnalyzeImageResponse.builder()
                    .analysisId(aiResult.getId())
                    .detectedObject(detectedObject)
                    .confidence(confidence)
                    .status(AnalysisStatus.COMPLETED.name())
                    .processingTime(processingTime)
                    .message("Image analyzed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error analyzing image: {}", e.getMessage(), e);
            createLog(requestId, "ANALYSIS_FAILED", LogStatus.FAILED, "Error: " + e.getMessage());
            throw new RuntimeException("Failed to analyze image: " + e.getMessage(), e);
        }
    }

    /**
     * Upload and analyze image
     */
    @Transactional
    public AnalyzeImageResponse uploadAndAnalyze(UploadImageRequest request) {
        log.info("Uploading and analyzing image: {}", request.getImageName());

        long startTime = System.currentTimeMillis();

        // Create AI Result entity
        AiResult aiResult = AiResult.builder()
                .imageName(request.getImageName())
                .imageUrl(request.getImageUrl())
                .cameraId(request.getCameraId())
                .status(AnalysisStatus.PROCESSING)
                .build();

        aiResult = aiResultRepository.save(aiResult);

        // Perform AI inference
        Map<String, Object> inferenceResult = performAiInference(request.getImageUrl());
        
        String detectedObject = (String) inferenceResult.get("object");
        Double confidence = (Double) inferenceResult.get("confidence");

        // Update results
        long processingTime = System.currentTimeMillis() - startTime;
        aiResult.setDetectedObject(detectedObject);
        aiResult.setConfidence(confidence);
        aiResult.setStatus(AnalysisStatus.COMPLETED);
        aiResult.setProcessingTime(processingTime);
        aiResult.setAnalyzedAt(LocalDateTime.now());

        aiResult = aiResultRepository.save(aiResult);

        // Notify other services
        notifyOtherServices(aiResult);

        return AnalyzeImageResponse.builder()
                .analysisId(aiResult.getId())
                .detectedObject(detectedObject)
                .confidence(confidence)
                .status(AnalysisStatus.COMPLETED.name())
                .processingTime(processingTime)
                .message("Image uploaded and analyzed successfully")
                .build();
    }

    /**
     * Get all analysis results
     */
    public List<AiResultResponse> getAllResults() {
        log.info("Fetching all analysis results");
        List<AiResult> results = aiResultRepository.findAll();
        return results.stream()
                .map(aiResultMapper::toResponse)
                .toList();
    }

    /**
     * Get result by ID
     */
    public AiResultResponse getResultById(Long id) {
        log.info("Fetching result by ID: {}", id);
        AiResult result = aiResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + id));
        return aiResultMapper.toResponse(result);
    }

    /**
     * Delete result by ID
     */
    @Transactional
    public void deleteResult(Long id) {
        log.info("Deleting result with ID: {}", id);
        AiResult result = aiResultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + id));
        aiResultRepository.delete(result);
    }

    /**
     * Get statistics
     */
    public StatisticsResponse getStatistics() {
        log.info("Fetching statistics");

        Long totalAnalyses = aiResultRepository.count();
        Long completedAnalyses = aiResultRepository.countByStatus(AnalysisStatus.COMPLETED);
        Long failedAnalyses = aiResultRepository.countByStatus(AnalysisStatus.FAILED);
        Long pendingAnalyses = aiResultRepository.countByStatus(AnalysisStatus.PENDING);

        Double avgConfidence = aiResultRepository.averageConfidence();
        Double avgProcessingTime = aiResultRepository.averageProcessingTime();

        List<Object[]> objectCounts = aiResultRepository.countByDetectedObject();
        Map<String, Long> detectedObjectCounts = new HashMap<>();
        for (Object[] row : objectCounts) {
            detectedObjectCounts.put((String) row[0], (Long) row[1]);
        }

        return StatisticsResponse.builder()
                .totalAnalyses(totalAnalyses)
                .completedAnalyses(completedAnalyses)
                .failedAnalyses(failedAnalyses)
                .pendingAnalyses(pendingAnalyses)
                .averageConfidence(avgConfidence != null ? avgConfidence : 0.0)
                .averageProcessingTime(avgProcessingTime != null ? avgProcessingTime : 0.0)
                .detectedObjectCounts(detectedObjectCounts)
                .message("Statistics retrieved successfully")
                .build();
    }

    /**
     * Simulate AI Inference
     */
    private Map<String, Object> performAiInference(String imageUrl) {
        log.debug("Performing AI inference on: {}", imageUrl);

        if (!simulationEnabled) {
            throw new UnsupportedOperationException("AI inference is not enabled");
        }

        // Simulate processing time
        try {
            Thread.sleep(100 + new Random().nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Randomly select detected object
        String detectedObject = detectedObjects.get(new Random().nextInt(detectedObjects.size()));

        // Generate random confidence
        double confidence = minConfidence + (maxConfidence - minConfidence) * new Random().nextDouble();
        confidence = Math.round(confidence * 100.0) / 100.0;

        Map<String, Object> result = new HashMap<>();
        result.put("object", detectedObject);
        result.put("confidence", confidence);

        log.debug("AI Inference result: {} with confidence {}", detectedObject, confidence);
        return result;
    }

    /**
     * Notify other services
     */
    private void notifyOtherServices(AiResult aiResult) {
        try {
            // Notify Access Gate Service (B3)
            if ("Person".equals(aiResult.getDetectedObject())) {
                externalServiceClient.notifyAccessGate(aiResult);
            }

            // Notify Analytics Service (B5)
            externalServiceClient.notifyAnalytics(aiResult);

            // Notify Core Business Service (B6)
            externalServiceClient.notifyCoreService(aiResult);

            // Notify Notification Service (B7) for high confidence detections
            if (aiResult.getConfidence() >= 0.90) {
                externalServiceClient.notifyNotificationService(aiResult);
            }

        } catch (Exception e) {
            log.error("Error notifying external services: {}", e.getMessage(), e);
        }
    }

    /**
     * Create processing log
     */
    private void createLog(String requestId, String action, LogStatus status, String message) {
        ProcessingLog log = ProcessingLog.builder()
                .requestId(requestId)
                .action(action)
                .status(status)
                .message(message)
                .build();
        processingLogRepository.save(log);
    }

    /**
     * Extract image name from URL
     */
    private String extractImageName(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return parts[parts.length - 1];
    }
}
