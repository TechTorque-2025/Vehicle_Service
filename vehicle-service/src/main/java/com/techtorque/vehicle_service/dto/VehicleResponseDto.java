package com.techtorque.vehicle_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDto {

    private String vehicleId;
    private String customerId;
    private String make;
    private String model;
    private Integer year;
    private String vin;
    private String licensePlate;
    private String color;
    private Integer mileage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
