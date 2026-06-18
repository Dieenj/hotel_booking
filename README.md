# StayEase — Hotel Booking Platform

A full-stack hotel booking web application built for portfolio purposes, covering a complete guest booking flow and an admin management panel.

## Tech Stack

**Backend**
- Java 21 + Spring Boot 3.3.5
- Spring Security + JWT (JJWT 0.12.3)
- Spring Data JPA + PostgreSQL (Supabase)
- Maven Daemon (mvnd)
- Springdoc OpenAPI (Swagger UI)

**Frontend**
- React 19 + TypeScript + Vite
- Tailwind CSS v4 + shadcn/ui (Radix UI)
- React Router v7 + Axios
- Recharts (admin charts) + Sonner (toast notifications)

## Features

**Guest**
- Browse hotels and search by city, check-in/check-out date, and guest count
- View hotel details, room types, and availability
- Book rooms with a multi-step flow (booking → payment → confirmation)
- Manage personal bookings (view, cancel)
- Write reviews for completed stays
- Edit profile

**Admin**
- Dashboard with revenue stats and booking overview (Recharts)
- Manage hotels (create, edit, delete) with amenities and image URLs
- Manage room types per hotel with pricing and availability
- Manage all bookings (confirm, cancel)

## Project Structure

```
HotelBooking/
├── backend/          # Spring Boot REST API
│   └── src/main/java/com/hotelbooking/
│       ├── auth/     # Authentication (JWT, register, login)
│       ├── booking/  # Bookings, availability, room inventory
│       ├── hotel/    # Hotels, room types, reviews
│       ├── common/   # Security filter, exception handling, base entities
│       └── config/   # SecurityConfig, CorsConfig
└── frontend/         # React + Vite SPA
    └── src/
        ├── pages/    # auth/, guest/, admin/
        ├── api/      # Axios API clients
        ├── components/
        └── context/  # AuthContext
```

## Getting Started

### Prerequisites
- Java 21+
- Node.js 20+ and pnpm
- A PostgreSQL database (Supabase free tier works)

### Backend

1. Create `backend/src/main/resources/application-local.yml` (gitignored):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://<host>:5432/postgres?pgbouncer=true&prepareThreshold=0
    username: <username>
    password: <password>
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 5
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
    show-sql: false

server:
  port: 8081

app:
  jwt:
    secret: <your-jwt-secret-min-32-chars>
    expiration: 86400000
    refresh-expiration: 604800000
  cors:
    allowed-origins: http://localhost:5173
```

2. Run:

```bash
cd backend
mvnd spring-boot:run
# or: mvn spring-boot:run
```

API runs at `http://localhost:8081`. Swagger UI: `http://localhost:8081/swagger-ui.html`

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

Frontend runs at `http://localhost:5173`, proxies `/api` → `http://localhost:8081`.

## API Overview

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register new account |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/hotels` | Public | List hotels with filters |
| GET | `/api/hotels/{id}` | Public | Hotel detail + room types |
| POST | `/api/bookings` | User | Create booking |
| GET | `/api/bookings/my` | User | My bookings |
| POST | `/api/reviews` | User | Submit review |
| GET | `/api/admin/dashboard` | Admin | Revenue stats |
| POST | `/api/admin/hotels` | Admin | Create hotel |
| PUT | `/api/admin/hotels/{id}` | Admin | Update hotel |
| DELETE | `/api/admin/hotels/{id}` | Admin | Delete hotel |
