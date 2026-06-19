package com.capg.tracking.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    private String status;
    private String location;
    private LocalDateTime timestamp;
}