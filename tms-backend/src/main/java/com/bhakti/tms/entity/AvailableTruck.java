package com.bhakti.tms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "available_trucks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTruck {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String truckType;
    private int count;

    @ManyToOne
    @JoinColumn(name = "transporter_id")
    private Transporter transporter;
}
