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

export interface MessageResponse {
  message: string;
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

export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface Patient {
  id: number;
  patientNumber: string;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  phone: string;
  email?: string | null;
  address?: string | null;
  createdAt: string;
}

export interface PatientRequest {
  patientNumber: string;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  phone: string;
  email?: string;
  address?: string;
}

export type AppointmentStatus =
  | 'SCHEDULED'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'NO_SHOW';

export interface Appointment {
  id: number;
  appointmentNumber: string;
  patientId: number;
  patientName: string;
  patientNumber: string;
  dentistId: number;
  dentistName: string;
  dentistNumber: string;
  treatmentId: number;
  treatmentName: string;
  treatmentCode: string;
  treatmentCost: number;
  appointmentDate: string;
  appointmentTime: string;
  status: AppointmentStatus;
  notes?: string | null;
  createdAt: string;
}

export interface AppointmentRequest {
  patientId: number;
  dentistId: number;
  treatmentId: number;
  appointmentDate: string;
  appointmentTime: string;
  status?: AppointmentStatus;
  notes?: string;
}

export interface AvailabilityResponse {
  available: boolean;
  reason?: string;
}

export interface DentistLookup {
  id: number;
  dentistNumber: string;
  fullName: string;
  specialization: string;
  phone: string;
  email?: string;
  active: boolean;
}

export interface Dentist {
  id: number;
  dentistNumber: string;
  fullName: string;
  specialization: string;
  phone: string;
  email?: string | null;
  active: boolean;
  createdAt?: string;
}

export interface DentistRequest {
  dentistNumber: string;
  fullName: string;
  specialization: string;
  phone: string;
  email?: string;
  active: boolean;
}

export interface TreatmentLookup {
  id: number;
  treatmentCode: string;
  treatmentName: string;
  description?: string;
  cost: number;
  active: boolean;
}

export type BillStatus = 'PENDING' | 'PAID' | 'CANCELLED' | 'REFUNDED';

export interface Bill {
  id: number;
  billNumber: string;
  appointmentId: number;
  appointmentNumber: string;
  patientId: number;
  patientName: string;
  patientNumber: string;
  dentistId: number;
  dentistName: string;
  treatmentId: number;
  treatmentName: string;
  treatmentCode: string;
  consultationFee: number;
  treatmentAmount: number;
  totalAmount: number;
  billDate: string;
  status: BillStatus;
  createdAt: string;
}

export interface BillRequest {
  appointmentId: number;
  consultationFee?: number;
  treatmentAmount?: number;
  billDate?: string;
  status?: BillStatus;
}

export interface ReceiptResponse {
  receiptNumber: string;
  billNumber: string;
  appointmentNumber: string;
  appointmentId: number;
  patientName: string;
  patientNumber: string;
  patientPhone?: string;
  patientEmail?: string;
  dentistName: string;
  dentistSpecialization?: string;
  treatmentName: string;
  treatmentCode: string;
  consultationFee: number;
  treatmentAmount: number;
  totalAmount: number;
  paymentStatus: BillStatus;
  paymentDate: string;
  issuedAt: string;
  clinicName: string;
  clinicAddress: string;
  clinicContact: string;
}

export interface RevenueReportResponse {
  totalRevenue: number;
  paidAmount: number;
  unpaidAmount: number;
  totalBills: number;
  paidBills: number;
  pendingBills: number;
  cancelledBills: number;
  refundedBills: number;
  fromDate?: string | null;
  toDate?: string | null;
}

export interface PaymentStatusItem {
  status: BillStatus;
  count: number;
  totalAmount: number;
}

export interface PaymentStatusReportResponse {
  breakdown: PaymentStatusItem[];
  totalCount: number;
  totalAmount: number;
}

export interface TreatmentRevenueResponse {
  treatmentId: number;
  treatmentCode: string;
  treatmentName: string;
  billCount: number;
  totalRevenue: number;
}



