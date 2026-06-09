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
 * Service for face matching operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FaceMatchService {

    private final FaceMatchRepository faceMatchRepository;
    private final FaceSuggestionRepository faceSuggestionRepository;
    private final DetectionRepository detectionRepository;
    private final ProcessingLogRepository processingLogRepository;
    private final ExternalServiceClient externalServiceClient;

    private final Random random = new Random();

    // Sample person database for simulation
    private final List<String> samplePersons = Arrays.asList(
            "PERSON_001:John Doe",
            "PERSON_002:Jane Smith",
            "PERSON_003:Michael Johnson",
            "PERSON_004:Emily Davis",
            "PERSON_005:Unknown Person"
    );

    /**
     * Match face from Core Business Service (B6)
     */
    @Transactional
    public FaceMatchResponse matchFace(FaceMatchRequest request) {
        log.info("Starting face matching for detection ID: {}", request.getDetectionId());
        
        String requestId = UUID.randomUUID().toString();
        createLog(requestId, "FACE_MATCH_STARTED", ProcessingLog.LogStatus.STARTED,
                 "Face matching started for detection: " + request.getDetectionId());

        try {
            // Verify detection exists
            Detection detection = detectionRepository.findById(request.getDetectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Detection not found with ID: " + request.getDetectionId()));

            // Perform face matching (simulated)
            Map<String, Object> matchResult = performFaceMatching(request);

            Boolean faceMatched = (Boolean) matchResult.get("matched");
            Double confidence = (Double) matchResult.get("confidence");
            String matchedPersonId = (String) matchResult.get("personId");
            String matchedPersonName = (String) matchResult.get("personName");

            // Create FaceMatch entity
            FaceMatch faceMatch = FaceMatch.builder()
                    .detection(detection)
                    .personId(matchedPersonId)
                    .personName(matchedPersonName)
                    .matchConfidence(confidence)
                    .faceMatched(faceMatched)
                    .matchThreshold(request.getMatchThreshold())
                    .matchedAt(LocalDateTime.now())
                    .build();

            faceMatch = faceMatchRepository.save(faceMatch);
            createLog(requestId, "FACE_MATCH_SAVED", ProcessingLog.LogStatus.IN_PROGRESS,
                     "Face match result saved with ID: " + faceMatch.getId());

            List<FaceSuggestionDto> suggestions = new ArrayList<>();

            // If confidence is low, generate suggestions
            if (!faceMatched || confidence < request.getMatchThreshold() + 0.05) {
                suggestions = generateFaceSuggestions(faceMatch, request.getMatchThreshold());
            }

            String status = determineMatchStatus(faceMatched, confidence, request.getMatchThreshold());

            createLog(requestId, "FACE_MATCH_COMPLETED", ProcessingLog.LogStatus.SUCCESS,
                     String.format("Face match completed with status: %s", status));

            // Notify other services
            notifyOtherServices(faceMatch);

            return FaceMatchResponse.builder()
                    .matchId(faceMatch.getId())
                    .detectionId(detection.getId())
                    .faceMatched(faceMatched)
                    .personId(matchedPersonId)
                    .personName(matchedPersonName)
                    .matchConfidence(confidence)
                    .matchThreshold(request.getMatchThreshold())
                    .status(status)
                    .suggestions(suggestions)
                    .matchedAt(faceMatch.getMatchedAt())
                    .message(faceMatched ? "Face matched successfully" : "Face not matched - check suggestions")
                    .build();

        } catch (Exception e) {
            log.error("Error during face matching: {}", e.getMessage(), e);
            createLog(requestId, "FACE_MATCH_FAILED", ProcessingLog.LogStatus.FAILED,
                     "Error: " + e.getMessage());
            throw new RuntimeException("Failed to match face: " + e.getMessage(), e);
        }
    }

    /**
     * Get face match by ID
     */
    public FaceMatchResponse getFaceMatchById(Long matchId) {
        log.info("Fetching face match with ID: {}", matchId);
        
        FaceMatch faceMatch = faceMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Face match not found with ID: " + matchId));

        List<FaceSuggestionDto> suggestions = faceSuggestionRepository.findByFaceMatchId(matchId)
                .stream()
                .map(this::mapSuggestionToDto)
                .collect(Collectors.toList());

        String status = determineMatchStatus(faceMatch.getFaceMatched(), 
                faceMatch.getMatchConfidence(), faceMatch.getMatchThreshold());

        return FaceMatchResponse.builder()
                .matchId(faceMatch.getId())
                .detectionId(faceMatch.getDetection().getId())
                .faceMatched(faceMatch.getFaceMatched())
                .personId(faceMatch.getPersonId())
                .personName(faceMatch.getPersonName())
                .matchConfidence(faceMatch.getMatchConfidence())
                .matchThreshold(faceMatch.getMatchThreshold())
                .status(status)
                .suggestions(suggestions)
                .matchedAt(faceMatch.getMatchedAt())
                .message("Face match retrieved successfully")
                .build();
    }

    /**
     * Get all face matches
     */
    public List<FaceMatchResponse> getAllFaceMatches() {
        log.info("Fetching all face matches");
        
        return faceMatchRepository.findAll().stream()
                .map(faceMatch -> {
                    List<FaceSuggestionDto> suggestions = faceSuggestionRepository.findByFaceMatchId(faceMatch.getId())
                            .stream()
                            .map(this::mapSuggestionToDto)
                            .collect(Collectors.toList());

                    String status = determineMatchStatus(faceMatch.getFaceMatched(),
                            faceMatch.getMatchConfidence(), faceMatch.getMatchThreshold());

                    return FaceMatchResponse.builder()
                            .matchId(faceMatch.getId())
                            .detectionId(faceMatch.getDetection().getId())
                            .faceMatched(faceMatch.getFaceMatched())
                            .personId(faceMatch.getPersonId())
                            .personName(faceMatch.getPersonName())
                            .matchConfidence(faceMatch.getMatchConfidence())
                            .matchThreshold(faceMatch.getMatchThreshold())
                            .status(status)
                            .suggestions(suggestions)
                            .matchedAt(faceMatch.getMatchedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Perform face matching (simulated)
     */
    private Map<String, Object> performFaceMatching(FaceMatchRequest request) {
        log.debug("Performing face matching");

        // Simulate processing time
        try {
            Thread.sleep(150 + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Random confidence score
        double confidence = 0.70 + (random.nextDouble() * 0.29); // 0.70 - 0.99
        confidence = Math.round(confidence * 100.0) / 100.0;

        boolean matched = confidence >= request.getMatchThreshold();

        // Select a random person
        String personData = samplePersons.get(random.nextInt(samplePersons.size()));
        String[] parts = personData.split(":");
        String personId = parts[0];
        String personName = parts[1];

        Map<String, Object> result = new HashMap<>();
        result.put("matched", matched);
        result.put("confidence", confidence);
        result.put("personId", personId);
        result.put("personName", personName);

        log.debug("Face matching result: matched={}, confidence={}, person={}", matched, confidence, personName);
        return result;
    }

    /**
     * Generate face suggestions for low confidence matches
     */
    private List<FaceSuggestionDto> generateFaceSuggestions(FaceMatch faceMatch, Double threshold) {
        log.debug("Generating face suggestions for match ID: {}", faceMatch.getId());

        List<FaceSuggestionDto> suggestions = new ArrayList<>();
        int numSuggestions = 2 + random.nextInt(3); // 2-4 suggestions

        for (int i = 0; i < numSuggestions; i++) {
            String personData = samplePersons.get(random.nextInt(samplePersons.size()));
            String[] parts = personData.split(":");
            String personId = parts[0];
            String personName = parts[1];

            double suggestionConfidence = threshold - 0.10 + (random.nextDouble() * 0.15);
            suggestionConfidence = Math.round(suggestionConfidence * 100.0) / 100.0;

            FaceSuggestion suggestion = FaceSuggestion.builder()
                    .faceMatch(faceMatch)
                    .suggestedPersonId(personId)
                    .suggestedPersonName(personName)
                    .suggestionConfidence(suggestionConfidence)
                    .status(FaceSuggestion.SuggestionStatus.PENDING)
                    .build();

            suggestion = faceSuggestionRepository.save(suggestion);
            suggestions.add(mapSuggestionToDto(suggestion));
        }

        return suggestions;
    }

    /**
     * Determine match status
     */
    private String determineMatchStatus(Boolean matched, Double confidence, Double threshold) {
        if (matched && confidence >= threshold + 0.10) {
            return "HIGH_CONFIDENCE_MATCH";
        } else if (matched) {
            return "MATCHED";
        } else if (confidence >= threshold - 0.05) {
            return "LOW_CONFIDENCE";
        } else {
            return "NOT_MATCHED";
        }
    }

    /**
     * Notify other services
     */
    private void notifyOtherServices(FaceMatch faceMatch) {
        try {
            // Notify Core Business Service (B6)
            externalServiceClient.notifyFaceMatchResult(faceMatch);

            // Notify Notification Service (B7) for failed matches
            if (!faceMatch.getFaceMatched()) {
                externalServiceClient.notifyFailedFaceMatch(faceMatch);
            }

        } catch (Exception e) {
            log.error("Error notifying external services: {}", e.getMessage(), e);
        }
    }

    /**
     * Map suggestion entity to DTO
     */
    private FaceSuggestionDto mapSuggestionToDto(FaceSuggestion suggestion) {
        return FaceSuggestionDto.builder()
                .id(suggestion.getId())
                .suggestedPersonId(suggestion.getSuggestedPersonId())
                .suggestedPersonName(suggestion.getSuggestedPersonName())
                .suggestionConfidence(suggestion.getSuggestionConfidence())
                .status(suggestion.getStatus().name())
                .build();
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
