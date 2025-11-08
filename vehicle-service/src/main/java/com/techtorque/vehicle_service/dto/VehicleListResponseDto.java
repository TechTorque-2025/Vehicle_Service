package com.techtorque.vehicle_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleListResponseDto {

    private String vehicleId;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String color;
    private Integer mileage;
}
