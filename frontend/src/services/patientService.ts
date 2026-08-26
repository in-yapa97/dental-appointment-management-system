import { ApiErrorResponse, Patient, PatientRequest } from '../types';
import { authService } from './authService';

const getAuthHeaders = (): HeadersInit => {
  const token = authService.getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export const patientService = {
  async getPatients(): Promise<Patient[]> {
    const response = await fetch('/api/v1/patients', {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to fetch patients list');
    }

    return response.json();
  },

  async getPatient(id: number): Promise<Patient> {
    const response = await fetch(`/api/v1/patients/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || `Failed to fetch patient #${id}`);
    }

    return response.json();
  },

  async searchPatients(keyword: string): Promise<Patient[]> {
    const url = keyword.trim()
      ? `/api/v1/patients/search?keyword=${encodeURIComponent(keyword.trim())}`
      : '/api/v1/patients';

    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to search patients');
    }

    return response.json();
  },

  async createPatient(data: PatientRequest): Promise<Patient> {
    const response = await fetch('/api/v1/patients', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      if (err.errors) {
        const fieldErrors = Object.values(err.errors).join(', ');
        throw new Error(fieldErrors || err.message || 'Patient creation failed');
      }
      throw new Error(err.message || 'Patient creation failed');
    }

    return response.json();
  },

  async updatePatient(id: number, data: PatientRequest): Promise<Patient> {
    const response = await fetch(`/api/v1/patients/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      if (err.errors) {
        const fieldErrors = Object.values(err.errors).join(', ');
        throw new Error(fieldErrors || err.message || 'Patient update failed');
      }
      throw new Error(err.message || 'Patient update failed');
    }

    return response.json();
  },

  async deletePatient(id: number): Promise<void> {
    const response = await fetch(`/api/v1/patients/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete patient');
    }
  },
};
