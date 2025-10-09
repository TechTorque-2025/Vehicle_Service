package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional // Ensures all database operations in a method are completed as a single unit
public class VehicleServiceImpl implements VehicleService {

  private final VehicleRepository vehicleRepository;

  // Using constructor injection is the recommended best practice
  public VehicleServiceImpl(VehicleRepository vehicleRepository) {
    this.vehicleRepository = vehicleRepository;
  }

  @Override
  public Vehicle registerVehicle(/*VehicleRequestDto vehicleRequest,*/ String customerId) {
    // TODO: Developer will implement this logic.
    // 1. Check if a vehicle with the same VIN already exists using vehicleRepository.findByVin().
    //    If it exists, throw a custom exception (e.g., DuplicateVinException).
    // 2. Create a new Vehicle entity from the DTO data.
    // 3. Set the customerId on the new entity.
    // 4. Save the entity using vehicleRepository.save(newVehicle).
    // 5. Return the saved entity.
    return null; // Placeholder
  }

  @Override
  public List<Vehicle> getVehiclesForCustomer(String customerId) {
    // TODO: Developer will implement this logic.
    // 1. Simply call vehicleRepository.findByCustomerId(customerId).
    // 2. Return the result.
    return List.of(); // Placeholder
  }

  @Override
  public Optional<Vehicle> getVehicleByIdAndCustomer(String id, String customerId) {
    // TODO: Developer will implement this logic.
    // 1. Call the security-focused repository method: vehicleRepository.findByIdAndCustomerId(id, customerId).
    // 2. Return the resulting Optional. The controller will handle the case where it's empty.
    return Optional.empty(); // Placeholder
  }

  @Override
  public Vehicle updateVehicle(String id, /*VehicleUpdateDto vehicleUpdate,*/ String customerId) {
    // TODO: Developer will implement this logic.
    // 1. Find the existing vehicle using getVehicleByIdAndCustomer(id, customerId).
    // 2. If the vehicle is not found, throw an exception (e.g., VehicleNotFoundException).
    // 3. Update the fields of the existing vehicle entity from the DTO.
    // 4. Save the updated entity using vehicleRepository.save(existingVehicle).
    // 5. Return the updated entity.
    return null; // Placeholder
  }

  @Override
  public void deleteVehicle(String id, String customerId) {
    // TODO: Developer will implement this logic.
    // 1. Find the existing vehicle using getVehicleByIdAndCustomer(id, customerId).
    // 2. If the vehicle is not found, throw an exception.
    // 3. If found, delete it using vehicleRepository.delete(existingVehicle).
  }
}