export const ChartCard = ({
  title,
  subtitle,
  action,
  children,
  className = '',
  height = 'h-72',
}) => {
  return (
    <div
      className={`bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-2xl p-5 shadow-xs flex flex-col justify-between ${className}`}
    >
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
        <div>
          <h3 className="text-base font-bold text-slate-900 dark:text-white">
            {title}
          </h3>
          {subtitle && (
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              {subtitle}
            </p>
          )}
        </div>
        {action && <div className="shrink-0">{action}</div>}
      </div>

      <div className={`w-full ${height} relative`}>
        {children}
      </div>
    </div>
  );
};

export default ChartCard;

