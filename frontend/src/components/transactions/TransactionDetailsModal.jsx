import { useState } from 'react';
import { Copy, Check, ArrowDownLeft, ArrowUpRight, ArrowLeftRight, Clock, ShieldCheck, Hash, FileText } from 'lucide-react';
import Modal from '../common/Modal';
import StatusBadge from '../common/StatusBadge';
import { formatCurrency, formatDateTime } from '../../utils/formatters';
import { copyToClipboard } from '../../utils/helpers';
import { maskAccountNumber } from '../../services/transactionService';

export const TransactionDetailsModal = ({ isOpen, onClose, transaction }) => {
  const [copiedRef, setCopiedRef] = useState(false);

  if (!transaction) return null;

  const handleCopy = async () => {
    const success = await copyToClipboard(transaction.transactionReference);
    if (success) {
      setCopiedRef(true);
      setTimeout(() => setCopiedRef(false), 2000);
    }
  };

  const isCredit =
    transaction.transactionType === 'DEPOSIT' ||
    transaction.transactionType === 'TRANSFER_CREDIT';

  const getTypeIcon = () => {
    switch (transaction.transactionType) {
      case 'DEPOSIT':
        return <ArrowDownLeft className="w-5 h-5 text-emerald-500" />;
      case 'WITHDRAWAL':
        return <ArrowUpRight className="w-5 h-5 text-rose-500" />;
      default:
        return <ArrowLeftRight className="w-5 h-5 text-sky-500" />;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Transaction Ledger Details"
      subtitle="Immutable cryptographic ledger record verified by Finova Core"
      maxWidth="max-w-lg"
      footer={
        <div className="flex items-center justify-between w-full">
          <button
            type="button"
            onClick={handleCopy}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200 transition-colors flex items-center gap-1.5"
          >
            {copiedRef ? (
              <>
                <Check className="w-4 h-4 text-emerald-500" />
                <span>Copied Reference</span>
              </>
            ) : (
              <>
                <Copy className="w-4 h-4" />
                <span>Copy Reference</span>
              </>
            )}
          </button>
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-bold text-white bg-slate-800 hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600 rounded-xl transition-colors"
          >
            Close Details
          </button>
        </div>
      }
    >
      <div className="space-y-5">
        {/* Top Header Card */}
        <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-white dark:bg-slate-700 border border-slate-200 dark:border-slate-600 flex items-center justify-center shadow-xs">
              {getTypeIcon()}
            </div>
            <div>
              <span className="text-2xs font-bold text-slate-400 uppercase tracking-wider block">
                {transaction.transactionType?.replace('_', ' ')}
              </span>
              <span className="text-xl font-black text-slate-900 dark:text-white">
                {isCredit ? '+' : '-'}{formatCurrency(transaction.amount, transaction.currency || 'INR')}
              </span>
            </div>
          </div>
          <StatusBadge status={transaction.transactionStatus} />
        </div>

        {/* Ledger Breakdown Fields */}
        <div className="space-y-2.5 text-xs">
          {/* Reference */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-500 dark:text-slate-400 flex items-center gap-1.5">
              <Hash className="w-3.5 h-3.5" /> Reference
            </span>
            <span className="font-mono font-bold text-slate-900 dark:text-slate-100 select-all">
              {transaction.transactionReference}
            </span>
          </div>

          {/* Account */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-500 dark:text-slate-400 flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5" /> Account
            </span>
            <span className="font-semibold text-slate-900 dark:text-slate-100">
              {maskAccountNumber(transaction.accountNumber)}
            </span>
          </div>

          {/* Timestamp */}
          <div className="flex items-center justify-between p-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800">
            <span className="text-slate-500 dark:text-slate-400 flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5" /> Booked At
            </span>
            <span className="font-medium text-slate-700 dark:text-slate-300">
              {formatDateTime(transaction.createdAt)}
            </span>
          </div>

          {/* Description */}
          {transaction.description && (
            <div className="p-3 rounded-xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-800 space-y-1">
              <span className="text-slate-500 dark:text-slate-400 flex items-center gap-1.5">
                <FileText className="w-3.5 h-3.5" /> Description / Memo
              </span>
              <p className="text-slate-800 dark:text-slate-200 font-medium pl-5">
                {transaction.description}
              </p>
            </div>
          )}

          {/* Balance Snapshots (Auditable Financial Ledger) */}
          <div className="p-4 rounded-xl bg-emerald-50/50 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/40 space-y-2">
            <span className="text-2xs font-bold text-emerald-800 dark:text-emerald-300 uppercase tracking-wider block">
              Ledger Balance Snapshots
            </span>
            <div className="grid grid-cols-2 gap-4 pt-1">
              <div>
                <span className="text-slate-500 dark:text-slate-400 block text-2xs">Balance Before</span>
                <span className="font-bold text-sm text-slate-900 dark:text-slate-100">
                  {transaction.balanceBefore !== undefined && transaction.balanceBefore !== null
                    ? formatCurrency(transaction.balanceBefore, transaction.currency || 'INR')
                    : '—'}
                </span>
              </div>
              <div>
                <span className="text-slate-500 dark:text-slate-400 block text-2xs">Balance After</span>
                <span className="font-bold text-sm text-emerald-700 dark:text-emerald-400">
                  {transaction.balanceAfter !== undefined && transaction.balanceAfter !== null
                    ? formatCurrency(transaction.balanceAfter, transaction.currency || 'INR')
                    : '—'}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  );
};

export default TransactionDetailsModal;

