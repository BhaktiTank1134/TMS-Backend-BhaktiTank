package com.bhakti.tms.dto;

import com.bhakti.tms.entity.WeightUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadRequestDTO {

    @NotBlank(message = "Shipper ID is required")
    private String shipperId;

    @NotBlank(message = "Loading city is required")
    private String loadingCity;

    @NotBlank(message = "Unloading city is required")
    private String unloadingCity;

    @NotNull(message = "Loading date is required")
    private Timestamp loadingDate;

    @NotBlank(message = "Product type is required")
    private String productType;

    @Min(value = 1, message = "Weight must be greater than 0")
    private double weight;

    @NotNull(message = "Weight unit is required")
    private WeightUnit weightUnit;

    @NotBlank(message = "Truck type is required")
    private String truckType;

    @Min(value = 1, message = "Number of trucks must be at least 1")
    private int noOfTrucks;
}
