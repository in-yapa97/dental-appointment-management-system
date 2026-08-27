/**
 * Shared API Configuration
 * Supports VITE_API_BASE_URL or VITE_API_URL environment variable for production deployments,
 * falling back to empty string for local development with Vite dev proxy.
 */
export const API_BASE_URL: string =
  (import.meta.env.VITE_API_BASE_URL as string) ||
  (import.meta.env.VITE_API_URL as string) ||
  '';
