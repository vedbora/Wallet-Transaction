const API_BASE = import.meta.env.VITE_API_URL || '';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function persistAuth(authResponse) {
  localStorage.setItem(TOKEN_KEY, authResponse.accessToken);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      userId: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    }),
  );
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

async function parseError(res) {
  try {
    const body = await res.json();
    const parts = [];
    if (body?.message) parts.push(body.message);
    if (Array.isArray(body?.details) && body.details.length) {
      parts.push(...body.details);
    }
    if (parts.length) return parts.join(' — ');
  } catch {
    /* ignore */
  }
  return `Request failed (${res.status})`;
}

export async function api(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };
  const token = getStoredToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (res.status === 204) return null;
  if (!res.ok) throw new Error(await parseError(res));
  return res.json();
}
