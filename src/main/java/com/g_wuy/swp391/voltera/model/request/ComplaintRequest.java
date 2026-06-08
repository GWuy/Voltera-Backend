package com.g_wuy.swp391.voltera.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintRequest {
    @NotBlank(message = "Problem title is required")
    @Size(max = 100, message = "Problem title cannot exceed 100 characters")
    private String problem;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String complaintType;
    private String priority;
}