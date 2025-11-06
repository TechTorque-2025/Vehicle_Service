package com.techtorque.vehicle_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

/**
 * Validator implementation for ValidVehicleYear annotation
 * Validates that vehicle year is between 1900 and next year
 */
public class VehicleYearValidator implements ConstraintValidator<ValidVehicleYear, Integer> {

    private static final int MIN_YEAR = 1900;

    @Override
    public void initialize(ValidVehicleYear constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true; // Let @NotNull handle null validation
        }

        int currentYear = Year.now().getValue();
        int maxYear = currentYear + 1; // Allow next year for upcoming models

        if (year < MIN_YEAR || year > maxYear) {
            // Custom message with dynamic year
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Year must be between %d and %d (current year + 1)", MIN_YEAR, maxYear)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
