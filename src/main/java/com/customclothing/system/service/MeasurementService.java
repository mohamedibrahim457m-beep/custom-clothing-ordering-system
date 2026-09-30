package com.customclothing.system.service;

import com.customclothing.system.dto.MeasurementDTO;
import com.customclothing.system.entity.Measurement;

import java.util.List;

public interface MeasurementService {

    MeasurementDTO saveMeasurement(MeasurementDTO measurementDTO);

    MeasurementDTO updateMeasurement(Long id, MeasurementDTO measurementDTO);

    MeasurementDTO findById(Long id);

    List<MeasurementDTO> findByCustomerId(Long customerId);

    void deleteMeasurement(Long id);

    Measurement getMeasurementEntity(Long id);
}
