package com.capg.delivery.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import com.capg.delivery.dto.AdminResponse;
import com.capg.delivery.dto.Request;
import com.capg.delivery.dto.Response;
import com.capg.delivery.entity.Activity;
import com.capg.delivery.entity.Delivery;
import com.capg.delivery.entity.PackageEntity;
import com.capg.delivery.enums.DeliveryStatus;
import com.capg.delivery.exception.DeliveryNotFoundException;
import com.capg.delivery.exception.InvalidStatusException;
import com.capg.delivery.mapper.DeliveryMapper;
import com.capg.delivery.repository.ActivityRepository;
import com.capg.delivery.repository.DeliveryRepository;
import com.capg.delivery.config.RabbitMQConfig;
import com.capg.delivery.dto.TrackingMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class DeliveryService {

    private final DeliveryRepository repository;
    private final ActivityRepository activityRepository;
    private final RabbitTemplate rabbitTemplate;

    public DeliveryService(DeliveryRepository repository, ActivityRepository activityRepository,
            RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.activityRepository = activityRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Response createDelivery(Request dto) {

        Delivery delivery = DeliveryMapper.toEntity(dto);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        delivery.setUsername(username);

        PackageEntity pkg = delivery.getPackageEntity();

        double volumetricWeight = (pkg.getLength() * pkg.getWidth() * pkg.getHeight()) / 5000.0;
        double chargeableWeight = Math.max(pkg.getWeight(), volumetricWeight);

        double baseRate = 300;
        double price = chargeableWeight * baseRate;

        pkg.setPrice(price);

        log.info("Creating delivery for package with chargeable weight: {}", chargeableWeight);
        Delivery savedDelivery = repository.save(delivery);

        // Log Activity
        activityRepository.save(Activity.builder()
                .deliveryId(savedDelivery.getId())
                .action("CREATED")
                .details("Shipment created and booked.")
                .build());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                new TrackingMessage(savedDelivery.getId(), savedDelivery.getStatus().name()));

        return DeliveryMapper.toDTO(savedDelivery);
    }

    public Response getDelivery(Long id) {
        Delivery delivery = repository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with id: " + id));

        return DeliveryMapper.toDTO(delivery);
    }

    public List<Response> getAllDeliveries() {
        return repository.findAll()
                .stream()
                .map(DeliveryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Response updateStatus(Long id, String status) {

        Delivery delivery = repository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found with id: " + id));

        DeliveryStatus newStatus;

        try {
            newStatus = DeliveryStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new InvalidStatusException("Invalid status value: " + status);
        }

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isValidTransition(delivery.getStatus(), newStatus, isAdmin)) {
            throw new InvalidStatusException(
                    "Invalid status transition from " + delivery.getStatus() + " to " + newStatus);
        }

        delivery.setStatus(newStatus);

        // Update specific timestamps based on status
        switch (newStatus) {
            case BOOKED -> delivery.setBookedAt(Instant.now());
            case PACKED -> delivery.setPackedAt(Instant.now());
            case DISPATCHED -> delivery.setDispatchedAt(Instant.now());
            case PICKED_UP -> delivery.setPickedUpAt(Instant.now());
            case IN_TRANSIT -> delivery.setInTransitAt(Instant.now());
            case OUT_FOR_DELIVERY -> delivery.setOutForDeliveryAt(Instant.now());
            case DELIVERED -> delivery.setDeliveredAt(Instant.now());
        }

        log.info("Updating delivery {} status to {}", id, newStatus);
        Delivery saved = repository.save(delivery);

        // Log Activity
        activityRepository.save(Activity.builder()
                .deliveryId(saved.getId())
                .action(newStatus.name())
                .details("Shipment status updated to " + newStatus.name().replace("_", " "))
                .build());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                new TrackingMessage(saved.getId(), saved.getStatus().name()));

        return DeliveryMapper.toDTO(saved);
    }

    private boolean isValidTransition(DeliveryStatus current, DeliveryStatus next, boolean isAdmin) {

        Set<DeliveryStatus> adminStates = Set.of(
                DeliveryStatus.DELAYED,
                DeliveryStatus.RETURNED,
                DeliveryStatus.FAILED,
                DeliveryStatus.DELIVERED);

        if (isAdmin && adminStates.contains(next)) {
            return true;
        }

        switch (current) {
            case DRAFT:
                return next == DeliveryStatus.BOOKED;

            case BOOKED:
                return next == DeliveryStatus.PACKED;

            case PACKED:
                return next == DeliveryStatus.DISPATCHED;

            case DISPATCHED:
                return next == DeliveryStatus.IN_TRANSIT || next == DeliveryStatus.PICKED_UP;

            case PICKED_UP:
                return next == DeliveryStatus.IN_TRANSIT;

            case IN_TRANSIT:
                return next == DeliveryStatus.OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY:
                return next == DeliveryStatus.DELIVERED;

            case DELIVERED:
            case FAILED:
                return next == DeliveryStatus.RETURNED;

            case RETURNED:
                return false;

            case DELAYED:
                return next == DeliveryStatus.IN_TRANSIT || next == DeliveryStatus.OUT_FOR_DELIVERY;

            default:
                return false;
        }
    }

    public List<AdminResponse> getAllDeliveriesDetailed() {
        return repository.findAll()
                .stream()
                .map(DeliveryMapper::toAdminDTO)
                .toList();
    }

    public List<Response> getDeliveriesBySender(String name) {
        return repository.findByUsername(name)
                .stream()
                .map(DeliveryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<Activity> getActivities(Long deliveryId) {
        return activityRepository.findByDeliveryIdOrderByTimestampDesc(deliveryId);
    }

    public String exportShipmentsToCsv(String username) {
        List<Delivery> deliveries = repository.findByUsername(username);
        return convertToCsv(deliveries);
    }

    public String exportAllShipmentsToCsv() {
        List<Delivery> deliveries = repository.findAll();
        return convertToCsv(deliveries);
    }

    private String convertToCsv(List<Delivery> deliveries) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Sender City,Receiver City,Description,Weight,Price,Status,Created At\n");

        for (Delivery d : deliveries) {
            csv.append(String.format("%d,%s,%s,%s,%.2f,%.2f,%s,%s\n",
                    d.getId(),
                    d.getSender().getCity(),
                    d.getReceiver().getCity(),
                    d.getPackageEntity().getDescription().replace(",", ";"),
                    d.getPackageEntity().getWeight(),
                    d.getPackageEntity().getPrice(),
                    d.getStatus().name(),
                    d.getCreatedAt()));
        }
        return csv.toString();
    }

    public String generateInvoice(Long id) {
        Delivery d = repository.findById(id).orElseThrow();
        StringBuilder invoice = new StringBuilder();
        invoice.append("==========================================\n");
        invoice.append("             BLINKSHIP INVOICE             \n");
        invoice.append("==========================================\n");
        invoice.append("Shipment ID: #").append(d.getId()).append("\n");
        invoice.append("Date: ").append(d.getCreatedAt()).append("\n");
        invoice.append("Status: ").append(d.getStatus()).append("\n");
        invoice.append("------------------------------------------\n");
        invoice.append("Sender: ").append(d.getSender().getName()).append("\n");
        invoice.append("Receiver: ").append(d.getReceiver().getName()).append("\n");
        invoice.append("------------------------------------------\n");
        invoice.append("Package: ").append(d.getPackageEntity().getDescription()).append("\n");
        invoice.append("Weight: ").append(d.getPackageEntity().getWeight()).append(" kg\n");
        invoice.append("Price: INR ").append(d.getPackageEntity().getPrice()).append("\n");
        invoice.append("==========================================\n");
        invoice.append("         Thank you for choosing us!       \n");
        invoice.append("==========================================\n");
        return invoice.toString();
    }

    public String uploadDocument(Long deliveryId, MultipartFile file) throws java.io.IOException {
        Path uploadPath = Paths.get("uploads", deliveryId.toString());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Log Activity
        activityRepository.save(Activity.builder()
                .deliveryId(deliveryId)
                .action("DOCUMENT_UPLOADED")
                .details("New document uploaded: " + file.getOriginalFilename())
                .build());

        return fileName;
    }

    public byte[] getDocument(Long deliveryId, String fileName) throws java.io.IOException {
        Path filePath = Paths.get("uploads", deliveryId.toString()).resolve(fileName);
        return Files.readAllBytes(filePath);
    }

    public double calculatePrice(PackageEntity pkg) {
        double volumetricWeight = (pkg.getLength() * pkg.getWidth() * pkg.getHeight()) / 5000.0;
        double chargeableWeight = Math.max(pkg.getWeight(), volumetricWeight);
        double baseRate = 300;
        return chargeableWeight * baseRate;
    }
}
