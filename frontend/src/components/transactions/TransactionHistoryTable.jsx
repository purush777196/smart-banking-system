import { useState, useEffect } from 'react';
import {
  ArrowDownLeft,
  ArrowUpRight,
  ArrowLeftRight,
  Search,
  RefreshCw,
  Calendar,
  AlertCircle,
  Eye,
  SlidersHorizontal,
  X,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown,
} from 'lucide-react';
import StatusBadge from '../common/StatusBadge';
import TransactionDetailsModal from './TransactionDetailsModal';
import {
  getTransactionHistory,
  getTransactionByReference,
  maskAccountNumber,
} from '../../services/transactionService';
import { formatCurrency, formatDateTime } from '../../utils/formatters';

export const TransactionHistoryTable = ({
  accounts = [],
  selectedAccountId,
  onAccountChange,
}) => {
  // Current active account ID for history
  const activeAccountId =
    selectedAccountId || (accounts.length > 0 ? accounts[0].id : null);

  // Data state
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Pagination state (server-side, 0-indexed)
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Sorting state
  const [sortBy, setSortBy] = useState('createdAt');
  const [direction, setDirection] = useState('desc');

  // Filter state
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Reference Lookup state
  const [searchRef, setSearchRef] = useState('');
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState('');

  // Details modal state
  const [selectedTx, setSelectedTx] = useState(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  // Load transaction history via effect
  useEffect(() => {
    let isMounted = true;

    const fetchHistory = async () => {
      if (!activeAccountId) {
        if (isMounted) setTransactions([]);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const data = await getTransactionHistory(activeAccountId, {
          page,
          size,
          sortBy,
          direction,
          type: typeFilter,
          status: statusFilter,
          startDate: startDate || undefined,
          endDate: endDate || undefined,
        });

        if (isMounted) {
          setTransactions(data.content || []);
          setTotalPages(data.totalPages || 0);
          setTotalElements(data.totalElements || 0);
        }
      } catch (err) {
        if (isMounted) {
          const msg =
            err.response?.data?.message ||
            err.message ||
            'Unable to load transaction records. Please try again.';
          setError(msg);
          setTransactions([]);
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    fetchHistory();

    return () => {
      isMounted = false;
    };
  }, [activeAccountId, page, size, sortBy, direction, typeFilter, statusFilter, startDate, endDate, refreshTrigger]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(field);
      setDirection('desc');
    }
    setPage(0);
  };

  // Handle direct Reference Lookup
  const handleReferenceLookup = async (e) => {
    e.preventDefault();
    if (!searchRef.trim()) return;

    setSearchLoading(true);
    setSearchError('');

    try {
      const tx = await getTransactionByReference(searchRef.trim());
      setSelectedTx(tx);
      setIsDetailsOpen(true);
      setSearchRef('');
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.message ||
        `Transaction with reference "${searchRef}" was not found or access is restricted.`;
      setSearchError(msg);
    } finally {
      setSearchLoading(false);
    }
  };

  const handleRowClick = (tx) => {
    setSelectedTx(tx);
    setIsDetailsOpen(true);
  };

  const handleClearFilters = () => {
    setTypeFilter('ALL');
    setStatusFilter('ALL');
    setStartDate('');
    setEndDate('');
    setPage(0);
  };

  const hasActiveFilters =
    typeFilter !== 'ALL' ||
    statusFilter !== 'ALL' ||
    Boolean(startDate) ||
    Boolean(endDate);

  const selectedAccount = accounts.find(
    (a) => String(a.id) === String(activeAccountId)
  );

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-2xl shadow-xs overflow-hidden flex flex-col">
      {/* Top Header & Account Switcher */}
      <div className="p-5 border-b border-slate-100 dark:border-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Financial Transaction Ledger
            </h3>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
              {totalElements} total
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Audit-logged transaction history with server-side pagination & filtering
          </p>
        </div>

        {/* Account Selector & Controls */}
        <div className="flex flex-wrap items-center gap-2">
          {accounts.length > 1 && onAccountChange && (
            <select
              value={activeAccountId || ''}
              onChange={(e) => {
                onAccountChange(e.target.value);
                setPage(0);
              }}
              className="px-3 py-1.5 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none"
            >
              {accounts.map((acc) => (
                <option key={acc.id} value={acc.id}>
                  {acc.accountType} ({maskAccountNumber(acc.accountNumber)})
                </option>
              ))}
            </select>
          )}

          <button
            type="button"
            onClick={() => setShowFilters(!showFilters)}
            className={`px-3 py-1.5 text-xs font-semibold rounded-xl border transition-colors flex items-center gap-1.5 ${
              hasActiveFilters || showFilters
                ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border-emerald-300 dark:border-emerald-800'
                : 'border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300'
            }`}
          >
            <SlidersHorizontal className="w-3.5 h-3.5" />
            <span>Filters</span>
            {hasActiveFilters && (
              <span className="w-2 h-2 rounded-full bg-emerald-500" />
            )}
          </button>

          <button
            type="button"
            onClick={() => setRefreshTrigger((c) => c + 1)}
            disabled={loading}
            className="p-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-300 transition-colors disabled:opacity-50"
            title="Refresh transactions"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Reference Lookup Search Bar */}
      <div className="px-5 py-3 bg-slate-50/60 dark:bg-slate-800/30 border-b border-slate-100 dark:border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-3">
        <form
          onSubmit={handleReferenceLookup}
          className="flex items-center gap-2 w-full sm:max-w-md"
        >
          <div className="relative flex-1">
            <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={searchRef}
              onChange={(e) => setSearchRef(e.target.value)}
              placeholder="Lookup by Reference (e.g. TXN-20261106-ABCDEF)"
              className="w-full pl-8 pr-3 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-white placeholder:text-slate-400 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none"
            />
          </div>
          <button
            type="submit"
            disabled={searchLoading || !searchRef.trim()}
            className="px-3 py-1.5 text-xs font-bold text-white bg-slate-800 hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600 disabled:opacity-50 rounded-xl transition-colors"
          >
            {searchLoading ? 'Looking up...' : 'Lookup'}
          </button>
        </form>

        {searchError && (
          <div className="text-2xs font-medium text-rose-600 dark:text-rose-400 flex items-center gap-1">
            <AlertCircle className="w-3.5 h-3.5 shrink-0" />
            <span>{searchError}</span>
          </div>
        )}
      </div>

      {/* Expandable Filter Panel */}
      {showFilters && (
        <div className="p-4 bg-slate-50 dark:bg-slate-800/50 border-b border-slate-100 dark:border-slate-800 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 text-xs animate-in slide-in-from-top-2 duration-150">
          {/* Type Filter */}
          <div>
            <label className="block text-2xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
              Transaction Type
            </label>
            <select
              value={typeFilter}
              onChange={(e) => {
                setTypeFilter(e.target.value);
                setPage(0);
              }}
              className="w-full px-2.5 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 outline-none"
            >
              <option value="ALL">All Types</option>
              <option value="DEPOSIT">Deposit</option>
              <option value="WITHDRAWAL">Withdrawal</option>
              <option value="TRANSFER_DEBIT">Transfer (Debit)</option>
              <option value="TRANSFER_CREDIT">Transfer (Credit)</option>
            </select>
          </div>

          {/* Status Filter */}
          <div>
            <label className="block text-2xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
              Ledger Status
            </label>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
              className="w-full px-2.5 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 outline-none"
            >
              <option value="ALL">All Statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="PENDING">Pending</option>
              <option value="FAILED">Failed</option>
              <option value="REVERSED">Reversed</option>
            </select>
          </div>

          {/* Start Date */}
          <div>
            <label className="block text-2xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
              From Date
            </label>
            <input
              type="date"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value);
                setPage(0);
              }}
              className="w-full px-2.5 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 outline-none"
            />
          </div>

          {/* End Date */}
          <div>
            <label className="block text-2xs font-bold text-slate-600 dark:text-slate-400 uppercase tracking-wider mb-1">
              To Date
            </label>
            <input
              type="date"
              value={endDate}
              onChange={(e) => {
                setEndDate(e.target.value);
                setPage(0);
              }}
              className="w-full px-2.5 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 outline-none"
            />
          </div>

          {/* Clear Button */}
          <div className="flex items-end">
            <button
              type="button"
              onClick={handleClearFilters}
              disabled={!hasActiveFilters}
              className="w-full py-1.5 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 disabled:opacity-40 transition-colors flex items-center justify-center gap-1.5"
            >
              <X className="w-3.5 h-3.5" />
              <span>Reset Filters</span>
            </button>
          </div>
        </div>
      )}

      {/* Main Table Content */}
      <div className="overflow-x-auto min-h-[280px]">
        {loading ? (
          <div className="flex flex-col items-center justify-center p-12 text-slate-400 space-y-3">
            <RefreshCw className="w-6 h-6 animate-spin text-emerald-600" />
            <span className="text-xs font-medium">Fetching verified ledger transactions...</span>
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center p-12 text-center space-y-3">
            <div className="w-10 h-10 rounded-full bg-rose-50 dark:bg-rose-950/40 text-rose-600 flex items-center justify-center">
              <AlertCircle className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-rose-700 dark:text-rose-400">{error}</p>
              <button
                type="button"
                onClick={() => setRefreshTrigger((c) => c + 1)}
                className="mt-2 px-3 py-1 text-xs font-semibold text-emerald-600 hover:underline"
              >
                Retry Request
              </button>
            </div>
          </div>
        ) : transactions.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-12 text-slate-400 space-y-2 text-center">
            <Calendar className="w-8 h-8 text-slate-300 dark:text-slate-600" />
            <span className="text-xs font-bold text-slate-700 dark:text-slate-300">
              No transactions found
            </span>
            <p className="text-2xs text-slate-400 max-w-sm">
              {hasActiveFilters
                ? 'Try adjusting your filters or date range to view earlier entries.'
                : 'Deposit or transfer funds into this account to initialize your transaction history.'}
            </p>
          </div>
        ) : (
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-100 dark:border-slate-800 bg-slate-50/75 dark:bg-slate-800/40 text-2xs uppercase tracking-wider font-bold text-slate-500 dark:text-slate-400">
                <th
                  className="py-3 px-4 cursor-pointer hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
                  onClick={() => handleSort('transactionReference')}
                >
                  <span className="flex items-center gap-1">
                    <span>Reference</span>
                    <ArrowUpDown className="w-3 h-3 opacity-60" />
                  </span>
                </th>
                <th
                  className="py-3 px-4 cursor-pointer hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
                  onClick={() => handleSort('createdAt')}
                >
                  <span className="flex items-center gap-1">
                    <span>Date & Time</span>
                    <ArrowUpDown className="w-3 h-3 opacity-60" />
                  </span>
                </th>
                <th className="py-3 px-4">Type</th>
                <th className="py-3 px-4">Description</th>
                <th
                  className="py-3 px-4 text-right cursor-pointer hover:text-slate-700 dark:hover:text-slate-200 transition-colors"
                  onClick={() => handleSort('amount')}
                >
                  <span className="flex items-center justify-end gap-1">
                    <span>Amount</span>
                    <ArrowUpDown className="w-3 h-3 opacity-60" />
                  </span>
                </th>
                <th className="py-3 px-4 text-center">Status</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-xs">
              {transactions.map((tx) => {
                const isCredit =
                  tx.transactionType === 'DEPOSIT' ||
                  tx.transactionType === 'TRANSFER_CREDIT';

                return (
                  <tr
                    key={tx.id || tx.transactionReference}
                    onClick={() => handleRowClick(tx)}
                    className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 cursor-pointer transition-colors"
                  >
                    {/* Reference */}
                    <td className="py-3.5 px-4 font-mono font-bold text-slate-900 dark:text-slate-200 whitespace-nowrap">
                      {tx.transactionReference}
                    </td>

                    {/* Date */}
                    <td className="py-3.5 px-4 text-slate-600 dark:text-slate-400 whitespace-nowrap">
                      {formatDateTime(tx.createdAt)}
                    </td>

                    {/* Type */}
                    <td className="py-3.5 px-4 whitespace-nowrap">
                      <span className="inline-flex items-center gap-1.5 font-semibold text-slate-800 dark:text-slate-200">
                        {tx.transactionType === 'DEPOSIT' && (
                          <ArrowDownLeft className="w-3.5 h-3.5 text-emerald-600" />
                        )}
                        {tx.transactionType === 'WITHDRAWAL' && (
                          <ArrowUpRight className="w-3.5 h-3.5 text-rose-600" />
                        )}
                        {(tx.transactionType === 'TRANSFER_DEBIT' ||
                          tx.transactionType === 'TRANSFER_CREDIT') && (
                          <ArrowLeftRight className="w-3.5 h-3.5 text-sky-600" />
                        )}
                        <span>{tx.transactionType?.replace('_', ' ')}</span>
                      </span>
                    </td>

                    {/* Description */}
                    <td className="py-3.5 px-4 text-slate-600 dark:text-slate-300 max-w-xs truncate">
                      {tx.description || '—'}
                    </td>

                    {/* Amount */}
                    <td className="py-3.5 px-4 text-right font-black whitespace-nowrap">
                      <span
                        className={
                          isCredit
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : 'text-slate-900 dark:text-slate-200'
                        }
                      >
                        {isCredit ? '+' : '-'}
                        {formatCurrency(tx.amount, tx.currency || selectedAccount?.currency || 'INR')}
                      </span>
                    </td>

                    {/* Status */}
                    <td className="py-3.5 px-4 text-center whitespace-nowrap">
                      <StatusBadge status={tx.transactionStatus} />
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right whitespace-nowrap">
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleRowClick(tx);
                        }}
                        className="p-1.5 text-slate-400 hover:text-emerald-600 dark:hover:text-emerald-400 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg transition-colors"
                        title="View Ledger Details"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination Bar */}
      <div className="p-4 border-t border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/20 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
          <span>Rows per page:</span>
          <select
            value={size}
            onChange={(e) => {
              setSize(Number(e.target.value));
              setPage(0);
            }}
            className="px-2 py-1 text-xs rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200 outline-none"
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
            <option value={50}>50</option>
          </select>
          <span className="ml-2">
            Page <strong className="text-slate-800 dark:text-slate-200">{page + 1}</strong> of{' '}
            <strong className="text-slate-800 dark:text-slate-200">{Math.max(1, totalPages)}</strong>
          </span>
        </div>

        <div className="flex items-center gap-1.5">
          <button
            type="button"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0 || loading}
            className="px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 disabled:opacity-40 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors flex items-center gap-1"
          >
            <ChevronLeft className="w-3.5 h-3.5" />
            <span>Previous</span>
          </button>
          <button
            type="button"
            onClick={() => setPage((p) => p + 1)}
            disabled={page >= totalPages - 1 || loading || totalPages === 0}
            className="px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 disabled:opacity-40 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors flex items-center gap-1"
          >
            <span>Next</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Details Modal */}
      <TransactionDetailsModal
        isOpen={isDetailsOpen}
        onClose={() => setIsDetailsOpen(false)}
        transaction={selectedTx}
      />
    </div>
  );
};

export default TransactionHistoryTable;
