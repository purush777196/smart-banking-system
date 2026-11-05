import { useState } from 'react';
import {
  FileCheck2,
  Clock,
  ShieldCheck,
  AlertTriangle,
  Download,
  Eye,
  CheckCircle2,
  Flag,
} from 'lucide-react';
import StatCard from '../../components/common/StatCard';
import DataTable from '../../components/tables/DataTable';
import StatusBadge from '../../components/common/StatusBadge';
import Modal from '../../components/common/Modal';
import { useToast } from '../../context/ToastContext';
import {
  AUDIT_STATS,
  AUDIT_LOGS,
  AUDIT_KYC_RECORDS,
  AUDIT_ACCOUNT_ACTIVITIES,
} from '../../data/mockAudit';

export const AuditDashboard = () => {
  const { addToast } = useToast();
  const [kycRecords, setKycRecords] = useState(AUDIT_KYC_RECORDS);
  const [selectedAuditLog, setSelectedAuditLog] = useState(null);
  const [selectedKyc, setSelectedKyc] = useState(null);

  const handleVerifyKyc = (id, customer) => {
    setKycRecords((prev) =>
      prev.map((k) => (k.id === id ? { ...k, status: 'Verified' } : k))
    );
    addToast(`KYC verification certified for ${customer}. Sanctions clearance granted.`, 'success');
    if (selectedKyc) setSelectedKyc(null);
  };

  const handleFlagKyc = (id, customer) => {
    setKycRecords((prev) =>
      prev.map((k) => (k.id === id ? { ...k, status: 'Under Enhanced DD' } : k))
    );
    addToast(`Enhanced Due Diligence (EDD) triggered for ${customer}.`, 'warning');
    if (selectedKyc) setSelectedKyc(null);
  };

  const handleExportAuditReport = () => {
    addToast('Generating Regulatory Compliance & Audit Trail Packet...', 'info');
    setTimeout(() => {
      addToast('Audit report compiled and downloaded: Finova_Audit_Q3.pdf', 'success');
    }, 1200);
  };

  const auditLogColumns = [
    { key: 'id', header: 'Event Ref' },
    { key: 'timestamp', header: 'Timestamp (UTC)' },
    {
      key: 'actor',
      header: 'Operator / Principal',
      render: (a) => <span className="font-bold text-slate-900 dark:text-white">{a}</span>,
    },
    { key: 'action', header: 'Executed Operation' },
    { key: 'ipAddress', header: 'IP / Terminal' },
    {
      key: 'severity',
      header: 'Severity',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    { key: 'status', header: 'Resolution' },
    {
      key: 'actions',
      header: 'Audit',
      align: 'center',
      sortable: false,
      render: (_, row) => (
        <button
          type="button"
          onClick={() => setSelectedAuditLog(row)}
          className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-600 transition-colors"
        >
          <Eye className="w-4 h-4" />
        </button>
      ),
    },
  ];

  const kycColumns = [
    { key: 'id', header: 'KYC Packet #' },
    {
      key: 'customer',
      header: 'Customer Entity',
      render: (c) => <span className="font-bold">{c}</span>,
    },
    { key: 'country', header: 'Jurisdiction' },
    { key: 'docType', header: 'Submitted Documentation' },
    { key: 'riskTier', header: 'Risk Tier' },
    {
      key: 'status',
      header: 'KYC State',
      align: 'center',
      render: (s) => <StatusBadge status={s} />,
    },
    {
      key: 'actions',
      header: 'Review',
      align: 'center',
      sortable: false,
      render: (_, row) => (
        <button
          type="button"
          onClick={() => setSelectedKyc(row)}
          className="px-2.5 py-1 text-xs font-semibold rounded-lg bg-emerald-50 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 hover:bg-emerald-100 transition-colors"
        >
          Review
        </button>
      ),
    },
  ];

  const activityColumns = [
    { key: 'id', header: 'Log Ref' },
    { key: 'account', header: 'Account #' },
    { key: 'customer', header: 'Customer' },
    { key: 'event', header: 'Security & Access Event' },
    { key: 'location', header: 'Access Point' },
    {
      key: 'status',
      header: 'Verification',
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
              Regulatory Audit & Compliance Oversight
            </h2>
            <span className="px-2 py-0.5 text-2xs font-bold rounded-full bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
              Audit Shield Online
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Compliance Officer: David Ross • Immutable Ledger Logbook & Real-time KYC Verification
          </p>
        </div>

        <button
          type="button"
          onClick={handleExportAuditReport}
          className="px-4 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors flex items-center gap-1.5 self-start sm:self-auto"
        >
          <Download className="w-4 h-4" />
          <span>Export Audit Packet</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Audit Events"
          value={AUDIT_STATS.totalAuditEvents.value}
          change={AUDIT_STATS.totalAuditEvents.change}
          isPositive={AUDIT_STATS.totalAuditEvents.isPositive}
          period={AUDIT_STATS.totalAuditEvents.period}
          icon={FileCheck2}
        />
        <StatCard
          title="Pending KYC Files"
          value={AUDIT_STATS.pendingKYC.value}
          change={AUDIT_STATS.pendingKYC.change}
          isPositive={AUDIT_STATS.pendingKYC.isPositive}
          period={AUDIT_STATS.pendingKYC.period}
          icon={Clock}
        />
        <StatCard
          title="KYC Compliance Rate"
          value={`${AUDIT_STATS.verifiedCustomers.value}%`}
          change={AUDIT_STATS.verifiedCustomers.change}
          isPositive={AUDIT_STATS.verifiedCustomers.isPositive}
          period={AUDIT_STATS.verifiedCustomers.period}
          icon={ShieldCheck}
        />
        <StatCard
          title="Compliance Inquiries"
          value={AUDIT_STATS.complianceAlerts.value}
          change={AUDIT_STATS.complianceAlerts.change}
          isPositive={AUDIT_STATS.complianceAlerts.isPositive}
          period={AUDIT_STATS.complianceAlerts.period}
          icon={AlertTriangle}
        />
      </div>

      {/* KYC Verification Records Section */}
      <DataTable
        title="Pending KYC & Customer Due Diligence"
        subtitle="Identity verification dossiers requiring compliance officer sign-off"
        columns={kycColumns}
        data={kycRecords}
      />

      {/* Recent Audit Logs Table */}
      <DataTable
        title="Immutable Core Audit Trail"
        subtitle="WORM (Write Once Read Many) compliant banking log entries"
        columns={auditLogColumns}
        data={AUDIT_LOGS}
      />

      {/* Customer Account Activity Logs */}
      <DataTable
        title="High-Value Account Activity & MFA History"
        subtitle="Device fingerprint changes and elevation of transactional limits"
        columns={activityColumns}
        data={AUDIT_ACCOUNT_ACTIVITIES}
      />

      {/* KYC Review Modal */}
      {selectedKyc && (
        <Modal
          isOpen={!!selectedKyc}
          onClose={() => setSelectedKyc(null)}
          title={`KYC Certification: ${selectedKyc.customer}`}
          subtitle={`Packet ${selectedKyc.id} • ${selectedKyc.country}`}
          maxWidth="max-w-md"
          footer={
            <div className="flex items-center justify-between w-full">
              <button
                type="button"
                onClick={() => handleFlagKyc(selectedKyc.id, selectedKyc.customer)}
                className="px-3.5 py-2 text-xs font-bold text-amber-700 bg-amber-50 hover:bg-amber-100 rounded-xl flex items-center gap-1.5"
              >
                <Flag className="w-3.5 h-3.5" />
                <span>Flag for EDD</span>
              </button>
              <button
                type="button"
                onClick={() => handleVerifyKyc(selectedKyc.id, selectedKyc.customer)}
                className="px-3.5 py-2 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl flex items-center gap-1.5"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>Certify KYC</span>
              </button>
            </div>
          }
        >
          <div className="space-y-3 text-xs">
            <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800 space-y-1.5">
              <div className="flex justify-between">
                <span className="text-slate-400">Document Type:</span>
                <span className="font-semibold">{selectedKyc.docType}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Risk Assessment:</span>
                <span className="font-semibold">{selectedKyc.riskTier}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Submission Date:</span>
                <span>{selectedKyc.submissionDate}</span>
              </div>
            </div>
            <p className="text-slate-500 text-2xs">
              Automated OFAC, PEP, and Interpol Red Notice databases returned 0 derogatory matches for this applicant.
            </p>
          </div>
        </Modal>
      )}

      {/* Audit Log Modal */}
      {selectedAuditLog && (
        <Modal
          isOpen={!!selectedAuditLog}
          onClose={() => setSelectedAuditLog(null)}
          title={`Audit Event: ${selectedAuditLog.id}`}
          subtitle={`Logged on ${selectedAuditLog.timestamp}`}
          maxWidth="max-w-md"
          footer={
            <button
              type="button"
              onClick={() => setSelectedAuditLog(null)}
              className="px-4 py-2 text-xs font-bold text-white bg-emerald-600 rounded-xl"
            >
              Done
            </button>
          }
        >
          <div className="space-y-2.5 text-xs">
            <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Actor:</span>
              <span className="font-bold">{selectedAuditLog.actor}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Operation:</span>
              <span>{selectedAuditLog.action}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
              <span className="text-slate-400">Terminal IP:</span>
              <span className="font-mono">{selectedAuditLog.ipAddress}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-slate-400">Status:</span>
              <StatusBadge status={selectedAuditLog.severity} />
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default AuditDashboard;
