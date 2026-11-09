# BlinkShip — Smart Courier Delivery Management System

A production-ready, enterprise-grade microservices courier management system built with **Spring Boot 3**, **Spring Cloud**, **Docker**, and an ultra-modern **Angular** frontend with **Tailwind CSS**.

---

## 🌐 Live Deployment & Links

| Service | Platform | Status / URL |
|---|---|---|
| **Frontend Web App** | **Vercel** | 🚀 **[blink-ship-courier-management-syste-seven.vercel.app](https://blink-ship-courier-management-syste-seven.vercel.app/)** |
| **Backend API Gateway** | **Docker + Cloudflare Tunnel** | 🔒 Active via Cloudflare Secure Zero-Trust Edge Tunnel |
| **CI/CD Pipeline** | **GitHub Actions + Vercel Git** | 🔄 Automated builds on push to `main` |

---

## 🚀 Deployment Architecture & Setup

BlinkShip uses a modern **hybrid cloud deployment architecture** separating the client-side SPA presentation layer from the multi-service containerized backend infrastructure.

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Browser                         │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
       (Web App Pages)                  (REST / SSE)
               │                              │
               ▼                              ▼
┌─────────────────────────────┐  ┌────────────────────────────┐
│      ▲ Vercel Edge CDN      │  │      Cloudflare Tunnel     │
│   (Angular SPA Frontend)    │  │   (Encrypted Edge Proxy)   │
│  - Automated Git Deploy     │  └─────────────┬──────────────┘
│  - Edge Asset Caching       │                │
│  - SPA Routing (vercel.json)│                ▼
└─────────────────────────────┘  ┌────────────────────────────┐
                                 │     API Gateway (8080)     │
                                 │  - JWT Auth Filter         │
                                 │  - CORS & Rate Limiting    │
                                 │  - Eureka Route Discovery  │
                                 └─────────────┬──────────────┘
                                               │
               ┌────────────────┬──────────────┴────────────────┬────────────────┐
               ▼                ▼                               ▼                ▼
     ┌──────────────────┐ ┌──────────────────┐        ┌──────────────────┐ ┌──────────────────┐
     │   Auth Service   │ │ Delivery Service │        │ Tracking Service │ │  Admin Service   │
     └─────────┬────────┘ └────────┬─────────┘        └────────┬─────────┘ └────────┬─────────┘
               │                   │                           │                    │
               ▼                   ▼                           ▼                    ▼
     ┌────────────────────────────────────────────────────────────────────────────────────────┐
     │                    Infrastructure: PostgreSQL Databases & RabbitMQ                     │
     └────────────────────────────────────────────────────────────────────────────────────────┘
```

### 1. Frontend: Vercel (Edge-Optimized Single Page App)
- **Platform**: Hosted globally on **Vercel**.
- **Build System**: Angular CLI (`@angular/build:application`) with production asset optimizations, CSS purging, and environment-specific variable injection.
- **Routing & Rewrites**: Configured via `vercel.json` with wildcard SPA rewrites (`"source": "/(.*)", "destination": "/index.html"`) so deep client routes (`/signup`, `/login`, `/dashboard`, `/track`) work on initial load and page refresh.
- **Environment Management**: Dynamically binds to the production API gateway URL using `src/environments/environment.prod.ts`.

### 2. Backend: Docker Compose (Microservices & Message Broker)
- **Platform**: Hosted in an isolated container environment (Linux VM / Google Cloud Shell).
- **Service Composition**:
  - **Eureka Server** (Port `8761`): Service registry & health monitoring.
  - **Spring Cloud Config Server** (Port `8888`): Centralized cloud configuration backed by Git.
  - **Spring Cloud API Gateway** (Port `8080`): Single unified entry point with JWT token validation and dynamic CORS handling (`allowed-origin-patterns=*`).
  - **Auth Service** (Port `8081`): Identity management, PBKDF2/BCrypt hashing, JWT issuance.
  - **Delivery Service** (Port `8082`): Booking engine, volumetric pricing algorithms, label generator.
  - **Tracking Service** (Port `8083`): Event consumer, async status propagation, real-time SSE stream.
  - **Admin Service** (Port `8084`): KPI aggregations, reporting, and operational controls.
  - **RabbitMQ** (Port `5672` / `15672`): Asynchronous messaging between Delivery and Tracking services with Dead Letter Queues (DLQ).
  - **PostgreSQL**: Isolated relational database instances for each microservice.

### 3. Edge Connectivity: Cloudflare Tunnel (`cloudflared`)
- The internal API Gateway (`localhost:8080`) is exposed securely to the internet using **Cloudflare Quick Tunnels** without requiring public IP addresses, opening router ports, or managing complex SSL certificates.
- Provides end-to-end HTTPS/TLS 1.3 encryption, DDoS mitigation, and HTTP/2 connectivity directly from Vercel to the backend microservices.

---

### How to Reproduce the Deployment

#### A. Deploying the Backend (Cloud Shell / VPS / Docker)
1. **Clone the repository**:
   ```bash
   git clone https://github.com/JomainaAhmed/BlinkShip-Courier-Management-System.git
   cd BlinkShip-Courier-Management-System
   ```
2. **Build service JARs**:
   ```bash
   chmod +x build-all.sh
   ./build-all.sh
   ```
3. **Launch the containers**:
   ```bash
   docker compose up -d
   ```
4. **Expose the API Gateway via Cloudflare**:
   ```bash
   cloudflared tunnel --url http://localhost:8080
   ```
   *(Note down the generated `https://*.trycloudflare.com` URL)*

#### B. Deploying the Frontend (Vercel)
1. In `courier-management-ui/src/environments/environment.prod.ts`, set the `apiUrl` to your Cloudflare Tunnel URL:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://<your-tunnel-url>.trycloudflare.com'
   };
   ```
2. Commit and push to GitHub:
   ```bash
   git commit -am "Update backend production URL"
   git push origin main
   ```
3. Import the repository into **Vercel**:
   - **Framework Preset**: Angular
   - **Root Directory**: `courier-management-ui`
   - **Output Directory**: `dist/courier-management-ui`
4. Click **Deploy**. Vercel will build the frontend and serve it at your `.vercel.app` domain.

---

## Architecture Overview

The system consists of several microservices:
- **Eureka Server**: Service discovery.
- **Config Server**: Centralized configuration management (Remote Git).
- **API Gateway**: Entry point for all requests, handles routing and JWT security.
- **Auth Service**: User authentication and JWT management.
- **Delivery Service**: Core delivery lifecycle and pricing logic.
- **Tracking Service**: Real-time tracking updates via RabbitMQ.
- **Admin Service**: Orchestrator for reports and dashboards.

## Infrastructure & Monitoring
- **Postgres**: Independent databases for each microservice.
- **RabbitMQ**: Message broker for asynchronous event processing.
- **Zipkin**: Distributed tracing (Port 9411).
- **Prometheus**: Metrics collection and monitoring (Port 9090).
- **SonarQube**: Static code analysis and quality gate (Port 9000).

## Getting Started

### Prerequisites
- Java 17
- Maven 3.8+
- Docker & Docker Desktop

### Build Instructions
1. Build all services using the provided script (uses Maven Wrapper):
   ```powershell
   powershell.exe -ExecutionPolicy Bypass -File .\build-all.ps1
   ```

2. Start the infrastructure and services:
   ```bash
   docker-compose up -d
   ```

### Configuration Management
The system uses **Spring Cloud Config Server**.
- **Source**: Fetching configs from `https://github.com/JomainaAhmed/config-repo1`.
- **Requirements**: Ensure internet access for the Config Server to clone the repository.
- **Prometheus Metrics**: Ensure your remote configurations enable metrics exposure:
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: "*"
  ```

## Key Features Implemented
- **Lombok**: Reduced boilerplate code across all services.
- **SLF4j Logging**: Console and File appenders enabled for all functional services.
- **Jacoco**: Code coverage analysis integrated into the Maven build.
- **Async Tracking**: Delivery updates are processed asynchronously via RabbitMQ with Dead Letter Queue (DLQ) support.
- **Real-Time Live Tracking**: Server-Sent Events (SSE) push updates in real-time to connected clients (`/tracking/{id}/live`).
- **Token Lifecycle & Security**: Refresh token flow (`/auth/refresh`), externalized JWT secrets, path traversal defenses.
- **Pagination & Sorting**: High-throughput paginated queries for deliveries (`/deliveries/paged`).
- **Resilient Microservices**: Fault-tolerant fallback handling for inter-service Feign calls.
- **CI/CD Automation**: GitHub Actions pipeline for automated builds, Jacoco coverage, and quality checks.

## API Documentation & Dashboards
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Prometheus**: `http://localhost:9090`
- **Eureka Dashboard**: `http://localhost:8761`
- **Zipkin**: `http://localhost:9411`
- **SonarQube**: `http://localhost:9000`

