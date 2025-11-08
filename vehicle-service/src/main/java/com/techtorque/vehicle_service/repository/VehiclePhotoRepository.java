package com.techtorque.vehicle_service.repository;

import com.techtorque.vehicle_service.entity.VehiclePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiclePhotoRepository extends JpaRepository<VehiclePhoto, String> {

    /**
     * Finds all photos associated with a specific vehicle.
     *
     * @param vehicleId The UUID of the vehicle.
     * @return A list of photos for the vehicle.
     */
    List<VehiclePhoto> findByVehicleId(String vehicleId);

    /**
     * Deletes all photos associated with a specific vehicle.
     * Useful when deleting a vehicle.
     *
     * @param vehicleId The UUID of the vehicle.
     */
    void deleteByVehicleId(String vehicleId);
}
