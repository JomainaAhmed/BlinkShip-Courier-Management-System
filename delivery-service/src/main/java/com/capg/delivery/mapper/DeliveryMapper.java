package com.capg.delivery.mapper;

import com.capg.delivery.dto.AddressDto;
import com.capg.delivery.dto.AdminResponse;
import com.capg.delivery.dto.Request;
import com.capg.delivery.dto.Response;
import com.capg.delivery.entity.Address;
import com.capg.delivery.entity.Delivery;
import com.capg.delivery.enums.DeliveryStatus;

public class DeliveryMapper {

    public static Delivery toEntity(Request dto) {
        Delivery delivery = new Delivery();
        delivery.setSender(dto.getSender());
        delivery.setReceiver(dto.getReceiver());
        delivery.setPackageEntity(dto.getPackageDetails());
        delivery.setStatus(DeliveryStatus.DRAFT);
        return delivery;
    }

    public static Response toDTO(Delivery d) {
        Response dto = new Response();
        dto.setId(d.getId());
        dto.setStatus(d.getStatus().name());

        if (d.getPackageEntity() != null) {
            dto.setPrice(d.getPackageEntity().getPrice());
            dto.setPackageDetails(d.getPackageEntity());
        }

        dto.setSender(mapAddress(d.getSender()));
        dto.setReceiver(mapAddress(d.getReceiver()));
        
        dto.setCreatedAt(d.getCreatedAt());
        dto.setPackedAt(d.getPackedAt());
        dto.setDispatchedAt(d.getDispatchedAt());
        dto.setInTransitAt(d.getInTransitAt());
        dto.setDeliveredAt(d.getDeliveredAt());
        dto.setBookedAt(d.getBookedAt());
        dto.setPickedUpAt(d.getPickedUpAt());
        dto.setOutForDeliveryAt(d.getOutForDeliveryAt());

        return dto;
    }

    public static AdminResponse toAdminDTO(Delivery d) {
        AdminResponse dto = new AdminResponse();
        dto.setId(d.getId());
        dto.setStatus(d.getStatus().name());

        if (d.getPackageEntity() != null) {
            dto.setPrice(d.getPackageEntity().getPrice());
        }

        dto.setSender(mapAddress(d.getSender()));
        dto.setReceiver(mapAddress(d.getReceiver()));
        dto.setCreatedAt(d.getCreatedAt());

        return dto;
    }

    private static AddressDto mapAddress(Address a) {
        if (a == null) return null;
        AddressDto dto = new AddressDto();
        dto.setName(a.getName());
        dto.setPhone(a.getPhone());
        dto.setAddressLine(a.getAddressLine());
        dto.setCity(a.getCity());
        dto.setState(a.getState());
        dto.setPincode(a.getPincode());
        return dto;
    }
}