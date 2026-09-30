package com.customclothing.system.dto;

import com.customclothing.system.entity.Measurement;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class MeasurementDTO {

    private Long id;
    private Long customerId;

    @NotBlank(message = "Profile name is required")
    private String profileName;

    private Double chest;
    private Double waist;
    private Double hips;
    private Double shoulderWidth;
    private Double sleeveLength;
    private Double shirtLength;
    private Double inseam;
    private Double neck;
    private String unit;
    private String notes;
    private LocalDateTime createdAt;

    public MeasurementDTO() {
    }

    public MeasurementDTO(Measurement measurement) {
        this.id = measurement.getId();
        if (measurement.getCustomer() != null) {
            this.customerId = measurement.getCustomer().getId();
        }
        this.profileName = measurement.getProfileName();
        this.chest = measurement.getChest();
        this.waist = measurement.getWaist();
        this.hips = measurement.getHips();
        this.shoulderWidth = measurement.getShoulderWidth();
        this.sleeveLength = measurement.getSleeveLength();
        this.shirtLength = measurement.getShirtLength();
        this.inseam = measurement.getInseam();
        this.neck = measurement.getNeck();
        this.unit = measurement.getUnit();
        this.notes = measurement.getNotes();
        this.createdAt = measurement.getCreatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Double getChest() {
        return chest;
    }

    public void setChest(Double chest) {
        this.chest = chest;
    }

    public Double getWaist() {
        return waist;
    }

    public void setWaist(Double waist) {
        this.waist = waist;
    }

    public Double getHips() {
        return hips;
    }

    public void setHips(Double hips) {
        this.hips = hips;
    }

    public Double getShoulderWidth() {
        return shoulderWidth;
    }

    public void setShoulderWidth(Double shoulderWidth) {
        this.shoulderWidth = shoulderWidth;
    }

    public Double getSleeveLength() {
        return sleeveLength;
    }

    public void setSleeveLength(Double sleeveLength) {
        this.sleeveLength = sleeveLength;
    }

    public Double getShirtLength() {
        return shirtLength;
    }

    public void setShirtLength(Double shirtLength) {
        this.shirtLength = shirtLength;
    }

    public Double getInseam() {
        return inseam;
    }

    public void setInseam(Double inseam) {
        this.inseam = inseam;
    }

    public Double getNeck() {
        return neck;
    }

    public void setNeck(Double neck) {
        this.neck = neck;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
