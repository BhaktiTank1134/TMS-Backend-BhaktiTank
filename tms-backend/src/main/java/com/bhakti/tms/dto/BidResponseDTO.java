package com.bhakti.tms.dto;

import com.bhakti.tms.entity.BidStatus;
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
public class BidResponseDTO {

    private UUID bidId;
    private UUID loadId;
    private UUID transporterId;
    private double proposedRate;
    private int trucksOffered;
    private BidStatus status;
    private Timestamp submittedAt;
}
