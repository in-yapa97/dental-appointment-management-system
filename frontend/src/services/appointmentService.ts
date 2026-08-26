import {
  ApiErrorResponse,
  Appointment,
  AppointmentRequest,
  AppointmentStatus,
  AvailabilityResponse,
  DentistLookup,
  TreatmentLookup,
} from '../types';
import { authService } from './authService';

const getAuthHeaders = (): HeadersInit => {
  const token = authService.getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export interface AppointmentFilters {
  patientId?: number;
  dentistId?: number;
  date?: string;
  status?: AppointmentStatus;
}

export const appointmentService = {
  async getAppointments(filters: AppointmentFilters = {}): Promise<Appointment[]> {
    const params = new URLSearchParams();
    if (filters.patientId) params.append('patientId', filters.patientId.toString());
    if (filters.dentistId) params.append('dentistId', filters.dentistId.toString());
    if (filters.date) params.append('date', filters.date);
    if (filters.status) params.append('status', filters.status);

    const queryString = params.toString();
    const url = queryString ? `/api/v1/appointments?${queryString}` : '/api/v1/appointments';

    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to fetch appointments');
    }

    return response.json();
  },

  async getAppointment(id: number): Promise<Appointment> {
    const response = await fetch(`/api/v1/appointments/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || `Failed to fetch appointment #${id}`);
    }

    return response.json();
  },

  async createAppointment(data: AppointmentRequest): Promise<Appointment> {
    const response = await fetch('/api/v1/appointments', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      if (err.errors) {
        const fieldErrors = Object.values(err.errors).join(', ');
        throw new Error(fieldErrors || err.message || 'Appointment creation failed');
      }
      throw new Error(err.message || 'Appointment creation failed');
    }

    return response.json();
  },

  async updateAppointment(id: number, data: AppointmentRequest): Promise<Appointment> {
    const response = await fetch(`/api/v1/appointments/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      if (err.errors) {
        const fieldErrors = Object.values(err.errors).join(', ');
        throw new Error(fieldErrors || err.message || 'Appointment update failed');
      }
      throw new Error(err.message || 'Appointment update failed');
    }

    return response.json();
  },

  async deleteAppointment(id: number): Promise<void> {
    const response = await fetch(`/api/v1/appointments/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete appointment');
    }
  },

  async checkAvailability(dentistId: number, date: string, time: string): Promise<AvailabilityResponse> {
    const formattedTime = time.length === 5 ? `${time}:00` : time;
    const url = `/api/v1/appointments/availability?dentistId=${dentistId}&date=${date}&time=${encodeURIComponent(formattedTime)}`;

    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to check dentist availability');
    }

    return response.json();
  },

  async getActiveDentists(): Promise<DentistLookup[]> {
    const response = await fetch('/api/v1/appointments/dentists', {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      return [];
    }

    return response.json();
  },

  async getActiveTreatments(): Promise<TreatmentLookup[]> {
    const response = await fetch('/api/v1/appointments/treatments', {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      return [];
    }

    return response.json();
  },
};
