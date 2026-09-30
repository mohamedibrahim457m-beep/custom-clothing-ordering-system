package com.customclothing.system.controller;

import com.customclothing.system.dto.ApiResponse;
import com.customclothing.system.dto.MeasurementDTO;
import com.customclothing.system.service.MeasurementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementService measurementService;

    public MeasurementController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MeasurementDTO>> saveMeasurement(@Valid @RequestBody MeasurementDTO measurementDTO) {
        MeasurementDTO saved = measurementService.saveMeasurement(measurementDTO);
        return new ResponseEntity<>(ApiResponse.success("Measurement profile saved successfully", saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MeasurementDTO>> updateMeasurement(
            @PathVariable Long id,
            @Valid @RequestBody MeasurementDTO measurementDTO) {
        MeasurementDTO updated = measurementService.updateMeasurement(id, measurementDTO);
        return ResponseEntity.ok(ApiResponse.success("Measurement profile updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MeasurementDTO>> getMeasurementById(@PathVariable Long id) {
        MeasurementDTO measurement = measurementService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Measurement retrieved successfully", measurement));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<MeasurementDTO>>> getCustomerMeasurements(@PathVariable Long customerId) {
        List<MeasurementDTO> measurements = measurementService.findByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer measurements retrieved successfully", measurements));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMeasurement(@PathVariable Long id) {
        measurementService.deleteMeasurement(id);
        return ResponseEntity.ok(ApiResponse.success("Measurement profile deleted successfully"));
    }
}
