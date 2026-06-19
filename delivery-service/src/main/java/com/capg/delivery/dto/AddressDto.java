package com.capg.delivery.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private String name;
    private String phone;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
}
