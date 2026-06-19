package com.capg.tracking.controller;

import com.capg.tracking.dto.Response;
import com.capg.tracking.entity.Tracking;
import com.capg.tracking.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final TrackingService service;

    public TrackingController(TrackingService service) {
        this.service = service;
    }

    @Operation(summary = "Get tracking details for a delivery")
    @GetMapping("/{deliveryId}")
    public List<Response> getTracking(@PathVariable("deliveryId") Long deliveryId) {
        return service.getTracking(deliveryId);
    }

    @Operation(summary = "Upload a tracking document", description = "Uploads a file associated with a delivery ID")
    @PostMapping(value = "/{deliveryId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(
            @PathVariable("deliveryId") Long deliveryId,
            @Parameter(description = "The document to upload", required = true, 
                       content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, 
                                          schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) throws Exception {

        service.uploadDocument(deliveryId, file);
        return "Uploaded successfully";
    }

    @Operation(summary = "Download a tracking document", 
               responses = @ApiResponse(responseCode = "200", 
                           content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE, 
                                              schema = @Schema(type = "string", format = "binary"))))
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping(value = "/{trackingId}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable("trackingId") Long trackingId) {

        Tracking t = service.getById(trackingId);

        String fileType = t.getFileType() != null && !t.getFileType().isEmpty() 
            ? t.getFileType() 
            : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + t.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileType))
                .body(t.getDocument());
    }
}