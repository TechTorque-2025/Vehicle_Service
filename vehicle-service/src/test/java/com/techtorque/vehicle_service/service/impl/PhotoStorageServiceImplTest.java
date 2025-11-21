package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.response.PhotoUploadResponseDto;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import com.techtorque.vehicle_service.exception.PhotoUploadException;
import com.techtorque.vehicle_service.repository.VehiclePhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoStorageServiceImplTest {

    @Mock
    private VehiclePhotoRepository vehiclePhotoRepository;

    @Mock
    private MultipartFile multipartFile1;

    @Mock
    private MultipartFile multipartFile2;

    @InjectMocks
    private PhotoStorageServiceImpl photoStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set the upload directory to temp directory
        ReflectionTestUtils.setField(photoStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void testStorePhotos_Success() throws IOException {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn("photo1.jpg");
        when(multipartFile1.getContentType()).thenReturn("image/jpeg");
        when(multipartFile1.getSize()).thenReturn(1024L);
        when(multipartFile1.getBytes()).thenReturn("fake image content 1".getBytes());

        when(multipartFile2.getOriginalFilename()).thenReturn("photo2.png");
        when(multipartFile2.getContentType()).thenReturn("image/png");
        when(multipartFile2.getSize()).thenReturn(2048L);
        when(multipartFile2.getBytes()).thenReturn("fake image content 2".getBytes());

        VehiclePhoto savedPhoto1 = VehiclePhoto.builder()
                .id("PHOTO-1")
                .vehicleId(vehicleId)
                .fileName("photo1.jpg")
                .build();

        VehiclePhoto savedPhoto2 = VehiclePhoto.builder()
                .id("PHOTO-2")
                .vehicleId(vehicleId)
                .fileName("photo2.png")
                .build();

        when(vehiclePhotoRepository.save(any(VehiclePhoto.class)))
                .thenReturn(savedPhoto1)
                .thenReturn(savedPhoto2);

        List<MultipartFile> files = Arrays.asList(multipartFile1, multipartFile2);

        // When
        PhotoUploadResponseDto result = photoStorageService.storePhotos(vehicleId, files);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getPhotoIds().size());
        assertEquals(2, result.getUrls().size());
        assertTrue(result.getPhotoIds().contains("PHOTO-1"));
        assertTrue(result.getPhotoIds().contains("PHOTO-2"));

        verify(vehiclePhotoRepository, times(2)).save(any(VehiclePhoto.class));

        // Verify files were created
        Path vehicleDir = tempDir.resolve("vehicle-photos").resolve(vehicleId);
        assertTrue(Files.exists(vehicleDir.resolve("photo1.jpg")));
        assertTrue(Files.exists(vehicleDir.resolve("photo2.png")));
    }

    @Test
    void testStorePhotos_InvalidFileType() {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile1.getContentType()).thenReturn("application/pdf");

        List<MultipartFile> files = Arrays.asList(multipartFile1);

        // When & Then
        assertThrows(PhotoUploadException.class, () -> photoStorageService.storePhotos(vehicleId, files));

        verify(vehiclePhotoRepository, never()).save(any());
    }

    @Test
    void testStorePhotos_NullContentType() {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile1.getContentType()).thenReturn(null);

        List<MultipartFile> files = Arrays.asList(multipartFile1);

        // When & Then
        assertThrows(PhotoUploadException.class, () -> photoStorageService.storePhotos(vehicleId, files));

        verify(vehiclePhotoRepository, never()).save(any());
    }

    @Test
    void testStorePhotos_EmptyFilename() {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn("");
        when(multipartFile1.getContentType()).thenReturn("image/jpeg");

        List<MultipartFile> files = Arrays.asList(multipartFile1);

        // When & Then
        assertThrows(PhotoUploadException.class, () -> photoStorageService.storePhotos(vehicleId, files));

        verify(vehiclePhotoRepository, never()).save(any());
    }

    @Test
    void testStorePhotos_NullFilename() {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn(null);
        when(multipartFile1.getContentType()).thenReturn("image/jpeg");

        List<MultipartFile> files = Arrays.asList(multipartFile1);

        // When & Then
        assertThrows(PhotoUploadException.class, () -> photoStorageService.storePhotos(vehicleId, files));

        verify(vehiclePhotoRepository, never()).save(any());
    }

    @Test
    void testStorePhotos_IOError() throws IOException {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn("photo1.jpg");
        when(multipartFile1.getContentType()).thenReturn("image/jpeg");
        when(multipartFile1.getSize()).thenReturn(1024L);
        when(multipartFile1.getBytes()).thenThrow(new IOException("IO Error"));

        List<MultipartFile> files = Arrays.asList(multipartFile1);

        // When & Then
        assertThrows(PhotoUploadException.class, () -> photoStorageService.storePhotos(vehicleId, files));

        verify(vehiclePhotoRepository, never()).save(any());
    }

    @Test
    void testGetPhotosForVehicle_Success() {
        // Given
        String vehicleId = "VEH-123";

        VehiclePhoto photo1 = VehiclePhoto.builder()
                .id("PHOTO-1")
                .vehicleId(vehicleId)
                .fileName("photo1.jpg")
                .filePath("/uploads/vehicle-photos/VEH-123/photo1.jpg")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo1.jpg")
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .id("PHOTO-2")
                .vehicleId(vehicleId)
                .fileName("photo2.png")
                .filePath("/uploads/vehicle-photos/VEH-123/photo2.png")
                .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo2.png")
                .build();

        when(vehiclePhotoRepository.findByVehicleId(vehicleId))
                .thenReturn(Arrays.asList(photo1, photo2));

        // When
        List<VehiclePhoto> result = photoStorageService.getPhotosForVehicle(vehicleId);

        // Then
        assertEquals(2, result.size());
        assertEquals("PHOTO-1", result.get(0).getId());
        assertEquals("PHOTO-2", result.get(1).getId());
        verify(vehiclePhotoRepository).findByVehicleId(vehicleId);
    }

    @Test
    void testGetPhotosForVehicle_EmptyList() {
        // Given
        String vehicleId = "VEH-123";
        when(vehiclePhotoRepository.findByVehicleId(vehicleId)).thenReturn(Arrays.asList());

        // When
        List<VehiclePhoto> result = photoStorageService.getPhotosForVehicle(vehicleId);

        // Then
        assertTrue(result.isEmpty());
        verify(vehiclePhotoRepository).findByVehicleId(vehicleId);
    }

    @Test
    void testDeletePhotosForVehicle_Success() throws IOException {
        // Given
        String vehicleId = "VEH-123";

        // Create test files
        Path vehicleDir = tempDir.resolve("vehicle-photos").resolve(vehicleId);
        Files.createDirectories(vehicleDir);
        Path photo1Path = vehicleDir.resolve("photo1.jpg");
        Path photo2Path = vehicleDir.resolve("photo2.png");
        Files.write(photo1Path, "test content 1".getBytes());
        Files.write(photo2Path, "test content 2".getBytes());

        VehiclePhoto photo1 = VehiclePhoto.builder()
                .id("PHOTO-1")
                .vehicleId(vehicleId)
                .fileName("photo1.jpg")
                .filePath(photo1Path.toString())
                .build();

        VehiclePhoto photo2 = VehiclePhoto.builder()
                .id("PHOTO-2")
                .vehicleId(vehicleId)
                .fileName("photo2.png")
                .filePath(photo2Path.toString())
                .build();

        when(vehiclePhotoRepository.findByVehicleId(vehicleId))
                .thenReturn(Arrays.asList(photo1, photo2));

        // Verify files exist before deletion
        assertTrue(Files.exists(photo1Path));
        assertTrue(Files.exists(photo2Path));

        // When
        photoStorageService.deletePhotosForVehicle(vehicleId);

        // Then
        verify(vehiclePhotoRepository).findByVehicleId(vehicleId);
        verify(vehiclePhotoRepository).deleteByVehicleId(vehicleId);

        // Verify files are deleted
        assertFalse(Files.exists(photo1Path));
        assertFalse(Files.exists(photo2Path));
    }

    @Test
    void testDeletePhotosForVehicle_NoPhotos() {
        // Given
        String vehicleId = "VEH-123";
        when(vehiclePhotoRepository.findByVehicleId(vehicleId)).thenReturn(Arrays.asList());

        // When
        photoStorageService.deletePhotosForVehicle(vehicleId);

        // Then
        verify(vehiclePhotoRepository).findByVehicleId(vehicleId);
        verify(vehiclePhotoRepository).deleteByVehicleId(vehicleId);
    }

    @Test
    void testValidateImageFile_ValidTypes() {
        // Valid image types should not throw exception
        assertDoesNotThrow(() -> photoStorageService.validateImageFile("image/jpeg", "photo.jpg"));
        assertDoesNotThrow(() -> photoStorageService.validateImageFile("image/png", "photo.png"));
        assertDoesNotThrow(() -> photoStorageService.validateImageFile("image/gif", "photo.gif"));
        assertDoesNotThrow(() -> photoStorageService.validateImageFile("image/webp", "photo.webp"));
        assertDoesNotThrow(() -> photoStorageService.validateImageFile("image/bmp", "photo.bmp"));
    }

    @Test
    void testValidateImageFile_InvalidTypes() {
        // Invalid types should throw exception
        assertThrows(PhotoUploadException.class,
                () -> photoStorageService.validateImageFile("application/pdf", "document.pdf"));

        assertThrows(PhotoUploadException.class, () -> photoStorageService.validateImageFile("text/plain", "text.txt"));

        assertThrows(PhotoUploadException.class, () -> photoStorageService.validateImageFile("video/mp4", "video.mp4"));
    }

    @Test
    void testValidateImageFile_NullValues() {
        assertThrows(PhotoUploadException.class, () -> photoStorageService.validateImageFile(null, "photo.jpg"));

        assertThrows(PhotoUploadException.class, () -> photoStorageService.validateImageFile("image/jpeg", null));

        assertThrows(PhotoUploadException.class, () -> photoStorageService.validateImageFile("image/jpeg", ""));
    }

    @Test
    void testCreateVehiclePhotoEntity() {
        // Given
        String vehicleId = "VEH-123";
        String fileName = "test.jpg";
        String filePath = "/uploads/vehicle-photos/VEH-123/test.jpg";
        long fileSize = 1024L;
        String contentType = "image/jpeg";

        // When
        VehiclePhoto result = photoStorageService.createVehiclePhotoEntity(
                vehicleId, fileName, filePath, fileSize, contentType);

        // Then
        assertNotNull(result);
        assertEquals(vehicleId, result.getVehicleId());
        assertEquals(fileName, result.getFileName());
        assertEquals(filePath, result.getFilePath());
        assertEquals(fileSize, result.getFileSize());
        assertEquals(contentType, result.getContentType());
        assertTrue(result.getFileUrl().contains("vehicles/" + vehicleId + "/photos/" + fileName));
    }

    @Test
    void testGenerateUniqueFileName() {
        // Given
        String originalName = "photo.jpg";

        // When
        String result1 = photoStorageService.generateUniqueFileName(originalName);
        String result2 = photoStorageService.generateUniqueFileName(originalName);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1, result2); // Should generate unique names
        assertTrue(result1.endsWith(".jpg"));
        assertTrue(result2.endsWith(".jpg"));
        assertTrue(result1.length() > originalName.length()); // Should have timestamp prefix
    }

    @Test
    void testGenerateUniqueFileName_NoExtension() {
        // Given
        String originalName = "photo";

        // When
        String result = photoStorageService.generateUniqueFileName(originalName);

        // Then
        assertNotNull(result);
        assertNotEquals(originalName, result);
        // Should still work even without extension
    }

    @Test
    void testStorePhotos_MixedValidInvalidFiles() {
        // Given
        String vehicleId = "VEH-123";

        when(multipartFile1.getOriginalFilename()).thenReturn("photo1.jpg");
        when(multipartFile1.getContentType()).thenReturn("image/jpeg");

        when(multipartFile2.getOriginalFilename()).thenReturn("document.pdf");
        when(multipartFile2.getContentType()).thenReturn("application/pdf");

        List<MultipartFile> files = Arrays.asList(multipartFile1, multipartFile2);

        // When & Then
        assertThrows(PhotoUploadException.class, () -> photoStorageService.storePhotos(vehicleId, files));

        verify(vehiclePhotoRepository, never()).save(any());
    }
}