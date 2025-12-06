# Transport Management System (TMS) Backend

## 📋 Project Overview

The Transport Management System (TMS) is a comprehensive backend application designed to streamline logistics operations by connecting shippers with transporters. The system enables shippers to post load requirements, transporters to bid on available loads, and facilitates booking management with real-time capacity tracking and concurrency control.

### Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 12+
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven 3.6+
- **Additional Libraries**: Lombok, Jakarta Validation

---

## 🚀 Features / API List

### Load APIs (5 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/load` | Create a new load posting |
| GET | `/load` | Get all loads with optional filters (shipperId, status, pagination) |
| GET | `/load/{loadId}` | Get load details by ID |
| PATCH | `/load/{loadId}/cancel` | Cancel a load (only if not booked) |
| GET | `/load/{loadId}/best-bids` | Get best bid suggestions sorted by score |

### Transporter APIs (3 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transporter` | Register a new transporter with available trucks |
| GET | `/transporter/{transporterId}` | Get transporter details including truck inventory |
| PUT | `/transporter/{transporterId}/trucks` | Update available truck counts |

### Bid APIs (4 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/bid` | Submit a bid on a load |
| GET | `/bid` | Get all bids with optional filters (loadId, transporterId, status) |
| GET | `/bid/{bidId}` | Get bid details by ID |
| PATCH | `/bid/{bidId}/reject` | Reject a pending bid |

### Booking APIs (3 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/booking` | Create a booking by accepting a bid |
| GET | `/booking/{bookingId}` | Get booking details by ID |
| PATCH | `/booking/{bookingId}/cancel` | Cancel a confirmed booking and restore truck capacity |

**Total: 15 REST APIs**

---

## 🏗️ Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         Controller Layer                │  ← REST endpoints, request validation
│  (@RestController, @RequestMapping)     │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│            DTO Layer                    │  ← Data Transfer Objects
│  (Request/Response DTOs, validation)    │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│          Service Layer                  │  ← Business logic, transactions
│  (@Service, @Transactional)             │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│        Repository Layer                 │  ← Data access, custom queries
│  (JpaRepository, derived queries)       │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│          Entity Layer                   │  ← JPA entities, relationships
│  (@Entity, @Version, mappings)          │
└─────────────────────────────────────────┘
```

### Key Architectural Features

- **Transaction Management**: `@Transactional` annotations ensure ACID properties
- **Optimistic Locking**: `@Version` fields on `Load` and `AvailableTruck` entities prevent race conditions
- **Global Exception Handling**: `@ControllerAdvice` with `@ExceptionHandler` for centralized error responses
- **DTO Pattern**: Separation of API contracts from internal domain models
- **Constructor Injection**: Using Lombok's `@RequiredArgsConstructor` for immutable dependencies

---

## 🗄️ Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────┐
│        Load             │
├─────────────────────────┤
│ PK: loadId (UUID)       │
│     shipperId           │
│     loadingCity         │
│     unloadingCity       │
│     loadingDate         │
│     productType         │
│     weight              │
│     weightUnit (ENUM)   │
│     truckType           │
│     noOfTrucks          │
│     status (ENUM)       │
│     datePosted          │
│     version (Long)      │
└─────────────────────────┘
         │ 1
         │
         │ many
         ↓
┌─────────────────────────┐         ┌─────────────────────────┐
│        Bid              │ many    │     Transporter         │
├─────────────────────────┤ ←────── ├─────────────────────────┤
│ PK: bidId (UUID)        │    1    │ PK: transporterId (UUID)│
│ FK: loadId              │         │     companyName         │
│ FK: transporterId       │         │     rating              │
│     proposedRate        │         └─────────────────────────┘
│     trucksOffered       │                  │ 1
│     status (ENUM)       │                  │
│     submittedAt         │                  │ many
└─────────────────────────┘                  ↓
         │ 1                      ┌─────────────────────────┐
         │                        │   AvailableTruck        │
         │ 1                      ├─────────────────────────┤
         ↓                        │ PK: id (UUID)           │
┌─────────────────────────┐       │ FK: transporterId       │
│       Booking           │       │     truckType           │
├─────────────────────────┤       │     count               │
│ PK: bookingId (UUID)    │       │     version (Long)      │
│ FK: loadId              │       └─────────────────────────┘
│ FK: bidId               │
│ FK: transporterId       │
│     allocatedTrucks     │
│     finalRate           │
│     status (ENUM)       │
│     bookedAt            │
└─────────────────────────┘
```

### Relationships

