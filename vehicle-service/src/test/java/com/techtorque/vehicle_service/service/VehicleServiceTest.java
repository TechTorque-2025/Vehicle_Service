package com.techtorque.vehicle_service.service;

import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.repository.VehiclePhotoRepository;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehiclePhotoRepository vehiclePhotoRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private VehicleRequestDto validRequest;
    private Vehicle existingVehicle;
    private VehicleUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        validRequest = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        existingVehicle = Vehicle.builder()
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

        updateDto = VehicleUpdateDto.builder()
                .mileage(20000)
                .color("Blue")
                .build();
    }

    @Test
    void testRegisterVehicle_Success() {
        // Given
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(existingVehicle);

        // When
        Vehicle result = vehicleService.registerVehicle(validRequest, "CUST-123");

        // Then
        assertNotNull(result);
        assertEquals("Toyota", result.getMake());
        assertEquals("CUST-123", result.getCustomerId());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void testGetVehiclesForCustomer_Success() {
        // Given
        List<Vehicle> vehicles = Arrays.asList(existingVehicle);
        when(vehicleRepository.findByCustomerId("CUST-123")).thenReturn(vehicles);

        // When
        List<Vehicle> result = vehicleService.getVehiclesForCustomer("CUST-123");

        // Then
        assertEquals(1, result.size());
        assertEquals(existingVehicle.getId(), result.get(0).getId());
        verify(vehicleRepository).findByCustomerId("CUST-123");
    }

    @Test
    void testGetAllVehicles_Success() {
        // Given
        List<Vehicle> vehicles = Arrays.asList(existingVehicle);
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // When
        List<Vehicle> result = vehicleService.getAllVehicles();

        // Then
        assertEquals(1, result.size());
        verify(vehicleRepository).findAll();
    }

    @Test
    void testGetVehicleByIdAndCustomer_Success() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123"))
                .thenReturn(Optional.of(existingVehicle));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(existingVehicle.getId(), result.get().getId());
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
    }

    @Test
    void testGetVehicleByIdAndCustomer_NotFound() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-456"))
                .thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-456");

        // Then
        assertFalse(result.isPresent());
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-456");
    }

    @Test
    void testGetVehicleById_Success() {
        // Given
        when(vehicleRepository.findById("VEH-123"))
                .thenReturn(Optional.of(existingVehicle));

        // When
        Optional<Vehicle> result = vehicleService.getVehicleById("VEH-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals(existingVehicle.getId(), result.get().getId());
        verify(vehicleRepository).findById("VEH-123");
    }

    @Test
    void testUpdateVehicle_Success() {
        // Given
        Vehicle updatedVehicle = Vehicle.builder()
                .id(existingVehicle.getId())
                .customerId(existingVehicle.getCustomerId())
                .make(existingVehicle.getMake())
                .model(existingVehicle.getModel())
                .year(existingVehicle.getYear())
                .vin(existingVehicle.getVin())
                .licensePlate(existingVehicle.getLicensePlate())
                .mileage(20000)
                .color("Blue")
                .build();

        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123"))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        // When
        Vehicle result = vehicleService.updateVehicle("VEH-123", updateDto, "CUST-123");

        // Then
        assertEquals(20000, result.getMileage());
        assertEquals("Blue", result.getColor());
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void testUpdateVehicle_VehicleNotFound() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> vehicleService.updateVehicle("VEH-123", updateDto, "CUST-123"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void testDeleteVehicle_Success() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123"))
                .thenReturn(Optional.of(existingVehicle));

        // When
        vehicleService.deleteVehicle("VEH-123", "CUST-123");

        // Then
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleRepository).delete(existingVehicle);
    }

    @Test
    void testDeleteVehicle_VehicleNotFound() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(VehicleNotFoundException.class,
                () -> vehicleService.deleteVehicle("VEH-123", "CUST-123"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }
    }
}