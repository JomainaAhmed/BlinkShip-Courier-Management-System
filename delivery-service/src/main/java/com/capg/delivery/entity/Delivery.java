package com.capg.delivery.entity;

import com.capg.delivery.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @OneToOne(cascade = CascadeType.ALL)
    private Address sender;

    @OneToOne(cascade = CascadeType.ALL)
    private Address receiver;

    @OneToOne(cascade = CascadeType.ALL)
    private PackageEntity packageEntity;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    private Instant packedAt;
    private Instant dispatchedAt;
    private Instant inTransitAt;
    private Instant deliveredAt;

    private Instant bookedAt;
    private Instant pickedUpAt;
    private Instant outForDeliveryAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}