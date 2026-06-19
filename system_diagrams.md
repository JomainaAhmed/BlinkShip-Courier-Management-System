# System Diagrams for Smart Courier Delivery Management System

Below is the complete set of requested diagrams for your report. You can embed these directly into your markdown documents, as they use standard Mermaid.js syntax which is widely supported by GitHub, GitLab, Notion, and most markdown editors.

## 1. Use Case Diagram
This diagram illustrates the primary actors (Customer and Admin) and their interactions with the system's features.

```mermaid
flowchart LR
    %% Actors
    Customer(["👤 Customer"])
    Admin(["🛠️ Admin"])

    %% Use Cases
    subgraph Blinkship Courier System
        UC1(["Register / Login"])
        UC2(["Book a Courier"])
        UC3(["Track a Shipment"])
        UC4(["Calculate Pricing Preview"])
        UC5(["View Personal Shipment History"])
        UC6(["Upload / Download Documents"])
        
        UC7(["Manage All Deliveries"])
        UC8(["Update Delivery Status"])
        UC9(["Resolve Stuck Deliveries"])
        UC10(["View System Dashboard & KPIs"])
        UC11(["Export Reports (CSV)"])
    end

    %% Customer Relationships
    Customer --> UC1
    Customer --> UC2
    Customer --> UC3
    Customer --> UC4
    Customer --> UC5
    Customer --> UC6

    %% Admin Relationships
    Admin --> UC1
    Admin --> UC3
    Admin --> UC4
    Admin --> UC6
    Admin --> UC7
    Admin --> UC8
    Admin --> UC9
    Admin --> UC10
    Admin --> UC11
```

## 2. System Architecture Diagram
A high-level view of how the microservices, infrastructure, and frontend interact.

```mermaid
graph TD
    Client["Angular Frontend UI"] -->|REST / HTTP| Gateway["API Gateway (Port 8080)"]
    
    subgraph Microservices ["Microservices Cluster"]
        Gateway -->|/auth| Auth["Auth Service (8081)"]
        Gateway -->|/deliveries| Delivery["Delivery Service (8082)"]
        Gateway -->|/tracking| Tracking["Tracking Service (8083)"]
        Gateway -->|/admin| AdminService["Admin Service (8084)"]
        
        AdminService -.->|Feign RPC| Auth
        AdminService -.->|Feign RPC| Delivery
    end
    
    subgraph Infrastructure ["Infrastructure and Messaging"]
        Config["Spring Cloud Config"] --> Auth & Delivery & Tracking & AdminService & Gateway
        Eureka["Eureka Discovery"] -.-> Gateway & Auth & Delivery & Tracking & AdminService
        
        Delivery -->|Status Event Publish| RMQ{{"RabbitMQ Broker"}}
        RMQ -->|Consume Event| Tracking
    end
    
    subgraph Database ["PostgreSQL Container"]
        Auth --> AuthDB[("auth_db2")]
        Delivery --> DelDB[("delivery_db2")]
        Tracking --> TrackDB[("tracking_db2")]
        AdminService --> AdminDB[("admin_db2")]
    end
```

## 3. Entity Relationship (ER) Diagram
The core data structure spread across the various microservice databases.

```mermaid
erDiagram
    %% Auth DB
    USERS {
        int id PK
        string username UK
        string email UK
        string password
        string role
    }

    %% Delivery DB
    DELIVERY {
        bigint id PK
        string username
        bigint sender_id FK
        bigint receiver_id FK
        bigint package_entity_id FK
        string status
        timestamp created_at
    }
    
    ADDRESS {
        bigint id PK
        string name
        string phone
        string address_line
        string city
        string state
        string pincode
    }
    
    PACKAGE_ENTITY {
        bigint id PK
        string description
        float weight
        float length
        float width
        float height
        float price
    }

    %% Tracking DB
    TRACKING {
        bigint id PK
        bigint delivery_id FK
        string status
        string location
        timestamp timestamp
        bytea document
    }

    %% Relationships
    DELIVERY ||--|| ADDRESS : "has sender"
    DELIVERY ||--|| ADDRESS : "has receiver"
    DELIVERY ||--|| PACKAGE_ENTITY : "contains"
    DELIVERY ||--o{ TRACKING : "has many"
```

