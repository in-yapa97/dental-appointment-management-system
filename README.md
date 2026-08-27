# Dental Appointment and Patient Management System

A full-stack, enterprise-ready clinic management system designed to manage dental clinic workflows, patient health records, appointment scheduling with dentist availability checking, billing, receipts, financial reporting, and an executive clinic dashboard.

> [!NOTE]
> **Project Status: Complete — Milestones 0–6 Completed**  
> All core architectural tiers, domain entities, REST APIs, security controls, business validation rules, automated tests, and frontend user interfaces have been fully implemented, integrated, and verified against PostgreSQL 17.

---

## Key Features

- **Stateless JWT Authentication**: Secure user login, registration, role-based access control (`STAFF`, `ADMIN`), and BCrypt (12-round) password hashing.
- **Executive Clinic Dashboard**: Real-time KPI summary cards (Registered Patients, Active Appointments, Revenue Collected, Pending Invoices), quick-action buttons, upcoming appointments calendar preview, and recent billing activity.
- **Patient Management**: Full CRUD operations, unique patient number generation (`PAT-XXXX`), case-insensitive multi-field search (by name, patient number, phone, email), and relational deletion protection.
- **Appointment Management**: Complete appointment booking, status lifecycle tracking (`SCHEDULED`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `NO_SHOW`), and server-assigned appointment numbers (`APT-XXXXXXXX`).
- **Dentist Availability & Conflict Prevention**: Live slot conflict checking (`/api/v1/appointments/availability`), double-booking prevention (HTTP 409 Conflict), active dentist verification, and automatic time-slot release upon cancellation.
- **Billing & Invoicing**: Strict 1:1 appointment-to-bill association, duplicate invoice rejection, accurate `BigDecimal` fee calculation ($\text{total} = \text{consultation} + \text{treatment}$), and payment status lifecycle management (`PENDING`, `PAID`, `CANCELLED`, `REFUNDED`).
- **Official Receipts & Printing**: Structured official receipt generation (`/api/v1/bills/{id}/receipt`) with clinic branding, patient details, procedure breakdown, status stamp, and print-optimized stylesheet (`@media print`).
- **Financial & Operational Reporting**:
  - **Revenue Summary**: Total collected revenue, paid invoices, pending receivables, and overall invoice counts.
  - **Payment Status Breakdown**: Grouped bill counts and amounts with visual percentage distribution bars.
  - **Treatment Profitability**: Clinical procedure volume and revenue rankings.
  - **Date Range Filters**: Custom date filtering (`from` and `to` parameters) with all-time aggregation fallback.
- **Robust Exception Handling & Validation**: Centralized `@RestControllerAdvice` mapping validation constraints, conflict states, and business errors to clean, human-friendly JSON error responses.

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.3.4
- **Language**: Java 17 LTS
- **Data Persistence**: Spring Data JPA / Hibernate ORM 6.5.3
- **Security**: Spring Security 6, JJWT 0.12.6 (HMAC-SHA256)
- **Validation**: Jakarta Bean Validation / Hibernate Validator
- **Build Tool**: Apache Maven (via included Maven Wrapper `mvnw`)

### Frontend
- **Framework**: React 18.3.1
- **Language**: TypeScript 5.6.3
- **Build Tool**: Vite 5.4.10
- **Styling**: Vanilla CSS with custom design system tokens and responsive layouts

### Database
- **Primary Database**: PostgreSQL 17 (production & local runtime on port 5432)
- **Test Database**: H2 in-memory database (for isolated unit & repository slice testing)

### Testing & Quality Assurance
- **Testing Frameworks**: JUnit 5, Mockito 5, Spring Boot Test, MockMvc, AssertJ
- **Automated Test Suite**: 159 tests (100% pass rate, 0 failures, 0 errors, 0 skipped)

---

## System Architecture

The system is designed according to a strict 4-tier layered architecture ensuring separation of concerns, transactional integrity, and maintainability:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                   React 18 Frontend (Vite + TypeScript)                  │
│        Dashboard  •  Patients  •  Appointments  •  Billing  •  Reports   │
└────────────────────────────────────┬─────────────────────────────────────┘
                                     │  HTTPS / REST + JWT Bearer Token
                                     ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                      Spring Boot 3.3 REST API Layer                      │
│                                                                          │
│  [REST Controllers]         Thin controllers, route mapping & DTOs       │
│           │                 - Jakarta Bean Validation (@Valid)           │
│           │                 - GlobalExceptionHandler (@RestControllerAdvice)│
│           ▼                                                              │
│  [Service Layer]            Transactional business logic & rules         │
│           │                 - Fee calculations & availability checks     │
│           │                 - Safe deletion & duplicate protections      │
│           ▼                                                              │
│  [Repository Layer]         Spring Data JPA & JpaSpecificationExecutor   │
│           │                 - Dynamic Specifications & JPQL Aggregations │
│           ▼                                                              │
│  [PostgreSQL 17]            Relational persistence with foreign keys,     │
│                             unique constraints, and check constraints    │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## Domain Entities

