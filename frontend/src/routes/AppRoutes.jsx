import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './ProtectedRoute';
import DashboardLayout from '../layouts/DashboardLayout';
import LoginPage from '../pages/LoginPage';
import NotFoundPage from '../pages/NotFoundPage';
import { useAuth } from '../context/AuthContext';

// Dashboards
import AdminDashboard from '../dashboards/admin/AdminDashboard';
import ManagerDashboard from '../dashboards/manager/ManagerDashboard';
import CustomerDashboard from '../dashboards/customer/CustomerDashboard';
import EmployeeDashboard from '../dashboards/employee/EmployeeDashboard';
import LoansDashboard from '../dashboards/loans/LoansDashboard';
import FraudDashboard from '../dashboards/fraud/FraudDashboard';
import FinanceDashboard from '../dashboards/finance/FinanceDashboard';
import AuditDashboard from '../dashboards/audit/AuditDashboard';
import SupportDashboard from '../dashboards/support/SupportDashboard';
import AnalyticsDashboard from '../dashboards/analytics/AnalyticsDashboard';

export const AppRoutes = () => {
  const { user, isAuthenticated, isLoading } = useAuth();

  // Root redirect destination for authenticated user
  const defaultDashboard = user?.defaultRoute || '/admin/dashboard';

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center p-6 text-white">
        <div className="w-12 h-12 rounded-2xl bg-emerald-600/20 border border-emerald-500/40 flex items-center justify-center mb-4">
          <span className="w-6 h-6 border-2 border-emerald-400 border-t-transparent rounded-full animate-spin" />
        </div>
        <h3 className="text-base font-bold text-white tracking-wide">
          Finova Banking System
        </h3>
        <p className="text-xs text-slate-400 mt-1">
          Initializing secure workstation environment...
        </p>
      </div>
    );
  }

  return (
    <Routes>
      {/* Public Route */}
      <Route
        path="/login"
        element={
          isAuthenticated ? (
            <Navigate to={defaultDashboard} replace />
          ) : (
            <LoginPage />
          )
        }
      />

      {/* Protected Dashboards wrapped in Layout */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        {/* Index Redirect to Role's Default Dashboard */}
        <Route index element={<Navigate to={defaultDashboard} replace />} />

        {/* 10 Specialized Dashboards */}
        <Route path="admin/dashboard" element={<AdminDashboard />} />
        <Route path="manager/dashboard" element={<ManagerDashboard />} />
        <Route path="customer/dashboard" element={<CustomerDashboard />} />
        <Route path="employee/dashboard" element={<EmployeeDashboard />} />
        <Route path="loans/dashboard" element={<LoansDashboard />} />
        <Route path="fraud/dashboard" element={<FraudDashboard />} />
        <Route path="finance/dashboard" element={<FinanceDashboard />} />
        <Route path="audit/dashboard" element={<AuditDashboard />} />
        <Route path="support/dashboard" element={<SupportDashboard />} />
        <Route path="analytics/dashboard" element={<AnalyticsDashboard />} />
      </Route>

      {/* Fallback 404 Route */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;