## 4. Sequence Diagram (Booking)
The step-by-step API flow when a user books a new courier delivery.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant UI as Angular UI
    participant Gateway as API Gateway
    participant Delivery as Delivery Service
    participant RMQ as RabbitMQ
    participant DB as PostgreSQL (delivery_db2)

    User->>UI: Fills Booking Form (Addresses, Dimensions)
    UI->>UI: Validates Form & Calculates Volumetric Weight
    UI->>Gateway: POST /deliveries/create + JWT Token
    Gateway->>Gateway: JwtFilter Validates Token
    Gateway->>Delivery: Forwards Request (with X-User Header)
    
    Delivery->>Delivery: Calculates Final Price (INR 300 * Chargeable Wt)
    Delivery->>Delivery: Generates Tracking QR Code (ZXing)
    Delivery->>DB: Save Delivery, Addresses, and Package Entities
    DB-->>Delivery: Returns Saved Entities
    
    Delivery->>RMQ: Publish Event (deliveryId, 'BOOKED') to courier-exchange
    Delivery-->>Gateway: 201 Created (Delivery details + QR)
    Gateway-->>UI: 201 Created
    UI-->>User: Displays Booking Confirmation & Invoice
```

## 5. Sequence Diagram (Tracking)
Shows both the asynchronous tracking event update and the user retrieving the tracking timeline.

```mermaid
sequenceDiagram
    autonumber
    
    %% Async event part
    actor Admin
    participant RMQ as RabbitMQ
    participant Tracking as Tracking Service
    participant DB as PostgreSQL DB
    
    Admin->>RMQ: Publishes Status Change Event (e.g. IN_TRANSIT)
    RMQ-->>Tracking: Consumes Message via Queue
    Tracking->>Tracking: Determines Location based on Status
    Tracking->>DB: Save new Tracking Log
    
    %% User checking part
    actor User
    participant UI as Angular UI
    participant Gateway as API Gateway
    
    User->>UI: Enters Tracking ID
    UI->>Gateway: GET /tracking/{deliveryId}
    Gateway->>Tracking: Forwards Request
    Tracking->>DB: Select * from Tracking logs
    DB-->>Tracking: Returns Ordered Logs
    Tracking-->>Gateway: 200 OK (List of Logs)
    Gateway-->>UI: 200 OK
    UI-->>User: Displays Visual Timeline Route
```

## 6. Activity Diagram
The lifecycle and states of a package delivery from booking to delivery or return.

```mermaid
stateDiagram-v2
    [*] --> BOOKED : User Books Courier
    
    BOOKED --> PACKED : Admin processes package
    PACKED --> DISPATCHED : Ready to leave origin
    DISPATCHED --> PICKED_UP : Courier takes package
    PICKED_UP --> IN_TRANSIT : Moving between hubs
    
    IN_TRANSIT --> OUT_FOR_DELIVERY : Arrived at local hub
    IN_TRANSIT --> DELAYED : Exception/Weather issue
    DELAYED --> IN_TRANSIT : Admin Resolves Exception
    
    OUT_FOR_DELIVERY --> DELIVERED : Successfully handed over
    OUT_FOR_DELIVERY --> FAILED : Customer unavailable
    
    FAILED --> RETURNED : Admin resolves as Return
    FAILED --> OUT_FOR_DELIVERY : Admin resolves as Retry
    
    DELIVERED --> [*]
    RETURNED --> [*]
