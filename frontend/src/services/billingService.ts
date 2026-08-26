import {
  ApiErrorResponse,
  Bill,
  BillRequest,
  BillStatus,
  MessageResponse,
  PaymentStatusReportResponse,
  ReceiptResponse,
  RevenueReportResponse,
  TreatmentRevenueResponse,
} from '../types';
import { authService } from './authService';

const getAuthHeaders = (): HeadersInit => {
  const token = authService.getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
};

export interface BillFilters {
  patientId?: number;
  appointmentId?: number;
  status?: BillStatus;
  date?: string;
  billNumber?: string;
}

export const billingService = {
  async getBills(filters: BillFilters = {}): Promise<Bill[]> {
    const params = new URLSearchParams();
    if (filters.patientId) params.append('patientId', filters.patientId.toString());
    if (filters.appointmentId) params.append('appointmentId', filters.appointmentId.toString());
    if (filters.status) params.append('status', filters.status);
    if (filters.date) params.append('date', filters.date);
    if (filters.billNumber) params.append('billNumber', filters.billNumber);

    const queryString = params.toString();
    const url = queryString ? `/api/v1/bills?${queryString}` : '/api/v1/bills';

    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to fetch bills');
    }

    return response.json();
  },

  async getBillById(id: number): Promise<Bill> {
    const response = await fetch(`/api/v1/bills/${id}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to fetch bill details');
    }

    return response.json();
  },

  async getBillByAppointmentId(appointmentId: number): Promise<Bill> {
    const response = await fetch(`/api/v1/bills/appointment/${appointmentId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'No bill found for this appointment');
    }

    return response.json();
  },

  async createBill(data: BillRequest): Promise<Bill> {
    const response = await fetch('/api/v1/bills', {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to create bill');
    }

    return response.json();
  },

  async updateBill(id: number, data: BillRequest): Promise<Bill> {
    const response = await fetch(`/api/v1/bills/${id}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to update bill');
    }

    return response.json();
  },

  async deleteBill(id: number): Promise<MessageResponse> {
    const response = await fetch(`/api/v1/bills/${id}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to delete bill');
    }

    return response.json();
  },

  async getReceipt(id: number): Promise<ReceiptResponse> {
    const response = await fetch(`/api/v1/bills/${id}/receipt`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to retrieve receipt');
    }

    return response.json();
  },

  async getRevenueReport(from?: string, to?: string): Promise<RevenueReportResponse> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    const queryString = params.toString();
    const url = queryString ? `/api/v1/reports/revenue?${queryString}` : '/api/v1/reports/revenue';

    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to generate revenue report');
    }

    return response.json();
  },

  async getPaymentStatusReport(): Promise<PaymentStatusReportResponse> {
    const response = await fetch('/api/v1/reports/payment-status', {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to generate payment status report');
    }

    return response.json();
  },

  async getTreatmentRevenueReport(): Promise<TreatmentRevenueResponse[]> {
    const response = await fetch('/api/v1/reports/treatment-revenue', {
      method: 'GET',
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      const err: ApiErrorResponse = await response.json().catch(() => ({}));
      throw new Error(err.message || 'Failed to generate treatment revenue report');
    }

    return response.json();
  },
};
