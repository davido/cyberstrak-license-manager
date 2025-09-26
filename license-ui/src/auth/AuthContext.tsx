import React, { createContext, useContext, useMemo, useState } from "react";
import axios from "axios";
import { TOKEN_STORAGE_KEY } from "../api/client";
import { parseJwt } from "./jwt";
import type { JwtPayload } from "./jwt";

type AuthContextType = {
  token: string | null;
  user: JwtPayload | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem(TOKEN_STORAGE_KEY));
  const [user, setUser] = useState<JwtPayload | null>(() =>
    token ? parseJwt(token) : null
  );

  const login = async (username: string, password: string) => {
    const baseURL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";
    const res = await axios.post(`${baseURL}/auth/login`, { username, password });
    const t = res.data?.token as string;
    if (!t) throw new Error("Kein Token erhalten");
    localStorage.setItem(TOKEN_STORAGE_KEY, t);
    setToken(t);
    setUser(parseJwt(t));
  };

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
  };

  const value = useMemo(() => ({ token, user, login, logout }), [token, user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};

