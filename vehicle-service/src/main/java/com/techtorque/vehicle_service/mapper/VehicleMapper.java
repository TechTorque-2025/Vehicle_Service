package com.techtorque.vehicle_service.mapper;

import com.techtorque.vehicle_service.dto.VehicleListResponseDto;
import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleResponseDto;
import com.techtorque.vehicle_service.entity.Vehicle;

import java.util.List;
import java.util.stream.Collectors;

public class VehicleMapper {

    public static Vehicle toEntity(VehicleRequestDto dto, String customerId) {
        return Vehicle.builder()
                .customerId(customerId)
                .make(dto.getMake())
                .model(dto.getModel())
                .year(dto.getYear())
                .vin(dto.getVin().toUpperCase())
                .licensePlate(dto.getLicensePlate())
                .color(dto.getColor())
                .mileage(dto.getMileage() != null ? dto.getMileage() : 0)
                .build();
    }

    public static VehicleResponseDto toResponseDto(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .vehicleId(vehicle.getId())
                .customerId(vehicle.getCustomerId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .vin(vehicle.getVin())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .mileage(vehicle.getMileage())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }

    public static VehicleListResponseDto toListResponseDto(Vehicle vehicle) {
        return VehicleListResponseDto.builder()
                .vehicleId(vehicle.getId())
                .customerId(vehicle.getCustomerId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .licensePlate(vehicle.getLicensePlate())
                .color(vehicle.getColor())
                .mileage(vehicle.getMileage())
                .build();
    }

    public static List<VehicleListResponseDto> toListResponseDtos(List<Vehicle> vehicles) {
        return vehicles.stream()
                .map(VehicleMapper::toListResponseDto)
                .collect(Collectors.toList());
    }
}

