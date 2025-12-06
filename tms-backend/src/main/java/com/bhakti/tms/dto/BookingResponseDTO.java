package com.bhakti.tms.dto;

import com.bhakti.tms.entity.BookingStatus;
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
public class BookingResponseDTO {

    private UUID bookingId;
    private UUID loadId;
    private UUID bidId;
    private UUID transporterId;
    private int allocatedTrucks;
    private double finalRate;
    private BookingStatus status;
    private Timestamp bookedAt;
}