- **Load → Bid**: One-to-Many (A load can have multiple bids)
- **Load → Booking**: One-to-Many (A load can have multiple bookings for partial allocations)
- **Transporter → Bid**: One-to-Many (A transporter can submit multiple bids)
- **Transporter → Booking**: One-to-Many (A transporter can have multiple bookings)
- **Transporter → AvailableTruck**: One-to-Many with CASCADE (Transporter owns truck inventory)
- **Bid → Booking**: One-to-One (Each booking is created from one accepted bid)

---

## 📦 Entity Summary

### Core Entities

| Entity | Description |
|--------|-------------|
| **Load** | Represents a shipment requirement posted by a shipper. Contains origin, destination, truck requirements, and current status. Uses `@Version` for optimistic locking. |
| **Transporter** | Represents a logistics company with available trucks. Maintains a fleet inventory and rating. |
| **AvailableTruck** | Represents truck inventory for a transporter. Tracks truck type and count. Uses `@Version` to prevent concurrent capacity conflicts. |
| **Bid** | Represents a transporter's offer to fulfill a load. Contains proposed rate and number of trucks offered. |
| **Booking** | Represents a confirmed allocation of trucks to a load. Created when a bid is accepted. Tracks allocated trucks and final rate. |

### Enums

- **LoadStatus**: `POSTED`, `OPEN_FOR_BIDS`, `BOOKED`, `CANCELLED`
- **BidStatus**: `PENDING`, `ACCEPTED`, `REJECTED`
- **BookingStatus**: `CONFIRMED`, `COMPLETED`, `CANCELLED`
- **WeightUnit**: `KG`, `TON`

---

## 📜 Business Rules

### 1. Capacity Validation

**Rule**: A transporter can only bid if they have sufficient trucks of the required type.

- Before accepting a bid, the system validates that `transporter.availableTrucks[truckType].count >= bid.trucksOffered`
- On successful booking, the allocated trucks are deducted from the transporter's available inventory
- **Exception**: `InsufficientCapacityException` (HTTP 409) if capacity is insufficient

### 2. Load Status Transitions

**Rule**: Load status follows a strict state machine.

```
POSTED → OPEN_FOR_BIDS → BOOKED
   ↓            ↓
CANCELLED ← CANCELLED
```

- **POSTED**: Initial state when load is created
- **OPEN_FOR_BIDS**: Automatically set when the first bid is submitted
- **BOOKED**: Set when `remainingTrucks == 0` (fully allocated)
- **CANCELLED**: Can be set manually, but only if status is not BOOKED
- Bids cannot be placed on CANCELLED or BOOKED loads

### 3. Multi-Truck Allocation

**Rule**: A load can be fulfilled by multiple bookings until all trucks are allocated.

- `remainingTrucks = load.noOfTrucks - SUM(booking.allocatedTrucks WHERE status = CONFIRMED)`
- Multiple transporters can fulfill portions of a load
- Load becomes BOOKED only when `remainingTrucks == 0`
- Partial bookings are allowed: `allocatedTrucks = min(bid.trucksOffered, remainingTrucks)`

### 4. Concurrent Booking Prevention (Optimistic Locking)

**Rule**: Prevent race conditions when multiple users try to book the same load simultaneously.

**Implementation**:
- `@Version` annotation on `Load` and `AvailableTruck` entities
- JPA automatically checks version numbers during updates
- If version mismatch detected → `OptimisticLockingFailureException`
- Service layer catches this and throws `LoadAlreadyBookedException` (HTTP 409)

**Scenario**:
```
Time    Transaction A              Transaction B
----    ----------------           ----------------
T1      Read Load (v=1)            Read Load (v=1)
T2      Allocate 5 trucks
T3      Save Load (v=2) ✓
T4                                 Allocate 5 trucks
T5                                 Save Load (v=2) ✗ Conflict!
```

### 5. Best-Bid Scoring Algorithm

**Rule**: Rank bids by a weighted score combining price and transporter rating.

**Formula**:
```
score = (1 / proposedRate) × 0.7 + (rating / 5) × 0.3
```

**Weights**:
- **70%**: Price competitiveness (lower rate = higher score)
- **30%**: Transporter reliability (higher rating = higher score)

**Example**:
```
Bid A: rate = 5000, rating = 4.5
  score = (1/5000) × 0.7 + (4.5/5) × 0.3 = 0.00014 + 0.27 = 0.27014

Bid B: rate = 4500, rating = 4.0
  score = (1/4500) × 0.7 + (4.0/5) × 0.3 = 0.000156 + 0.24 = 0.240156

Result: Bid A ranks higher (better balance of price and quality)
```

---

## 🛠️ Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Git

### Step 1: Clone the Repository

```bash
git clone https://github.com/BhaktiTank1134/TMS-Backend-BhaktiTank.git
cd TMS-Backend-BhaktiTank/tms-backend
```

