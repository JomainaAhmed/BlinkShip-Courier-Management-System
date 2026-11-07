package com.capg.delivery.repository;

import com.capg.delivery.entity.Delivery;
import com.capg.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByUsername(String username);
    Page<Delivery> findByUsername(String username, Pageable pageable);
    Page<Delivery> findByStatus(DeliveryStatus status, Pageable pageable);
}
