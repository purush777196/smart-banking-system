export const CUSTOMER_STATS = {
  availableBalance: { value: 42850.50, change: 4.8, isPositive: true, period: 'vs last month' },
  totalDeposits: { value: 12400.00, change: 8.2, isPositive: true, period: 'this month' },
  totalWithdrawals: { value: 3850.00, change: -12.4, isPositive: true, period: 'this month' },
  monthlySpending: { value: 2640.80, change: -5.1, isPositive: true, period: 'vs budget' },
};

export const CUSTOMER_ACCOUNTS = [
  {
    id: 'ACC-1092',
    name: 'Premier Checking Account',
    type: 'Checking',
    accountNumber: '9842109284',
    iban: 'US34FINO9842109284',
    balance: 24650.50,
    currency: 'USD',
    isPrimary: true,
    status: 'Active',
    cardBrand: 'Visa Infinite',
    cardExpiry: '09/29',
    cardNumber: '4192 •••• •••• 8821',
  },
  {
    id: 'ACC-8831',
    name: 'High-Yield Growth Savings',
    type: 'Savings',
    accountNumber: '4410883109',
    iban: 'US34FINO4410883109',
    balance: 15400.00,
    currency: 'USD',
    apy: '4.85% APY',
    isPrimary: false,
    status: 'Active',
  },
  {
    id: 'ACC-3120',
    name: 'Finova Global Treasury Portfolio',
    type: 'Investment',
    accountNumber: '7720312045',
    iban: 'US34FINO7720312045',
    balance: 2800.00,
    currency: 'USD',
    apy: '+8.4% Return YTD',
    isPrimary: false,
    status: 'Active',
  },
];

export const CUSTOMER_SPENDING_CATEGORIES = [
  { name: 'Housing & Rent', value: 1200, color: '#10B981' },
  { name: 'Groceries & Dining', value: 580, color: '#047857' },
  { name: 'Utilities & Subscriptions', value: 310, color: '#34D399' },
  { name: 'Travel & Mobility', value: 280, color: '#6EE7B7' },
  { name: 'Personal & Healthcare', value: 270.80, color: '#0F172A' },
];

export const CUSTOMER_MONTHLY_SPEND_TREND = [
  { month: 'Jun', spending: 2950, income: 4200 },
  { month: 'Jul', spending: 3100, income: 4200 },
  { month: 'Aug', spending: 2850, income: 4500 },
  { month: 'Sep', spending: 3300, income: 4500 },
  { month: 'Oct', spending: 2640, income: 4800 },
];

export const CUSTOMER_BENEFICIARIES = [
  { id: 'BEN-01', name: 'Liam Vance', bank: 'Finova Internal', account: '•••• 4921', avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80' },
  { id: 'BEN-02', name: 'Maya Lin', bank: 'J.P. Morgan Chase', account: '•••• 8831', avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80' },
  { id: 'BEN-03', name: 'Noah Green', bank: 'Bank of America', account: '•••• 1209', avatar: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=120&q=80' },
  { id: 'BEN-04', name: 'Apex Property LLC', bank: 'Wells Fargo', account: '•••• 9012', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=120&q=80' },
];

export const CUSTOMER_RECENT_TRANSACTIONS = [
  { id: 'TX-C891', title: 'Salary Deposit — Finova Global Inc', category: 'Income', amount: 4800.00, isCredit: true, date: '2026-11-01', status: 'Completed', account: 'Checking •••• 8821' },
  { id: 'TX-C890', title: 'Luxury Apt Lease Payment', category: 'Housing', amount: -1200.00, isCredit: false, date: '2026-11-02', status: 'Completed', account: 'Checking •••• 8821' },
  { id: 'TX-C889', title: 'Whole Foods Market', category: 'Groceries', amount: -142.30, isCredit: false, date: '2026-11-03', status: 'Completed', account: 'Checking •••• 8821' },
  { id: 'TX-C888', title: 'High Yield Interest Payout', category: 'Interest', amount: 58.40, isCredit: true, date: '2026-11-03', status: 'Completed', account: 'Savings •••• 3109' },
  { id: 'TX-C887', title: 'Apple Inc Subscription', category: 'Utilities', amount: -14.99, isCredit: false, date: '2026-11-04', status: 'Completed', account: 'Checking •••• 8821' },
  { id: 'TX-C886', title: 'Transfer to Maya Lin', category: 'Transfer', amount: -250.00, isCredit: false, date: '2026-11-04', status: 'Completed', account: 'Checking •••• 8821' },
];

