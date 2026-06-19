package com.capg.tracking.service;

import com.capg.tracking.dto.Response;
import com.capg.tracking.entity.Tracking;
import com.capg.tracking.mapper.TrackingMapper;
import com.capg.tracking.repository.TrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TrackingService {

    private final TrackingRepository repository;

    public TrackingService(TrackingRepository repository) {
        this.repository = repository;
    }

    public void saveTracking(Long deliveryId, String status) {

        Tracking tracking = new Tracking();
        tracking.setDeliveryId(deliveryId);
        tracking.setStatus(status);
        tracking.setLocation(getLocation(status));
        tracking.setTimestamp(LocalDateTime.now());

        log.info("Recording tracking info for delivery {}: {}", deliveryId, status);
        repository.save(tracking);
    }

    public List<Response> getTracking(Long deliveryId) {
        return repository.findByDeliveryIdOrderByTimestampAsc(deliveryId)
                .stream()
                .map(TrackingMapper::toDTO)
                .toList();
    }

    public void uploadDocument(Long deliveryId, MultipartFile file) throws IOException {

        Tracking tracking = new Tracking();

        tracking.setDeliveryId(deliveryId);
        tracking.setStatus("Document uploaded");
        tracking.setLocation("System");
        tracking.setTimestamp(LocalDateTime.now());

        tracking.setDocument(file.getBytes());
        tracking.setFileName(file.getOriginalFilename());
        tracking.setFileType(file.getContentType());

        log.info("Uploading document {} for delivery {}", file.getOriginalFilename(), deliveryId);
        repository.save(tracking);
    }

    public Tracking getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new com.capg.tracking.exception.TrackingNotFoundException("Tracking not found"));
    }

    private String getLocation(String status) {
        return switch (status) {
            case "BOOKED" -> "Origin Hub";
            case "PICKED_UP" -> "Pickup Location";
            case "IN_TRANSIT" -> "Transit Hub";
            case "OUT_FOR_DELIVERY" -> "Local Hub";
            case "DELIVERED" -> "Delivered";
            default -> "Processing Center";
        };
    }
}