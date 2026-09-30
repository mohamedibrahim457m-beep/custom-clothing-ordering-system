package com.customclothing.system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Measurement entity storing custom body dimensions submitted by customers.
 * Tailors view these measurements when crafting custom garments.
 */
@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotBlank(message = "Measurement profile name is required (e.g., 'My Formal Fit')")
    @Column(name = "profile_name", nullable = false, length = 100)
    private String profileName;

    private Double chest;
    private Double waist;
    private Double hips;

    @Column(name = "shoulder_width")
    private Double shoulderWidth;

    @Column(name = "sleeve_length")
    private Double sleeveLength;

    @Column(name = "shirt_length")
    private Double shirtLength;

    private Double inseam;
    private Double neck;

    @Column(length = 10)
    private String unit = "inches"; // inches or cm

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Measurement() {
    }

    public Measurement(User customer, String profileName, Double chest, Double waist, Double hips,
                       Double shoulderWidth, Double sleeveLength, Double shirtLength,
                       Double inseam, Double neck, String unit, String notes) {
        this.customer = customer;
        this.profileName = profileName;
        this.chest = chest;
        this.waist = waist;
        this.hips = hips;
        this.shoulderWidth = shoulderWidth;
        this.sleeveLength = sleeveLength;
        this.shirtLength = shirtLength;
        this.inseam = inseam;
        this.neck = neck;
        this.unit = unit != null ? unit : "inches";
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
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