### Step 2: Create PostgreSQL Database

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE tms_db;

-- Verify
\l
```

### Step 3: Configure Application Properties

Create or update `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/tms_db
spring.datasource.username=postgres
spring.datasource.password=your_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

**Note**: Update `username` and `password` with your PostgreSQL credentials.

### Step 4: Build and Run

```bash
# Clean and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Step 5: Verify Installation

```bash
# Check if server is running
curl http://localhost:8080/load
```

Expected response: `[]` (empty array) or list of loads if data exists.

---

## 📖 API Documentation

### Base URL

```
http://localhost:8080
```

### Example Requests and Responses

#### 1. Create a Load

**Request**:
```http
POST /load
Content-Type: application/json

{
  "shipperId": "SHIP001",
  "loadingCity": "Mumbai",
  "unloadingCity": "Delhi",
  "loadingDate": "2024-12-15T10:00:00",
  "productType": "Electronics",
  "weight": 5000,
  "weightUnit": "KG",
  "truckType": "Container",
  "noOfTrucks": 10
}
```

**Response** (201 Created):
```json
{
  "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "shipperId": "SHIP001",
  "loadingCity": "Mumbai",
  "unloadingCity": "Delhi",
  "loadingDate": "2024-12-15T10:00:00",
  "productType": "Electronics",
  "weight": 5000,
  "weightUnit": "KG",
  "truckType": "Container",
  "noOfTrucks": 10,
  "status": "POSTED",
  "datePosted": "2024-12-06T14:30:00"
}
```

#### 2. Register a Transporter

**Request**:
```http
POST /transporter
Content-Type: application/json

