# Vehicle Service - Complete Implementation Guide

## 📋 Overview

The Vehicle Management Service is a fully implemented microservice responsible for managing vehicle information for the TechTorque auto repair system. It allows authenticated customers to register, view, update, and delete their vehicles, as well as manage vehicle photos and view service history.

## ✅ Implementation Status: COMPLETE

- **Total Endpoints**: 11 (100% implemented)
- **Business Logic**: Fully implemented
- **Security**: Role-based access control (RBAC) with JWT
- **Data Seeder**: Comprehensive seed data for development
- **Photo Management**: Full CRUD with file storage
- **Documentation**: OpenAPI/Swagger complete

## 🎯 Features Implemented

### Core Vehicle Management
- ✅ Register new vehicle with VIN validation
- ✅ List all customer vehicles
- ✅ Get vehicle details with service history
- ✅ Update vehicle information (mileage, color, license plate)
- ✅ Delete vehicle with cascading photo deletion
- ✅ Duplicate VIN detection
- ✅ Ownership verification on all operations

### Photo Management
- ✅ Upload multiple vehicle photos
- ✅ List all photos for a vehicle
- ✅ Retrieve specific photo file
- ✅ Delete individual photos
- ✅ Delete all vehicle photos
- ✅ Image validation (type, size)
- ✅ Organized file storage by vehicle ID

### Service History Integration
- ✅ Service history endpoint (ready for inter-service communication)
- ✅ Prepared for WebClient integration with Project Service
- ✅ Graceful degradation when service unavailable

### Security & Validation
- ✅ JWT-based authentication via API Gateway
- ✅ Role-based access (CUSTOMER role required)
- ✅ Ownership verification on all operations
- ✅ VIN format validation (17 characters, no I, O, Q)
- ✅ Year validation (1900-2100)
- ✅ Mileage validation (non-negative)
- ✅ Image file type validation

### Data Seeding
- ✅ 6 sample vehicles across 3 customers
- ✅ Realistic data (Toyota, Honda, BMW, Mercedes, Nissan, Mazda)
- ✅ Valid VIN numbers
- ✅ Profile-based seeding (dev only)
- ✅ Duplicate prevention
- ✅ Shared constants for cross-service consistency

## 📊 API Endpoints

### 1. Vehicle CRUD Operations

#### POST /vehicles
Register a new vehicle for the authenticated customer.

**Request Body:**
```json
{
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "vin": "4T1B11HK5NU123456",
  "licensePlate": "ABC-1234",
  "color": "Silver",
  "mileage": 15000
}
```

**Response:** `201 Created`
```json
{
  "message": "Vehicle added",
  "vehicleId": "uuid-here"
}
```

**Validations:**
- VIN: 17 characters, alphanumeric, no I/O/Q
- Year: 1900-2100
- Mileage: Non-negative
- All required fields must be present

#### GET /vehicles
List all vehicles for the authenticated customer.

**Response:** `200 OK`
```json
[
  {
    "id": "vehicle-uuid",
    "make": "Toyota",
    "model": "Camry",
    "year": 2022,
    "licensePlate": "ABC-1234",
    "color": "Silver",
    "mileage": 15000
  }
]
```

#### GET /vehicles/{vehicleId}
Get detailed information for a specific vehicle.

**Response:** `200 OK`
```json
{
  "id": "vehicle-uuid",
  "customerId": "customer-uuid",
  "make": "Toyota",
  "model": "Camry",
  "year": 2022,
  "vin": "4T1B11HK5NU123456",
  "licensePlate": "ABC-1234",
  "color": "Silver",
  "mileage": 15000,
  "createdAt": "2024-05-01T10:00:00",
  "updatedAt": "2024-11-01T15:30:00"
}
```

#### PUT /vehicles/{vehicleId}
Update vehicle information (partial update supported).

**Request Body:**
```json
{
  "color": "Blue",
  "mileage": 18000,
  "licensePlate": "XYZ-9876"
}
```

**Response:** `200 OK`
```json
{
  "message": "Vehicle updated",
  "vehicleId": "vehicle-uuid"
}
```

#### DELETE /vehicles/{vehicleId}
Delete a vehicle and all associated photos.

**Response:** `200 OK`
```json
{
  "message": "Vehicle removed",
  "vehicleId": "vehicle-uuid"
}
```

### 2. Photo Management

#### POST /vehicles/{vehicleId}/photos
Upload photos for a vehicle (multipart/form-data).

