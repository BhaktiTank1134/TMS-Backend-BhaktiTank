package com.bhakti.tms.controller;

import com.bhakti.tms.dto.BidRequestDTO;
import com.bhakti.tms.dto.BidResponseDTO;
import com.bhakti.tms.entity.BidStatus;
import com.bhakti.tms.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bid")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<BidResponseDTO> submitBid(@Valid @RequestBody BidRequestDTO bidRequestDTO) {
        BidResponseDTO response = bidService.submitBid(bidRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BidResponseDTO>> getBids(
            @RequestParam(required = false) UUID loadId,
            @RequestParam(required = false) UUID transporterId,
            @RequestParam(required = false) BidStatus status) {
        List<BidResponseDTO> bids = bidService.getBids(loadId, transporterId, status);
        return ResponseEntity.ok(bids);
    }

    @GetMapping("/{bidId}")
    public ResponseEntity<BidResponseDTO> getBidById(@PathVariable UUID bidId) {
        BidResponseDTO bid = bidService.getBidById(bidId);
        return ResponseEntity.ok(bid);
    }

    @PatchMapping("/{bidId}/reject")
    public ResponseEntity<BidResponseDTO> rejectBid(@PathVariable UUID bidId) {
        BidResponseDTO rejectedBid = bidService.rejectBid(bidId);
        return ResponseEntity.ok(rejectedBid);
    }
}
