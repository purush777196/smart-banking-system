import { ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { formatCurrency, formatNumber } from '../../utils/formatters';

export const StatCard = ({
  title,
  value,
  isCurrency = false,
  compact = false,
  change,
  isPositive = true,
  period = 'vs last month',
  icon: Icon,
  className = '',
  badge,
}) => {
  const displayValue = isCurrency
    ? typeof value === 'number'
      ? formatCurrency(value, 'USD', compact)
      : value
    : typeof value === 'number'
    ? formatNumber(value, compact)
    : value;

  return (
    <div
      className={`relative overflow-hidden bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 rounded-2xl p-5 shadow-sm hover:shadow-md transition-all duration-200 ${className}`}
    >
      <div className="flex items-start justify-between">
        <div>
          <span className="text-xs font-semibold tracking-wider text-slate-500 dark:text-slate-400 uppercase">
            {title}
          </span>
          <div className="mt-2 text-2xl md:text-3xl font-bold tracking-tight text-slate-900 dark:text-white">
            {displayValue}
          </div>
        </div>

        {Icon && (
          <div className="p-3 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 border border-emerald-100 dark:border-emerald-900/40 shadow-xs shrink-0">
            <Icon className="w-6 h-6" />
          </div>
        )}
      </div>

      <div className="mt-4 flex items-center justify-between text-xs">
        {change !== undefined ? (
          <div className="flex items-center gap-1">
            <span
              className={`inline-flex items-center font-semibold px-1.5 py-0.5 rounded-md ${
                isPositive
                  ? 'text-emerald-700 bg-emerald-50 dark:text-emerald-300 dark:bg-emerald-950/60'
                  : 'text-rose-700 bg-rose-50 dark:text-rose-300 dark:bg-rose-950/60'
              }`}
            >
              {isPositive ? (
                <ArrowUpRight className="w-3.5 h-3.5 mr-0.5" />
              ) : (
                <ArrowDownRight className="w-3.5 h-3.5 mr-0.5" />
              )}
              {change > 0 ? `+${change}%` : `${change}%`}
            </span>
            <span className="text-slate-500 dark:text-slate-400 ml-1">{period}</span>
          </div>
        ) : badge ? (
          <span className="text-xs text-slate-500 dark:text-slate-400">{badge}</span>
        ) : (
          <span className="text-slate-500 dark:text-slate-400">{period}</span>
        )}
      </div>
    </div>
  );
};

export default StatCard;

