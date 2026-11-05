import { useState } from 'react';
import {
  Users,
  Clock,
  UserCheck,
  CheckCircle,
  XCircle,
  PlusCircle,
  CheckCircle2,
} from 'lucide-react';
import StatCard from '../../components/common/StatCard';
import DataTable from '../../components/tables/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';
import { formatCurrency } from '../../utils/formatters';
import {
  EMPLOYEE_STATS,
  EMPLOYEE_ACCOUNT_REQUESTS,
  EMPLOYEE_DEPOSIT_REQUESTS,
  EMPLOYEE_WITHDRAWAL_REQUESTS,
  EMPLOYEE_RECENT_CUSTOMERS,
} from '../../data/mockEmployee';

export const EmployeeDashboard = () => {
  const { addToast } = useToast();

  const [accountRequests, setAccountRequests] = useState(EMPLOYEE_ACCOUNT_REQUESTS);
  const [depositRequests, setDepositRequests] = useState(EMPLOYEE_DEPOSIT_REQUESTS);
  const [withdrawalRequests, setWithdrawalRequests] = useState(EMPLOYEE_WITHDRAWAL_REQUESTS);
  const [customers, setCustomers] = useState(EMPLOYEE_RECENT_CUSTOMERS);

  // Modals
  const [isRegisterCustModalOpen, setIsRegisterCustModalOpen] = useState(false);

  // Form states
  const [newCustName, setNewCustName] = useState('');
  const [newCustTier, setNewCustTier] = useState('Standard');
  const [newCustDeposit, setNewCustDeposit] = useState('');

  const handleApproveAccount = (id, applicant) => {
    setAccountRequests((prev) => prev.filter((r) => r.id !== id));
    addToast(`Account opening approved for ${applicant}. Core ledger identity created.`, 'success');
  };

  const handleRejectAccount = (id, applicant) => {
    setAccountRequests((prev) => prev.filter((r) => r.id !== id));
    addToast(`Account opening rejected for ${applicant}. Customer notified.`, 'error');
  };

  const handleProcessDeposit = (id, customer, amount) => {
    setDepositRequests((prev) => prev.filter((r) => r.id !== id));
    addToast(`Processed cash/cheque deposit of ${formatCurrency(amount)} for ${customer}.`, 'success');
  };

  const handleProcessWithdrawal = (id, customer, amount) => {
    setWithdrawalRequests((prev) => prev.filter((r) => r.id !== id));
    addToast(`Dispensed ${formatCurrency(amount)} withdrawal for ${customer}.`, 'success');
  };

  const handleRegisterCustomer = (e) => {
    e.preventDefault();
    if (!newCustName.trim()) return;

    const newC = {
      id: `CUST-${Math.floor(800 + Math.random() * 200)}`,
      name: newCustName,
      tier: newCustTier,
      balance: parseFloat(newCustDeposit) || 1000.0,
      accounts: 1,
      kycStatus: 'Verified',
      lastVisit: 'Just now',
    };

    setCustomers([newC, ...customers]);
    addToast(`Registered new customer "${newCustName}" successfully.`, 'success');
    setNewCustName('');
    setNewCustDeposit('');
    setIsRegisterCustModalOpen(false);
  };

  const customerColumns = [
    { key: 'id', header: 'Client ID' },
    {
      key: 'name',
      header: 'Customer Name',
      render: (name) => <span className="font-bold text-slate-900 dark:text-white">{name}</span>,
    },
    {
      key: 'tier',
      header: 'Tier',
      render: (t) => (
        <span className="px-2 py-0.5 rounded-md text-xs font-semibold bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300">
          {t}
        </span>
      ),
    },
    {
      key: 'balance',
      header: 'Ledger Balance',
      align: 'right',
      render: (b) => <span className="font-semibold">{formatCurrency(b)}</span>,
    },
    {
      key: 'kycStatus',
      header: 'KYC Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    { key: 'lastVisit', header: 'Last Visit' },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Bank Teller Counter #04
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              Station Active
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Teller Officer: Daniel Wright • Downtown Metropolitan Branch #104
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => setIsRegisterCustModalOpen(true)}
            className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Register Customer</span>
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Customers Served"
          value={EMPLOYEE_STATS.customersServed.value}
          change={EMPLOYEE_STATS.customersServed.change}
          isPositive={EMPLOYEE_STATS.customersServed.isPositive}
          period={EMPLOYEE_STATS.customersServed.period}
          icon={Users}
        />
        <StatCard
          title="Pending Requests"
          value={EMPLOYEE_STATS.pendingRequests.value}
          change={EMPLOYEE_STATS.pendingRequests.change}
          isPositive={EMPLOYEE_STATS.pendingRequests.isPositive}
          period={EMPLOYEE_STATS.pendingRequests.period}
          icon={Clock}
        />
        <StatCard
          title="Accounts Opened"
          value={EMPLOYEE_STATS.accountsOpened.value}
          change={EMPLOYEE_STATS.accountsOpened.change}
          isPositive={EMPLOYEE_STATS.accountsOpened.isPositive}
          period={EMPLOYEE_STATS.accountsOpened.period}
          icon={UserCheck}
        />
        <StatCard
          title="Txns Processed"
          value={EMPLOYEE_STATS.transactionsProcessed.value}
          change={EMPLOYEE_STATS.transactionsProcessed.change}
          isPositive={EMPLOYEE_STATS.transactionsProcessed.isPositive}
          period={EMPLOYEE_STATS.transactionsProcessed.period}
          icon={CheckCircle}
        />
      </div>

      {/* Account Opening Requests Queue */}
      <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Account Opening Applications ({accountRequests.length})
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              KYC-verified walk-in and digital account onboarding requests awaiting teller sign-off
            </p>
          </div>
        </div>

        {accountRequests.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5">
            {accountRequests.map((req) => (
              <div
                key={req.id}
                className="p-4 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40 flex flex-col justify-between space-y-3"
              >
                <div>
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                        {req.applicant}
                      </h4>
                      <span className="text-2xs text-slate-400">
                        {req.nationalId} • {req.timestamp}
                      </span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full text-2xs font-semibold bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300">
                      {req.riskScore}
                    </span>
                  </div>

                  <div className="mt-2 text-xs text-slate-600 dark:text-slate-300 space-y-1">
                    <div className="flex justify-between">
                      <span className="text-slate-400">Account Type:</span>
                      <span className="font-semibold">{req.accountType}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-400">Initial Deposit:</span>
                      <span className="font-bold text-emerald-600">
                        {formatCurrency(req.depositAmount)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-2 border-t border-slate-200/60 dark:border-slate-700/60">
                  <button
                    type="button"
                    onClick={() => handleRejectAccount(req.id, req.applicant)}
                    className="flex-1 py-1.5 px-3 rounded-lg text-xs font-semibold text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/40 hover:bg-rose-100 transition-colors flex items-center justify-center gap-1"
                  >
                    <XCircle className="w-3.5 h-3.5" />
                    <span>Reject</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => handleApproveAccount(req.id, req.applicant)}
                    className="flex-1 py-1.5 px-3 rounded-lg text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 transition-colors flex items-center justify-center gap-1 shadow-xs"
                  >
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    <span>Approve & Open</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-8 text-center text-xs text-slate-400 border border-dashed rounded-xl">
            All account opening requests have been processed!
          </div>
        )}
      </div>

      {/* Cash Counter Requests: Deposit & Withdrawal Queues */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {/* Deposit Requests */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-bold text-slate-900 dark:text-white">
              Over-the-Counter Deposit Queue ({depositRequests.length})
            </h4>
          </div>

          <div className="space-y-2">
            {depositRequests.map((dep) => (
              <div
                key={dep.id}
                className="p-3 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/50 flex items-center justify-between text-xs"
              >
                <div>
                  <div className="font-bold text-slate-900 dark:text-white">{dep.customer}</div>
                  <div className="text-slate-400 text-2xs">{dep.method} • {dep.account}</div>
                  <div className="text-emerald-600 font-bold mt-0.5">{formatCurrency(dep.amount)}</div>
                </div>
                <button
                  type="button"
                  onClick={() => handleProcessDeposit(dep.id, dep.customer, dep.amount)}
                  className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs shadow-xs"
                >
                  Accept & Clear
                </button>
              </div>
            ))}
            {depositRequests.length === 0 && (
              <div className="p-4 text-center text-xs text-slate-400">
                Deposit queue is clear.
              </div>
            )}
          </div>
        </div>

        {/* Withdrawal Requests */}
        <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h4 className="text-sm font-bold text-slate-900 dark:text-white">
              Counter Cash Withdrawal Queue ({withdrawalRequests.length})
            </h4>
          </div>

          <div className="space-y-2">
            {withdrawalRequests.map((wth) => (
              <div
                key={wth.id}
                className="p-3 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/70 dark:bg-slate-800/50 flex items-center justify-between text-xs"
              >
                <div>
                  <div className="font-bold text-slate-900 dark:text-white">{wth.customer}</div>
                  <div className="text-slate-400 text-2xs">{wth.method} • {wth.account}</div>
                  <div className="text-slate-900 dark:text-white font-bold mt-0.5">{formatCurrency(wth.amount)}</div>
                </div>
                <button
                  type="button"
                  onClick={() => handleProcessWithdrawal(wth.id, wth.customer, wth.amount)}
                  className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-white font-semibold text-xs shadow-xs"
                >
                  Dispense Cash
                </button>
              </div>
            ))}
            {withdrawalRequests.length === 0 && (
              <div className="p-4 text-center text-xs text-slate-400">
                Withdrawal queue is clear.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Customer Directory */}
      <DataTable
        title="Walk-In Customer Profiles"
        subtitle="Recently serviced customers at Counter Desk #04"
        columns={customerColumns}
        data={customers}
      />

      {/* Register Customer Modal */}
      <Modal
        isOpen={isRegisterCustModalOpen}
        onClose={() => setIsRegisterCustModalOpen(false)}
        title="Register New Walk-In Customer"
        subtitle="Onboard a retail client with biometric KYC verification"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleRegisterCustomer}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Complete Onboarding
          </button>
        }
      >
        <form onSubmit={handleRegisterCustomer} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Customer Full Legal Name
            </label>
            <input
              type="text"
              value={newCustName}
              onChange={(e) => setNewCustName(e.target.value)}
              placeholder="e.g. Benjamin Ross"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Customer Tier
            </label>
            <select
              value={newCustTier}
              onChange={(e) => setNewCustTier(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            >
              <option value="Standard">Standard Retail</option>
              <option value="Premier">Premier Banking</option>
              <option value="Private Wealth">Private Wealth</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Initial Deposit Amount ($)
            </label>
            <input
              type="number"
              step="0.01"
              value={newCustDeposit}
              onChange={(e) => setNewCustDeposit(e.target.value)}
              placeholder="1000.00"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default EmployeeDashboard;
