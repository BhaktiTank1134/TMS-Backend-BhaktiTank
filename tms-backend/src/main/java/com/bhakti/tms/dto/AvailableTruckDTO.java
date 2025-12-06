package com.bhakti.tms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableTruckDTO {

    @NotBlank(message = "Truck type is required")
    private String truckType;

    @Min(value = 1, message = "Count must be at least 1")
    private int count;
}
