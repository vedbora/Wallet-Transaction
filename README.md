# Wallet Transaction System

Spring Boot REST API for **transactions** with **JWT authentication**, **role-based access (USER / ADMIN)**, validation, global error handling, **Springdoc OpenAPI (Swagger)**, and a **React (Vite)** SPA. Wallet balance and Kafka were removed in favor of a focused transaction CRUD model.

## Features

- **Authentication**: `POST /auth/register`, `POST /auth/login`; passwords hashed with **BCrypt**; **JWT** access tokens (**1 hour** expiry); stateless **JWT filter** on every request except `/auth/**` and Swagger.
- **Authorization**: `USER` sees and manages **only their** transactions; `ADMIN` sees **all** transactions and can update/delete any row. Enforced with `@PreAuthorize` on controllers and checks in the service layer.
- **Transactions**: CRUD on `/transactions` with fields `id`, `amount`, `type` (`CREDIT` | `DEBIT`), `description`, `userId`, `createdAt`.
- **Stand-out feature**: **Filtering** on `GET /transactions` by optional query params `type`, `from`, `to` (ISO-8601 datetimes, UTC).
- **Validation & errors**: Bean validation on DTOs; consistent JSON errors via `GlobalExceptionHandler` and security entry points.
- **API docs**: Swagger UI at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (use **Authorize** with `Bearer <token>` for protected routes; registration/login operations are documented without requiring a token).

## Prerequisites

- **Java 17+**
- **Maven 3.9+**
- **Node.js 18+** (for the React UI)

## Backend setup

```bash
cd /path/to/wallet-transaction-system-main
mvn spring-boot:run
```

Default profile uses **in-memory H2** on port **8080**.

### Configuration

| Property | Description |
|----------|-------------|
| `JWT_SECRET` | UTF-8 secret for HS256; **must be at least 32 characters** in production. |
| `app.jwt.expiration-ms` | Token lifetime (default `3600000` = 1 hour). |
| `PORT` | HTTP port (default `8080`). |

Example:

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
mvn spring-boot:run
```

### MySQL (optional)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Adjust `src/main/resources/application-mysql.yml` for your database. Use `ddl-auto: update` for first-time schema creation if needed.

### Default admin user

On startup (non-`test` profile), if no user exists with email `admin@example.com`, one is created:

- **Email**: `admin@example.com`
- **Password**: `Admin@123`
- **Role**: `ADMIN`

Change this password immediately in any shared or production environment.

## Frontend setup

```bash
cd frontend
npm install
npm run dev
```

The dev server runs at **http://localhost:5173** and **proxies** `/auth` and `/transactions` to `http://localhost:8080`.

For a **production build** against a remote API:

```bash
echo 'VITE_API_URL=https://your-api.example.com' > .env.production
npm run build
```

Serve the `frontend/dist` folder with any static host. The app stores the JWT in **localStorage** and sends `Authorization: Bearer <token>` on API calls.

## API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | No | Register as `USER`; returns JWT + profile. |
| POST | `/auth/login` | No | Login; returns JWT + profile. |
| POST | `/transactions` | JWT | Create a transaction for the **authenticated** user. |
| GET | `/transactions` | JWT | List transactions (`USER`: own; `ADMIN`: all). Query: `type`, `from`, `to`. |
| PUT | `/transactions/{id}` | JWT | Full update (`USER`: own; `ADMIN`: any). |
| DELETE | `/transactions/{id}` | JWT | Delete (`USER`: own; `ADMIN`: any). |

OpenAPI JSON: `GET /v3/api-docs`.

## Project layout (backend)

```
src/main/java/com/example/wallet/
  config/          # Security, JWT properties, OpenAPI, admin seed
  controller/      # Auth, Transactions
  dto/             # Request/response DTOs
  exception/       # Exceptions + GlobalExceptionHandler
  model/           # JPA entities + enums
  repository/      # Spring Data JPA (+ specifications for filters)
  security/        # JWT service, filter, user details
  service/         # Auth, user, transaction logic
```

## Scalability notes

- **Microservices**: Split **auth** (identity, token issuance) and **transaction** (read/write with its own DB) behind an API gateway; propagate identity via signed JWT claims or opaque tokens introspected at the gateway.
- **Caching**: Cache **read-heavy** transaction lists per user (e.g. Redis) with short TTLs and explicit invalidation on writes; never cache without a clear keying and eviction strategy.
- **Load balancing**: Run **multiple stateless** API instances behind a load balancer; use **sticky sessions only if unavoidable**—JWT keeps the API stateless. Prefer **database connection pools** sized per instance and **read replicas** for reporting-style queries if volume grows.

## Legacy note

Older Postman collections or docs that referenced `/api/users`, wallet credit/debit, or Kafka are **obsolete**; use Swagger or the table above.
