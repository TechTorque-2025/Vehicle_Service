package com.techtorque.vehicle_service.controller;

import com.techtorque.vehicle_service.dto.*;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.entity.VehiclePhoto;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.mapper.VehicleMapper;
import com.techtorque.vehicle_service.service.PhotoStorageService;
import com.techtorque.vehicle_service.service.ServiceHistoryService;
import com.techtorque.vehicle_service.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    @MockBean
    private PhotoStorageService photoStorageService;

    @MockBean
    private ServiceHistoryService serviceHistoryService;

    private Vehicle testVehicle;
    private VehicleRequestDto validRequest;
    private VehicleUpdateDto validUpdate;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .id("VEH-123")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        validRequest = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        validUpdate = VehicleUpdateDto.builder()
                .mileage(20000)
                .color("Blue")
                .build();
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testRegisterNewVehicle_Success() throws Exception {
        when(vehicleService.registerVehicle(any(VehicleRequestDto.class), eq("CUST-123")))
                .thenReturn(testVehicle);

        mockMvc.perform(post("/vehicles")
                .with(csrf())
                .header("X-User-Subject", "CUST-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Vehicle added"))
                .andExpect(jsonPath("$.vehicleId").value("VEH-123"));

        verify(vehicleService).registerVehicle(any(VehicleRequestDto.class), eq("CUST-123"));
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testListCustomerVehicles_Customer() throws Exception {
        List<Vehicle> vehicles = Arrays.asList(testVehicle);
        when(vehicleService.getVehiclesForCustomer("CUST-123")).thenReturn(vehicles);

        mockMvc.perform(get("/vehicles")
                .header("X-User-Subject", "CUST-123")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("VEH-123"));

        verify(vehicleService).getVehiclesForCustomer("CUST-123");
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testListCustomerVehicles_Admin() throws Exception {
        List<Vehicle> vehicles = Arrays.asList(testVehicle);
        when(vehicleService.getAllVehicles()).thenReturn(vehicles);

        mockMvc.perform(get("/vehicles")
                .header("X-User-Subject", "ADMIN-123")
                .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk());

        verify(vehicleService).getAllVehicles();
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testGetVehicleDetails_Success() throws Exception {
        when(vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-123"))
                .thenReturn(Optional.of(testVehicle));

        mockMvc.perform(get("/vehicles/VEH-123")
                .header("X-User-Subject", "CUST-123")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("VEH-123"))
                .andExpect(jsonPath("$.make").value("Toyota"));

        verify(vehicleService).getVehicleByIdAndCustomer("VEH-123", "CUST-123");
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testGetVehicleDetails_NotFound() throws Exception {
        when(vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-123"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/vehicles/VEH-123")
                .header("X-User-Subject", "CUST-123")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());

        verify(vehicleService).getVehicleByIdAndCustomer("VEH-123", "CUST-123");
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testUpdateVehicleInfo_Success() throws Exception {
        Vehicle updatedVehicle = testVehicle.toBuilder()
                .mileage(20000)
                .color("Blue")
                .build();

        when(vehicleService.updateVehicle(eq("VEH-123"), any(VehicleUpdateDto.class), eq("CUST-123")))
                .thenReturn(updatedVehicle);

        mockMvc.perform(put("/vehicles/VEH-123")
                .with(csrf())
                .header("X-User-Subject", "CUST-123")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle updated"))
                .andExpect(jsonPath("$.vehicleId").value("VEH-123"));

        verify(vehicleService).updateVehicle(eq("VEH-123"), any(VehicleUpdateDto.class), eq("CUST-123"));
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testRemoveVehicle_Success() throws Exception {
        doNothing().when(vehicleService).deleteVehicle("VEH-123", "CUST-123");
        doNothing().when(photoStorageService).deleteVehiclePhotos("VEH-123");

        mockMvc.perform(delete("/vehicles/VEH-123")
                .with(csrf())
                .header("X-User-Subject", "CUST-123")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle removed"))
                .andExpect(jsonPath("$.vehicleId").value("VEH-123"));

        verify(vehicleService).deleteVehicle("VEH-123", "CUST-123");
        verify(photoStorageService).deleteVehiclePhotos("VEH-123");
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testGetVehiclePhotoList_Success() throws Exception {
        VehiclePhoto photo = VehiclePhoto.builder()
                .id("PHOTO-123")
                .vehicle(testVehicle)
                .fileName("front.jpg")
                .originalFileName("front_view.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .build();

        List<VehiclePhoto> photos = Arrays.asList(photo);
        when(photoStorageService.getVehiclePhotos("VEH-123", "CUST-123"))
                .thenReturn(photos);

        mockMvc.perform(get("/vehicles/VEH-123/photos")
                .header("X-User-Subject", "CUST-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fileName").value("front.jpg"));

        verify(photoStorageService).getVehiclePhotos("VEH-123", "CUST-123");
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testGetServiceHistory_Success() throws Exception {
        ServiceHistoryDto historyItem = ServiceHistoryDto.builder()
                .type("Oil Change")
                .cost(new java.math.BigDecimal("50.00"))
                .date(java.time.LocalDateTime.now())
                .description("Regular oil change service")
                .build();

        List<ServiceHistoryDto> history = Arrays.asList(historyItem);
        when(serviceHistoryService.getServiceHistory("VEH-123", "CUST-123"))
                .thenReturn(history);

        mockMvc.perform(get("/vehicles/VEH-123/history")
                .header("X-User-Subject", "CUST-123")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].serviceType").value("Oil Change"));

        verify(serviceHistoryService).getServiceHistory("VEH-123", "CUST-123");
    }

    @Test
    @WithMockUser(roles = { "CUSTOMER" })
    void testDeleteSinglePhoto_Success() throws Exception {
        doNothing().when(photoStorageService).deleteSinglePhoto("PHOTO-123", "CUST-123");

        mockMvc.perform(delete("/vehicles/photos/PHOTO-123")
                .with(csrf())
                .header("X-User-Subject", "CUST-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Photo deleted successfully"));

        verify(photoStorageService).deleteSinglePhoto("PHOTO-123", "CUST-123");
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = { "ADMIN" })
    void testGetVehicleDetails_AdminAccess() throws Exception {
        when(vehicleService.getVehicleById("VEH-123"))
                .thenReturn(Optional.of(testVehicle));

        mockMvc.perform(get("/vehicles/VEH-123")
                .header("X-User-Subject", "ADMIN-123")
                .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("VEH-123"));

        verify(vehicleService).getVehicleById("VEH-123");
    }
}