## Frontend (Angular & Tailwind CSS)
The system includes a premium frontend built with **Angular** and **Tailwind CSS**.
- **Location**: `courier-management-ui/`
- **Styling**: Tailwind CSS v3 with Glassmorphism effects.
- **Features**: JWT Auth, User Dashboard, Admin Reports.

### Running the Frontend
1. Navigate to the directory:
   ```bash
   cd courier-management-ui
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the dev server:
   ```bash
   npm start
   ```
The UI will be available at `http://localhost:4200`.

## Testing & Quality
To run unit tests for the Delivery Service:
```bash
cd delivery-service
./mvnw test
```

### Running SonarQube Analysis
1. Ensure SonarQube is running: `docker-compose up -d sonarqube`
2. Run the scan script for all services:
   ```powershell
   powershell.exe -ExecutionPolicy Bypass -File .\scripts\sonar-scan.ps1
   ```
3. View results at `http://localhost:9000` (Default login: admin/admin)

4. ---

# System Architecture

![System Architecture](docs/architecture.png)

The Smart Courier Delivery Management System follows a distributed microservices architecture built using Spring Boot and Spring Cloud. The Angular frontend communicates with the API Gateway, which routes requests to individual backend services. RabbitMQ enables asynchronous communication between services, while PostgreSQL stores operational data.

