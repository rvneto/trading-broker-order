# 🚀 Broker Order API

Microservice responsible for orchestrating the complete lifecycle of market orders in the **My Broker B3** ecosystem. It validates user balance, persists orders, routes them to the B3 Matching Engine via RabbitMQ, and publishes lifecycle events to Kafka for downstream consumers.

> 📘 This service is part of a series of articles documenting the **My Broker B3** ecosystem.
> Follow the full series on [dev.to/rvneto](https://dev.to/rvneto).

---

## 🏗️ Architecture & Flow
[User]
│
│  POST /api/v1/orders
▼
[broker-order-api]
│
├─ 1. Validate balance → Wallet API (Feign/REST)
├─ 2. Persist order   → MySQL (status: PENDING)
├─ 3. Send to B3      → RabbitMQ (mq-broker-to-b3)
└─ 4. Notify          → Kafka (order-events-v1, PENDING)
     ─ ─ ─ (B3 processes the order) ─ ─ ─
[b3-matching-engine-api]
│  RabbitMQ (mq-b3-to-broker)
▼
[broker-order-api]
├─ 5. Update status → MySQL (FILLED or REJECTED)
└─ 6. Notify        → Kafka (order-events-v1, final status)
│
[broker-wallet-api] (blocks/settles/refunds)

---

## 🛠️ Tech Stack

| Technology | Usage |
| :--- | :--- |
| **Java 21** + **Spring Boot 3.3.5** | Service core |
| **Spring Cloud OpenFeign** | Sync REST call to Wallet API for balance validation |
| **Apache Kafka** | Internal event bus — order lifecycle events (`order-events-v1`) |
| **RabbitMQ** | External integration with B3 Matching Engine |
| **MySQL 8.0** + **Flyway** | Order persistence and schema versioning |
| **SpringDoc OpenAPI** | Swagger UI documentation |

---

## 🌐 REST API Endpoints

Base URL: `http://localhost:8088/api/v1`

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| POST | `/orders` | Place a new buy or sell order |
| GET | `/orders/{id}` | Get order details by ID |
| GET | `/orders/user/{userId}` | List all orders for a user |

📄 **Swagger UI**: [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
📄 **OpenAPI Spec**: [http://localhost:8088/v3/api-docs](http://localhost:8088/v3/api-docs)

### Request Payload Example

```json
{
  "userId": "e82b8e13-1df9-41ba-a961-a28a4fe4e38b",
  "ticker": "PETR4",
  "quantity": 10,
  "price": 35.50,
  "side": "BUY"
}
```

## 🔧 Environment Variables

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3308` |
| `DB_USER` | MySQL username | `broker_user` |
| `DB_PASSWORD` | MySQL password | `broker_pass` |
| `KAFKA_HOST` | Kafka broker host | `localhost` |
| `RABBIT_HOST` | RabbitMQ host | `localhost` |
| `RABBIT_USER` | RabbitMQ username | `admin` |
| `RABBIT_PASSWORD` | RabbitMQ password | `admin_pass` |

## 📋 Prerequisites

Make sure the following services are running:

- MySQL on port `3308`
- Kafka on port `9092`
- RabbitMQ on port `5672` (Management UI on `15672`)
- `broker-wallet-api` on port `8085` (required for BUY order balance validation)
- `b3-matching-engine-api` on port `8091` (consuming from `mq-broker-to-b3`)

## 🐳 Running with Docker

```bash
docker build -t broker-order-api .
```

```bash
docker run --network finance-network \
  -e DB_HOST=broker-order-db \
  -e KAFKA_HOST=kafka \
  -e RABBIT_HOST=rabbitmq \
  broker-order-api
```

## 🚦 Health Check

Spring Actuator is enabled for health monitoring:

- Endpoint: `GET /actuator/health`
- Port: `8088`
