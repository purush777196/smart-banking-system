export const LOAN_STATS = {
  totalApplications: { value: 842, change: 16.8, isPositive: true, period: 'this quarter' },
  approvedLoans: { value: 624, change: 12.3, isPositive: true, period: 'approval rate 74.1%' },
  pendingApplications: { value: 128, change: -5.4, isPositive: true, period: 'under underwriting' },
  outstandingAmount: { value: 48600000, change: 9.1, isPositive: true, period: 'portfolio volume' },
};

export const LOAN_DISTRIBUTION = [
  { name: 'Residential Mortgages', value: 24300000, color: '#10B981' },
  { name: 'Commercial Real Estate', value: 14200000, color: '#047857' },
  { name: 'Small Business Lines', value: 6100000, color: '#34D399' },
  { name: 'Auto & Personal Loans', value: 4000000, color: '#0F172A' },
];

export const LOAN_MONTHLY_APPROVALS = [
  { month: 'May', requested: 14.2, approved: 10.8 },
  { month: 'Jun', requested: 15.6, approved: 11.4 },
  { month: 'Jul', requested: 16.8, approved: 12.5 },
  { month: 'Aug', requested: 18.2, approved: 13.9 },
  { month: 'Sep', requested: 19.5, approved: 14.6 },
  { month: 'Oct', requested: 21.0, approved: 15.8 },
];

export const LOAN_REPAYMENT_ANALYTICS = [
  { month: 'May', onTime: 96.2, gracePeriod: 2.8, defaulted: 1.0 },
  { month: 'Jun', onTime: 95.8, gracePeriod: 3.1, defaulted: 1.1 },
  { month: 'Jul', onTime: 96.5, gracePeriod: 2.6, defaulted: 0.9 },
  { month: 'Aug', onTime: 96.9, gracePeriod: 2.3, defaulted: 0.8 },
  { month: 'Sep', onTime: 97.2, gracePeriod: 2.1, defaulted: 0.7 },
  { month: 'Oct', onTime: 97.4, gracePeriod: 2.0, defaulted: 0.6 },
];

export const LOAN_APPLICATIONS = [
  {
    id: 'LN-2026-901',
    applicant: 'AeroDynamics Logistics Corp',
    loanType: 'Commercial Line',
    amount: 1500000.00,
    tenure: '60 Months',
    interestRate: '6.45%',
    creditScore: 785,
    riskRating: 'Low Risk',
    status: 'Under Review',
    submittedDate: '2026-11-04',
  },
  {
    id: 'LN-2026-902',
    applicant: 'Marcus & Elena Vance',
    loanType: 'Home Mortgage 30Y',
    amount: 680000.00,
    tenure: '360 Months',
    interestRate: '5.85%',
    creditScore: 810,
    riskRating: 'Low Risk',
    status: 'Approved',
    submittedDate: '2026-11-03',
  },
  {
    id: 'LN-2026-903',
    applicant: 'Apex Artisan Bakery LLC',
    loanType: 'Small Business Equipment',
    amount: 120000.00,
    tenure: '36 Months',
    interestRate: '7.20%',
    creditScore: 690,
    riskRating: 'Medium Risk',
    status: 'Under Review',
    submittedDate: '2026-11-02',
  },
  {
    id: 'LN-2026-904',
    applicant: 'Julian K. Mercer',
    loanType: 'Personal Debt Consolidation',
    amount: 35000.00,
    tenure: '24 Months',
    interestRate: '9.40%',
    creditScore: 620,
    riskRating: 'High Risk',
    status: 'Pending Documents',
    submittedDate: '2026-11-01',
  },
  {
    id: 'LN-2026-905',
    applicant: 'Dr. Evelyn Foster',
    loanType: 'Practice Expansion Loan',
    amount: 450000.00,
    tenure: '84 Months',
    interestRate: '6.15%',
    creditScore: 825,
    riskRating: 'Low Risk',
    status: 'Approved',
    submittedDate: '2026-10-30',
  },
];

export const LOAN_REPAYMENT_RECORDS = [
  { id: 'RPM-8812', loanId: 'LN-2026-844', borrower: 'Nexus Media Inc', emiAmount: 18450.00, principal: 14200.00, interest: 4250.00, dueDate: '2026-11-10', status: 'Paid On Time' },
  { id: 'RPM-8813', loanId: 'LN-2026-791', borrower: 'Gregory Hayes', emiAmount: 3420.00, principal: 2750.00, interest: 670.00, dueDate: '2026-11-12', status: 'Scheduled' },
  { id: 'RPM-8814', loanId: 'LN-2026-650', borrower: 'Solstice Hospitality', emiAmount: 42000.00, principal: 32000.00, interest: 10000.00, dueDate: '2026-11-15', status: 'Scheduled' },
  { id: 'RPM-8815', loanId: 'LN-2026-512', borrower: 'Katherine Hughes', emiAmount: 1250.00, principal: 980.00, interest: 270.00, dueDate: '2026-11-05', status: 'Paid On Time' },
];

