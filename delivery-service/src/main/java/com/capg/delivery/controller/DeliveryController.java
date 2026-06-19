package com.capg.delivery.controller;

import com.capg.delivery.dto.AdminResponse;
import com.capg.delivery.dto.Request;
import com.capg.delivery.dto.Response;
import com.capg.delivery.service.DeliveryService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    // USER → Create delivery
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/create")
    public Response create(@Valid @RequestBody Request dto) {
        return service.createDelivery(dto);
    }

    // PUBLIC → Get delivery by ID (For Tracking)
    @GetMapping("/{id}")
    public Response get(@PathVariable("id") Long id) {
        return service.getDelivery(id);
    }

    // ADMIN → Update delivery status
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public Response updateStatus(@PathVariable("id") Long id,
            @RequestParam("status") String status) {
        return service.updateStatus(id, status);
    }

    // ADMIN → Get all deliveries
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<AdminResponse> getAll() {
        return service.getAllDeliveriesDetailed();
    }

    // USER → Get user deliveries
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/user/{username}")
    public List<Response> getByUser(@PathVariable("username") String username) {
        return service.getDeliveriesBySender(username);
    }

    @PostMapping("/calculate")
    public Double calculate(@RequestBody com.capg.delivery.entity.PackageEntity details) {
        return service.calculatePrice(details);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/activities")
    public List<com.capg.delivery.entity.Activity> getActivities(@PathVariable("id") Long id) {
        return service.getActivities(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/export/{username}")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(@PathVariable("username") String username) {
        String csv = service.exportShipmentsToCsv(username);
        byte[] csvData = csv.getBytes();

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=shipments_" + username + ".csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export/all")
    public org.springframework.http.ResponseEntity<byte[]> exportAllCsv() {
        String csv = service.exportAllShipmentsToCsv();
        byte[] csvData = csv.getBytes();

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=all_shipments.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

}
