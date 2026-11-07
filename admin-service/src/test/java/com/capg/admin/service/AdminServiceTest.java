package com.capg.admin.service;

import com.capg.admin.client.AuthClient;
import com.capg.admin.client.DeliveryClient;
import com.capg.admin.dto.AdminDeliveryDto;
import com.capg.admin.dto.DashboardDto;
import com.capg.admin.dto.DeliveryDto;
import com.capg.admin.dto.ReportDto;
import com.capg.admin.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private DeliveryClient deliveryClient;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private AdminService adminService;

    private AdminDeliveryDto deliveryDto1;
    private AdminDeliveryDto deliveryDto2;

    @BeforeEach
    void setUp() {
        deliveryDto1 = new AdminDeliveryDto();
        deliveryDto1.setId(1L);
        deliveryDto1.setStatus("DELIVERED");

        deliveryDto2 = new AdminDeliveryDto();
        deliveryDto2.setId(2L);
        deliveryDto2.setStatus("FAILED");
    }

    @Test
    void testGetDashboard() {
        when(deliveryClient.getAllDeliveries()).thenReturn(List.of(deliveryDto1, deliveryDto2));

        DashboardDto dashboard = adminService.getDashboard();

        assertNotNull(dashboard);
        assertEquals(2, dashboard.getTotal());
        assertEquals(1, dashboard.getDelivered());
        assertEquals(1, dashboard.getFailed());
        assertEquals(50.0, dashboard.getSuccessRate());
    }

    @Test
    void testGetDashboard_FallbackWhenClientFails() {
        when(deliveryClient.getAllDeliveries()).thenThrow(new RuntimeException("Service unavailable"));

        DashboardDto dashboard = adminService.getDashboard();

        assertNotNull(dashboard);
        assertEquals(0, dashboard.getTotal());
        assertEquals(0.0, dashboard.getSuccessRate());
    }

    @Test
    void testResolve_DelayedToInTransit() {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setId(1L);
        deliveryDto.setStatus("DELAYED");

        when(deliveryClient.getDelivery(1L)).thenReturn(deliveryDto);

        assertDoesNotThrow(() -> adminService.resolve(1L));
        verify(deliveryClient, times(1)).updateStatus(1L, "IN_TRANSIT");
    }

    @Test
    void testResolve_FailedToReturned() {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setId(2L);
        deliveryDto.setStatus("FAILED");

        when(deliveryClient.getDelivery(2L)).thenReturn(deliveryDto);

        assertDoesNotThrow(() -> adminService.resolve(2L));
        verify(deliveryClient, times(1)).updateStatus(2L, "RETURNED");
    }

    @Test
    void testResolve_NonExceptionStateThrows() {
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setId(3L);
        deliveryDto.setStatus("DELIVERED");

        when(deliveryClient.getDelivery(3L)).thenReturn(deliveryDto);

        assertThrows(CustomException.class, () -> adminService.resolve(3L));
    }

    @Test
    void testGetReports() {
        when(deliveryClient.getAllDeliveries()).thenReturn(List.of(deliveryDto1, deliveryDto2));

        ReportDto report = adminService.getReports();

        assertNotNull(report);
        assertEquals(2, report.getTotal());
        assertEquals(1, report.getDelivered());
        assertEquals(1, report.getFailed());
        assertEquals(50.0, report.getSuccessRate());
        assertEquals(50.0, report.getFailureRate());
    }
}
