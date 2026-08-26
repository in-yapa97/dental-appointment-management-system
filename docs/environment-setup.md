# Environment Setup Guide

## Prerequisites

- **Java Development Kit**: JDK 17 (or newer)
- **Node.js**: Node.js 18+ (Node 20+ LTS recommended) and npm
- **Database**: PostgreSQL 14+ (Local service or Docker container)

---

## Backend Environment Variables

The backend application is configured via environment variables. Create a `.env` file (or export the variables in your shell) based on the provided `backend/.env.example`:

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `SERVER_PORT` | Port the Spring Boot REST API listens on | `8080` |
| `DB_HOST` | Hostname of the PostgreSQL server | `localhost` |
| `DB_PORT` | Port of the PostgreSQL server | `5432` |
| `DB_NAME` | Database name | `dental_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | *(empty / prompt on startup)* |
| `DB_URL` | Full JDBC URL (overrides individual DB settings) | `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}` |

> [!NOTE]
> For Milestone 0, database credentials are not required to boot the application and inspect the `/api/v1/health` endpoint. Connection timeout resilience is enabled by default.

---

## Database Initialization (PostgreSQL)

When ready to initialize PostgreSQL locally:

```sql
-- Connect to PostgreSQL and create the database
CREATE DATABASE dental_db;
```

Ensure the user specified by `DB_USERNAME` has all necessary privileges on `dental_db`.
