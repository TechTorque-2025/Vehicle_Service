package com.techtorque.vehicle_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpdateDto {

    @Size(min = 2, max = 15, message = "License plate must be between 2 and 15 characters")
    private String licensePlate;

    @Size(max = 30, message = "Color must not exceed 30 characters")
    private String color;

    @Min(value = 0, message = "Mileage cannot be negative")
    @Max(value = 1000000, message = "Mileage seems unrealistic")
    private Integer mileage;
}
