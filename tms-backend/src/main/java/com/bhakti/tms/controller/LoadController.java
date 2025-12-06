package com.bhakti.tms.controller;

import com.bhakti.tms.dto.LoadRequestDTO;
import com.bhakti.tms.dto.LoadResponseDTO;
import com.bhakti.tms.entity.LoadStatus;
import com.bhakti.tms.service.LoadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/load")
@RequiredArgsConstructor
public class LoadController {

    private final LoadService loadService;

    @PostMapping
    public ResponseEntity<LoadResponseDTO> createLoad(@Valid @RequestBody LoadRequestDTO loadRequestDTO) {
        LoadResponseDTO response = loadService.createLoad(loadRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LoadResponseDTO>> getLoads(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) LoadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<LoadResponseDTO> loads = loadService.getLoads(shipperId, status, page, size);
        return ResponseEntity.ok(loads);
    }

    @GetMapping("/{loadId}")
    public ResponseEntity<LoadResponseDTO> getLoadById(@PathVariable UUID loadId) {
        LoadResponseDTO load = loadService.getLoadById(loadId);
        return ResponseEntity.ok(load);
    }

    @PatchMapping("/{loadId}/cancel")
    public ResponseEntity<LoadResponseDTO> cancelLoad(@PathVariable UUID loadId) {
        LoadResponseDTO cancelledLoad = loadService.cancelLoad(loadId);
        return ResponseEntity.ok(cancelledLoad);
    }
}
