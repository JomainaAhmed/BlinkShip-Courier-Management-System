# Simplified System Diagrams for PowerPoint

These diagrams have been simplified to reduce visual clutter, making them ideal for PowerPoint slides or executive presentations.

## 1. Simplified Use Case Diagram
Highlights only the core interactions for the main actors.

```mermaid
flowchart LR
    Customer(["👤 Customer"])
    Admin(["🛠️ Admin"])

    subgraph Core Features
        Book(["Book a Courier"])
        Track(["Track a Shipment"])
        Manage(["Manage Deliveries"])
        Dashboard(["System Dashboard"])
    end

    Customer --> Book
    Customer --> Track
    
    Admin --> Track
    Admin --> Manage
    Admin --> Dashboard
```

## 2. Simplified System Architecture Diagram
Focuses on the main flow of data from the client to the microservices and databases, abstracting away infrastructural components like Eureka and Config.

```mermaid
graph TD
    Client["Frontend UI"] -->|HTTP REST| Gateway["API Gateway"]
    
    subgraph Microservices Cluster
        Gateway --> Auth["Auth Service"]
        Gateway --> Delivery["Delivery Service"]
        Gateway --> Tracking["Tracking Service"]
    end
    
    Delivery -->|Async Events| MQ{{"Message Broker (RabbitMQ)"}}
    MQ --> Tracking
    
    Auth & Delivery & Tracking --> DB[("Database (PostgreSQL)")]
```

## 3. Simplified Shipment Booking Sequence Diagram
A straightforward look at the booking flow without the deep technical details (like JWT filtering or RabbitMQ publishing steps).

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as Frontend UI
    participant Gateway as API Gateway
    participant Delivery as Delivery Service
    participant DB as Database

    User->>UI: Submit Booking Details
    UI->>Gateway: POST /deliveries/create
    Gateway->>Delivery: Forward Request
    Delivery->>Delivery: Calculate Price & Generate QR
    Delivery->>DB: Save Delivery Info
    DB-->>Delivery: Confirm Save
    Delivery-->>Gateway: 201 Created (Details + QR)
    Gateway-->>UI: 201 Created
    UI-->>User: Display Booking Confirmation
```
