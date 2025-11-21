package com.techtorque.vehicle_service.repository;

import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class VehiclePhotoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VehiclePhotoRepository vehiclePhotoRepository;

    @Test
    void testFindByVehicleId() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .id("VEH-123")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .build();

        VehiclePhoto photo1 = VehiclePhoto.builder()
                .vehicleId("VEH-123")
                .fileName("front.jpg")
                .filePath("/uploads/VEH-123/front.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/front.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .vehicleId("VEH-123")
                .fileName("side.jpg")
                .filePath("/uploads/VEH-123/side.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/side.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo3 = VehiclePhoto.builder()
                .vehicleId("VEH-456") // Different vehicle
                .fileName("back.jpg")
                .filePath("/uploads/VEH-456/back.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-456/photos/back.jpg")
                .fileSize(1536L)
                .contentType("image/jpeg")
                .build();

        entityManager.persistAndFlush(vehicle);
        entityManager.persistAndFlush(photo1);
        entityManager.persistAndFlush(photo2);
        entityManager.persistAndFlush(photo3);

        // When
        List<VehiclePhoto> photos = vehiclePhotoRepository.findByVehicleId("VEH-123");

        // Then
        assertEquals(2, photos.size());
        assertTrue(photos.stream().anyMatch(p -> p.getFileName().equals("front.jpg")));
        assertTrue(photos.stream().anyMatch(p -> p.getFileName().equals("side.jpg")));
        assertFalse(photos.stream().anyMatch(p -> p.getFileName().equals("back.jpg")));
    }

    @Test
    void testFindByVehicleIdWithNoPhotos() {
        // When
        List<VehiclePhoto> photos = vehiclePhotoRepository.findByVehicleId("VEH-NONEXISTENT");

        // Then
        assertEquals(0, photos.size());
        assertTrue(photos.isEmpty());
    }

    @Test
    void testDeleteByVehicleId() {
        // Given
        VehiclePhoto photo1 = VehiclePhoto.builder()
                .vehicleId("VEH-DELETE")
                .fileName("photo1.jpg")
                .filePath("/uploads/VEH-DELETE/photo1.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-DELETE/photos/photo1.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .vehicleId("VEH-DELETE")
                .fileName("photo2.jpg")
                .filePath("/uploads/VEH-DELETE/photo2.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-DELETE/photos/photo2.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo3 = VehiclePhoto.builder()
                .vehicleId("VEH-KEEP")
                .fileName("photo3.jpg")
                .filePath("/uploads/VEH-KEEP/photo3.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-KEEP/photos/photo3.jpg")
                .fileSize(1536L)
                .contentType("image/jpeg")
                .build();

        entityManager.persistAndFlush(photo1);
        entityManager.persistAndFlush(photo2);
        entityManager.persistAndFlush(photo3);

        // Verify photos exist
        List<VehiclePhoto> beforeDelete = vehiclePhotoRepository.findByVehicleId("VEH-DELETE");
        assertEquals(2, beforeDelete.size());

        List<VehiclePhoto> beforeDeleteKeep = vehiclePhotoRepository.findByVehicleId("VEH-KEEP");
        assertEquals(1, beforeDeleteKeep.size());

        // When
        vehiclePhotoRepository.deleteByVehicleId("VEH-DELETE");
        entityManager.flush();

        // Then
        List<VehiclePhoto> afterDelete = vehiclePhotoRepository.findByVehicleId("VEH-DELETE");
        assertEquals(0, afterDelete.size());

        List<VehiclePhoto> afterDeleteKeep = vehiclePhotoRepository.findByVehicleId("VEH-KEEP");
        assertEquals(1, afterDeleteKeep.size()); // Should remain unchanged
    }

    @Test
    void testSaveAndFindById() {
        // Given
        VehiclePhoto photo = VehiclePhoto.builder()
                .vehicleId("VEH-789")
                .fileName("test.png")
                .filePath("/uploads/VEH-789/test.png")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-789/photos/test.png")
                .fileSize(4096L)
                .contentType("image/png")
                .build();

        // When
        VehiclePhoto saved = vehiclePhotoRepository.save(photo);

        // Then
        assertNotNull(saved.getId());
        assertNotNull(saved.getUploadedAt());

        Optional<VehiclePhoto> found = vehiclePhotoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("VEH-789", found.get().getVehicleId());
        assertEquals("test.png", found.get().getFileName());
        assertEquals(4096L, found.get().getFileSize());
        assertEquals("image/png", found.get().getContentType());
    }

    @Test
    void testFindByVehicleIdOrderByUploadedAt() {
        // Given
        VehiclePhoto photo1 = VehiclePhoto.builder()
                .vehicleId("VEH-ORDER")
                .fileName("first.jpg")
                .filePath("/uploads/VEH-ORDER/first.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-ORDER/photos/first.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .vehicleId("VEH-ORDER")
                .fileName("second.jpg")
                .filePath("/uploads/VEH-ORDER/second.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-ORDER/photos/second.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .build();

        // Save in specific order
        VehiclePhoto savedFirst = vehiclePhotoRepository.save(photo1);
        entityManager.flush();

        // Small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        VehiclePhoto savedSecond = vehiclePhotoRepository.save(photo2);
        entityManager.flush();

        // When
        List<VehiclePhoto> photos = vehiclePhotoRepository.findByVehicleId("VEH-ORDER");

        // Then
        assertEquals(2, photos.size());

        // Verify first photo was uploaded before second
        assertTrue(savedFirst.getUploadedAt().isBefore(savedSecond.getUploadedAt()) ||
                savedFirst.getUploadedAt().isEqual(savedSecond.getUploadedAt()));
    }

    @Test
    void testDeleteSinglePhoto() {
        // Given
        VehiclePhoto photo = VehiclePhoto.builder()
                .vehicleId("VEH-SINGLE")
                .fileName("single.jpg")
                .filePath("/uploads/VEH-SINGLE/single.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-SINGLE/photos/single.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto saved = vehiclePhotoRepository.save(photo);
        entityManager.flush();

        // Verify it exists
        Optional<VehiclePhoto> beforeDelete = vehiclePhotoRepository.findById(saved.getId());
        assertTrue(beforeDelete.isPresent());

        // When
        vehiclePhotoRepository.delete(saved);
        entityManager.flush();

        // Then
        Optional<VehiclePhoto> afterDelete = vehiclePhotoRepository.findById(saved.getId());
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void testFindAllPhotos() {
        // Given
        VehiclePhoto photo1 = VehiclePhoto.builder()
                .vehicleId("VEH-ALL-1")
                .fileName("all1.jpg")
                .filePath("/uploads/VEH-ALL-1/all1.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-ALL-1/photos/all1.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .vehicleId("VEH-ALL-2")
                .fileName("all2.jpg")
                .filePath("/uploads/VEH-ALL-2/all2.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-ALL-2/photos/all2.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .build();

        entityManager.persistAndFlush(photo1);
        entityManager.persistAndFlush(photo2);

        // When
        List<VehiclePhoto> allPhotos = vehiclePhotoRepository.findAll();

        // Then
        assertTrue(allPhotos.size() >= 2);
        assertTrue(allPhotos.stream().anyMatch(p -> p.getFileName().equals("all1.jpg")));
        assertTrue(allPhotos.stream().anyMatch(p -> p.getFileName().equals("all2.jpg")));
    }

    @Test
    void testPhotoWithDifferentContentTypes() {
        // Given
        VehiclePhoto jpegPhoto = VehiclePhoto.builder()
                .vehicleId("VEH-TYPES")
                .fileName("image.jpg")
                .filePath("/uploads/VEH-TYPES/image.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-TYPES/photos/image.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto pngPhoto = VehiclePhoto.builder()
                .vehicleId("VEH-TYPES")
                .fileName("image.png")
                .filePath("/uploads/VEH-TYPES/image.png")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-TYPES/photos/image.png")
                .fileSize(2048L)
                .contentType("image/png")
                .build();

        VehiclePhoto webpPhoto = VehiclePhoto.builder()
                .vehicleId("VEH-TYPES")
                .fileName("image.webp")
                .filePath("/uploads/VEH-TYPES/image.webp")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-TYPES/photos/image.webp")
                .fileSize(1536L)
                .contentType("image/webp")
                .build();

        // When
        vehiclePhotoRepository.save(jpegPhoto);
        vehiclePhotoRepository.save(pngPhoto);
        vehiclePhotoRepository.save(webpPhoto);
        entityManager.flush();

        // Then
        List<VehiclePhoto> photos = vehiclePhotoRepository.findByVehicleId("VEH-TYPES");
        assertEquals(3, photos.size());

        assertTrue(photos.stream().anyMatch(p -> p.getContentType().equals("image/jpeg")));
        assertTrue(photos.stream().anyMatch(p -> p.getContentType().equals("image/png")));
        assertTrue(photos.stream().anyMatch(p -> p.getContentType().equals("image/webp")));
    }
}