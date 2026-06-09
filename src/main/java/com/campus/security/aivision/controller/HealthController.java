package com.campus.security.aivision.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service and database are running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "AI Vision Service");
        response.put("version", "1.0.0");
        response.put("timestamp", LocalDateTime.now());
        
        // Check database connection
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            response.put("database", "UP");
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            response.put("database", "DOWN");
            response.put("status", "DEGRADED");
        }
        
        return ResponseEntity.ok(response);
    }
}
