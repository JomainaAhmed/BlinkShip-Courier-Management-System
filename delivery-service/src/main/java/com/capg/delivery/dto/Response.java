package com.capg.delivery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private Long id;
    private String status;
    private double price;
    private com.capg.delivery.dto.AddressDto sender;
    private com.capg.delivery.dto.AddressDto receiver;
    private com.capg.delivery.entity.PackageEntity packageDetails;
    private java.time.Instant createdAt;
    private java.time.Instant packedAt;
    private java.time.Instant dispatchedAt;
    private java.time.Instant inTransitAt;
    private java.time.Instant deliveredAt;
    private java.time.Instant bookedAt;
    private java.time.Instant pickedUpAt;
    private java.time.Instant outForDeliveryAt;
}