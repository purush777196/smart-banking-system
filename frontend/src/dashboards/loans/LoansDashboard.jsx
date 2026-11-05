import { useState } from 'react';
import {
  Landmark,
  CheckCircle2,
  Clock,
  DollarSign,
  Calculator,
  Eye,
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  AreaChart,
  Area,
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
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  LOAN_STATS,
  LOAN_DISTRIBUTION,
  LOAN_MONTHLY_APPROVALS,
  LOAN_REPAYMENT_ANALYTICS,
  LOAN_APPLICATIONS,
  LOAN_REPAYMENT_RECORDS,
} from '../../data/mockLoans';

export const LoansDashboard = () => {
  const { addToast } = useToast();
  const [applications, setApplications] = useState(LOAN_APPLICATIONS);
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [isCalculatorModalOpen, setIsCalculatorModalOpen] = useState(false);

  // EMI Calculator inputs
  const [calcPrincipal, setCalcPrincipal] = useState(250000);
  const [calcRate, setCalcRate] = useState(6.5);
  const [calcTenureYears, setCalcTenureYears] = useState(15);

  const calculateEMI = () => {
    const p = parseFloat(calcPrincipal) || 0;
    const r = (parseFloat(calcRate) || 0) / 12 / 100;
    const n = (parseFloat(calcTenureYears) || 0) * 12;
    if (p <= 0 || r <= 0 || n <= 0) return 0;
    return (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
  };

  const handleApproveLoan = (id, applicant, amount) => {
    setApplications((prev) =>
      prev.map((app) => (app.id === id ? { ...app, status: 'Approved' } : app))
    );
    addToast(`Loan ${id} approved for ${applicant} (${formatCurrency(amount)})`, 'success');
    if (selectedApplication) setSelectedApplication(null);
  };

  const handleRejectLoan = (id, applicant) => {
    setApplications((prev) =>
      prev.map((app) => (app.id === id ? { ...app, status: 'Rejected' } : app))
    );
    addToast(`Loan ${id} declined for ${applicant}. Underwriting adverse notice issued.`, 'error');
    if (selectedApplication) setSelectedApplication(null);
  };

  const applicationColumns = [
    { key: 'id', header: 'Application #' },
    {
      key: 'applicant',
      header: 'Applicant Entity',
      render: (a) => <span className="font-bold text-slate-900 dark:text-white">{a}</span>,
    },
    { key: 'loanType', header: 'Loan Product' },
    {
      key: 'amount',
      header: 'Principal',
      align: 'right',
      render: (amt) => <span className="font-bold">{formatCurrency(amt)}</span>,
    },
    { key: 'interestRate', header: 'Rate' },
    {
      key: 'creditScore',
      header: 'FICO',
      align: 'center',
      render: (score) => (
        <span
          className={`font-bold ${
            score >= 750
              ? 'text-emerald-600'
              : score >= 650
              ? 'text-amber-500'
              : 'text-rose-500'
          }`}
        >
          {score}
        </span>
      ),
    },
    {
      key: 'riskRating',
      header: 'Risk',
      align: 'center',
      render: (r) => <StatusBadge status={r} />,
    },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'center',
      sortable: false,
      render: (_, row) => (
        <button
          type="button"
          onClick={() => setSelectedApplication(row)}
          className="p-1.5 rounded-lg text-slate-500 hover:text-emerald-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          <Eye className="w-4 h-4" />
        </button>
      ),
    },
  ];

  const repaymentColumns = [
    { key: 'id', header: 'Invoice Ref' },
    { key: 'loanId', header: 'Facility ID' },
    { key: 'borrower', header: 'Borrower Name' },
    {
      key: 'emiAmount',
      header: 'Monthly EMI',
      align: 'right',
      render: (val) => <span className="font-semibold">{formatCurrency(val)}</span>,
    },
    {
      key: 'dueDate',
      header: 'Due Date',
      render: (d) => formatDate(d),
    },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Credit Underwriting & Loan Portfolio
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              Active Lending Desk
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Officer: Amina Patel • Commercial Real Estate & Residential Mortgages
          </p>
        </div>

        <button
          type="button"
          onClick={() => setIsCalculatorModalOpen(true)}
          className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5 self-start sm:self-auto"
        >
          <Calculator className="w-4 h-4" />
          <span>EMI Schedule Calculator</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Loan Applications"
          value={LOAN_STATS.totalApplications.value}
          change={LOAN_STATS.totalApplications.change}
          isPositive={LOAN_STATS.totalApplications.isPositive}
          period={LOAN_STATS.totalApplications.period}
          icon={Landmark}
        />
        <StatCard
          title="Approved Loans"
          value={LOAN_STATS.approvedLoans.value}
          change={LOAN_STATS.approvedLoans.change}
          isPositive={LOAN_STATS.approvedLoans.isPositive}
          period={LOAN_STATS.approvedLoans.period}
          icon={CheckCircle2}
        />
        <StatCard
          title="Pending Queue"
          value={LOAN_STATS.pendingApplications.value}
          change={LOAN_STATS.pendingApplications.change}
          isPositive={LOAN_STATS.pendingApplications.isPositive}
          period={LOAN_STATS.pendingApplications.period}
          icon={Clock}
        />
        <StatCard
          title="Outstanding Portfolio"
          value={LOAN_STATS.outstandingAmount.value}
          isCurrency
          compact
          change={LOAN_STATS.outstandingAmount.change}
          isPositive={LOAN_STATS.outstandingAmount.isPositive}
          period={LOAN_STATS.outstandingAmount.period}
          icon={DollarSign}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Loan Distribution Pie */}
        <div className="lg:col-span-5">
          <ChartCard
            title="Loan Asset Distribution"
            subtitle="Volume allocation by credit facility category"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={LOAN_DISTRIBUTION}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {LOAN_DISTRIBUTION.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val) => [formatCurrency(val, 'USD', true), '']}
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

        {/* Monthly Approvals vs Requested */}
        <div className="lg:col-span-7">
          <ChartCard
            title="Monthly Loan Approvals ($M)"
            subtitle="Volume requested vs approved by underwriting team"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={LOAN_MONTHLY_APPROVALS}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} unit="M" />
                <Tooltip
                  formatter={(val) => [`$${val}M`, '']}
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Bar dataKey="requested" name="Requested" fill="#94a3b8" radius={[4, 4, 0, 0]} />
                <Bar dataKey="approved" name="Approved" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Repayment Analytics Chart */}
      <ChartCard
        title="Portfolio Repayment Health (%)"
        subtitle="Historical on-time loan installments vs 30-day grace and delinquency"
        height="h-56"
      >
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={LOAN_REPAYMENT_ANALYTICS}>
            <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
            <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} tickLine={false} />
            <YAxis stroke="#94a3b8" fontSize={11} domain={[90, 100]} tickLine={false} unit="%" />
            <Tooltip
              formatter={(val) => [`${val}%`, '']}
              contentStyle={{
                backgroundColor: '#0f172a',
                borderColor: '#334155',
                borderRadius: '0.75rem',
                color: '#fff',
                fontSize: '12px',
              }}
            />
            <Area type="monotone" dataKey="onTime" name="Paid On Time" stroke="#10b981" fill="#10b981" fillOpacity={0.2} strokeWidth={2.5} />
          </AreaChart>
        </ResponsiveContainer>
      </ChartCard>

      {/* Tables Section */}
      <div className="grid grid-cols-1 gap-6">
        <DataTable
          title="Recent Loan Applications"
          subtitle="Awaiting credit committee underwriting decisions"
          columns={applicationColumns}
          data={applications}
        />

        <DataTable
          title="Upcoming Loan Installments & Repayments"
          subtitle="Automated debit and scheduled wire settlements"
          columns={repaymentColumns}
          data={LOAN_REPAYMENT_RECORDS}
        />
      </div>

      {/* Review Application Modal */}
      {selectedApplication && (
        <Modal
          isOpen={!!selectedApplication}
          onClose={() => setSelectedApplication(null)}
          title={`Underwriting Review: ${selectedApplication.id}`}
          subtitle={`Applicant: ${selectedApplication.applicant}`}
          maxWidth="max-w-md"
          footer={
            <div className="flex items-center justify-between w-full">
              <button
                type="button"
                onClick={() =>
                  handleRejectLoan(
                    selectedApplication.id,
                    selectedApplication.applicant
                  )
                }
                className="px-4 py-2 text-xs font-bold text-rose-600 bg-rose-50 hover:bg-rose-100 rounded-xl"
              >
                Reject Loan
              </button>
              <button
                type="button"
                onClick={() =>
                  handleApproveLoan(
                    selectedApplication.id,
                    selectedApplication.applicant,
                    selectedApplication.amount
                  )
                }
                className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
              >
                Approve & Disburse
              </button>
            </div>
          }
        >
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Loan Facility:</span>
              <span className="font-bold">{selectedApplication.loanType}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Requested Principal:</span>
              <span className="font-bold text-emerald-600 text-sm">
                {formatCurrency(selectedApplication.amount)}
              </span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Tenure & Rate:</span>
              <span>
                {selectedApplication.tenure} @ {selectedApplication.interestRate}
              </span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">FICO Score & Assessment:</span>
              <span className="font-bold">{selectedApplication.creditScore} • {selectedApplication.riskRating}</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-slate-400">Current Status:</span>
              <StatusBadge status={selectedApplication.status} />
            </div>
          </div>
        </Modal>
      )}

      {/* EMI Calculator Modal */}
      <Modal
        isOpen={isCalculatorModalOpen}
        onClose={() => setIsCalculatorModalOpen(false)}
        title="Finova Loan EMI Calculator"
        subtitle="Simulate monthly installment schedules across terms and rates"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={() => setIsCalculatorModalOpen(false)}
            className="w-full py-2 text-xs font-bold text-white bg-emerald-600 rounded-xl"
          >
            Close Calculator
          </button>
        }
      >
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Loan Amount ($)
            </label>
            <input
              type="number"
              value={calcPrincipal}
              onChange={(e) => setCalcPrincipal(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                Interest Rate (% p.a.)
              </label>
              <input
                type="number"
                step="0.1"
                value={calcRate}
                onChange={(e) => setCalcRate(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                Tenure (Years)
              </label>
              <input
                type="number"
                value={calcTenureYears}
                onChange={(e) => setCalcTenureYears(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
              />
            </div>
          </div>

          <div className="p-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/60 text-center">
            <span className="text-xs text-emerald-800 dark:text-emerald-300 font-semibold">
              Estimated Monthly Installment (EMI)
            </span>
            <div className="text-2xl font-black text-emerald-700 dark:text-emerald-300 mt-1">
              {formatCurrency(calculateEMI())}
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default LoansDashboard;
