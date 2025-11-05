package com.techtorque.vehicle_service.constants;

/**
 * Shared constants for seed data across all microservices.
 * These IDs should match the users created by the Authentication Service seeder.
 * 
 * IMPORTANT: The Gateway forwards X-User-Subject header which contains the USERNAME,
 * not numeric user IDs. Therefore, customer_id in vehicles should be the username.
 */
public class SeedDataConstants {
    
    // User IDs from Auth Service - using USERNAMES as they appear in X-User-Subject header
    // The Auth service creates users: superadmin, admin, employee, customer, user, testuser, demo
    
    public static final String CUSTOMER_1_ID = "customer";
    public static final String CUSTOMER_2_ID = "testuser";
    public static final String CUSTOMER_3_ID = "demo";
    
    public static final String EMPLOYEE_1_ID = "employee";
    public static final String EMPLOYEE_2_ID = "employee"; // Auth service only seeds one employee
    public static final String EMPLOYEE_3_ID = "employee";
    
    public static final String ADMIN_ID = "admin";
    public static final String SUPER_ADMIN_ID = "superadmin";
    
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
