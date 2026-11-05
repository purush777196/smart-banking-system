import { useState } from 'react';
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
import DataTable from '../../components/tables/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';
import { copyToClipboard } from '../../utils/helpers';
import { formatCurrency, formatDate } from '../../utils/formatters';
import {
  CUSTOMER_STATS,
  CUSTOMER_ACCOUNTS,
  CUSTOMER_SPENDING_CATEGORIES,
  CUSTOMER_MONTHLY_SPEND_TREND,
  CUSTOMER_BENEFICIARIES,
  CUSTOMER_RECENT_TRANSACTIONS,
} from '../../data/mockCustomer';

export const CustomerDashboard = () => {
  const { addToast } = useToast();
  const [copiedId, setCopiedId] = useState(null);

  // Modals state
  const [isDepositModalOpen, setIsDepositModalOpen] = useState(false);
  const [isWithdrawModalOpen, setIsWithdrawModalOpen] = useState(false);
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [isBeneficiaryModalOpen, setIsBeneficiaryModalOpen] = useState(false);

  // Transaction form states
  const [transferAmount, setTransferAmount] = useState('');
  const [transferRecipient, setTransferRecipient] = useState(CUSTOMER_BENEFICIARIES[0].name);
  const [depositAmount, setDepositAmount] = useState('');
  const [withdrawAmount, setWithdrawAmount] = useState('');

  const [transactions, setTransactions] = useState(CUSTOMER_RECENT_TRANSACTIONS);
  const [beneficiaries, setBeneficiaries] = useState(CUSTOMER_BENEFICIARIES);

  // New beneficiary form
  const [newBenName, setNewBenName] = useState('');
  const [newBenBank, setNewBenBank] = useState('');
  const [newBenAcc, setNewBenAcc] = useState('');

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

    const newTx = {
      id: `TX-C${Math.floor(1000 + Math.random() * 9000)}`,
      title: `Transfer to ${transferRecipient}`,
      category: 'Transfer',
      amount: -amt,
      isCredit: false,
      date: new Date().toISOString().split('T')[0],
      status: 'Completed',
      account: 'Checking •••• 8821',
    };

    setTransactions([newTx, ...transactions]);
    addToast(`Successfully transferred ${formatCurrency(amt)} to ${transferRecipient}!`, 'success');
    setTransferAmount('');
    setIsTransferModalOpen(false);
  };

  const handleDeposit = (e) => {
    e.preventDefault();
    const amt = parseFloat(depositAmount);
    if (!amt || amt <= 0) return;

    const newTx = {
      id: `TX-C${Math.floor(1000 + Math.random() * 9000)}`,
      title: 'Mobile Check / Wire Deposit',
      category: 'Deposit',
      amount: amt,
      isCredit: true,
      date: new Date().toISOString().split('T')[0],
      status: 'Completed',
      account: 'Checking •••• 8821',
    };

    setTransactions([newTx, ...transactions]);
    addToast(`Successfully deposited ${formatCurrency(amt)} into Premier Checking!`, 'success');
    setDepositAmount('');
    setIsDepositModalOpen(false);
  };

  const handleWithdraw = (e) => {
    e.preventDefault();
    const amt = parseFloat(withdrawAmount);
    if (!amt || amt <= 0) return;

    const newTx = {
      id: `TX-C${Math.floor(1000 + Math.random() * 9000)}`,
      title: 'ATM Cash Withdrawal Request',
      category: 'Withdrawal',
      amount: -amt,
      isCredit: false,
      date: new Date().toISOString().split('T')[0],
      status: 'Completed',
      account: 'Checking •••• 8821',
    };

    setTransactions([newTx, ...transactions]);
    addToast(`Dispensed ${formatCurrency(amt)} via digital cardless PIN.`, 'success');
    setWithdrawAmount('');
    setIsWithdrawModalOpen(false);
  };

  const handleAddBeneficiary = (e) => {
    e.preventDefault();
    if (!newBenName.trim() || !newBenAcc.trim()) return;

    const newBen = {
      id: `BEN-0${beneficiaries.length + 1}`,
      name: newBenName,
      bank: newBenBank || 'Finova Core',
      account: `•••• ${newBenAcc.slice(-4)}`,
      avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80',
    };

    setBeneficiaries([...beneficiaries, newBen]);
    addToast(`Added ${newBenName} to your trusted beneficiaries`, 'success');
    setNewBenName('');
    setNewBenBank('');
    setNewBenAcc('');
    setIsBeneficiaryModalOpen(false);
  };

  const handleExportStatement = () => {
    addToast('Generating encrypted monthly statement (PDF)...', 'info');
    setTimeout(() => {
      addToast('Statement downloaded: Finova_Statement_Oct2026.pdf', 'success');
    }, 1200);
  };

  const transactionColumns = [
    { key: 'id', header: 'Reference' },
    {
      key: 'title',
      header: 'Description',
      render: (title, row) => (
        <div>
          <span className="font-semibold text-slate-900 dark:text-white block">{title}</span>
          <span className="text-2xs text-slate-400">{row.account}</span>
        </div>
      ),
    },
    { key: 'category', header: 'Category' },
    {
      key: 'amount',
      header: 'Amount',
      align: 'right',
      render: (amount, row) => (
        <span
          className={`font-bold ${
            row.isCredit
              ? 'text-emerald-600 dark:text-emerald-400'
              : 'text-slate-900 dark:text-white'
          }`}
        >
          {row.isCredit ? `+${formatCurrency(amount)}` : formatCurrency(amount)}
        </span>
      ),
    },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    {
      key: 'date',
      header: 'Date',
      render: (d) => formatDate(d),
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Banner & Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              Welcome back, Sophia Rodriguez
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              Signature Tier
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Primary Account: FIN-8429-0192 • Priority Concierge Active
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => setIsDepositModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition-colors flex items-center gap-1.5"
          >
            <PlusCircle className="w-4 h-4 text-emerald-600" />
            <span>Deposit</span>
          </button>
          <button
            type="button"
            onClick={() => setIsWithdrawModalOpen(true)}
            className="px-3.5 py-2 text-xs font-bold rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 transition-colors flex items-center gap-1.5"
          >
            <ArrowUpRight className="w-4 h-4" />
            <span>Withdraw</span>
          </button>
          <button
            type="button"
            onClick={() => setIsTransferModalOpen(true)}
            className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5"
          >
            <Send className="w-4 h-4" />
            <span>Transfer Money</span>
          </button>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Available Balance"
          value={CUSTOMER_STATS.availableBalance.value}
          isCurrency
          change={CUSTOMER_STATS.availableBalance.change}
          isPositive={CUSTOMER_STATS.availableBalance.isPositive}
          period={CUSTOMER_STATS.availableBalance.period}
          icon={Wallet}
        />
        <StatCard
          title="Total Deposits"
          value={CUSTOMER_STATS.totalDeposits.value}
          isCurrency
          change={CUSTOMER_STATS.totalDeposits.change}
          isPositive={CUSTOMER_STATS.totalDeposits.isPositive}
          period={CUSTOMER_STATS.totalDeposits.period}
          icon={ArrowDownLeft}
        />
        <StatCard
          title="Total Withdrawals"
          value={CUSTOMER_STATS.totalWithdrawals.value}
          isCurrency
          change={CUSTOMER_STATS.totalWithdrawals.change}
          isPositive={CUSTOMER_STATS.totalWithdrawals.isPositive}
          period={CUSTOMER_STATS.totalWithdrawals.period}
          icon={ArrowUpRight}
        />
        <StatCard
          title="Monthly Spending"
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
          <h3 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <CreditCard className="w-5 h-5 text-emerald-600" />
            Your Finova Bank Accounts
          </h3>
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
          {CUSTOMER_ACCOUNTS.map((acc) => (
            <div
              key={acc.id}
              className={`p-5 rounded-2xl border transition-all duration-200 relative overflow-hidden ${
                acc.isPrimary
                  ? 'bg-gradient-to-br from-[#0b132b] to-[#152a55] text-white border-slate-700 shadow-md'
                  : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
              }`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <span
                    className={`text-2xs font-bold uppercase tracking-wider ${
                      acc.isPrimary ? 'text-emerald-400' : 'text-slate-400'
                    }`}
                  >
                    {acc.type} Account
                  </span>
                  <h4
                    className={`text-sm font-bold mt-0.5 ${
                      acc.isPrimary ? 'text-white' : 'text-slate-900 dark:text-white'
                    }`}
                  >
                    {acc.name}
                  </h4>
                </div>
                {acc.isPrimary && (
                  <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-400/30">
                    Primary
                  </span>
                )}
              </div>

              <div className="mt-4">
                <span
                  className={`text-xs ${
                    acc.isPrimary ? 'text-slate-300' : 'text-slate-400'
                  }`}
                >
                  Balance
                </span>
                <div
                  className={`text-2xl font-black ${
                    acc.isPrimary ? 'text-white' : 'text-slate-900 dark:text-white'
                  }`}
                >
                  {formatCurrency(acc.balance)}
                </div>
              </div>

              <div
                className={`mt-4 pt-3 border-t text-xs space-y-1.5 ${
                  acc.isPrimary
                    ? 'border-slate-700 text-slate-300'
                    : 'border-slate-100 dark:border-slate-800 text-slate-500 dark:text-slate-400'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span>Acc: {acc.accountNumber}</span>
                  <button
                    type="button"
                    onClick={() => handleCopy(acc.accountNumber, `acc-${acc.id}`)}
                    className="hover:text-emerald-400"
                  >
                    {copiedId === `acc-${acc.id}` ? (
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                    ) : (
                      <Copy className="w-3.5 h-3.5" />
                    )}
                  </button>
                </div>
                <div className="flex items-center justify-between">
                  <span className="truncate max-w-[190px]">IBAN: {acc.iban}</span>
                  <button
                    type="button"
                    onClick={() => handleCopy(acc.iban, `iban-${acc.id}`)}
                    className="hover:text-emerald-400"
                  >
                    {copiedId === `iban-${acc.id}` ? (
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                    ) : (
                      <Copy className="w-3.5 h-3.5" />
                    )}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

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
            subtitle="Monthly comparison of direct deposits and expenses ($)"
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

      {/* Recent Transactions Table */}
      <DataTable
        title="Recent Account Transactions"
        subtitle="Real-time statement feed across all linked checking & savings products"
        columns={transactionColumns}
        data={transactions}
      />

      {/* Transfer Modal */}
      <Modal
        isOpen={isTransferModalOpen}
        onClose={() => setIsTransferModalOpen(false)}
        title="Transfer Funds"
        subtitle="Instant wire or internal Finova transfer"
        maxWidth="max-w-md"
        footer={
          <>
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
          </>
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
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
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
              Transfer Amount ($)
            </label>
            <input
              type="number"
              step="0.01"
              value={transferAmount}
              onChange={(e) => setTransferAmount(e.target.value)}
              placeholder="e.g. 250.00"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </form>
      </Modal>

      {/* Deposit Modal */}
      <Modal
        isOpen={isDepositModalOpen}
        onClose={() => setIsDepositModalOpen(false)}
        title="Deposit Funds"
        subtitle="Simulate mobile check or ACH direct deposit"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleDeposit}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Process Deposit
          </button>
        }
      >
        <div className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Deposit Amount ($)
            </label>
            <input
              type="number"
              step="0.01"
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
              placeholder="e.g. 1000.00"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </div>
      </Modal>

      {/* Withdraw Modal */}
      <Modal
        isOpen={isWithdrawModalOpen}
        onClose={() => setIsWithdrawModalOpen(false)}
        title="ATM / Branch Withdrawal"
        subtitle="Generate one-time cardless OTP or schedule OTC cash pickup"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleWithdraw}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Generate Withdrawal Code
          </button>
        }
      >
        <div className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Withdrawal Amount ($)
            </label>
            <input
              type="number"
              step="0.01"
              value={withdrawAmount}
              onChange={(e) => setWithdrawAmount(e.target.value)}
              placeholder="e.g. 200.00"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </div>
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
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
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
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
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
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default CustomerDashboard;
