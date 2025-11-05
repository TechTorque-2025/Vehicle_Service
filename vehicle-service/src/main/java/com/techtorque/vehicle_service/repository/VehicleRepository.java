package com.techtorque.vehicle_service.repository;

import com.techtorque.vehicle_service.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

  /**
   * Finds all vehicles associated with a specific customer ID.
   * Spring Data JPA will automatically generate the query for this method
   * based on the method name.
   *
   * @param customerId The UUID of the customer.
   * @return A list of vehicles belonging to the customer.
   */
  List<Vehicle> findByCustomerId(String customerId);

  /**
   * Finds a vehicle by its unique Vehicle Identification Number (VIN).
   *
   * @param vin The VIN of the vehicle.
   * @return An Optional containing the vehicle if found, otherwise empty.
   */
  Optional<Vehicle> findByVin(String vin);

  /**
   * Finds a specific vehicle by its ID and ensures it belongs to the specified customer.
   * This is crucial for security to prevent users from accessing other users' vehicle data.
   *
   * @param id The UUID of the vehicle.
   * @param customerId The UUID of the customer who should own the vehicle.
   * @return An Optional containing the vehicle if found and owned by the customer.
   */
  Optional<Vehicle> findByIdAndCustomerId(String id, String customerId);

  /**
   * Counts the number of vehicles associated with a specific customer ID.
   *
   * @param customerId The UUID of the customer.
   * @return The count of vehicles belonging to the customer.
   */
  long countByCustomerId(String customerId);

}