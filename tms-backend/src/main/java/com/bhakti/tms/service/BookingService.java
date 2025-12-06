package com.bhakti.tms.service;

import com.bhakti.tms.dto.BookingRequestDTO;
import com.bhakti.tms.dto.BookingResponseDTO;
import com.bhakti.tms.entity.*;
import com.bhakti.tms.exception.InsufficientCapacityException;
import com.bhakti.tms.exception.InvalidStatusTransitionException;
import com.bhakti.tms.exception.LoadAlreadyBookedException;
import com.bhakti.tms.exception.ResourceNotFoundException;
import com.bhakti.tms.repository.BidRepository;
import com.bhakti.tms.repository.BookingRepository;
import com.bhakti.tms.repository.LoadRepository;
import com.bhakti.tms.repository.TransporterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;

    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto) {
        try {
            // Fetch entities
            Load load = loadRepository.findById(dto.getLoadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

            Bid bid = bidRepository.findById(dto.getBidId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

            Transporter transporter = transporterRepository.findById(dto.getTransporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Transporter not found"));

            // Validate bid status
            if (bid.getStatus() != BidStatus.PENDING) {
                throw new InvalidStatusTransitionException("Bid must be in PENDING status to be accepted");
            }

            // Validate load status
            if (load.getStatus() == LoadStatus.CANCELLED) {
                throw new InvalidStatusTransitionException("Cannot accept bids for cancelled loads");
            }

            // Calculate remaining trucks
            int remainingTrucks = calculateRemainingTrucks(load);
            
            if (remainingTrucks == 0) {
                throw new InvalidStatusTransitionException("Load is already fully booked");
            }

            // Validate allocated trucks
            int allocatedTrucks = Math.min(dto.getAllocatedTrucks(), remainingTrucks);
            if (allocatedTrucks <= 0) {
                throw new InvalidStatusTransitionException("Invalid number of trucks to allocate");
            }

            // Validate and deduct capacity
            AvailableTruck availableTruck = transporter.getAvailableTrucks().stream()
                    .filter(truck -> truck.getTruckType().equalsIgnoreCase(load.getTruckType()))
                    .findFirst()
                    .orElseThrow(() -> new InsufficientCapacityException("Not enough trucks available to accept this bid"));

            if (availableTruck.getCount() < allocatedTrucks) {
                throw new InsufficientCapacityException("Not enough trucks available to accept this bid");
            }

            // Deduct trucks
            availableTruck.setCount(availableTruck.getCount() - allocatedTrucks);

            // Create booking
            Booking booking = Booking.builder()
                    .load(load)
                    .bid(bid)
                    .transporter(transporter)
                    .allocatedTrucks(allocatedTrucks)
                    .finalRate(bid.getProposedRate())
                    .status(BookingStatus.CONFIRMED)
                    .bookedAt(new Timestamp(System.currentTimeMillis()))
                    .build();

            // Update bid status
            bid.setStatus(BidStatus.ACCEPTED);

            // Update load status
            int newRemainingTrucks = remainingTrucks - allocatedTrucks;
            if (newRemainingTrucks == 0) {
                load.setStatus(LoadStatus.BOOKED);
            } else if (load.getStatus() == LoadStatus.POSTED) {
                load.setStatus(LoadStatus.OPEN_FOR_BIDS);
            }

            // Save all changes (optimistic locking will be checked here)
            bidRepository.save(bid);
            loadRepository.save(load);
            transporterRepository.save(transporter);
            Booking savedBooking = bookingRepository.save(booking);

            return toResponseDTO(savedBooking);

        } catch (OptimisticLockingFailureException | ObjectOptimisticLockingFailureException ex) {
            throw new LoadAlreadyBookedException("Concurrent update conflict — booking failed");
        }
    }

    public BookingResponseDTO getBooking(UUID bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return toResponseDTO(booking);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(UUID bookingId) {
        try {
            Booking booking = bookingRepository.findByBookingId(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new InvalidStatusTransitionException("Only CONFIRMED bookings can be cancelled");
            }

            // Restore trucks
            Transporter transporter = booking.getTransporter();
            AvailableTruck availableTruck = transporter.getAvailableTrucks().stream()
                    .filter(truck -> truck.getTruckType().equalsIgnoreCase(booking.getLoad().getTruckType()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Truck type not found"));

            availableTruck.setCount(availableTruck.getCount() + booking.getAllocatedTrucks());

            // Update booking status
            booking.setStatus(BookingStatus.CANCELLED);

            // Update load status if needed
            Load load = booking.getLoad();
            int remainingTrucks = calculateRemainingTrucks(load);
            
            if (load.getStatus() == LoadStatus.BOOKED && remainingTrucks > 0) {
                load.setStatus(LoadStatus.OPEN_FOR_BIDS);
            }

            // Save changes
            transporterRepository.save(transporter);
            loadRepository.save(load);
            Booking savedBooking = bookingRepository.save(booking);

            return toResponseDTO(savedBooking);

        } catch (OptimisticLockingFailureException | ObjectOptimisticLockingFailureException ex) {
            throw new LoadAlreadyBookedException("Concurrent update conflict — cancellation failed");
        }
    }

    private int calculateRemainingTrucks(Load load) {
        int allocatedTotal = bookingRepository.findByLoad_LoadId(load.getLoadId()).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED)
                .mapToInt(Booking::getAllocatedTrucks)
                .sum();
        return load.getNoOfTrucks() - allocatedTotal;
    }

    private BookingResponseDTO toResponseDTO(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .loadId(booking.getLoad().getLoadId())
                .bidId(booking.getBid().getBidId())
                .transporterId(booking.getTransporter().getTransporterId())
                .allocatedTrucks(booking.getAllocatedTrucks())
                .finalRate(booking.getFinalRate())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .build();
    }
}
