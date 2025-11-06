package com.techtorque.vehicle_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for vehicle year
 * Ensures year is between 1900 and current year + 1 (for upcoming models)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = VehicleYearValidator.class)
@Documented
public @interface ValidVehicleYear {

    String message() default "Year must be between 1900 and next year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
