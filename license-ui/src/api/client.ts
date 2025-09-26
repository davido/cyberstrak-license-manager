import axios from "axios";

const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
export const TOKEN_STORAGE_KEY = "authToken";

export const api = axios.create({ baseURL });

// Token aus localStorage anhängen
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Bei 401 → zurück zum Login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err?.response?.status === 401) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
      // harte Navigation vermeidet Hook-Kontext:
      window.location.assign("/login");
    }
    return Promise.reject(err);
  }
);

