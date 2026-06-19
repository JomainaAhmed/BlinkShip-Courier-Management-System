package com.capg.delivery.controller;

import com.capg.delivery.service.DeliveryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/deliveries/documents")
public class FileUploadController {

    private final DeliveryService deliveryService;

    public FileUploadController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/upload")
    public ResponseEntity<String> upload(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            String fileName = deliveryService.uploadDocument(id, file);
            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable("id") Long id, @PathVariable("fileName") String fileName) {
        try {
            byte[] data = deliveryService.getDocument(id, fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
