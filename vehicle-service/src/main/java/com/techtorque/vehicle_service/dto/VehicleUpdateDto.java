package com.techtorque.vehicle_service.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpdateDto {

    private String color;

    @Min(value = 0, message = "Mileage cannot be negative")
    private Integer mileage;

    private String licensePlate;
}
