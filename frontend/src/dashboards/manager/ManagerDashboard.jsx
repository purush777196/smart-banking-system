import { useState } from 'react';
import {
  Users,
  Wallet,
  ArrowDownLeft,
  ArrowUpRight,
  UserPlus,
  FileText,
  Building2,
} from 'lucide-react';
import {
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
import { formatCurrency } from '../../utils/formatters';
import {
  MANAGER_STATS,
  MANAGER_PERFORMANCE_DATA,
  MANAGER_MONTHLY_TRANSACTIONS,
  MANAGER_EMPLOYEE_PERFORMANCE,
  MANAGER_EMPLOYEES,
  MANAGER_RECENT_TRANSACTIONS,
} from '../../data/mockManager';

export const ManagerDashboard = () => {
  const { addToast } = useToast();
  const [employees, setEmployees] = useState(MANAGER_EMPLOYEES);
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [isCustomerModalOpen, setIsCustomerModalOpen] = useState(false);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);

  // New Employee Form
  const [newEmpName, setNewEmpName] = useState('');
  const [newEmpRole, setNewEmpRole] = useState('Teller Desk #2');
  const [newEmpEmail, setNewEmpEmail] = useState('');

  const handleAddEmployee = (e) => {
    e.preventDefault();
    if (!newEmpName.trim()) return;

    const newEmp = {
      id: `EMP-0${employees.length + 1}`,
      name: newEmpName,
      role: newEmpRole,
      email: newEmpEmail || `${newEmpName.toLowerCase().replace(' ', '.')}@finova.bank`,
      status: 'On Duty',
      accountsOpened: 0,
      satisfactionScore: 5.0,
    };

    setEmployees([newEmp, ...employees]);
    addToast(`Employee "${newEmpName}" assigned to branch roster.`, 'success');
    setNewEmpName('');
    setNewEmpEmail('');
    setIsEmployeeModalOpen(false);
  };

  const handleGenerateReport = () => {
    addToast('Generating monthly branch operations & teller audit statement...', 'info');
    setTimeout(() => {
      addToast('Branch performance report exported successfully.', 'success');
      setIsReportModalOpen(false);
    }, 1000);
  };

  const employeeColumns = [
    { key: 'id', header: 'Staff ID' },
    {
      key: 'name',
      header: 'Staff Member',
      render: (name) => <span className="font-bold text-slate-900 dark:text-white">{name}</span>,
    },
    { key: 'role', header: 'Assigned Desk' },
    { key: 'email', header: 'Internal Email' },
    {
      key: 'accountsOpened',
      header: 'Accounts Opened',
      align: 'center',
      render: (count) => <span className="font-semibold">{count}</span>,
    },
    {
      key: 'satisfactionScore',
      header: 'CSAT Rating',
      align: 'center',
      render: (score) => (
        <span className="font-bold text-emerald-600 dark:text-emerald-400">
          ★ {score}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Duty Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
  ];

  const transactionColumns = [
    { key: 'id', header: 'Txn Ref' },
    { key: 'customer', header: 'Client' },
    { key: 'type', header: 'Operation' },
    {
      key: 'amount',
      header: 'Amount',
      align: 'right',
      render: (val) => (
        <span className="font-semibold text-slate-900 dark:text-white">
          {formatCurrency(val)}
        </span>
      ),
    },
    { key: 'desk', header: 'Serviced At' },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    { key: 'time', header: 'Time' },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Branch Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div className="flex items-center gap-3.5">
          <div className="p-3 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
            <Building2 className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-xl font-bold text-slate-900 dark:text-white">
                Downtown Metropolitan Branch #104
              </h2>
              <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
                Flagship Branch
              </span>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              General Manager: Marcus Sterling • Daily Cash Float: $2,500,000.00 in Vault
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => setIsCustomerModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition-colors"
          >
            View Branch Customers
          </button>
          <button
            type="button"
            onClick={() => setIsEmployeeModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition-colors flex items-center gap-1.5"
          >
            <UserPlus className="w-4 h-4 text-emerald-600" />
            <span>Manage Staff</span>
          </button>
          <button
            type="button"
            onClick={() => setIsReportModalOpen(true)}
            className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
          >
            <FileText className="w-4 h-4" />
            <span>Branch Reports</span>
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Branch Customers"
          value={MANAGER_STATS.branchCustomers.value}
          change={MANAGER_STATS.branchCustomers.change}
          isPositive={MANAGER_STATS.branchCustomers.isPositive}
          period={MANAGER_STATS.branchCustomers.period}
          icon={Users}
        />
        <StatCard
          title="Active Accounts"
          value={MANAGER_STATS.activeAccounts.value}
          change={MANAGER_STATS.activeAccounts.change}
          isPositive={MANAGER_STATS.activeAccounts.isPositive}
          period={MANAGER_STATS.activeAccounts.period}
          icon={Wallet}
        />
        <StatCard
          title="Branch Deposits"
          value={MANAGER_STATS.branchDeposits.value}
          isCurrency
          compact
          change={MANAGER_STATS.branchDeposits.change}
          isPositive={MANAGER_STATS.branchDeposits.isPositive}
          period={MANAGER_STATS.branchDeposits.period}
          icon={ArrowDownLeft}
        />
        <StatCard
          title="Branch Withdrawals"
          value={MANAGER_STATS.branchWithdrawals.value}
          isCurrency
          compact
          change={MANAGER_STATS.branchWithdrawals.change}
          isPositive={MANAGER_STATS.branchWithdrawals.isPositive}
          period={MANAGER_STATS.branchWithdrawals.period}
          icon={ArrowUpRight}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Performance vs Target */}
        <div className="lg:col-span-6">
          <ChartCard
            title="Branch Performance (Target vs Actual)"
            subtitle="Monthly deposit intake targets vs realized intake ($M)"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={MANAGER_PERFORMANCE_DATA}>
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
                <Bar dataKey="target" name="Monthly Target" fill="#94a3b8" radius={[4, 4, 0, 0]} />
                <Bar dataKey="actual" name="Actual Intake" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>

        {/* Monthly Transactions Flow */}
        <div className="lg:col-span-6">
          <ChartCard
            title="Weekly Cash Flow Velocity"
            subtitle="Deposits versus withdrawals across four fiscal weeks ($M)"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={MANAGER_MONTHLY_TRANSACTIONS}>
                <defs>
                  <linearGradient id="mgrDepGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="week" stroke="#94a3b8" fontSize={11} tickLine={false} />
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
                <Area type="monotone" dataKey="deposits" name="Deposits" stroke="#10b981" fillOpacity={1} fill="url(#mgrDepGrad)" strokeWidth={2.5} />
                <Area type="monotone" dataKey="withdrawals" name="Withdrawals" stroke="#f43f5e" fillOpacity={0.2} fill="#f43f5e" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Staff Productivity Bar */}
      <ChartCard
        title="Employee Performance & Target Achievement"
        subtitle="Staff CSAT and monthly throughput index rating"
        height="h-56"
      >
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={MANAGER_EMPLOYEE_PERFORMANCE} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
            <XAxis type="number" domain={[0, 100]} stroke="#94a3b8" fontSize={11} />
            <YAxis dataKey="name" type="category" stroke="#94a3b8" fontSize={11} width={100} tickLine={false} />
            <Tooltip
              contentStyle={{
                backgroundColor: '#0f172a',
                borderColor: '#334155',
                borderRadius: '0.75rem',
                color: '#fff',
                fontSize: '12px',
              }}
            />
            <Bar dataKey="score" name="Performance Score" fill="#10b981" radius={[0, 4, 4, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>

      {/* Tables Section */}
      <div className="grid grid-cols-1 gap-6">
        <DataTable
          title="Branch Staff Directory"
          subtitle="Tellers, Loan Specialists, and Client Onboarding Desks"
          columns={employeeColumns}
          data={employees}
        />

        <DataTable
          title="Recent Branch Transactions"
          subtitle="Live teller counter operations"
          columns={transactionColumns}
          data={MANAGER_RECENT_TRANSACTIONS}
        />
      </div>

      {/* Manage Employees Modal */}
      <Modal
        isOpen={isEmployeeModalOpen}
        onClose={() => setIsEmployeeModalOpen(false)}
        title="Assign Branch Staff Member"
        subtitle="Add a teller or specialist to Downtown Metro #104"
        maxWidth="max-w-md"
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsEmployeeModalOpen(false)}
              className="px-4 py-2 text-xs text-slate-600 dark:text-slate-300"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleAddEmployee}
              className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
            >
              Assign Staff
            </button>
          </>
        }
      >
        <form onSubmit={handleAddEmployee} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Employee Full Name
            </label>
            <input
              type="text"
              value={newEmpName}
              onChange={(e) => setNewEmpName(e.target.value)}
              placeholder="e.g. Samantha Vance"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Assigned Desk / Role
            </label>
            <select
              value={newEmpRole}
              onChange={(e) => setNewEmpRole(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            >
              <option value="Head Teller Desk #1">Head Teller Desk #1</option>
              <option value="Teller Desk #2">Teller Desk #2</option>
              <option value="Retail Lending Desk">Retail Lending Desk</option>
              <option value="Client Onboarding Desk">Client Onboarding Desk</option>
              <option value="Vault Custodian">Vault Custodian</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Corporate Email
            </label>
            <input
              type="email"
              value={newEmpEmail}
              onChange={(e) => setNewEmpEmail(e.target.value)}
              placeholder="samantha.v@finova.bank"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </form>
      </Modal>

      {/* View Customers Modal */}
      <Modal
        isOpen={isCustomerModalOpen}
        onClose={() => setIsCustomerModalOpen(false)}
        title="Branch Client Records"
        subtitle="4,820 accounts domiciled at Downtown Metropolitan Branch"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={() => setIsCustomerModalOpen(false)}
            className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 rounded-xl"
          >
            Done
          </button>
        }
      >
        <p className="text-xs text-slate-600 dark:text-slate-400">
          Showing local branch ledger. 94% of domiciled accounts have digital mobile banking activated with regular biometric logins.
        </p>
      </Modal>

      {/* Reports Modal */}
      <Modal
        isOpen={isReportModalOpen}
        onClose={() => setIsReportModalOpen(false)}
        title="Generate Branch Report"
        subtitle="Reconciliation statements for Branch #104"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleGenerateReport}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Export Comprehensive Branch Summary
          </button>
        }
      >
        <div className="space-y-2 text-xs text-slate-600 dark:text-slate-300">
          <p>This report compiles:</p>
          <ul className="list-disc pl-5 space-y-1">
            <li>End-of-day cash drawer balancing</li>
            <li>Teller turnaround SLA metrics</li>
            <li>Vault deposits and armored car dispatches</li>
          </ul>
        </div>
      </Modal>
    </div>
  );
};

export default ManagerDashboard;
