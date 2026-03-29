# Wallet Transaction UI (React)

Vite + React SPA for register, login, and a transaction dashboard (list with filters, create, edit, delete). JWT is stored in `localStorage` and sent as `Authorization: Bearer …`.

## Scripts

- `npm run dev` — dev server (proxies API to `http://localhost:8080`)
- `npm run build` — production bundle
- `npm run preview` — preview the production build

## Environment

- **`VITE_API_URL`**: Base URL of the Spring Boot API (empty string = same origin, or use the Vite proxy in dev).
