package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.PhotoUploadResponseDto;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import com.techtorque.vehicle_service.exception.PhotoUploadException;
import com.techtorque.vehicle_service.exception.UnauthorizedVehicleAccessException;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.repository.VehiclePhotoRepository;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.PhotoStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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
        vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
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
    @Transactional(readOnly = true)
    public List<VehiclePhoto> getVehiclePhotos(String vehicleId, String customerId) {
        log.info("Fetching photos for vehicle: {}", vehicleId);

        // Verify vehicle exists and belongs to customer
        vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, customerId));

        return photoRepository.findByVehicleId(vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public VehiclePhoto getPhotoById(String photoId, String customerId) {
        log.info("Fetching photo with ID: {}", photoId);

        VehiclePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoUploadException("Photo not found with ID: " + photoId));

        // Verify ownership
        vehicleRepository.findByIdAndCustomerId(photo.getVehicleId(), customerId)
                .orElseThrow(() -> new UnauthorizedVehicleAccessException(photo.getVehicleId(), customerId));

        return photo;
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadPhotoAsResource(String vehicleId, String fileName, String customerId) {
        log.info("Loading photo file: {} for vehicle: {}", fileName, vehicleId);

        // Verify vehicle exists and belongs to customer
        vehicleRepository.findByIdAndCustomerId(vehicleId, customerId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId, customerId));

        try {
            Path vehiclePhotoDir = uploadDirectory.resolve(vehicleId);
            Path filePath = vehiclePhotoDir.resolve(fileName).normalize();

            // Security check: ensure file is within allowed directory
            if (!filePath.startsWith(vehiclePhotoDir)) {
                throw new PhotoUploadException("Invalid file path");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new PhotoUploadException("Photo file not found or not readable: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new PhotoUploadException("Photo file not found: " + fileName, e);
        }
    }

    @Override
    public void deleteSinglePhoto(String photoId, String customerId) {
        log.info("Deleting photo with ID: {}", photoId);

        VehiclePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new PhotoUploadException("Photo not found with ID: " + photoId));

        // Verify ownership
        vehicleRepository.findByIdAndCustomerId(photo.getVehicleId(), customerId)
                .orElseThrow(() -> new UnauthorizedVehicleAccessException(photo.getVehicleId(), customerId));

        try {
            // Delete physical file
            Path filePath = Paths.get(photo.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Deleted photo file: {}", photo.getFileName());
        } catch (IOException e) {
            log.warn("Could not delete photo file: {}", photo.getFilePath(), e);
        }

        // Delete database record
        photoRepository.delete(photo);
        log.info("Deleted photo record with ID: {}", photoId);
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
