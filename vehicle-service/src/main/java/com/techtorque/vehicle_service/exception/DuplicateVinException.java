package com.techtorque.vehicle_service.exception;

public class DuplicateVinException extends RuntimeException {
    public DuplicateVinException(String vin) {
        super(String.format("A vehicle with VIN '%s' already exists in the system", vin));
    }
}
