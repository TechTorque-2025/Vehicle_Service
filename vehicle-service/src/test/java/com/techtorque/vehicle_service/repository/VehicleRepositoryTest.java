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

        Vehicle vehicle3 = Vehicle.builder()
                .customerId("CUST-456")
                .make("Ford")
                .model("F-150")
                .year(2020)
                .vin("1FTFW1ET5LKD12345")
                .licensePlate("FORD123")
                .color("Red")
                .mileage(50000)
                .build();

        entityManager.persistAndFlush(vehicle1);
        entityManager.persistAndFlush(vehicle2);
        entityManager.persistAndFlush(vehicle3);

        // When
        List<Vehicle> customer123Vehicles = vehicleRepository.findByCustomerId("CUST-123");
        List<Vehicle> customer456Vehicles = vehicleRepository.findByCustomerId("CUST-456");
        List<Vehicle> nonExistentCustomerVehicles = vehicleRepository.findByCustomerId("CUST-999");

        // Then
        assertEquals(2, customer123Vehicles.size());
        assertEquals(1, customer456Vehicles.size());
        assertEquals(0, nonExistentCustomerVehicles.size());

        assertTrue(customer123Vehicles.stream().anyMatch(v -> v.getMake().equals("Toyota")));
        assertTrue(customer123Vehicles.stream().anyMatch(v -> v.getMake().equals("Honda")));
        assertEquals("Ford", customer456Vehicles.get(0).getMake());
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
                .color("Silver")
                .mileage(15000)
                .build();

        entityManager.persistAndFlush(vehicle);

        // When
        Optional<Vehicle> found = vehicleRepository.findByIdAndCustomerId("VEH-TEST-123", "CUST-123");
        Optional<Vehicle> notFoundWrongCustomer = vehicleRepository.findByIdAndCustomerId("VEH-TEST-123", "CUST-456");
        Optional<Vehicle> notFoundWrongId = vehicleRepository.findByIdAndCustomerId("VEH-WRONG-ID", "CUST-123");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Toyota", found.get().getMake());
        assertEquals("CUST-123", found.get().getCustomerId());

        assertFalse(notFoundWrongCustomer.isPresent());
        assertFalse(notFoundWrongId.isPresent());
    }

    @Test
    void testExistsByVin() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .build();

        entityManager.persistAndFlush(vehicle);

        // When
        boolean exists = vehicleRepository.existsByVin("1HGBH41JXMN109186");
        boolean notExists = vehicleRepository.existsByVin("NONEXISTENT_VIN");

        // Then
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void testExistsByVinAndIdNot() {
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
        boolean existsForDifferentId = vehicleRepository.existsByVinAndIdNot("1HGBH41JXMN109186", "VEH-DIFFERENT-456");
        boolean notExistsForSameId = vehicleRepository.existsByVinAndIdNot("1HGBH41JXMN109186", "VEH-TEST-123");
        boolean notExistsForNonExistentVin = vehicleRepository.existsByVinAndIdNot("NONEXISTENT_VIN", "VEH-ANY-ID");

        // Then
        assertTrue(existsForDifferentId); // VIN exists but for different vehicle ID
        assertFalse(notExistsForSameId); // Same vehicle, should return false
        assertFalse(notExistsForNonExistentVin); // VIN doesn't exist at all
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

        // Verify it exists
        Optional<Vehicle> beforeDelete = vehicleRepository.findByIdAndCustomerId("VEH-DELETE-123", "CUST-123");
        assertTrue(beforeDelete.isPresent());

        // When
        vehicleRepository.deleteByIdAndCustomerId("VEH-DELETE-123", "CUST-123");
        entityManager.flush();

        // Then
        Optional<Vehicle> afterDelete = vehicleRepository.findByIdAndCustomerId("VEH-DELETE-123", "CUST-123");
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void testDeleteByIdAndCustomerIdWithWrongCustomer() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .id("VEH-DELETE-456")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109187")
                .licensePlate("ABC124")
                .build();

        entityManager.persistAndFlush(vehicle);

        // When - try to delete with wrong customer ID
        vehicleRepository.deleteByIdAndCustomerId("VEH-DELETE-456", "CUST-WRONG");
        entityManager.flush();

        // Then - should still exist since customer ID didn't match
        Optional<Vehicle> afterDelete = vehicleRepository.findByIdAndCustomerId("VEH-DELETE-456", "CUST-123");
        assertTrue(afterDelete.isPresent());
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
        assertEquals("X5", found.get().getModel());
        assertEquals(2023, found.get().getYear());
    }

    @Test
    void testUniqueVinConstraint() {
        // Given
        Vehicle vehicle1 = Vehicle.builder()
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("DUPLICATE_VIN_123")
                .licensePlate("ABC123")
                .build();

        Vehicle vehicle2 = Vehicle.builder()
                .customerId("CUST-456")
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("DUPLICATE_VIN_123") // Same VIN
                .licensePlate("XYZ789")
                .build();

        // When & Then
        vehicleRepository.save(vehicle1);
        entityManager.flush();

        // Trying to save second vehicle with same VIN should throw exception
        assertThrows(Exception.class, () -> {
            vehicleRepository.save(vehicle2);
            entityManager.flush();
        });
    }

    @Test
    void testFindAll() {
        // Given - clean state, add some vehicles
        Vehicle vehicle1 = Vehicle.builder()
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .build();

        Vehicle vehicle2 = Vehicle.builder()
                .customerId("CUST-456")
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("2HGFC2F59MH123456")
                .licensePlate("XYZ789")
                .build();

        entityManager.persistAndFlush(vehicle1);
        entityManager.persistAndFlush(vehicle2);

        // When
        List<Vehicle> allVehicles = vehicleRepository.findAll();

        // Then
        assertTrue(allVehicles.size() >= 2);
        assertTrue(allVehicles.stream().anyMatch(v -> v.getVin().equals("1HGBH41JXMN109186")));
        assertTrue(allVehicles.stream().anyMatch(v -> v.getVin().equals("2HGFC2F59MH123456")));
    }
}