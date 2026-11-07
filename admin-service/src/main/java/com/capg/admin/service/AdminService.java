package com.capg.admin.service;

import com.capg.admin.client.*;
import com.capg.admin.dto.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AdminService {

    private final DeliveryClient deliveryClient;
    private final AuthClient authClient;

    public AdminService(DeliveryClient deliveryClient, AuthClient authClient) {
        this.deliveryClient = deliveryClient;
        this.authClient = authClient;
    }

    public DashboardDto getDashboard() {
        log.info("Generating admin dashboard data");
        List<AdminDeliveryDto> deliveries;
        try {
            deliveries = deliveryClient.getAllDeliveries();
            if (deliveries == null) {
                deliveries = Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Failed to fetch deliveries from Delivery Service: {}. Returning empty dashboard fallback.", e.getMessage());
            deliveries = Collections.emptyList();
        }

        int total = deliveries.size();
        int draft = 0, booked = 0, packed = 0, dispatched = 0, pickedUp = 0, inTransit = 0, outForDelivery = 0, delivered = 0, delayed = 0, failed = 0, returned = 0;
        for (AdminDeliveryDto d : deliveries) {
            if (d == null || d.getStatus() == null) continue;
            switch (d.getStatus()) {
                case "DRAFT" -> draft++;
                case "BOOKED" -> booked++;
                case "PACKED" -> packed++;
                case "DISPATCHED" -> dispatched++;
                case "PICKED_UP" -> pickedUp++;
                case "IN_TRANSIT" -> inTransit++;
                case "OUT_FOR_DELIVERY" -> outForDelivery++;
                case "DELIVERED" -> delivered++;
                case "DELAYED" -> delayed++;
                case "FAILED" -> failed++;
                case "RETURNED" -> returned++;
            }
        }

        DashboardDto dto = new DashboardDto();
        dto.setTotal(total);
        dto.setDraft(draft);
        dto.setBooked(booked);
        dto.setPacked(packed);
        dto.setDispatched(dispatched);
        dto.setPickedUp(pickedUp);
        dto.setInTransit(inTransit);
        dto.setOutForDelivery(outForDelivery);
        dto.setDelivered(delivered);
        dto.setDelayed(delayed);
        dto.setFailed(failed);
        dto.setReturned(returned);

        double successRate = total == 0 ? 0 : (delivered * 100.0 / total);
        dto.setSuccessRate(successRate);

        return dto;
    }

    public List<AdminDeliveryDto> getAllDeliveries() {
        try {
            List<AdminDeliveryDto> list = deliveryClient.getAllDeliveries();
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving deliveries from DeliveryClient: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public void resolve(Long id) {
        DeliveryDto delivery = deliveryClient.getDelivery(id);
        String current = delivery.getStatus();
        String next;

        switch (current) {
            case "DELAYED":
                next = "IN_TRANSIT";
                break;
            case "FAILED":
                next = "RETURNED";
                break;
            case "RETURNED":
                throw new com.capg.admin.exception.CustomException("Cannot resolve returned delivery");
            default:
                throw new com.capg.admin.exception.CustomException("Only exception states can be resolved");
        }

        log.info("Resolving delivery {} from status {} to {}", id, current, next);
        deliveryClient.updateStatus(id, next);
    }

    public void updateStatus(Long id, String status) {
        log.info("Admin manual status update for shipment {}: {}", id, status);
        deliveryClient.updateStatus(id, status);
    }

    public ReportDto getReports() {
        List<AdminDeliveryDto> deliveries;
        try {
            deliveries = deliveryClient.getAllDeliveries();
            if (deliveries == null) {
                deliveries = Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Failed to fetch deliveries for reports: {}. Returning empty report fallback.", e.getMessage());
            deliveries = Collections.emptyList();
        }

        int total = deliveries.size();
        int delivered = 0, failed = 0, delayed = 0;

        for (AdminDeliveryDto d : deliveries) {
            if (d == null || d.getStatus() == null) continue;
            switch (d.getStatus()) {
                case "DELIVERED" -> delivered++;
                case "FAILED" -> failed++;
                case "DELAYED" -> delayed++;
            }
        }

        ReportDto report = new ReportDto();
        report.setTotal(total);
        report.setDelivered(delivered);
        report.setFailed(failed);
        report.setDelayed(delayed);

        double successRate = total == 0 ? 0 : (delivered * 100.0 / total);
        double failureRate = total == 0 ? 0 : (failed * 100.0 / total);
        double delayRate = total == 0 ? 0 : (delayed * 100.0 / total);

        report.setSuccessRate(successRate);
        report.setFailureRate(failureRate);
        report.setDelayRate(delayRate);

        return report;
    }

    public List<UserDto> getAllUsers() {
        try {
            List<UserDto> users = authClient.getAllUsers();
            return users != null ? users : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error retrieving users from AuthClient: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
