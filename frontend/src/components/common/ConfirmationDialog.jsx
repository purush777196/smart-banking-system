import { AlertTriangle, CheckCircle, Info, XCircle } from 'lucide-react';
import Modal from './Modal';

export const ConfirmationDialog = ({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirm Action',
  message = 'Are you sure you want to proceed with this operation?',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  type = 'warning',
  isLoading = false,
}) => {
  const icons = {
    warning: <AlertTriangle className="w-6 h-6 text-amber-600 dark:text-amber-400" />,
    danger: <XCircle className="w-6 h-6 text-rose-600 dark:text-rose-400" />,
    info: <Info className="w-6 h-6 text-sky-600 dark:text-sky-400" />,
    success: <CheckCircle className="w-6 h-6 text-emerald-600 dark:text-emerald-400" />,
  };

  const confirmColors = {
    warning: 'bg-amber-600 hover:bg-amber-700 text-white',
    danger: 'bg-rose-600 hover:bg-rose-700 text-white',
    info: 'bg-sky-600 hover:bg-sky-700 text-white',
    success: 'bg-emerald-600 hover:bg-emerald-700 text-white',
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      maxWidth="max-w-md"
      footer={
        <>
          <button
            type="button"
            onClick={onClose}
            disabled={isLoading}
            className="px-4 py-2 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors"
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={isLoading}
            className={`px-4 py-2 text-sm font-medium rounded-xl shadow-xs transition-colors flex items-center gap-2 ${confirmColors[type]}`}
          >
            {isLoading ? (
              <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : null}
            {confirmText}
          </button>
        </>
      }
    >
      <div className="flex items-start gap-4">
        <div className="p-3 rounded-xl bg-slate-100 dark:bg-slate-800 shrink-0">
          {icons[type]}
        </div>
        <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed pt-1">
          {message}
        </p>
      </div>
    </Modal>
  );
};

export default ConfirmationDialog;

