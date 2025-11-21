package com.techtorque.vehicle_service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleRequestDtoTest {

    @Test
    void testBuilder() {
        VehicleRequestDto dto = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .licensePlate("ABC123")
                .color("Silver")
                .mileage(15000)
                .build();

        assertEquals("Toyota", dto.getMake());
        assertEquals("Camry", dto.getModel());
        assertEquals(2022, dto.getYear());
        assertEquals("1HGBH41JXMN109186", dto.getVin());
        assertEquals("ABC123", dto.getLicensePlate());
        assertEquals("Silver", dto.getColor());
        assertEquals(15000, dto.getMileage());
    }

    @Test
    void testEqualsAndHashCode() {
        VehicleRequestDto dto1 = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .build();

        VehicleRequestDto dto2 = VehicleRequestDto.builder()
                .make("Toyota")
                .model("Camry")
                .year(2022)
                .vin("1HGBH41JXMN109186")
                .build();

        VehicleRequestDto dto3 = VehicleRequestDto.builder()
                .make("Honda")
                .model("Civic")
                .year(2021)
                .vin("2HGFC2F59MH123456")
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testToString() {
        VehicleRequestDto dto = VehicleRequestDto.builder()
                .make("BMW")
                .model("X5")
                .year(2023)
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("BMW"));
        assertTrue(toString.contains("X5"));
        assertTrue(toString.contains("2023"));
    }
}