package com.techtorque.vehicle_service.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class VehicleEntityTest {

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();
    }

    @Test
    void testVehicleCreation() {
        assertNotNull(vehicle);
        assertEquals("CUST-123", vehicle.getCustomerId());
        assertEquals("Toyota", vehicle.getMake());
        assertEquals("Camry", vehicle.getModel());
        assertEquals(2022, vehicle.getYear());
        assertEquals("1HGBH41JXMN109186", vehicle.getVin());
        assertEquals("ABC123", vehicle.getLicensePlate());
        assertEquals("Silver", vehicle.getColor());
        assertEquals(15000, vehicle.getMileage());
    }

    @Test
    void testGenerateId() {
        // Simulate @PrePersist behavior
        vehicle.generateId();

        assertNotNull(vehicle.getId());
        assertTrue(vehicle.getId().startsWith("VEH-2022-TOYOTA-CAMRY-"));
        assertEquals(21, vehicle.getId().length()); // VEH-YYYY-MAKE-MODEL-XXXX format
    }

    @Test
    void testGenerateIdWithSpecialCharacters() {
        vehicle.setMake("BMW X3");
        vehicle.setModel("M-Sport");
        vehicle.generateId();

        assertNotNull(vehicle.getId());
        assertTrue(vehicle.getId().startsWith("VEH-2022-BMWX3-MSPORT-"));
        assertTrue(vehicle.getId().matches("VEH-2022-BMWX3-MSPORT-[A-Z0-9]{4}"));
    }

    @Test
    void testGenerateIdDoesNotOverrideExisting() {
        String existingId = "VEH-CUSTOM-ID";
        vehicle.setId(existingId);
        vehicle.generateId();

        assertEquals(existingId, vehicle.getId());
    }

    @Test
    void testEqualsAndHashCode() {
        Vehicle vehicle1 = Vehicle.builder()
                .id("VEH-1")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .build();

        Vehicle vehicle2 = Vehicle.builder()
                .id("VEH-1")
                .customerId("CUST-123")
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .build();

        assertEquals(vehicle1, vehicle2);
        assertEquals(vehicle1.hashCode(), vehicle2.hashCode());
    }

    @Test
    void testToString() {
        String toString = vehicle.toString();

        assertTrue(toString.contains("Toyota"));
        assertTrue(toString.contains("Camry"));
        assertTrue(toString.contains("2022"));
        assertTrue(toString.contains("1HGBH41JXMN109186"));
    }

    @Test
    void testBuilderPattern() {
        Vehicle testVehicle = Vehicle.builder()
                .customerId("CUST-456")
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("2HGFC2F59MH123456")
                .licensePlate("XYZ789")
                .color("Blue")
                .mileage(25000)
                .build();

        assertEquals("CUST-456", testVehicle.getCustomerId());
        assertEquals("Honda", testVehicle.getMake());
        assertEquals("Civic", testVehicle.getModel());
        assertEquals(2021, testVehicle.getYear());
    }

    @Test
    void testNoArgsConstructor() {
        Vehicle emptyVehicle = new Vehicle();
        assertNotNull(emptyVehicle);
        assertNull(emptyVehicle.getId());
        assertNull(emptyVehicle.getMake());
        assertEquals(0, emptyVehicle.getYear());
    }

    @Test
    void testAllArgsConstructor() {
        Vehicle testVehicle = new Vehicle(
                "VEH-123",
                "CUST-789",
                "Ford",
                "F-150",
                2020,
                "1FTFW1ET5LKD12345",
                "FORD123",
                "Red",
                50000,
                null,
                null);

        assertEquals("VEH-123", testVehicle.getId());
        assertEquals("CUST-789", testVehicle.getCustomerId());
        assertEquals("Ford", testVehicle.getMake());
        assertEquals("F-150", testVehicle.getModel());
    }
}