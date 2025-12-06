package com.bhakti.tms.dto;

import com.bhakti.tms.entity.LoadStatus;
import com.bhakti.tms.entity.WeightUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoadResponseDTO {

    private UUID loadId;
    private String shipperId;
    private String loadingCity;
    private String unloadingCity;
    private Timestamp loadingDate;
    private String productType;
    private double weight;
    private WeightUnit weightUnit;
    private String truckType;
    private int noOfTrucks;
    private LoadStatus status;
    private Timestamp datePosted;
}
