package com.techtorque.vehicle_service.service;

import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.entity.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleService {

  /**
   * Creates and saves a new vehicle for a given customer.
   * @param vehicleRequest The DTO containing the new vehicle's data.
   * @param customerId The ID of the user who owns this vehicle.
   * @return The newly created Vehicle entity.
   */
  Vehicle registerVehicle(VehicleRequestDto vehicleRequest, String customerId);

  /**
   * Retrieves all vehicles belonging to a specific customer.
   * @param customerId The ID of the customer.
   * @return A list of vehicles.
   */
  List<Vehicle> getVehiclesForCustomer(String customerId);

  /**
   * Retrieves all vehicles in the system (Admin/Employee only).
   * @return A list of all vehicles.
   */
  List<Vehicle> getAllVehicles();

  /**
   * Retrieves a single vehicle by its ID, ensuring it belongs to the specified customer.
   * @param id The ID of the vehicle.
   * @param customerId The ID of the owning customer.
   * @return An Optional containing the vehicle if found and ownership is verified.
   */
  Optional<Vehicle> getVehicleByIdAndCustomer(String id, String customerId);

  /**
   * Updates an existing vehicle's information.
   * @param id The ID of the vehicle to update.
   * @param vehicleUpdate The DTO with the fields to update.
   * @param customerId The ID of the owning customer.
   * @return The updated Vehicle entity.
   */
  Vehicle updateVehicle(String id, VehicleUpdateDto vehicleUpdate, String customerId);

  /**
   * Deletes a vehicle after verifying ownership.
   * @param id The ID of the vehicle to delete.
   * @param customerId The ID of the owning customer.
   */
  void deleteVehicle(String id, String customerId);
}