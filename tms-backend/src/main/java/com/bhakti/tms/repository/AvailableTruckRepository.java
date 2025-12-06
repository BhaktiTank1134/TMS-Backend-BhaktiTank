package com.bhakti.tms.repository;

import com.bhakti.tms.entity.AvailableTruck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AvailableTruckRepository extends JpaRepository<AvailableTruck, UUID> {

    List<AvailableTruck> findByTransporter_TransporterId(UUID transporterId);
}
