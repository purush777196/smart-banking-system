import { useState } from 'react';
import { Trash2, Bell, AlertTriangle, ShieldAlert, FileText, CheckCircle2 } from 'lucide-react';
import { MOCK_NOTIFICATIONS } from '../../data/mockNotifications';

export const NotificationPanel = ({ onClose }) => {
  const [notifications, setNotifications] = useState(MOCK_NOTIFICATIONS);
  const [filter, setFilter] = useState('all'); // 'all' | 'unread'

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAllAsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const clearAll = () => {
    setNotifications([]);
  };

  const toggleRead = (id) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: !n.read } : n))
    );
  };

  const filtered = filter === 'unread'
    ? notifications.filter((n) => !n.read)
    : notifications;

  const getIcon = (type) => {
    switch (type) {
      case 'danger':
        return <ShieldAlert className="w-4 h-4 text-rose-500" />;
      case 'warning':
        return <AlertTriangle className="w-4 h-4 text-amber-500" />;
      case 'success':
        return <CheckCircle2 className="w-4 h-4 text-emerald-500" />;
      default:
        return <FileText className="w-4 h-4 text-sky-500" />;
    }
  };

  return (
    <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 overflow-hidden z-50 animate-in fade-in zoom-in-95 duration-150">
      {/* Header */}
      <div className="p-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Bell className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
          <h4 className="text-sm font-bold text-slate-900 dark:text-white">
            Notifications
          </h4>
          {unreadCount > 0 && (
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300">
              {unreadCount} new
            </span>
          )}
        </div>

        <div className="flex items-center gap-2 text-xs">
          {unreadCount > 0 && (
            <button
              type="button"
              onClick={markAllAsRead}
              className="text-xs text-slate-500 hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors"
            >
              Mark read
            </button>
          )}
          {notifications.length > 0 && (
            <button
              type="button"
              onClick={clearAll}
              title="Clear all"
              className="text-slate-400 hover:text-rose-500 transition-colors"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50 px-4 py-2 text-xs gap-3 font-medium">
        <button
          type="button"
          onClick={() => setFilter('all')}
          className={`pb-0.5 transition-colors ${
            filter === 'all'
              ? 'text-emerald-600 dark:text-emerald-400 border-b-2 border-emerald-600 font-bold'
              : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          All ({notifications.length})
        </button>
        <button
          type="button"
          onClick={() => setFilter('unread')}
          className={`pb-0.5 transition-colors ${
            filter === 'unread'
              ? 'text-emerald-600 dark:text-emerald-400 border-b-2 border-emerald-600 font-bold'
              : 'text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          Unread ({unreadCount})
        </button>
      </div>

      {/* List */}
      <div className="max-h-80 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800/60">
        {filtered.length > 0 ? (
          filtered.map((item) => (
            <div
              key={item.id}
              onClick={() => toggleRead(item.id)}
              className={`p-3.5 transition-colors cursor-pointer flex items-start gap-3 ${
                !item.read
                  ? 'bg-emerald-50/30 dark:bg-emerald-950/20'
                  : 'hover:bg-slate-50/80 dark:hover:bg-slate-800/40'
              }`}
            >
              <div className="p-2 rounded-xl bg-slate-100 dark:bg-slate-800 shrink-0 mt-0.5">
                {getIcon(item.type)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between gap-1">
                  <span className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate">
                    {item.title}
                  </span>
                  <span className="text-2xs text-slate-400 whitespace-nowrap">
                    {item.time}
                  </span>
                </div>
                <p className="text-xs text-slate-600 dark:text-slate-400 mt-0.5 line-clamp-2">
                  {item.message}
                </p>
                <div className="mt-1 flex items-center justify-between">
                  <span className="text-2xs font-medium text-slate-400">
                    {item.category}
                  </span>
                  {!item.read && (
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                  )}
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="p-6 text-center text-xs text-slate-400">
            No notifications to show.
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="p-3 bg-slate-50 dark:bg-slate-950/50 border-t border-slate-100 dark:border-slate-800 text-center">
        <button
          type="button"
          onClick={onClose}
          className="text-xs font-medium text-slate-600 dark:text-slate-400 hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors"
        >
          Close Panel
        </button>
      </div>
    </div>
  );
};

export default NotificationPanel;
