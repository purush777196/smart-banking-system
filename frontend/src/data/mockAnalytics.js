export const ANALYTICS_DATA_BY_PERIOD = {
  today: {
    kpis: {
      volume: { value: 148200000, change: 4.2, isPositive: true, period: 'vs yesterday' },
      growth: { value: 38, change: 12.0, isPositive: true, period: 'new clients' },
      revenue: { value: 2450000, change: 6.8, isPositive: true, period: 'today' },
      portfolio: { value: 182000000, change: 0.4, isPositive: true, period: 'active balance' },
    },
    trends: [
      { time: '08:00', volume: 12.4, transactions: 1400 },
      { time: '10:00', volume: 28.6, transactions: 3800 },
      { time: '12:00', volume: 42.1, transactions: 5200 },
      { time: '14:00', volume: 36.8, transactions: 4600 },
      { time: '16:00', volume: 22.5, transactions: 2900 },
      { time: '18:00', volume: 5.8, transactions: 1100 },
    ],
  },
  '7d': {
    kpis: {
      volume: { value: 980000000, change: 8.4, isPositive: true, period: 'vs last 7 days' },
      growth: { value: 290, change: 15.2, isPositive: true, period: 'new clients' },
      revenue: { value: 18400000, change: 9.1, isPositive: true, period: '7-day sum' },
      portfolio: { value: 182400000, change: 1.2, isPositive: true, period: 'active balance' },
    },
    trends: [
      { time: 'Mon', volume: 135.2, transactions: 14200 },
      { time: 'Tue', volume: 142.8, transactions: 15800 },
      { time: 'Wed', volume: 158.4, transactions: 17200 },
      { time: 'Thu', volume: 149.1, transactions: 16100 },
      { time: 'Fri', volume: 182.5, transactions: 20400 },
      { time: 'Sat', volume: 112.0, transactions: 11800 },
      { time: 'Sun', volume: 100.0, transactions: 9900 },
    ],
  },
  '30d': {
    kpis: {
      volume: { value: 4820000000, change: 14.8, isPositive: true, period: 'vs last month' },
      growth: { value: 1420, change: 18.6, isPositive: true, period: 'new clients' },
      revenue: { value: 84250000, change: 16.4, isPositive: true, period: 'monthly revenue' },
      portfolio: { value: 185000000, change: 5.8, isPositive: true, period: 'loan portfolio' },
    },
    trends: [
      { time: 'Week 1', volume: 1120, transactions: 98000 },
      { time: 'Week 2', volume: 1180, transactions: 104000 },
      { time: 'Week 3', volume: 1240, transactions: 112000 },
      { time: 'Week 4', volume: 1280, transactions: 116000 },
    ],
  },
  '12m': {
    kpis: {
      volume: { value: 58400000000, change: 24.5, isPositive: true, period: 'vs prior year' },
      growth: { value: 18400, change: 28.2, isPositive: true, period: 'net new accounts' },
      revenue: { value: 940000000, change: 21.0, isPositive: true, period: 'annual revenue' },
      portfolio: { value: 210000000, change: 14.2, isPositive: true, period: 'portfolio growth' },
    },
    trends: [
      { time: 'Nov', volume: 4400, transactions: 380000 },
      { time: 'Jan', volume: 4600, transactions: 400000 },
      { time: 'Mar', volume: 4900, transactions: 420000 },
      { time: 'May', volume: 5100, transactions: 440000 },
      { time: 'Jul', volume: 5400, transactions: 470000 },
      { time: 'Sep', volume: 5600, transactions: 490000 },
      { time: 'Oct', volume: 5840, transactions: 510000 },
    ],
  },
};

export const ANALYTICS_ACQUISITION_CHANNELS = [
  { channel: 'Mobile App Store & Direct', count: 48, color: '#10B981' },
  { channel: 'Branch Walk-In & Advisory', count: 24, color: '#047857' },
  { channel: 'Corporate Payroll Partnerships', count: 18, color: '#34D399' },
  { channel: 'Fintech Referral Network', count: 10, color: '#0F172A' },
];

export const ANALYTICS_BRANCH_COMPARISON = [
  { branch: 'Downtown Financial HQ', volume: 1450, growth: 18.2, nps: 78 },
  { branch: 'Midtown Corporate', volume: 1180, growth: 14.5, nps: 74 },
  { branch: 'Silicon Bay Tech', volume: 1090, growth: 22.4, nps: 82 },
  { branch: 'Chicago Loop', volume: 880, growth: 11.2, nps: 69 },
  { branch: 'London Gateway', volume: 720, growth: 16.8, nps: 76 },
];

export const ANALYTICS_TRANSACTION_DISTRIBUTION = [
  { type: 'Commercial Real-Time Clearing', percentage: 42, color: '#10B981' },
  { type: 'Card Network Interchange (POS/E-Com)', percentage: 28, color: '#047857' },
  { type: 'Automated Clearing House (ACH)', percentage: 18, color: '#34D399' },
  { type: 'International Swift Wires', percentage: 12, color: '#0F172A' },
];

