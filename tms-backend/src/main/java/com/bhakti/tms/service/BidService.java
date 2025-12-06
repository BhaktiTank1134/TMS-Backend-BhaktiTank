package com.bhakti.tms.service;

import com.bhakti.tms.dto.BidRequestDTO;
import com.bhakti.tms.dto.BidResponseDTO;
import com.bhakti.tms.entity.*;
import com.bhakti.tms.exception.InsufficientCapacityException;
import com.bhakti.tms.exception.InvalidStatusTransitionException;
import com.bhakti.tms.exception.ResourceNotFoundException;
import com.bhakti.tms.repository.BidRepository;
import com.bhakti.tms.repository.LoadRepository;
import com.bhakti.tms.repository.TransporterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final LoadRepository loadRepository;
    private final TransporterRepository transporterRepository;

    @Transactional
    public BidResponseDTO submitBid(BidRequestDTO dto) {
        Load load = loadRepository.findById(dto.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        Transporter transporter = transporterRepository.findById(dto.getTransporterId())
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found"));

        // Validate load status
        if (load.getStatus() == LoadStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot bid on cancelled load");
        }
        if (load.getStatus() == LoadStatus.BOOKED) {
            throw new InvalidStatusTransitionException("Cannot bid on booked load");
        }

        // Validate capacity
        validateCapacity(transporter, load.getTruckType(), dto.getTrucksOffered());

        // Create bid
        Bid bid = Bid.builder()
                .proposedRate(dto.getProposedRate())
                .trucksOffered(dto.getTrucksOffered())
                .status(BidStatus.PENDING)
                .submittedAt(new Timestamp(System.currentTimeMillis()))
                .load(load)
                .transporter(transporter)
                .build();

        Bid savedBid = bidRepository.save(bid);

        // Update load status if first bid
        if (load.getStatus() == LoadStatus.POSTED) {
            load.setStatus(LoadStatus.OPEN_FOR_BIDS);
            loadRepository.save(load);
        }

        return toResponseDTO(savedBid);
    }

    public List<BidResponseDTO> getBids(UUID loadId, UUID transporterId, BidStatus status) {
        List<Bid> bids;

        if (loadId != null) {
            bids = bidRepository.findByLoad_LoadId(loadId);
        } else if (transporterId != null) {
            bids = bidRepository.findByTransporter_TransporterId(transporterId);
        } else if (status != null) {
            bids = bidRepository.findByStatus(status);
        } else {
            bids = bidRepository.findAll();
        }

        return bids.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public BidResponseDTO getBidById(UUID bidId) {
        Bid bid = bidRepository.findByBidId(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));
        return toResponseDTO(bid);
    }

    @Transactional
    public BidResponseDTO rejectBid(UUID bidId) {
        Bid bid = bidRepository.findByBidId(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found"));

        bid.setStatus(BidStatus.REJECTED);
        Bid updatedBid = bidRepository.save(bid);
        return toResponseDTO(updatedBid);
    }

    private void validateCapacity(Transporter transporter, String requiredTruckType, int trucksOffered) {
        if (transporter.getAvailableTrucks() == null || transporter.getAvailableTrucks().isEmpty()) {
            throw new InsufficientCapacityException("Not enough trucks available");
        }

        int availableCount = transporter.getAvailableTrucks().stream()
                .filter(truck -> truck.getTruckType().equalsIgnoreCase(requiredTruckType))
                .mapToInt(AvailableTruck::getCount)
                .sum();

        if (availableCount < trucksOffered) {
            throw new InsufficientCapacityException("Not enough trucks available");
        }
    }

    private BidResponseDTO toResponseDTO(Bid bid) {
        return BidResponseDTO.builder()
                .bidId(bid.getBidId())
                .loadId(bid.getLoad().getLoadId())
                .transporterId(bid.getTransporter().getTransporterId())
                .proposedRate(bid.getProposedRate())
                .trucksOffered(bid.getTrucksOffered())
                .status(bid.getStatus())
                .submittedAt(bid.getSubmittedAt())
                .build();
    }
}
