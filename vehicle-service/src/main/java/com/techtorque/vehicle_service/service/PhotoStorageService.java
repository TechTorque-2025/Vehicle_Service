package com.techtorque.vehicle_service.service;

import com.techtorque.vehicle_service.dto.PhotoUploadResponseDto;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoStorageService {

    /**
     * Uploads photos for a specific vehicle.
     *
     * @param vehicleId The ID of the vehicle.
     * @param files The array of photo files to upload.
     * @param customerId The ID of the customer (for ownership verification).
     * @return Response containing photo IDs and URLs.
     */
    PhotoUploadResponseDto uploadVehiclePhotos(String vehicleId, MultipartFile[] files, String customerId);

    /**
     * Gets all photos for a specific vehicle.
     *
     * @param vehicleId The ID of the vehicle.
     * @param customerId The ID of the customer (for ownership verification).
     * @return List of vehicle photos.
     */
    List<VehiclePhoto> getVehiclePhotos(String vehicleId, String customerId);

    /**
     * Gets a specific photo by ID.
     *
     * @param photoId The ID of the photo.
     * @param customerId The ID of the customer (for ownership verification).
     * @return The vehicle photo.
     */
    VehiclePhoto getPhotoById(String photoId, String customerId);

    /**
     * Loads a photo file as a resource for serving.
     *
     * @param vehicleId The ID of the vehicle.
     * @param fileName The name of the file.
     * @param customerId The ID of the customer (for ownership verification).
     * @return Resource containing the photo file.
     */
    Resource loadPhotoAsResource(String vehicleId, String fileName, String customerId);

    /**
     * Deletes a single photo by ID.
     *
     * @param photoId The ID of the photo.
     * @param customerId The ID of the customer (for ownership verification).
     */
    void deleteSinglePhoto(String photoId, String customerId);

    /**
     * Deletes all photos associated with a vehicle.
     *
     * @param vehicleId The ID of the vehicle.
     */
    void deleteVehiclePhotos(String vehicleId);
}
