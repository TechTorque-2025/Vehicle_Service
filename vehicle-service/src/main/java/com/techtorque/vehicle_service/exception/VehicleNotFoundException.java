package com.techtorque.vehicle_service.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }

    public VehicleNotFoundException(String vehicleId, String customerId) {
        super(String.format("Vehicle with ID '%s' not found for customer '%s'", vehicleId, customerId));
    }
}

