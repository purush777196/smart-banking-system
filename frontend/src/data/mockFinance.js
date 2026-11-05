export const FINANCE_STATS = {
  totalRevenue: { value: 148200000, change: 14.8, isPositive: true, period: 'vs prior year' },
  interestIncome: { value: 96400000, change: 18.2, isPositive: true, period: '65.0% of total' },
  operatingExpenses: { value: 42100000, change: 4.1, isPositive: false, period: 'efficiency ratio 28.4%' },
  netProfit: { value: 54100000, change: 22.4, isPositive: true, period: 'EBITDA margin 36.5%' },
};

export const FINANCE_REVENUE_EXPENSES = [
  { month: 'May', revenue: 22.4, expenses: 7.8, netProfit: 14.6 },
  { month: 'Jun', revenue: 23.9, expenses: 8.1, netProfit: 15.8 },
  { month: 'Jul', revenue: 24.5, expenses: 8.4, netProfit: 16.1 },
  { month: 'Aug', revenue: 25.2, expenses: 8.2, netProfit: 17.0 },
  { month: 'Sep', revenue: 25.8, expenses: 8.5, netProfit: 17.3 },
  { month: 'Oct', revenue: 26.4, expenses: 9.1, netProfit: 17.3 },
];

export const FINANCE_INCOME_DISTRIBUTION = [
  { name: 'Commercial & Retail Interest', value: 96400000, color: '#10B981' },
  { name: 'Treasury & Market Trading', value: 24200000, color: '#047857' },
  { name: 'Wealth & Asset Management Fees', value: 16800000, color: '#34D399' },
  { name: 'Interbank & Card Interchange', value: 10800000, color: '#0F172A' },
];

export const FINANCE_REVENUE_BREAKDOWN = [
  { division: 'Retail Banking Products', q3Actual: 48200000, q3Budget: 44000000, variance: '+9.5%', status: 'Exceeded' },
  { division: 'Corporate & Syndicated Loans', q3Actual: 52100000, q3Budget: 49500000, variance: '+5.3%', status: 'Exceeded' },
  { division: 'Global Treasury Services', q3Actual: 24800000, q3Budget: 25000000, variance: '-0.8%', status: 'On Target' },
  { division: 'Finova Private Wealth', q3Actual: 16200000, q3Budget: 15000000, variance: '+8.0%', status: 'Exceeded' },
  { division: 'Merchant Payment Gateway', q3Actual: 6900000, q3Budget: 6500000, variance: '+6.1%', status: 'Exceeded' },
];

export const FINANCE_TRANSACTIONS = [
  { id: 'FIN-TX-901', ledgerCode: 'L-4010', description: 'FedWire Settlement Tranche #84', category: 'Interbank Settlement', debit: 0, credit: 18500000.00, date: '2026-11-04' },
  { id: 'FIN-TX-902', ledgerCode: 'L-5120', description: 'Enterprise Data Center Lease Q4', category: 'Operating Expense', debit: 1250000.00, credit: 0, date: '2026-11-03' },
  { id: 'FIN-TX-903', ledgerCode: 'L-3020', description: 'Syndicated Term Loan Coupon Payout', category: 'Interest Income', debit: 0, credit: 4890000.00, date: '2026-11-03' },
  { id: 'FIN-TX-904', ledgerCode: 'L-5410', description: 'Visa / Mastercard Interchange Settlement', category: 'Interchange Fee', debit: 340000.00, credit: 0, date: '2026-11-02' },
  { id: 'FIN-TX-905', ledgerCode: 'L-4800', description: 'Corporate Tax Provision Allocation', category: 'Tax Liability', debit: 3100000.00, credit: 0, date: '2026-11-01' },
];

