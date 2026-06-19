package com.capg.delivery.repository;

import com.capg.delivery.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByDeliveryIdOrderByTimestampDesc(Long deliveryId);
}
