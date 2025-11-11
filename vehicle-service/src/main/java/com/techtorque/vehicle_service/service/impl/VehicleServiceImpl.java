package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.exception.DuplicateVinException;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.mapper.VehicleMapper;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional // Ensures all database operations in a method are completed as a single unit
@Slf4j
public class VehicleServiceImpl implements VehicleService {

  private final VehicleRepository vehicleRepository;

  // Using constructor injection is the recommended best practice
  public VehicleServiceImpl(VehicleRepository vehicleRepository) {
    this.vehicleRepository = vehicleRepository;
  }

  @Override
  public Vehicle registerVehicle(VehicleRequestDto vehicleRequest, String customerId) {
    log.info("Registering new vehicle for customer: {}", customerId);

    // Check if a vehicle with the same VIN already exists
    String normalizedVin = vehicleRequest.getVin().toUpperCase();
    Optional<Vehicle> existingVehicle = vehicleRepository.findByVin(normalizedVin);

    if (existingVehicle.isPresent()) {
      log.warn("Attempt to register duplicate VIN: {}", normalizedVin);
      throw new DuplicateVinException(normalizedVin);
    }

    // Create new vehicle entity from DTO
    Vehicle newVehicle = VehicleMapper.toEntity(vehicleRequest, customerId);

    // Save and return the new vehicle
    Vehicle savedVehicle = vehicleRepository.save(newVehicle);
    log.info("Successfully registered vehicle with ID: {} for customer: {}", savedVehicle.getId(), customerId);

    return savedVehicle;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Vehicle> getVehiclesForCustomer(String customerId) {
    log.info("Fetching all vehicles for customer: {}", customerId);
    return vehicleRepository.findByCustomerId(customerId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Vehicle> getAllVehicles() {
    log.info("Fetching all vehicles in the system");
    return vehicleRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Vehicle> getVehicleByIdAndCustomer(String id, String customerId) {
    log.info("Fetching vehicle {} for customer: {}", id, customerId);
    return vehicleRepository.findByIdAndCustomerId(id, customerId);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Vehicle> getVehicleById(String id) {
    log.info("Fetching vehicle {} (Admin/Employee access)", id);
    return vehicleRepository.findById(id);
  }

  @Override
  public Vehicle updateVehicle(String id, VehicleUpdateDto vehicleUpdate, String customerId) {
    Vehicle existingVehicle;

    // If customerId is null, it's an Admin/Employee request - no ownership check
    if (customerId == null) {
      log.info("Updating vehicle {} (Admin/Employee access)", id);
      existingVehicle = vehicleRepository.findById(id)
              .orElseThrow(() -> new VehicleNotFoundException(id, "admin"));
    } else {
      log.info("Updating vehicle {} for customer: {}", id, customerId);
      // Find the existing vehicle and verify ownership
      existingVehicle = vehicleRepository.findByIdAndCustomerId(id, customerId)
              .orElseThrow(() -> new VehicleNotFoundException(id, customerId));
    }

    // Update fields if provided in the DTO
    if (vehicleUpdate.getColor() != null) {
      existingVehicle.setColor(vehicleUpdate.getColor());
    }

    if (vehicleUpdate.getMileage() != null) {
      existingVehicle.setMileage(vehicleUpdate.getMileage());
    }

    if (vehicleUpdate.getLicensePlate() != null) {
      existingVehicle.setLicensePlate(vehicleUpdate.getLicensePlate());
    }

    // Save and return the updated vehicle
    Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
    log.info("Successfully updated vehicle: {}", id);

    return updatedVehicle;
  }

  @Override
  public void deleteVehicle(String id, String customerId) {
    Vehicle existingVehicle;

    // If customerId is null, it's an Admin/Employee request - no ownership check
    if (customerId == null) {
      log.info("Deleting vehicle {} (Admin/Employee access)", id);
      existingVehicle = vehicleRepository.findById(id)
              .orElseThrow(() -> new VehicleNotFoundException(id, "admin"));
    } else {
      log.info("Deleting vehicle {} for customer: {}", id, customerId);
      // Find the existing vehicle and verify ownership
      existingVehicle = vehicleRepository.findByIdAndCustomerId(id, customerId)
              .orElseThrow(() -> new VehicleNotFoundException(id, customerId));
    }

    // Delete the vehicle
    vehicleRepository.delete(existingVehicle);
    log.info("Successfully deleted vehicle: {}", id);
  }
}