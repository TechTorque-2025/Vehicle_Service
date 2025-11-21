package com.techtorque.vehicle_service.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class VehiclePhotoEntityTest {

    private VehiclePhoto vehiclePhoto;

    @BeforeEach
    void setUp() {
        vehiclePhoto = VehiclePhoto.builder()
                .vehicleId("VEH-123")
                .fileName("test_image.jpg")
                .filePath("/uploads/vehicle-photos/VEH-123/test_image.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/test_image.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();
    }

    @Test
    void testVehiclePhotoCreation() {
        assertNotNull(vehiclePhoto);
        assertEquals("VEH-123", vehiclePhoto.getVehicleId());
        assertEquals("test_image.jpg", vehiclePhoto.getFileName());
        assertEquals("/uploads/vehicle-photos/VEH-123/test_image.jpg", vehiclePhoto.getFilePath());
        assertEquals("http://localhost:8082/api/v1/vehicles/VEH-123/photos/test_image.jpg", vehiclePhoto.getFileUrl());
        assertEquals(1024L, vehiclePhoto.getFileSize());
        assertEquals("image/jpeg", vehiclePhoto.getContentType());
    }

    @Test
    void testGenerateId() {
        // Simulate @PrePersist behavior
        vehiclePhoto.generateId();

        assertNotNull(vehiclePhoto.getId());
        assertTrue(vehiclePhoto.getId().startsWith("PHOTO-"));
        assertTrue(vehiclePhoto.getId().length() > 6); // PHOTO- prefix + UUID portion
    }

    @Test
    void testGenerateIdDoesNotOverrideExisting() {
        String existingId = "PHOTO-CUSTOM-ID";
        vehiclePhoto.setId(existingId);
        vehiclePhoto.generateId();

        assertEquals(existingId, vehiclePhoto.getId());
    }

    @Test
    void testBuilderPattern() {
        VehiclePhoto photo = VehiclePhoto.builder()
                .id("PHOTO-456")
                .vehicleId("VEH-456")
                .fileName("another_image.png")
                .filePath("/uploads/vehicle-photos/VEH-456/another_image.png")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-456/photos/another_image.png")
                .fileSize(2048L)
                .contentType("image/png")
                .build();

        assertEquals("PHOTO-456", photo.getId());
        assertEquals("VEH-456", photo.getVehicleId());
        assertEquals("another_image.png", photo.getFileName());
        assertEquals(2048L, photo.getFileSize());
        assertEquals("image/png", photo.getContentType());
    }

    @Test
    void testEqualsAndHashCode() {
        VehiclePhoto photo1 = VehiclePhoto.builder()
                .id("PHOTO-1")
                .vehicleId("VEH-123")
                .fileName("test.jpg")
                .filePath("/path/test.jpg")
                .fileUrl("http://test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .id("PHOTO-1")
                .vehicleId("VEH-123")
                .fileName("test.jpg")
                .filePath("/path/test.jpg")
                .fileUrl("http://test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        assertEquals(photo1, photo2);
        assertEquals(photo1.hashCode(), photo2.hashCode());
    }

    @Test
    void testToString() {
        String toString = vehiclePhoto.toString();

        assertTrue(toString.contains("VEH-123"));
        assertTrue(toString.contains("test_image.jpg"));
        assertTrue(toString.contains("image/jpeg"));
        assertTrue(toString.contains("1024"));
    }

    @Test
    void testNoArgsConstructor() {
        VehiclePhoto emptyPhoto = new VehiclePhoto();
        assertNotNull(emptyPhoto);
        assertNull(emptyPhoto.getId());
        assertNull(emptyPhoto.getVehicleId());
        assertNull(emptyPhoto.getFileName());
        assertEquals(0L, emptyPhoto.getFileSize());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        VehiclePhoto photo = new VehiclePhoto(
                "PHOTO-789",
                "VEH-789",
                "constructor_test.jpg",
                "/path/constructor_test.jpg",
                "http://constructor_test.jpg",
                512L,
                "image/jpeg",
                now);

        assertEquals("PHOTO-789", photo.getId());
        assertEquals("VEH-789", photo.getVehicleId());
        assertEquals("constructor_test.jpg", photo.getFileName());
        assertEquals(512L, photo.getFileSize());
        assertEquals(now, photo.getUploadedAt());
    }

    @Test
    void testDifferentContentTypes() {
        VehiclePhoto jpegPhoto = VehiclePhoto.builder()
                .fileName("image.jpg")
                .contentType("image/jpeg")
                .build();

        VehiclePhoto pngPhoto = VehiclePhoto.builder()
                .fileName("image.png")
                .contentType("image/png")
                .build();

        VehiclePhoto gifPhoto = VehiclePhoto.builder()
                .fileName("image.gif")
                .contentType("image/gif")
                .build();

        assertEquals("image/jpeg", jpegPhoto.getContentType());
        assertEquals("image/png", pngPhoto.getContentType());
        assertEquals("image/gif", gifPhoto.getContentType());
    }

    @Test
    void testFileSizeHandling() {
        VehiclePhoto smallPhoto = VehiclePhoto.builder()
                .fileSize(100L)
                .build();

        VehiclePhoto largePhoto = VehiclePhoto.builder()
                .fileSize(10_000_000L) // 10MB
                .build();

        assertEquals(100L, smallPhoto.getFileSize());
        assertEquals(10_000_000L, largePhoto.getFileSize());
    }
}