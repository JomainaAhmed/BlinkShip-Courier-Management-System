package com.capg.admin.controller;

import com.capg.admin.dto.*;
import com.capg.admin.service.AdminService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public DashboardDto dashboard() {
        return service.getDashboard();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deliveries")
    public List<AdminDeliveryDto> deliveries() {
        return service.getAllDeliveries();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deliveries/{id}/resolve")
    public String resolve(@PathVariable("id") Long id) {
        service.resolve(id);
        return "Issue has been resolved successfully";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/deliveries/{id}/status")
    public String updateStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        service.updateStatus(id, status);
        return "Status updated successfully";
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public ReportDto reports() {
        return service.getReports();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public List<UserDto> users() {
        return service.getAllUsers();
    }
}