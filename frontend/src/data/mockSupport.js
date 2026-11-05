export const SUPPORT_STATS = {
  totalTickets: { value: 1240, change: 4.8, isPositive: true, period: 'this month' },
  openTickets: { value: 42, change: -18.2, isPositive: true, period: 'active backlog' },
  resolvedTickets: { value: 1156, change: 12.1, isPositive: true, period: 'resolution rate 93.2%' },
  highPriorityTickets: { value: 9, change: -3, isPositive: true, period: 'SLA < 1hr' },
};

export const SUPPORT_RESOLUTION_TRENDS = [
  { day: 'Mon', opened: 68, resolved: 72 },
  { day: 'Tue', opened: 74, resolved: 76 },
  { day: 'Wed', opened: 82, resolved: 85 },
  { day: 'Thu', opened: 65, resolved: 70 },
  { day: 'Fri', opened: 90, resolved: 92 },
  { day: 'Sat', opened: 45, resolved: 48 },
  { day: 'Sun', opened: 32, resolved: 36 },
];

export const SUPPORT_CATEGORY_DISTRIBUTION = [
  { name: 'Card Replacement & ATM Issues', value: 340, color: '#10B981' },
  { name: 'Wire Transfers & Processing', value: 290, color: '#047857' },
  { name: 'MFA & Mobile App Access', value: 240, color: '#34D399' },
  { name: 'Fee Inquiries & Dispute Filing', value: 180, color: '#0F172A' },
  { name: 'Loan Account Servicing', value: 106, color: '#6EE7B7' },
];

export const SUPPORT_TICKETS = [
  {
    id: 'TCK-2026-891',
    customer: 'Eleanor Sterling',
    account: 'FIN-3190-22',
    subject: 'Urgent: Debit Card Blocked While Traveling Abroad',
    category: 'Card Issues',
    priority: 'High',
    slaRemaining: '24 mins',
    assignee: 'Hannah Abbott',
    status: 'In Progress',
    createdAt: '2026-11-05 09:20',
  },
  {
    id: 'TCK-2026-890',
    customer: 'Apex Logistics LLC',
    account: 'FIN-9901-44',
    subject: 'Missing FedWire Reference for $45,000 Supplier Payment',
    category: 'Wire Transfers',
    priority: 'High',
    slaRemaining: '45 mins',
    assignee: 'Liam Gallagher',
    status: 'Open',
    createdAt: '2026-11-05 08:50',
  },
  {
    id: 'TCK-2026-889',
    customer: 'Dr. Evelyn Foster',
    account: 'FIN-5512-99',
    subject: 'Request for Notarized Balance Certificate for Visa',
    category: 'Account Servicing',
    priority: 'Medium',
    slaRemaining: '3 hours',
    assignee: 'Maya Patel',
    status: 'In Progress',
    createdAt: '2026-11-05 08:15',
  },
  {
    id: 'TCK-2026-888',
    customer: 'Marcus Brody',
    account: 'FIN-1099-01',
    subject: 'Two-Factor Authentication device change request',
    category: 'Security / MFA',
    priority: 'Low',
    slaRemaining: '6 hours',
    assignee: 'Hannah Abbott',
    status: 'Resolved',
    createdAt: '2026-11-05 07:30',
  },
];

export const SUPPORT_COMPLAINTS = [
  { id: 'CMP-104', customer: 'Vanguard Realty Group', department: 'Commercial Underwriting', issue: 'Delay in processing second mortgage tranche', escalationLevel: 'Level 2 Escalated', status: 'Under Review' },
  { id: 'CMP-103', customer: 'Lucas Henderson', department: 'Branch Teller Ops', issue: 'Foreign exchange conversion rate query', escalationLevel: 'Resolved by Mgr', status: 'Closed' },
  { id: 'CMP-102', customer: 'Serena Gomez', department: 'Mobile Banking Tech', issue: 'Push notifications delay for biometric sign-in', escalationLevel: 'Transferred to IT', status: 'In Progress' },
];

