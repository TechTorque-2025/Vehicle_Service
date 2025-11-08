package com.techtorque.vehicle_service.seeder;

import com.techtorque.vehicle_service.constants.SeedDataConstants;
import com.techtorque.vehicle_service.entity.Vehicle;
import com.techtorque.vehicle_service.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.List;

/**
 * Data seeder for Vehicle Service.
 * Seeds sample vehicles for development and testing purposes.
 * Only runs in 'dev' profile to prevent accidental data seeding in production.
 */
@Configuration
@Slf4j
public class VehicleDataSeeder {

    /**
     * Seeds sample vehicles for test customers.
     * Creates realistic vehicle data with various makes, models, and years.
     * 
     * @param vehicleRepository Repository for vehicle data access
     * @return CommandLineRunner that seeds data on application startup
     */
    @Bean
    @Order(1)
    @Profile("dev") // Only run in dev profile
    public CommandLineRunner seedVehicleData(VehicleRepository vehicleRepository) {
        return args -> {
            log.info("=== Starting Vehicle Data Seeding (dev profile) ===");
            
            // Check if seed data already exists by checking for specific vehicle IDs
            if (vehicleRepository.existsById(SeedDataConstants.VEHICLE_1_ID)) {
                log.info("Seed data already exists. Skipping vehicle seeding.");
                long totalVehicles = vehicleRepository.count();
                log.info("Database contains {} vehicles.", totalVehicles);
                return;
            }
            
            // Create sample vehicles for Customer 1 (John Doe)
            // Note: Don't set createdAt/updatedAt - let @CreationTimestamp and @UpdateTimestamp handle them
            List<Vehicle> customer1Vehicles = Arrays.asList(
                Vehicle.builder()
                    .id(SeedDataConstants.VEHICLE_1_ID)
                    .customerId(SeedDataConstants.CUSTOMER_1_ID)
                    .make("Toyota")
                    .model("Camry")
                    .year(2022)
                    .vin("4T1B11HK5NU123456") // Valid VIN format
                    .licensePlate("ABC-1234")
                    .color("Silver")
                    .mileage(15000)
                    .build(),
                    
                Vehicle.builder()
                    .id(SeedDataConstants.VEHICLE_2_ID)
                    .customerId(SeedDataConstants.CUSTOMER_1_ID)
                    .make("Honda")
                    .model("Accord")
                    .year(2021)
                    .vin("1HGCV1F36LA123789") // Valid VIN format
                    .licensePlate("XYZ-5678")
                    .color("Black")
                    .mileage(28000)
                    .build()
            );
            
            // Create sample vehicles for Customer 2 (Jane Smith)
            List<Vehicle> customer2Vehicles = Arrays.asList(
                Vehicle.builder()
                    .id(SeedDataConstants.VEHICLE_3_ID)
                    .customerId(SeedDataConstants.CUSTOMER_2_ID)
                    .make("BMW")
                    .model("X5")
                    .year(2023)
                    .vin("5UXCR6C53N9A12345") // Valid VIN format
                    .licensePlate("BMW-2023")
                    .color("White")
                    .mileage(8500)
                    .build(),
                    
                Vehicle.builder()
                    .id(SeedDataConstants.VEHICLE_4_ID)
                    .customerId(SeedDataConstants.CUSTOMER_2_ID)
                    .make("Mercedes-Benz")
                    .model("C 300")
                    .year(2020)
                    .vin("55SWF4KB7LU123456") // Valid VIN format
                    .licensePlate("MERC-300")
                    .color("Blue")
                    .mileage(42000)
                    .build()
            );
            
            // Create sample vehicles for Customer 3 (Bob Johnson)
            List<Vehicle> customer3Vehicles = Arrays.asList(
                Vehicle.builder()
                    .id(SeedDataConstants.VEHICLE_5_ID)
                    .customerId(SeedDataConstants.CUSTOMER_3_ID)
                    .make("Nissan")
                    .model("Altima")
                    .year(2022)
                    .vin("1N4BL4BV5NC123456") // Valid VIN format
                    .licensePlate("NIS-2022")
                    .color("Red")
                    .mileage(18500)
                    .build(),
                    
                Vehicle.builder()
                    .id(SeedDataConstants.VEHICLE_6_ID)
                    .customerId(SeedDataConstants.CUSTOMER_3_ID)
                    .make("Mazda")
                    .model("CX-5")
                    .year(2019)
                    .vin("JM3KFBCM5K0123456") // Valid VIN format
                    .licensePlate("MAZ-CX5")
                    .color("Gray")
                    .mileage(55000)
                    .build()
            );
            
            // Save all vehicles - using saveAll which will handle the persistence correctly
            log.info("Seeding vehicles for Customer 1 ({})...", SeedDataConstants.CUSTOMER_1_NAME);
            vehicleRepository.saveAll(customer1Vehicles);
            log.info("✓ Created {} vehicles for Customer 1", customer1Vehicles.size());
            
            log.info("Seeding vehicles for Customer 2 ({})...", SeedDataConstants.CUSTOMER_2_NAME);
            vehicleRepository.saveAll(customer2Vehicles);
            log.info("✓ Created {} vehicles for Customer 2", customer2Vehicles.size());
            
            log.info("Seeding vehicles for Customer 3 ({})...", SeedDataConstants.CUSTOMER_3_NAME);
            vehicleRepository.saveAll(customer3Vehicles);
            log.info("✓ Created {} vehicles for Customer 3", customer3Vehicles.size());
            
            long totalVehicles = vehicleRepository.count();
            log.info("=== Vehicle Data Seeding Complete: {} total vehicles created ===", totalVehicles);
            
            // Log summary
            log.info("\nVehicle Seed Data Summary:");
            log.info("  - Customer 1 ({}, ID: {}): {} vehicles", 
                SeedDataConstants.CUSTOMER_1_NAME, 
                SeedDataConstants.CUSTOMER_1_ID, 
                customer1Vehicles.size());
            log.info("  - Customer 2 ({}, ID: {}): {} vehicles", 
                SeedDataConstants.CUSTOMER_2_NAME, 
                SeedDataConstants.CUSTOMER_2_ID, 
                customer2Vehicles.size());
            log.info("  - Customer 3 ({}, ID: {}): {} vehicles", 
                SeedDataConstants.CUSTOMER_3_NAME, 
                SeedDataConstants.CUSTOMER_3_ID, 
                customer3Vehicles.size());
            
            log.info("\nVehicle Details:");
            vehicleRepository.findAll().forEach(vehicle -> 
                log.info("  - {} {} {} (VIN: {}, Mileage: {}km)", 
                    vehicle.getYear(), 
                    vehicle.getMake(), 
                    vehicle.getModel(), 
                    vehicle.getVin(), 
                    vehicle.getMileage())
            );
        };
    }
}
