# TMS Backend

Spring Boot 3.2+ application with Java 17.

## Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

## Setup
1. Create PostgreSQL database: `tms_db`
2. Update credentials in `application.properties` if needed
3. Run: `mvn spring-boot:run`

## Project Structure
```
com.bhakti.tms
├── controller    # REST controllers
├── service       # Business logic
├── repository    # Data access layer
├── entity        # JPA entities
├── dto           # Data transfer objects
└── exception     # Custom exceptions
```
