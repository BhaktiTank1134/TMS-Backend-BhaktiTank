package com.bhakti.tms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transporters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transporter {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID transporterId;

    private String companyName;
    private double rating;

    @OneToMany(mappedBy = "transporter", cascade = CascadeType.ALL)
    private List<AvailableTruck> availableTrucks;

    @OneToMany(mappedBy = "transporter")
    private List<Bid> bids;

    @OneToMany(mappedBy = "transporter")
    private List<Booking> bookings;
}
