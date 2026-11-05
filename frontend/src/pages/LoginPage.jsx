import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Sparkles,
  Eye,
  EyeOff,
  Lock,
  Mail,
  ShieldCheck,
  ArrowRight,
  Landmark,
  CheckCircle2,
  Users,
  AlertCircle,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Modal from '../components/common/Modal';

export const LoginPage = () => {
  const { login, demoUsers } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const isExpired = searchParams.get('expired') === 'true';

  const [email, setEmail] = useState('admin@finova.bank');
  const [password, setPassword] = useState('demo1234');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [selectedRoleId, setSelectedRoleId] = useState(demoUsers[0].id);
  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [isForgotModalOpen, setIsForgotModalOpen] = useState(false);

  const handleRolePillClick = (user) => {
    setSelectedRoleId(user.id);
    setEmail(user.email);
    setPassword('demo1234');
    setErrors({});
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!email.trim()) {
      newErrors.email = 'Email address is required';
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    if (!password) {
      newErrors.password = 'Password is required';
    } else if (password.length < 4) {
      newErrors.password = 'Password must be at least 4 characters';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setIsLoading(true);
    setErrors({});

    try {
      const loggedUser = await login(email, password);
      setIsLoading(false);
      addToast(`Welcome back, ${loggedUser.name}! Authenticated as ${loggedUser.roleTitle}.`, 'success');
      navigate(loggedUser.defaultRoute || '/admin/dashboard');
    } catch (err) {
      setIsLoading(false);
      const errMsg = err?.message || 'Authentication failed. Please verify credentials.';
      setErrors({ auth: errMsg });
      addToast(errMsg, 'error');
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 flex items-center justify-center p-4 sm:p-6 lg:p-8">
      {/* Container Card */}
      <div className="max-w-5xl w-full grid grid-cols-1 lg:grid-cols-12 bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden">
        
        {/* Left Side: Brand Showcase (Hidden on Mobile) */}
        <div className="hidden lg:flex lg:col-span-5 bg-[#0b132b] text-white p-10 flex-col justify-between relative overflow-hidden">
          {/* Subtle background glow */}
          <div className="absolute -top-24 -left-24 w-72 h-72 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none" />
          <div className="absolute -bottom-24 -right-24 w-72 h-72 bg-emerald-600/15 rounded-full blur-3xl pointer-events-none" />

          {/* Top Brand */}
          <div className="relative z-10">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-emerald-600 to-emerald-400 flex items-center justify-center text-white shadow-lg shadow-emerald-500/20">
                <Sparkles className="w-6 h-6" />
              </div>
              <div>
                <h1 className="font-extrabold text-xl tracking-tight text-white leading-none">
                  FINOVA
                </h1>
                <span className="text-2xs font-semibold tracking-widest text-emerald-400 uppercase">
                  Enterprise Banking System
                </span>
              </div>
            </div>

            <p className="mt-8 text-sm text-slate-300 leading-relaxed">
              Unified digital core for next-generation retail, commercial, credit, and risk intelligence operations.
            </p>
          </div>

          {/* Core Feature Highlights */}
          <div className="relative z-10 space-y-4 my-8">
            <div className="flex items-center gap-3 p-3 rounded-2xl bg-slate-800/40 border border-slate-700/50">
              <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
              <div className="text-xs">
                <div className="font-bold text-white">10 Specialized Dashboards</div>
                <div className="text-slate-400">Complete role-based fintech views</div>
              </div>
            </div>

            <div className="flex items-center gap-3 p-3 rounded-2xl bg-slate-800/40 border border-slate-700/50">
              <ShieldCheck className="w-5 h-5 text-emerald-400 shrink-0" />
              <div className="text-xs">
                <div className="font-bold text-white">Spring Security + JWT</div>
                <div className="text-slate-400">Stateless bearer token authorization</div>
              </div>
            </div>

            <div className="flex items-center gap-3 p-3 rounded-2xl bg-slate-800/40 border border-slate-700/50">
              <Landmark className="w-5 h-5 text-emerald-400 shrink-0" />
              <div className="text-xs">
                <div className="font-bold text-white">PostgreSQL Persistence</div>
                <div className="text-slate-400">BCrypt hashed credential storage</div>
              </div>
            </div>
          </div>

          {/* Footer note */}
          <div className="relative z-10 text-2xs text-slate-400 border-t border-slate-800 pt-4">
            Phase 3 Authentication • Spring Boot 4 + Spring Security + JWT & RBAC.
          </div>
        </div>

        {/* Right Side: Login Form */}
        <div className="lg:col-span-7 p-6 sm:p-10 flex flex-col justify-center">
          {/* Header */}
          <div className="mb-6">
            <div className="flex items-center gap-2 lg:hidden mb-4">
              <div className="w-8 h-8 rounded-xl bg-emerald-600 flex items-center justify-center text-white">
                <Sparkles className="w-4 h-4" />
              </div>
              <span className="font-extrabold text-base text-slate-900 dark:text-white">
                FINOVA
              </span>
            </div>

            <h2 className="text-2xl font-black text-slate-900 dark:text-white">
              Sign in to your portal
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
              Select any demo persona below for 1-click credentials and instant backend JWT authentication.
            </p>
          </div>

          {/* Session Expired Notice */}
          {isExpired && (
            <div className="mb-4 p-3 rounded-xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 text-amber-800 dark:text-amber-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0 text-amber-600" />
              <span>Your security session has expired. Please sign in again.</span>
            </div>
          )}

          {/* General Auth Error */}
          {errors.auth && (
            <div className="mb-4 p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0 text-rose-500" />
              <span>{errors.auth}</span>
            </div>
          )}

          {/* 1-Click Role Selector */}
          <div className="mb-6">
            <div className="flex items-center justify-between mb-2">
              <label className="text-xs font-bold uppercase tracking-wider text-slate-600 dark:text-slate-400 flex items-center gap-1.5">
                <Users className="w-3.5 h-3.5 text-emerald-600" />
                Select Demo Role ({demoUsers.length} Available)
              </label>
              <span className="text-2xs text-emerald-600 dark:text-emerald-400 font-semibold">
                Click any role to load
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-1.5 max-h-44 overflow-y-auto p-1 bg-slate-50 dark:bg-slate-800/40 rounded-2xl border border-slate-200 dark:border-slate-800">
              {demoUsers.map((u) => {
                const isSelected = selectedRoleId === u.id;
                return (
                  <button
                    key={u.id}
                    type="button"
                    onClick={() => handleRolePillClick(u)}
                    className={`flex flex-col items-center justify-center p-2 rounded-xl text-center transition-all ${
                      isSelected
                        ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20 font-bold scale-[1.02]'
                        : 'bg-white dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700/80 text-slate-700 dark:text-slate-300 border border-slate-200/60 dark:border-slate-700/60'
                    }`}
                  >
                    <span className="text-xs truncate w-full font-semibold">
                      {u.roleTitle.split(' ')[0]}
                    </span>
                    <span
                      className={`text-2xs truncate w-full ${
                        isSelected ? 'text-emerald-100' : 'text-slate-400'
                      }`}
                    >
                      {u.badge}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Form */}
          <form onSubmit={handleLogin} className="space-y-4">
            {/* Email Field */}
            <div>
              <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1.5">
                Work Email Address
              </label>
              <div className="relative">
                <Mail className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@finova.bank"
                  className={`w-full pl-10 pr-4 py-2.5 rounded-xl border text-sm outline-hidden transition-all bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 ${
                    errors.email
                      ? 'border-rose-400 focus:border-rose-500 ring-1 ring-rose-400'
                      : 'border-slate-200 dark:border-slate-700 focus:border-emerald-500 ring-emerald-500/20'
                  }`}
                />
              </div>
              {errors.email && (
                <p className="mt-1 text-xs text-rose-500">{errors.email}</p>
              )}
            </div>

            {/* Password Field */}
            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300">
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => setIsForgotModalOpen(true)}
                  className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 hover:underline"
                >
                  Forgot password?
                </button>
              </div>
              <div className="relative">
                <Lock className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className={`w-full pl-10 pr-10 py-2.5 rounded-xl border text-sm outline-hidden transition-all bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 ${
                    errors.password
                      ? 'border-rose-400 focus:border-rose-500 ring-1 ring-rose-400'
                      : 'border-slate-200 dark:border-slate-700 focus:border-emerald-500 ring-emerald-500/20'
                  }`}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-3 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
              {errors.password && (
                <p className="mt-1 text-xs text-rose-500">{errors.password}</p>
              )}
            </div>

            {/* Remember Me */}
            <div className="flex items-center justify-between pt-1">
              <label className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500 border-slate-300 dark:border-slate-700"
                />
                <span className="text-xs text-slate-600 dark:text-slate-400 font-medium">
                  Remember this workstation for 30 days
                </span>
              </label>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-2 py-3 px-4 rounded-xl font-bold text-sm text-white bg-emerald-600 hover:bg-emerald-700 focus:ring-4 focus:ring-emerald-500/20 transition-all shadow-md shadow-emerald-600/20 flex items-center justify-center gap-2 cursor-pointer disabled:opacity-70 disabled:cursor-not-allowed"
            >
              {isLoading ? (
                <>
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  <span>Verifying with Spring Security...</span>
                </>
              ) : (
                <>
                  <span>Sign In with Spring Security</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Demo disclaimer */}
          <div className="mt-6 p-3 rounded-xl bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-800 text-center">
            <p className="text-2xs text-slate-500 dark:text-slate-400 leading-normal">
              <strong className="text-emerald-600 dark:text-emerald-400">Spring Security + JWT:</strong> Real authentication layer with PostgreSQL persistence. Pre-seeded with 10 role personas (passcode: <code className="bg-slate-200 dark:bg-slate-700 px-1 py-0.5 rounded text-emerald-600 font-mono">demo1234</code>).
            </p>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      <Modal
        isOpen={isForgotModalOpen}
        onClose={() => setIsForgotModalOpen(false)}
        title="Reset Password"
        subtitle="Recover your Finova portal credentials"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={() => {
              addToast('Password reset instructions sent to your registered work email', 'info');
              setIsForgotModalOpen(false);
            }}
            className="px-4 py-2 text-sm font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl transition-colors"
          >
            Send Reset Link
          </button>
        }
      >
        <p className="text-sm text-slate-600 dark:text-slate-300">
          In this prototype phase, all accounts use the demo passcode <code className="bg-slate-100 dark:bg-slate-800 px-1.5 py-0.5 rounded text-emerald-600">demo1234</code>.
          Click below to simulate sending an automated reset email to your corporate address.
        </p>
      </Modal>
    </div>
  );
};

export default LoginPage;
