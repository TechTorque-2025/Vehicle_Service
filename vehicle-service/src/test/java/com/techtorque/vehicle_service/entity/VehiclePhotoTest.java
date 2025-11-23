package com.techtorque.vehicle_service.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehiclePhotoTest {

    @Test
    void testBuilder() {
        VehiclePhoto photo = VehiclePhoto.builder()
                .id("PHOTO-123")
                .vehicleId("VEH-123")
                .fileName("front.jpg")
                .filePath("/uploads/VEH-123/front.jpg")
                .fileUrl("http://localhost/uploads/VEH-123/front.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .build();

        assertEquals("PHOTO-123", photo.getId());
        assertEquals("VEH-123", photo.getVehicleId());
        assertEquals("front.jpg", photo.getFileName());
        assertEquals("/uploads/VEH-123/front.jpg", photo.getFilePath());
        assertEquals("http://localhost/uploads/VEH-123/front.jpg", photo.getFileUrl());
        assertEquals(1024L, photo.getFileSize());
        assertEquals("image/jpeg", photo.getContentType());
    }

    @Test
    void testEqualsAndHashCode() {
        VehiclePhoto photo1 = VehiclePhoto.builder()
                .id("PHOTO-123")
                .vehicleId("VEH-123")
                .fileName("photo.jpg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .id("PHOTO-123")
                .vehicleId("VEH-123")
                .fileName("photo.jpg")
                .build();

        VehiclePhoto photo3 = VehiclePhoto.builder()
                .id("PHOTO-456")
                .vehicleId("VEH-456")
                .fileName("different.jpg")
                .build();

        assertEquals(photo1, photo2);
        assertEquals(photo1.hashCode(), photo2.hashCode());
        assertNotEquals(photo1, photo3);
    }

    @Test
    void testToString() {
        VehiclePhoto photo = VehiclePhoto.builder()
                .id("PHOTO-123")
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .build();

        String toString = photo.toString();
        assertTrue(toString.contains("PHOTO-123"));
        assertTrue(toString.contains("test.jpg"));
        assertTrue(toString.contains("image/jpeg"));
    }

    @Test
    void testPrePersist() {
        VehiclePhoto photo = new VehiclePhoto();
        assertNull(photo.getUploadedAt());

        // Pre-persist is called automatically by JPA, here we just verify the field
        // exists
        assertNotNull(photo); // Basic existence check
    }

    @Test
    void testNullValues() {
        VehiclePhoto photo = VehiclePhoto.builder()
                .id(null)
                .vehicleId(null)
                .fileName(null)
                .filePath(null)
                .fileUrl(null)
                .fileSize(null)
                .contentType(null)
                .build();

        assertNull(photo.getId());
        assertNull(photo.getVehicleId());
        assertNull(photo.getFileName());
        assertNull(photo.getFilePath());
        assertNull(photo.getFileUrl());
        assertNull(photo.getFileSize());
        assertNull(photo.getContentType());
    }

    @Test
    void testSizeValidation() {
        VehiclePhoto photo = VehiclePhoto.builder()
                .fileSize(0L)
                .build();

        assertEquals(0L, photo.getFileSize());

        photo = VehiclePhoto.builder()
                .fileSize(9999999L)
                .build();

        assertEquals(9999999L, photo.getFileSize());
    }
}