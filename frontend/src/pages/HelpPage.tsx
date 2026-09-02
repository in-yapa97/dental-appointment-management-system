import React, { useState } from 'react';
import {
  Users,
  Calendar,
  CreditCard,
  BarChart3,
  Stethoscope,
  ShieldCheck,
  Search,
  AlertTriangle,
  ChevronRight,
  BookOpen,
  Sparkles,
} from 'lucide-react';

interface GuideSection {
  id: string;
  title: string;
  icon: React.ReactNode;
  badge: string;
  description: string;
  steps: {
    stepNumber: number;
    stepTitle: string;
    instructions: string;
    tip?: string;
  }[];
  keyRules?: string[];
}

const GUIDE_SECTIONS: GuideSection[] = [
  {
    id: 'patients',
    title: '1. Patient Registration & Management',
    icon: <Users size={20} className="text-primary" />,
    badge: 'Core Module',
    description: 'How to register new patients, search patient medical directories, and maintain patient contact records.',
    steps: [
      {
        stepNumber: 1,
        stepTitle: 'Access Patient Directory',
        instructions: 'Click the "Patients" tab in the top navigation bar to view the complete list of registered clinic patients.',
      },
      {
        stepNumber: 2,
        stepTitle: 'Register a New Patient',
        instructions: 'Click the "+ Add Patient" button on the top right. A modal window will open.',
        tip: 'Click the "Auto" button next to Patient ID to generate a standardized ID like PAT-101.',
      },
      {
        stepNumber: 3,
        stepTitle: 'Fill in Patient Demographics',
        instructions: 'Enter Full Name, Date of Birth (YYYY-MM-DD), Gender, Phone Number, Email (optional), and Residential Address.',
      },
      {
        stepNumber: 4,
        stepTitle: 'Search and Filter Records',
        instructions: 'Use the real-time search bar to search by Patient ID (e.g. PAT-001), full name, contact phone number, or email.',
      },
      {
        stepNumber: 5,
        stepTitle: 'Edit or View Patient Details',
        instructions: 'Click "View" to see full demographic details or "Edit" to update phone number, email, or address.',
        tip: 'Patients with existing appointments cannot be deleted to preserve medical history integrity.',
      },
    ],
    keyRules: [
      'Patient ID must be unique across the entire clinic system.',
      'Valid phone number and date of birth are mandatory for booking appointment eligibility.',
    ],
  },
  {
    id: 'dentists',
    title: '2. Dentist & Practitioner Management',
    icon: <Stethoscope size={20} className="text-primary" />,
    badge: 'Practitioner Catalog',
    description: 'How to register dental practitioners, assign specializations, and manage practicing availability.',
    steps: [
      {
        stepNumber: 1,
        stepTitle: 'Navigate to Dentists Directory',
        instructions: 'Click the "Dentists" tab in the top navigation bar to view all licensed clinic specialists.',
      },
      {
        stepNumber: 2,
        stepTitle: 'Register a Dental Specialist',
        instructions: 'Click "+ Register Dentist". Provide Practitioner Full Name (e.g. Dr. Emily Thorne) and Dentist ID (e.g. DEN-101).',
        tip: 'Use the "Auto" button to generate a unique dentist ID automatically.',
      },
      {
        stepNumber: 3,
        stepTitle: 'Select Specialization & Contact',
        instructions: 'Select the primary specialty (General Dentistry, Orthodontics, Endodontics, Oral Surgery, etc.) and enter phone and email.',
      },
      {
        stepNumber: 4,
        stepTitle: 'Set Practicing Status',
        instructions: 'Ensure "Active Practicing Status" is checked so the dentist appears in appointment booking menus.',
      },
      {
        stepNumber: 5,
        stepTitle: 'Filter by Specialty',
        instructions: 'Use the Specialization dropdown filter to quickly find all Orthodontists, Oral Surgeons, or General Dentists.',
      },
    ],
    keyRules: [
      'Inactive dentists cannot be selected for new appointment bookings.',
      'Dentists with scheduled or completed patient appointments cannot be deleted from the database.',
    ],
  },
  {
    id: 'appointments',
    title: '3. Appointment Scheduling & Availability Check',
    icon: <Calendar size={20} className="text-primary" />,
    badge: 'Scheduling Engine',
    description: 'Step-by-step appointment booking, conflict prevention, procedure selection, and status lifecycle management.',
    steps: [
      {
        stepNumber: 1,
        stepTitle: 'Open Booking Form',
        instructions: 'Navigate to "Appointments" and click "+ Book Appointment".',
      },
      {
        stepNumber: 2,
        stepTitle: 'Select Patient, Dentist & Procedure',
        instructions: 'Choose the registered Patient, the assigned Dentist, and the Treatment Procedure (e.g. Comprehensive Dental Exam, Tooth Extraction, Root Canal).',
      },
      {
        stepNumber: 3,
        stepTitle: 'Choose Date & Time Slot',
        instructions: 'Select the scheduled appointment date and time slot (e.g. 09:00 AM, 02:00 PM).',
      },
      {
        stepNumber: 4,
        stepTitle: 'Verify Availability (Conflict Prevention)',
        instructions: 'Click the "Check Dentist Availability" button. The system verifies in real-time whether the dentist has any conflicting appointments at that time.',
        tip: 'If a slot is already taken, a warning banner will appear advising staff to pick another time.',
      },
      {
        stepNumber: 5,
        stepTitle: 'Manage Status Lifecycle',
        instructions: 'During or after the consultation, staff can update status: Scheduled → Confirmed → Completed / Cancelled / No Show.',
      },
    ],
    keyRules: [
      'The scheduling engine strictly prevents double-booking a dentist at the same date and time.',
      'Completed appointments unlock instant one-click billing generation.',
    ],
  },
  {
    id: 'billing',
    title: '4. Billing, Invoicing & Official Receipts',
    icon: <CreditCard size={20} className="text-primary" />,
    badge: 'Financial Management',
    description: 'How to generate invoices, calculate consultation and treatment fees, record payments, and print receipts.',
    steps: [
      {
        stepNumber: 1,
        stepTitle: 'Create an Invoice',
        instructions: 'Navigate to "Billing" and click "+ Create Bill". Select the target appointment from the dropdown.',
      },
      {
        stepNumber: 2,
        stepTitle: 'Automatic Fee Calculation',
        instructions: 'The system automatically pulls the procedure cost (e.g., $150.00 for Extraction) and adds the Consultation Fee ($50.00) to calculate the Total Amount ($200.00).',
        tip: 'Staff can customize the Consultation Fee if special clinic discounts apply.',
      },
      {
        stepNumber: 3,
        stepTitle: 'Record Payment Settlement',
        instructions: 'When the patient pays via cash, card, or insurance, update the bill status from "PENDING" to "PAID".',
      },
      {
        stepNumber: 4,
        stepTitle: 'Print Official Clinic Receipt',
        instructions: 'Click the "Receipt" / "Print" button on any bill record. An official branded receipt with clinic header, itemized breakdown, transaction timestamp, and authorization stamp will open for printing or PDF export.',
      },
    ],
    keyRules: [
      'Each appointment can only have one associated billing record to prevent duplicate billing.',
      'Paid bills are permanently recorded and feed directly into executive financial reports.',
    ],
  },
  {
    id: 'reports',
    title: '5. Clinic Reports & Executive Analytics',
    icon: <BarChart3 size={20} className="text-primary" />,
    badge: 'Business Intelligence',
    description: 'How to monitor clinic performance, total gross revenue, top procedures, and daily financial summaries.',
    steps: [
      {
        stepNumber: 1,
        stepTitle: 'Navigate to Reports',
        instructions: 'Click the "Reports" tab in the top navigation bar to load real-time clinic analytics.',
      },
      {
        stepNumber: 2,
        stepTitle: 'Review Financial KPI Summary',
        instructions: 'View total gross revenue collected, number of settled bills, pending receivables, and overall collection rate.',
      },
      {
        stepNumber: 3,
        stepTitle: 'Analyze Procedure Breakdown',
        instructions: 'Examine the "Top Treatments by Revenue" breakdown to see which clinical services generate the highest clinic revenue.',
      },
      {
        stepNumber: 4,
        stepTitle: 'Audit Daily Revenue Log',
        instructions: 'Inspect chronological daily financial transactions and export or print data for audit reviews.',
      },
    ],
  },
  {
    id: 'troubleshooting',
    title: '6. Common Errors & Troubleshooting FAQ',
    icon: <AlertTriangle size={20} className="text-primary" />,
    badge: 'Staff FAQ',
    description: 'Quick solutions to common operational questions and error messages.',
    steps: [
      {
        stepNumber: 1,
        stepTitle: 'Error: "Time slot is already booked for this dentist"',
        instructions: 'The selected practitioner already has an active appointment at that time. Choose an alternate time slot or select another available practitioner with matching specialization.',
      },
      {
        stepNumber: 2,
        stepTitle: 'Error: "Cannot delete dentist / patient with existing appointments"',
        instructions: 'Relational database foreign keys protect medical history. You must cancel or remove linked appointments before deleting a patient or dentist record.',
      },
      {
        stepNumber: 3,
        stepTitle: 'Missing Treatment Procedures in Dropdown',
        instructions: 'If procedures are not showing, ensure your internet connectivity is active and check the backend health indicator in My Profile.',
      },
      {
        stepNumber: 4,
        stepTitle: 'How to Log Out or Switch Accounts',
        instructions: 'Click your profile avatar pill on the top right to access account details, or click "Logout" to end the session securely.',
      },
    ],
  },
];

