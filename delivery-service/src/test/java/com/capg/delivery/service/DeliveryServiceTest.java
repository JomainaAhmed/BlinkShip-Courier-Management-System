package com.capg.delivery.service;

import com.capg.delivery.dto.Request;
import com.capg.delivery.dto.Response;
import com.capg.delivery.entity.Delivery;
import com.capg.delivery.entity.PackageEntity;
import com.capg.delivery.enums.DeliveryStatus;
import com.capg.delivery.exception.InvalidStatusException;
import com.capg.delivery.repository.ActivityRepository;
import com.capg.delivery.repository.DeliveryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private DeliveryRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DeliveryService deliveryService;

    private Request sampleRequest;
    private Delivery sampleDelivery;

    @BeforeEach
    void setUp() {
        sampleRequest = new Request();

        com.capg.delivery.entity.Address sender = new com.capg.delivery.entity.Address();
        sender.setName("John Doe");
        sender.setAddressLine("Street 1");
        sender.setCity("City A");
        sender.setState("State A");
        sender.setPincode("123456");
        sampleRequest.setSender(sender);

        com.capg.delivery.entity.Address receiver = new com.capg.delivery.entity.Address();
        receiver.setName("Jane Smith");
        receiver.setAddressLine("Street 2");
        receiver.setCity("City B");
        receiver.setState("State B");
        receiver.setPincode("654321");
        sampleRequest.setReceiver(receiver);

        PackageEntity pkg = new PackageEntity();
        pkg.setWeight(2.0);
        pkg.setLength(10.0);
        pkg.setWidth(10.0);
        pkg.setHeight(10.0);
        sampleRequest.setPackageDetails(pkg);

        sampleDelivery = new Delivery();
        sampleDelivery.setId(1L);
        sampleDelivery.setStatus(DeliveryStatus.DRAFT);
        sampleDelivery.setPackageEntity(pkg);
    }

    @Test
    void testCreateDelivery_PricingLogic() {
        // Volumetric weight: (10*10*10)/5000 = 0.2
        // Chargeable weight: max(2.0, 0.2) = 2.0
        // Price: 2.0 * 300 = 600.0

        when(repository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            d.setId(1L);
            return d;
        });

        Response response = deliveryService.createDelivery(sampleRequest);

        assertNotNull(response);
        assertEquals(600.0, response.getPrice());
        verify(repository, times(1)).save(any(Delivery.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void testUpdateStatus_ValidTransition_User() {
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);
        when(context.getAuthentication())
                .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass", Collections.emptyList()));

        when(repository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(repository.save(any(Delivery.class))).thenReturn(sampleDelivery);

        Response response = deliveryService.updateStatus(1L, "BOOKED");

        assertNotNull(response);
        assertEquals(DeliveryStatus.BOOKED, sampleDelivery.getStatus());
    }

    @Test
    void testUpdateStatus_InvalidTransition_User() {
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);
        when(context.getAuthentication())
                .thenReturn(new UsernamePasswordAuthenticationToken("user", "pass", Collections.emptyList()));

        when(repository.findById(1L)).thenReturn(Optional.of(sampleDelivery));

        assertThrows(InvalidStatusException.class, () -> {
            deliveryService.updateStatus(1L, "DELIVERED");
        });
    }

    @Test
    void testUpdateStatus_ValidTransition_Admin() {
        SecurityContext context = mock(SecurityContext.class);
        SecurityContextHolder.setContext(context);
        when(context.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken("admin", "pass",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        when(repository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(repository.save(any(Delivery.class))).thenReturn(sampleDelivery);

        // Admin can force DELIVERED from DRAFT (based on logic in service)
        Response response = deliveryService.updateStatus(1L, "DELIVERED");

        assertNotNull(response);
        assertEquals(DeliveryStatus.DELIVERED, sampleDelivery.getStatus());
    }
}
