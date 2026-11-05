import { useState } from 'react';
import {
  Users,
  Wallet,
  ArrowDownLeft,
  ArrowUpRight,
  TrendingUp,
  Building,
  FileSpreadsheet,
  ShieldCheck,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  LineChart,
  Line,
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
import { formatCurrency, formatDateTime } from '../../utils/formatters';
import {
  ADMIN_STATS,
  ADMIN_REVENUE_TRENDS,
  ADMIN_VOLUME_DATA,
  ADMIN_CUSTOMER_GROWTH,
  ADMIN_RECENT_TRANSACTIONS,
  ADMIN_TOP_BRANCHES,
} from '../../data/mockAdmin';

export const AdminDashboard = () => {
  const { addToast } = useToast();
  const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
  const [isBranchModalOpen, setIsBranchModalOpen] = useState(false);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [selectedTxn, setSelectedTxn] = useState(null);

  // New Branch Form State
  const [branchName, setBranchName] = useState('');
  const [branchManager, setBranchManager] = useState('');

  const handleCreateBranch = (e) => {
    e.preventDefault();
    if (!branchName.trim()) return;
    addToast(`Branch "${branchName}" successfully provisioned to core network.`, 'success');
    setBranchName('');
    setBranchManager('');
    setIsBranchModalOpen(false);
  };

  const handleExportAuditReport = () => {
    addToast('Generating encrypted enterprise summary report (PDF/CSV)...', 'info');
    setTimeout(() => {
      addToast('Executive bank report downloaded successfully.', 'success');
      setIsReportModalOpen(false);
    }, 1200);
  };

  const transactionColumns = [
    { key: 'id', header: 'Txn ID', sortable: true },
    { key: 'customer', header: 'Customer', sortable: true },
    { key: 'type', header: 'Type' },
    {
      key: 'amount',
      header: 'Amount',
      align: 'right',
      sortable: true,
      render: (val) => (
        <span className="font-semibold text-slate-900 dark:text-white">
          {formatCurrency(val)}
        </span>
      ),
    },
    { key: 'branch', header: 'Origin Branch' },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (status) => <StatusBadge status={status} />,
    },
    {
      key: 'date',
      header: 'Timestamp',
      render: (d) => formatDateTime(d),
    },
  ];

  const branchColumns = [
    { key: 'id', header: 'Branch Code' },
    {
      key: 'name',
      header: 'Branch Location',
      render: (name) => <span className="font-bold text-slate-900 dark:text-white">{name}</span>,
    },
    { key: 'manager', header: 'Branch Manager' },
    {
      key: 'deposits',
      header: 'Total Deposits',
      align: 'right',
      render: (val) => formatCurrency(val, 'USD', true),
    },
    {
      key: 'revenue',
      header: 'YTD Revenue',
      align: 'right',
      render: (val) => formatCurrency(val, 'USD', true),
    },
    {
      key: 'healthScore',
      header: 'Health Score',
      align: 'center',
      render: (score) => (
        <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300">
          {score}%
        </span>
      ),
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
      {/* Top Banner / Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-gradient-to-r from-[#0b132b] via-[#102042] to-[#047857] text-white shadow-xl relative overflow-hidden">
        <div className="relative z-10">
          <div className="inline-flex items-center gap-2 px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-300 text-xs font-bold mb-2 border border-emerald-400/30">
            <ShieldCheck className="w-3.5 h-3.5" />
            Executive Oversight
          </div>
          <h2 className="text-xl sm:text-2xl font-black">
            Finova Global Bank Command Center
          </h2>
          <p className="text-xs sm:text-sm text-slate-300 mt-1 max-w-xl">
            Real-time telemetry across all 5 international branches, liquidity reserves, and high-value institutional settlement rails.
          </p>
        </div>

        <div className="relative z-10 flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={() => setIsCustomerModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-white/10 hover:bg-white/20 text-white border border-white/20 transition-all shadow-xs flex items-center gap-1.5"
          >
            <Users className="w-4 h-4" />
            <span>Manage Customers</span>
          </button>
          <button
            type="button"
            onClick={() => setIsBranchModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-white/10 hover:bg-white/20 text-white border border-white/20 transition-all shadow-xs flex items-center gap-1.5"
          >
            <Building className="w-4 h-4" />
            <span>Manage Branches</span>
          </button>
          <button
            type="button"
            onClick={() => setIsReportModalOpen(true)}
            className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-500 hover:bg-emerald-600 text-white transition-all shadow-md shadow-emerald-500/20 flex items-center gap-1.5"
          >
            <FileSpreadsheet className="w-4 h-4" />
            <span>View Reports</span>
          </button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <StatCard
          title="Total Customers"
          value={ADMIN_STATS.totalCustomers.value}
          change={ADMIN_STATS.totalCustomers.change}
          isPositive={ADMIN_STATS.totalCustomers.isPositive}
          period={ADMIN_STATS.totalCustomers.period}
          icon={Users}
        />
        <StatCard
          title="Total Accounts"
          value={ADMIN_STATS.totalAccounts.value}
          change={ADMIN_STATS.totalAccounts.change}
          isPositive={ADMIN_STATS.totalAccounts.isPositive}
          period={ADMIN_STATS.totalAccounts.period}
          icon={Wallet}
        />
        <StatCard
          title="Total Deposits"
          value={ADMIN_STATS.totalDeposits.value}
          isCurrency
          compact
          change={ADMIN_STATS.totalDeposits.change}
          isPositive={ADMIN_STATS.totalDeposits.isPositive}
          period={ADMIN_STATS.totalDeposits.period}
          icon={ArrowDownLeft}
        />
        <StatCard
          title="Total Withdrawals"
          value={ADMIN_STATS.totalWithdrawals.value}
          isCurrency
          compact
          change={ADMIN_STATS.totalWithdrawals.change}
          isPositive={ADMIN_STATS.totalWithdrawals.isPositive}
          period={ADMIN_STATS.totalWithdrawals.period}
          icon={ArrowUpRight}
        />
        <StatCard
          title="Bank Revenue"
          value={ADMIN_STATS.totalRevenue.value}
          isCurrency
          compact
          change={ADMIN_STATS.totalRevenue.change}
          isPositive={ADMIN_STATS.totalRevenue.isPositive}
          period={ADMIN_STATS.totalRevenue.period}
          icon={TrendingUp}
        />
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Monthly Revenue Chart */}
        <div className="lg:col-span-7">
          <ChartCard
            title="Monthly Revenue & Net Profit"
            subtitle="Historical gross revenue vs operational expenditure (in $ Millions)"
            height="h-72"
          >
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={ADMIN_REVENUE_TRENDS}>
                <defs>
                  <linearGradient id="revenueGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                  <linearGradient id="netGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0b132b" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#0b132b" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} unit="M" />
                <Tooltip
                  formatter={(value) => [`$${value}M`, '']}
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Area type="monotone" dataKey="revenue" name="Gross Revenue" stroke="#10b981" strokeWidth={2.5} fillOpacity={1} fill="url(#revenueGrad)" />
                <Area type="monotone" dataKey="net" name="Net Margin" stroke="#38bdf8" strokeWidth={2} fillOpacity={1} fill="url(#netGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>

        {/* Transaction Volume Chart */}
        <div className="lg:col-span-5">
          <ChartCard
            title="Daily Transaction Volumes"
            subtitle="Internal clearing, wire settlements & ATM loads"
            height="h-72"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={ADMIN_VOLUME_DATA}>
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
                <Bar dataKey="internal" name="Internal Clearing" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="wires" name="Interbank Wires" fill="#047857" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Customer Growth Chart */}
      <ChartCard
        title="Institutional & Retail Customer Acquisition"
        subtitle="Cumulative portfolio growth over previous 6 quarters"
        height="h-56"
      >
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={ADMIN_CUSTOMER_GROWTH}>
            <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
            <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} tickLine={false} />
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
            <Line type="monotone" dataKey="retail" name="Retail Clients" stroke="#10b981" strokeWidth={2.5} dot={{ r: 4 }} activeDot={{ r: 6 }} />
            <Line type="monotone" dataKey="corporate" name="Corporate Accounts" stroke="#6366f1" strokeWidth={2.5} dot={{ r: 4 }} />
          </LineChart>
        </ResponsiveContainer>
      </ChartCard>

      {/* Tables Section */}
      <div className="grid grid-cols-1 gap-6">
        {/* Recent Transactions */}
        <DataTable
          title="Recent System Transactions"
          subtitle="Real-time multi-branch ledger activity"
          columns={transactionColumns}
          data={ADMIN_RECENT_TRANSACTIONS}
          onRowClick={(row) => setSelectedTxn(row)}
        />

        {/* Top Performing Branches */}
        <DataTable
          title="Top Performing Regional Branches"
          subtitle="Branch network deposit quotas and operations ranking"
          columns={branchColumns}
          data={ADMIN_TOP_BRANCHES}
        />
      </div>

      {/* Manage Customers Modal */}
      <Modal
        isOpen={isCustomerModalOpen}
        onClose={() => setIsCustomerModalOpen(false)}
        title="Global Customer Management"
        subtitle="48,290 Active Retail & Corporate Customers registered in core database"
        maxWidth="max-w-2xl"
        footer={
          <button
            type="button"
            onClick={() => setIsCustomerModalOpen(false)}
            className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Close Directory
          </button>
        }
      >
        <div className="space-y-3">
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Super Administrator controls allow bulk tier upgrades, compliance audits, and sanction screenings across customer accounts.
          </p>
          <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 space-y-2">
            <div className="flex items-center justify-between text-xs font-bold">
              <span>Customer Verification Rating</span>
              <span className="text-emerald-600">98.4% Compliant</span>
            </div>
            <div className="w-full bg-slate-200 dark:bg-slate-700 h-2 rounded-full overflow-hidden">
              <div className="bg-emerald-500 h-full w-[98.4%]" />
            </div>
          </div>
          <button
            type="button"
            onClick={() => {
              addToast('Full customer directory CSV generated and scheduled for transmission', 'info');
              setIsCustomerModalOpen(false);
            }}
            className="w-full py-2.5 rounded-xl border border-dashed border-emerald-500 text-emerald-600 dark:text-emerald-400 font-bold text-xs hover:bg-emerald-50 dark:hover:bg-emerald-950/30 transition-colors"
          >
            Export Complete Master Customer Ledger (.CSV)
          </button>
        </div>
      </Modal>

      {/* Manage Branches Modal */}
      <Modal
        isOpen={isBranchModalOpen}
        onClose={() => setIsBranchModalOpen(false)}
        title="Provision New Regional Branch"
        subtitle="Add a new branch node to Finova interbank routing"
        maxWidth="max-w-md"
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsBranchModalOpen(false)}
              className="px-4 py-2 text-xs text-slate-600 dark:text-slate-300 hover:bg-slate-100 rounded-xl"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleCreateBranch}
              className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
            >
              Provision Node
            </button>
          </>
        }
      >
        <form onSubmit={handleCreateBranch} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Branch Name & Location
            </label>
            <input
              type="text"
              value={branchName}
              onChange={(e) => setBranchName(e.target.value)}
              placeholder="e.g. Frankfurt Financial Gateway #106"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Appointed Branch General Manager
            </label>
            <input
              type="text"
              value={branchManager}
              onChange={(e) => setBranchManager(e.target.value)}
              placeholder="e.g. Julian Sommer"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </form>
      </Modal>

      {/* Reports Modal */}
      <Modal
        isOpen={isReportModalOpen}
        onClose={() => setIsReportModalOpen(false)}
        title="Executive Financial Reports"
        subtitle="Consolidated regulatory statements and profitability models"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleExportAuditReport}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs"
          >
            Download Q3 Executive Compilation
          </button>
        }
      >
        <div className="space-y-2.5 text-xs text-slate-600 dark:text-slate-300">
          <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <span className="font-semibold">GAAP & IFRS Financial Balance</span>
            <span className="text-emerald-600 font-bold">Verified</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <span className="font-semibold">Central Bank Reserve Ratio (14.2%)</span>
            <span className="text-emerald-600 font-bold">Compliant</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <span className="font-semibold">Basel III Capital Adequacy</span>
            <span className="text-emerald-600 font-bold">Grade A</span>
          </div>
        </div>
      </Modal>

      {/* Transaction Details Modal */}
      {selectedTxn && (
        <Modal
          isOpen={!!selectedTxn}
          onClose={() => setSelectedTxn(null)}
          title={`Transaction Audit: ${selectedTxn.id}`}
          subtitle={`Ledger entry registered at ${selectedTxn.branch}`}
          maxWidth="max-w-md"
          footer={
            <button
              type="button"
              onClick={() => setSelectedTxn(null)}
              className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
            >
              Done
            </button>
          }
        >
          <div className="space-y-3 text-xs">
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Customer:</span>
              <span className="font-bold text-slate-900 dark:text-white">{selectedTxn.customer}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Account:</span>
              <span className="font-mono">{selectedTxn.account}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Amount:</span>
              <span className="font-bold text-emerald-600 text-sm">{formatCurrency(selectedTxn.amount)}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Transfer Type:</span>
              <span>{selectedTxn.type}</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-slate-400">Status:</span>
              <StatusBadge status={selectedTxn.status} />
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default AdminDashboard;
