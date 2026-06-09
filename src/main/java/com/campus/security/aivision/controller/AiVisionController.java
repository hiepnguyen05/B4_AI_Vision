package com.campus.security.aivision.controller;

import com.campus.security.aivision.dto.*;
import com.campus.security.aivision.service.AiVisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for AI Vision Service
 * Product B4 - Campus Security System
 */
@RestController
@RequestMapping("/vision")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Vision", description = "AI Vision Service APIs for image analysis and object detection")
public class AiVisionController {

    private final AiVisionService aiVisionService;
    private final com.campus.security.aivision.service.DetectionService detectionService;
    private final com.campus.security.aivision.service.FaceMatchService faceMatchService;

    @PostMapping("/analyze")
    @Operation(
        summary = "Analyze image from Camera Stream Service",
        description = "Receives image URL from Camera Stream Service (B2), performs AI analysis, and returns detected objects"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image analyzed successfully",
                    content = @Content(schema = @Schema(implementation = AnalyzeImageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnalyzeImageResponse> analyzeImage(@Valid @RequestBody AnalyzeImageRequest request) {
        log.info("Received analyze request from camera: {}", request.getCameraId());
        AnalyzeImageResponse response = aiVisionService.analyzeImage(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    @Operation(
        summary = "Upload and analyze image",
        description = "Upload an image directly and perform AI analysis"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Image uploaded and analyzed successfully",
                    content = @Content(schema = @Schema(implementation = AnalyzeImageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AnalyzeImageResponse> uploadImage(@Valid @RequestBody UploadImageRequest request) {
        log.info("Received upload request for image: {}", request.getImageName());
        AnalyzeImageResponse response = aiVisionService.uploadAndAnalyze(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/results")
    @Operation(
        summary = "Get all analysis results",
        description = "Retrieve all image analysis results from the database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AiResultResponse.class)))
    })
    public ResponseEntity<List<AiResultResponse>> getAllResults() {
        log.info("Fetching all analysis results");
        List<AiResultResponse> results = aiVisionService.getAllResults();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/results/{id}")
    @Operation(
        summary = "Get analysis result by ID",
        description = "Retrieve a specific analysis result by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Result retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AiResultResponse.class))),
        @ApiResponse(responseCode = "404", description = "Result not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AiResultResponse> getResultById(@PathVariable Long id) {
        log.info("Fetching result with ID: {}", id);
        AiResultResponse result = aiVisionService.getResultById(id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/results/{id}")
    @Operation(
        summary = "Delete analysis result",
        description = "Delete a specific analysis result by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Result deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Result not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteResult(@PathVariable Long id) {
        log.info("Deleting result with ID: {}", id);
        aiVisionService.deleteResult(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Operation(
        summary = "Get analysis statistics",
        description = "Retrieve statistical information about all analyses"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StatisticsResponse.class)))
    })
    public ResponseEntity<StatisticsResponse> getStatistics() {
        log.info("Fetching analysis statistics");
        StatisticsResponse statistics = aiVisionService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ===== NEW DETECTION ENDPOINTS =====

    @PostMapping("/detect")
    @Operation(
        summary = "Detect objects in image (New API)",
        description = "Receives image from Camera Stream Service (B2), detects multiple objects with bounding boxes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Objects detected successfully",
                    content = @Content(schema = @Schema(implementation = DetectionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DetectionResponse> detectObjects(@Valid @RequestBody AnalyzeImageRequest request) {
        log.info("Received detect request from camera: {}", request.getCameraId());
        DetectionResponse response = detectionService.detectObjects(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detections/{detectionId}")
    @Operation(
        summary = "Get detection by ID",
        description = "Retrieve a specific detection result with all detected objects"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detection retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DetectionResponse.class))),
        @ApiResponse(responseCode = "404", description = "Detection not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DetectionResponse> getDetectionById(@PathVariable Long detectionId) {
        log.info("Fetching detection with ID: {}", detectionId);
        DetectionResponse response = detectionService.getDetectionById(detectionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detections")
    @Operation(
        summary = "Get all detections",
        description = "Retrieve all detection results"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detections retrieved successfully")
    })
    public ResponseEntity<List<DetectionResponse>> getAllDetections() {
        log.info("Fetching all detections");
        List<DetectionResponse> detections = detectionService.getAllDetections();
        return ResponseEntity.ok(detections);
    }

    // ===== FACE MATCHING ENDPOINTS =====

    @PostMapping("/face-match")
    @Operation(
        summary = "Match face from Core Business Service",
        description = "Receives face matching request from Core Business Service (B6), performs face recognition"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Face matching completed",
                    content = @Content(schema = @Schema(implementation = FaceMatchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FaceMatchResponse> matchFace(@Valid @RequestBody FaceMatchRequest request) {
        log.info("Received face match request for detection ID: {}", request.getDetectionId());
        FaceMatchResponse response = faceMatchService.matchFace(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/face-matches/{matchId}")
    @Operation(
        summary = "Get face match by ID",
        description = "Retrieve a specific face match result"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Face match retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FaceMatchResponse.class))),
        @ApiResponse(responseCode = "404", description = "Face match not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FaceMatchResponse> getFaceMatchById(@PathVariable Long matchId) {
        log.info("Fetching face match with ID: {}", matchId);
        FaceMatchResponse response = faceMatchService.getFaceMatchById(matchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/face-matches")
    @Operation(
        summary = "Get all face matches",
        description = "Retrieve all face match results"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Face matches retrieved successfully")
    })
    public ResponseEntity<List<FaceMatchResponse>> getAllFaceMatches() {
        log.info("Fetching all face matches");
        List<FaceMatchResponse> matches = faceMatchService.getAllFaceMatches();
        return ResponseEntity.ok(matches);
    }
}
