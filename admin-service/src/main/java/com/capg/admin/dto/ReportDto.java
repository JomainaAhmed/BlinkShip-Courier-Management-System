package com.capg.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {

    private int total;
    private int delivered;
    private int failed;
    private int delayed;
    private double successRate;
    private double failureRate;
    private double delayRate;
}