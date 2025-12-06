package com.bhakti.tms.controller;

import com.bhakti.tms.dto.AvailableTruckDTO;
import com.bhakti.tms.dto.TransporterRequestDTO;
import com.bhakti.tms.dto.TransporterResponseDTO;
import com.bhakti.tms.service.TransporterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transporter")
@RequiredArgsConstructor
public class TransporterController {

    private final TransporterService transporterService;

    @PostMapping
    public ResponseEntity<TransporterResponseDTO> registerTransporter(
            @Valid @RequestBody TransporterRequestDTO requestDTO) {
        TransporterResponseDTO response = transporterService.registerTransporter(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{transporterId}")
    public ResponseEntity<TransporterResponseDTO> getTransporter(@PathVariable UUID transporterId) {
        TransporterResponseDTO response = transporterService.getTransporter(transporterId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{transporterId}/trucks")
    public ResponseEntity<TransporterResponseDTO> updateAvailableTrucks(
            @PathVariable UUID transporterId,
            @Valid @RequestBody List<AvailableTruckDTO> trucks) {
        TransporterResponseDTO response = transporterService.updateAvailableTrucks(transporterId, trucks);
        return ResponseEntity.ok(response);
    }
}
