export const EMPLOYEE_STATS = {
  customersServed: { value: 142, change: 14.5, isPositive: true, period: 'today' },
  pendingRequests: { value: 18, change: -4, isPositive: true, period: 'in queue' },
  accountsOpened: { value: 9, change: 25.0, isPositive: true, period: 'today' },
  transactionsProcessed: { value: 184, change: 8.2, isPositive: true, period: 'today' },
};

export const EMPLOYEE_ACCOUNT_REQUESTS = [
  { id: 'REQ-ACC-101', applicant: 'Gabriel Sterling', nationalId: 'SSN-•••-4912', accountType: 'Checking Standard', depositAmount: 1500.00, riskScore: 'Low (0.12)', status: 'Pending Review', timestamp: '12m ago' },
  { id: 'REQ-ACC-102', applicant: 'Chloe Bennet', nationalId: 'SSN-•••-1108', accountType: 'High-Yield Savings', depositAmount: 25000.00, riskScore: 'Low (0.08)', status: 'Pending Review', timestamp: '24m ago' },
  { id: 'REQ-ACC-103', applicant: 'Kaelen Miller', nationalId: 'SSN-•••-7721', accountType: 'Premier Checking', depositAmount: 5000.00, riskScore: 'Medium (0.42)', status: 'Pending Review', timestamp: '45m ago' },
  { id: 'REQ-ACC-104', applicant: 'Zenith Tech LLC', nationalId: 'EIN-•••-9901', accountType: 'Corporate Escrow', depositAmount: 100000.00, riskScore: 'Low (0.15)', status: 'Pending Review', timestamp: '1h ago' },
];

export const EMPLOYEE_DEPOSIT_REQUESTS = [
  { id: 'REQ-DEP-401', customer: 'Brandon Cole', account: 'FIN-4402-19', method: 'Cash Vault Deposit', amount: 9800.00, verifiedId: true, status: 'Awaiting Teller' },
  { id: 'REQ-DEP-402', customer: 'Maria Gomez', account: 'FIN-8812-70', method: 'Certified Cashier Check', amount: 34200.00, verifiedId: true, status: 'Awaiting Teller' },
  { id: 'REQ-DEP-403', customer: 'Orion Global Ltd', account: 'FIN-9901-44', method: 'Endorsed Cheque Batch', amount: 145000.00, verifiedId: true, status: 'Awaiting Teller' },
];

export const EMPLOYEE_WITHDRAWAL_REQUESTS = [
  { id: 'REQ-WTH-801', customer: 'Anthony Hall', account: 'FIN-1123-90', method: 'Over-the-Counter Cash', amount: 8000.00, authVerified: true, status: 'Awaiting Teller' },
  { id: 'REQ-WTH-802', customer: 'Evelyn Reed', account: 'FIN-5561-38', method: 'Banker Draft', amount: 25000.00, authVerified: true, status: 'Awaiting Teller' },
];

export const EMPLOYEE_RECENT_CUSTOMERS = [
  { id: 'CUST-881', name: 'Alexander Price', tier: 'Premier', balance: 84900.00, accounts: 3, kycStatus: 'Verified', lastVisit: 'Today 10:14 AM' },
  { id: 'CUST-882', name: 'Harper Bailey', tier: 'Standard', balance: 14200.00, accounts: 2, kycStatus: 'Verified', lastVisit: 'Today 09:48 AM' },
  { id: 'CUST-883', name: 'Sebastian Cruz', tier: 'Private Banking', balance: 340000.00, accounts: 5, kycStatus: 'Verified', lastVisit: 'Today 09:12 AM' },
  { id: 'CUST-884', name: 'Zoe Washington', tier: 'Standard', balance: 8900.00, accounts: 1, kycStatus: 'Under Review', lastVisit: 'Yesterday' },
];

