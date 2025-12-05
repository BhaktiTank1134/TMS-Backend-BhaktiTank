package com.bhakti.tms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Load {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID loadId;

    private String shipperId;
    private String loadingCity;
    private String unloadingCity;
    private Timestamp loadingDate;
    private String productType;
    private double weight;

    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;

    private String truckType;
    private int noOfTrucks;

    @Enumerated(EnumType.STRING)
    private LoadStatus status;

    private Timestamp datePosted;

    @OneToMany(mappedBy = "load")
    private List<Bid> bids;

    @OneToMany(mappedBy = "load")
    private List<Booking> bookings;

    @Version
    private Long version;
}