```
  ┌──────────────┐          ┌─────────────────┐
  │     User     │          │     Dentist     │
  └──────────────┘          └────────┬────────┘
                                     │ 1
                                     │
  ┌──────────────┐          ┌────────┴────────┐          ┌─────────────────┐
  │   Patient    ├──────────┤   Appointment   ├──────────┤    Treatment    │
  └──────────────┘ 1      * └────────┬────────┘ *      1 └─────────────────┘
                                     │ 1
                                     │
                                     │ 1
                            ┌────────┴────────┐
                            │      Bill       │
                            └─────────────────┘
```

1. **User (`users`)**: Clinic staff and administrator accounts with BCrypt-hashed credentials and assigned roles (`STAFF`, `ADMIN`).
2. **Patient (`patients`)**: Patient demographics, unique medical record identifier (`patient_number`), contact information, and address.
3. **Dentist (`dentists`)**: Licensed dental practitioners, unique identifier (`dentist_number`), specialization, contact info, and active practicing status.
4. **Treatment (`treatments`)**: Dental procedures, services, and checkups with unique code (`treatment_code`), standard fee (`cost`), and active status.
5. **Appointment (`appointments`)**: Scheduled clinical visits linking Patient, Dentist, and Treatment, including date, time, status, and clinical notes.
6. **Bill (`bills`)**: Financial billing record in a strict 1:1 relationship with an Appointment, containing consultation fee, treatment amount, total amount, bill date, and payment status.

---

## REST API Reference

All protected endpoints require the header `Authorization: Bearer <JWT_TOKEN>`.

### Health Check
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/health` | Service liveness and health metadata | No |

### Authentication
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/auth/register` | Register a new clinic staff/admin user | No |
| `POST` | `/api/v1/auth/login` | Authenticate with credentials and receive JWT | No |
| `POST` | `/api/v1/auth/logout` | End session and invalidate client token | No |
| `GET` | `/api/v1/auth/me` | Retrieve authenticated user profile | **Yes** |

### Patients
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/patients` | List all registered patients | **Yes** |
| `POST` | `/api/v1/patients` | Create a new patient record | **Yes** |
| `GET` | `/api/v1/patients/{id}` | Retrieve patient details by ID | **Yes** |
| `PUT` | `/api/v1/patients/{id}` | Update existing patient record | **Yes** |
| `DELETE` | `/api/v1/patients/{id}` | Delete patient (blocked if appointments exist) | **Yes** |
| `GET` | `/api/v1/patients/search` | Search patients by name, number, phone, email | **Yes** |

### Appointments & Availability
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/appointments` | Filter appointments (by patient, dentist, date, status) | **Yes** |
| `POST` | `/api/v1/appointments` | Book an appointment with availability check | **Yes** |
| `GET` | `/api/v1/appointments/{id}` | Retrieve appointment by ID | **Yes** |
| `PUT` | `/api/v1/appointments/{id}` | Update appointment date, time, status, or notes | **Yes** |
| `DELETE` | `/api/v1/appointments/{id}` | Delete appointment (blocked if bill exists) | **Yes** |
| `GET` | `/api/v1/appointments/availability` | Check dentist availability for a specific slot | **Yes** |
| `GET` | `/api/v1/appointments/dentists` | Lookup active dentists for booking dropdowns | **Yes** |
| `GET` | `/api/v1/appointments/treatments` | Lookup active treatments and costs | **Yes** |

### Billing, Invoicing & Receipts
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `POST` | `/api/v1/bills` | Create invoice for an appointment (1:1 constraint) | **Yes** |
| `GET` | `/api/v1/bills` | Search and filter bills (status, date, bill number) | **Yes** |
| `GET` | `/api/v1/bills/{id}` | Retrieve bill by ID | **Yes** |
| `GET` | `/api/v1/bills/appointment/{appointmentId}` | Retrieve bill for a specific appointment | **Yes** |
| `PUT` | `/api/v1/bills/{id}` | Update bill fees or payment status | **Yes** |
| `DELETE` | `/api/v1/bills/{id}` | Safe delete bill (blocked if status is `PAID`) | **Yes** |
| `GET` | `/api/v1/bills/{id}/receipt` | Generate structured official receipt | **Yes** |