```

## 7. Deployment Diagram
How the Docker containers are deployed and orchestrated on the host machine.

```mermaid
graph TD
    subgraph Docker Host Machine
        
        subgraph Frontend
            UI["Angular App Container (Nginx / Node)"]
        end
        
        subgraph Spring Cloud Network
            Gateway["api-gateway :8080"]
            Auth["auth-service :8081"]
            Delivery["delivery-service :8082"]
            Tracking["tracking-service :8083"]
            Admin["admin-service :8084"]
            
            Config["config-server :8888"]
            Eureka["eureka-server :8761"]
        end
        
        subgraph Persistence & Infrastructure
            DB[("postgres:15 (Port 5432)")]
            MQ{{"rabbitmq:3-management (Port 5672/15672)"}}
            Zipkin["zipkin (Port 9411)"]
            Prometheus["prometheus (Port 9090)"]
        end
        
        UI -->|HTTP Requests| Gateway
        Gateway --> Auth & Delivery & Tracking & Admin
        
        Auth & Delivery & Tracking & Admin --> DB
        Delivery --> MQ
        MQ --> Tracking
        
        Config -.-> Auth & Delivery & Tracking & Admin & Gateway
        Eureka -.-> Gateway & Auth & Delivery & Tracking & Admin
    end
```

## 8. Authentication Flow Diagram
The zero-trust security model implemented using JWTs across the API Gateway and microservices.

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Gateway as API Gateway (JwtFilter)
    participant Auth as Auth Service
    participant Microservice as Target Service (e.g. Delivery)

    %% Login Flow
    User->>Gateway: POST /auth/login (Credentials)
    Gateway->>Auth: Forward (No Token Required)
    Auth->>Auth: Verify BCrypt Password
    Auth->>Auth: Generate JWT (Signed with Secret)
    Auth-->>Gateway: 200 OK + JWT Token
    Gateway-->>User: Returns JWT Token
    
    %% Authenticated Request Flow
    User->>Gateway: POST /deliveries/create (Header: Bearer {JWT})
    Gateway->>Gateway: Validates Signature & Expiration
    Gateway->>Gateway: Extracts Claims (Username, Role)
    Gateway->>Microservice: Forwards Request + X-User & X-Role Headers
    
    Microservice->>Microservice: Internal JwtFilter reads Headers
    Microservice->>Microservice: Authorizes Action (e.g., checks if role is Admin)
    Microservice-->>Gateway: 200 OK (Response)
    Gateway-->>User: Returns Data
```

## 9. RabbitMQ Asynchronous Messaging Flow
This diagram details the pub/sub event-driven architecture using RabbitMQ for decoupled tracking updates.

```mermaid
graph LR
    subgraph Publisher ["Publisher"]
        Delivery["Delivery Service (Producer)"]
    end
    
    subgraph Broker ["RabbitMQ Broker"]
        Exchange{"Exchange: courier-exchange"}
        Queue["Queue: tracking-queue"]
        
        Exchange -->|Routing Key: courier-routing-key| Queue
    end
    
    subgraph Consumer ["Consumer"]
        Tracking["Tracking Service (Consumer)"]
    end
    
    Delivery -->|Publish: {deliveryId, status}| Exchange
    Queue -->|Consume Message Thread| Tracking
```

## 10. Service Discovery (Eureka) Flow
This illustrates how microservices register themselves on startup and how the API Gateway dynamically routes traffic without hardcoded IPs.

```mermaid
sequenceDiagram
    autonumber
    
    participant MS as Microservice (e.g. Auth/Delivery)
    participant Eureka as Eureka Server
    participant Gateway as API Gateway
    
    %% Registration Phase
    MS->>Eureka: Startup: Register IP, Port, and Service ID
    Eureka-->>MS: Acknowledge Registration
    MS->>Eureka: Send Heartbeat (Every 30s)
    
    %% Discovery Phase
    Gateway->>Eureka: Fetch Registry (List of all Services)
    Eureka-->>Gateway: Returns active IPs/Ports mapped to IDs
    
    %% Routing Phase
    actor Client
    Client->>Gateway: Request to /deliveries/create
    Gateway->>Gateway: Look up DELIVERY-SERVICE in Registry Cache
    Gateway->>MS: Route to specific IP:Port of Delivery Service
```
