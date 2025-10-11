# 🚗 Vehicle Management Service

## 🚦 Build Status

**main**

[![Build and Test Vehicle Service](https://github.com/TechTorque-2025/Vehicle_Service/actions/workflows/buildtest.yaml/badge.svg)](https://github.com/TechTorque-2025/Vehicle_Service/actions/workflows/buildtest.yaml)

**dev**

[![Build and Test Vehicle Service](https://github.com/TechTorque-2025/Vehicle_Service/actions/workflows/buildtest.yaml/badge.svg?branch=dev)](https://github.com/TechTorque-2025/Vehicle_Service/actions/workflows/buildtest.yaml)

This microservice is responsible for managing all vehicle-related information for customers.

**Assigned Team:** Akith, Pramudi

### 🎯 Key Responsibilities

- Allow authenticated customers to register, update, and remove vehicles on their profile.
- List all vehicles associated with a customer.
- Provide detailed information for a specific vehicle.
- Handle vehicle photo uploads.

### ⚙️ Tech Stack

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

- **Framework:** Java / Spring Boot
- **Database:** PostgreSQL
- **Security:** Spring Security (consumes JWTs)

### ℹ️ API Information

- **Local Port:** `8082`
- **Swagger UI:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

### 🚀 Running Locally

This service is designed to be run as part of the main `docker-compose` setup from the project's root directory.

```bash
# From the root of the TechTorque-2025 project
docker-compose up --build vehicle-service
```
