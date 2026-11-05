import { Link } from 'react-router-dom';
import { ArrowLeft, Home, Sparkles } from 'lucide-react';

export const NotFoundPage = () => {
  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-6 text-center">
      <div className="w-16 h-16 rounded-3xl bg-gradient-to-tr from-emerald-600 to-emerald-400 flex items-center justify-center text-white shadow-xl shadow-emerald-500/20 mb-6">
        <Sparkles className="w-8 h-8" />
      </div>

      <h1 className="text-6xl md:text-8xl font-black tracking-tight text-white mb-2">
        404
      </h1>
      <h2 className="text-xl md:text-2xl font-bold text-slate-200 mb-3">
        Banking Page Not Found
      </h2>
      <p className="text-sm text-slate-400 max-w-md mb-8">
        The route or resource you requested could not be located in Finova Core. Please check the URL or return to your active dashboard.
      </p>

      <div className="flex flex-wrap items-center justify-center gap-3">
        <Link
          to="/"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-bold bg-emerald-600 hover:bg-emerald-700 text-white shadow-lg shadow-emerald-600/20 transition-all"
        >
          <Home className="w-4 h-4" />
          <span>Go to Home Dashboard</span>
        </Link>
        <Link
          to="/login"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-all"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Switch Account / Sign In</span>
        </Link>
      </div>
    </div>
  );
};

export default NotFoundPage;

