package com.techtorque.vehicle_service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleUpdateDtoTest {

    @Test
    void testBuilder() {
        VehicleUpdateDto dto = VehicleUpdateDto.builder()
                .color("Blue")
                .mileage(25000)
                .build();

        assertEquals("Blue", dto.getColor());
        assertEquals(25000, dto.getMileage());
    }

    @Test
    void testEqualsAndHashCode() {
        VehicleUpdateDto dto1 = VehicleUpdateDto.builder()
                .color("Red")
                .mileage(30000)
                .build();

        VehicleUpdateDto dto2 = VehicleUpdateDto.builder()
                .color("Red")
                .mileage(30000)
                .build();

        VehicleUpdateDto dto3 = VehicleUpdateDto.builder()
                .color("Blue")
                .mileage(25000)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
    }

    @Test
    void testToString() {
        VehicleUpdateDto dto = VehicleUpdateDto.builder()
                .color("Green")
                .mileage(40000)
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("Green"));
        assertTrue(toString.contains("40000"));
    }

    @Test
    void testNullValues() {
        VehicleUpdateDto dto = VehicleUpdateDto.builder()
                .color(null)
                .mileage(null)
                .build();

        assertNull(dto.getColor());
        assertNull(dto.getMileage());
    }
}