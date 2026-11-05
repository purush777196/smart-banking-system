import { useState, useRef, useEffect } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import {
  Menu,
  Bell,
  Sun,
  Moon,
  Search,
  ChevronDown,
  ArrowRight,
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useTheme } from '../../hooks/useTheme';
import { NotificationPanel } from './NotificationPanel';
import { ProfileDropdown } from './ProfileDropdown';
import { ALL_NAVIGATION_ITEMS } from '../../routes/navigationConfig';
import Modal from '../common/Modal';

export const TopNavbar = ({ onOpenMobile }) => {
  const { user } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const location = useLocation();
  const navigate = useNavigate();

  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isSearchModalOpen, setIsSearchModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const notifRef = useRef(null);
  const profileRef = useRef(null);

  // Click outside listener for dropdowns
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (notifRef.current && !notifRef.current.contains(e.target)) {
        setIsNotifOpen(false);
      }
      if (profileRef.current && !profileRef.current.contains(e.target)) {
        setIsProfileOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Keyboard shortcut ⌘K / Ctrl+K for search
  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsSearchModalOpen((prev) => !prev);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Compute current page info
  const currentNav = ALL_NAVIGATION_ITEMS.find((item) => item.path === location.pathname) || {
    title: 'Dashboard',
    description: 'Financial operations overview',
  };

  const filteredSearchItems = ALL_NAVIGATION_ITEMS.filter((item) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    return (
      item.title.toLowerCase().includes(q) ||
      item.description.toLowerCase().includes(q) ||
      item.badge.toLowerCase().includes(q)
    );
  });

  const handleSelectSearchItem = (item) => {
    navigate(item.path);
    setIsSearchModalOpen(false);
    setSearchQuery('');
  };

  return (
    <>
      <header className="sticky top-0 z-30 h-16 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md border-b border-slate-200/80 dark:border-slate-800 px-4 md:px-6 flex items-center justify-between transition-colors">
        {/* Left: Mobile Toggle & Breadcrumbs */}
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={onOpenMobile}
            className="lg:hidden p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex flex-col">
            <div className="flex items-center gap-1.5 text-2xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider">
              <Link to="/" className="hover:text-emerald-600 dark:hover:text-emerald-400 transition-colors">
                Finova
              </Link>
              <span>/</span>
              <span className="text-slate-600 dark:text-slate-300 font-bold">
                {currentNav.title}
              </span>
            </div>
            <h1 className="text-sm md:text-base font-extrabold text-slate-900 dark:text-white leading-tight truncate">
              {currentNav.title}
            </h1>
          </div>
        </div>

        {/* Center: Global Search Bar */}
        <div className="hidden md:flex items-center flex-1 max-w-md mx-6">
          <button
            type="button"
            onClick={() => setIsSearchModalOpen(true)}
            className="w-full flex items-center justify-between px-3.5 py-1.5 bg-slate-100/80 dark:bg-slate-800/60 hover:bg-slate-100 dark:hover:bg-slate-800 border border-slate-200/70 dark:border-slate-700/60 rounded-xl text-xs text-slate-400 transition-all shadow-2xs group"
          >
            <div className="flex items-center gap-2">
              <Search className="w-3.5 h-3.5 text-slate-400 group-hover:text-emerald-600 transition-colors" />
              <span>Search dashboards, records, tools...</span>
            </div>
            <kbd className="hidden sm:inline-block px-1.5 py-0.5 text-2xs font-bold text-slate-400 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-md">
              ⌘K
            </kbd>
          </button>
        </div>

        {/* Right: Actions, Notifications, Theme, Profile */}
        <div className="flex items-center gap-2 sm:gap-3">
          {/* Mobile search trigger */}
          <button
            type="button"
            onClick={() => setIsSearchModalOpen(true)}
            className="md:hidden p-2 text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:white rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <Search className="w-4 h-4" />
          </button>

          {/* Theme Toggle */}
          <button
            type="button"
            onClick={toggleTheme}
            title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
            className="p-2 text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            {theme === 'dark' ? (
              <Sun className="w-4 h-4 text-amber-400" />
            ) : (
              <Moon className="w-4 h-4 text-slate-600" />
            )}
          </button>

          {/* Notifications Dropdown */}
          <div className="relative" ref={notifRef}>
            <button
              type="button"
              onClick={() => setIsNotifOpen((prev) => !prev)}
              className="relative p-2 text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <Bell className="w-4 h-4" />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-500 ring-2 ring-white dark:ring-slate-900 animate-pulse" />
            </button>

            {isNotifOpen && (
              <NotificationPanel onClose={() => setIsNotifOpen(false)} />
            )}
          </div>

          <div className="h-6 w-px bg-slate-200 dark:bg-slate-800 hidden sm:block" />

          {/* Profile Dropdown */}
          <div className="relative" ref={profileRef}>
            <button
              type="button"
              onClick={() => setIsProfileOpen((prev) => !prev)}
              className="flex items-center gap-2 p-1 pl-1.5 sm:pr-2.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            >
              <img
                src={
                  user?.avatar ||
                  'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80'
                }
                alt={user?.name || 'User'}
                className="w-8 h-8 rounded-full object-cover border border-emerald-500/50 shadow-2xs"
              />
              <div className="hidden sm:flex flex-col text-left">
                <span className="text-xs font-bold text-slate-800 dark:text-slate-200 leading-tight">
                  {user?.name?.split(' ')[0] || 'User'}
                </span>
                <span className="text-2xs font-semibold text-emerald-600 dark:text-emerald-400">
                  {user?.badge || 'Member'}
                </span>
              </div>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 hidden sm:block" />
            </button>

            {isProfileOpen && (
              <ProfileDropdown onClose={() => setIsProfileOpen(false)} />
            )}
          </div>
        </div>
      </header>

      {/* Global Search Modal */}
      <Modal
        isOpen={isSearchModalOpen}
        onClose={() => setIsSearchModalOpen(false)}
        title="Quick Navigation & Search"
        subtitle="Jump directly to any of Finova's 10 specialized dashboards"
        maxWidth="max-w-xl"
      >
        <div className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3.5 top-3 w-4 h-4 text-slate-400" />
            <input
              type="text"
              autoFocus
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search by dashboard name, function, or keywords..."
              className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm outline-hidden focus:border-emerald-500 text-slate-800 dark:text-slate-100"
            />
          </div>

          <div className="max-h-72 overflow-y-auto space-y-1.5">
            {filteredSearchItems.map((item) => {
              const Icon = item.icon;
              return (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => handleSelectSearchItem(item)}
                  className="w-full flex items-center justify-between p-3 rounded-xl hover:bg-emerald-50/50 dark:hover:bg-emerald-950/20 hover:border-emerald-200 dark:hover:border-emerald-800/50 border border-transparent transition-all text-left group"
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 group-hover:bg-emerald-100 dark:group-hover:bg-emerald-900/50 group-hover:text-emerald-600 transition-colors">
                      <Icon className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="text-xs font-bold text-slate-800 dark:text-slate-200 group-hover:text-emerald-600 dark:group-hover:text-emerald-400 transition-colors">
                        {item.title}
                      </div>
                      <div className="text-2xs text-slate-400 line-clamp-1">
                        {item.description}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span className="text-2xs font-semibold px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-500">
                      {item.badge}
                    </span>
                    <ArrowRight className="w-3.5 h-3.5 text-slate-300 group-hover:text-emerald-600 group-hover:translate-x-0.5 transition-all" />
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </Modal>
    </>
  );
};

export default TopNavbar;
