# Real-Time Betting Engine

Production-style real-time betting backend system built using Java, Spring Boot, Kafka, PostgreSQL, Redis, and WebSocket technologies.

---

# Project Overview

This project simulates a scalable betting platform backend capable of handling:

- Real-time odds updates
- Bet placement processing
- Bet settlements
- Event streaming
- Live WebSocket updates
- Distributed event-driven communication
- Secure backend APIs

The system architecture follows modern production engineering principles commonly used in iGaming and high-throughput backend systems.

---

# Tech Stack

## Backend
- Java 25
- Spring Boot 3.2.5
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate ORM

## Database
- PostgreSQL

## Messaging & Streaming
- Apache Kafka

## Real-Time Communication
- WebSocket

## Caching
- Redis

## Build Tool
- Maven

## Embedded Server
- Apache Tomcat

## JSON Handling
- Jackson JSON

---

# System Architecture

```text
Client UI
   ↓
REST API / WebSocket
   ↓
Spring Boot Backend
   ↓
Kafka Event Streaming
   ↓
Consumers & Services
   ↓
PostgreSQL + Redis
```

---

# Main Features

## User Management
- User registration
- Authentication
- Wallet handling
- User balance tracking

## Betting System
- Place bets
- Store bet history
- Bet settlement processing
- Odds management

## Real-Time Updates
- Live odds broadcasting
- WebSocket event streaming
- Instant client updates

## Kafka Event Processing
- Bet placement events
- Odds update events
- Settlement events

## Security
- Spring Security filters
- Protected APIs
- Authentication handling

---

# Project Structure

```text
Real-Time-Betting-Engine/
│
├── src/
│   ├── main/
│   │   ├── java/com/betting/
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── BetController.java
│   │   │   └── OddsController.java
│   │   │
│   │   ├── service/
│   │   │   ├── BetService.java
│   │   │   ├── WalletService.java
│   │   │   └── KafkaProducerService.java
│   │   │
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── BetRepository.java
│   │   │   ├── WalletRepository.java
│   │   │   ├── EventRepository.java
│   │   │   └── OddsRepository.java
│   │   │
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Bet.java
│   │   │   ├── Wallet.java
│   │   │   ├── Event.java
│   │   │   └── Odds.java
│   │   │
│   │   ├── kafka/
│   │   │   ├── BetPlacementConsumer.java
│   │   │   ├── OddsUpdateConsumer.java
│   │   │   └── SettlementConsumer.java
│   │   │
│   │   ├── websocket/
│   │   │   └── OddsWebSocketHandler.java
│   │   │
│   │   ├── security/
│   │   │   ├── SecurityConfig.java
│   │   │   └── JwtFilter.java
│   │   │
│   │   └── BettingEngineApplication.java
│   │
│   └── resources/
│       ├── application.yml
│       ├── templates/
│       └── static/
│
├── pom.xml
├── README.md
└── docker-compose.yml
```

---

# Kafka Topics

```text
bet-placements
odds-updates
bet-settlements
```

---

# WebSocket Flow

```text
Odds Update
    ↓
Kafka Producer
    ↓
Kafka Topic
    ↓
Kafka Consumer
    ↓
WebSocket Broadcast
    ↓
Connected Clients
```

---

# Database Entities

## User
Stores:
- Username
- Password
- Wallet balance
- Roles

## Bet
Stores:
- Bet amount
- Odds
- Event details
- Bet status

## Wallet
Stores:
- Balance
- Transactions
- User relation

## Event
Stores:
- Match information
- Betting event details

## Odds
Stores:
- Live odds
- Market updates

---

# Redis Usage

Redis can be used for:

- Live odds caching
- Session storage
- Fast lookup operations
- Real-time temporary data

Example:

```java
@RedisHash
public class LiveOdds {
}
```

---

# Running the Project

## Start PostgreSQL

```bash
sudo systemctl start postgresql
```

## Start Redis

```bash
sudo systemctl start redis
```

## Start Kafka

```bash
bin/zookeeper-server-start.sh config/zookeeper.properties

bin/kafka-server-start.sh config/server.properties
```

## Run Spring Boot Application

```bash
mvn spring-boot:run
```

---

# Default Server

```text
http://localhost:8080
```

---

# Important Production Concepts Used

- Event-Driven Architecture
- Distributed Messaging
- Real-Time Communication
- Async Processing
- Concurrent Consumers
- Connection Pooling
- Secure API Design
- High Throughput Backend Systems

---

# Current Working Components

## Confirmed Running From Logs

- Spring Boot
- PostgreSQL
- Hibernate
- Spring Security
- Kafka
- Kafka Consumers
- WebSocket
- Tomcat
- HikariCP
- Jackson JSON

---

# Future Improvements

- JWT Authentication
- Docker Deployment
- Kubernetes Deployment
- Kafka Cluster
- Redis Cluster
- Load Balancing
- Monitoring with Prometheus & Grafana
- CI/CD Pipelines
- ClickHouse Analytics
- Rate Limiting

---

# Industry Use Cases

This architecture pattern is commonly used in:

- Betting Platforms
- Stock Trading Systems
- Payment Gateways
- Real-Time Analytics Systems
- Financial Applications
- Distributed Event Platforms

---

# Author

Missari Ahil

Portfolio:
https://missari.pages.dev/

---
