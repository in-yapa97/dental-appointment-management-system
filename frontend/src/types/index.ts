/**
 * Core domain types and system contracts.
 * Business entities (Patient, Appointment, Dentist, etc.) will be added in subsequent milestones.
 */

export interface SystemHealth {
  status: 'UP' | 'DOWN' | string;
  service: string;
  timestamp: string;
  version: string;
}
