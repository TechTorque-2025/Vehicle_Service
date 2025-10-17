package com.techtorque.vehicle_service.exception;

public class PhotoUploadException extends RuntimeException {
    public PhotoUploadException(String message) {
        super(message);
    }

    public PhotoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
