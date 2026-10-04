import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { ReconciliationRun, ReconciliationIssue, AuditLog } from '../types';
import { Shield, Play, CheckCircle2, AlertTriangle, FileText, RefreshCw, Wrench } from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'reconciliation' | 'audit'>('reconciliation');
  const [issues, setIssues] = useState<ReconciliationIssue[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [latestRun, setLatestRun] = useState<ReconciliationRun | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [runningRec, setRunningRec] = useState<boolean>(false);

  // Resolution Modal State
  const [resolveModalIssue, setResolveModalIssue] = useState<ReconciliationIssue | null>(null);
  const [resolutionText, setResolutionText] = useState<string>('');
  const [resolving, setResolving] = useState<boolean>(false);

  const fetchData = async () => {
    setLoading(true);
    try {
      const issueList = await adminApi.getReconciliationIssues();
      setIssues(issueList);

      const auditList = await adminApi.getAuditLogs(0, 50);
      setAuditLogs(auditList);
    } catch (err) {
      console.warn('Backend admin API connection fallback', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleRunReconciliation = async () => {
    setRunningRec(true);
    try {
      const runResult = await adminApi.runReconciliation();
      setLatestRun(runResult);
      fetchData();
    } catch (err) {
      console.error(err);
    } finally {
      setRunningRec(false);
    }
  };

  const handleResolveSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!resolveModalIssue || !resolutionText.trim()) return;
    setResolving(true);
    try {
      await adminApi.resolveIssue(resolveModalIssue.id, resolutionText);
      setResolveModalIssue(null);
      setResolutionText('');
      fetchData();
    } catch (err) {
      console.error(err);
    } finally {
      setResolving(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white flex items-center gap-2">
            <Shield className="w-6 h-6 text-purple-400" />
            Admin System & Financial Reconciliation Portal
          </h1>
          <p className="text-xs text-slate-400">
            Audit log exploration, automated mismatch detection, and manual issue resolution
          </p>
        </div>

        <button
          onClick={handleRunReconciliation}
          disabled={runningRec}
          className="px-5 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white font-bold text-xs shadow-lg shadow-purple-500/25 flex items-center gap-2 transition-all"
        >
          <Play className={`w-4 h-4 ${runningRec ? 'animate-spin' : ''}`} />
          Run Automated Reconciliation Batch
        </button>
      </div>

      {/* Latest Run Banner */}
      {latestRun && (
        <div className="p-4 bg-purple-950/40 border border-purple-800/80 rounded-2xl flex items-center justify-between text-xs text-purple-200">
          <div className="flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 text-purple-400" />
            <div>
              <span className="font-bold block">Reconciliation Batch Completed ({latestRun.id.substring(0, 8)})</span>
              <span>Evaluated {latestRun.totalTransactions} transactions | Found {latestRun.mismatches} mismatches</span>
            </div>
          </div>
          <span className="font-mono text-slate-400">{new Date(latestRun.startedAt).toLocaleTimeString()}</span>
        </div>
      )}

      {/* Tab Controls */}
      <div className="flex gap-4 border-b border-slate-800">
        <button
          onClick={() => setActiveTab('reconciliation')}
          className={`pb-3 px-1 text-sm font-bold border-b-2 transition-all flex items-center gap-2 ${
            activeTab === 'reconciliation'
              ? 'border-purple-500 text-purple-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <AlertTriangle className="w-4 h-4" />
          Reconciliation Issues ({issues.length})
        </button>
        <button
          onClick={() => setActiveTab('audit')}
          className={`pb-3 px-1 text-sm font-bold border-b-2 transition-all flex items-center gap-2 ${
            activeTab === 'audit'
              ? 'border-purple-500 text-purple-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <FileText className="w-4 h-4" />
          System Audit Logs ({auditLogs.length})
        </button>
      </div>

      {/* Tab 1: Reconciliation Issues */}
      {activeTab === 'reconciliation' && (
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-xl space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-base font-bold text-white">Financial Mismatch & Reconciliation Issue Desk</h2>
            <button onClick={fetchData} className="p-1.5 rounded bg-slate-800 text-slate-400 hover:text-white">
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 font-semibold uppercase">
                  <th className="py-3 px-4">Issue ID</th>
                  <th className="py-3 px-4">Issue Type</th>
                  <th className="py-3 px-4">Target Ref ID</th>
                  <th className="py-3 px-4">Expected vs Actual</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {issues.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-slate-500">
                      No reconciliation issues detected. System is 100% financially balanced.
                    </td>
                  </tr>
                ) : (
                  issues.map((issue) => (
                    <tr key={issue.id} className="hover:bg-slate-800/40">
                      <td className="py-3 px-4 text-slate-400">{issue.id.substring(0, 8)}...</td>
                      <td className="py-3 px-4 font-bold text-amber-400">{issue.issueType}</td>
                      <td className="py-3 px-4 text-cyan-400">
                        {issue.transactionId ? `Tx: ${issue.transactionId.substring(0, 8)}...` : `Acc: ${issue.accountId?.substring(0, 8)}...`}
                      </td>
                      <td className="py-3 px-4 text-slate-300">
                        Exp: {issue.expectedValue ?? 'N/A'} | Act: {issue.actualValue ?? 'N/A'}
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`px-2 py-0.5 rounded font-bold text-[11px] ${
                            issue.status === 'RESOLVED'
                              ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                              : 'bg-amber-950 text-amber-400 border border-amber-800'
                          }`}
                        >
                          {issue.status}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        {issue.status === 'OPEN' ? (
                          <button
                            onClick={() => {
                              setResolveModalIssue(issue);
                              setResolutionText('');
                            }}
                            className="px-2.5 py-1 bg-purple-900/80 hover:bg-purple-800 text-purple-200 rounded flex items-center gap-1 ml-auto"
                          >
                            <Wrench className="w-3 h-3" />
                            Resolve
                          </button>
                        ) : (
                          <span className="text-slate-500 text-[11px]">Resolved</span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 2: System Audit Logs */}
      {activeTab === 'audit' && (
        <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-xl space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-base font-bold text-white">Immutable Event Audit Trail (audit.audit_logs)</h2>
            <button onClick={fetchData} className="p-1.5 rounded bg-slate-800 text-slate-400 hover:text-white">
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs font-mono">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 font-semibold uppercase">
                  <th className="py-3 px-4">Audit ID</th>
                  <th className="py-3 px-4">Action</th>
                  <th className="py-3 px-4">Transaction ID</th>
                  <th className="py-3 px-4">Correlation ID</th>
                  <th className="py-3 px-4">Created At</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {auditLogs.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="py-8 text-center text-slate-500">
                      No audit logs recorded.
                    </td>
                  </tr>
                ) : (
                  auditLogs.map((log) => (
                    <tr key={log.id} className="hover:bg-slate-800/40">
                      <td className="py-3 px-4 text-slate-400">{log.id.substring(0, 8)}...</td>
                      <td className="py-3 px-4 font-bold text-cyan-400">{log.action}</td>
                      <td className="py-3 px-4 text-slate-300">{log.transactionId ? log.transactionId.substring(0, 8) + '...' : 'N/A'}</td>
                      <td className="py-3 px-4 text-purple-300">{log.correlationId || 'N/A'}</td>
                      <td className="py-3 px-4 text-slate-400">{new Date(log.createdAt).toLocaleString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Issue Resolution Modal */}
      {resolveModalIssue && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-md bg-slate-950/80">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-md w-full shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-white flex items-center gap-2">
              <Wrench className="w-5 h-5 text-purple-400" />
              Resolve Reconciliation Issue
            </h3>

            <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-xs font-mono text-slate-300 space-y-1">
              <div>Issue Type: <span className="text-amber-400 font-bold">{resolveModalIssue.issueType}</span></div>
              <div>Issue ID: {resolveModalIssue.id}</div>
            </div>

            <form onSubmit={handleResolveSubmit} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-300 font-semibold mb-1">Resolution Notes</label>
                <textarea
                  value={resolutionText}
                  onChange={(e) => setResolutionText(e.target.value)}
                  placeholder="Describe resolution action (e.g. Manually compensated balance or re-posted ledger entry)"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-slate-200 focus:outline-none focus:border-purple-500 h-24"
                  required
                />
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={() => setResolveModalIssue(null)}
                  className="flex-1 py-2 rounded-xl border border-slate-800 text-slate-300"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={resolving}
                  className="flex-1 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-bold"
                >
                  {resolving ? 'Submitting...' : 'Mark Resolved'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
