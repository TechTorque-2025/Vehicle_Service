package com.techtorque.vehicle_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.repository.VehiclePhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = "CUSTOMER")
class VehicleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehiclePhotoRepository vehiclePhotoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CUSTOMER_ID = "CUST-INTEGRATION-TEST";

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        vehiclePhotoRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void testCompleteVehicleLifecycle() throws Exception {
        // 1. Register a new vehicle
        VehicleRequestDto registerRequest = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        String vehicleId = mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
                .with(csrf()))
                .andExpected(status().isCreated())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract vehicle ID from response
        String actualVehicleId = objectMapper.readTree(vehicleId).get("data").asText();
        assertNotNull(actualVehicleId);

        // Verify vehicle exists in database
        Optional<Vehicle> savedVehicle = vehicleRepository.findById(actualVehicleId);
        assertTrue(savedVehicle.isPresent());
        assertEquals("Toyota", savedVehicle.get().getMake());
        assertEquals("Camry", savedVehicle.get().getModel());
        assertEquals(CUSTOMER_ID, savedVehicle.get().getCustomerId());

        // 2. Get all vehicles for customer
        mockMvc.perform(get("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$[0].id").value(actualVehicleId))
                .andExpected(jsonPath("$[0].make").value("Toyota"));

        // 3. Get specific vehicle details
        mockMvc.perform(get("/vehicles/" + actualVehicleId)
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.id").value(actualVehicleId))
                .andExpected(jsonPath("$.make").value("Toyota"))
                .andExpected(jsonPath("$.vin").value("1HGBH41JXMN109186"));

        // 4. Update vehicle
        VehicleUpdateDto updateRequest = VehicleUpdateDto.builder()
                .color("Blue")
                .mileage(20000)
                .licensePlate("XYZ789")
                .build();

        mockMvc.perform(put("/vehicles/" + actualVehicleId)
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true));

        // Verify update in database
        Vehicle updatedVehicle = vehicleRepository.findById(actualVehicleId).get();
        assertEquals("Blue", updatedVehicle.getColor());
        assertEquals(20000, updatedVehicle.getMileage());
        assertEquals("XYZ789", updatedVehicle.getLicensePlate());

        // 5. Upload photos
        MockMultipartFile photo1 = new MockMultipartFile("files", "photo1.jpg", "image/jpeg",
                "fake image 1".getBytes());
        MockMultipartFile photo2 = new MockMultipartFile("files", "photo2.png", "image/png", "fake image 2".getBytes());

        mockMvc.perform(multipart("/vehicles/" + actualVehicleId + "/photos")
                .file(photo1)
                .file(photo2)
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.photoIds").isArray())
                .andExpected(jsonPath("$.photoIds").isNotEmpty());

        // 6. Get vehicle photos
        mockMvc.perform(get("/vehicles/" + actualVehicleId + "/photos")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray())
                .andExpected(jsonPath("$.length()").value(2));

        // 7. Get service history
        mockMvc.perform(get("/vehicles/" + actualVehicleId + "/history")
                .header("X-User-Subject", CUSTOMER_ID))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$").isArray());

        // 8. Delete vehicle
        mockMvc.perform(delete("/vehicles/" + actualVehicleId)
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true));

        // Verify deletion
        Optional<Vehicle> deletedVehicle = vehicleRepository.findById(actualVehicleId);
        assertFalse(deletedVehicle.isPresent());

        // Verify photos are also deleted
        assertEquals(0, vehiclePhotoRepository.findByVehicleId(actualVehicleId).size());
    }

    @Test
    void testDuplicateVinRegistration() throws Exception {
        // Register first vehicle
        VehicleRequestDto firstVehicle = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("DUPLICATE_VIN_TEST")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstVehicle))
                .with(csrf()))
                .andExpected(status().isCreated());

        // Try to register second vehicle with same VIN
        VehicleRequestDto secondVehicle = VehicleRequestDto.builder()
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("DUPLICATE_VIN_TEST") // Same VIN
                .licensePlate("XYZ789")
                .color("Blue")
                .mileage(25000)
                .build();

        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondVehicle))
                .with(csrf()))
                .andExpected(status().isConflict())
                .andExpected(jsonPath("$.message").value("Vehicle with VIN DUPLICATE_VIN_TEST already exists"));
    }

    @Test
    void testVehicleAccessControl() throws Exception {
        // Customer A registers a vehicle
        VehicleRequestDto vehicleRequest = VehicleRequestDto.builder()
                .make("BMW")
                .model("X5")
                .year(2023)
                .vin("ACCESS_CONTROL_TEST")
                .licensePlate("BMW123")
                .color("Black")
                .mileage(5000)
                .build();

        String responseA = mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", "CUST-A")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest))
                .with(csrf()))
                .andExpected(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String vehicleId = objectMapper.readTree(responseA).get("data").asText();

        // Customer B tries to access Customer A's vehicle
        mockMvc.perform(get("/vehicles/" + vehicleId)
                .header("X-User-Subject", "CUST-B"))
                .andExpected(status().isNotFound());

        // Customer B tries to update Customer A's vehicle
        VehicleUpdateDto updateRequest = VehicleUpdateDto.builder()
                .color("Red")
                .build();

        mockMvc.perform(put("/vehicles/" + vehicleId)
                .header("X-User-Subject", "CUST-B")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(csrf()))
                .andExpected(status().isNotFound());

        // Customer B tries to delete Customer A's vehicle
        mockMvc.perform(delete("/vehicles/" + vehicleId)
                .header("X-User-Subject", "CUST-B")
                .with(csrf()))
                .andExpected(status().isNotFound());

        // Customer A can access their own vehicle
        mockMvc.perform(get("/vehicles/" + vehicleId)
                .header("X-User-Subject", "CUST-A"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.id").value(vehicleId));
    }

    @Test
    void testInvalidFileUpload() throws Exception {
        // Register a vehicle first
        VehicleRequestDto vehicleRequest = VehicleRequestDto.builder()
                .make("Ford")
                .model("F-150")
                .year(2020)
                .vin("FILE_UPLOAD_TEST")
                .licensePlate("FORD123")
                .color("Red")
                .mileage(50000)
                .build();

        String response = mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest))
                .with(csrf()))
                .andExpected(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String vehicleId = objectMapper.readTree(response).get("data").asText();

        // Try to upload non-image file
        MockMultipartFile invalidFile = new MockMultipartFile("files", "document.pdf", "application/pdf",
                "fake pdf content".getBytes());

        mockMvc.perform(multipart("/vehicles/" + vehicleId + "/photos")
                .file(invalidFile)
                .header("X-User-Subject", CUSTOMER_ID)
                .with(csrf()))
                .andExpected(status().isBadRequest())
                .andExpected(jsonPath("$.message").exists());
    }

    @Test
    void testVehicleValidation() throws Exception {
        // Test invalid year
        VehicleRequestDto invalidYear = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(1800) // Too old
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidYear))
                .with(csrf()))
                .andExpected(status().isBadRequest());

        // Test invalid VIN
        VehicleRequestDto invalidVin = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("SHORT") // Too short
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVin))
                .with(csrf()))
                .andExpected(status().isBadRequest());

        // Test negative mileage
        VehicleRequestDto negativeMileage = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(-100) // Negative
                .build();

        mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(negativeMileage))
                .with(csrf()))
                .andExpected(status().isBadRequest());
    }

    @Test
    void testMissingCustomerHeader() throws Exception {
        VehicleRequestDto vehicleRequest = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .build();

        // Missing X-User-Subject header
        mockMvc.perform(post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest))
                .with(csrf()))
                .andExpected(status().isBadRequest());
    }

    @Test
    void testPartialVehicleUpdate() throws Exception {
        // Register vehicle
        VehicleRequestDto vehicleRequest = VehicleRequestDto.builder()
                .make("Mazda")
                .model("CX-5")
                .year(2021)
                .vin("PARTIAL_UPDATE_TEST")
                .licensePlate("MAZDA123")
                .color("White")
                .mileage(30000)
                .build();

        String response = mockMvc.perform(post("/vehicles")
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest))
                .with(csrf()))
                .andExpected(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String vehicleId = objectMapper.readTree(response).get("data").asText();

        // Update only color
        VehicleUpdateDto partialUpdate = VehicleUpdateDto.builder()
                .color("Black")
                .build();

        mockMvc.perform(put("/vehicles/" + vehicleId)
                .header("X-User-Subject", CUSTOMER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate))
                .with(csrf()))
                .andExpected(status().isOk());

        // Verify only color changed
        Vehicle updatedVehicle = vehicleRepository.findById(vehicleId).get();
        assertEquals("Black", updatedVehicle.getColor());
        assertEquals("MAZDA123", updatedVehicle.getLicensePlate()); // Should remain unchanged
        assertEquals(30000, updatedVehicle.getMileage()); // Should remain unchanged
    }
}