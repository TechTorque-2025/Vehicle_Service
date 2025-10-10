# 🚗 Vehicle Management Service

This microservice is responsible for managing all vehicle-related information for customers.

**Assigned Team:** Akith, Pramudi

### 🎯 Key Responsibilities

-   Allow authenticated customers to register, update, and remove vehicles on their profile.
-   List all vehicles associated with a customer.
-   Provide detailed information for a specific vehicle.
-   Handle vehicle photo uploads.

### ⚙️ Tech Stack

-   **Framework:** Java / Spring Boot
-   **Database:** PostgreSQL
-   **Security:** Spring Security (consumes JWTs)

### ℹ️ API Information

-   **Local Port:** `8082`
-   **Swagger UI:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

### 🚀 Running Locally

This service is designed to be run as part of the main `docker-compose` setup from the project's root directory.

```bash
# From the root of the TechTorque-2025 project
docker-compose up --build vehicle-service