package com.techtorque.vehicle_service.exception;

public class DuplicateVinException extends RuntimeException {
    public DuplicateVinException(String message) {
        super(message);
    }

    public DuplicateVinException(String message, Throwable cause) {
        super(message, cause);
    }
}
