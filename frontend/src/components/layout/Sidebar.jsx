import { NavLink, useNavigate } from 'react-router-dom';
import {
  ChevronLeft,
  ChevronRight,
  LogOut,
  Sparkles,
  Lock,
  CheckCircle,
} from 'lucide-react';
import { NAVIGATION_GROUPS } from '../../routes/navigationConfig';
import { useAuth } from '../../hooks/useAuth';
import { useToast } from '../../hooks/useToast';

export const Sidebar = ({ isCollapsed, toggleCollapsed, isMobileOpen, closeMobile }) => {
  const { user, logout } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    addToast('Signed out of Finova session', 'info');
    navigate('/login');
    if (closeMobile) closeMobile();
  };

  return (
    <>
      {/* Mobile Backdrop */}
      {isMobileOpen && (
        <div
          className="fixed inset-0 bg-slate-950/70 backdrop-blur-xs z-40 lg:hidden"
          onClick={closeMobile}
        />
      )}

      {/* Sidebar Container */}
      <aside
        className={`fixed top-0 bottom-0 left-0 z-40 flex flex-col bg-[#0b132b] text-slate-300 border-r border-slate-800/80 transition-all duration-300 ease-in-out ${
          isCollapsed ? 'w-20' : 'w-72'
        } ${
          isMobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Logo Branding */}
        <div className="h-16 flex items-center justify-between px-5 border-b border-slate-800/80">
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-600 to-emerald-400 flex items-center justify-center text-white shadow-md shadow-emerald-500/20 shrink-0">
              <Sparkles className="w-5 h-5 text-white" />
            </div>
            {!isCollapsed && (
              <div className="flex flex-col min-w-0">
                <span className="font-extrabold text-lg tracking-tight text-white leading-none">
                  FINOVA
                </span>
                <span className="text-2xs font-semibold tracking-widest text-emerald-400 uppercase mt-0.5">
                  Smart Banking
                </span>
              </div>
            )}
          </div>

          {/* Desktop Collapse Toggle */}
          <button
            type="button"
            onClick={toggleCollapsed}
            title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
            className="hidden lg:flex items-center justify-center w-7 h-7 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800/80 transition-colors"
          >
            {isCollapsed ? (
              <ChevronRight className="w-4 h-4" />
            ) : (
              <ChevronLeft className="w-4 h-4" />
            )}
          </button>
        </div>

        {/* Navigation Groups */}
        <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
          {NAVIGATION_GROUPS.map((group) => (
            <div key={group.groupTitle} className="space-y-1">
              {!isCollapsed ? (
                <div className="px-3 pb-1 text-2xs font-bold uppercase tracking-wider text-slate-400">
                  {group.groupTitle}
                </div>
              ) : (
                <div className="my-2 border-t border-slate-800/60" />
              )}

              {group.items.map((item) => {
                const Icon = item.icon;
                const isPermitted = user?.role && item.role === user.role;

                return (
                  <NavLink
                    key={item.id}
                    to={item.path}
                    onClick={() => closeMobile && closeMobile()}
                    title={isCollapsed ? item.title : undefined}
                    className={({ isActive }) =>
                      `group relative flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-semibold transition-all duration-150 ${
                        isActive
                          ? 'bg-emerald-500/15 text-emerald-400 font-bold border border-emerald-500/30 shadow-xs'
                          : isPermitted
                          ? 'text-slate-300 hover:text-white hover:bg-slate-800/60'
                          : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/40 opacity-80'
                      } ${isCollapsed ? 'justify-center px-0' : ''}`
                    }
                  >
                    {({ isActive }) => (
                      <>
                        <Icon
                          className={`w-4 h-4 shrink-0 transition-colors ${
                            isActive
                              ? 'text-emerald-400'
                              : isPermitted
                              ? 'text-emerald-500/80 group-hover:text-emerald-400'
                              : 'text-slate-500 group-hover:text-slate-400'
                          }`}
                        />

                        {!isCollapsed && (
                          <div className="flex items-center justify-between w-full min-w-0">
                            <span className="truncate">{item.title}</span>
                            <div className="flex items-center gap-1.5 shrink-0">
                              {isPermitted ? (
                                <span className="text-2xs font-bold px-1.5 py-0.5 rounded-md bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 flex items-center gap-1">
                                  <CheckCircle className="w-2.5 h-2.5 text-emerald-400" />
                                  <span>Active</span>
                                </span>
                              ) : (
                                <span className="text-2xs font-normal px-1.5 py-0.5 rounded-md bg-slate-800/60 text-slate-500 flex items-center gap-1">
                                  <Lock className="w-2.5 h-2.5" />
                                  <span>RBAC</span>
                                </span>
                              )}
                            </div>
                          </div>
                        )}

                        {/* Collapsed Tooltip Indicator */}
                        {isCollapsed && isActive && (
                          <div className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-5 bg-emerald-400 rounded-r-md" />
                        )}
                      </>
                    )}
                  </NavLink>
                );
              })}
            </div>
          ))}
        </div>

        {/* User Footer Profile */}
        <div className="p-3 border-t border-slate-800/80 bg-slate-950/40">
          <div
            className={`flex items-center gap-3 p-2 rounded-xl transition-colors hover:bg-slate-800/50 ${
              isCollapsed ? 'justify-center p-1' : ''
            }`}
          >
            <img
              src={
                user?.avatar ||
                'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80'
              }
              alt={user?.name || 'User'}
              className="w-9 h-9 rounded-full object-cover border border-emerald-500/40 shrink-0"
            />

            {!isCollapsed && (
              <div className="flex-1 min-w-0">
                <div className="text-xs font-bold text-white truncate">
                  {user?.name || 'Finova User'}
                </div>
                <div className="text-2xs text-emerald-400 truncate font-medium">
                  {user?.roleTitle || user?.role || 'Operator'}
                </div>
              </div>
            )}

            {!isCollapsed && (
              <button
                type="button"
                onClick={handleLogout}
                title="Log out"
                className="p-1.5 text-slate-400 hover:text-rose-400 rounded-lg hover:bg-slate-800 transition-colors cursor-pointer"
              >
                <LogOut className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
