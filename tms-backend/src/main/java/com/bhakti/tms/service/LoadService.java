package com.bhakti.tms.service;

import com.bhakti.tms.dto.LoadRequestDTO;
import com.bhakti.tms.dto.LoadResponseDTO;
import com.bhakti.tms.entity.Load;
import com.bhakti.tms.entity.LoadStatus;
import com.bhakti.tms.exception.InvalidStatusTransitionException;
import com.bhakti.tms.exception.ResourceNotFoundException;
import com.bhakti.tms.repository.LoadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoadService {

    private final LoadRepository loadRepository;

    public LoadResponseDTO createLoad(LoadRequestDTO dto) {
        Load load = Load.builder()
                .shipperId(dto.getShipperId())
                .loadingCity(dto.getLoadingCity())
                .unloadingCity(dto.getUnloadingCity())
                .loadingDate(dto.getLoadingDate())
                .productType(dto.getProductType())
                .weight(dto.getWeight())
                .weightUnit(dto.getWeightUnit())
                .truckType(dto.getTruckType())
                .noOfTrucks(dto.getNoOfTrucks())
                .status(LoadStatus.POSTED)
                .datePosted(new Timestamp(System.currentTimeMillis()))
                .build();

        Load savedLoad = loadRepository.save(load);
        return toResponseDTO(savedLoad);
    }

    public List<LoadResponseDTO> getLoads(String shipperId, LoadStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Load> loadPage;

        if (shipperId != null) {
            loadPage = loadRepository.findByShipperId(shipperId, pageable);
        } else if (status != null) {
            loadPage = loadRepository.findByStatus(status, pageable);
        } else {
            loadPage = loadRepository.findAll(pageable);
        }

        return loadPage.getContent().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public LoadResponseDTO getLoadById(UUID loadId) {
        Load load = loadRepository.findByLoadId(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));
        return toResponseDTO(load);
    }

    public LoadResponseDTO cancelLoad(UUID loadId) {
        Load load = loadRepository.findByLoadId(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found"));

        if (load.getStatus() == LoadStatus.BOOKED) {
            throw new InvalidStatusTransitionException("Cannot cancel a booked load");
        }

        load.setStatus(LoadStatus.CANCELLED);
        Load updatedLoad = loadRepository.save(load);
        return toResponseDTO(updatedLoad);
    }

    private LoadResponseDTO toResponseDTO(Load load) {
        return LoadResponseDTO.builder()
                .loadId(load.getLoadId())
                .shipperId(load.getShipperId())
                .loadingCity(load.getLoadingCity())
                .unloadingCity(load.getUnloadingCity())
                .loadingDate(load.getLoadingDate())
                .productType(load.getProductType())
                .weight(load.getWeight())
                .weightUnit(load.getWeightUnit())
                .truckType(load.getTruckType())
                .noOfTrucks(load.getNoOfTrucks())
                .status(load.getStatus())
                .datePosted(load.getDatePosted())
                .build();
    }
}
