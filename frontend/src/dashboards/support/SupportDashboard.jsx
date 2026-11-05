import { useState } from 'react';
import {
  HeadphonesIcon,
  Clock,
  CheckCircle2,
  AlertCircle,
  PlusCircle,
} from 'lucide-react';
import {
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import StatCard from '../../components/common/StatCard';
import ChartCard from '../../components/common/ChartCard';
import DataTable from '../../components/tables/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';
import {
  SUPPORT_STATS,
  SUPPORT_RESOLUTION_TRENDS,
  SUPPORT_CATEGORY_DISTRIBUTION,
  SUPPORT_TICKETS,
  SUPPORT_COMPLAINTS,
} from '../../data/mockSupport';

export const SupportDashboard = () => {
  const { addToast } = useToast();
  const [tickets, setTickets] = useState(SUPPORT_TICKETS);
  const [isNewTicketModalOpen, setIsNewTicketModalOpen] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState(null);

  // New ticket form
  const [ticketCustomer, setTicketCustomer] = useState('');
  const [ticketSubject, setTicketSubject] = useState('');
  const [ticketCategory, setTicketCategory] = useState('Card Issues');
  const [ticketPriority, setTicketPriority] = useState('Medium');

  const handleCreateTicket = (e) => {
    e.preventDefault();
    if (!ticketCustomer.trim() || !ticketSubject.trim()) return;

    const newT = {
      id: `TCK-2026-${Math.floor(900 + Math.random() * 100)}`,
      customer: ticketCustomer,
      account: 'FIN-8429-01',
      subject: ticketSubject,
      category: ticketCategory,
      priority: ticketPriority,
      slaRemaining: '4 hours',
      assignee: 'Hannah Abbott',
      status: 'In Progress',
      createdAt: 'Just now',
    };

    setTickets([newT, ...tickets]);
    addToast(`Support ticket ${newT.id} created for ${ticketCustomer}.`, 'success');
    setTicketCustomer('');
    setTicketSubject('');
    setIsNewTicketModalOpen(false);
  };

  const handleUpdateStatus = (id, newStatus) => {
    setTickets((prev) =>
      prev.map((t) => (t.id === id ? { ...t, status: newStatus } : t))
    );
    addToast(`Ticket ${id} status updated to "${newStatus}".`, 'info');
    if (selectedTicket) setSelectedTicket(null);
  };

  const ticketColumns = [
    { key: 'id', header: 'Ticket #' },
    {
      key: 'customer',
      header: 'Customer',
      render: (c, row) => (
        <div>
          <span className="font-bold text-slate-900 dark:text-white block">{c}</span>
          <span className="text-2xs text-slate-400">{row.account}</span>
        </div>
      ),
    },
    {
      key: 'subject',
      header: 'Inquiry Subject',
      render: (s) => <span className="font-medium text-xs line-clamp-1">{s}</span>,
    },
    { key: 'category', header: 'Category' },
    {
      key: 'priority',
      header: 'Priority',
      align: 'center',
      render: (p) => <StatusBadge status={p} />,
    },
    { key: 'slaRemaining', header: 'SLA Timer' },
    { key: 'assignee', header: 'Agent' },
    {
      key: 'status',
      header: 'Status',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    {
      key: 'actions',
      header: 'Action',
      align: 'center',
      sortable: false,
      render: (_, row) => (
        <button
          type="button"
          onClick={() => setSelectedTicket(row)}
          className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 transition-colors"
        >
          Manage
        </button>
      ),
    },
  ];

  const complaintColumns = [
    { key: 'id', header: 'Case Ref' },
    {
      key: 'customer',
      header: 'Client',
      render: (c) => <span className="font-bold">{c}</span>,
    },
    { key: 'department', header: 'Department' },
    { key: 'issue', header: 'Escalation Details' },
    {
      key: 'escalationLevel',
      header: 'Severity Tier',
      align: 'center',
      render: (e) => <StatusBadge status={e} />,
    },
    {
      key: 'status',
      header: 'Resolution',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Top Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              24/7 Priority Customer Support Concierge
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              SLA 98.7% On Target
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Lead Agent: Hannah Abbott • Omni-channel ticketing, disputes & high-net-worth client support
          </p>
        </div>

        <button
          type="button"
          onClick={() => setIsNewTicketModalOpen(true)}
          className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5 self-start sm:self-auto"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Create Ticket</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Inquiries"
          value={SUPPORT_STATS.totalTickets.value}
          change={SUPPORT_STATS.totalTickets.change}
          isPositive={SUPPORT_STATS.totalTickets.isPositive}
          period={SUPPORT_STATS.totalTickets.period}
          icon={HeadphonesIcon}
        />
        <StatCard
          title="Open Ticket Queue"
          value={SUPPORT_STATS.openTickets.value}
          change={SUPPORT_STATS.openTickets.change}
          isPositive={SUPPORT_STATS.openTickets.isPositive}
          period={SUPPORT_STATS.openTickets.period}
          icon={Clock}
        />
        <StatCard
          title="Resolved Tickets"
          value={SUPPORT_STATS.resolvedTickets.value}
          change={SUPPORT_STATS.resolvedTickets.change}
          isPositive={SUPPORT_STATS.resolvedTickets.isPositive}
          period={SUPPORT_STATS.resolvedTickets.period}
          icon={CheckCircle2}
        />
        <StatCard
          title="Urgent Priority (SLA < 1h)"
          value={SUPPORT_STATS.highPriorityTickets.value}
          change={SUPPORT_STATS.highPriorityTickets.change}
          isPositive={SUPPORT_STATS.highPriorityTickets.isPositive}
          period={SUPPORT_STATS.highPriorityTickets.period}
          icon={AlertCircle}
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
        {/* Ticket Resolution Trends */}
        <div className="lg:col-span-7">
          <ChartCard
            title="Weekly Ticket Inflow vs Resolution"
            subtitle="Opened incoming inquiries vs successful resolutions"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={SUPPORT_RESOLUTION_TRENDS}>
                <CartesianGrid strokeDasharray="3 3" opacity={0.15} />
                <XAxis dataKey="day" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Line type="monotone" dataKey="opened" name="Inflow (Opened)" stroke="#94a3b8" strokeWidth={2} />
                <Line type="monotone" dataKey="resolved" name="Resolved SLA" stroke="#10b981" strokeWidth={2.5} />
              </LineChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>

        {/* Category Breakdown Donut */}
        <div className="lg:col-span-5">
          <ChartCard
            title="Ticket Category Distribution"
            subtitle="Dominant inquiry drivers across retail & corporate accounts"
            height="h-64"
          >
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={SUPPORT_CATEGORY_DISTRIBUTION}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {SUPPORT_CATEGORY_DISTRIBUTION.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val) => [`${val} Tickets`, '']}
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '0.75rem',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </ChartCard>
        </div>
      </div>

      {/* Tables Section */}
      <div className="grid grid-cols-1 gap-6">
        <DataTable
          title="Active Customer Support Queue"
          subtitle="Real-time ticket triage with SLA remaining countdown"
          columns={ticketColumns}
          data={tickets}
        />

        <DataTable
          title="Escalated Complaints & Ombudsman Inquiries"
          subtitle="Tier 2 and legal compliance inquiries"
          columns={complaintColumns}
          data={SUPPORT_COMPLAINTS}
        />
      </div>

      {/* Create Ticket Modal */}
      <Modal
        isOpen={isNewTicketModalOpen}
        onClose={() => setIsNewTicketModalOpen(false)}
        title="Create Customer Support Ticket"
        subtitle="Log a customer phone, email, or counter inquiry"
        maxWidth="max-w-md"
        footer={
          <button
            type="button"
            onClick={handleCreateTicket}
            className="w-full py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
          >
            Log & Assign Ticket
          </button>
        }
      >
        <form onSubmit={handleCreateTicket} className="space-y-3">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Customer Name
            </label>
            <input
              type="text"
              value={ticketCustomer}
              onChange={(e) => setTicketCustomer(e.target.value)}
              placeholder="e.g. Jonathan Drake"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              Inquiry Subject
            </label>
            <input
              type="text"
              value={ticketSubject}
              onChange={(e) => setTicketSubject(e.target.value)}
              placeholder="e.g. Wire transfer delay on Swift reference #9941"
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                Category
              </label>
              <select
                value={ticketCategory}
                onChange={(e) => setTicketCategory(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
              >
                <option value="Card Issues">Card Issues</option>
                <option value="Wire Transfers">Wire Transfers</option>
                <option value="Security / MFA">Security / MFA</option>
                <option value="Account Servicing">Account Servicing</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                Priority
              </label>
              <select
                value={ticketPriority}
                onChange={(e) => setTicketPriority(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
              >
                <option value="Low">Low</option>
                <option value="Medium">Medium</option>
                <option value="High">High</option>
              </select>
            </div>
          </div>
        </form>
      </Modal>

      {/* Ticket Management Modal */}
      {selectedTicket && (
        <Modal
          isOpen={!!selectedTicket}
          onClose={() => setSelectedTicket(null)}
          title={`Ticket Dossier: ${selectedTicket.id}`}
          subtitle={`Client: ${selectedTicket.customer} • ${selectedTicket.category}`}
          maxWidth="max-w-md"
          footer={
            <div className="flex items-center justify-between w-full">
              <button
                type="button"
                onClick={() => handleUpdateStatus(selectedTicket.id, 'In Progress')}
                className="px-3.5 py-2 text-xs font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-xl"
              >
                Mark In Progress
              </button>
              <button
                type="button"
                onClick={() => handleUpdateStatus(selectedTicket.id, 'Resolved')}
                className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl"
              >
                Mark as Resolved
              </button>
            </div>
          }
        >
          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800">
              <span className="text-slate-400 block text-2xs uppercase font-bold">
                Customer Issue
              </span>
              <p className="font-semibold text-slate-900 dark:text-white mt-1">
                {selectedTicket.subject}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-2 py-1">
              <div>
                <span className="text-slate-400 block">SLA Remaining</span>
                <span className="font-bold text-rose-600">{selectedTicket.slaRemaining}</span>
              </div>
              <div>
                <span className="text-slate-400 block">Current Status</span>
                <StatusBadge status={selectedTicket.status} />
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default SupportDashboard;
