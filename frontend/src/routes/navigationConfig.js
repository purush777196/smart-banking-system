import {
  ShieldAlert,
  Building2,
  UserCheck,
  Briefcase,
  Landmark,
  TrendingUp,
  FileCheck2,
  HeadphonesIcon,
  BarChart3,
} from 'lucide-react';

export const NAVIGATION_GROUPS = [
  {
    groupTitle: 'Executive & Oversight',
    items: [
      {
        id: 'admin',
        title: 'Super Admin',
        path: '/admin/dashboard',
        icon: Landmark,
        badge: 'HQ',
        description: 'System-wide governance, customer volume, and revenue metrics',
        role: 'SUPER_ADMIN',
      },
      {
        id: 'manager',
        title: 'Branch Manager',
        path: '/manager/dashboard',
        icon: Building2,
        badge: 'Branch',
        description: 'Branch performance, staff productivity, and local cash flows',
        role: 'BRANCH_MANAGER',
      },
      {
        id: 'analytics',
        title: 'Banking Analytics',
        path: '/analytics/dashboard',
        icon: BarChart3,
        badge: 'BI',
        description: 'Cross-channel growth, acquisition funnels, and volume trends',
        role: 'ANALYTICS_HEAD',
      },
    ],
  },
  {
    groupTitle: 'Core Operations & Retail',
    items: [
      {
        id: 'customer',
        title: 'Retail Customer',
        path: '/customer/dashboard',
        icon: UserCheck,
        badge: 'Client',
        description: 'Multi-account balances, spending analytics, and money transfers',
        role: 'CUSTOMER',
      },
      {
        id: 'employee',
        title: 'Bank Teller Desk',
        path: '/employee/dashboard',
        icon: Briefcase,
        badge: 'Desk',
        description: 'Over-the-counter customer service, requests, and cash deposits',
        role: 'EMPLOYEE',
      },
      {
        id: 'loans',
        title: 'Loan Management',
        path: '/loans/dashboard',
        icon: Landmark,
        badge: 'Credit',
        description: 'Commercial lines, mortgages, underwriting, and repayments',
        role: 'LOAN_OFFICER',
      },
    ],
  },
  {
    groupTitle: 'Risk, Security & Audit',
    items: [
      {
        id: 'fraud',
        title: 'Fraud Detection',
        path: '/fraud/dashboard',
        icon: ShieldAlert,
        badge: 'SecOps',
        description: 'Rule-based risk triggers, velocity anomalies, and account freezes',
        role: 'FRAUD_ANALYST',
      },
      {
        id: 'audit',
        title: 'Audit & Compliance',
        path: '/audit/dashboard',
        icon: FileCheck2,
        badge: 'Audit',
        description: 'Immutable ledger logs, KYC verifications, and regulatory filings',
        role: 'COMPLIANCE_OFFICER',
      },
    ],
  },
  {
    groupTitle: 'Corporate Services',
    items: [
      {
        id: 'finance',
        title: 'Financial Management',
        path: '/finance/dashboard',
        icon: TrendingUp,
        badge: 'Treasury',
        description: 'P&L statements, interest margins, operating expense control',
        role: 'FINANCE_MANAGER',
      },
      {
        id: 'support',
        title: 'Customer Support',
        path: '/support/dashboard',
        icon: HeadphonesIcon,
        badge: '24/7',
        description: 'SLA ticket queues, customer inquiries, and dispute resolution',
        role: 'SUPPORT_AGENT',
      },
    ],
  },
];

export const ALL_NAVIGATION_ITEMS = NAVIGATION_GROUPS.flatMap((g) => g.items);