### Financial & Clinical Reports
| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `GET` | `/api/v1/reports/revenue` | Revenue summary (optional `from` & `to` dates) | **Yes** |
| `GET` | `/api/v1/reports/payment-status` | Payment status volume and amount breakdown | **Yes** |
| `GET` | `/api/v1/reports/treatment-revenue` | Treatment procedure revenue rankings | **Yes** |

---

## Authentication & Security

- **Stateless Token Verification**: JJWT 0.12.6 with HMAC-SHA256 token signing and configurable expiration (`JWT_EXPIRATION_MS`).
- **Password Protection**: BCrypt one-way hashing with 12 rounds; plain text passwords are never stored or logged.
- **Zero-Secret Codebase**: Application coordinates and secrets are strictly sourced from environment variables; `backend/.env` is ignored by Git.
- **Centralized Security Filter**: `JwtAuthenticationFilter` intercepts requests, validates Bearer tokens, loads `UserDetails`, and populates Spring Security's `SecurityContextHolder`.
- **Granular Exception Shielding**: Database exceptions, duplicate key violations, and invalid queries are captured by `GlobalExceptionHandler`, returning clean client responses and preventing internal stack trace leaks.

---

## Database Configuration & Relational Integrity

- **Database**: PostgreSQL 17
- **Connection Pool**: HikariCP with non-blocking initialization (`initialization-fail-timeout = -1`)
- **Relational Integrity Enforcements**:
  - **Unique Constraints**: `users.username`, `patients.patient_number`, `dentists.dentist_number`, `treatments.treatment_code`, `appointments.appointment_number`, `bills.bill_number`, and `bills.appointment_id`.
  - **One-to-One Invoicing**: `bills.appointment_id` is constrained with a unique index, ensuring an appointment can only have one billing record.
  - **Audit Deletion Protection**: Bills with status `PAID` cannot be deleted via the API. Appointments with associated bills cannot be deleted until the bill is handled. Patients with existing appointments cannot be removed.

---

## Frontend Pages & Views

| View / Page | Component | Key Capabilities |
| :--- | :--- | :--- |
| **Login** | `LoginPage.tsx` | Staff sign-in, input validation, JWT token storage, error alerts |
| **Register** | `RegisterPage.tsx` | New clinic user account registration with field validation |
| **Dashboard** | `DashboardPage.tsx` | Live KPI cards (Patients, Appointments, Revenue, Pending), quick actions, recent activity |
| **Patients** | `PatientsPage.tsx` | Patient directory table, multi-field search, create/edit modal, delete protection |
| **Appointments** | `AppointmentsPage.tsx` | Appointment calendar table, booking modal with live availability checker, status transitions |
| **Billing** | `BillingPage.tsx` | Invoices table, fee calculation, quick payment status updater, receipt viewer, safe deletion |
| **Reports** | `ReportsPage.tsx` | Revenue KPI summary, date range picker, payment status distribution bars, procedure ranking table |
| **Profile** | `UserProfilePage.tsx` | Authenticated user metadata, role badge, session logout |
| **System Status** | `App.tsx` (Health) | Real-time backend connectivity check (`/api/v1/health`) and architecture overview |

---

## Milestone Completion Summary

| Milestone | Scope / Domain | Status |
| :--- | :--- | :---: |
| **Milestone 0** | Project Foundation, Architecture, Spring Boot 3 & Vite Skeleton, Health Check | **COMPLETE** |
| **Milestone 1** | Database Schema, 6 JPA Entities, Repositories, Constraints, PostgreSQL 17 | **COMPLETE** |
| **Milestone 2** | Authentication, Spring Security 6, Stateless JWT, BCrypt, AuthController, Profile UI | **COMPLETE** |
| **Milestone 3** | Patient Management CRUD, Multi-field Search, Unique Patient Numbers, PatientsPage UI | **COMPLETE** |
| **Milestone 4** | Appointment Management, Dentist Availability Checker, Double Booking Prevention | **COMPLETE** |
| **Milestone 5** | Billing & Invoicing (1:1), Fee Calculation, Receipts, Financial Reports, ReportsPage UI | **COMPLETE** |
| **Milestone 6** | Final Integration, Executive Clinic Dashboard, UI/UX Polish, Full E2E Verification | **COMPLETE** |

---

## Verification & Quality Assurance

- **Automated Test Results**:
  ```text
  [INFO] Results:
  [INFO] 
  [INFO] Tests run: 159, Failures: 0, Errors: 0, Skipped: 0
  [INFO] 
  [INFO] ------------------------------------------------------------------------
  [INFO] BUILD SUCCESS
  [INFO] ------------------------------------------------------------------------
  ```
