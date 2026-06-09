package com.campus.security.aivision.service;

import com.campus.security.aivision.dto.*;
import com.campus.security.aivision.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Client for communicating with external services
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${external.services.access-gate.url:http://localhost:8083/api/access}")
    private String accessGateUrl;

    @Value("${external.services.analytics.url:http://localhost:8085/api/analytics}")
    private String analyticsUrl;

    @Value("${external.services.core.url:http://localhost:8086/api/core}")
    private String coreUrl;

    @Value("${external.services.notification.url:http://localhost:8087/api/notification}")
    private String notificationUrl;

    /**
     * Notify Access Gate Service (B3)
     */
    public void notifyAccessGate(AiResult aiResult) {
        try {
            log.info("Notifying Access Gate Service for analysis ID: {}", aiResult.getId());

            AccessEvaluateRequest request = AccessEvaluateRequest.builder()
                    .analysisId(aiResult.getId())
                    .personDetected("Person".equals(aiResult.getDetectedObject()))
                    .confidence(aiResult.getConfidence())
                    .cameraId(aiResult.getCameraId())
                    .timestamp(aiResult.getCreatedAt().format(FORMATTER))
                    .build();

            String url = accessGateUrl + "/evaluate";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Access Gate Service");
        } catch (Exception e) {
            log.error("Failed to notify Access Gate Service: {}", e.getMessage());
        }
    }

    /**
     * Notify Analytics Service (B5)
     */
    public void notifyAnalytics(AiResult aiResult) {
        try {
            log.info("Notifying Analytics Service for analysis ID: {}", aiResult.getId());

            AnalyticsEventRequest request = AnalyticsEventRequest.builder()
                    .analysisId(aiResult.getId())
                    .eventType("VISION_DETECTION")
                    .confidence(aiResult.getConfidence())
                    .detectedObject(aiResult.getDetectedObject())
                    .cameraId(aiResult.getCameraId())
                    .timestamp(aiResult.getCreatedAt().format(FORMATTER))
                    .build();

            String url = analyticsUrl + "/events";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Analytics Service");
        } catch (Exception e) {
            log.error("Failed to notify Analytics Service: {}", e.getMessage());
        }
    }

    /**
     * Notify Core Business Service (B6)
     */
    public void notifyCoreService(AiResult aiResult) {
        try {
            log.info("Notifying Core Business Service for analysis ID: {}", aiResult.getId());

            CoreEventRequest request = CoreEventRequest.builder()
                    .analysisId(aiResult.getId())
                    .eventType("VISION_RESULT")
                    .status(aiResult.getStatus().name())
                    .detectedObject(aiResult.getDetectedObject())
                    .confidence(aiResult.getConfidence())
                    .timestamp(aiResult.getCreatedAt().format(FORMATTER))
                    .build();

            String url = coreUrl + "/events";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Core Business Service");
        } catch (Exception e) {
            log.error("Failed to notify Core Business Service: {}", e.getMessage());
        }
    }

    /**
     * Notify Notification Service (B7)
     */
    public void notifyNotificationService(AiResult aiResult) {
        try {
            log.info("Notifying Notification Service for analysis ID: {}", aiResult.getId());

            String severity = aiResult.getConfidence() >= 0.95 ? "HIGH" : "MEDIUM";
            String message = String.format("%s detected with %.2f%% confidence at camera %s",
                    aiResult.getDetectedObject(), 
                    aiResult.getConfidence() * 100,
                    aiResult.getCameraId());

            NotificationRequest request = NotificationRequest.builder()
                    .title("AI Vision Alert")
                    .message(message)
                    .severity(severity)
                    .analysisId(aiResult.getId())
                    .cameraId(aiResult.getCameraId())
                    .build();

            String url = notificationUrl + "/send";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Notification Service");
        } catch (Exception e) {
            log.error("Failed to notify Notification Service: {}", e.getMessage());
        }
    }

    // ===== NEW METHODS FOR DETECTION SERVICE =====

    /**
     * Notify Access Gate Service with Detection data
     */
    public void notifyAccessGateDetection(Detection detection) {
        try {
            log.info("Notifying Access Gate Service for detection ID: {}", detection.getId());

            AccessEvaluateRequest request = AccessEvaluateRequest.builder()
                    .analysisId(detection.getId())
                    .personDetected(true)
                    .confidence(0.90)
                    .cameraId(detection.getCameraId())
                    .timestamp(detection.getDetectionTimestamp().format(FORMATTER))
                    .build();

            String url = accessGateUrl + "/evaluate";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Access Gate Service");
        } catch (Exception e) {
            log.error("Failed to notify Access Gate Service: {}", e.getMessage());
        }
    }

    /**
     * Notify Analytics Service with Detection data
     */
    public void notifyAnalyticsDetection(Detection detection, List<DetectedObjectDto> objects) {
        try {
            log.info("Notifying Analytics Service for detection ID: {}", detection.getId());

            for (DetectedObjectDto obj : objects) {
                AnalyticsEventRequest request = AnalyticsEventRequest.builder()
                        .analysisId(detection.getId())
                        .eventType("OBJECT_DETECTED")
                        .confidence(obj.getConfidence())
                        .detectedObject(obj.getObjectType())
                        .cameraId(detection.getCameraId())
                        .timestamp(detection.getDetectionTimestamp().format(FORMATTER))
                        .build();

                String url = analyticsUrl + "/events";
                restTemplate.postForObject(url, request, Object.class);
            }
            
            log.info("Successfully notified Analytics Service");
        } catch (Exception e) {
            log.error("Failed to notify Analytics Service: {}", e.getMessage());
        }
    }

    /**
     * Notify Core Business Service with Detection data
     */
    public void notifyCoreServiceDetection(Detection detection) {
        try {
            log.info("Notifying Core Business Service for detection ID: {}", detection.getId());

            CoreEventRequest request = CoreEventRequest.builder()
                    .analysisId(detection.getId())
                    .eventType("DETECTION_COMPLETED")
                    .status(detection.getStatus().name())
                    .detectedObject("Multiple")
                    .confidence(0.90)
                    .timestamp(detection.getDetectionTimestamp().format(FORMATTER))
                    .build();

            String url = coreUrl + "/events";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Core Business Service");
        } catch (Exception e) {
            log.error("Failed to notify Core Business Service: {}", e.getMessage());
        }
    }

    /**
     * Notify emergency for dangerous objects (FIRE, SMOKE)
     */
    public void notifyEmergency(Detection detection, List<DetectedObjectDto> objects) {
        try {
            log.info("Sending emergency notification for detection ID: {}", detection.getId());

            String dangerousObjects = objects.stream()
                    .filter(obj -> obj.getObjectType().equals("FIRE") || obj.getObjectType().equals("SMOKE"))
                    .map(DetectedObjectDto::getObjectType)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Unknown danger");

            String message = String.format("EMERGENCY: %s detected at camera %s in zone %s",
                    dangerousObjects,
                    detection.getCameraId(),
                    detection.getZoneId());

            NotificationRequest request = NotificationRequest.builder()
                    .title("EMERGENCY ALERT")
                    .message(message)
                    .severity("CRITICAL")
                    .analysisId(detection.getId())
                    .cameraId(detection.getCameraId())
                    .build();

            String url = notificationUrl + "/send";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully sent emergency notification");
        } catch (Exception e) {
            log.error("Failed to send emergency notification: {}", e.getMessage());
        }
    }

    // ===== NEW METHODS FOR FACE MATCH SERVICE =====

    /**
     * Notify Core Business Service with Face Match result
     */
    public void notifyFaceMatchResult(FaceMatch faceMatch) {
        try {
            log.info("Notifying Core Business Service for face match ID: {}", faceMatch.getId());

            CoreEventRequest request = CoreEventRequest.builder()
                    .analysisId(faceMatch.getId())
                    .eventType("FACE_MATCH_RESULT")
                    .status(faceMatch.getFaceMatched() ? "MATCHED" : "NOT_MATCHED")
                    .detectedObject(faceMatch.getPersonName())
                    .confidence(faceMatch.getMatchConfidence())
                    .timestamp(faceMatch.getMatchedAt().format(FORMATTER))
                    .build();

            String url = coreUrl + "/events";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully notified Core Business Service");
        } catch (Exception e) {
            log.error("Failed to notify Core Business Service: {}", e.getMessage());
        }
    }

    /**
     * Notify failed face match
     */
    public void notifyFailedFaceMatch(FaceMatch faceMatch) {
        try {
            log.info("Sending failed face match notification for match ID: {}", faceMatch.getId());

            String message = String.format("Failed to match face with confidence %.2f%% (threshold: %.2f%%). Person ID: %s",
                    faceMatch.getMatchConfidence() * 100,
                    faceMatch.getMatchThreshold() * 100,
                    faceMatch.getPersonId());

            NotificationRequest request = NotificationRequest.builder()
                    .title("Face Match Failed")
                    .message(message)
                    .severity("MEDIUM")
                    .analysisId(faceMatch.getId())
                    .cameraId(faceMatch.getDetection().getCameraId())
                    .build();

            String url = notificationUrl + "/send";
            restTemplate.postForObject(url, request, Object.class);
            
            log.info("Successfully sent failed face match notification");
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }
}