{
  "companyName": "FastLogistics Pvt Ltd",
  "rating": 4.5,
  "availableTrucks": [
    {
      "truckType": "Container",
      "count": 20
    },
    {
      "truckType": "Flatbed",
      "count": 15
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "transporterId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "companyName": "FastLogistics Pvt Ltd",
  "rating": 4.5,
  "availableTrucks": [
    {
      "truckType": "Container",
      "count": 20
    },
    {
      "truckType": "Flatbed",
      "count": 15
    }
  ]
}
```

#### 3. Submit a Bid

**Request**:
```http
POST /bid
Content-Type: application/json

{
  "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "transporterId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "proposedRate": 50000,
  "trucksOffered": 10
}
```

**Response** (201 Created):
```json
{
  "bidId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "transporterId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "proposedRate": 50000,
  "trucksOffered": 10,
  "status": "PENDING",
  "submittedAt": "2024-12-06T14:35:00"
}
```

#### 4. Get Best Bids for a Load

**Request**:
```http
GET /load/a1b2c3d4-e5f6-7890-abcd-ef1234567890/best-bids
```

**Response** (200 OK):
```json
[
  {
    "bidId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "transporterId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "proposedRate": 48000,
    "trucksOffered": 10,
    "status": "PENDING",
    "submittedAt": "2024-12-06T14:35:00"
  },
  {
    "bidId": "d4e5f6a7-b8c9-0123-def0-234567890123",
    "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "transporterId": "e5f6a7b8-c9d0-1234-ef01-345678901234",
    "proposedRate": 52000,
    "trucksOffered": 8,
    "status": "PENDING",
    "submittedAt": "2024-12-06T14:40:00"
  }
]
```

#### 5. Create a Booking

**Request**:
```http
POST /booking
Content-Type: application/json

{
  "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "bidId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "transporterId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "allocatedTrucks": 10
}
```

**Response** (201 Created):
```json
{
  "bookingId": "e5f6a7b8-c9d0-1234-ef01-345678901234",
  "loadId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "bidId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "transporterId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "allocatedTrucks": 10,
  "finalRate": 50000,
  "status": "CONFIRMED",
  "bookedAt": "2024-12-06T14:45:00"
}
```

### Error Responses

#### 404 Not Found
```json
{
  "error": "Load not found"
}
```

#### 409 Conflict
```json
{
  "error": "Not enough trucks available to accept this bid"
}
```

#### 400 Bad Request
```json
{
  "shipperId": "Shipper ID is required",
  "noOfTrucks": "Number of trucks must be at least 1"
}
```

### API Testing Tools

- **Postman Collection**: [Add link to exported collection]
- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html` (if configured)
- **cURL**: Examples provided above

---

## 🧪 Test Coverage

### Implemented Tests

The project includes comprehensive unit tests for critical business logic:

#### BookingServiceTest

1. **testSuccessfulBookingReducesAvailableTrucks**
   - Verifies that creating a booking correctly deducts trucks from transporter inventory
   - Validates booking creation with proper status and allocation

2. **testConcurrentBookingThrowsLoadAlreadyBookedException**
   - Simulates concurrent booking attempts using optimistic locking
   - Ensures second transaction fails with `LoadAlreadyBookedException` (HTTP 409)

3. **testCancelBookingRestoresTrucksAndUpdatesLoadStatus**
   - Verifies that canceling a booking restores truck capacity
   - Validates load status transitions from BOOKED back to OPEN_FOR_BIDS

4. **testInsufficientCapacityThrowsException**
   - Ensures system rejects bookings when transporter lacks sufficient trucks
   - Validates `InsufficientCapacityException` is thrown

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookingServiceTest

# Run with coverage report
mvn clean test jacoco:report
```

### Test Coverage Goals

- **Service Layer**: 80%+ coverage
- **Critical Business Logic**: 100% coverage (capacity validation, concurrency handling)
- **Repository Layer**: Integration tests with H2 in-memory database

---

## 📂 Project Structure

```
tms-backend/
├── src/
│   ├── main/
│   │   ├── java/com/bhakti/tms/
│   │   │   ├── controller/          # REST controllers
│   │   │   │   ├── LoadController.java
│   │   │   │   ├── TransporterController.java
│   │   │   │   ├── BidController.java
│   │   │   │   └── BookingController.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── LoadService.java
│   │   │   │   ├── TransporterService.java
│   │   │   │   ├── BidService.java
│   │   │   │   └── BookingService.java
│   │   │   ├── repository/          # Data access
│   │   │   │   ├── LoadRepository.java
│   │   │   │   ├── TransporterRepository.java
│   │   │   │   ├── AvailableTruckRepository.java
│   │   │   │   ├── BidRepository.java
│   │   │   │   └── BookingRepository.java
│   │   │   ├── entity/              # JPA entities
│   │   │   │   ├── Load.java
│   │   │   │   ├── Transporter.java
│   │   │   │   ├── AvailableTruck.java
│   │   │   │   ├── Bid.java
│   │   │   │   ├── Booking.java
│   │   │   │   └── [Enums]
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── LoadRequestDTO.java
│   │   │   │   ├── LoadResponseDTO.java
│   │   │   │   ├── TransporterRequestDTO.java
│   │   │   │   ├── TransporterResponseDTO.java
│   │   │   │   ├── BidRequestDTO.java
│   │   │   │   ├── BidResponseDTO.java
│   │   │   │   ├── BookingRequestDTO.java
│   │   │   │   ├── BookingResponseDTO.java
│   │   │   │   └── AvailableTruckDTO.java
│   │   │   ├── exception/           # Custom exceptions
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── InvalidStatusTransitionException.java
│   │   │   │   ├── InsufficientCapacityException.java
│   │   │   │   ├── LoadAlreadyBookedException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── TmsBackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/bhakti/tms/
│           └── service/
│               └── BookingServiceTest.java
├── pom.xml
└── README.md
```

---

## 🔒 Security Considerations

- **Input Validation**: All DTOs use Jakarta Validation annotations (`@NotNull`, `@Min`, `@NotBlank`)
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **Optimistic Locking**: Prevents lost updates in concurrent scenarios
- **Exception Handling**: No sensitive information leaked in error responses

### Future Enhancements

- JWT-based authentication and authorization
- Role-based access control (Shipper, Transporter, Admin)
- API rate limiting
- Audit logging for all transactions

---

## 🚀 Deployment

### Docker Support (Future)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/tms-backend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables

```bash
export DB_URL=jdbc:postgresql://localhost:5432/tms_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

---

## 📧 Submission Instructions

### For Job Application

Send the following to **careers@cargopro.ai**:

**Subject**: `<YourName>_Backend_TMS_Assignment`

**Email Body**:
```
Dear Hiring Team,

Please find my submission for the Backend TMS Assignment:

GitHub Repository: https://github.com/BhaktiTank1134/TMS-Backend-BhaktiTank
Resume: [Attached]

Key Highlights:
- 15 REST APIs implemented
- Optimistic locking for concurrency control
- Comprehensive test coverage
- Clean architecture with proper separation of concerns

Looking forward to your feedback.

Best regards,
[Your Name]
```

**Attachments**:
- Resume (PDF format)

---

## 👨‍💻 Author

**Bhakti Tank**

- GitHub: [@BhaktiTank1134](https://github.com/BhaktiTank1134)
- Email: [Your Email]

---

## 📄 License

This project is created as part of a technical assignment for CargoPro.

---

## 🙏 Acknowledgments

- Spring Boot Documentation
- PostgreSQL Community
- Lombok Project

---

**Last Updated**: December 6, 2024
