/**
 * Core domain types and system contracts for Dental Management System.
 */

export type UserRole = 'ADMIN' | 'DENTIST' | 'RECEPTIONIST' | 'STAFF';

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: UserRole;
  active: boolean;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  user: User;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  errors?: Record<string, string>;
}

export interface SystemHealth {
  status: 'UP' | 'DOWN' | string;
  service: string;
  timestamp: string;
  version: string;
}
