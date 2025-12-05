package com.bhakti.tms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID bookingId;

    private int allocatedTrucks;
    private double finalRate;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Timestamp bookedAt;

    @ManyToOne
    @JoinColumn(name = "load_id")
    private Load load;

    @ManyToOne
    @JoinColumn(name = "transporter_id")
    private Transporter transporter;

    @OneToOne
    @JoinColumn(name = "bid_id")
    private Bid bid;
}
