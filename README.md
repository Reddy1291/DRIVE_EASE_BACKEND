# DriveEase Fleet Lifecycle & Rental Asset Management Platform — Backend

A complete backend-only microservices application built with **Spring Boot**, **Spring Cloud**, **Eureka Service Discovery**, **Spring Cloud API Gateway**, **OpenFeign**, **JWT Authentication**, and **PostgreSQL**.

---

## 1. Architecture Overview

```text
                               POSTMAN
                                  │
                                  ▼
                         API GATEWAY :8080
                                  │
                   ┌──────────────┼──────────────┐
                   │              │              │
                   ▼              ▼              ▼
             USER-SERVICE   VEHICLE-SERVICE  BOOKING-SERVICE
                :8081           :8082            :8083
                   │              │              │
                   ▼              ▼              ▼
              PostgreSQL     PostgreSQL       PostgreSQL
            driveease_user  driveease_veh    driveease_book
                   \              │              /
                    \             │             /
                             EUREKA :8761
```

### Microservices List

| Service Name | Port | Description |
|---|---|---|
| **eureka-server** | `8761` | Netflix Eureka Service Registry & Discovery Server |
| **api-gateway** | `8080` | Spring Cloud Gateway (reactive) with JWT verification filter |
| **user-service** | `8081` | User management, BCrypt registration, login, and JWT generation |
| **vehicle-service** | `8082` | Vehicle inventory, CRUD, and status management |
| **booking-service** | `8083` | Booking lifecycle, overlap validation, inter-service Feign calls |

---

## 2. Prerequisites

1. **Java JDK 21+** (Java 25 compatible, source level 21)
2. **Apache Maven 3.9+**
3. **PostgreSQL Server running on port 5432**

### Database Setup

Run the following SQL commands in your PostgreSQL instance (e.g. `psql` or pgAdmin):

```sql
CREATE DATABASE driveease_user_db;
CREATE DATABASE driveease_vehicle_db;
CREATE DATABASE driveease_booking_db;
```

*Default credentials configured in `application.properties`:*
- Username: `postgres`
- Password: `postgres`
- Host: `localhost:5432`

---

## 3. How to Start the Services (Strict Order)

Open 5 separate terminal tabs or run them sequentially:

### Step 1: Eureka Server (Port 8761)
```powershell
cd c:\Users\reddy\Desktop\SOA_PROJECT\DriveEase\eureka-server
mvn spring-boot:run
```
*Wait until Eureka dashboard is available at: http://localhost:8761*

### Step 2: User Service (Port 8081)
```powershell
cd c:\Users\reddy\Desktop\SOA_PROJECT\DriveEase\user-service
mvn spring-boot:run
```

### Step 3: Vehicle Service (Port 8082)
```powershell
cd c:\Users\reddy\Desktop\SOA_PROJECT\DriveEase\vehicle-service
mvn spring-boot:run
```

### Step 4: Booking Service (Port 8083)
```powershell
cd c:\Users\reddy\Desktop\SOA_PROJECT\DriveEase\booking-service
mvn spring-boot:run
```

### Step 5: API Gateway (Port 8080)
```powershell
cd c:\Users\reddy\Desktop\SOA_PROJECT\DriveEase\api-gateway
mvn spring-boot:run
```

---

## 4. API Endpoints Reference

All requests route through **API Gateway** on port `8080`.

### User Service (`/api/users/**`)
- `POST /api/users` — Register a new user (Public)
- `POST /api/users/login` — Login with credentials, returns JWT token (Public)
- `GET /api/users` — Get all users (Protected, requires `Bearer <token>`)
- `GET /api/users/{id}` — Get user by ID (Protected)
- `PUT /api/users/{id}` — Update user details (Protected)
- `DELETE /api/users/{id}` — Delete user by ID (Protected)

### Vehicle Service (`/api/vehicles/**`)
- `POST /api/vehicles` — Add a new vehicle (Protected)
- `GET /api/vehicles` — Get all vehicles (Protected)
- `GET /api/vehicles/available` — Get only available vehicles (Protected)
- `GET /api/vehicles/{id}` — Get vehicle by ID (Protected)
- `PUT /api/vehicles/{id}` — Update vehicle info (Protected)
- `PUT /api/vehicles/{id}/status` — Update vehicle availability status (Protected)
- `DELETE /api/vehicles/{id}` — Delete vehicle (Protected)

### Booking Service (`/api/bookings/**`)
- `POST /api/bookings` — Create booking with overlap check & Feign verification (Protected)
- `GET /api/bookings` — Get all bookings (Protected)
- `GET /api/bookings/{id}` — Get booking by ID (Protected)
- `GET /api/bookings/user/{userId}` — Get bookings by user (Protected)
- `GET /api/bookings/vehicle/{vehicleId}` — Get bookings by vehicle (Protected)
- `PUT /api/bookings/{id}` — Update booking dates (Protected)
- `PUT /api/bookings/{id}/start` — Start rental: sets status `ACTIVE`, vehicle `RENTED` (Protected)
- `PUT /api/bookings/{id}/return` — Return vehicle: sets status `COMPLETED`, vehicle `AVAILABLE` (Protected)
- `PUT /api/bookings/{id}/cancel` — Cancel booking: sets status `CANCELLED`, vehicle `AVAILABLE` (Protected)
- `DELETE /api/bookings/{id}` — Delete booking (Protected)

---

## 5. Postman Testing Guide

1. Open **Postman**.
2. Click **Import** and select `DriveEase_Postman_Collection.json`.
3. Run request **1.1 Register User** (`POST http://localhost:8080/api/users`).
4. Run request **1.2 Login User** (`POST http://localhost:8080/api/users/login`).
   * The test script automatically saves the returned JWT token into `{{jwt_token}}` variable.
5. Execute requests sequentially across Vehicle Service and Booking Service.
6. Verify request **2.7 Test Security (Expect 401)** rejects unauthenticated requests.
7. Verify request **3.2 Test Overlap Booking** rejects overlapping date reservations.
