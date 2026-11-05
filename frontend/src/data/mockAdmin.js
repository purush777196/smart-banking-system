export const ADMIN_STATS = {
  totalCustomers: { value: 48290, change: 12.4, isPositive: true, period: 'vs last month' },
  totalAccounts: { value: 64120, change: 8.7, isPositive: true, period: 'vs last month' },
  totalDeposits: { value: 1842900000, change: 15.2, isPositive: true, period: 'vs last month' },
  totalWithdrawals: { value: 642300000, change: -3.8, isPositive: true, period: 'vs last month' },
  totalRevenue: { value: 84250000, change: 18.6, isPositive: true, period: 'vs last month' },
};

export const ADMIN_REVENUE_TRENDS = [
  { month: 'Jan', revenue: 64.2, expenses: 38.1, net: 26.1 },
  { month: 'Feb', revenue: 67.8, expenses: 39.4, net: 28.4 },
  { month: 'Mar', revenue: 71.5, expenses: 40.2, net: 31.3 },
  { month: 'Apr', revenue: 69.1, expenses: 39.8, net: 29.3 },
  { month: 'May', revenue: 74.8, expenses: 41.5, net: 33.3 },
  { month: 'Jun', revenue: 77.2, expenses: 42.1, net: 35.1 },
  { month: 'Jul', revenue: 79.6, expenses: 42.9, net: 36.7 },
  { month: 'Aug', revenue: 81.3, expenses: 43.4, net: 37.9 },
  { month: 'Sep', revenue: 83.0, expenses: 43.8, net: 39.2 },
  { month: 'Oct', revenue: 84.25, expenses: 44.1, net: 40.15 },
];

export const ADMIN_VOLUME_DATA = [
  { day: 'Mon', internal: 4200, wires: 1800, atm: 1200 },
  { day: 'Tue', internal: 4900, wires: 2100, atm: 1350 },
  { day: 'Wed', internal: 5300, wires: 2400, atm: 1400 },
  { day: 'Thu', internal: 5100, wires: 2300, atm: 1380 },
  { day: 'Fri', internal: 6400, wires: 3100, atm: 1950 },
  { day: 'Sat', internal: 3800, wires: 1200, atm: 2200 },
  { day: 'Sun', internal: 2900, wires: 950, atm: 1800 },
];

export const ADMIN_CUSTOMER_GROWTH = [
  { month: 'May', retail: 34200, corporate: 3100 },
  { month: 'Jun', retail: 36100, corporate: 3250 },
  { month: 'Jul', retail: 38500, corporate: 3400 },
  { month: 'Aug', retail: 41200, corporate: 3580 },
  { month: 'Sep', retail: 44100, corporate: 3790 },
  { month: 'Oct', retail: 48290, corporate: 4020 },
];

export const ADMIN_RECENT_TRANSACTIONS = [
  {
    id: 'TXN-902184',
    customer: 'Sarah Jenkins',
    account: 'FIN-7821-44',
    type: 'Wire Transfer',
    amount: 145000.00,
    status: 'Completed',
    date: '2026-11-05T09:15:00',
    branch: 'Downtown Metro',
  },
  {
    id: 'TXN-902183',
    customer: 'Quantum Capital LLC',
    account: 'FIN-1092-88',
    type: 'Commercial Deposit',
    amount: 4500000.00,
    status: 'Completed',
    date: '2026-11-05T08:52:00',
    branch: 'Financial District',
  },
  {
    id: 'TXN-902182',
    customer: 'David H. Brooks',
    account: 'FIN-3391-02',
    type: 'International Swift',
    amount: 84200.00,
    status: 'Pending',
    date: '2026-11-05T08:41:00',
    branch: 'Midtown East',
  },
  {
    id: 'TXN-902181',
    customer: 'Elena Rostova',
    account: 'FIN-5512-99',
    type: 'ATM Withdrawal',
    amount: 1200.00,
    status: 'Completed',
    date: '2026-11-05T08:30:00',
    branch: 'Metro Center',
  },
  {
    id: 'TXN-902180',
    customer: 'Vanguard Realty Group',
    account: 'FIN-8819-21',
    type: 'Loan Disbursement',
    amount: 850000.00,
    status: 'Flagged',
    date: '2026-11-05T08:12:00',
    branch: 'Upper West Side',
  },
];

export const ADMIN_TOP_BRANCHES = [
  { id: 'BR-101', name: 'Downtown Financial HQ', manager: 'Marcus Sterling', deposits: 482500000, revenue: 24800000, healthScore: 98, status: 'Active' },
  { id: 'BR-102', name: 'Midtown Corporate Center', manager: 'Valerie Adams', deposits: 395100000, revenue: 19400000, healthScore: 95, status: 'Active' },
  { id: 'BR-103', name: 'Silicon Bay Tech Hub', manager: 'Arjun Mehta', deposits: 341800000, revenue: 18200000, healthScore: 96, status: 'Active' },
  { id: 'BR-104', name: 'Chicago Loop Regional', manager: 'Brian Gallagher', deposits: 289400000, revenue: 14100000, healthScore: 92, status: 'Active' },
  { id: 'BR-105', name: 'London City Gateway', manager: 'Camilla Thorne', deposits: 245000000, revenue: 12600000, healthScore: 94, status: 'Active' },
];

