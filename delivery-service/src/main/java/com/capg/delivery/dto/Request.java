package com.capg.delivery.dto;

import com.capg.delivery.entity.Address;
import com.capg.delivery.entity.PackageEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    @NotNull
    private Address sender;

    @NotNull
    private Address receiver;

    @NotNull
    private PackageEntity packageDetails;
}