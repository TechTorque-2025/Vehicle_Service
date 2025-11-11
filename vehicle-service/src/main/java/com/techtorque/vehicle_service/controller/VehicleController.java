package com.techtorque.vehicle_service.controller;

import com.techtorque.vehicle_service.dto.*;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.mapper.VehicleMapper;
import com.techtorque.vehicle_service.service.PhotoStorageService;
import com.techtorque.vehicle_service.service.ServiceHistoryService;
import com.techtorque.vehicle_service.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicle Management", description = "Endpoints for customers to manage their vehicles.")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class VehicleController {

  private final VehicleService vehicleService;
  private final PhotoStorageService photoStorageService;
  private final ServiceHistoryService serviceHistoryService;

  public VehicleController(
          VehicleService vehicleService,
          PhotoStorageService photoStorageService,
          ServiceHistoryService serviceHistoryService) {
    this.vehicleService = vehicleService;
    this.photoStorageService = photoStorageService;
    this.serviceHistoryService = serviceHistoryService;
  }

  @Operation(summary = "Register a new vehicle for the current customer")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Vehicle successfully registered",
                  content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
          @ApiResponse(responseCode = "400", description = "Invalid input data"),
          @ApiResponse(responseCode = "409", description = "Vehicle with this VIN already exists")
  })
  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<ApiResponseDto> registerNewVehicle(
          @Valid @RequestBody VehicleRequestDto vehicleRequest,
          @RequestHeader("X-User-Subject") String customerId) {

    Vehicle savedVehicle = vehicleService.registerVehicle(vehicleRequest, customerId);

    ApiResponseDto response = ApiResponseDto.builder()
            .message("Vehicle added")
            .vehicleId(savedVehicle.getId())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "List all vehicles for the current customer")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle list",
                  content = @Content(schema = @Schema(implementation = VehicleListResponseDto.class)))
  })
  @GetMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<List<VehicleListResponseDto>> listCustomerVehicles(
          @RequestHeader("X-User-Subject") String customerId,
          @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

    log.info("Listing vehicles for user: {} with roles: {}", customerId, userRoles);

    try {
      List<Vehicle> vehicles;
      
      // Admin and Employee can see all vehicles, Customer sees only their own
      if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("EMPLOYEE"))) {
        log.info("Admin/Employee access - fetching all vehicles");
        vehicles = vehicleService.getAllVehicles();
      } else {
        log.info("Customer access - fetching vehicles for customerId: {}", customerId);
        vehicles = vehicleService.getVehiclesForCustomer(customerId);
      }
      
      List<VehicleListResponseDto> response = VehicleMapper.toListResponseDtos(vehicles);

      log.info("Successfully retrieved {} vehicles", vehicles.size());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error listing vehicles for user: {}", customerId, e);
      throw e;
    }
  }

  @Operation(summary = "Get details for a specific vehicle")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle details",
                  content = @Content(schema = @Schema(implementation = VehicleResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Vehicle not found"),
          @ApiResponse(responseCode = "403", description = "Not authorized to access this vehicle")
  })
  @GetMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<VehicleResponseDto> getVehicleDetails(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId,
          @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

    Vehicle vehicle;

    // Admin and Employee can see any vehicle, Customer sees only their own
    if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("EMPLOYEE"))) {
      vehicle = vehicleService.getVehicleById(vehicleId)
              .orElseThrow(() -> new com.techtorque.vehicle_service.exception.VehicleNotFoundException(vehicleId, customerId));
    } else {
      vehicle = vehicleService.getVehicleByIdAndCustomer(vehicleId, customerId)
              .orElseThrow(() -> new com.techtorque.vehicle_service.exception.VehicleNotFoundException(vehicleId, customerId));
    }

    VehicleResponseDto response = VehicleMapper.toResponseDto(vehicle);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update information for a specific vehicle")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Vehicle successfully updated",
                  content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Vehicle not found"),
          @ApiResponse(responseCode = "403", description = "Not authorized to update this vehicle")
  })
  @PutMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<ApiResponseDto> updateVehicleInfo(
          @PathVariable String vehicleId,
          @Valid @RequestBody VehicleUpdateDto vehicleUpdate,
          @RequestHeader("X-User-Subject") String customerId,
          @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

    // For Admin/Employee, allow updating any vehicle (pass empty string to bypass customer check)
    // For Customer, verify ownership
    if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("EMPLOYEE"))) {
      // Admin/Employee can update any vehicle - need to modify service method
      vehicleService.updateVehicle(vehicleId, vehicleUpdate, null);
    } else {
      vehicleService.updateVehicle(vehicleId, vehicleUpdate, customerId);
    }

    ApiResponseDto response = ApiResponseDto.builder()
            .message("Vehicle updated")
            .vehicleId(vehicleId)
            .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Remove a vehicle for the current customer")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Vehicle successfully removed",
                  content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Vehicle not found"),
          @ApiResponse(responseCode = "403", description = "Not authorized to delete this vehicle")
  })
  @DeleteMapping("/{vehicleId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<ApiResponseDto> removeVehicle(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId,
          @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

    // Admin/Employee can delete any vehicle, Customer can only delete their own
    if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("EMPLOYEE"))) {
      vehicleService.deleteVehicle(vehicleId, null);
    } else {
      vehicleService.deleteVehicle(vehicleId, customerId);
    }

    // Also delete associated photos
    photoStorageService.deleteVehiclePhotos(vehicleId);

    ApiResponseDto response = ApiResponseDto.builder()
            .message("Vehicle removed")
            .vehicleId(vehicleId)
            .build();

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Upload photos for a vehicle")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Photos successfully uploaded",
                  content = @Content(schema = @Schema(implementation = PhotoUploadResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Vehicle not found"),
          @ApiResponse(responseCode = "400", description = "Invalid file format")
  })
  @PostMapping("/{vehicleId}/photos")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<PhotoUploadResponseDto> uploadVehiclePhotos(
          @PathVariable String vehicleId,
          @RequestParam("files") MultipartFile[] files,
          @RequestHeader("X-User-Subject") String customerId) {

    PhotoUploadResponseDto response = photoStorageService.uploadVehiclePhotos(vehicleId, files, customerId);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get service history for a specific vehicle")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved service history",
                  content = @Content(schema = @Schema(implementation = ServiceHistoryDto.class))),
          @ApiResponse(responseCode = "404", description = "Vehicle not found")
  })
  @GetMapping("/{vehicleId}/history")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'EMPLOYEE')")
  public ResponseEntity<List<ServiceHistoryDto>> getServiceHistory(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId,
          @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {

    // Admin and Employee can see any vehicle's history, Customer sees only their own
    List<ServiceHistoryDto> history;
    if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("EMPLOYEE"))) {
      // For Admin/Employee, pass null as customerId to skip ownership check
      history = serviceHistoryService.getServiceHistory(vehicleId, null);
    } else {
      history = serviceHistoryService.getServiceHistory(vehicleId, customerId);
    }

    return ResponseEntity.ok(history);
  }

  @Operation(summary = "Get all photos for a vehicle")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved photo list"),
          @ApiResponse(responseCode = "404", description = "Vehicle not found")
  })
  @GetMapping("/{vehicleId}/photos")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<List<com.techtorque.vehicle_service.entity.VehiclePhoto>> getVehiclePhotoList(
          @PathVariable String vehicleId,
          @RequestHeader("X-User-Subject") String customerId) {

    List<com.techtorque.vehicle_service.entity.VehiclePhoto> photos = 
            photoStorageService.getVehiclePhotos(vehicleId, customerId);

    return ResponseEntity.ok(photos);
  }

  @Operation(summary = "Get a specific photo file")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Successfully retrieved photo file"),
          @ApiResponse(responseCode = "404", description = "Photo not found")
  })
  @GetMapping("/{vehicleId}/photos/{fileName}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<org.springframework.core.io.Resource> getPhotoFile(
          @PathVariable String vehicleId,
          @PathVariable String fileName,
          @RequestHeader("X-User-Subject") String customerId) {

    org.springframework.core.io.Resource resource = 
            photoStorageService.loadPhotoAsResource(vehicleId, fileName, customerId);

    // Determine content type
    String contentType = "image/jpeg"; // default
    try {
      contentType = java.nio.file.Files.probeContentType(
              java.nio.file.Paths.get(resource.getFile().getAbsolutePath()));
      if (contentType == null) {
        contentType = "image/jpeg";
      }
    } catch (Exception e) {
      log.warn("Could not determine file type for: {}", fileName);
    }

    return ResponseEntity.ok()
            .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                    "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
  }

  @Operation(summary = "Delete a specific photo")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Photo successfully deleted",
                  content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "Photo not found")
  })
  @DeleteMapping("/photos/{photoId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponseDto> deleteSinglePhoto(
          @PathVariable String photoId,
          @RequestHeader("X-User-Subject") String customerId) {

    photoStorageService.deleteSinglePhoto(photoId, customerId);

    ApiResponseDto response = ApiResponseDto.builder()
            .message("Photo deleted successfully")
            .build();

    return ResponseEntity.ok(response);
  }
}
