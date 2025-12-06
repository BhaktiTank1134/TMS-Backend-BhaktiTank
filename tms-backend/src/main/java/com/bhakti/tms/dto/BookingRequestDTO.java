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
public class BookingRequestDTO {

    @NotNull(message = "Load ID is required")
    private UUID loadId;

    @NotNull(message = "Bid ID is required")
    private UUID bidId;

    @NotNull(message = "Transporter ID is required")
    private UUID transporterId;

    @Min(value = 1, message = "Allocated trucks must be at least 1")
    private int allocatedTrucks;
}
