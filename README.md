# NexMart E-Commerce Platform

NexMart is a Spring Boot 3 + Vue 3 e-commerce project built for a realistic interview/demo scenario. It covers user shopping workflows, admin management, promotions, flash sale ordering, payment, refund, WebSocket customer service, and a Spring AI + DeepSeek powered AI customer assistant.

Frontend repositories:

- User side: [NexMart-user](https://github.com/EricSu-Dev/NexMart-user)
- Admin side: [NexMart-admin](https://github.com/EricSu-Dev/NexMart-admin)

## Tech Stack

Backend:

- Java 21, Spring Boot 3.5, Spring Security, Spring Validation
- MyBatis-Plus, MySQL 8
- Redis, RabbitMQ
- Spring AI, DeepSeek OpenAI-compatible API
- WebSocket, JWT, SpringDoc OpenAPI
- Alipay Sandbox SDK, Aliyun OSS

Frontend:

- Vue 3
- Element Plus
- Axios
- ECharts

## Core Highlights

### AI Customer Service

- Uses Spring AI + DeepSeek for streaming AI customer service.
- Routes business questions through strategy-pattern intent handlers instead of a large switch block.
- Queries deterministic business data such as products, orders, coupons, points, promotions, and flash sale information from backend services.
- Uses an `ai_knowledge` table as a lightweight RAG-style knowledge source for shopping rules, refund policy, coupon rules, and flash sale explanations.
- Stores short-term multi-turn context in Redis and persists chat history in MySQL.
- Falls back to AI recommendations when product search returns no exact result.

### Order And Payment Safety

- Order creation validates cart ownership and product availability.
- Product stock and SKU stock are deducted with conditional updates.
- Coupons are consumed with user, status, type, and expiration checks to avoid duplicate or cross-user usage.
- Order timeout cancellation is driven by RabbitMQ delayed/dead-letter flow, with scheduled scan as a final consistency fallback.
- Alipay callback handling validates signature-related business fields such as `app_id`, optional `seller_id`, order number, and total amount before updating order/payment status.

### Flash Sale Design

- Flash sale stock is preloaded into Redis.
- Lua scripts atomically check stock and per-user purchase limits.
- Successful Redis deduction sends an async RabbitMQ message for database order creation.
- Consumer-side idempotency prevents duplicate order creation when MQ redelivers the same message.
- Redis rollback is executed when message sending or final consumption fails.

### RabbitMQ Reliability

- Producer-side confirm and return callbacks retry failed sends.
- Order timeout messages use delay/dead-letter queues.
- Flash sale consumers use manual ACK.
- Consumption failures are retried through retry queues; after retries are exhausted, messages are sent to failed compensation queues.
- Failed queues preserve messages for manual or scheduled compensation.

### Redis Usage

- Category and homepage data cache.
- Flash sale stock and user purchase-count cache.
- Search hot keywords.
- Check-in status and streak tracking.
- Cache invalidation when categories/products/home sections change.

### Security And Enterprise Enhancements

- JWT authentication reloads user status and role from DB instead of trusting only token claims.
- Configurable CORS origins.
- Unified result format with `traceId`.
- Global exception handling and unified error codes.
- Request trace filter with operation logging.
- Upload security checks: file size, extension, MIME type, and file magic number validation.
- Health check endpoint.
- SpringDoc OpenAPI configuration with JWT bearer auth.
- Dockerfile and Docker Compose for local one-command startup.

## Main Modules

- User authentication and profile management
- Product, category, favorite, browse history, and search
- Cart and order creation
- Alipay payment and refund workflow
- Coupon, promotion, and points system
- Flash sale activities and async order creation
- Manual customer service via WebSocket
- AI customer service with business-data query and knowledge retrieval
- Admin dashboard, product, order, coupon, flash sale, user, and support management

## Recent Enhancements

### 2026-07-07

- Added AI knowledge retrieval based on MySQL `ai_knowledge`.
- Refactored AI intent routing from switch logic to strategy-pattern handlers.
- Hardened order creation, coupon consumption, payment callback validation, JWT authentication, CORS, and upload validation.
- Added request traceId, global error code handling, operation logs, health check, OpenAPI security config, Dockerfile, and Docker Compose.
- Added RabbitMQ consumer retry and failed compensation queues for order timeout and flash sale order processing.
- Added unit and lightweight integration tests for login authentication, order creation, flash sale ordering, RabbitMQ idempotency, Redis cache reads, and application context loading.

## Test Coverage

Run all tests:

```bash
mvn test
```

Current core tests cover:

- `LoginServiceImplTest`: valid login token generation and invalid credential rejection.
- `AuthControllerIntegrationTest`: login API JSON response and request validation.
- `OrderServiceImplTest`: order creation, stock deduction, order item saving, cart cleanup, and timeout MQ scheduling.
- `SeckillOrderServiceImplTest`: Redis flash sale reservation and async MQ dispatch.
- `OrderMQConsumerTest`: MQ duplicate message idempotency.
- `CategoryServiceImplTest`: Redis cache-hit read path.
- `NexMartApplicationTests`: Spring application context startup.

## Quick Start

### Prerequisites

- JDK 21
- Maven 3.9+
- Docker and Docker Compose

### Start With Docker Compose

```bash
docker compose up -d --build
```

Services:

- Backend: `http://localhost:8087`
- Swagger UI: `http://localhost:8087/swagger-ui.html`
- RabbitMQ Management: `http://localhost:15672`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

The compose file starts MySQL, Redis, RabbitMQ, and the backend service. MySQL is initialized with the project SQL scripts, including AI knowledge schema and seed data.

### Local Development

Create `src/main/resources/application-local.yml` for local secrets if needed, or provide environment variables:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/nexmart_db?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your_password
  data:
    redis:
      host: localhost
  rabbitmq:
    host: localhost
  ai:
    openai:
      api-key: your_deepseek_api_key

jwt:
  secret: please-change-this-secret-at-least-32-chars

alipay:
  app-id: your_app_id
  private-key: your_private_key
  alipay-public-key: your_alipay_public_key
  notify-url: your_public_notify_url

aliyun:
  oss:
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
```

Start backend:

```bash
mvn spring-boot:run
```

## Interview Talking Points

- Why flash sale stock is deducted in Redis first and persisted asynchronously through MQ.
- How Lua scripts avoid overselling and duplicate purchases.
- How MQ producer confirm, retry queues, failed queues, and manual ACK improve reliability.
- Why payment callbacks must validate amount, app id, seller id, order number, and signature.
- Why AI customer service should use backend deterministic queries for business data and RAG-style knowledge retrieval for rules.
- How traceId, unified errors, validation, health checks, Docker Compose, and tests make the project closer to enterprise practice.
