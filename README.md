# Realtime Leaderboard System

A real-time leaderboard system built with Spring Boot, Redis, and SQLite. This project implements a scalable backend service for managing user scores, rankings, and authentication.

**Project Reference:** [roadmap.sh - Realtime Leaderboard System](https://roadmap.sh/projects/realtime-leaderboard-system)

## Features

- 🔐 **JWT Authentication** - Secure user authentication and authorization
- 🏆 **Real-time Leaderboard** - Fast ranking updates using Redis
- 💾 **Persistent Storage** - SQLite database for user data
- 📊 **Score Management** - Add, update, and retrieve user scores
- 🔒 **Spring Security** - Protected endpoints with role-based access
- 📚 **API Documentation** - Interactive Swagger/OpenAPI documentation
- ⚡ **High Performance** - Redis caching for fast leaderboard queries

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Database operations
- **Spring Data Redis** - Caching and leaderboard management
- **SQLite** - Lightweight persistent storage
- **JWT (JSON Web Tokens)** - Secure authentication
- **Lombok** - Code simplification
- **Springdoc OpenAPI** - API documentation
- **Gradle** - Build automation

## Prerequisites

- Java 21 or higher
- Redis server (local or remote)
- Gradle (or use the included Gradle wrapper)

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd LeaderboardLearning
```

### 2. Configure Application

Copy the template configuration file:

```bash
cp application-template.properties application.properties
```

Edit `application.properties` and configure the following:

```properties
# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your-redis-password

# JWT Configuration (generate a secure secret)
jwt.secret=your-secret-key-here-change-this-to-a-strong-random-value-at-least-256-bits
jwt.expiration=86400000

# Database Configuration
spring.datasource.url=jdbc:sqlite:test.db
```

**Generate a secure JWT secret:**
```bash
openssl rand -base64 32
```

### 3. Build the Project

Using Gradle wrapper (Windows):
```bash
.\gradlew build
```

Using Gradle wrapper (Linux/Mac):
```bash
./gradlew build
```

### 4. Run the Application

```bash
.\gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access the interactive API documentation at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## Running Tests

Execute all tests:
```bash
.\gradlew test
```

View test reports:
- Open `build/reports/tests/test/index.html` in your browser

## Project Structure

```
src/
├── main/
│   ├── java/com/leaderboard/
│   │   ├── Main.java                 # Application entry point
│   │   ├── SecurityConfig.java       # Security configuration
│   │   ├── config/                   # Configuration classes
│   │   ├── controller/               # REST API controllers
│   │   ├── exception/                # Custom exceptions
│   │   ├── filter/                   # Security filters
│   │   ├── model/                    # Data models/entities
│   │   └── service/                  # Business logic services
│   └── resources/
│       └── application.properties    # Configuration file
└── test/                             # Test files
```

## Environment Variables

The application supports the following environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_HOST` | Redis server hostname | `localhost` |
| `REDIS_PORT` | Redis server port | `6379` |
| `REDIS_PASSWORD` | Redis password | (empty) |
| `JWT_SECRET` | Secret key for JWT signing | (see application.properties) |
| `JWT_EXPIRATION` | JWT token expiration time (ms) | `86400000` (24 hours) |
| `DATABASE_URL` | Database connection URL | `jdbc:sqlite:test.db` |

## Development

### Development Tools

The project includes Spring Boot DevTools for automatic restart during development.

### Code Quality

The project uses:
- Lombok to reduce boilerplate code
- Spring Boot validation for input validation
- JPA for database abstraction

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is created for learning purposes.

## Acknowledgments

- Project inspired by [roadmap.sh](https://roadmap.sh/projects/realtime-leaderboard-system)
- Built with Spring Boot and Redis
