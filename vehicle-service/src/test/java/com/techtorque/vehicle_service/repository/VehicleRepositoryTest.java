package com.techtorque.vehicle_service.repository;

import com.techtorque.vehicle_service.entity.Vehicle;
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
class VehicleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void testFindByCustomerId() {
        // Given
        Vehicle vehicle1 = Vehicle.builder()
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        Vehicle vehicle2 = Vehicle.builder()
                .customerId("CUST-123")
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("2HGFC2F59MH123456")
                .licensePlate("XYZ789")
                .color("Blue")
                .mileage(25000)
                .build();

        entityManager.persistAndFlush(vehicle1);
        entityManager.persistAndFlush(vehicle2);

        // When
        List<Vehicle> vehicles = vehicleRepository.findByCustomerId("CUST-123");

        // Then
        assertEquals(2, vehicles.size());
        assertTrue(vehicles.stream().anyMatch(v -> v.getMake().equals("Toyota")));
        assertTrue(vehicles.stream().anyMatch(v -> v.getMake().equals("Honda")));
    }

    @Test
    void testFindByIdAndCustomerId() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .id("VEH-TEST-123")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .build();

        entityManager.persistAndFlush(vehicle);

        // When
        Optional<Vehicle> found = vehicleRepository.findByIdAndCustomerId("VEH-TEST-123", "CUST-123");
        Optional<Vehicle> notFound = vehicleRepository.findByIdAndCustomerId("VEH-TEST-123", "CUST-456");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Toyota", found.get().getMake());
        assertFalse(notFound.isPresent());
    }

    @Test
    void testSaveAndFindById() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .customerId("CUST-123")
                .make("BMW")
                .model("X5")
                .year(2023)
                .vin("5UXCR6C0XN9123456")
                .licensePlate("BMW123")
                .color("Black")
                .mileage(5000)
                .build();

        // When
        Vehicle saved = vehicleRepository.save(vehicle);

        // Then
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Optional<Vehicle> found = vehicleRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("BMW", found.get().getMake());
    }

    @Test
    void testDeleteByIdAndCustomerId() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .id("VEH-DELETE-123")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .build();

        entityManager.persistAndFlush(vehicle);

        // When
        Optional<Vehicle> vehicleToDelete = vehicleRepository.findByIdAndCustomerId("VEH-DELETE-123", "CUST-123");
        assertTrue(vehicleToDelete.isPresent());
        vehicleRepository.delete(vehicleToDelete.get());
        entityManager.flush();

        // Then
        Optional<Vehicle> afterDelete = vehicleRepository.findByIdAndCustomerId("VEH-DELETE-123", "CUST-123");
        assertFalse(afterDelete.isPresent());
    }
}