export const HelpPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('patients');
  const [searchQuery, setSearchQuery] = useState<string>('');

  const currentSection = GUIDE_SECTIONS.find((s) => s.id === activeTab) || GUIDE_SECTIONS[0];

  const filteredSections = searchQuery.trim()
    ? GUIDE_SECTIONS.filter(
        (s) =>
          s.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          s.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
          s.steps.some(
            (st) =>
              st.stepTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
              st.instructions.toLowerCase().includes(searchQuery.toLowerCase())
          )
      )
    : GUIDE_SECTIONS;

  return (
    <div className="patients-container" style={{ maxWidth: '1100px', margin: '0 auto' }}>
      {/* Page Header */}
      <div className="page-header" style={{ marginBottom: '1.75rem' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.4rem', color: '#0284c7', fontWeight: 700, fontSize: '0.85rem', marginBottom: '0.35rem' }}>
            <BookOpen size={16} />
            <span>Staff Onboarding &amp; Operations Manual</span>
          </div>
          <h1 className="page-title" style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            Help &amp; Step-by-Step Staff Guide
          </h1>
          <p className="page-subtitle">
            Comprehensive workflow instructions, clinical scheduling rules, billing guides, and troubleshooting for clinic staff
          </p>
        </div>
      </div>

      {/* Search Help Topics */}
      <div className="filter-bar-card" style={{ marginBottom: '1.5rem' }}>
        <div style={{ position: 'relative', width: '100%', display: 'flex', alignItems: 'center' }}>
          <Search size={18} style={{ position: 'absolute', left: '1rem', color: '#94a3b8', pointerEvents: 'none' }} />
          <input
            type="text"
            className="form-input search-input"
            style={{ paddingLeft: '2.75rem', width: '100%' }}
            placeholder="Search instructions, workflows, error codes, or topics (e.g. 'book appointment', 'receipt', 'auto id')..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            id="input-search-help"
          />
        </div>
      </div>

      {/* Layout: Sidebar Guide Tabs + Content Area */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(260px, 320px) 1fr', gap: '1.5rem', alignItems: 'start' }}>
        {/* Navigation Sidebar */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          <div style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', letterSpacing: '0.05em', padding: '0.25rem 0.5rem' }}>
            Operations Modules ({filteredSections.length})
          </div>
          {filteredSections.map((section) => (
            <button
              key={section.id}
              onClick={() => setActiveTab(section.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '0.85rem 1rem',
                borderRadius: '10px',
                border: activeTab === section.id ? '1.5px solid #0284c7' : '1px solid var(--border-color)',
                backgroundColor: activeTab === section.id ? '#f0f9ff' : 'var(--bg-card)',
                color: activeTab === section.id ? '#0284c7' : 'var(--text-main)',
                fontWeight: activeTab === section.id ? 700 : 600,
                textAlign: 'left',
                cursor: 'pointer',
                transition: 'all 0.15s ease',
                boxShadow: activeTab === section.id ? '0 2px 6px rgba(2, 132, 199, 0.12)' : 'var(--shadow-sm)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                {section.icon}
                <span style={{ fontSize: '0.875rem' }}>{section.title}</span>
              </div>
              <ChevronRight size={16} style={{ opacity: activeTab === section.id ? 1 : 0.4 }} />
            </button>
          ))}

          {/* Quick Support Card */}
          <div className="card" style={{ marginTop: '1rem', background: 'linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%)' }}>
            <h4 style={{ fontSize: '0.9rem', fontWeight: 700, color: 'var(--text-main)', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.4rem' }}>
              <ShieldCheck size={16} className="text-primary" />
              Role Permissions
            </h4>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: 1.4 }}>
              All registered staff members have authorized access to manage Patients, Appointments, Dentists, Invoices, and Reports under JWT security.
            </p>
          </div>
        </div>

        {/* Main Content Area */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Section Overview Card */}
          <div className="card" style={{ borderLeft: '4px solid #0284c7' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
              <span className="tech-tag" style={{ backgroundColor: '#e0f2fe', color: '#0369a1', borderColor: '#bae6fd', fontWeight: 700 }}>
                {currentSection.badge}
              </span>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                {currentSection.steps.length} Steps in Workflow
              </span>
            </div>
            <h2 style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--text-main)', marginBottom: '0.4rem' }}>
              {currentSection.title}
            </h2>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', lineHeight: 1.5 }}>
              {currentSection.description}
            </p>
          </div>

          {/* Step-by-Step Instructions */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {currentSection.steps.map((step) => (
              <div
                key={step.stepNumber}
                className="card"
                style={{
                  display: 'flex',
                  gap: '1rem',
                  padding: '1.25rem',
                  border: '1px solid var(--border-color)',
                  boxShadow: 'var(--shadow-sm)',
                }}
              >
                <div
                  style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '50%',
                    backgroundColor: '#0284c7',
                    color: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontWeight: 800,
                    fontSize: '0.9rem',
                    flexShrink: 0,
                  }}
                >
                  {step.stepNumber}
                </div>
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                  <h3 style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--text-main)' }}>
                    {step.stepTitle}
                  </h3>
                  <p style={{ fontSize: '0.875rem', color: '#334155', lineHeight: 1.5, margin: 0 }}>
                    {step.instructions}
                  </p>
                  {step.tip && (
                    <div
                      style={{
                        marginTop: '0.5rem',
                        padding: '0.5rem 0.75rem',
                        backgroundColor: '#f0fdf4',
                        border: '1px solid #bbf7d0',
                        borderRadius: '6px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.45rem',
                        fontSize: '0.8rem',
                        color: '#166534',
                        fontWeight: 600,
                      }}
                    >
                      <Sparkles size={14} style={{ color: '#16a34a', flexShrink: 0 }} />
                      <span><strong>Staff Tip:</strong> {step.tip}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Key Clinical & System Rules */}
          {currentSection.keyRules && currentSection.keyRules.length > 0 && (
            <div
              style={{
                backgroundColor: '#fffbeb',
                border: '1px solid #fde68a',
                borderRadius: '10px',
                padding: '1.2rem',
              }}
            >
              <h4 style={{ fontSize: '0.9rem', fontWeight: 700, color: '#92400e', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.5rem' }}>
                <AlertTriangle size={16} />
                Important System Constraints &amp; Validation Rules
              </h4>
              <ul style={{ margin: 0, paddingLeft: '1.25rem', color: '#78350f', fontSize: '0.85rem', lineHeight: 1.5 }}>
                {currentSection.keyRules.map((rule, idx) => (
                  <li key={idx} style={{ marginBottom: '0.25rem' }}>{rule}</li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
export default HelpPage;
