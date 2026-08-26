import { SystemHealth } from '../types';

/**
 * Service to query system health and backend connectivity status.
 */
export const getSystemHealth = async (): Promise<SystemHealth> => {
  const response = await fetch('/api/v1/health');
  if (!response.ok) {
    throw new Error(`Health check failed with status: ${response.status}`);
  }
  return response.json();
};
