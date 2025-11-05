package com.techtorque.vehicle_service.constants;

/**
 * Shared constants for seed data across all microservices.
 * These UUIDs should match the users created by the Authentication Service seeder.
 * 
 * IMPORTANT: These IDs must be coordinated with the Authentication Service to ensure
 * cross-service data consistency.
 */
public class SeedDataConstants {
    
    // User IDs from Auth Service
    // Note: Auth service generates dynamic UUIDs, but for dev environment we use fixed IDs
    // These should be updated to match actual Auth service seed data
    
    public static final String CUSTOMER_1_ID = "customer1-uuid-0000-0000-000000000001";
    public static final String CUSTOMER_2_ID = "customer2-uuid-0000-0000-000000000002";
    public static final String CUSTOMER_3_ID = "customer3-uuid-0000-0000-000000000003";
    
    public static final String EMPLOYEE_1_ID = "employee1-uuid-0000-0000-000000000001";
    public static final String EMPLOYEE_2_ID = "employee2-uuid-0000-0000-000000000002";
    public static final String EMPLOYEE_3_ID = "employee3-uuid-0000-0000-000000000003";
    
    public static final String ADMIN_ID = "admin000-uuid-0000-0000-000000000001";
    public static final String SUPER_ADMIN_ID = "superadm-uuid-0000-0000-000000000001";
    
    // Vehicle seed data IDs
    public static final String VEHICLE_1_ID = "VEH-2022-TOYOTA-CAMRY-0001";
    public static final String VEHICLE_2_ID = "VEH-2021-HONDA-ACCORD-0002";
    public static final String VEHICLE_3_ID = "VEH-2023-BMW-X5-0003";
    public static final String VEHICLE_4_ID = "VEH-2020-MERCEDES-C300-0004";
    public static final String VEHICLE_5_ID = "VEH-2022-NISSAN-ALTIMA-0005";
    public static final String VEHICLE_6_ID = "VEH-2019-MAZDA-CX5-0006";
    
    // Customer names for logging
    public static final String CUSTOMER_1_NAME = "John Doe";
    public static final String CUSTOMER_2_NAME = "Jane Smith";
    public static final String CUSTOMER_3_NAME = "Bob Johnson";
    
    private SeedDataConstants() {
        // Private constructor to prevent instantiation
    }
}
