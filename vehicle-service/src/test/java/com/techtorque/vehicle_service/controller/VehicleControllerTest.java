package com.techtorque.vehicle_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.dto.ApiResponseDto;
import com.techtorque.vehicle_service.dto.PhotoUploadResponseDto;
import com.techtorque.vehicle_service.dto.VehicleListResponseDto;
import com.techtorque.vehicle_service.dto.VehicleResponseDto;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import com.techtorque.vehicle_service.exception.DuplicateVinException;
import com.techtorque.vehicle_service.exception.PhotoUploadException;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.service.PhotoStorageService;
import com.techtorque.vehicle_service.service.ServiceHistoryService;
import com.techtorque.vehicle_service.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@WithMockUser(roles = "CUSTOMER")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private PhotoStorageService photoStorageService;

    @MockBean
    private ServiceHistoryService serviceHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CUSTOMER_ID = "CUST-123";

    @Test
    void testRegisterVehicle_Success() throws Exception {
        // Given
        VehicleRequestDto requestDto = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        when(vehicleService.registerVehicle(any(VehicleRequestDto.class), eq(CUSTOMER_ID)))
                .thenReturn("VEH-2022-TOYOTA-CAMRY-1234");

        // When & Then
        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle registered successfully"))
                .andExpect(jsonPath("$.data").value("VEH-2022-TOYOTA-CAMRY-1234"));

        verify(vehicleService).registerVehicle(any(VehicleRequestDto.class), eq(CUSTOMER_ID));
    }

    @Test
    void testRegisterVehicle_DuplicateVin() throws Exception {
        // Given
        VehicleRequestDto requestDto = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        when(vehicleService.registerVehicle(any(VehicleRequestDto.class), eq(CUSTOMER_ID)))
                .thenThrow(new DuplicateVinException("Vehicle with VIN 1HGBH41JXMN109186 already exists"));

        // When & Then
        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vehicle with VIN 1HGBH41JXMN109186 already exists"));
    }

    @Test
    void testRegisterVehicle_ValidationErrors() throws Exception {
        // Given - invalid request with missing required fields
        VehicleRequestDto requestDto = VehicleRequestDto.builder()
                .make("") // Invalid - empty
                .model("Camry")
                .year(1800) // Invalid - too old
                .vin("SHORT") // Invalid - too short
                .licensePlate("ABC123")
                .mileage(-100) // Invalid - negative
                .build();

        // When & Then
        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetVehicles_Success() throws Exception {
        // Given
        List<VehicleListResponseDto> vehicles = Arrays.asList(
                VehicleListResponseDto.builder()
                        .id("VEH-1")
                        .make("Toyota")
                        .model("Camry")
                        .year(2022)
                        .licensePlate("ABC123")
                        .color("Silver")
                        .build(),
                VehicleListResponseDto.builder()
                        .id("VEH-2")
                        .make("Honda")
                        .model("Civic")
                        .year(2021)
                        .licensePlate("XYZ789")
                        .color("Blue")
                        .build());

        when(vehicleService.getVehiclesForCustomer(CUSTOMER_ID)).thenReturn(vehicles);

        // When & Then
        mockMvc.perform(get("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("VEH-1"))
                .andExpect(jsonPath("$[0].make").value("Toyota"))
                .andExpect(jsonPath("$[1].id").value("VEH-2"))
                .andExpect(jsonPath("$[1].make").value("Honda"));

        verify(vehicleService).getVehiclesForCustomer(CUSTOMER_ID);
    }

    @Test
    void testGetVehicles_EmptyList() throws Exception {
        // Given
        when(vehicleService.getVehiclesForCustomer(CUSTOMER_ID)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(vehicleService).getVehiclesForCustomer(CUSTOMER_ID);
    }

    @Test
    void testGetVehicleDetails_Success() throws Exception {
        // Given
        VehicleResponseDto vehicleDto = VehicleResponseDto.builder()
                .id("VEH-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(vehicleService.getVehicleByIdAndCustomer("VEH-123", CUSTOMER_ID))
                .thenReturn(vehicleDto);

        // When & Then
        mockMvc.perform(get("/vehicles/VEH-123")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("VEH-123"))
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.vin").value("1HGBH41JXMN109186"));

        verify(vehicleService).getVehicleByIdAndCustomer("VEH-123", CUSTOMER_ID);
    }

    @Test
    void testGetVehicleDetails_NotFound() throws Exception {
        // Given
        when(vehicleService.getVehicleByIdAndCustomer("VEH-NONEXISTENT", CUSTOMER_ID))
                .thenThrow(new VehicleNotFoundException("Vehicle not found with ID: VEH-NONEXISTENT"));

        // When & Then
        mockMvc.perform(get("/vehicles/VEH-NONEXISTENT")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with ID: VEH-NONEXISTENT"));
    }

    @Test
    void testUpdateVehicle_Success() throws Exception {
        // Given
        VehicleUpdateDto updateDto = VehicleUpdateDto.builder()
                .color("Blue")
                .mileage(20000)
                .licensePlate("XYZ789")
                .build();

        doNothing().when(vehicleService).updateVehicle("VEH-123", updateDto, CUSTOMER_ID);

        // When & Then
        mockMvc.perform(put("/vehicles/VEH-123")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle updated successfully"));

        verify(vehicleService).updateVehicle("VEH-123", updateDto, CUSTOMER_ID);
    }

    @Test
    void testUpdateVehicle_NotFound() throws Exception {
        // Given
        VehicleUpdateDto updateDto = VehicleUpdateDto.builder()
                .color("Blue")
                .build();

        doThrow(new VehicleNotFoundException("Vehicle not found with ID: VEH-NONEXISTENT"))
                .when(vehicleService).updateVehicle("VEH-NONEXISTENT", updateDto, CUSTOMER_ID);

        // When & Then
        mockMvc.perform(put("/vehicles/VEH-NONEXISTENT")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with ID: VEH-NONEXISTENT"));
    }

    @Test
    void testDeleteVehicle_Success() throws Exception {
        // Given
        doNothing().when(vehicleService).deleteVehicle("VEH-123", CUSTOMER_ID);

        // When & Then
        mockMvc.perform(delete("/vehicles/VEH-123")
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vehicle deleted successfully"));

        verify(vehicleService).deleteVehicle("VEH-123", CUSTOMER_ID);
    }

    @Test
    void testDeleteVehicle_NotFound() throws Exception {
        // Given
        doThrow(new VehicleNotFoundException("Vehicle not found with ID: VEH-NONEXISTENT"))
                .when(vehicleService).deleteVehicle("VEH-NONEXISTENT", CUSTOMER_ID);

        // When & Then
        mockMvc.perform(delete("/vehicles/VEH-NONEXISTENT")
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpected(jsonPath("$.message").value("Vehicle not found with ID: VEH-NONEXISTENT"));
    }

    @Test
    void testUploadPhotos_Success() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("files", "photo1.jpg", "image/jpeg", "fake image 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "photo2.png", "image/png", "fake image 2".getBytes());

        PhotoUploadResponseDto uploadResponse = PhotoUploadResponseDto.builder()
                .photoIds(Arrays.asList("PHOTO-1", "PHOTO-2"))
                .urls(Arrays.asList(
                        "http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo1.jpg",
                        "http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo2.png"))
                .build();

        when(vehicleService.validateVehicleAccess("VEH-123", CUSTOMER_ID)).thenReturn(null);
        when(photoStorageService.storePhotos(eq("VEH-123"), anyList())).thenReturn(uploadResponse);

        // When & Then
        mockMvc.perform(multipart("/vehicles/VEH-123/photos")
                .file(file1)
                .file(file2)
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoIds").isArray())
                .andExpect(jsonPath("$.photoIds[0]").value("PHOTO-1"))
                .andExpect(jsonPath("$.photoIds[1]").value("PHOTO-2"))
                .andExpect(jsonPath("$.urls").isArray())
                .andExpect(
                        jsonPath("$.urls[0]").value("http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo1.jpg"));

        verify(vehicleService).validateVehicleAccess("VEH-123", CUSTOMER_ID);
        verify(photoStorageService).storePhotos(eq("VEH-123"), anyList());
    }

    @Test
    void testUploadPhotos_VehicleNotFound() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("files", "photo.jpg", "image/jpeg", "fake image".getBytes());

        when(vehicleService.validateVehicleAccess("VEH-NONEXISTENT", CUSTOMER_ID))
                .thenThrow(new VehicleNotFoundException("Vehicle not found"));

        // When & Then
        mockMvc.perform(multipart("/vehicles/VEH-NONEXISTENT/photos")
                .file(file)
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(photoStorageService, never()).storePhotos(anyString(), anyList());
    }

    @Test
    void testUploadPhotos_InvalidFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("files", "document.pdf", "application/pdf",
                "fake pdf".getBytes());

        when(vehicleService.validateVehicleAccess("VEH-123", CUSTOMER_ID)).thenReturn(null);
        when(photoStorageService.storePhotos(eq("VEH-123"), anyList()))
                .thenThrow(new PhotoUploadException("Invalid file type"));

        // When & Then
        mockMvc.perform(multipart("/vehicles/VEH-123/photos")
                .file(file)
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid file type"));
    }

    @Test
    void testGetVehiclePhotos_Success() throws Exception {
        // Given
        List<VehiclePhoto> photos = Arrays.asList(
                VehiclePhoto.builder()
                        .id("PHOTO-1")
                        .vehicleId("VEH-123")
                        .fileName("photo1.jpg")
                        .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo1.jpg")
                        .build(),
                VehiclePhoto.builder()
                        .id("PHOTO-2")
                        .vehicleId("VEH-123")
                        .fileName("photo2.png")
                        .fileUrl("http://localhost:8082/api/v1/vehicles/VEH-123/photos/photo2.png")
                        .build());

        when(vehicleService.validateVehicleAccess("VEH-123", CUSTOMER_ID)).thenReturn(null);
        when(photoStorageService.getPhotosForVehicle("VEH-123")).thenReturn(photos);

        // When & Then
        mockMvc.perform(get("/vehicles/VEH-123/photos")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("PHOTO-1"))
                .andExpect(jsonPath("$[0].fileName").value("photo1.jpg"))
                .andExpect(jsonPath("$[1].id").value("PHOTO-2"));

        verify(vehicleService).validateVehicleAccess("VEH-123", CUSTOMER_ID);
        verify(photoStorageService).getPhotosForVehicle("VEH-123");
    }

    @Test
    void testGetVehicleHistory_Success() throws Exception {
        // Given
        when(vehicleService.validateVehicleAccess("VEH-123", CUSTOMER_ID)).thenReturn(null);
        when(serviceHistoryService.getServiceHistoryForVehicle("VEH-123")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/vehicles/VEH-123/history")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$").isEmpty());

        verify(vehicleService).validateVehicleAccess("VEH-123", CUSTOMER_ID);
        verify(serviceHistoryService).getServiceHistoryForVehicle("VEH-123");
    }

    @Test
    void testMissingCustomerIdHeader() throws Exception {
        // When & Then
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Different role
    void testUnauthorizedRole() throws Exception {
        // When & Then
        mockMvc.perform(get("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpected(status().isForbidden());
    }
}