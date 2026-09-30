package com.customclothing.system.controller;

import com.customclothing.system.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> status = Map.of(
                "status", "UP",
                "application", "Custom Clothing Ordering System Backend",
                "version", "1.0.0"
        );
        return ResponseEntity.ok(ApiResponse.success("Service is running smoothly", status));
    }

    @GetMapping("/welcome")
    public ResponseEntity<ApiResponse<String>> welcome() {
        return ResponseEntity.ok(ApiResponse.success("Welcome to Custom Clothing Ordering System API"));
    }
}
