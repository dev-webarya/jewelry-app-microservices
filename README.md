# Jewelry App Microservices

Industry-grade microservices architecture for a jewelry e-commerce platform, built with Spring Boot 3.2 and Spring Cloud.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway (:8080)                       │
│                      (JWT Auth + Routing)                        │
└─────────────────────────────┬───────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────────┐
│                     Eureka Server (:8761)                        │
│                     (Service Discovery)                          │
└─────────────────────────────┬───────────────────────────────────┘
          ┌───────────┬───────┴───────┬───────────┬───────────┐
          ▼           ▼               ▼           ▼           ▼
     ┌─────────┐ ┌─────────┐   ┌─────────┐ ┌─────────┐ ┌─────────┐
     │  User   │ │ Product │   │  Order  │ │  Cart   │ │  Store  │
     │ :8081   │ │ :8082   │   │ :8084   │ │ :8085   │ │ :8089   │
     └─────────┘ └─────────┘   └─────────┘ └─────────┘ └─────────┘
```

## Tech Stack

- **Spring Boot 3.2.0** - Base framework
- **Spring Cloud 2023.0.0** - Microservices patterns
- **Netflix Eureka** - Service discovery
- **Spring Cloud Gateway** - API Gateway
- **OpenFeign** - Inter-service communication
- **H2 Database** - Development database
- **Docker Compose** - Container orchestration
- **SpringDoc OpenAPI** - API documentation

## Services

| Service | Port | Description |
|---------|------|-------------|
| config-server | 8888 | Centralized configuration |
| eureka-server | 8761 | Service registry |
| api-gateway | 8080 | API Gateway + JWT auth |
| user-service | 8081 | Authentication & users |
| product-service | 8082 | Product catalog |
| inventory-service | 8083 | Stock management |
| order-service | 8084 | Order processing |
| cart-service | 8085 | Shopping cart |
| payment-service | 8086 | Payments |
| shipping-service | 8087 | Shipments |
| notification-service | 8088 | Emails/OTP |
| store-service | 8089 | Store management |
| review-service | 8090 | Product reviews |

## Quick Start

### Using Docker Compose (Recommended)
```bash
# Build and run all services
./build.bat          # Windows
./build.sh           # Linux/Mac

# Start containers
docker-compose up -d

# View logs
docker-compose logs -f api-gateway
```

### Local Development
```bash
# Start in order:
cd config-server && mvn spring-boot:run
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
# ... other services
```

## API Documentation

- **Aggregated Swagger**: http://localhost:8080/swagger-ui.html
- **Eureka Dashboard**: http://localhost:8761

## Sample Users (Dev Profile)

| Email | Password | Role |
|-------|----------|------|
| admin@app.com | Password@123 | ADMIN |
| manager.main@app.com | Password@123 | STORE_MANAGER |
| manager.mall@app.com | Password@123 | STORE_MANAGER |

## Project Structure

```
jewelry-app-microservices/
├── common-lib/           # Shared DTOs, exceptions, Feign clients
├── config-server/        # Spring Cloud Config
├── eureka-server/        # Service discovery
├── api-gateway/          # Gateway + JWT filter
├── user-service/         # Auth & users
├── product-service/      # Catalog
├── inventory-service/    # Stock
├── order-service/        # Orders
├── cart-service/         # Cart
├── payment-service/      # Payments
├── shipping-service/     # Shipping
├── notification-service/ # Notifications
├── store-service/        # Stores
├── review-service/       # Reviews
├── docker-compose.yml    # Container orchestration
└── pom.xml              # Parent POM
```

## License

MIT
