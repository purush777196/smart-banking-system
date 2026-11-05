import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut, Sun, Moon, Users, ShieldCheck, ChevronRight, Loader2 } from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useTheme } from '../../hooks/useTheme';
import { useToast } from '../../hooks/useToast';

export const ProfileDropdown = ({ onClose }) => {
  const { user, switchRole, logout, demoUsers } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const { addToast } = useToast();
  const navigate = useNavigate();
  const [showRoleSelector, setShowRoleSelector] = useState(false);
  const [isSwitching, setIsSwitching] = useState(false);

  const handleRoleSelect = async (selectedUser) => {
    setIsSwitching(true);
    try {
      const loggedUser = await switchRole(selectedUser.id);
      addToast(`Authenticated as ${loggedUser.roleTitle}`, 'success');
      navigate(loggedUser.defaultRoute);
      onClose();
    } catch (err) {
      addToast(err?.message || 'Failed to switch role', 'error');
    } finally {
      setIsSwitching(false);
    }
  };

  const handleLogout = () => {
    logout();
    addToast('Signed out from Finova session', 'info');
    navigate('/login');
    onClose();
  };

  return (
    <div className="absolute right-0 mt-2 w-80 bg-white dark:bg-slate-900 rounded-2xl shadow-xl border border-slate-200 dark:border-slate-800 overflow-hidden z-50 animate-in fade-in zoom-in-95 duration-150">
      {/* User Header */}
      <div className="p-4 bg-slate-50 dark:bg-slate-950/60 border-b border-slate-100 dark:border-slate-800 flex items-center gap-3">
        <img
          src={user?.avatar || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80'}
          alt={user?.name || 'User'}
          className="w-11 h-11 rounded-full object-cover border-2 border-emerald-500/40 shadow-xs"
        />
        <div className="min-w-0 flex-1">
          <h4 className="text-sm font-bold text-slate-900 dark:text-white truncate">
            {user?.name || 'Finova User'}
          </h4>
          <p className="text-xs text-slate-500 dark:text-slate-400 truncate">
            {user?.email || 'user@finova.bank'}
          </p>
          <span className="inline-block mt-1 px-2 py-0.5 text-2xs font-semibold rounded-full bg-emerald-100 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300">
            {user?.roleTitle || user?.role || 'Operator'}
          </span>
        </div>
      </div>

      {/* Role Switcher Menu */}
      <div className="p-2 border-b border-slate-100 dark:border-slate-800">
        <button
          type="button"
          onClick={() => setShowRoleSelector(!showRoleSelector)}
          disabled={isSwitching}
          className="w-full flex items-center justify-between p-2.5 rounded-xl text-xs font-semibold text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors disabled:opacity-50"
        >
          <div className="flex items-center gap-2.5">
            {isSwitching ? (
              <Loader2 className="w-4 h-4 text-emerald-600 animate-spin" />
            ) : (
              <Users className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            )}
            <span>Switch Demo Role ({demoUsers.length} Profiles)</span>
          </div>
          <ChevronRight
            className={`w-4 h-4 text-slate-400 transition-transform ${
              showRoleSelector ? 'rotate-90' : ''
            }`}
          />
        </button>

        {showRoleSelector && (
          <div className="mt-1 space-y-1 max-h-52 overflow-y-auto pl-2 pr-1">
            {demoUsers.map((u) => {
              const isSelected = u.role === user?.role;
              return (
                <button
                  key={u.id}
                  type="button"
                  disabled={isSwitching}
                  onClick={() => handleRoleSelect(u)}
                  className={`w-full flex items-center justify-between p-2 rounded-lg text-left text-xs transition-colors cursor-pointer ${
                    isSelected
                      ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 font-bold'
                      : 'text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800/60'
                  }`}
                >
                  <div className="truncate mr-2">
                    <div className="truncate font-medium">{u.name}</div>
                    <div className="text-2xs text-slate-400">{u.roleTitle}</div>
                  </div>
                  {isSelected && (
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                  )}
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* Secondary Actions */}
      <div className="p-2 space-y-1">
        <button
          type="button"
          onClick={toggleTheme}
          className="w-full flex items-center justify-between p-2.5 rounded-xl text-xs font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
        >
          <div className="flex items-center gap-2.5">
            {theme === 'dark' ? (
              <Sun className="w-4 h-4 text-amber-500" />
            ) : (
              <Moon className="w-4 h-4 text-slate-600" />
            )}
            <span>Theme: {theme === 'dark' ? 'Dark Mode' : 'Light Mode'}</span>
          </div>
          <span className="text-2xs uppercase tracking-wider font-semibold px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-500">
            {theme}
          </span>
        </button>

        <button
          type="button"
          onClick={handleLogout}
          className="w-full flex items-center gap-2.5 p-2.5 rounded-xl text-xs font-medium text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors cursor-pointer"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </div>
  );
};

export default ProfileDropdown;
