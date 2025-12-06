package com.bhakti.tms.repository;

import com.bhakti.tms.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingId(UUID bookingId);

    List<Booking> findByLoad_LoadId(UUID loadId);
}
