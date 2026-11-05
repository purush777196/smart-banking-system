import { useState } from 'react';
import {
  TrendingUp,
  Percent,
  Receipt,
  Scale,
  FileSpreadsheet,
  Download,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
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
  FINANCE_STATS,
  FINANCE_REVENUE_EXPENSES,
  FINANCE_INCOME_DISTRIBUTION,
  FINANCE_REVENUE_BREAKDOWN,
  FINANCE_TRANSACTIONS,
} from '../../data/mockFinance';

export const FinanceDashboard = () => {
  const { addToast } = useToast();
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);

  const handleExportFinancials = () => {
    addToast('Compiling General Ledger (GL) & Financial Statements...', 'info');
    setTimeout(() => {
      addToast('Exported Finova_GL_Q3_Audited.xlsx successfully.', 'success');
    }, 1200);
  };

  const revenueBreakdownColumns = [
    { key: 'division', header: 'Operating Division' },
    {
      key: 'q3Actual',
      header: 'Q3 Actual',
      align: 'right',
      render: (val) => <span className="font-bold">{formatCurrency(val, 'USD', true)}</span>,
    },
    {
      key: 'q3Budget',
      header: 'Q3 Budget',
      align: 'right',
      render: (val) => formatCurrency(val, 'USD', true),
    },
    {
      key: 'variance',
      header: 'Variance',
      align: 'center',
      render: (v) => (
        <span
          className={`font-bold text-xs ${
            v.startsWith('+') ? 'text-emerald-600' : 'text-rose-600'
          }`}
        >
          {v}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Target Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
  ];

  const transactionColumns = [
    { key: 'id', header: 'Journal ID' },
    { key: 'ledgerCode', header: 'GL Code' },
    { key: 'description', header: 'Description' },
    { key: 'category', header: 'Accounting Head' },
    {
      key: 'debit',
      header: 'Debit ($)',
      align: 'right',
      render: (val) => (val > 0 ? formatCurrency(val) : '—'),
    },
    {
      key: 'credit',
      header: 'Credit ($)',
      align: 'right',
      render: (val) => (val > 0 ? <span className="text-emerald-600 font-bold">{formatCurrency(val)}</span> : '—'),
    },
    {
      key: 'date',
      header: 'Posting Date',
      render: (d) => formatDate(d),
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Corporate Treasury & Financial Controller
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              Q3 Fiscal Balanced
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Controller: Claire Beaumont • Statutory Reporting, GL Balancing & Asset-Liability Management
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => setIsReportModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition-colors flex items-center gap-1.5"
          >
            <FileSpreadsheet className="w-4 h-4 text-emerald-600" />
            <span>P&L Statements</span>
          </button>
          <button
            type="button"
            onClick={handleExportFinancials}
            className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
          >
            <Download className="w-4 h-4" />
            <span>Export Financials</span>
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Bank Revenue"
          value={FINANCE_STATS.totalRevenue.value}
          isCurrency
          compact
          change={FINANCE_STATS.totalRevenue.change}
          isPositive={FINANCE_STATS.totalRevenue.isPositive}
          period={FINANCE_STATS.totalRevenue.period}
          icon={TrendingUp}
        />
        <StatCard
          title="Interest Income"
          value={FINANCE_STATS.interestIncome.value}
          isCurrency
          compact
          change={FINANCE_STATS.interestIncome.change}
          isPositive={FINANCE_STATS.interestIncome.isPositive}
          period={FINANCE_STATS.interestIncome.period}
          icon={Percent}
        />
        <StatCard
          title="Operating Expenses"
          value={FINANCE_STATS.operatingExpenses.value}
          isCurrency
          compact
          change={FINANCE_STATS.operatingExpenses.change}
          isPositive={FINANCE_STATS.operatingExpenses.isPositive}
          period={FINANCE_STATS.operatingExpenses.period}
          icon={Receipt}
        />
        <StatCard
          title="Net Profit (EBITDA)"
          value={FINANCE_STATS.netProfit.value}
          isCurrency
          compact
          change={FINANCE_STATS.netProfit.change}
          isPositive={FINANCE_STATS.netProfit.isPositive}
          period={FINANCE_STATS.netProfit.period}
          icon={Scale}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Revenue vs Expenses Comparative */}
        <div className="lg:col-span-7">
          <ChartCard
            title="Monthly Revenue vs OPEX Comparison"
            subtitle="Gross earnings versus operating burn rate ($ Millions)"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={FINANCE_REVENUE_EXPENSES}>
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
                <Bar dataKey="revenue" name="Total Revenue" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="expenses" name="Operating Expenses" fill="#f43f5e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>

        {/* Income Distribution Donut */}
        <div className="lg:col-span-5">
          <ChartCard
            title="Income Head Distribution"
            subtitle="Commercial, treasury, asset management, and interchange revenue"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={FINANCE_INCOME_DISTRIBUTION}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {FINANCE_INCOME_DISTRIBUTION.map((entry, index) => (
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
      </div>

      {/* Monthly Financial Margin Trend Area */}
      <ChartCard
        title="Net Operating Margin Realization"
        subtitle="Historical monthly net bottom-line profit curve ($ Millions)"
        height="h-56"
      >
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={FINANCE_REVENUE_EXPENSES}>
            <defs>
              <linearGradient id="profitGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
              </linearGradient>
            </defs>
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
            <Area type="monotone" dataKey="netProfit" name="Net Profit" stroke="#10b981" strokeWidth={2.5} fillOpacity={1} fill="url(#profitGrad)" />
          </AreaChart>
        </ResponsiveContainer>
      </ChartCard>

      {/* Tables Section */}
      <div className="grid grid-cols-1 gap-6">
        <DataTable
          title="Revenue Breakdown by Business Unit"
          subtitle="Q3 variance vs executive financial forecast"
          columns={revenueBreakdownColumns}
          data={FINANCE_REVENUE_BREAKDOWN}
        />

        <DataTable
          title="Corporate General Ledger Journal"
          subtitle="Recent debits and credits posted to core Finova ledger"
          columns={transactionColumns}
          data={FINANCE_TRANSACTIONS}
        />
      </div>

      {/* P&L Modal */}
      <Modal
        isOpen={isReportModalOpen}
        onClose={() => setIsReportModalOpen(false)}
        title="Financial Profit & Loss Statement (Q3)"
        subtitle="Consolidated IFRS & GAAP statement of comprehensive income"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={() => setIsReportModalOpen(false)}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 rounded-xl"
          >
            Close Statement
          </button>
        }
      >
        <div className="space-y-2.5 text-xs text-slate-700 dark:text-slate-300">
          <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
            <span>Interest Income</span>
            <span className="font-bold">$96,400,000.00</span>
          </div>
          <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
            <span>Non-Interest Fees & Interchange</span>
            <span className="font-bold">$51,800,000.00</span>
          </div>
          <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800 text-rose-600 font-semibold">
            <span>Total Operating Expenditure</span>
            <span>($42,100,000.00)</span>
          </div>
          <div className="flex justify-between py-2 text-sm font-black text-emerald-600 pt-2 border-t-2 border-slate-200 dark:border-slate-700">
            <span>Net Operating Profit</span>
            <span>$54,100,000.00</span>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default FinanceDashboard;
