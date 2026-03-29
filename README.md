# 🚀 Wallet Transaction System (Full Stack)

A scalable full-stack application with secure JWT authentication, role-based access control, and transaction management.

> ⚡ Fully deployed full-stack application with real-time API interaction and secure authentication.

---

## 🔥 Live Demo

* 🌐 Frontend: https://transaction-wallet.web.app/login
* ⚙️ Backend API: https://wallet-transaction-ttoj.onrender.com

---

## 🔗 API Documentation

Interactive API documentation is available via Swagger UI:

* Swagger UI: https://wallet-transaction-ttoj.onrender.com/swagger-ui.html
* OpenAPI Spec: https://wallet-transaction-ttoj.onrender.com/v3/api-docs

👉 You can directly test all APIs from Swagger.

For protected routes:

1. Login using `/auth/login`
2. Copy the JWT token
3. Click **Authorize**
4. Enter: `Bearer <your-jwt-token>`

---

## 📌 Features

### 🔐 Authentication

* `POST /auth/register`, `POST /auth/login`
* Passwords hashed with BCrypt
* JWT authentication (1 hour expiry)
* Stateless JWT filter

### 🔐 Authorization

* USER: Access only own transactions
* ADMIN: Access all transactions
* Secured using `@PreAuthorize` and service-level checks

### 💼 Transactions

* CRUD operations on `/transactions`
* Fields: `id`, `amount`, `type (CREDIT/DEBIT)`, `description`, `userId`, `createdAt`
* Filtering support using query params:

  * `type`
  * `from`
  * `to` (ISO-8601 datetime)

### 🛡️ Security & Validation

* Bean validation on DTOs
* Global exception handling
* Consistent error responses

---

## 🧱 Tech Stack

### Backend

* Java 17 + Spring Boot
* Spring Security + JWT
* Hibernate / JPA

### Frontend

* React.js (Vite)

### Database

* H2 (default) / MySQL (optional)

---

## ⚙️ Backend Setup

```bash
mvn spring-boot:run
```

Default runs on **http://localhost:8080**

---

## ⚙️ Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Runs on **http://localhost:5173**

---

## 🔗 API Endpoints

| Method | Path                 | Auth | Description        |
| ------ | -------------------- | ---- | ------------------ |
| POST   | `/auth/register`     | No   | Register user      |
| POST   | `/auth/login`        | No   | Login and get JWT  |
| POST   | `/transactions`      | JWT  | Create transaction |
| GET    | `/transactions`      | JWT  | Get transactions   |
| PUT    | `/transactions/{id}` | JWT  | Update transaction |
| DELETE | `/transactions/{id}` | JWT  | Delete transaction |

---

## 📂 Project Structure

```bash
backend/
 ├── controller/
 ├── service/
 ├── repository/
 ├── dto/
 ├── security/
 ├── exception/
 ├── config/

frontend/
 ├── pages/
 ├── components/
 ├── services/
```

---

## 🧠 Scalability Approach

* Stateless authentication using JWT
* Modular architecture for scalability
* Microservices-ready design
* Redis caching can be added
* Load balancing supported

---

## 🚀 Future Improvements

* Refresh Token implementation
* Rate limiting
* Docker deployment
* CI/CD pipeline

---

## 👨‍💻 Author

**Vedant Bora**
