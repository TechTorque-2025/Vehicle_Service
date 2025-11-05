package com.techtorque.vehicle_service.service;

import com.techtorque.vehicle_service.dto.PhotoUploadResponseDto;
import org.springframework.web.multipart.MultipartFile;

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
     * Deletes all photos associated with a vehicle.
     *
     * @param vehicleId The ID of the vehicle.
     */
    void deleteVehiclePhotos(String vehicleId);
}