- **Frontend Production Build**: `tsc -b && vite build` compiled in **1.83s** with **0 TypeScript errors**.
- **Backend Production Package**: Executable JAR built successfully (`dental-management-backend-0.0.1-SNAPSHOT.jar`).
- **PostgreSQL 17 End-to-End Walkthrough**: **26 / 26 scenarios passed** (covering authentication, patient CRUD, appointment booking, dentist availability conflict rejection, billing calculations, payment status updates, receipt generation, financial reports, safe deletion, and unauthenticated access rejection).
- **Security Audit**: Zero hard-coded credentials, `backend/.env` gitignored, passwords hashed with BCrypt.

---

## Setup & Running the Application

### Prerequisites
- **Node.js**: v18+ or v20+ LTS and npm
- **Java**: JDK 17 LTS or higher
- **PostgreSQL**: v14+ (v17 recommended)
- **Git**

---

### 1. Environment Configuration

1. Copy the environment template into `backend/.env`:
   ```bash
   cp backend/.env.example backend/.env
   ```

2. Configure your local PostgreSQL credentials and JWT secret in `backend/.env`:
   ```properties
   SERVER_PORT=8080
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=dental_db
   DB_USERNAME=postgres
   DB_PASSWORD=your_postgres_password
   JPA_DDL_AUTO=update
   JWT_SECRET=your_base64_encoded_256bit_jwt_secret_here
   JWT_EXPIRATION_MS=86400000
   ```

---

### 2. Running the Backend

**On Windows (PowerShell):**
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**On Linux / macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

- Backend API: `http://localhost:8080`
- Health Check: `http://localhost:8080/api/v1/health`

To run backend tests:
```powershell
.\mvnw.cmd test
```

To build the executable JAR:
```powershell
.\mvnw.cmd clean package -DskipTests
```

---

### 3. Running the Frontend

Navigate to `frontend/`, install dependencies, and start the development server:

```bash
cd frontend
npm install
npm run dev
```

- Frontend Application: `http://localhost:3000` (or the port displayed in terminal)

To create a production build:
```bash
npm run build
```

---

## Project Structure

```
dental-appointment-management-system/
├── backend/                              # Spring Boot 3 Backend
│   ├── src/
│   │   ├── main/java/com/dental/management/
│   │   │   ├── config/                   # Security & CORS configuration
│   │   │   ├── controller/               # REST Controllers (Auth, Patient, Appt, Bill, Report, Health)
│   │   │   ├── dto/                      # Request & Response DTOs
│   │   │   ├── entity/                   # JPA Domain Entities & Enums
│   │   │   ├── exception/                # Domain Exceptions & GlobalExceptionHandler
│   │   │   ├── repository/               # Spring Data JPA Repositories
│   │   │   ├── security/                 # JWT Utility, Filter & UserDetailsService
│   │   │   ├── service/                  # Business Service Interfaces
│   │   │   │   └── impl/                 # Service Implementations
│   │   │   └── DentalManagementApplication.java
│   │   ├── main/resources/
│   │   │   └── application.properties    # Environment-driven properties
│   │   └── test/                         # 159 Unit, Slice, Repository & Security Tests
│   ├── pom.xml                           # Maven dependencies
│   ├── .env.example                      # Template for backend environment variables
│   └── mvnw / mvnw.cmd                   # Cross-platform Maven Wrapper
├── frontend/                             # React 18 + TypeScript + Vite Frontend
│   ├── src/
│   │   ├── components/                   # Navbar and shared components
│   │   ├── pages/                        # Dashboard, Patients, Appointments, Billing, Reports, Auth
│   │   ├── services/                     # Typed API clients with JWT injection
│   │   ├── types/                        # TypeScript interfaces and domain types
│   │   ├── App.tsx                       # Root component, routing & session guard
│   │   ├── App.css                       # Design tokens, responsive styles & print stylesheet
│   │   └── main.tsx                      # Entry point
│   ├── package.json                      # Frontend dependencies
│   └── vite.config.ts                    # Vite build & dev proxy configuration
├── docs/                                 # Architectural specifications
├── .gitignore
└── README.md
```

---

## Future Enhancements (Optional / Roadmap)

The following items are optional future enhancements beyond the current M0–M6 scope:
- **End-to-End Browser Automation**: Adding Cypress or Playwright test suites into the CI/CD pipeline.
- **Exporting Reports**: Adding one-click export to CSV and downloadable PDF for treatment profitability reports.
- **SMS / Email Notifications**: Automated reminder dispatch for upcoming appointments.

---

## Academic Assessment & License

This project was developed as a university assessment project demonstrating full-stack enterprise web development, clean layered architecture, relational database modeling, stateless security, and modern UI engineering.
