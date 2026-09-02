import { ApiErrorResponse, Dentist, DentistRequest } from '../types';
import { authService } from './authService';
import { API_BASE_URL } from './apiConfig';

const getAuthHeaders = (): HeadersInit => {
  const token = authService.getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export const dentistService = {
  async getDentists(): Promise<Dentist[]> {
    const response = await fetch(`${API_BASE_URL}/api/v1/dentists`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to fetch dentists list');
    }

    return response.json();
  },

  async getDentist(id: number): Promise<Dentist> {
    const response = await fetch(`${API_BASE_URL}/api/v1/dentists/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || `Failed to fetch dentist #${id}`);
    }

    return response.json();
  },

  async searchDentists(keyword: string): Promise<Dentist[]> {
    const url = keyword.trim()
      ? `${API_BASE_URL}/api/v1/dentists/search?keyword=${encodeURIComponent(keyword.trim())}`
      : `${API_BASE_URL}/api/v1/dentists`;

    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to search dentists');
    }

    return response.json();
  },

  async createDentist(data: DentistRequest): Promise<Dentist> {
    const response = await fetch(`${API_BASE_URL}/api/v1/dentists`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      if (err.errors) {
        const fieldErrors = Object.values(err.errors).join(', ');
        throw new Error(fieldErrors || err.message || 'Dentist creation failed');
      }
      throw new Error(err.message || 'Dentist creation failed');
    }

    return response.json();
  },

  async updateDentist(id: number, data: DentistRequest): Promise<Dentist> {
    const response = await fetch(`${API_BASE_URL}/api/v1/dentists/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      if (err.errors) {
        const fieldErrors = Object.values(err.errors).join(', ');
        throw new Error(fieldErrors || err.message || 'Dentist update failed');
      }
      throw new Error(err.message || 'Dentist update failed');
    }

    return response.json();
  },

  async deleteDentist(id: number): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/api/v1/dentists/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete dentist');
    }
  },
};
