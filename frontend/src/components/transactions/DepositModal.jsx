import { useState } from 'react';
import { ArrowDownLeft, ShieldCheck, AlertCircle, CheckCircle2, Loader2, ArrowRight } from 'lucide-react';
import Modal from '../common/Modal';
import { depositFunds, generateIdempotencyKey, maskAccountNumber } from '../../services/transactionService';
import { formatCurrency } from '../../utils/formatters';

export const DepositModal = ({
  isOpen,
  onClose,
  accounts = [],
  selectedAccountId,
  onSuccess,
}) => {
  const [targetAccountId, setTargetAccountId] = useState(() => {
    if (selectedAccountId) return String(selectedAccountId);
    if (accounts.length > 0) return String(accounts[0].id);
    return '';
  });
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [idempotencyKey] = useState(() => generateIdempotencyKey('FIN-DEP'));

  // UI state: 'input' | 'review' | 'success'
  const [step, setStep] = useState('input');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [validationError, setValidationError] = useState('');
  const [successResult, setSuccessResult] = useState(null);

  const selectedAccount = accounts.find(
    (acc) => String(acc.id) === String(targetAccountId)
  ) || accounts[0];

  const validateInput = () => {
    setValidationError('');
    setErrorMessage('');

    if (!targetAccountId) {
      setValidationError('Please select a target account');
      return false;
    }

    const num = parseFloat(amount);
    if (!amount || isNaN(num) || num <= 0) {
      setValidationError('Deposit amount must be strictly greater than 0');
      return false;
    }

    // Check scale (at most 2 decimals)
    const parts = amount.toString().split('.');
    if (parts.length > 1 && parts[1].length > 2) {
      setValidationError('Amount cannot have more than 2 decimal places');
      return false;
    }

    if (num > 1000000000) {
      setValidationError('Amount exceeds single transaction limit');
      return false;
    }

    if (description && description.length > 255) {
      setValidationError('Description cannot exceed 255 characters');
      return false;
    }

    return true;
  };

  const handleProceedToReview = (e) => {
    e.preventDefault();
    if (validateInput()) {
      setStep('review');
    }
  };

  const handleConfirmDeposit = async () => {
    if (isSubmitting) return;
    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const response = await depositFunds(targetAccountId, {
        amount: parseFloat(amount),
        description: description.trim(),
        idempotencyKey,
      });

      setSuccessResult(response);
      setStep('success');
      if (onSuccess) {
        onSuccess(response);
      }
    } catch (err) {
      const serverMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Failed to process deposit. Please check account status and try again.';
      setErrorMessage(serverMsg);
      setStep('input');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (isSubmitting) return;
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Deposit Funds"
      subtitle="Fund your Finova account with atomic ledger recording"
      maxWidth="max-w-md"
      footer={
        step === 'input' ? (
          <div className="flex items-center justify-between w-full">
            <button
              type="button"
              onClick={handleClose}
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:text-slate-800 transition-colors"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleProceedToReview}
              className="px-4 py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl transition-all flex items-center gap-1.5 shadow-xs"
            >
              <span>Review Deposit</span>
              <ArrowRight className="w-3.5 h-3.5" />
            </button>
          </div>
        ) : step === 'review' ? (
          <div className="flex items-center justify-between w-full">
            <button
              type="button"
              onClick={() => setStep('input')}
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-400 hover:text-slate-800 transition-colors"
            >
              Back to Edit
            </button>
            <button
              type="button"
              onClick={handleConfirmDeposit}
              disabled={isSubmitting}
              className="px-5 py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 disabled:opacity-60 rounded-xl transition-all flex items-center gap-2 shadow-xs"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  <span>Processing...</span>
                </>
              ) : (
                <span>Confirm & Post Deposit</span>
              )}
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={handleClose}
            className="w-full py-2.5 text-xs font-bold text-white bg-slate-900 hover:bg-slate-800 dark:bg-slate-800 dark:hover:bg-slate-700 rounded-xl transition-colors"
          >
            Done
          </button>
        )
      }
    >
      {/* Error alert */}
      {errorMessage && (
        <div className="mb-4 p-3.5 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800/50 flex items-start gap-2.5 text-rose-700 dark:text-rose-300 text-xs">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <div className="flex-1 font-medium">{errorMessage}</div>
        </div>
      )}

      {/* Validation alert */}
      {validationError && (
        <div className="mb-4 p-3 rounded-xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800/50 flex items-start gap-2 text-amber-700 dark:text-amber-300 text-xs">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <span className="font-medium">{validationError}</span>
        </div>
      )}

      {/* STEP 1: Input form */}
      {step === 'input' && (
        <form onSubmit={handleProceedToReview} className="space-y-4">
          {/* Target Account Selector */}
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1.5">
              Select Destination Account
            </label>
            {accounts.length > 1 ? (
              <select
                value={targetAccountId}
                onChange={(e) => setTargetAccountId(e.target.value)}
                className="w-full px-3.5 py-2.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white font-medium focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition-all"
              >
                {accounts.map((acc) => (
                  <option key={acc.id} value={acc.id}>
                    {acc.accountType} ({maskAccountNumber(acc.accountNumber)}) — {formatCurrency(acc.balance, acc.currency || 'INR')}
                  </option>
                ))}
              </select>
            ) : selectedAccount ? (
              <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 flex items-center justify-between text-xs">
                <div>
                  <span className="font-bold text-slate-900 dark:text-white block">
                    {selectedAccount.accountType} Account
                  </span>
                  <span className="text-2xs text-slate-400 font-mono">
                    {maskAccountNumber(selectedAccount.accountNumber)}
                  </span>
                </div>
                <div className="text-right">
                  <span className="text-2xs text-slate-400 block">Available</span>
                  <span className="font-bold text-slate-900 dark:text-white">
                    {formatCurrency(selectedAccount.balance, selectedAccount.currency || 'INR')}
                  </span>
                </div>
              </div>
            ) : null}
          </div>

          {/* Amount */}
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1.5">
              Deposit Amount
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-sm font-bold text-slate-400">
                {selectedAccount?.currency === 'INR' ? '₹' : '$'}
              </span>
              <input
                type="number"
                step="0.01"
                min="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
                className="w-full pl-8 pr-4 py-2.5 text-sm font-bold rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition-all"
                required
              />
            </div>
            <p className="text-2xs text-slate-400 mt-1">
              Minimum 0.01 • Strictly maximum 2 decimal places
            </p>
          </div>

          {/* Optional Description */}
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1.5">
              Description / Note <span className="text-slate-400 font-normal">(Optional)</span>
            </label>
            <input
              type="text"
              maxLength={255}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="e.g. Salary direct deposit, savings allocation"
              className="w-full px-3.5 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-400 focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500 outline-none transition-all"
            />
          </div>

          {/* Security notice */}
          <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200/60 dark:border-slate-800 flex items-center gap-2 text-2xs text-slate-500 dark:text-slate-400">
            <ShieldCheck className="w-4 h-4 text-emerald-600 shrink-0" />
            <span>Protected by Finova atomic ledger & idempotency duplicate-submission filter.</span>
          </div>
        </form>
      )}

      {/* STEP 2: Review & Confirmation */}
      {step === 'review' && (
        <div className="space-y-4">
          <div className="p-4 rounded-2xl bg-emerald-50/60 dark:bg-emerald-950/20 border border-emerald-100 dark:border-emerald-900/40 text-center">
            <span className="text-2xs font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-400 block mb-1">
              Total Deposit Amount
            </span>
            <span className="text-2xl font-black text-slate-900 dark:text-white">
              {formatCurrency(parseFloat(amount), selectedAccount?.currency || 'INR')}
            </span>
          </div>

          <div className="space-y-2 text-xs">
            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
              <span className="text-slate-500 dark:text-slate-400">Destination Account</span>
              <span className="font-semibold text-slate-900 dark:text-white">
                {selectedAccount?.accountType} ({maskAccountNumber(selectedAccount?.accountNumber)})
              </span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
              <span className="text-slate-500 dark:text-slate-400">Operation Type</span>
              <span className="font-semibold text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                <ArrowDownLeft className="w-3.5 h-3.5" /> DEPOSIT
              </span>
            </div>

            {description && (
              <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                <span className="text-slate-500 dark:text-slate-400 block mb-0.5">Description</span>
                <span className="font-medium text-slate-900 dark:text-white">{description}</span>
              </div>
            )}

            <div className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50">
              <span className="text-slate-500 dark:text-slate-400">Idempotency Key</span>
              <span className="font-mono text-2xs text-slate-600 dark:text-slate-400 truncate max-w-[200px]">
                {idempotencyKey}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* STEP 3: Success Result */}
      {step === 'success' && successResult && (
        <div className="space-y-4 text-center py-2">
          <div className="w-12 h-12 rounded-full bg-emerald-100 dark:bg-emerald-950/60 border border-emerald-300 dark:border-emerald-700 flex items-center justify-center mx-auto text-emerald-600 dark:text-emerald-400">
            <CheckCircle2 className="w-7 h-7" />
          </div>

          <div>
            <h4 className="text-base font-bold text-slate-900 dark:text-white">
              Deposit Completed Successfully!
            </h4>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
              Ledger record confirmed and balance updated atomically.
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 text-left space-y-2 text-xs">
            <div className="flex items-center justify-between">
              <span className="text-slate-500 dark:text-slate-400">Reference:</span>
              <span className="font-mono font-bold text-slate-900 dark:text-white select-all">
                {successResult.transactionReference}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-500 dark:text-slate-400">Deposited:</span>
              <span className="font-bold text-emerald-600 dark:text-emerald-400">
                +{formatCurrency(successResult.amount, successResult.currency || 'INR')}
              </span>
            </div>
            <div className="flex items-center justify-between pt-1 border-t border-slate-200 dark:border-slate-700">
              <span className="text-slate-500 dark:text-slate-400">Updated Balance:</span>
              <span className="font-black text-sm text-slate-900 dark:text-white">
                {formatCurrency(successResult.balanceAfter, successResult.currency || 'INR')}
              </span>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
};

export default DepositModal;
