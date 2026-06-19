package com.capg.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private int total;
    private int draft;
    private int booked;
    private int packed;
    private int dispatched;
    private int pickedUp;
    private int inTransit;
    private int outForDelivery;
    private int delivered;
    private int delayed;
    private int failed;
    private int returned;
    private double successRate;
}