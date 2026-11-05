export const StatusBadge = ({ status, variant, className = '' }) => {
  if (!status) return null;

  const getStyle = () => {
    const s = String(status).toLowerCase();
    
    if (variant === 'success' || s.includes('complete') || s.includes('active') || s.includes('approved') || s.includes('verified') || s.includes('on time') || s.includes('low') || s.includes('on duty') || s.includes('exceeded')) {
      return 'bg-emerald-50 text-emerald-700 border-emerald-200 dark:bg-emerald-950/40 dark:text-emerald-300 dark:border-emerald-800/50';
    }
    if (variant === 'danger' || s.includes('critical') || s.includes('high') || s.includes('danger') || s.includes('restricted') || s.includes('reject') || s.includes('default') || s.includes('flagged') || s.includes('freeze')) {
      return 'bg-rose-50 text-rose-700 border-rose-200 dark:bg-rose-950/40 dark:text-rose-300 dark:border-rose-800/50';
    }
    if (variant === 'warning' || s.includes('medium') || s.includes('under review') || s.includes('review') || s.includes('warning') || s.includes('hold') || s.includes('escalated')) {
      return 'bg-amber-50 text-amber-700 border-amber-200 dark:bg-amber-950/40 dark:text-amber-300 dark:border-amber-800/50';
    }
    if (variant === 'info' || s.includes('pending') || s.includes('progress') || s.includes('scheduled') || s.includes('open') || s.includes('in meeting')) {
      return 'bg-sky-50 text-sky-700 border-sky-200 dark:bg-sky-950/40 dark:text-sky-300 dark:border-sky-800/50';
    }
    
    return 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700';
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium rounded-full border transition-colors ${getStyle()} ${className}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current opacity-80" />
      {status}
    </span>
  );
};

export default StatusBadge;

