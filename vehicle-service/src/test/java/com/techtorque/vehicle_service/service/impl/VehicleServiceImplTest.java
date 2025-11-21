package com.techtorque.vehicle_service.service.impl;

import com.techtorque.vehicle_service.dto.VehicleRequestDto;
import com.techtorque.vehicle_service.dto.VehicleUpdateDto;
import com.techtorque.vehicle_service.dto.VehicleListResponseDto;
import com.techtorque.vehicle_service.dto.VehicleResponseDto;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.exception.DuplicateVinException;
import com.techtorque.vehicle_service.exception.UnauthorizedVehicleAccessException;
import com.techtorque.vehicle_service.exception.VehicleNotFoundException;
import com.techtorque.vehicle_service.mapper.VehicleMapper;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import com.techtorque.vehicle_service.repository.VehiclePhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehiclePhotoRepository vehiclePhotoRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private VehicleRequestDto vehicleRequestDto;
    private Vehicle vehicle;
    private VehicleResponseDto vehicleResponseDto;

    @BeforeEach
    void setUp() {
        vehicleRequestDto = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        vehicle = Vehicle.builder()
                .id("VEH-2022-TOYOTA-CAMRY-1234")
                .customerId("CUST-123")
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

        vehicleResponseDto = VehicleResponseDto.builder()
                .id("VEH-2022-TOYOTA-CAMRY-1234")
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
    }

    @Test
    void testRegisterVehicle_Success() {
        // Given
        when(vehicleRepository.existsByVin(vehicleRequestDto.getVin())).thenReturn(false);
        when(vehicleMapper.toEntity(vehicleRequestDto, "CUST-123")).thenReturn(vehicle);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        // When
        String result = vehicleService.registerVehicle(vehicleRequestDto, "CUST-123");

        // Then
        assertEquals(vehicle.getId(), result);
        verify(vehicleRepository).existsByVin(vehicleRequestDto.getVin());
        verify(vehicleMapper).toEntity(vehicleRequestDto, "CUST-123");
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void testRegisterVehicle_DuplicateVin() {
        // Given
        when(vehicleRepository.existsByVin(vehicleRequestDto.getVin())).thenReturn(true);

        // When & Then
        assertThrows(DuplicateVinException.class, () -> vehicleService.registerVehicle(vehicleRequestDto, "CUST-123"));

        verify(vehicleRepository).existsByVin(vehicleRequestDto.getVin());
        verify(vehicleMapper, never()).toEntity(any(), any());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void testGetVehiclesForCustomer_Success() {
        // Given
        Vehicle vehicle1 = Vehicle.builder().id("VEH-1").customerId("CUST-123").make("Toyota").build();
        Vehicle vehicle2 = Vehicle.builder().id("VEH-2").customerId("CUST-123").make("Honda").build();
        List<Vehicle> vehicles = Arrays.asList(vehicle1, vehicle2);

        VehicleListResponseDto dto1 = VehicleListResponseDto.builder().id("VEH-1").make("Toyota").build();
        VehicleListResponseDto dto2 = VehicleListResponseDto.builder().id("VEH-2").make("Honda").build();

        when(vehicleRepository.findByCustomerId("CUST-123")).thenReturn(vehicles);
        when(vehicleMapper.toListResponseDto(vehicle1)).thenReturn(dto1);
        when(vehicleMapper.toListResponseDto(vehicle2)).thenReturn(dto2);

        // When
        List<VehicleListResponseDto> result = vehicleService.getVehiclesForCustomer("CUST-123");

        // Then
        assertEquals(2, result.size());
        assertEquals("VEH-1", result.get(0).getId());
        assertEquals("VEH-2", result.get(1).getId());
        assertEquals("Toyota", result.get(0).getMake());
        assertEquals("Honda", result.get(1).getMake());

        verify(vehicleRepository).findByCustomerId("CUST-123");
        verify(vehicleMapper).toListResponseDto(vehicle1);
        verify(vehicleMapper).toListResponseDto(vehicle2);
    }

    @Test
    void testGetVehiclesForCustomer_EmptyList() {
        // Given
        when(vehicleRepository.findByCustomerId("CUST-123")).thenReturn(Arrays.asList());

        // When
        List<VehicleListResponseDto> result = vehicleService.getVehiclesForCustomer("CUST-123");

        // Then
        assertTrue(result.isEmpty());
        verify(vehicleRepository).findByCustomerId("CUST-123");
    }

    @Test
    void testGetVehicleByIdAndCustomer_Success() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponseDto(vehicle)).thenReturn(vehicleResponseDto);

        // When
        VehicleResponseDto result = vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-123");

        // Then
        assertEquals(vehicleResponseDto.getId(), result.getId());
        assertEquals(vehicleResponseDto.getMake(), result.getMake());
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleMapper).toResponseDto(vehicle);
    }

    @Test
    void testGetVehicleByIdAndCustomer_NotFound() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VehicleNotFoundException.class,
                () -> vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-123"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleMapper, never()).toResponseDto(any());
    }

    @Test
    void testGetVehicleByIdAndCustomer_UnauthorizedAccess() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-456")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VehicleNotFoundException.class,
                () -> vehicleService.getVehicleByIdAndCustomer("VEH-123", "CUST-456"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-456");
    }

    @Test
    void testUpdateVehicle_Success() {
        // Given
        VehicleUpdateDto updateDto = VehicleUpdateDto.builder()
                .color("Blue")
                .mileage(20000)
                .licensePlate("XYZ789")
                .build();

        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        // When
        vehicleService.updateVehicle("VEH-123", updateDto, "CUST-123");

        // Then
        assertEquals("Blue", vehicle.getColor());
        assertEquals(20000, vehicle.getMileage());
        assertEquals("XYZ789", vehicle.getLicensePlate());

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void testUpdateVehicle_NotFound() {
        // Given
        VehicleUpdateDto updateDto = VehicleUpdateDto.builder()
                .color("Blue")
                .build();

        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VehicleNotFoundException.class,
                () -> vehicleService.updateVehicle("VEH-123", updateDto, "CUST-123"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void testUpdateVehicle_PartialUpdate() {
        // Given
        VehicleUpdateDto updateDto = VehicleUpdateDto.builder()
                .color("Red")
                .build(); // Only color, no mileage or license plate

        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        String originalLicensePlate = vehicle.getLicensePlate();
        int originalMileage = vehicle.getMileage();

        // When
        vehicleService.updateVehicle("VEH-123", updateDto, "CUST-123");

        // Then
        assertEquals("Red", vehicle.getColor());
        assertEquals(originalLicensePlate, vehicle.getLicensePlate()); // Should remain unchanged
        assertEquals(originalMileage, vehicle.getMileage()); // Should remain unchanged

        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void testDeleteVehicle_Success() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.of(vehicle));

        // When
        vehicleService.deleteVehicle("VEH-123", "CUST-123");

        // Then
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehiclePhotoRepository).deleteByVehicleId("VEH-123");
        verify(vehicleRepository).deleteByIdAndCustomerId("VEH-123", "CUST-123");
    }

    @Test
    void testDeleteVehicle_NotFound() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VehicleNotFoundException.class, () -> vehicleService.deleteVehicle("VEH-123", "CUST-123"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
        verify(vehiclePhotoRepository, never()).deleteByVehicleId(any());
        verify(vehicleRepository, never()).deleteByIdAndCustomerId(any(), any());
    }

    @Test
    void testDeleteVehicle_UnauthorizedAccess() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-WRONG")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VehicleNotFoundException.class, () -> vehicleService.deleteVehicle("VEH-123", "CUST-WRONG"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-WRONG");
        verify(vehiclePhotoRepository, never()).deleteByVehicleId(any());
        verify(vehicleRepository, never()).deleteByIdAndCustomerId(any(), any());
    }

    @Test
    void testValidateVehicleAccess_Success() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.of(vehicle));

        // When
        Vehicle result = vehicleService.validateVehicleAccess("VEH-123", "CUST-123");

        // Then
        assertEquals(vehicle, result);
        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-123");
    }

    @Test
    void testValidateVehicleAccess_Unauthorized() {
        // Given
        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-WRONG")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UnauthorizedVehicleAccessException.class,
                () -> vehicleService.validateVehicleAccess("VEH-123", "CUST-WRONG"));

        verify(vehicleRepository).findByIdAndCustomerId("VEH-123", "CUST-WRONG");
    }

    @Test
    void testRegisterVehicle_WithNullColor() {
        // Given
        VehicleRequestDto requestWithNullColor = VehicleRequestDto.builder()
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("2HGFC2F59MH123456")
                .licensePlate("XYZ789")
                .color(null) // Null color
                .mileage(25000)
                .build();

        Vehicle vehicleWithNullColor = Vehicle.builder()
                .customerId("CUST-123")
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("2HGFC2F59MH123456")
                .licensePlate("XYZ789")
                .color(null)
                .mileage(25000)
                .build();

        when(vehicleRepository.existsByVin(requestWithNullColor.getVin())).thenReturn(false);
        when(vehicleMapper.toEntity(requestWithNullColor, "CUST-123")).thenReturn(vehicleWithNullColor);
        when(vehicleRepository.save(vehicleWithNullColor)).thenReturn(vehicleWithNullColor);

        // When
        String result = vehicleService.registerVehicle(requestWithNullColor, "CUST-123");

        // Then
        assertNotNull(result);
        verify(vehicleRepository).save(vehicleWithNullColor);
    }

    @Test
    void testUpdateVehicle_WithNullValues() {
        // Given
        VehicleUpdateDto updateDto = VehicleUpdateDto.builder()
                .color(null)
                .mileage(null)
                .licensePlate(null)
                .build(); // All null values

        when(vehicleRepository.findByIdAndCustomerId("VEH-123", "CUST-123")).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

        String originalColor = vehicle.getColor();
        String originalLicensePlate = vehicle.getLicensePlate();
        int originalMileage = vehicle.getMileage();

        // When
        vehicleService.updateVehicle("VEH-123", updateDto, "CUST-123");

        // Then - all values should remain unchanged
        assertEquals(originalColor, vehicle.getColor());
        assertEquals(originalLicensePlate, vehicle.getLicensePlate());
        assertEquals(originalMileage, vehicle.getMileage());

        verify(vehicleRepository).save(vehicle);
    }
}