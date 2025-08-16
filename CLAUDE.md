# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java-based implementation of "三國殺" (Legends of The Three Kingdoms), a Chinese card game based on the Three Kingdoms period. The project is built using Spring Boot 3, JDK 17, and Maven, following Domain-Driven Design (DDD) principles with clean architecture.

## Common Development Commands

### Build and Run
- `./mvnw clean install` - Full build with tests
- `./mvnw spring-boot:run -pl spring` - Run the Spring Boot application
- `./mvnw test` - Run unit tests
- `./mvnw test -Dtest=ClassName` - Run a specific test class
- `./mvnw test -Dtest=ClassName#methodName` - Run a specific test method

### Code Quality
- `./mvnw checkstyle:check` - Run Checkstyle analysis
- `./mvnw spotbugs:check` - Run SpotBugs analysis
- `./mvnw verify` - Run integration tests

### Docker
- `docker-compose up -d` - Start MongoDB and other services
- `docker build -f docker/Dockerfile .` - Build application Docker image

## Architecture

### Module Structure
The project follows a multi-module Maven structure with clean architecture:

- **domain/** - Core business logic and domain models
  - Contains the main `Game` class which orchestrates game flow
  - Implements game rules, player actions, and card behaviors
  - Uses behavior patterns for handling different card effects

- **app/** - Application layer (use cases)
  - Contains use cases like `PlayCardUseCase`, `StartGameUseCase`
  - Implements the application logic without framework dependencies

- **spring/** - Infrastructure layer
  - Spring Boot web application with REST controllers
  - WebSocket support for real-time game updates
  - MongoDB integration with repository pattern
  - Presentation layer with DTOs and ViewModels

### Key Design Patterns
- **Behavior Pattern**: Used for handling different card effects and player actions
- **Chain of Responsibility**: `PlayCardBehaviorHandler` chains different card behaviors
- **Repository Pattern**: `GameRepository` interface with multiple implementations
- **Clean Architecture**: Clear separation between domain, application, and infrastructure layers

### Game Flow
1. Game initialization with player setup and role assignment
2. Round-based gameplay with phases: Judgement → Drawing → Action → Discard
3. Card effects handled through behavior stack system
4. Real-time updates via WebSocket to all players

## Testing

### Test Structure
- Domain tests focus on game logic and rules
- Integration tests use TestContainers for MongoDB
- Extensive JSON test fixtures in `spring/src/test/resources/TestJsonFile/`
- Test utilities in `domain/src/test/java/com/gaas/threeKingdoms/Utils.java`

### Running Tests
- Unit tests: Focus on individual components and behaviors
- Integration tests: Test full game flows and scenarios
- Test data: Pre-configured game states in JSON format for complex scenarios

## Important Configuration

### Environment Variables
- `MONGODB_URI` - MongoDB connection string (required for persistent storage)

### Development Setup
1. Ensure JDK 17 is installed
2. Start MongoDB (via Docker or locally)
3. Set `MONGODB_URI` environment variable
4. Run `./mvnw spring-boot:run -pl spring`

## Code Conventions
- Use Lombok for reducing boilerplate code
- Follow Java naming conventions
- Domain models should be framework-agnostic
- Use dependency injection through constructor injection
- Implement proper error handling with domain-specific exceptions