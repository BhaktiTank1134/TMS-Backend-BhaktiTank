package com.bhakti.tms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransporterResponseDTO {

    private UUID transporterId;
    private String companyName;
    private double rating;
    private List<AvailableTruckDTO> availableTrucks;
}
