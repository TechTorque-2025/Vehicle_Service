package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.exception.DuplicateVinException;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class VehicleServiceImpl implements VehicleService {

  private final VehicleRepository vehicleRepository;

  public VehicleServiceImpl(VehicleRepository vehicleRepository) {
    this.vehicleRepository = vehicleRepository;
  }

  @Override
  public Vehicle registerVehicle(VehicleRequestDto vehicleRequest, String customerId) {
    log.info("Registering new vehicle for customer: {}", customerId);

    // Check if VIN already exists
    Optional<Vehicle> existingVehicle = vehicleRepository.findByVin(vehicleRequest.getVin());
    if (existingVehicle.isPresent()) {
      log.warn("Attempt to register vehicle with duplicate VIN: {}", vehicleRequest.getVin());
      throw new DuplicateVinException("A vehicle with VIN " + vehicleRequest.getVin() + " already exists");
    }

    // Create new vehicle entity
    Vehicle newVehicle = Vehicle.builder()
            .customerId(customerId)
            .make(vehicleRequest.getMake())
            .model(vehicleRequest.getModel())
            .year(vehicleRequest.getYear())
            .vin(vehicleRequest.getVin().toUpperCase())
            .licensePlate(vehicleRequest.getLicensePlate().toUpperCase())
            .color(vehicleRequest.getColor())
            .mileage(vehicleRequest.getMileage() != null ? vehicleRequest.getMileage() : 0)
            .build();

    Vehicle savedVehicle = vehicleRepository.save(newVehicle);
    log.info("Successfully registered vehicle with ID: {} for customer: {}", savedVehicle.getId(), customerId);

    return savedVehicle;
  }

  @Override
  public List<Vehicle> getVehiclesForCustomer(String customerId) {
    log.info("Fetching all vehicles for customer: {}", customerId);
    return vehicleRepository.findByCustomerId(customerId);
  }

  @Override
  public Optional<Vehicle> getVehicleByIdAndCustomer(String id, String customerId) {
    log.info("Fetching vehicle {} for customer: {}", id, customerId);
    return vehicleRepository.findByIdAndCustomerId(id, customerId);
  }

  @Override
  public Vehicle updateVehicle(String id, VehicleUpdateDto vehicleUpdate, String customerId) {
    log.info("Updating vehicle {} for customer: {}", id, customerId);

    // Find existing vehicle and verify ownership
    Vehicle existingVehicle = getVehicleByIdAndCustomer(id, customerId)
            .orElseThrow(() -> {
              log.warn("Vehicle {} not found for customer: {}", id, customerId);
              return new VehicleNotFoundException("Vehicle not found or you don't have permission to update it");
            });

    // Update only provided fields
    if (vehicleUpdate.getLicensePlate() != null) {
      existingVehicle.setLicensePlate(vehicleUpdate.getLicensePlate().toUpperCase());
    }
    if (vehicleUpdate.getColor() != null) {
      existingVehicle.setColor(vehicleUpdate.getColor());
    }
    if (vehicleUpdate.getMileage() != null) {
      existingVehicle.setMileage(vehicleUpdate.getMileage());
    }

    Vehicle updatedVehicle = vehicleRepository.save(existingVehicle);
    log.info("Successfully updated vehicle: {}", id);

    return updatedVehicle;
  }

  @Override
  public void deleteVehicle(String id, String customerId) {
    log.info("Deleting vehicle {} for customer: {}", id, customerId);

    // Find existing vehicle and verify ownership
    Vehicle existingVehicle = getVehicleByIdAndCustomer(id, customerId)
            .orElseThrow(() -> {
              log.warn("Vehicle {} not found for customer: {}", id, customerId);
              return new VehicleNotFoundException("Vehicle not found or you don't have permission to delete it");
            });

    vehicleRepository.delete(existingVehicle);
    log.info("Successfully deleted vehicle: {}", id);
  }
}