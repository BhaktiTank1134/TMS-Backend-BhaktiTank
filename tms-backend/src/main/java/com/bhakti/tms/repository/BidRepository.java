package com.bhakti.tms.repository;

import com.bhakti.tms.entity.Bid;
import com.bhakti.tms.entity.BidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {

    List<Bid> findByLoad_LoadId(UUID loadId);

    List<Bid> findByTransporter_TransporterId(UUID transporterId);

    List<Bid> findByStatus(BidStatus status);

    Optional<Bid> findByBidId(UUID bidId);
}
