/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useCallback } from 'react';
import { CheckCircle2, AlertCircle, Info, XCircle, X } from 'lucide-react';

export const ToastContext = createContext();

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'success', duration = 3500) => {
    const id = Date.now() + Math.random().toString(36).substring(2, 9);
    setToasts((prev) => [...prev, { id, message, type }]);

    if (duration > 0) {
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, duration);
    }
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ addToast, removeToast }}>
      {children}
      {/* Toast container */}
      <div className="fixed bottom-5 right-5 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none">
        {toasts.map((toast) => {
          const icons = {
            success: <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0" />,
            error: <XCircle className="w-5 h-5 text-rose-500 shrink-0" />,
            warning: <AlertCircle className="w-5 h-5 text-amber-500 shrink-0" />,
            info: <Info className="w-5 h-5 text-sky-500 shrink-0" />,
          };

          const borderColors = {
            success: 'border-emerald-500/20 bg-white dark:bg-slate-900 shadow-emerald-500/5',
            error: 'border-rose-500/20 bg-white dark:bg-slate-900 shadow-rose-500/5',
            warning: 'border-amber-500/20 bg-white dark:bg-slate-900 shadow-amber-500/5',
            info: 'border-sky-500/20 bg-white dark:bg-slate-900 shadow-sky-500/5',
          };

          return (
            <div
              key={toast.id}
              className={`pointer-events-auto flex items-center justify-between p-4 rounded-xl border shadow-lg backdrop-blur-sm transition-all duration-300 transform translate-y-0 ${borderColors[toast.type]}`}
            >
              <div className="flex items-center gap-3">
                {icons[toast.type]}
                <p className="text-sm font-medium text-slate-800 dark:text-slate-100">{toast.message}</p>
              </div>
              <button
                onClick={() => removeToast(toast.id)}
                className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors ml-3"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};
