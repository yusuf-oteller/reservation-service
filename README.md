# Reservation Service

This service manages hotel reservations in a microservices-based architecture. It is responsible for creating, updating, retrieving, and deleting reservations. It also listens for payment status updates via Kafka and adjusts reservation status accordingly.

## Features

- Create, update, retrieve, delete reservations
- Prevent double-booking via pessimistic locking
- Kafka integration:
  - Listens to `payment-result-topic`
  - Sends failed messages to `payment-result-dead-letter`
- JWT-based authentication and authorization
- Integration and unit test support
- JSON structured logging via Logstash

## Tech Stack

- Java 21
- Spring Boot 3.x
- PostgreSQL
- Kafka
- Docker
- JUnit & Testcontainers
- Lombok
- MapStruct
- Logstash Logback

## Endpoints

| Method | Endpoint             | Description            | Auth        |
|--------|----------------------|------------------------|-------------|
| POST   | /api/v1/reservations | Create reservation     | USER, ADMIN |
| GET    | /api/v1/reservations/{id} | Get reservation by ID | USER, ADMIN |
| GET    | /api/v1/reservations | Get all reservations   | ADMIN       |
| PUT    | /api/v1/reservations/{id} | Update reservation     | USER, ADMIN |
| DELETE | /api/v1/reservations/{id} | Delete reservation     | USER, ADMIN |

## Kafka Topics

- `payment-result-topic`: receives `PaymentResultEvent`
- `payment-result-dead-letter`: DLQ for failures

## Running the Service

```bash
docker-compose up --build
```

## Running Tests

```bash
./mvnw clean test
```

Test types:

- `ReservationServiceTest`: Unit tests for service logic
- `ReservationControllerTest`: REST endpoint tests with WebTestClient
- `KafkaPaymentConsumerTest`: Verifies Kafka event consumption behavior

## Environment Variables

| Variable                   | Description                         |
|----------------------------|-------------------------------------|
| SPRING_PROFILES_ACTIVE     | e.g. `docker`, `dev`                |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | Kafka broker address           |
| JWT_SECRET                 | JWT signing secret                  |
| DB_URL, DB_USERNAME, DB_PASSWORD | PostgreSQL credentials        |

## Future Improvements

- Retry & backoff logic on Kafka consumer
- Outbox pattern implementation
- Email notifications on confirmation
