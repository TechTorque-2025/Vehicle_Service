package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.PhotoUploadResponseDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import com.techtorque.vehicle_service.exception.PhotoUploadException;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.repository.VehiclePhotoRepository;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.PhotoStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class PhotoStorageServiceImpl implements PhotoStorageService {

    private final VehicleRepository vehicleRepository;
    private final VehiclePhotoRepository photoRepository;
    private final Path uploadDirectory;

    public PhotoStorageServiceImpl(
            VehicleRepository vehicleRepository,
            VehiclePhotoRepository photoRepository,
            @Value("${vehicle.photo.upload-dir:uploads/vehicle-photos}") String uploadDir) {
        this.vehicleRepository = vehicleRepository;
        this.photoRepository = photoRepository;
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadDirectory);
            log.info("Photo upload directory created/verified at: {}", this.uploadDirectory);
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
            throw new PhotoUploadException("Could not create upload directory", e);
        }
    }

    @Override
    public PhotoUploadResponseDto uploadVehiclePhotos(String vehicleId, MultipartFile[] files, String customerId) {
        log.info("Uploading {} photos for vehicle: {}", files.length, vehicleId);

        // Verify vehicle exists and belongs to customer
        Vehicle vehicle = vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, customerId));

        List<String> photoIds = new ArrayList<>();
        List<String> photoUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            try {
                // Validate file type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new PhotoUploadException("File must be an image: " + file.getOriginalFilename());
                }

                // Generate unique filename
                String originalFilename = file.getOriginalFilename();
                String fileExtension = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : ".jpg";
                String uniqueFilename = vehicleId + "_" + UUID.randomUUID() + fileExtension;

                // Create vehicle-specific subdirectory
                Path vehiclePhotoDir = uploadDirectory.resolve(vehicleId);
                Files.createDirectories(vehiclePhotoDir);

                // Save file to disk
                Path targetLocation = vehiclePhotoDir.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                // Create photo record in database
                String photoUrl = "/api/v1/vehicles/" + vehicleId + "/photos/" + uniqueFilename;
                VehiclePhoto photo = VehiclePhoto.builder()
                        .vehicleId(vehicleId)
                        .fileName(uniqueFilename)
                        .filePath(targetLocation.toString())
                        .fileUrl(photoUrl)
                        .fileSize(file.getSize())
                        .contentType(contentType)
                        .build();

                VehiclePhoto savedPhoto = photoRepository.save(photo);
                photoIds.add(savedPhoto.getId());
                photoUrls.add(savedPhoto.getFileUrl());

                log.info("Successfully uploaded photo: {} for vehicle: {}", uniqueFilename, vehicleId);

            } catch (IOException e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                throw new PhotoUploadException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }

        return PhotoUploadResponseDto.builder()
                .photoIds(photoIds)
                .urls(photoUrls)
                .build();
    }

    @Override
    public void deleteVehiclePhotos(String vehicleId) {
        log.info("Deleting all photos for vehicle: {}", vehicleId);

        List<VehiclePhoto> photos = photoRepository.findByVehicleId(vehicleId);

        for (VehiclePhoto photo : photos) {
            try {
                // Delete physical file
                Path filePath = Paths.get(photo.getFilePath());
                Files.deleteIfExists(filePath);
                log.info("Deleted photo file: {}", photo.getFileName());
            } catch (IOException e) {
                log.warn("Could not delete photo file: {}", photo.getFilePath(), e);
            }
        }

        // Delete database records
        photoRepository.deleteByVehicleId(vehicleId);
        log.info("Deleted {} photo records for vehicle: {}", photos.size(), vehicleId);
    }
}
