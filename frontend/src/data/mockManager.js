export const MANAGER_STATS = {
  branchCustomers: { value: 4820, change: 8.4, isPositive: true, period: 'vs last month' },
  activeAccounts: { value: 6140, change: 5.2, isPositive: true, period: 'vs last month' },
  branchDeposits: { value: 142800000, change: 11.8, isPositive: true, period: 'vs last month' },
  branchWithdrawals: { value: 38400000, change: -2.1, isPositive: true, period: 'vs last month' },
};

export const MANAGER_PERFORMANCE_DATA = [
  { month: 'May', target: 120, actual: 128 },
  { month: 'Jun', target: 125, actual: 132 },
  { month: 'Jul', target: 130, actual: 135 },
  { month: 'Aug', target: 135, actual: 138 },
  { month: 'Sep', target: 140, actual: 142 },
  { month: 'Oct', target: 145, actual: 154 },
];

export const MANAGER_MONTHLY_TRANSACTIONS = [
  { week: 'Week 1', deposits: 32.4, withdrawals: 8.9 },
  { week: 'Week 2', deposits: 35.1, withdrawals: 9.4 },
  { week: 'Week 3', deposits: 38.6, withdrawals: 10.2 },
  { week: 'Week 4', deposits: 36.7, withdrawals: 9.9 },
];

export const MANAGER_EMPLOYEE_PERFORMANCE = [
  { name: 'Daniel Wright', role: 'Head Teller', target: 95, score: 98, processedCount: 420 },
  { name: 'Jessica Miller', role: 'Relationship Mgr', target: 90, score: 94, processedCount: 310 },
  { name: 'Ethan Hunt', role: 'Loan Specialist', target: 85, score: 91, processedCount: 185 },
  { name: 'Rachel Zane', role: 'Customer Desk', target: 88, score: 89, processedCount: 290 },
  { name: 'Oscar Diaz', role: 'Junior Teller', target: 80, score: 86, processedCount: 340 },
];

export const MANAGER_EMPLOYEES = [
  { id: 'EMP-01', name: 'Daniel Wright', role: 'Head Teller Desk #1', email: 'daniel.w@finova.bank', status: 'On Duty', accountsOpened: 38, satisfactionScore: 4.9 },
  { id: 'EMP-02', name: 'Jessica Miller', role: 'Senior Relationship Mgr', email: 'jessica.m@finova.bank', status: 'On Duty', accountsOpened: 42, satisfactionScore: 4.8 },
  { id: 'EMP-03', name: 'Ethan Hunt', role: 'Retail Lending Desk', email: 'ethan.h@finova.bank', status: 'In Meeting', accountsOpened: 19, satisfactionScore: 4.7 },
  { id: 'EMP-04', name: 'Rachel Zane', role: 'Client Onboarding Desk', email: 'rachel.z@finova.bank', status: 'On Duty', accountsOpened: 51, satisfactionScore: 4.9 },
  { id: 'EMP-05', name: 'Oscar Diaz', role: 'Junior Teller Desk #3', email: 'oscar.d@finova.bank', status: 'On Duty', accountsOpened: 27, satisfactionScore: 4.6 },
];

export const MANAGER_RECENT_TRANSACTIONS = [
  { id: 'TXN-B104-091', customer: 'Arthur Pendelton', type: 'Cash Deposit', amount: 15000.00, desk: 'Desk #1', status: 'Completed', time: '10 mins ago' },
  { id: 'TXN-B104-090', customer: 'Lillian Thorne', type: 'Cashier Check', amount: 48500.00, desk: 'Desk #2', status: 'Completed', time: '25 mins ago' },
  { id: 'TXN-B104-089', customer: 'Omega Studios Corp', type: 'Merchant Batch', amount: 128400.00, desk: 'Desk #1', status: 'Completed', time: '40 mins ago' },
  { id: 'TXN-B104-088', customer: 'Lucas Graham', type: 'Wire Transfer', amount: 8900.00, desk: 'Desk #4', status: 'Pending Approval', time: '1 hour ago' },
  { id: 'TXN-B104-087', customer: 'Nora Vasquez', type: 'Vault Deposit', amount: 72000.00, desk: 'Vault Ops', status: 'Completed', time: '2 hours ago' },
];

