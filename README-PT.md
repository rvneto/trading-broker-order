# 🚀 Broker Order API

Microserviço responsável por orquestrar o ciclo de vida completo das ordens de mercado no ecossistema **My Broker B3**. Valida o saldo do usuário, persiste as ordens, as encaminha ao Matching Engine da B3 via RabbitMQ e publica eventos de ciclo de vida no Kafka para os consumidores downstream.

> 📘 Este serviço faz parte de uma série de artigos documentando o ecossistema **My Broker B3**.
> Acompanhe a série completa em [dev.to/rvneto](https://dev.to/rvneto).

---

## 🏗️ Arquitetura e Fluxo
[Usuário]
│
│  POST /api/v1/orders
▼
[broker-order-api]
│
├─ 1. Valida saldo  → Wallet API (Feign/REST)
├─ 2. Persiste      → MySQL (status: PENDING)
├─ 3. Envia para B3 → RabbitMQ (mq-broker-to-b3)
└─ 4. Notifica      → Kafka (order-events-v1, PENDING)
     ─ ─ ─ (B3 processa a ordem) ─ ─ ─
[b3-matching-engine-api]
│  RabbitMQ (mq-b3-to-broker)
▼
[broker-order-api]
├─ 5. Atualiza status → MySQL (FILLED ou REJECTED)
└─ 6. Notifica        → Kafka (order-events-v1, status final)
│
[broker-wallet-api] (bloqueia/liquida/estorna)

---

## 🛠️ Stack Tecnológica

| Tecnologia | Uso |
| :--- | :--- |
| **Java 21** + **Spring Boot 3.3.5** | Core do serviço |
| **Spring Cloud OpenFeign** | Chamada REST síncrona à Wallet API para validação de saldo |
| **Apache Kafka** | Barramento interno de eventos de ciclo de vida (`order-events-v1`) |
| **RabbitMQ** | Integração externa com o Matching Engine da B3 |
| **MySQL 8.0** + **Flyway** | Persistência de ordens e versionamento do schema |
| **SpringDoc OpenAPI** | Documentação via Swagger UI |

---

## 🌐 Endpoints REST

Base URL: `http://localhost:8088/api/v1`

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| POST | `/orders` | Submeter nova ordem de compra ou venda |
| GET | `/orders/{id}` | Consultar detalhes de uma ordem por ID |
| GET | `/orders/user/{userId}` | Listar todas as ordens de um usuário |

📄 **Swagger UI**: [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)
📄 **OpenAPI Spec**: [http://localhost:8088/v3/api-docs](http://localhost:8088/v3/api-docs)

### Exemplo de Payload (POST /orders)

```json
{
  "userId": "e82b8e13-1df9-41ba-a961-a28a4fe4e38b",
  "ticker": "PETR4",
  "quantity": 10,
  "price": 35.50,
  "side": "BUY"
}
```

## 🔧 Variáveis de Ambiente

| Variável | Descrição | Padrão |
| :--- | :--- | :--- |
| `DB_HOST` | Host do MySQL | `localhost` |
| `DB_PORT` | Porta do MySQL | `3308` |
| `DB_USER` | Usuário do MySQL | `broker_user` |
| `DB_PASSWORD` | Senha do MySQL | `broker_pass` |
| `KAFKA_HOST` | Host do broker Kafka | `localhost` |
| `RABBIT_HOST` | Host do RabbitMQ | `localhost` |
| `RABBIT_USER` | Usuário do RabbitMQ | `admin` |
| `RABBIT_PASSWORD` | Senha do RabbitMQ | `admin_pass` |

## 📋 Pré-requisitos

Certifique-se de que os seguintes serviços estão rodando:

- MySQL na porta `3308`
- Kafka na porta `9092`
- RabbitMQ na porta `5672` (Management UI na `15672`)
- `broker-wallet-api` na porta `8085` (necessário para validação de saldo em ordens BUY)
- `b3-matching-engine-api` na porta `8091` (consumindo de `mq-broker-to-b3`)

## 🐳 Rodando com Docker

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

O Spring Actuator está habilitado para monitoramento:

- Endpoint: `GET /actuator/health`
- Porta: `8088`
