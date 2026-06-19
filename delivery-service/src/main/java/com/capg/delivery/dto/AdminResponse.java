package com.capg.delivery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {

    private Long id;
    private String status;
    private double price;

    private AddressDto sender;
    private AddressDto receiver;
    private java.time.Instant createdAt;
}
