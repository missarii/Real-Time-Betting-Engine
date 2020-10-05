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
