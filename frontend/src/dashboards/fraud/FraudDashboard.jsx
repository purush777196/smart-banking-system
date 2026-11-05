import { useState } from 'react';
import {
  ShieldAlert,
  AlertTriangle,
  Lock,
  CheckCircle2,
  ShieldCheck,
  Ban,
  Info,
} from 'lucide-react';
import {
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import StatCard from '../../components/common/StatCard';
import ChartCard from '../../components/common/ChartCard';
import DataTable from '../../components/tables/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';
import { formatCurrency } from '../../utils/formatters';
import {
  FRAUD_STATS,
  FRAUD_ALERT_TRENDS,
  FRAUD_RISK_DISTRIBUTION,
  FRAUD_CATEGORIES,
  FRAUD_SUSPICIOUS_TRANSACTIONS,
  FRAUD_HIGH_RISK_ACCOUNTS,
} from '../../data/mockFraud';

export const FraudDashboard = () => {
  const { addToast } = useToast();
  const [suspiciousTxns, setSuspiciousTxns] = useState(FRAUD_SUSPICIOUS_TRANSACTIONS);
  const [investigatingTxn, setInvestigatingTxn] = useState(null);

  const handleMarkUnderReview = (id, holder) => {
    setSuspiciousTxns((prev) =>
      prev.map((t) => (t.id === id ? { ...t, status: 'Under Review' } : t))
    );
    addToast(`Account for ${holder} marked under formal SecOps review.`, 'warning');
    if (investigatingTxn) setInvestigatingTxn(null);
  };

  const handleResolveAlert = (id, holder) => {
    setSuspiciousTxns((prev) => prev.filter((t) => t.id !== id));
    addToast(`Security alert on ${id} (${holder}) marked as false-positive & resolved.`, 'success');
    if (investigatingTxn) setInvestigatingTxn(null);
  };

  const handleFreezeAccount = (holder, account) => {
    addToast(`EMERGENCY: Immediate temporary debit freeze placed on ${account} (${holder}).`, 'error');
    if (investigatingTxn) setInvestigatingTxn(null);
  };

  const transactionColumns = [
    { key: 'id', header: 'Alert Ref' },
    { key: 'account', header: 'Account #' },
    {
      key: 'holder',
      header: 'Account Name',
      render: (h) => <span className="font-bold text-slate-900 dark:text-white">{h}</span>,
    },
    {
      key: 'amount',
      header: 'Flagged Amount',
      align: 'right',
      render: (val) => <span className="font-bold text-rose-600">{formatCurrency(val)}</span>,
    },
    {
      key: 'riskScore',
      header: 'Risk Index',
      align: 'center',
      render: (score) => (
        <span
          className={`px-2 py-0.5 rounded-full text-xs font-bold ${
            score >= 85
              ? 'bg-rose-100 dark:bg-rose-950/60 text-rose-700 dark:text-rose-300'
              : 'bg-amber-100 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300'
          }`}
        >
          {score}/100
        </span>
      ),
    },
    { key: 'ruleTriggered', header: 'Rule Triggered' },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    {
      key: 'actions',
      header: 'Actions',
      align: 'center',
      sortable: false,
      render: (_, row) => (
        <button
          type="button"
          onClick={() => setInvestigatingTxn(row)}
          className="px-2.5 py-1 rounded-lg text-xs font-semibold bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition-colors"
        >
          Investigate
        </button>
      ),
    },
  ];

  const highRiskAccountColumns = [
    { key: 'account', header: 'Account Number' },
    {
      key: 'customer',
      header: 'Account Holder',
      render: (c) => <span className="font-bold">{c}</span>,
    },
    {
      key: 'openAlerts',
      header: 'Open Triggers',
      align: 'center',
      render: (a) => (
        <span className="font-bold text-rose-600">{a}</span>
      ),
    },
    {
      key: 'cumulativeFlagged',
      header: 'Cumulative Exposure',
      align: 'right',
      render: (val) => formatCurrency(val),
    },
    {
      key: 'status',
      header: 'SecOps Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Banner & Disclaimer */}
      <div className="p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm space-y-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-2xl bg-rose-50 dark:bg-rose-950/60 text-rose-600 dark:text-rose-400 border border-rose-200 dark:border-rose-800">
              <ShieldAlert className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                  Fraud Detection & AML SecOps Center
                </h2>
                <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-rose-100 dark:bg-rose-950 text-rose-700 dark:text-rose-300">
                  DEFCON 2 Monitoring
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Lead Analyst: Julian Chen • Real-time rule-based AML & Velocity Trigger Engine
              </p>
            </div>
          </div>
        </div>

        {/* Rule Engine Notice (Required) */}
        <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60 flex items-start gap-2.5 text-xs text-slate-600 dark:text-slate-300">
          <Info className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
          <span>
            <strong>Deterministic Rules Engine:</strong> Telemetry is evaluated using explicit heuristic filters (velocity spikes, geo-IP incongruence, KYC threshold anomalies). Demo figures demonstrate financial crime prevention workflows.
          </span>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Active Fraud Alerts"
          value={FRAUD_STATS.totalAlerts.value}
          change={FRAUD_STATS.totalAlerts.change}
          isPositive={FRAUD_STATS.totalAlerts.isPositive}
          period={FRAUD_STATS.totalAlerts.period}
          icon={ShieldAlert}
        />
        <StatCard
          title="High Risk Txns"
          value={FRAUD_STATS.highRiskTxns.value}
          change={FRAUD_STATS.highRiskTxns.change}
          isPositive={FRAUD_STATS.highRiskTxns.isPositive}
          period={FRAUD_STATS.highRiskTxns.period}
          icon={AlertTriangle}
        />
        <StatCard
          title="Under Review"
          value={FRAUD_STATS.accountsUnderReview.value}
          change={FRAUD_STATS.accountsUnderReview.change}
          isPositive={FRAUD_STATS.accountsUnderReview.isPositive}
          period={FRAUD_STATS.accountsUnderReview.period}
          icon={Lock}
        />
        <StatCard
          title="Resolved Alerts"
          value={FRAUD_STATS.resolvedAlerts.value}
          change={FRAUD_STATS.resolvedAlerts.change}
          isPositive={FRAUD_STATS.resolvedAlerts.isPositive}
          period={FRAUD_STATS.resolvedAlerts.period}
          icon={CheckCircle2}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Fraud Alert Trends Line Chart */}
        <div className="lg:col-span-7">
          <ChartCard
            title="Weekly Alert Anomaly Trends"
            subtitle="Incident volume grouped by heuristic detection trigger"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={FRAUD_ALERT_TRENDS}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="day" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Line type="monotone" dataKey="velocityTriggers" name="Velocity Bursts" stroke="#ef4444" strokeWidth={2.5} />
                <Line type="monotone" dataKey="geoAnomaly" name="Geo-IP Anomaly" stroke="#f59e0b" strokeWidth={2} />
                <Line type="monotone" dataKey="structuring" name="Structuring Patterns" stroke="#3b82f6" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>

        {/* Risk Distribution Donut */}
        <div className="lg:col-span-5">
          <ChartCard
            title="Risk Score Severity Distribution"
            subtitle="Active accounts categorized by risk threshold tier"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={FRAUD_RISK_DISTRIBUTION}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {FRAUD_RISK_DISTRIBUTION.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val) => [`${val} Accounts`, '']}
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Suspicious Categories Breakdown */}
      <ChartCard
        title="Suspicious Trigger Classifications"
        subtitle="Distribution of flagged transaction triggers"
        height="h-56"
      >
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={FRAUD_CATEGORIES} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
            <XAxis type="number" stroke="#94a3b8" fontSize={11} />
            <YAxis dataKey="category" type="category" stroke="#94a3b8" fontSize={10} width={170} tickLine={false} />
            <Tooltip
              contentStyle={{
                backgroundColor: '#0f172a',
                borderColor: '#334155',
                borderRadius: '0.75rem',
                color: '#fff',
                fontSize: '12px',
              }}
            />
            <Bar dataKey="count" name="Trigger Count" fill="#ef4444" radius={[0, 4, 4, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>

      {/* Tables Section */}
      <div className="grid grid-cols-1 gap-6">
        <DataTable
          title="Suspicious Transactions Queue"
          subtitle="Real-time AML rule triggers awaiting SecOps triage"
          columns={transactionColumns}
          data={suspiciousTxns}
        />

        <DataTable
          title="High Risk Accounts Under Watch"
          subtitle="Customer profiles with multiple open risk infractions"
          columns={highRiskAccountColumns}
          data={FRAUD_HIGH_RISK_ACCOUNTS}
        />
      </div>

      {/* Investigate Modal */}
      {investigatingTxn && (
        <Modal
          isOpen={!!investigatingTxn}
          onClose={() => setInvestigatingTxn(null)}
          title={`SecOps Investigation: ${investigatingTxn.id}`}
          subtitle={`Account: ${investigatingTxn.account} • ${investigatingTxn.holder}`}
          maxWidth="max-w-lg"
          footer={
            <div className="flex flex-wrap items-center justify-between w-full gap-2">
              <button
                type="button"
                onClick={() =>
                  handleFreezeAccount(
                    investigatingTxn.holder,
                    investigatingTxn.account
                  )
                }
                className="px-3.5 py-2 text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-xl flex items-center gap-1.5"
              >
                <Ban className="w-3.5 h-3.5" />
                <span>Freeze Account</span>
              </button>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() =>
                    handleMarkUnderReview(
                      investigatingTxn.id,
                      investigatingTxn.holder
                    )
                  }
                  className="px-3 py-2 text-xs font-bold text-amber-700 bg-amber-50 hover:bg-amber-100 rounded-xl"
                >
                  Mark Under Review
                </button>
                <button
                  type="button"
                  onClick={() =>
                    handleResolveAlert(
                      investigatingTxn.id,
                      investigatingTxn.holder
                    )
                  }
                  className="px-3.5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl flex items-center gap-1.5"
                >
                  <ShieldCheck className="w-3.5 h-3.5" />
                  <span>Resolve Alert</span>
                </button>
              </div>
            </div>
          }
        >
          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/60">
              <div className="text-2xs font-bold uppercase text-rose-600 tracking-wider">
                Triggered Rule
              </div>
              <div className="font-bold text-slate-900 dark:text-white mt-0.5">
                {investigatingTxn.ruleTriggered}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2 py-1 border-b border-slate-100 dark:border-slate-800">
              <div>
                <span className="text-slate-400 block">Flagged Value</span>
                <span className="text-base font-black text-rose-600">
                  {formatCurrency(investigatingTxn.amount)}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block">Calculated Risk Index</span>
                <span className="text-base font-black text-slate-900 dark:text-white">
                  {investigatingTxn.riskScore} / 100
                </span>
              </div>
            </div>

            <div className="space-y-1.5 text-xs text-slate-600 dark:text-slate-300">
              <div className="flex justify-between">
                <span className="text-slate-400">Geolocation:</span>
                <span className="font-mono">{investigatingTxn.location}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Device Fingerprint:</span>
                <span>{investigatingTxn.device}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Time Elapsed:</span>
                <span>{investigatingTxn.timestamp}</span>
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default FraudDashboard;
