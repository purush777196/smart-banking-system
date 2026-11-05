import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ShieldAlert, ArrowRight, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { isRouteAllowedForRole, getRouteForRole, ROUTE_TO_ROLE_MAP } from './roleMapping';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, isAuthenticated, isLoading, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  // Show a sleek loading state during initial session validation
  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center p-6 text-white">
        <div className="w-12 h-12 rounded-2xl bg-emerald-600/20 border border-emerald-500/40 flex items-center justify-center mb-4">
          <span className="w-6 h-6 border-2 border-emerald-400 border-t-transparent rounded-full animate-spin" />
        </div>
        <h3 className="text-base font-bold text-white tracking-wide">
          Verifying Finova Security Context...
        </h3>
        <p className="text-xs text-slate-400 mt-1">
          Validating JWT token and role credentials against Spring Security
        </p>
      </div>
    );
  }

  // Not authenticated: redirect to login
  if (!isAuthenticated || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // Explicit allowedRoles prop check if provided
  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    return (
      <AccessDeniedScreen
        user={user}
        attemptedPath={location.pathname}
        onNavigate={() => navigate(getRouteForRole(user.role))}
        onLogout={logout}
      />
    );
  }

  // Check if current route is a dashboard route and whether user's role is allowed
  const isDashboardRoute = Object.keys(ROUTE_TO_ROLE_MAP).includes(location.pathname);
  if (isDashboardRoute && !isRouteAllowedForRole(location.pathname, user.role)) {
    return (
      <AccessDeniedScreen
        user={user}
        attemptedPath={location.pathname}
        onNavigate={() => navigate(getRouteForRole(user.role))}
        onLogout={logout}
      />
    );
  }

  return children;
};

const AccessDeniedScreen = ({ user, attemptedPath, onNavigate, onLogout }) => {
  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-6">
      <div className="max-w-md w-full bg-slate-900 border border-rose-500/30 rounded-3xl p-8 text-center shadow-2xl shadow-rose-950/40">
        <div className="w-16 h-16 mx-auto rounded-2xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400 mb-6">
          <ShieldAlert className="w-8 h-8" />
        </div>

        <span className="inline-block px-3 py-1 rounded-full text-2xs font-bold uppercase tracking-wider bg-rose-500/20 text-rose-300 border border-rose-500/30 mb-3">
          403 Forbidden • RBAC Violation
        </span>

        <h2 className="text-xl font-black text-white">
          Access Restricted
        </h2>

        <p className="text-xs text-slate-400 mt-3 leading-relaxed">
          Your active account role is <strong className="text-emerald-400">{user?.roleTitle || user?.role}</strong> (<code className="text-slate-300 font-mono">{user?.role}</code>).
          You do not have authorization clearance to access:
        </p>

        <div className="my-4 py-2 px-3 rounded-xl bg-slate-800/80 border border-slate-700/60 font-mono text-xs text-rose-300 truncate">
          {attemptedPath}
        </div>

        <p className="text-2xs text-slate-500 leading-normal mb-6">
          Finova role-based access control (RBAC) enforces strict departmental isolation. Please return to your designated dashboard or switch credentials.
        </p>

        <div className="space-y-3">
          <button
            type="button"
            onClick={onNavigate}
            className="w-full py-3 px-4 rounded-xl font-bold text-xs text-white bg-emerald-600 hover:bg-emerald-700 transition-all flex items-center justify-center gap-2 cursor-pointer shadow-md shadow-emerald-600/20"
          >
            <span>Return to My Dashboard</span>
            <ArrowRight className="w-4 h-4" />
          </button>

          <button
            type="button"
            onClick={onLogout}
            className="w-full py-2.5 px-4 rounded-xl font-semibold text-xs text-slate-400 hover:text-white bg-slate-800/60 hover:bg-slate-800 border border-slate-700/60 transition-colors flex items-center justify-center gap-2 cursor-pointer"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Switch Role / Sign Out</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProtectedRoute;
