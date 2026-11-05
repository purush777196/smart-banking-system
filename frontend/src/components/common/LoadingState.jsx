export const LoadingState = ({
  type = 'spinner', // 'spinner' | 'card' | 'table'
  rows = 4,
  message = 'Loading banking records...',
}) => {
  if (type === 'card') {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5 animate-pulse">
        {Array.from({ length: 4 }).map((_, i) => (
          <div
            key={i}
            className="h-32 bg-slate-200 dark:bg-slate-800 rounded-2xl"
          />
        ))}
      </div>
    );
  }

  if (type === 'table') {
    return (
      <div className="w-full bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-4 space-y-4 animate-pulse">
        <div className="h-8 bg-slate-200 dark:bg-slate-800 rounded-lg w-1/4" />
        <div className="space-y-2">
          {Array.from({ length: rows }).map((_, i) => (
            <div
              key={i}
              className="h-10 bg-slate-100 dark:bg-slate-800/60 rounded-lg w-full"
            />
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center py-12 space-y-3">
      <div className="w-8 h-8 border-3 border-emerald-500/30 border-t-emerald-600 rounded-full animate-spin" />
      <span className="text-xs font-medium text-slate-500 dark:text-slate-400">
        {message}
      </span>
    </div>
  );
};

export default LoadingState;

