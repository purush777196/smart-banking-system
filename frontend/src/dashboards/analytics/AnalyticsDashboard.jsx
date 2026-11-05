import { useState } from 'react';
import {
  BarChart3,
  TrendingUp,
  DollarSign,
  PieChart as PieIcon,
  Download,
} from 'lucide-react';
import {
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
import DateRangeFilter from '../../components/common/DateRangeFilter';
import DataTable from '../../components/tables/DataTable';
import { useToast } from '../../hooks/useToast';
import {
  ANALYTICS_DATA_BY_PERIOD,
  ANALYTICS_ACQUISITION_CHANNELS,
  ANALYTICS_BRANCH_COMPARISON,
  ANALYTICS_TRANSACTION_DISTRIBUTION,
} from '../../data/mockAnalytics';

export const AnalyticsDashboard = () => {
  const { addToast } = useToast();
  const [period, setPeriod] = useState('30d');

  const currentDataset = ANALYTICS_DATA_BY_PERIOD[period] || ANALYTICS_DATA_BY_PERIOD['30d'];

  const handlePeriodChange = (newPeriod) => {
    setPeriod(newPeriod);
    addToast(`Telemetry refreshed for period: ${newPeriod.toUpperCase()}`, 'info');
  };

  const handleExportAnalytics = () => {
    addToast('Compiling analytical datasets (BI JSON / CSV)...', 'info');
    setTimeout(() => {
      addToast(`Analytics report exported for ${period.toUpperCase()}`, 'success');
    }, 1200);
  };

  const branchComparisonColumns = [
    { key: 'branch', header: 'Branch Node' },
    {
      key: 'volume',
      header: 'Processing Volume ($M)',
      align: 'right',
      render: (val) => <span className="font-bold">${val}M</span>,
    },
    {
      key: 'growth',
      header: 'Net Growth %',
      align: 'center',
      render: (g) => <span className="font-semibold text-emerald-600">+{g}%</span>,
    },
    {
      key: 'nps',
      header: 'NPS Score',
      align: 'center',
      render: (score) => (
        <span className="px-2 py-0.5 rounded-full text-xs font-bold bg-slate-100 dark:bg-slate-800 text-slate-800 dark:text-slate-200">
          {score} / 100
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Banner & Date Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Institutional Banking Analytics & Telemetry
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              Live BI Engine
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Head of Analytics: Devin Sharma • Multi-period cohort tracking, volume velocities & branch KPIs
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          {/* Interactive Date Range Filter */}
          <DateRangeFilter
            activePeriod={period}
            onSelect={handlePeriodChange}
            options={[
              { label: 'Today', value: 'today' },
              { label: '7 Days', value: '7d' },
              { label: '30 Days', value: '30d' },
              { label: '12 Months', value: '12m' },
            ]}
          />

          <button
            type="button"
            onClick={handleExportAnalytics}
            className="px-3.5 py-1.5 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Export BI</span>
          </button>
        </div>
      </div>

      {/* KPI Cards (Dynamically driven by selected period!) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Banking Volume"
          value={currentDataset.kpis.volume.value}
          isCurrency
          compact
          change={currentDataset.kpis.volume.change}
          isPositive={currentDataset.kpis.volume.isPositive}
          period={currentDataset.kpis.volume.period}
          icon={TrendingUp}
        />
        <StatCard
          title="Customer Growth"
          value={currentDataset.kpis.growth.value}
          change={currentDataset.kpis.growth.change}
          isPositive={currentDataset.kpis.growth.isPositive}
          period={currentDataset.kpis.growth.period}
          icon={BarChart3}
        />
        <StatCard
          title="Consolidated Revenue"
          value={currentDataset.kpis.revenue.value}
          isCurrency
          compact
          change={currentDataset.kpis.revenue.change}
          isPositive={currentDataset.kpis.revenue.isPositive}
          period={currentDataset.kpis.revenue.period}
          icon={DollarSign}
        />
        <StatCard
          title="Active Loan Portfolio"
          value={currentDataset.kpis.portfolio.value}
          isCurrency
          compact
          change={currentDataset.kpis.portfolio.change}
          isPositive={currentDataset.kpis.portfolio.isPositive}
          period={currentDataset.kpis.portfolio.period}
          icon={PieIcon}
        />
      </div>

      {/* Primary Trend Area Chart (Driven by Period) */}
      <ChartCard
        title={`Volume Velocity Trends (${period.toUpperCase()})`}
        subtitle="Gross financial transmission volume across core banking nodes"
        height="h-72"
      >
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={currentDataset.trends}>
            <defs>
              <linearGradient id="anaVolumeGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
            <XAxis dataKey="time" stroke="#94a3b8" fontSize={11} tickLine={false} />
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
            <Area type="monotone" dataKey="volume" name="Volume Index" stroke="#10b981" strokeWidth={2.5} fillOpacity={1} fill="url(#anaVolumeGrad)" />
          </AreaChart>
        </ResponsiveContainer>
      </ChartCard>

      {/* Grid: Acquisition Channels & Transaction Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Customer Acquisition Channels */}
        <div className="lg:col-span-6">
          <ChartCard
            title="Customer Acquisition by Channel (%)"
            subtitle="Origination points for newly registered customer accounts"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={ANALYTICS_ACQUISITION_CHANNELS}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="count"
                >
                  {ANALYTICS_ACQUISITION_CHANNELS.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
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
              </PieChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>

        {/* Transaction Rail Distribution */}
        <div className="lg:col-span-6">
          <ChartCard
            title="Transaction Settlement Distribution"
            subtitle="Clearing mechanism breakdown across the banking network"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={ANALYTICS_TRANSACTION_DISTRIBUTION}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="percentage"
                >
                  {ANALYTICS_TRANSACTION_DISTRIBUTION.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
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
              </PieChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Branch Comparison Table */}
      <DataTable
        title="Regional Branch Analytical Benchmark"
        subtitle="Processing volume, net quarter growth, and Net Promoter Score"
        columns={branchComparisonColumns}
        data={ANALYTICS_BRANCH_COMPARISON}
      />
    </div>
  );
};

export default AnalyticsDashboard;
