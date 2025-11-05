package com.techtorque.vehicle_service.config;

import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data seeder to populate sample vehicles for testing
 * Only runs in 'dev' profile to avoid polluting production data
 * 
 * Follows the pattern established in Authentication and Time Logging services
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) throws Exception {
        // Only seed data in development profile
        if (!isDevProfile()) {
            logger.info("Not in 'dev' profile. Skipping vehicle data seeding.");
            return;
        }

        // Check if data already exists to avoid duplicates
        if (vehicleRepository.count() > 0) {
            logger.info("Vehicles already exist in database ({} entries). Skipping seeding.",
                       vehicleRepository.count());
            return;
        }

        logger.info("Starting vehicle data seeding for development environment...");
        seedSampleVehicles();
        logger.info("Vehicle data seeding completed successfully!");
    }

    /**
     * Check if 'dev' profile is active
     */
    private boolean isDevProfile() {
        String[] activeProfiles = env.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Seed sample vehicles for testing
     * Creates realistic test data for different customers with various vehicle types
     */
    private void seedSampleVehicles() {
        // Sample customer IDs (corresponding to users from Auth service)
        // These should match the users created in Auth DataSeeder
        String customerUserId = getUserIdByUsername("customer"); // From auth service
        String testUserId = getUserIdByUsername("testuser");
        String demoUserId = getUserIdByUsername("demo");
        String userUserId = getUserIdByUsername("user");

        // Since we can't actually query auth service in seeder, we'll use placeholder IDs
        // In a real scenario, these would be fetched from auth service or use fixed UUIDs
        String[] customerIds = {"customer", "testuser", "demo", "user"};

        // Vehicle data arrays for realistic combinations
        String[] makes = {"Toyota", "Honda", "Nissan", "BMW", "Mercedes-Benz", "Audi", "Ford", "Chevrolet", "Volkswagen", "Hyundai"};
        String[] toyotaModels = {"Corolla", "Camry", "RAV4", "Prius", "Land Cruiser"};
        String[] hondaModels = {"Civic", "Accord", "CR-V", "Fit", "Pilot"};
        String[] nissanModels = {"Altima", "Rogue", "Sentra", "Maxima", "Pathfinder"};
        String[] bmwModels = {"3 Series", "5 Series", "X3", "X5", "7 Series"};
        String[] colors = {"Black", "White", "Silver", "Blue", "Red", "Gray", "Green", "Pearl White", "Midnight Blue"};

        int vehicleCount = 0;

        // Create vehicles for customer user
        vehicleCount += createVehicle(customerIds[0], "Toyota", "Camry", 2022, 
            generateVIN("Toyota"), "CAA-1234", "Pearl White", 15000);
        vehicleCount += createVehicle(customerIds[0], "Honda", "CR-V", 2021, 
            generateVIN("Honda"), "CAB-5678", "Silver", 28000);

        // Create vehicles for testuser
        vehicleCount += createVehicle(customerIds[1], "BMW", "3 Series", 2023, 
            generateVIN("BMW"), "CAC-9012", "Black", 8000);
        vehicleCount += createVehicle(customerIds[1], "Nissan", "Rogue", 2020, 
            generateVIN("Nissan"), "CAD-3456", "Blue", 45000);

        // Create vehicles for demo user
        vehicleCount += createVehicle(customerIds[2], "Mercedes-Benz", "C-Class", 2024, 
            generateVIN("Mercedes-Benz"), "CAE-7890", "Midnight Blue", 5000);
        vehicleCount += createVehicle(customerIds[2], "Audi", "A4", 2022, 
            generateVIN("Audi"), "CAF-2345", "Gray", 18000);
        vehicleCount += createVehicle(customerIds[2], "Toyota", "RAV4", 2021, 
            generateVIN("Toyota"), "CAG-6789", "Red", 32000);

        // Create vehicles for user
        vehicleCount += createVehicle(customerIds[3], "Ford", "F-150", 2023, 
            generateVIN("Ford"), "CAH-0123", "White", 12000);
        vehicleCount += createVehicle(customerIds[3], "Chevrolet", "Malibu", 2020, 
            generateVIN("Chevrolet"), "CAI-4567", "Black", 38000);
        vehicleCount += createVehicle(customerIds[3], "Volkswagen", "Jetta", 2022, 
            generateVIN("Volkswagen"), "CAJ-8901", "Silver", 22000);

        // Additional vehicles for variety
        vehicleCount += createVehicle(customerIds[0], "Hyundai", "Tucson", 2023, 
            generateVIN("Hyundai"), "CAK-2345", "Blue", 10000);
        vehicleCount += createVehicle(customerIds[1], "Toyota", "Prius", 2024, 
            generateVIN("Toyota"), "CAL-6789", "Green", 3000);
        vehicleCount += createVehicle(customerIds[2], "Honda", "Accord", 2022, 
            generateVIN("Honda"), "CAM-0123", "White", 19000);
        vehicleCount += createVehicle(customerIds[3], "Nissan", "Altima", 2021, 
            generateVIN("Nissan"), "CAN-4567", "Silver", 27000);

        long totalCount = vehicleRepository.count();
        logger.info("✅ Successfully seeded {} vehicles across {} customers",
                   totalCount, customerIds.length);

        // Log summary statistics
        for (String customerId : customerIds) {
            long count = vehicleRepository.countByCustomerId(customerId);
            if (count > 0) {
                logger.info("   Customer '{}': {} vehicles", customerId, count);
            }
        }
    }

    /**
     * Create and save a vehicle
     * Returns 1 if successful, 0 if failed (for counting)
     */
    private int createVehicle(String customerId, String make, String model, int year,
                              String vin, String licensePlate, String color, int mileage) {
        try {
            Vehicle vehicle = Vehicle.builder()
                    .customerId(customerId)
                    .make(make)
                    .model(model)
                    .year(year)
                    .vin(vin)
                    .licensePlate(licensePlate)
                    .color(color)
                    .mileage(mileage)
                    .build();

            vehicleRepository.save(vehicle);
            logger.debug("Created vehicle: {} {} {} for customer {}", 
                        year, make, model, customerId);
            return 1;
        } catch (Exception e) {
            logger.error("Failed to create vehicle {} {} for customer {}: {}", 
                        make, model, customerId, e.getMessage());
            return 0;
        }
    }

    /**
     * Generate a realistic VIN (Vehicle Identification Number)
     * Format: 17 characters (simplified for testing)
     */
    private String generateVIN(String make) {
        // VIN structure (simplified): 
        // 1-3: World Manufacturer Identifier (WMI)
        // 4-9: Vehicle Descriptor Section (VDS)
        // 10-17: Vehicle Identifier Section (VIS)
        
        String wmi = getManufacturerCode(make);
        String vds = String.format("%06d", (int)(Math.random() * 1000000));
        String vis = String.format("%08d", (int)(Math.random() * 100000000));
        
        return wmi + vds + vis;
    }

    /**
     * Get manufacturer code for VIN
     */
    private String getManufacturerCode(String make) {
        switch (make.toUpperCase()) {
            case "TOYOTA": return "JTD";
            case "HONDA": return "JHM";
            case "NISSAN": return "JN1";
            case "BMW": return "WBA";
            case "MERCEDES-BENZ": return "WDD";
            case "AUDI": return "WAU";
            case "FORD": return "1FA";
            case "CHEVROLET": return "1G1";
            case "VOLKSWAGEN": return "WVW";
            case "HYUNDAI": return "KMH";
            default: return "XXX";
        }
    }

    /**
     * Helper method to simulate getting user ID by username
     * In a real scenario, this would query the auth service
     * For now, we return the username itself as placeholder
     */
    private String getUserIdByUsername(String username) {
        // In production, this would be a REST call to auth service
        // or use fixed UUIDs shared across services
        return username;
    }
}
