package com.capg.tracking.service;

import com.capg.tracking.dto.Response;
import com.capg.tracking.entity.Tracking;
import com.capg.tracking.mapper.TrackingMapper;
import com.capg.tracking.repository.TrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class TrackingService {

    private final TrackingRepository repository;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

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
        Tracking saved = repository.save(tracking);

        // Push real-time event to SSE subscribers if any
        notifySubscribers(deliveryId, TrackingMapper.toDTO(saved));
    }

    public SseEmitter subscribe(Long deliveryId) {
        SseEmitter emitter = new SseEmitter(180000L); // 3 minutes timeout
        emitters.computeIfAbsent(deliveryId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(deliveryId, emitter));
        emitter.onTimeout(() -> removeEmitter(deliveryId, emitter));
        emitter.onError(e -> removeEmitter(deliveryId, emitter));

        // Send current tracking history immediately
        try {
            List<Response> history = getTracking(deliveryId);
            emitter.send(SseEmitter.event().name("INIT").data(history));
        } catch (Exception e) {
            log.warn("Failed to send initial tracking state to SSE subscriber: {}", e.getMessage());
        }

        return emitter;
    }

    private void removeEmitter(Long deliveryId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(deliveryId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(deliveryId);
            }
        }
    }

    private void notifySubscribers(Long deliveryId, Response update) {
        List<SseEmitter> list = emitters.get(deliveryId);
        if (list != null && !list.isEmpty()) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().name("TRACKING_UPDATE").data(update));
                } catch (Exception e) {
                    removeEmitter(deliveryId, emitter);
                }
            }
        }
    }

    public List<Response> getTracking(Long deliveryId) {
        return repository.findByDeliveryIdOrderByTimestampAsc(deliveryId)
                .stream()
                .map(TrackingMapper::toDTO)
                .toList();
    }

    public void uploadDocument(Long deliveryId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String rawFilename = file.getOriginalFilename();
        if (rawFilename == null || rawFilename.contains("..")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String safeFileName = Paths.get(rawFilename).getFileName().toString();

        Tracking tracking = new Tracking();
        tracking.setDeliveryId(deliveryId);
        tracking.setStatus("Document uploaded");
        tracking.setLocation("System");
        tracking.setTimestamp(LocalDateTime.now());

        tracking.setDocument(file.getBytes());
        tracking.setFileName(safeFileName);
        tracking.setFileType(file.getContentType());

        log.info("Uploading document {} for delivery {}", safeFileName, deliveryId);
        repository.save(tracking);
    }

    public Tracking getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new com.capg.tracking.exception.TrackingNotFoundException("Tracking not found"));
    }

    public String getLocation(String status) {
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
