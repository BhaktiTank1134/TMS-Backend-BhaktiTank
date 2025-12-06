package com.bhakti.tms.service;

import com.bhakti.tms.dto.BookingRequestDTO;
import com.bhakti.tms.dto.BookingResponseDTO;
import com.bhakti.tms.entity.*;
import com.bhakti.tms.exception.InsufficientCapacityException;
import com.bhakti.tms.exception.LoadAlreadyBookedException;
import com.bhakti.tms.repository.BidRepository;
import com.bhakti.tms.repository.BookingRepository;
import com.bhakti.tms.repository.LoadRepository;
import com.bhakti.tms.repository.TransporterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private LoadRepository loadRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @InjectMocks
    private BookingService bookingService;

    private Load load;
    private Bid bid;
    private Transporter transporter;
    private AvailableTruck availableTruck;
    private BookingRequestDTO bookingRequestDTO;

    @BeforeEach
    void setUp() {
        UUID loadId = UUID.randomUUID();
        UUID bidId = UUID.randomUUID();
        UUID transporterId = UUID.randomUUID();

        load = Load.builder()
                .loadId(loadId)
                .truckType("Container")
                .noOfTrucks(10)
                .status(LoadStatus.OPEN_FOR_BIDS)
                .build();

        availableTruck = AvailableTruck.builder()
                .id(UUID.randomUUID())
                .truckType("Container")
                .count(15)
                .build();

        List<AvailableTruck> trucks = new ArrayList<>();
        trucks.add(availableTruck);

        transporter = Transporter.builder()
                .transporterId(transporterId)
                .companyName("Test Transport")
                .rating(4.5)
                .availableTrucks(trucks)
                .build();

        availableTruck.setTransporter(transporter);

        bid = Bid.builder()
                .bidId(bidId)
                .load(load)
                .transporter(transporter)
                .proposedRate(5000.0)
                .trucksOffered(5)
                .status(BidStatus.PENDING)
                .build();

        bookingRequestDTO = BookingRequestDTO.builder()
                .loadId(loadId)
                .bidId(bidId)
                .transporterId(transporterId)
                .allocatedTrucks(5)
                .build();
    }

    @Test
    void testSuccessfulBookingReducesAvailableTrucks() {
        // Arrange
        when(loadRepository.findById(any())).thenReturn(Optional.of(load));
        when(bidRepository.findById(any())).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(any())).thenReturn(Optional.of(transporter));
        when(bookingRepository.findByLoad_LoadId(any())).thenReturn(new ArrayList<>());
        
        Booking savedBooking = Booking.builder()
                .bookingId(UUID.randomUUID())
                .load(load)
                .bid(bid)
                .transporter(transporter)
                .allocatedTrucks(5)
                .finalRate(5000.0)
                .status(BookingStatus.CONFIRMED)
                .bookedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        
        when(bookingRepository.save(any())).thenReturn(savedBooking);

        // Act
        BookingResponseDTO response = bookingService.createBooking(bookingRequestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getAllocatedTrucks());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(10, availableTruck.getCount()); // 15 - 5 = 10
        verify(bookingRepository, times(1)).save(any());
        verify(transporterRepository, times(1)).save(any());
    }

    @Test
    void testConcurrentBookingThrowsLoadAlreadyBookedException() {
        // Arrange
        when(loadRepository.findById(any())).thenReturn(Optional.of(load));
        when(bidRepository.findById(any())).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(any())).thenReturn(Optional.of(transporter));
        when(bookingRepository.findByLoad_LoadId(any())).thenReturn(new ArrayList<>());
        when(loadRepository.save(any())).thenThrow(new OptimisticLockingFailureException("Version mismatch"));

        // Act & Assert
        assertThrows(LoadAlreadyBookedException.class, () -> {
            bookingService.createBooking(bookingRequestDTO);
        });
    }

    @Test
    void testCancelBookingRestoresTrucksAndUpdatesLoadStatus() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        availableTruck.setCount(10); // Already reduced
        
        Booking booking = Booking.builder()
                .bookingId(bookingId)
                .load(load)
                .bid(bid)
                .transporter(transporter)
                .allocatedTrucks(5)
                .finalRate(5000.0)
                .status(BookingStatus.CONFIRMED)
                .bookedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        load.setStatus(LoadStatus.BOOKED);

        when(bookingRepository.findByBookingId(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.findByLoad_LoadId(any())).thenReturn(List.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        // Act
        BookingResponseDTO response = bookingService.cancelBooking(bookingId);

        // Assert
        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(15, availableTruck.getCount()); // 10 + 5 = 15 restored
        assertEquals(LoadStatus.OPEN_FOR_BIDS, load.getStatus()); // Changed from BOOKED
        verify(transporterRepository, times(1)).save(any());
        verify(loadRepository, times(1)).save(any());
    }

    @Test
    void testInsufficientCapacityThrowsException() {
        // Arrange
        availableTruck.setCount(3); // Less than requested 5
        when(loadRepository.findById(any())).thenReturn(Optional.of(load));
        when(bidRepository.findById(any())).thenReturn(Optional.of(bid));
        when(transporterRepository.findById(any())).thenReturn(Optional.of(transporter));
        when(bookingRepository.findByLoad_LoadId(any())).thenReturn(new ArrayList<>());

        // Act & Assert
        assertThrows(InsufficientCapacityException.class, () -> {
            bookingService.createBooking(bookingRequestDTO);
        });
    }
}