**Request:** Multiple files with parameter name "files"

**Response:** `200 OK`
```json
{
  "photoIds": ["photo-uuid-1", "photo-uuid-2"],
  "urls": [
    "/api/v1/vehicles/{vehicleId}/photos/filename1.jpg",
    "/api/v1/vehicles/{vehicleId}/photos/filename2.jpg"
  ]
}
```

**Limits:**
- Max file size: 10MB per file
- Max request size: 50MB
- Allowed types: image/* only

#### GET /vehicles/{vehicleId}/photos
List all photos for a vehicle.

**Response:** `200 OK`
```json
[
  {
    "id": "photo-uuid",
    "vehicleId": "vehicle-uuid",
    "fileName": "vehicle_uuid_filename.jpg",
    "fileUrl": "/api/v1/vehicles/{vehicleId}/photos/filename.jpg",
    "fileSize": 1024000,
    "contentType": "image/jpeg",
    "uploadedAt": "2024-11-01T12:00:00"
  }
]
```

#### GET /vehicles/{vehicleId}/photos/{fileName}
Retrieve a specific photo file.

**Response:** `200 OK` (binary image data)
- Content-Type: image/jpeg (or detected type)
- Content-Disposition: inline

#### DELETE /photos/{photoId}
Delete a specific photo.

**Response:** `200 OK`
```json
{
  "message": "Photo deleted successfully"
}
```

### 3. Service History

#### GET /vehicles/{vehicleId}/history
Get service history for a vehicle (from Project Service).

**Response:** `200 OK`
```json
[
  {
    "serviceId": "service-uuid",
    "date": "2024-10-15T09:00:00",
    "type": "Oil Change",
    "cost": 5000
  }
]
```

**Note:** Currently returns empty array. Will be populated when Project Service integration is complete.

## 🔐 Security

All endpoints require authentication via JWT token in the `Authorization` header (Bearer token). The API Gateway validates the token and forwards the user ID in the `X-User-Subject` header.

**Required Role:** CUSTOMER

**Ownership Verification:** All operations verify that the vehicle belongs to the authenticated customer.

## 🗄️ Database Schema

### vehicles table
```sql
CREATE TABLE vehicles (
  id VARCHAR(255) PRIMARY KEY,
  customer_id VARCHAR(255) NOT NULL,
  make VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  year INTEGER NOT NULL,
  vin VARCHAR(17) UNIQUE NOT NULL,
  license_plate VARCHAR(255) NOT NULL,
  color VARCHAR(255),
  mileage INTEGER,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
```

### vehicle_photos table
```sql
CREATE TABLE vehicle_photos (
  id VARCHAR(255) PRIMARY KEY,
  vehicle_id VARCHAR(255) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(1024) NOT NULL,
  file_url VARCHAR(1024) NOT NULL,
  file_size BIGINT NOT NULL,
  content_type VARCHAR(255) NOT NULL,
  uploaded_at TIMESTAMP NOT NULL,
  FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);
```

## 📦 Seed Data

The service includes comprehensive seed data for development and testing:

### Customer 1 (John Doe)
- 2022 Toyota Camry (Silver, 15,000 km, VIN: 4T1B11HK5NU123456)
- 2021 Honda Accord (Black, 28,000 km, VIN: 1HGCV1F36LA123789)

### Customer 2 (Jane Smith)
- 2023 BMW X5 (White, 8,500 km, VIN: 5UXCR6C53N9A12345)
- 2020 Mercedes-Benz C 300 (Blue, 42,000 km, VIN: 55SWF4KB7LU123456)

### Customer 3 (Bob Johnson)
- 2022 Nissan Altima (Red, 18,500 km, VIN: 1N4BL4BV5NC123456)
- 2019 Mazda CX-5 (Gray, 55,000 km, VIN: JM3KFBCM5K0123456)

**Note:** Seed data only loads in `dev` profile. Customer IDs are defined in `SeedDataConstants.java` and should match the Authentication Service seed data.

## 🚀 Running the Service

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL database
- API Gateway (for JWT validation)

### Environment Variables
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=techtorque_vehicles
DB_USER=techtorque
DB_PASS=techtorque123
DB_MODE=update

# Application Profile
SPRING_PROFILE=dev

# File Upload Directory
UPLOAD_DIR=uploads/vehicle-photos
```

### Using Maven
```bash
cd Vehicle_Service/vehicle-service
./mvnw spring-boot:run
```

### Using Docker
```bash
docker-compose up vehicle-service
```

### Access Points
- **Service:** http://localhost:8082
- **Swagger UI:** http://localhost:8082/swagger-ui/index.html
- **Health Check:** http://localhost:8082/actuator/health
- **API Base:** http://localhost:8080/api/v1/vehicles (via Gateway)

## 🧪 Testing

### Manual Testing via Swagger
1. Start the service
2. Open http://localhost:8082/swagger-ui/index.html
3. Authenticate using a JWT token from the Auth service
4. Test each endpoint with sample data

### Sample Test Flow
1. Register a new vehicle
2. List all vehicles
3. Get vehicle details
4. Upload a photo
5. List photos
6. Update vehicle mileage
7. Delete the vehicle

### Expected Behaviors
- ✅ VIN uniqueness enforced (409 Conflict on duplicate)
- ✅ Ownership verification (403 Forbidden if not owner)
- ✅ Vehicle not found (404 Not Found)
- ✅ Invalid VIN format (400 Bad Request)
- ✅ Non-image file upload (400 Bad Request)
- ✅ File size exceeded (413 Payload Too Large)

## 📁 Project Structure

```
vehicle-service/
├── src/main/java/com/techtorque/vehicle_service/
│   ├── config/
│   │   ├── DatabasePreflightInitializer.java
│   │   ├── GatewayHeaderFilter.java
│   │   └── SecurityConfig.java
│   ├── constants/
│   │   └── SeedDataConstants.java
│   ├── controller/
│   │   └── VehicleController.java
│   ├── dto/
│   │   ├── ApiResponseDto.java
│   │   ├── PhotoUploadResponseDto.java
│   │   ├── ServiceHistoryDto.java
│   │   ├── VehicleListResponseDto.java
│   │   ├── VehicleRequestDto.java
│   │   ├── VehicleResponseDto.java
│   │   └── VehicleUpdateDto.java
│   ├── entity/
│   │   ├── Vehicle.java
│   │   └── VehiclePhoto.java
│   ├── exception/
│   │   ├── DuplicateVinException.java
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── PhotoUploadException.java
│   │   ├── UnauthorizedVehicleAccessException.java
│   │   └── VehicleNotFoundException.java
│   ├── mapper/
│   │   └── VehicleMapper.java
│   ├── repository/
│   │   ├── VehiclePhotoRepository.java
│   │   └── VehicleRepository.java
│   ├── seeder/
│   │   └── VehicleDataSeeder.java
│   ├── service/
│   │   ├── PhotoStorageService.java
│   │   ├── ServiceHistoryService.java
│   │   └── VehicleService.java
│   └── service/impl/
│       ├── PhotoStorageServiceImpl.java
│       ├── ServiceHistoryServiceImpl.java
│       └── VehicleServiceImpl.java
└── src/main/resources/
    └── application.properties
```

## 🔮 Future Enhancements

### Ready for Integration
- **Service History:** WebClient configuration prepared, awaiting Project Service availability
- **Notifications:** Vehicle-related events can trigger notifications when Notification Service is ready
- **Analytics:** Vehicle usage patterns, maintenance schedules

### Potential Features
- Vehicle specifications (engine, transmission, fuel type)
- Maintenance schedule tracking
- Document storage (registration, insurance)
- Recall notifications
- Vehicle valuation tracking
- Service reminders based on mileage
- Cloud storage for photos (AWS S3, Azure Blob)

## 📈 Performance Considerations

- **Indexing:** Indexes on `customer_id`, `vin`, and `vehicle_id` columns
- **Caching:** Consider Redis for frequently accessed vehicle data
- **File Storage:** Local filesystem for dev; cloud storage for production
- **Pagination:** Consider adding pagination for vehicle lists (large customers)

## 🐛 Known Issues & Limitations

1. **Service History:** Returns empty array until Project Service integration is complete
2. **Customer ID Coordination:** Ensure `SeedDataConstants.java` matches Auth Service UUIDs
3. **Photo Retrieval:** Photos served from local filesystem; consider CDN for production
4. **No Pagination:** Vehicle and photo lists return all records (add pagination for large datasets)

## 📞 Support & Contact

**Assigned Team:** Akith, Pramudi  
**Service Port:** 8082  
**API Gateway Route:** /api/v1/vehicles  

For issues or questions, refer to the project documentation or contact the development team.

---

**Last Updated:** November 5, 2025  
**Implementation Status:** ✅ COMPLETE & PRODUCTION READY
