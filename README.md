# 三國殺 Legends of The Three Kingdoms

<p align="center">
  <img src="game_picture.jpeg" width="600" alt="Legends of The Three Kingdoms" />
</p>

A multiplayer online card game inspired by the classic Chinese board game **三國殺**, set in the Three Kingdoms era. Players take on the roles of iconic historical generals, using strategy, deception, and combat cards to outwit their opponents.

> 3–8 players | Role-based hidden identity | Turn-based strategy

## Architecture

The project follows **Clean Architecture** with a multi-module Maven structure:

```
LegendsOfTheThreeKingdoms/
├── domain/     # Core business logic, game rules, card behaviors
├── app/        # Application layer (use cases)
└── spring/     # Infrastructure: REST API, WebSocket, MongoDB
```

### Design Principles

- **Domain-Driven Design** — Game rules live in a framework-agnostic domain layer
- **Behavior Stack** — Card effects are modeled as stackable behaviors (e.g., Ward can interrupt any scroll card)
- **Test-Driven Development** — Domain tests first, then E2E integration tests (ATDD)
- **Event-Driven Updates** — Real-time game state pushed to all players via WebSocket

## Tech Stack

| Layer          | Technology                          |
|----------------|-------------------------------------|
| Language       | Java 17                             |
| Framework      | Spring Boot 3                       |
| Build          | Maven (multi-module)                |
| Database       | MongoDB                             |
| Real-time      | WebSocket (STOMP)                   |
| Testing        | JUnit 5, Testcontainers, MockMvc    |
| CI/CD          | GitHub Actions, Docker, AWS ECR/EC2 |

## Getting Started

### Prerequisites

- JDK 17
- Docker (for MongoDB via Testcontainers)

### Run Locally

```bash
# Start MongoDB
docker-compose up -d

# Build and run
./mvnw clean install
./mvnw spring-boot:run -pl spring
```

### Run Tests

```bash
# All tests
./mvnw verify

# Domain tests only
./mvnw test -pl domain

# Specific test class
./mvnw test -Dtest=ClassName
```

## Development Methodology

This project is built following a rigorous software craftsmanship approach:

1. **Event Storming** — Align team understanding of game flows
2. **Example Mapping** — Break down rules into concrete scenarios
3. **OOAD / UML** — Design domain models before coding
4. **Walking Skeleton** — Ship a minimal viable product first
5. **ATDD** — Write E2E acceptance tests from the user's perspective
6. **TDD** — Red-Green-Refactor at the domain level

## License

This project is for educational and practice purposes.
