package com.techtorque.vehicle_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Assuming you will create these DTOs later
// import com.techtorque.vehicle_service.dto.VehicleRequestDto;
// import com.techtorque.vehicle_service.dto.VehicleUpdateDto;

@RestController
@RequestMapping("/vehicles") // The gateway routes /api/v1/vehicles to this controller
@Tag(name = "Vehicle Management", description = "Endpoints for customers to manage their vehicles.")
@SecurityRequirement(name = "bearerAuth") // Indicates all endpoints here require JWT auth
public class VehicleController {

  // @Autowired
  // private VehicleService vehicleService;

  @Operation(summary = "Register a new vehicle for the current customer")
  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> registerNewVehicle(
          // @RequestBody VehicleRequestDto vehicleRequest,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Delegate to VehicleService to create and save the new vehicle for the customer
    return ResponseEntity.ok().body("{\"message\": \"Vehicle added\"}");
  }

  @Operation(summary = "List all vehicles for the current customer")
  @GetMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> listCustomerVehicles(
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Delegate to VehicleService to find all vehicles by customerId
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get details for a specific vehicle")
  @GetMapping("/{vehicleId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> getVehicleDetails(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Delegate to VehicleService, which MUST verify this vehicle belongs to the customer
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Update information for a specific vehicle")
  @PutMapping("/{vehicleId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> updateVehicleInfo(
          @PathVariable String vehicleId,
          // @RequestBody VehicleUpdateDto vehicleUpdate,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Delegate to VehicleService, which MUST verify ownership before updating
    return ResponseEntity.ok().body("{\"message\": \"Vehicle updated\"}");
  }

  @Operation(summary = "Remove a vehicle for the current customer")
  @DeleteMapping("/{vehicleId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> removeVehicle(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Delegate to VehicleService, which MUST verify ownership before deleting
    return ResponseEntity.ok().body("{\"message\": \"Vehicle removed\"}");
  }

  @Operation(summary = "Upload photos for a vehicle")
  @PostMapping("/{vehicleId}/photos")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> uploadVehiclePhotos(
          @PathVariable String vehicleId,
          @RequestParam("files") MultipartFile[] files,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: Delegate to a service that handles file uploads and associates photos with the vehicle
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Get service history for a specific vehicle")
  @GetMapping("/{vehicleId}/history")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<?> getServiceHistory(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    // TODO: This might require an inter-service call to the Project/Service Management service
    return ResponseEntity.ok().build();
  }
}