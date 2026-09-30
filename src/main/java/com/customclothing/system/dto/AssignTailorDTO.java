package com.customclothing.system.dto;

import jakarta.validation.constraints.NotNull;

public class AssignTailorDTO {

    @NotNull(message = "Tailor ID is required")
    private Long tailorId;

    public AssignTailorDTO() {
    }

    public AssignTailorDTO(Long tailorId) {
        this.tailorId = tailorId;
    }

    public Long getTailorId() {
        return tailorId;
    }

    public void setTailorId(Long tailorId) {
        this.tailorId = tailorId;
    }
}
