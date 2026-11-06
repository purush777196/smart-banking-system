import { useState, useEffect, useMemo } from 'react';
import {
  Wallet,
  ArrowDownLeft,
  ArrowUpRight,
  TrendingDown,
  Send,
  PlusCircle,
  Copy,
  Check,
  CreditCard,
  FileDown,
  Users,
  ShieldCheck,
  Building2,
  RefreshCw,
  Info,
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';
import StatCard from '../../components/common/StatCard';
import ChartCard from '../../components/common/ChartCard';
import Modal from '../../components/common/Modal';
import DepositModal from '../../components/transactions/DepositModal';
import WithdrawModal from '../../components/transactions/WithdrawModal';
import TransactionHistoryTable from '../../components/transactions/TransactionHistoryTable';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { copyToClipboard } from '../../utils/helpers';
import { formatCurrency } from '../../utils/formatters';
import {
  getMyAccounts,
  maskAccountNumber,
} from '../../services/transactionService';
import {
  CUSTOMER_STATS,
  CUSTOMER_ACCOUNTS,
  CUSTOMER_SPENDING_CATEGORIES,
  CUSTOMER_MONTHLY_SPEND_TREND,
  CUSTOMER_BENEFICIARIES,
} from '../../data/mockCustomer';

export const CustomerDashboard = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [copiedId, setCopiedId] = useState(null);

  // Real Accounts State
  const [accounts, setAccounts] = useState([]);
  const [selectedAccountId, setSelectedAccountId] = useState(null);
  const [accountsLoading, setAccountsLoading] = useState(true);
  const [isLiveBackend, setIsLiveBackend] = useState(false);
  const [refreshCounter, setRefreshCounter] = useState(0);

  // Modals state
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [isBeneficiaryModalOpen, setIsBeneficiaryModalOpen] = useState(false);

  // Transfer simulation states
  const [transferAmount, setTransferAmount] = useState('');
  const [transferRecipient, setTransferRecipient] = useState(CUSTOMER_BENEFICIARIES[0].name);
  const [beneficiaries, setBeneficiaries] = useState(CUSTOMER_BENEFICIARIES);

  // New beneficiary form
  const [newBenName, setNewBenName] = useState('');
  const [newBenBank, setNewBenBank] = useState('');
  const [newBenAcc, setNewBenAcc] = useState('');

  // Fetch real accounts from backend via effect
  useEffect(() => {
    let isMounted = true;

    const fetchAccounts = async () => {
      setAccountsLoading(true);
      try {
        const liveAccounts = await getMyAccounts();
        if (isMounted) {
          if (Array.isArray(liveAccounts) && liveAccounts.length > 0) {
            setAccounts(liveAccounts);
            setIsLiveBackend(true);
            setSelectedAccountId((prev) => (prev ? prev : liveAccounts[0].id));
          } else {
            setAccounts(CUSTOMER_ACCOUNTS);
            setIsLiveBackend(false);
            setSelectedAccountId((prev) => (prev ? prev : CUSTOMER_ACCOUNTS[0].id));
          }
        }
      } catch {
        if (isMounted) {
          setAccounts(CUSTOMER_ACCOUNTS);
          setIsLiveBackend(false);
          setSelectedAccountId((prev) => (prev ? prev : CUSTOMER_ACCOUNTS[0].id));
        }
      } finally {
        if (isMounted) {
          setAccountsLoading(false);
        }
      }
    };

    fetchAccounts();

    return () => {
      isMounted = false;
    };
  }, [refreshCounter]);

  const handleCopy = async (text, id) => {
    const success = await copyToClipboard(text);
    if (success) {
      setCopiedId(id);
      addToast(`Copied ${text} to clipboard`, 'info');
      setTimeout(() => setCopiedId(null), 2000);
    }
  };

  const handleTransfer = (e) => {
    e.preventDefault();
    const amt = parseFloat(transferAmount);
    if (!amt || amt <= 0) return;

    addToast(
      `Demo transfer of ${formatCurrency(amt)} to ${transferRecipient} recorded in simulation.`,
      'info'
    );
    setTransferAmount('');
    setIsTransferModalOpen(false);
  };

  const handleAddBeneficiary = (e) => {
    e.preventDefault();
    if (!newBenName.trim() || !newBenAcc.trim()) return;

    const newBen = {
      id: `BEN-0${beneficiaries.length + 1}`,
      name: newBenName,
      bank: newBenBank || 'Finova Core',
      account: `•••• ${newBenAcc.slice(-4)}`,
      avatar:
        'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80',
    };

    setBeneficiaries([...beneficiaries, newBen]);
    addToast(`Added ${newBenName} to your trusted beneficiaries`, 'success');
    setNewBenName('');
    setNewBenBank('');
    setNewBenAcc('');
    setIsBeneficiaryModalOpen(false);
  };

  const handleExportStatement = () => {
    addToast('Generating encrypted financial statement (PDF)...', 'info');
    setTimeout(() => {
      addToast('Statement downloaded: Finova_Ledger_Statement.pdf', 'success');
    }, 1200);
  };

  // Success handler for real deposit
  const handleDepositSuccess = (transactionResponse) => {
    setRefreshCounter((c) => c + 1);
    addToast(
      `Deposit of ${formatCurrency(transactionResponse.amount, transactionResponse.currency || 'INR')} confirmed! Ref: ${transactionResponse.transactionReference}`,
      'success'
    );
  };

  // Success handler for real withdrawal
  const handleWithdrawSuccess = (transactionResponse) => {
    setRefreshCounter((c) => c + 1);
    addToast(
      `Withdrawal of ${formatCurrency(transactionResponse.amount, transactionResponse.currency || 'INR')} executed! Ref: ${transactionResponse.transactionReference}`,
      'success'
    );
  };

  // Compute live total balance if live accounts are present
  const totalBalance = useMemo(() => {
    if (!accounts.length) return 0;
    return accounts.reduce(
      (sum, acc) => sum + (parseFloat(acc.balance) || 0),
      0
    );
  }, [accounts]);

  const activeAccount = useMemo(() => {
    return (
      accounts.find((a) => String(a.id) === String(selectedAccountId)) ||
      accounts[0] ||
      null
    );
  }, [accounts, selectedAccountId]);

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Welcome back, {user?.name || 'Retail Banking Client'}
            </h2>
            {isLiveBackend ? (
              <span className="px-2.5 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800 flex items-center gap-1">
                <ShieldCheck className="w-3 h-3 text-emerald-600" />
                Live Core Connected
              </span>
            ) : (
              <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-amber-100 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300 border border-amber-300 dark:border-amber-800 flex items-center gap-1">
                <Info className="w-3 h-3 text-amber-600" />
                Demo Simulation
              </span>
            )}
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Primary Account:{' '}
            <strong className="text-slate-700 dark:text-slate-300 font-mono">
              {activeAccount ? maskAccountNumber(activeAccount.accountNumber) : '•••• 0192'}
            </strong>{' '}
            • Priority Concierge Active
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => setIsDepositModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-emerald-50 hover:bg-emerald-100 dark:bg-emerald-950/40 dark:hover:bg-emerald-900/50 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800/60 transition-colors flex items-center gap-1.5 shadow-xs"
          >
            <PlusCircle className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span>Deposit</span>
          </button>
          <button
            type="button"
            onClick={() => setIsWithdrawModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-rose-50 hover:bg-rose-100 dark:bg-rose-950/40 dark:hover:bg-rose-900/50 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-800/60 transition-colors flex items-center gap-1.5 shadow-xs"
          >
            <ArrowUpRight className="w-4 h-4 text-rose-600 dark:text-rose-400" />
            <span>Withdraw</span>
          </button>
          <button
            type="button"
            onClick={() => setIsTransferModalOpen(true)}
            className="px-4 py-2 text-xs font-bold rounded-xl bg-slate-900 hover:bg-slate-800 dark:bg-slate-800 dark:hover:bg-slate-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
          >
            <Send className="w-4 h-4 text-emerald-400" />
            <span>Transfer Money</span>
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Portfolio Balance"
          value={isLiveBackend ? totalBalance : CUSTOMER_STATS.availableBalance.value}
          isCurrency
          change={CUSTOMER_STATS.availableBalance.change}
          isPositive={CUSTOMER_STATS.availableBalance.isPositive}
          period={isLiveBackend ? 'live synced' : CUSTOMER_STATS.availableBalance.period}
          icon={Wallet}
        />
        <StatCard
          title="Total Inflow"
          value={CUSTOMER_STATS.totalDeposits.value}
          isCurrency
          change={CUSTOMER_STATS.totalDeposits.change}
          isPositive={CUSTOMER_STATS.totalDeposits.isPositive}
          period={CUSTOMER_STATS.totalDeposits.period}
          icon={ArrowDownLeft}
        />
        <StatCard
          title="Total Outflow"
          value={CUSTOMER_STATS.totalWithdrawals.value}
          isCurrency
          change={CUSTOMER_STATS.totalWithdrawals.change}
          isPositive={CUSTOMER_STATS.totalWithdrawals.isPositive}
          period={CUSTOMER_STATS.totalWithdrawals.period}
          icon={ArrowUpRight}
        />
        <StatCard
          title="Monthly Activity"
          value={CUSTOMER_STATS.monthlySpending.value}
          isCurrency
          change={CUSTOMER_STATS.monthlySpending.change}
          isPositive={CUSTOMER_STATS.monthlySpending.isPositive}
          period={CUSTOMER_STATS.monthlySpending.period}
          icon={TrendingDown}
        />
      </div>

      {/* Bank Account Details Cards */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CreditCard className="w-5 h-5 text-emerald-600" />
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Your Finova Bank Accounts
            </h3>
            {accountsLoading && (
              <RefreshCw className="w-3.5 h-3.5 animate-spin text-slate-400" />
            )}
          </div>
          <button
            type="button"
            onClick={handleExportStatement}
            className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:underline flex items-center gap-1"
          >
            <FileDown className="w-3.5 h-3.5" />
            <span>Download Statement</span>
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {accounts.map((acc, index) => {
            const isSelected = String(acc.id) === String(selectedAccountId);
            const isPrimary = acc.isPrimary || index === 0;

            return (
              <div
                key={acc.id}
                onClick={() => setSelectedAccountId(acc.id)}
                className={`p-5 rounded-2xl border transition-all duration-200 relative overflow-hidden cursor-pointer ${
                  isSelected
                    ? 'ring-2 ring-emerald-500 shadow-md'
                    : 'hover:border-slate-300 dark:hover:border-slate-700'
                } ${
                  isPrimary
                    ? 'bg-gradient-to-br from-[#0b132b] to-[#152a55] text-white border-slate-700'
                    : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <span
                      className={`text-2xs font-bold uppercase tracking-wider ${
                        isPrimary ? 'text-emerald-400' : 'text-slate-400'
                      }`}
                    >
                      {acc.accountType || acc.type} Account
                    </span>
                    <h4
                      className={`text-sm font-bold mt-0.5 ${
                        isPrimary ? 'text-white' : 'text-slate-900 dark:text-white'
                      }`}
                    >
                      {acc.name || `${acc.accountType || 'Finova'} Active Ledger`}
                    </h4>
                  </div>
                  <div className="flex items-center gap-1">
                    {isPrimary && (
                      <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-400/30">
                        Primary
                      </span>
                    )}
                    {isSelected && (
                      <span className="px-1.5 py-0.5 text-2xs font-bold rounded-md bg-emerald-600 text-white">
                        Active
                      </span>
                    )}
                  </div>
                </div>

                <div className="mt-4">
                  <span
                    className={`text-xs ${
                      isPrimary ? 'text-slate-300' : 'text-slate-400'
                    }`}
                  >
                    Balance
                  </span>
                  <div
                    className={`text-2xl font-black ${
                      isPrimary ? 'text-white' : 'text-slate-900 dark:text-white'
                    }`}
                  >
                    {formatCurrency(acc.balance, acc.currency || 'INR')}
                  </div>
                </div>

                <div
                  className={`mt-4 pt-3 border-t text-xs space-y-1.5 ${
                    isPrimary
                      ? 'border-slate-700 text-slate-300'
                      : 'border-slate-100 dark:border-slate-800 text-slate-500 dark:text-slate-400'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-mono">Acc: {acc.accountNumber}</span>
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCopy(acc.accountNumber, `acc-${acc.id}`);
                      }}
                      className="hover:text-emerald-400"
                      title="Copy account number"
                    >
                      {copiedId === `acc-${acc.id}` ? (
                        <Check className="w-3.5 h-3.5 text-emerald-400" />
                      ) : (
                        <Copy className="w-3.5 h-3.5" />
                      )}
                    </button>
                  </div>
                  {acc.branchName && (
                    <div className="flex items-center justify-between text-2xs text-slate-400">
                      <span className="flex items-center gap-1">
                        <Building2 className="w-3 h-3" /> {acc.branchName}
                      </span>
                      <span>{acc.ifscCode || acc.branchCode}</span>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Real Transaction History Table & Reference Lookup */}
      <TransactionHistoryTable
        accounts={accounts}
        selectedAccountId={selectedAccountId}
        onAccountChange={(id) => setSelectedAccountId(id)}
      />

      {/* Charts & Analytics Section */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Spending by Category Donut */}
        <div className="lg:col-span-5">
          <ChartCard
            title="Spending by Category"
            subtitle="Expense breakdown for current calendar month"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={CUSTOMER_SPENDING_CATEGORIES}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {CUSTOMER_SPENDING_CATEGORIES.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val) => [formatCurrency(val), '']}
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

        {/* Income vs Spending Trend */}
        <div className="lg:col-span-7">
          <ChartCard
            title="Monthly Cash Inflow vs Outflow"
            subtitle="Monthly comparison of direct deposits and expenses"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={CUSTOMER_MONTHLY_SPEND_TREND}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="month" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip
                  formatter={(val) => [formatCurrency(val), '']}
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Bar dataKey="income" name="Income" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="spending" name="Spending" fill="#0b132b" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Quick Beneficiaries Carousel */}
      <div className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-xs space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Users className="w-4 h-4 text-emerald-600" />
            <h4 className="text-sm font-bold text-slate-900 dark:text-white">
              Trusted Beneficiaries & Quick Pay
            </h4>
          </div>
          <button
            type="button"
            onClick={() => setIsBeneficiaryModalOpen(true)}
            className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:underline"
          >
            + Add Beneficiary
          </button>
        </div>

        <div className="flex items-center gap-3 overflow-x-auto pb-1">
          {beneficiaries.map((b) => (
            <div
              key={b.id}
              onClick={() => {
                setTransferRecipient(b.name);
                setIsTransferModalOpen(true);
              }}
              className="flex items-center gap-3 p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 hover:border-emerald-500 dark:hover:border-emerald-500 cursor-pointer transition-all hover:shadow-xs min-w-[210px] shrink-0"
            >
              <img
                src={b.avatar}
                alt={b.name}
                className="w-9 h-9 rounded-full object-cover border border-emerald-500/40"
              />
              <div className="min-w-0 flex-1">
                <span className="text-xs font-bold text-slate-900 dark:text-white block truncate">
                  {b.name}
                </span>
                <span className="text-2xs text-slate-400 block truncate">
                  {b.bank} • {b.account}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Real Deposit Modal (fresh mount when open) */}
      {isDepositModalOpen && (
        <DepositModal
          isOpen={isDepositModalOpen}
          onClose={() => setIsDepositModalOpen(false)}
          accounts={accounts}
          selectedAccountId={selectedAccountId}
          onSuccess={handleDepositSuccess}
        />
      )}

      {/* Real Withdrawal Modal (fresh mount when open) */}
      {isWithdrawModalOpen && (
        <WithdrawModal
          isOpen={isWithdrawModalOpen}
          onClose={() => setIsWithdrawModalOpen(false)}
          accounts={accounts}
          selectedAccountId={selectedAccountId}
          onSuccess={handleWithdrawSuccess}
        />
      )}

      {/* Transfer Simulation Modal */}
      <Modal
        isOpen={isTransferModalOpen}
        onClose={() => setIsTransferModalOpen(false)}
        title="Transfer Funds"
        subtitle="Interbank wire or internal Finova transfer simulation"
        maxWidth="max-w-md"
        footer={
          <div className="flex items-center justify-between w-full">
            <button
              type="button"
              onClick={() => setIsTransferModalOpen(false)}
              className="px-4 py-2 text-xs text-slate-600 dark:text-slate-300"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleTransfer}
              className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
            >
              Confirm Transfer
            </button>
          </div>
        }
      >
        <form onSubmit={handleTransfer} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Select Beneficiary
            </label>
            <select
              value={transferRecipient}
              onChange={(e) => setTransferRecipient(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            >
              {beneficiaries.map((b) => (
                <option key={b.id} value={b.name}>
                  {b.name} ({b.bank} - {b.account})
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Transfer Amount
            </label>
            <input
              type="number"
              step="0.01"
              value={transferAmount}
              onChange={(e) => setTransferAmount(e.target.value)}
              placeholder="e.g. 250.00"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>
        </form>
      </Modal>

      {/* Manage Beneficiaries Modal */}
      <Modal
        isOpen={isBeneficiaryModalOpen}
        onClose={() => setIsBeneficiaryModalOpen(false)}
        title="Add New Beneficiary"
        subtitle="Save external bank account for quick wire routing"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleAddBeneficiary}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Save Beneficiary
          </button>
        }
      >
        <form onSubmit={handleAddBeneficiary} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Recipient Full Name
            </label>
            <input
              type="text"
              value={newBenName}
              onChange={(e) => setNewBenName(e.target.value)}
              placeholder="e.g. Marcus Vance"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Receiving Bank Name
            </label>
            <input
              type="text"
              value={newBenBank}
              onChange={(e) => setNewBenBank(e.target.value)}
              placeholder="e.g. J.P. Morgan Chase"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Account / IBAN Number
            </label>
            <input
              type="text"
              value={newBenAcc}
              onChange={(e) => setNewBenAcc(e.target.value)}
              placeholder="e.g. 9842109284"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default CustomerDashboard;
