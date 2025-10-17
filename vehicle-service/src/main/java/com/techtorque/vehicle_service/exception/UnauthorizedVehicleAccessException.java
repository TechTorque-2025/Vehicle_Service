package com.techtorque.vehicle_service.exception;

public class UnauthorizedVehicleAccessException extends RuntimeException {
    public UnauthorizedVehicleAccessException(String message) {
        super(message);
    }

    public UnauthorizedVehicleAccessException(String vehicleId, String customerId) {
        super(String.format("Customer '%s' is not authorized to access vehicle '%s'", customerId, vehicleId));
    }
}
