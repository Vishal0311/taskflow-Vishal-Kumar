# TaskFlow — Task Management API

A REST API built with Spring Boot for managing projects and tasks with JWT authentication.

---

## 1. Overview

TaskFlow is a task management system that allows users to register, log in, create projects, add tasks, and assign tasks to team members.

**Tech Stack:**
- Java 21 + Spring Boot 3.5
- Spring Security + JWT (JJWT 0.12.3)
- Spring Data JPA + Hibernate
- PostgreSQL 16
- Flyway (database migrations)
- Docker + Docker Compose
- Maven

---

## 2. Architecture Decisions

- **Layered architecture** — Controller → Service → Repository. Each layer has one responsibility.
- **JWT stateless auth** — No sessions. Every request carries a signed token. Expiry is 24 hours.
- **Flyway over Hibernate DDL** — Migrations are versioned SQL files. Schema changes are tracked and reproducible.
- **DTOs everywhere** — Entities never leave the service layer. Request/Response DTOs decouple the API contract from the database schema.
- **BCrypt cost 12** — Meets the assignment requirement. Higher cost = slower brute force.
- **EAGER fetch for relations** — Chosen to avoid LazyInitializationException in this stateless context. In a larger system, LAZY + @Transactional would be preferred.
- **Pagination on list endpoints** — All list endpoints support `?page=0&limit=10` query params.
- **Intentionally left out** — Rate limiting, refresh tokens, email verification. These would be added in production.

---

## 3. Running Locally

### Prerequisites
- Docker Desktop installed and running

### Steps

```bash
git clone https://github.com/your-username/taskflow
cd taskflow
cp .env.example .env
docker compose up --build
```

App available at: `http://localhost:8080`

Migrations run automatically on startup. Seed data is included.

---

## 4. Running Migrations

Migrations run **automatically** on container start via Flyway.

Migration files are in `backend/src/main/resources/db/migration/`:

| File | Description |
|---|---|
| V1__create_users_table.sql | Users table |
| V2__create_projects_table.sql | Projects table |
| V3__create_tasks_table.sql | Tasks table with enums |
| V4__seed_data.sql | Seed user, project, and tasks |

---

## 5. Test Credentials

A seed user is created automatically:
Email:    test@example.com
Password: password123

---

## 6. API Reference

### Base URL
http://localhost:8080

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | No | Register new user |
| POST | `/auth/login` | No | Login, returns JWT |

### Projects

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/projects` | Yes | List accessible projects |
| POST | `/projects` | Yes | Create project |
| GET | `/projects/{id}` | Yes | Get project + tasks |
| PATCH | `/projects/{id}` | Yes | Update project (owner only) |
| DELETE | `/projects/{id}` | Yes | Delete project (owner only) |
| GET | `/projects/{id}/stats` | Yes | Task counts by status/assignee |

### Tasks

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/projects/{id}/tasks` | Yes | List tasks (filter by status/assignee) |
| POST | `/projects/{id}/tasks` | Yes | Create task |
| PATCH | `/tasks/{id}` | Yes | Update task |
| DELETE | `/tasks/{id}` | Yes | Delete task |

### Pagination

All list endpoints support:
?page=0&limit=10

### Auth Header
Authorization: Bearer <token>

### Example — Register
POST /auth/register
{
"name": "John Doe",
"email": "john@example.com",
"password": "password123"
}

Response `201`:
{
"token": "eyJhbGci...",
"userId": "uuid",
"name": "John Doe",
"email": "john@example.com"
}

---

## 7. What I'd Do With More Time

- **Refresh tokens** — Current JWT has 24hr expiry with no refresh mechanism
- **Pagination on project detail** — `GET /projects/{id}` returns all tasks without pagination
- **Role-based access** — Currently only owner-based checks, no team roles
- **Rate limiting** — No protection against brute force on `/auth/login`
- **More integration tests** — Only 3 written, full coverage would include project and task endpoints
- **Soft deletes** — Currently hard deletes, no audit trail
- **Input sanitization** — Basic validation exists but no XSS protection
- **Docker health checks on backend** — Currently only PostgreSQL has a health check

---

## Postman Collection

Import `postman/TaskFlow.postman_collection.json` into Postman to test all endpoints.
