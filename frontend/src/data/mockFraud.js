export const FRAUD_STATS = {
  totalAlerts: { value: 34, change: -12.8, isPositive: true, period: 'active queue' },
  highRiskTxns: { value: 12, change: 4.2, isPositive: false, period: 'urgency: critical' },
  accountsUnderReview: { value: 8, change: -2, isPositive: true, period: 'frozen / restricted' },
  resolvedAlerts: { value: 142, change: 18.5, isPositive: true, period: 'past 30 days' },
};

export const FRAUD_ALERT_TRENDS = [
  { day: 'Mon', velocityTriggers: 6, geoAnomaly: 4, structuring: 2 },
  { day: 'Tue', velocityTriggers: 8, geoAnomaly: 5, structuring: 3 },
  { day: 'Wed', velocityTriggers: 12, geoAnomaly: 7, structuring: 4 },
  { day: 'Thu', velocityTriggers: 7, geoAnomaly: 3, structuring: 1 },
  { day: 'Fri', velocityTriggers: 14, geoAnomaly: 9, structuring: 5 },
  { day: 'Sat', velocityTriggers: 9, geoAnomaly: 6, structuring: 2 },
  { day: 'Sun', velocityTriggers: 5, geoAnomaly: 2, structuring: 1 },
];

export const FRAUD_RISK_DISTRIBUTION = [
  { name: 'Low Risk Score (0-30)', value: 48, color: '#10B981' },
  { name: 'Medium Risk Score (31-60)', value: 24, color: '#F59E0B' },
  { name: 'High Risk Score (61-85)', value: 16, color: '#F97316' },
  { name: 'Critical Risk Score (86-100)', value: 12, color: '#EF4444' },
];

export const FRAUD_CATEGORIES = [
  { category: 'Rapid Velocity / Bursts', count: 18, severity: 'High' },
  { category: 'Unusual Geo-IP Distance', count: 14, severity: 'Critical' },
  { category: 'Structuring / Smurfing Pattern', count: 9, severity: 'High' },
  { category: 'Known Malicious IP / Tor Exit', count: 7, severity: 'Critical' },
  { category: 'Mismatched Beneficiary Swift', count: 4, severity: 'Medium' },
];

export const FRAUD_SUSPICIOUS_TRANSACTIONS = [
  {
    id: 'TX-FRD-991',
    account: 'FIN-4820-91',
    holder: 'Vance Alexander',
    amount: 48500.00,
    riskScore: 92,
    ruleTriggered: 'Velocity Limit Exceeded: 4 transfers in 3 mins',
    location: 'Lagos, NG (IP: 102.89.34.12)',
    device: 'Unknown Linux Firefox',
    status: 'High Risk',
    timestamp: '14 mins ago',
  },
  {
    id: 'TX-FRD-990',
    account: 'FIN-1099-34',
    holder: 'Astra Holding Corp',
    amount: 198000.00,
    riskScore: 84,
    ruleTriggered: 'High-Value Cross Border Wire without Prior History',
    location: 'Nicosia, CY (IP: 185.220.101.5)',
    device: 'Safari MacOS via Tor Node',
    status: 'High Risk',
    timestamp: '32 mins ago',
  },
  {
    id: 'TX-FRD-989',
    account: 'FIN-7721-02',
    holder: 'Liam O’Connor',
    amount: 9950.00,
    riskScore: 78,
    ruleTriggered: 'Potential Structuring ($9,950 below $10K reporting threshold)',
    location: 'Chicago, US (IP: 24.12.89.102)',
    device: 'Chrome Android 14',
    status: 'Flagged',
    timestamp: '1 hour ago',
  },
  {
    id: 'TX-FRD-988',
    account: 'FIN-3112-88',
    holder: 'Serena Gomez',
    amount: 1450.00,
    riskScore: 64,
    ruleTriggered: 'New Merchant Category: Offshore Crypto Exchange',
    location: 'Miami, US (IP: 73.18.99.201)',
    device: 'Finova Mobile App iOS 18',
    status: 'Under Review',
    timestamp: '2 hours ago',
  },
];

export const FRAUD_HIGH_RISK_ACCOUNTS = [
  { account: 'FIN-4820-91', customer: 'Vance Alexander', openAlerts: 3, cumulativeFlagged: 89400.00, status: 'Restricted' },
  { account: 'FIN-1099-34', customer: 'Astra Holding Corp', openAlerts: 2, cumulativeFlagged: 345000.00, status: 'Temporary Hold' },
  { account: 'FIN-8819-21', customer: 'Vanguard Realty Group', openAlerts: 1, cumulativeFlagged: 850000.00, status: 'Under Investigation' },
  { account: 'FIN-6651-40', customer: 'Phoenix Trading LLC', openAlerts: 2, cumulativeFlagged: 120000.00, status: 'Restricted' },
];

