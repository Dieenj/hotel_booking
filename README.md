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
- Sonner (toast notifications)

## Features

**Guest**
- Browse hotels and search by city, check-in/check-out date, and guest count
- View hotel details, room types, and availability
- Book rooms with a multi-step flow (booking → payment → confirmation)
- Manage personal bookings (view, cancel)
- Write reviews for completed stays
- Edit profile

**Admin**
- Dashboard with this month's bookings, revenue and recent bookings
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

Full, always-current reference: Swagger UI at `/swagger-ui.html`.

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register new account |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| GET | `/api/auth/me` | User | Current user profile |
| PUT | `/api/auth/profile` | User | Update profile |
| GET | `/api/hotels` | Public | Search hotels (`city`, `name`, `minRating`, `sort` = `newest` \| `rating_desc` \| `name_asc`) |
| GET | `/api/hotels/{id}` | Public | Hotel detail |
| GET | `/api/hotels/{id}/room-types` | Public | Room types, with availability when `checkIn`/`checkOut` given |
| GET | `/api/hotels/{id}/reviews` | Public | Hotel reviews |
| GET | `/api/availability` | Public | Rooms available for a room type and date range (max 30 nights) |
| POST | `/api/bookings` | User | Create booking (status `PENDING`) |
| GET | `/api/bookings` | User | My bookings |
| GET | `/api/bookings/{id}` | User | Booking detail |
| POST | `/api/bookings/{id}/pay` | User | Mock payment → `CONFIRMED` |
| POST | `/api/bookings/{id}/cancel` | User | Cancel booking |
| POST | `/api/reviews` | User | Review a `COMPLETED` booking |
| GET | `/api/reviews/my` | User | My reviews |
| GET | `/api/admin/stats` | Admin | Dashboard stats |
| GET | `/api/admin/bookings` | Admin | All bookings |
| POST | `/api/admin/bookings/{id}/confirm` \| `cancel` | Admin | Confirm / cancel booking |
| POST/PUT/DELETE | `/api/admin/hotels[/{id}]` | Admin | Manage hotels |
| POST | `/api/admin/hotels/{id}/room-types` | Admin | Create room type |
| PUT/DELETE | `/api/admin/room-types/{id}` | Admin | Update / delete room type |

Errors use the same envelope: `{ "success": false, "error": "..." }` with 400/401/403/404/409.

## Booking Lifecycle

`PENDING` → (`/pay`) → `CONFIRMED` → (check-out date reached) → `COMPLETED` → can be reviewed.

Scheduled jobs (disable with `app.scheduling.enabled=false`):
- Unpaid `PENDING` bookings are cancelled after `app.booking.pending-ttl` (default `30m`, env `BOOKING_PENDING_TTL`) and their rooms released.
- `CONFIRMED`/`CHECKED_IN` bookings whose check-out date has arrived become `COMPLETED` (hourly).

## Development

```bash
cd backend && mvn verify          # integration tests on in-memory H2, no database needed
# Same tests on PostgreSQL (what CI runs):
# TEST_DB_URL=jdbc:postgresql://localhost:5432/hotel_test TEST_DB_USERNAME=postgres TEST_DB_PASSWORD=... mvn verify
cd frontend && pnpm lint && pnpm build
```

CI (`.github/workflows/ci.yml`) runs the same checks on every push and pull request.

### Docker

```bash
cp .env.example .env              # then set JWT_SECRET (>= 32 chars)
docker compose up --build
```
