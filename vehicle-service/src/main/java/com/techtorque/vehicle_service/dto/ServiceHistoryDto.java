package com.techtorque.vehicle_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHistoryDto {

    private String serviceId;
    private LocalDateTime date;
    private String type;
    private BigDecimal cost;
    private String description;
}