### Core Components

| Component | Responsibility |
|------------|---------------|
| API Gateway | Centralized routing, JWT validation, request forwarding |
| Auth Service | User authentication and JWT token management |
| Delivery Service | Courier booking, pricing calculations, QR generation |
| Tracking Service | Shipment tracking and activity logging |
| Admin Service | Reports, dashboards, delivery management |
| RabbitMQ | Event-driven communication |
| PostgreSQL | Persistent storage |
| Eureka Server | Service registration and discovery |
| Config Server | Centralized configuration management |

---

# Use Case Diagram

![Use Case](docs/use-case.png)

The system supports two primary actors:

### Customer
- Register / Login
- Book a Courier
- Track a Shipment
- Calculate Pricing Preview
- View Shipment History
- Upload & Download Documents

### Administrator
- Manage Deliveries
- Update Delivery Status
- Resolve Failed Deliveries
- View Dashboard & KPIs
- Export Reports

---

# Courier Booking Workflow

![Courier Booking](docs/courier-booking.png)

### Booking Process

1. User fills courier booking form.
2. Angular validates package dimensions and weight.
3. Request is sent through API Gateway.
4. Delivery Service calculates pricing.
5. Delivery details are stored in PostgreSQL.
6. A booking event is published to RabbitMQ.
7. Tracking information is generated.
8. User receives booking confirmation.

---

# Shipment Tracking Architecture

![Tracking Architecture](docs/tracking.png)

Tracking updates are processed asynchronously using RabbitMQ.

### Benefits

- Event-driven architecture
- Loose service coupling
- Better scalability
- Faster response times
- Improved fault tolerance

---

# Database Design

![ER Diagram](docs/er-diagram.png)

### Main Entities

#### Users
Stores customer and administrator credentials.

#### Address
Stores sender and receiver information.

#### Package Entity
Stores parcel dimensions, weight, and pricing.

#### Delivery
Stores delivery status and shipment details.

#### Tracking
Maintains shipment history and location updates.

---

# Service Discovery

![Eureka Dashboard](docs/eureka-service-registry.png)

Spring Cloud Eureka enables automatic service registration and discovery. Services dynamically register themselves and the API Gateway uses Eureka to locate healthy instances.

---

# API Documentation

![Swagger](docs/swagger.png)

Swagger/OpenAPI documentation is available for:

- Auth Service
- Delivery Service
- Tracking Service
- Admin Service

The documentation is exposed through the API Gateway for centralized API exploration and testing.

---

# Distributed Tracing

![Zipkin](docs/zipkin.png)

Zipkin provides distributed tracing across all microservices, making it easier to:

- Trace requests
- Analyze latency
- Debug service interactions
- Monitor performance bottlenecks

---

# Message Broker

![RabbitMQ](docs/rabbitmq.png)

RabbitMQ powers asynchronous event-driven communication between services.

### Event Flow

Delivery Service
→ RabbitMQ Exchange
→ Tracking Queue
→ Tracking Service

This architecture improves reliability and scalability by decoupling producers and consumers.

---

# Frontend (Angular + Tailwind CSS)

## Landing Page

![Landing Page](docs/label.png)

## Services Page

![Services](docs/services-page.png)

## Parcel Booking Interface

![Booking](docs/parcel-booking-interface.png)

## Authentication

![Login](docs/parcel-booking-interface2.png)

### Features

- JWT Authentication
- Responsive Design
- Shipment Tracking
- Courier Booking
- Delivery History
- Admin Operations
- Modern Tailwind UI

---

# Demo Videos

## Complete Project Demonstration

https://youtu.be/eVhZzyJQdl0

## SonarQube Setup & Analysis

https://youtu.be/augClel4vZA

## BlinkShip Frontend Demonstration

https://youtu.be/eVhZzyJQdl0

---
