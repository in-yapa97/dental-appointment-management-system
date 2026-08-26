# Dental Appointment and Patient Management System

A university assessment project designed to deliver a modern, reliable, and scalable web-based solution for managing dental clinic workflows, patient records, and appointment scheduling.

> [!NOTE]
> **Project Status: Milestone 0 (Foundation)**  
> This repository contains the initial project foundation and infrastructure. Business features (such as appointment booking, patient records, dental charts, and billing) have **not** been implemented yet and are scheduled for subsequent milestones.

---

## Technology Stack

### Frontend
- **Framework**: [React 19](https://react.dev/)
- **Language**: [TypeScript](https://www.typescriptlang.org/)
- **Build Tool**: [Vite](https://vitejs.dev/)
- **Styling**: Vanilla CSS with modern custom design tokens

### Backend
- **Framework**: [Spring Boot 3.3](https://spring.io/projects/spring-boot)
- **Language**: [Java 17 LTS](https://www.oracle.com/java/)
- **Build Tool**: Apache Maven (via Maven Wrapper `mvnw`)
- **API Style**: RESTful API (JSON)
- **Architecture**: Layered Architecture (Controller → Service → Repository)

### Database
- **Engine**: [PostgreSQL](https://www.postgresql.org/)
- **ORM / Persistence**: Spring Data JPA / Hibernate

### Testing & Quality
- **Unit & Integration Testing**: JUnit 5, Mockito, Spring Boot Test
- **Version Control**: Git & GitHub (`develop` branch workflow)
- **CI/CD**: GitHub Actions (ready for pipeline integration)

---

## Planned Architecture

The system follows a strict layered architecture to guarantee separation of concerns, testability, and maintainability:

```
React Frontend (Vite + TypeScript)
               │  HTTP / REST
               ▼
   Spring Boot REST Controllers (Thin layer, route & DTO mapping)
               │
   Service Layer (Business rules, workflows, validation)
               │
   Repository Layer (Spring Data JPA abstractions)
               │  JDBC / SQL
               ▼
   PostgreSQL Database
```

For detailed architectural principles, see [docs/architecture.md](docs/architecture.md).

---

## Repository Structure

```
.
├── frontend/             # React + TypeScript Vite frontend application
│   ├── src/
│   │   ├── components/   # Reusable UI components
│   │   ├── pages/        # View / Page level components
│   │   ├── services/     # API communication clients
│   │   ├── types/        # TypeScript interfaces & domain models
│   │   ├── App.tsx       # Root component & system status dashboard
│   │   └── main.tsx      # Application entry point
│   └── package.json
├── backend/              # Spring Boot Maven backend application
│   ├── src/
│   │   ├── main/java/com/dental/management/
│   │   │   ├── controller/   # REST Controllers (HealthController)
│   │   │   ├── service/      # Service interfaces & implementations
│   │   │   ├── repository/   # JPA repositories (prepared for M1)
│   │   │   ├── entity/       # JPA entities (prepared for M1)
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── exception/    # Global exception handling
│   │   │   ├── config/       # Web & CORS configuration
│   │   │   └── DentalManagementApplication.java
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml
│   └── mvnw / mvnw.cmd   # Cross-platform Maven Wrapper
├── docs/                 # Project documentation & design specs
├── .gitignore
└── README.md
```

---

## Setup & Running the Application

### Prerequisites
- **Node.js**: v18+ or v20+ LTS and npm
- **Java**: JDK 17 LTS or higher
- **PostgreSQL**: v14+ (optional for Milestone 0 health checks)

---

### 1. Running the Frontend

Navigate to the `frontend/` directory, install dependencies, and start the development server:

```bash
# Navigate to frontend folder
cd frontend

# Install dependencies
npm install

# Start Vite dev server
npm run dev
```

The frontend application will be accessible at: `http://localhost:3000` (or the port indicated in the terminal).

To build the frontend for production:

```bash
npm run build
```

---

### 2. Running the Backend

Navigate to the `backend/` directory and run the Spring Boot application using the included Maven Wrapper:

**On Windows (PowerShell / Command Prompt):**
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

The REST API will start on: `http://localhost:8080`

To run backend tests:
```powershell
.\mvnw.cmd test
```

To package the backend into an executable JAR:
```powershell
.\mvnw.cmd package
```

---

### 3. Verifying the System Health Endpoint

With the backend running, verify the service status:

```bash
curl http://localhost:8080/api/v1/health
```

Expected JSON response:
```json
{
  "status": "UP",
  "service": "Dental Appointment and Patient Management System API",
  "timestamp": "2026-08-26T07:30:00Z",
  "version": "0.0.1-SNAPSHOT"
}
```

---

## Environment Configuration

The backend is configured via environment variables with zero hard-coded secrets. An example template is provided in `backend/.env.example`.

Refer to [docs/environment-setup.md](docs/environment-setup.md) for full configuration options.
