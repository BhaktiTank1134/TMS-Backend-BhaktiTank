package com.bhakti.tms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidRequestDTO {

    @NotNull(message = "Load ID is required")
    private UUID loadId;

    @NotNull(message = "Transporter ID is required")
    private UUID transporterId;

    @Min(value = 1, message = "Proposed rate must be greater than 0")
    private double proposedRate;

    @Min(value = 1, message = "Trucks offered must be at least 1")
    private int trucksOffered;
}
