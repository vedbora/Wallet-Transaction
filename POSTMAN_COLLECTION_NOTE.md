# Postman / manual testing

The API has changed: use **Swagger UI** at `http://localhost:8080/swagger-ui.html` or see the **API endpoints** table in the project `README.md`.

Flow:

1. `POST /auth/register` or `POST /auth/login` — copy `accessToken`.
2. In Swagger, **Authorize** → enter `Bearer <accessToken>` (or paste token only; UI may add `Bearer`).
3. Call `/transactions` endpoints.

Default admin (if seeded): `admin@example.com` / `Admin@123`.
