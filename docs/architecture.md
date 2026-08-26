# System Architecture & Layered Design

## Overview
The **Dental Appointment and Patient Management System** is designed as a modern, decoupled web application following clean architecture and separation of concerns.

```
+------------------------------------------------------------------+
|                    React Frontend (Vite + TS)                    |
|          Pages  <--->  Components  <--->  API Services           |
+------------------------------------------------------------------+
                                 │
                            HTTP / REST
                                 ▼
+------------------------------------------------------------------+
|                    Spring Boot REST API                          |
|                                                                  |
|   [Controller Layer]       Thin REST controllers                 |
|            │               - Request mapping & input validation  |
|            ▼               - HTTP status codes & DTO responses   |
|                                                                  |
|   [Service Layer]          Business Logic Layer                  |
|            │               - Core domain operations & workflows  |
|            ▼               - Transaction management              |
|                                                                  |
|   [Repository Layer]       Data Access Layer                     |
|            │               - Spring Data JPA interfaces          |
|            ▼               - Queries and persistence             |
+------------------------------------------------------------------+
                                 │
                            JDBC / SQL
                                 ▼
+------------------------------------------------------------------+
|                    PostgreSQL Database                           |
+------------------------------------------------------------------+
```

---

## Architectural Principles

1. **Thin Controllers**:
   Controllers only handle HTTP request routing, basic validation, and delegating calls to the service layer. No business logic is placed in controllers.

2. **Decoupled Service Layer**:
   Business rules, validation, and domain logic reside exclusively in services, exposed via clean Java interfaces and implemented by Spring-managed service beans.

3. **Repository Abstraction**:
   Database access is encapsulated using Spring Data JPA repository interfaces, abstracting SQL and underlying storage details from the business logic.

4. **DTO-Based Communication**:
   Entities are not exposed directly to the REST API surface. Dedicated Data Transfer Objects (DTOs) protect internal database schemas and ensure clean API contracts.

5. **Centralized Error Handling**:
   A global exception handler (`@RestControllerAdvice`) provides consistent, standardized JSON error responses with proper HTTP status codes across all endpoints.

6. **Environment-Driven Configuration**:
   All infrastructure coordinates (database URL, ports, credentials) are loaded from external environment variables with zero hard-coded secrets.
