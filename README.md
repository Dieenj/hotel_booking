# Hotel Booking Platform

A full-stack hotel booking web application built with Spring Boot and React.

## Tech Stack

**Backend**
- Java 17 + Spring Boot 3
- Spring Security + JWT Authentication
- Spring Data JPA + PostgreSQL (Supabase)
- Maven

**Frontend**
- React 18 + TypeScript + Vite
- Tailwind CSS v4 + shadcn/ui (Radix UI)
- React Router v7 + Axios
- Recharts + Sonner

## Features

- Guest: Browse hotels, search by city/date, book rooms, payment flow, manage bookings, write reviews
- Admin: Dashboard with revenue stats, manage hotels & room types, manage bookings (confirm/cancel)

## Getting Started

### Backend
```bash
cd backend
# Configure application.properties with your Supabase PostgreSQL URL
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
pnpm install
pnpm dev
```

Frontend runs at `http://localhost:5173`, proxies `/api` → `http://localhost:8080`.
