package com.campus.security.aivision.service;

import com.campus.security.aivision.dto.*;
import com.campus.security.aivision.entity.*;
import com.campus.security.aivision.exception.ResourceNotFoundException;
import com.campus.security.aivision.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for object detection operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionService {

    private final DetectionRepository detectionRepository;
    private final DetectedObjectRepository detectedObjectRepository;
    private final ProcessingLogRepository processingLogRepository;
    private final ExternalServiceClient externalServiceClient;
    private final RabbitMQSender rabbitMQSender;

    private final Random random = new Random();
    private final List<String> objectTypes = Arrays.asList("PERSON", "VEHICLE", "HELMET", "FACE", "FIRE", "SMOKE");

    /**
     * Detect objects in image from Camera Service (B2)
     */
    @Transactional
    public DetectionResponse detectObjects(AnalyzeImageRequest request) {
        log.info("Starting object detection for camera: {}", request.getCameraId());
        
        String requestId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();

        createLog(requestId, "DETECTION_STARTED", ProcessingLog.LogStatus.STARTED, 
                 "Object detection started for camera: " + request.getCameraId());

        try {
            // Create Detection entity
            Detection detection = Detection.builder()
                    .cameraId(request.getCameraId())
                    .imageUrl(request.getImageUrl())
                    .zoneId(extractZoneId(request.getCameraId()))
                    .detectionTimestamp(LocalDateTime.now())
                    .status(Detection.DetectionStatus.PROCESSING)
                    .build();

            detection = detectionRepository.save(detection);
            createLog(requestId, "DETECTION_SAVED", ProcessingLog.LogStatus.IN_PROGRESS, 
                     "Detection record created with ID: " + detection.getId());

            // Simulate AI detection - detect multiple objects
            List<DetectedObjectDto> detectedObjectDtos = performObjectDetection(detection);

            // Update detection status
            long processingTime = System.currentTimeMillis() - startTime;
            detection.setProcessingTimeMs(processingTime);
            detection.setStatus(Detection.DetectionStatus.COMPLETED);
            detection.setUpdatedAt(LocalDateTime.now());
            detectionRepository.save(detection);

            createLog(requestId, "DETECTION_COMPLETED", ProcessingLog.LogStatus.SUCCESS,
                     String.format("Detected %d objects in %dms", detectedObjectDtos.size(), processingTime));

            // Notify other services
            notifyOtherServices(detection, detectedObjectDtos);

            return DetectionResponse.builder()
                    .detectionId(detection.getId())
                    .cameraId(detection.getCameraId())
                    .zoneId(detection.getZoneId())
                    .imageUrl(detection.getImageUrl())
                    .status(detection.getStatus().name())
                    .processingTimeMs(processingTime)
                    .objectCount(detectedObjectDtos.size())
                    .detectedObjects(detectedObjectDtos)
                    .detectionTimestamp(detection.getDetectionTimestamp())
                    .message("Detection completed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error during object detection: {}", e.getMessage(), e);
            createLog(requestId, "DETECTION_FAILED", ProcessingLog.LogStatus.FAILED, 
                     "Error: " + e.getMessage());
            throw new RuntimeException("Failed to detect objects: " + e.getMessage(), e);
        }
    }

    /**
     * Get detection by ID
     */
    public DetectionResponse getDetectionById(Long detectionId) {
        log.info("Fetching detection with ID: {}", detectionId);
        
        Detection detection = detectionRepository.findById(detectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Detection not found with ID: " + detectionId));

        List<DetectedObjectDto> objectDtos = detectedObjectRepository.findByDetectionId(detectionId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return DetectionResponse.builder()
                .detectionId(detection.getId())
                .cameraId(detection.getCameraId())
                .zoneId(detection.getZoneId())
                .imageUrl(detection.getImageUrl())
                .status(detection.getStatus().name())
                .processingTimeMs(detection.getProcessingTimeMs())
                .objectCount(objectDtos.size())
                .detectedObjects(objectDtos)
                .detectionTimestamp(detection.getDetectionTimestamp())
                .message("Detection retrieved successfully")
                .build();
    }

    /**
     * Get all detections
     */
    public List<DetectionResponse> getAllDetections() {
        log.info("Fetching all detections");
        
        return detectionRepository.findAll().stream()
                .map(detection -> {
                    List<DetectedObjectDto> objects = detectedObjectRepository.findByDetectionId(detection.getId())
                            .stream()
                            .map(this::mapToDto)
                            .collect(Collectors.toList());

                    return DetectionResponse.builder()
                            .detectionId(detection.getId())
                            .cameraId(detection.getCameraId())
                            .zoneId(detection.getZoneId())
                            .imageUrl(detection.getImageUrl())
                            .status(detection.getStatus().name())
                            .processingTimeMs(detection.getProcessingTimeMs())
                            .objectCount(objects.size())
                            .detectedObjects(objects)
                            .detectionTimestamp(detection.getDetectionTimestamp())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Perform AI object detection (simulated)
     */
    private List<DetectedObjectDto> performObjectDetection(Detection detection) {
        log.debug("Performing object detection for detection ID: {}", detection.getId());

        // Simulate detecting 1-4 objects
        int numObjects = 1 + random.nextInt(4);
        List<DetectedObjectDto> detectedObjects = new ArrayList<>();

        for (int i = 0; i < numObjects; i++) {
            String objectType = objectTypes.get(random.nextInt(objectTypes.size()));
            double confidence = 0.75 + (random.nextDouble() * 0.24); // 0.75 - 0.99

            // Generate random bounding box
            int x = random.nextInt(800);
            int y = random.nextInt(600);
            int width = 100 + random.nextInt(300);
            int height = 100 + random.nextInt(300);

            DetectedObject detectedObject = DetectedObject.builder()
                    .detection(detection)
                    .objectType(objectType)
                    .confidence(Math.round(confidence * 100.0) / 100.0)
                    .boundingBoxX(x)
                    .boundingBoxY(y)
                    .boundingBoxWidth(width)
                    .boundingBoxHeight(height)
                    .build();

            detectedObject = detectedObjectRepository.save(detectedObject);
            detectedObjects.add(mapToDto(detectedObject));
        }

        return detectedObjects;
    }

    /**
     * Notify other services
     */
    private void notifyOtherServices(Detection detection, List<DetectedObjectDto> objects) {
        try {
            // Check if any dangerous objects detected (FIRE, SMOKE)
            boolean hasDangerousObject = objects.stream()
                    .anyMatch(obj -> obj.getObjectType().equals("FIRE") || obj.getObjectType().equals("SMOKE"));

            // Notify Access Gate (B3) if person detected
            boolean hasPersonDetected = objects.stream()
                    .anyMatch(obj -> obj.getObjectType().equals("PERSON"));
            
            if (hasPersonDetected) {
                externalServiceClient.notifyAccessGateDetection(detection);
            }

            // Notify Analytics Service (B5)
            externalServiceClient.notifyAnalyticsDetection(detection, objects);

            // Notify Core Business Service (B6)
            externalServiceClient.notifyCoreServiceDetection(detection);

            // Notify Notification Service (B7) for dangerous objects
            if (hasDangerousObject) {
                externalServiceClient.notifyEmergency(detection, objects);
            }

            // --- RabbitMQ Event Publishing ---
            // 1. Publish standard detection event
            VisionNormalEvent normalEvent = VisionNormalEvent.builder()
                    .detectionId(detection.getId())
                    .cameraId(detection.getCameraId())
                    .zoneId(detection.getZoneId())
                    .imageUrl(detection.getImageUrl())
                    .detectedObjects(objects)
                    .timestamp(detection.getDetectionTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                    .build();
            rabbitMQSender.sendNormalEvent(normalEvent);

            // 2. If fire/smoke detected, publish critical alert event
            if (hasDangerousObject) {
                List<String> dangerousObjects = objects.stream()
                        .filter(obj -> obj.getObjectType().equals("FIRE") || obj.getObjectType().equals("SMOKE"))
                        .map(DetectedObjectDto::getObjectType)
                        .collect(Collectors.toList());

                String alertMessage = String.format("RabbitMQ EMERGENCY: %s detected at camera %s in zone %s",
                        String.join(", ", dangerousObjects),
                        detection.getCameraId(),
                        detection.getZoneId());

                VisionAlertEvent alertEvent = VisionAlertEvent.builder()
                        .detectionId(detection.getId())
                        .cameraId(detection.getCameraId())
                        .zoneId(detection.getZoneId())
                        .imageUrl(detection.getImageUrl())
                        .detectedObjects(dangerousObjects)
                        .severity("CRITICAL")
                        .timestamp(detection.getDetectionTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                        .message(alertMessage)
                        .build();
                rabbitMQSender.sendAlertEvent(alertEvent);
            }

        } catch (Exception e) {
            log.error("Error notifying external services and publishing events: {}", e.getMessage(), e);
        }
    }

    /**
     * Map entity to DTO
     */
    private DetectedObjectDto mapToDto(DetectedObject object) {
        return DetectedObjectDto.builder()
                .id(object.getId())
                .objectType(object.getObjectType())
                .confidence(object.getConfidence())
                .boundingBoxX(object.getBoundingBoxX())
                .boundingBoxY(object.getBoundingBoxY())
                .boundingBoxWidth(object.getBoundingBoxWidth())
                .boundingBoxHeight(object.getBoundingBoxHeight())
                .build();
    }

    /**
     * Extract zone ID from camera ID
     */
    private String extractZoneId(String cameraId) {
        // Simple mapping: CAM001 -> Zone_Gate_01, CAM002 -> Zone_Lobby, etc.
        if (cameraId.contains("001")) return "Zone_Gate_01";
        if (cameraId.contains("002")) return "Zone_Lobby";
        if (cameraId.contains("003")) return "Zone_Parking";
        return "Zone_Unknown";
    }

    /**
     * Create processing log
     */
    private void createLog(String requestId, String action, ProcessingLog.LogStatus status, String message) {
        ProcessingLog log = ProcessingLog.builder()
                .requestId(requestId)
                .action(action)
                .status(status)
                .message(message)
                .build();
        processingLogRepository.save(log);
    }
}
