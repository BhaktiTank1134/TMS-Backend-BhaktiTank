package com.bhakti.tms.service;

import com.bhakti.tms.dto.AvailableTruckDTO;
import com.bhakti.tms.dto.TransporterRequestDTO;
import com.bhakti.tms.dto.TransporterResponseDTO;
import com.bhakti.tms.entity.AvailableTruck;
import com.bhakti.tms.entity.Transporter;
import com.bhakti.tms.exception.ResourceNotFoundException;
import com.bhakti.tms.repository.TransporterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransporterService {

    private final TransporterRepository transporterRepository;

    public TransporterResponseDTO registerTransporter(TransporterRequestDTO dto) {
        Transporter transporter = Transporter.builder()
                .companyName(dto.getCompanyName())
                .rating(dto.getRating())
                .availableTrucks(new ArrayList<>())
                .build();

        if (dto.getAvailableTrucks() != null) {
            List<AvailableTruck> trucks = dto.getAvailableTrucks().stream()
                    .map(truckDTO -> AvailableTruck.builder()
                            .truckType(truckDTO.getTruckType())
                            .count(truckDTO.getCount())
                            .transporter(transporter)
                            .build())
                    .collect(Collectors.toList());
            transporter.setAvailableTrucks(trucks);
        }

        Transporter savedTransporter = transporterRepository.save(transporter);
        return toResponseDTO(savedTransporter);
    }

    public TransporterResponseDTO getTransporter(UUID transporterId) {
        Transporter transporter = transporterRepository.findByTransporterId(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found"));
        return toResponseDTO(transporter);
    }

    public TransporterResponseDTO updateAvailableTrucks(UUID transporterId, List<AvailableTruckDTO> trucks) {
        Transporter transporter = transporterRepository.findByTransporterId(transporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Transporter not found"));

        transporter.getAvailableTrucks().clear();

        List<AvailableTruck> updatedTrucks = trucks.stream()
                .map(truckDTO -> AvailableTruck.builder()
                        .truckType(truckDTO.getTruckType())
                        .count(truckDTO.getCount())
                        .transporter(transporter)
                        .build())
                .collect(Collectors.toList());

        transporter.setAvailableTrucks(updatedTrucks);
        Transporter savedTransporter = transporterRepository.save(transporter);
        return toResponseDTO(savedTransporter);
    }

    private TransporterResponseDTO toResponseDTO(Transporter transporter) {
        List<AvailableTruckDTO> truckDTOs = null;
        if (transporter.getAvailableTrucks() != null) {
            truckDTOs = transporter.getAvailableTrucks().stream()
                    .map(truck -> AvailableTruckDTO.builder()
                            .truckType(truck.getTruckType())
                            .count(truck.getCount())
                            .build())
                    .collect(Collectors.toList());
        }

        return TransporterResponseDTO.builder()
                .transporterId(transporter.getTransporterId())
                .companyName(transporter.getCompanyName())
                .rating(transporter.getRating())
                .availableTrucks(truckDTOs)
                .build();
    }
}
