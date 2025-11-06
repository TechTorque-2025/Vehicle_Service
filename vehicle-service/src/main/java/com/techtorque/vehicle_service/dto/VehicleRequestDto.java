package com.techtorque.vehicle_service.dto;

import com.techtorque.vehicle_service.validation.ValidVehicleYear;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestDto {

    @NotBlank(message = "Make is required")
    @Size(min = 2, max = 50, message = "Make must be between 2 and 50 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
    private String model;

    @NotNull(message = "Year is required")
    @ValidVehicleYear
    private Integer year;

    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "Invalid VIN format")
    private String vin;

    @NotBlank(message = "License plate is required")
    @Size(min = 2, max = 15, message = "License plate must be between 2 and 15 characters")
    private String licensePlate;

    @Size(max = 30, message = "Color must not exceed 30 characters")
    private String color;

    @Min(value = 0, message = "Mileage cannot be negative")
    @Max(value = 1000000, message = "Mileage seems unrealistic")
    private Integer mileage;
}
