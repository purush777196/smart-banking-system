export const AUDIT_STATS = {
  totalAuditEvents: { value: 12480, change: 8.5, isPositive: true, period: 'past 30 days' },
  pendingKYC: { value: 46, change: -14.2, isPositive: true, period: 'in verification queue' },
  verifiedCustomers: { value: 98.4, change: 0.6, isPositive: true, period: 'compliance rating %' },
  complianceAlerts: { value: 7, change: -2, isPositive: true, period: 'active oversight cases' },
};

export const AUDIT_LOGS = [
  { id: 'LOG-8812', timestamp: '2026-11-05 09:42:15', actor: 'Daniel Wright (Teller #04)', action: 'Overrode Daily Cash Limit ($15,000)', ipAddress: '10.240.12.84', severity: 'Warning', status: 'Approved by Mgr' },
  { id: 'LOG-8811', timestamp: '2026-11-05 09:12:08', actor: 'System Automated Engine', action: 'Daily OFAC Sanctions List Sync', ipAddress: 'Internal VPC', severity: 'Info', status: 'Completed 0 matches' },
  { id: 'LOG-8810', timestamp: '2026-11-05 08:31:54', actor: 'Eleanor Vance (Admin)', action: 'Modified Role Permissions for Branch Mgrs', ipAddress: '10.240.1.10', severity: 'Warning', status: 'Audited' },
  { id: 'LOG-8809', timestamp: '2026-11-05 07:15:22', actor: 'Julian Chen (SecOps)', action: 'Placed Temporary Freeze on ACC #FIN-4820', ipAddress: '10.240.8.19', severity: 'Danger', status: 'Hold Imposed' },
  { id: 'LOG-8808', timestamp: '2026-11-04 18:40:02', actor: 'Amina Patel (Loan Officer)', action: 'Approved Credit Facility $680,000.00', ipAddress: '10.240.4.92', severity: 'Info', status: 'Compliant' },
];

export const AUDIT_KYC_RECORDS = [
  { id: 'KYC-991', customer: 'Mateo De Luca', docType: 'Passport + Proof of Address', riskTier: 'Low', submissionDate: '2026-11-05', status: 'Pending Review', country: 'Italy' },
  { id: 'KYC-992', customer: 'Nadia Rostova', docType: 'National ID Card + Utility Bill', riskTier: 'Low', submissionDate: '2026-11-05', status: 'Pending Review', country: 'Germany' },
  { id: 'KYC-993', customer: 'Vanguard Realty LLC', docType: 'Articles of Inc + Beneficial Ownership', riskTier: 'High', submissionDate: '2026-11-04', status: 'Under Enhanced DD', country: 'United States' },
  { id: 'KYC-994', customer: 'Dr. Tariq Al-Mansoor', docType: 'Diplomatic Passport', riskTier: 'Medium (PEP Check)', submissionDate: '2026-11-04', status: 'Pending Review', country: 'UAE' },
];

export const AUDIT_ACCOUNT_ACTIVITIES = [
  { id: 'ACT-401', account: 'FIN-8942-01', customer: 'Sophia Rodriguez', event: 'New Device Login Registered (iPhone 16 Pro)', location: 'New York, US', status: 'MFA Verified' },
  { id: 'ACT-402', account: 'FIN-1092-88', customer: 'Quantum Capital LLC', event: 'Authorized Signer Added: Patrick Sloan', location: 'London, UK', status: 'Documentation Verified' },
  { id: 'ACT-403', account: 'FIN-3391-02', customer: 'David H. Brooks', event: 'International Swift Limit Elevated to $100K', location: 'Branch Desk #02', status: 'Manager Signoff Completed' },
];

