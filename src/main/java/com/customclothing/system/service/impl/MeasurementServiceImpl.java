package com.customclothing.system.service.impl;

import com.customclothing.system.dto.MeasurementDTO;
import com.customclothing.system.entity.Measurement;
import com.customclothing.system.entity.User;
import com.customclothing.system.exception.ResourceNotFoundException;
import com.customclothing.system.repository.MeasurementRepository;
import com.customclothing.system.service.MeasurementService;
import com.customclothing.system.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MeasurementServiceImpl implements MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final UserService userService;

    public MeasurementServiceImpl(MeasurementRepository measurementRepository, UserService userService) {
        this.measurementRepository = measurementRepository;
        this.userService = userService;
    }

    @Override
    public MeasurementDTO saveMeasurement(MeasurementDTO dto) {
        User customer = userService.getUserEntity(dto.getCustomerId());

        Measurement measurement = new Measurement(
                customer,
                dto.getProfileName(),
                dto.getChest(),
                dto.getWaist(),
                dto.getHips(),
                dto.getShoulderWidth(),
                dto.getSleeveLength(),
                dto.getShirtLength(),
                dto.getInseam(),
                dto.getNeck(),
                dto.getUnit(),
                dto.getNotes()
        );

        Measurement saved = measurementRepository.save(measurement);
        return new MeasurementDTO(saved);
    }

    @Override
    public MeasurementDTO updateMeasurement(Long id, MeasurementDTO dto) {
        Measurement measurement = getMeasurementEntity(id);
        measurement.setProfileName(dto.getProfileName());
        measurement.setChest(dto.getChest());
        measurement.setWaist(dto.getWaist());
        measurement.setHips(dto.getHips());
        measurement.setShoulderWidth(dto.getShoulderWidth());
        measurement.setSleeveLength(dto.getSleeveLength());
        measurement.setShirtLength(dto.getShirtLength());
        measurement.setInseam(dto.getInseam());
        measurement.setNeck(dto.getNeck());
        if (dto.getUnit() != null) {
            measurement.setUnit(dto.getUnit());
        }
        measurement.setNotes(dto.getNotes());
        Measurement updated = measurementRepository.save(measurement);
        return new MeasurementDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MeasurementDTO findById(Long id) {
        return new MeasurementDTO(getMeasurementEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeasurementDTO> findByCustomerId(Long customerId) {
        return measurementRepository.findByCustomerId(customerId).stream()
                .map(MeasurementDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMeasurement(Long id) {
        Measurement measurement = getMeasurementEntity(id);
        measurementRepository.delete(measurement);
    }

    @Override
    @Transactional(readOnly = true)
    public Measurement getMeasurementEntity(Long id) {
        return measurementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", "id", id));
    }
}
