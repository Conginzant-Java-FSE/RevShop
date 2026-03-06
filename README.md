# RevShop Backend 🛍️

Spring Boot 3 REST API for the RevShop e-commerce platform, supporting Buyers, Sellers, and Shippers with JWT-based role access control, product management, order processing, and in-app notifications.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Order Status Flow](#order-status-flow)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4 |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8 (production), H2 (testing) |
| Payment | Razorpay API |
| Build | Maven 3.9 |
| Testing | JUnit 5, Spring Test, H2 |
| Code Quality | SonarQube |

---

## Prerequisites

- Java 21+
- MySQL 8.0+
- Maven 3.9+ *(or use the included `mvnw` wrapper)*

---

## Getting Started

### 1. Create the Database

```sql
CREATE DATABASE revshop;
```

Optionally seed with sample data:
```bash
mysql -u root -p revshop < ../sample_data.sql
```

### 2. Configure the Application

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/revshop
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

jwt.secret=YOUR_JWT_SECRET_KEY

razorpay.key.id=YOUR_RAZORPAY_KEY_ID
razorpay.key.secret=YOUR_RAZORPAY_KEY_SECRET

spring.jpa.hibernate.ddl-auto=update
```

### 3. Run the Application

```bash
# Windows
./mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

API available at: **http://localhost:8080**

---

## Configuration

| Property | Description |
|----------|-------------|
| `spring.datasource.url` | MySQL JDBC connection URL |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `jwt.secret` | Secret key for signing JWT tokens |
| `razorpay.key.id` | Razorpay public API key |
| `razorpay.key.secret` | Razorpay secret key |

---

## Running Tests

```bash
# Run all unit and integration tests
./mvnw.cmd test

# Run with SonarQube scan
./mvnw.cmd clean verify sonar:sonar
```

> Tests use an **H2 in-memory database** — no MySQL required for testing.

The SonarQube token is stored in `~/.m2/settings.xml` (not committed to version control).

---

## API Overview

All protected endpoints require: `Authorization: Bearer <JWT_TOKEN>`

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register/buyer` | Register as buyer |
| `POST` | `/api/auth/register/seller` | Register as seller |
| `POST` | `/api/auth/login` | Login — returns JWT |
| `POST` | `/api/auth/shipper/register` | Register shipper |
| `POST` | `/api/auth/shipper/login` | Shipper login |

### Products
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/products` | List products (paginated, sortable) |
| `GET` | `/api/products/search` | Search by keyword |
| `GET` | `/api/products/filter` | Filter by category, price range |
| `GET` | `/api/products/{id}` | Get product details |
| `POST` | `/api/products` | Add product *(Seller)* |
| `PUT` | `/api/products/{id}` | Update product *(Seller)* |
| `DELETE` | `/api/products/{id}` | Delete product *(Seller)* |

### Cart & Orders
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/cart/{userId}` | Get cart |
| `POST` | `/api/cart/add` | Add item to cart |
| `PUT` | `/api/cart/update` | Update item quantity |
| `DELETE` | `/api/cart/remove/{itemId}` | Remove cart item |
| `POST` | `/api/orders/place` | Place an order |
| `GET` | `/api/orders/buyer/{userId}` | Buyer order history |
| `GET` | `/api/orders/seller/{sellerId}` | Seller order list |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/payments/create-order` | Create Razorpay order |
| `POST` | `/api/payments/verify` | Verify payment signature |

### Other
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/notifications/{userId}` | Get notifications |
| `PUT` | `/api/notifications/{id}/read` | Mark as read |
| `GET` | `/api/reviews/product/{productId}` | Get product reviews |
| `POST` | `/api/reviews` | Submit a review |
| `GET` | `/api/tracking/{orderId}` | Get order tracking history |

---

## Project Structure

```
src/
├── main/java/com/revature/revshop/
│   ├── controller/        # REST Controllers (15)
│   ├── service/           # Business Logic (19)
│   ├── repository/        # Spring Data JPA Repos (17)
│   ├── model/             # JPA Entities (17)
│   ├── dto/               # Data Transfer Objects (29)
│   ├── security/          # JWT Filter, Util, Config
│   ├── config/            # App & Jackson Config
│   └── exception/         # Custom Exceptions (9)
├── main/resources/
│   └── application.properties
└── test/
    ├── java/              # Unit & Integration Tests
    └── resources/
        └── application.properties   # H2 test config
```

---

## Order Status Flow

```
PENDING → PROCESSING → SHIPPED → OUT_FOR_DELIVERY → DELIVERED
    ↓                                                    ↓
CANCELLED                                      RETURN_REQUESTED
                                               ↙            ↘
                                    RETURN_APPROVED    RETURN_REJECTED
```

---

## Roles

| Role | Description |
|------|-------------|
| `BUYER` | Browse, purchase, review, track orders |
| `SELLER` | Manage products, view orders, receive stock alerts |
| `SHIPPER` | View & update assigned delivery orders |
