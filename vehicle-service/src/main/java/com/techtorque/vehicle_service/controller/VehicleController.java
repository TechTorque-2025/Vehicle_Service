package com.techtorque.vehicle_service.controller;

import com.techtorque.vehicle_service.dto.ApiResponse;
import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleResponseDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for customers to manage their vehicles.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  @Operation(summary = "Register a new vehicle for the current customer")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> registerNewVehicle(
          @Valid @RequestBody VehicleRequestDto vehicleRequest,
          @RequestHeader("X-User-Subject") String customerId) {

    Vehicle vehicle = vehicleService.registerVehicle(vehicleRequest, customerId);
    VehicleResponseDto response = mapToResponseDto(vehicle);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Vehicle registered successfully", response));
  }

  @Operation(summary = "List all vehicles for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> listCustomerVehicles(
          @RequestHeader("X-User-Subject") String customerId) {

    List<Vehicle> vehicles = vehicleService.getVehiclesForCustomer(customerId);
    List<VehicleResponseDto> response = vehicles.stream()
            .map(this::mapToResponseDto)
            .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", response));
  }

  @Operation(summary = "Get details for a specific vehicle")
  @GetMapping("/{vehicleId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> getVehicleDetails(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    Vehicle vehicle = vehicleService.getVehicleByIdAndCustomer(vehicleId, customerId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found or access denied"));

    VehicleResponseDto response = mapToResponseDto(vehicle);
    return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved successfully", response));
  }

  @Operation(summary = "Update information for a specific vehicle")
  @PutMapping("/{vehicleId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> updateVehicleInfo(
          @PathVariable String vehicleId,
          @Valid @RequestBody VehicleUpdateDto vehicleUpdate,
          @RequestHeader("X-User-Subject") String customerId) {

    Vehicle vehicle = vehicleService.updateVehicle(vehicleId, vehicleUpdate, customerId);
    VehicleResponseDto response = mapToResponseDto(vehicle);

    return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
  }

  @Operation(summary = "Remove a vehicle for the current customer")
  @DeleteMapping("/{vehicleId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> removeVehicle(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    vehicleService.deleteVehicle(vehicleId, customerId);
    return ResponseEntity.ok(ApiResponse.success("Vehicle deleted successfully"));
  }

  @Operation(summary = "Upload photos for a vehicle")
  @PostMapping("/{vehicleId}/photos")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> uploadVehiclePhotos(
          @PathVariable String vehicleId,
          @RequestParam("files") MultipartFile[] files,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Implement file upload service
    // For now, return a placeholder response
    return ResponseEntity.ok(ApiResponse.success("Photo upload feature coming soon"));
  }

  @Operation(summary = "Get service history for a specific vehicle")
  @GetMapping("/{vehicleId}/history")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse> getServiceHistory(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    // Verify ownership first
    vehicleService.getVehicleByIdAndCustomer(vehicleId, customerId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found or access denied"));

    // TODO: Inter-service call to Project/Service Management service
    // For now, return placeholder
    return ResponseEntity.ok(ApiResponse.success("Service history feature coming soon", List.of()));
  }

  // Helper method to map Entity to DTO
  private VehicleResponseDto mapToResponseDto(Vehicle vehicle) {
    return VehicleResponseDto.builder()
            .id(vehicle.getId())
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
}