package com.capg.tracking.service;

import com.capg.tracking.dto.Response;
import com.capg.tracking.entity.Tracking;
import com.capg.tracking.repository.TrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackingServiceTest {

    @Mock
    private TrackingRepository repository;

    @InjectMocks
    private TrackingService trackingService;

    private Tracking tracking;

    @BeforeEach
    void setUp() {
        tracking = new Tracking();
        tracking.setId(1L);
        tracking.setDeliveryId(100L);
        tracking.setStatus("BOOKED");
        tracking.setLocation("Origin Hub");
        tracking.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testSaveTracking() {
        when(repository.save(any(Tracking.class))).thenReturn(tracking);

        trackingService.saveTracking(100L, "BOOKED");

        verify(repository, times(1)).save(any(Tracking.class));
    }

    @Test
    void testGetTracking() {
        when(repository.findByDeliveryIdOrderByTimestampAsc(100L)).thenReturn(List.of(tracking));

        List<Response> list = trackingService.getTracking(100L);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("BOOKED", list.get(0).getStatus());
    }

    @Test
    void testGetLocationMapping() {
        assertEquals("Origin Hub", trackingService.getLocation("BOOKED"));
        assertEquals("Pickup Location", trackingService.getLocation("PICKED_UP"));
        assertEquals("Transit Hub", trackingService.getLocation("IN_TRANSIT"));
        assertEquals("Local Hub", trackingService.getLocation("OUT_FOR_DELIVERY"));
        assertEquals("Delivered", trackingService.getLocation("DELIVERED"));
        assertEquals("Processing Center", trackingService.getLocation("OTHER"));
    }

    @Test
    void testGetById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(tracking));

        Tracking result = trackingService.getById(1L);

        assertNotNull(result);
        assertEquals(100L, result.getDeliveryId());
    }
}
