import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { ledgerApi } from '../services/api';
import { LedgerEntry, BalanceVerification } from '../types';
import { BookOpen, ShieldCheck, ShieldAlert, RefreshCw, Layers } from 'lucide-react';

export const LedgerView: React.FC = () => {
  const { currentUser } = useAuth();
  const [entries, setEntries] = useState<LedgerEntry[]>([]);
  const [verification, setVerification] = useState<BalanceVerification | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [verifying, setVerifying] = useState<boolean>(false);

  const fetchLedgerData = async () => {
    setLoading(true);
    try {
      const data = await ledgerApi.getLedgerForAccount(currentUser.accountId);
      setEntries(data);

      const verData = await ledgerApi.verifyAccountBalance(currentUser.accountId);
      setVerification(verData);
    } catch (err) {
      console.warn('Backend ledger API fallback', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLedgerData();
  }, [currentUser.accountId]);

  const runVerificationAudit = async () => {
    setVerifying(true);
    try {
      const verData = await ledgerApi.verifyAccountBalance(currentUser.accountId);
      setVerification(verData);
    } catch (err) {
      console.error(err);
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white flex items-center gap-2">
            <BookOpen className="w-6 h-6 text-cyan-400" />
            Double-Entry Immutable Ledger Engine
          </h1>
          <p className="text-xs text-slate-400">
            Append-only financial entries mapped to ledger.ledger_entries. Never updated or deleted.
          </p>
        </div>

        <button
          onClick={runVerificationAudit}
          disabled={verifying}
          className="px-4 py-2.5 rounded-xl bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-slate-950 font-bold text-xs shadow-lg shadow-emerald-500/20 flex items-center gap-2 transition-all"
        >
          <ShieldCheck className={`w-4 h-4 ${verifying ? 'animate-spin' : ''}`} />
          Run Ledger Audit Verification
        </button>
      </div>

      {/* Balance Verification Summary Card */}
      {verification && (
        <div
          className={`p-6 rounded-2xl border backdrop-blur-md shadow-2xl transition-all ${
            verification.isBalanced
              ? 'bg-emerald-950/40 border-emerald-500/30 text-emerald-200'
              : 'bg-rose-950/40 border-rose-500/40 text-rose-200'
          }`}
        >
          <div className="flex items-center justify-between border-b border-current/20 pb-4 mb-4">
            <div className="flex items-center gap-3">
              {verification.isBalanced ? (
                <ShieldCheck className="w-7 h-7 text-emerald-400" />
              ) : (
                <ShieldAlert className="w-7 h-7 text-rose-400 animate-bounce" />
              )}
              <div>
                <h3 className="text-base font-bold">
                  {verification.isBalanced ? 'Zero-Sum Ledger Audit PASSED' : 'Ledger Mismatch Detected'}
                </h3>
                <p className="text-xs opacity-80">{verification.message}</p>
              </div>
            </div>
            <span
              className={`px-3 py-1 rounded-full text-xs font-mono font-bold ${
                verification.isBalanced ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'
              }`}
            >
              {verification.isBalanced ? 'AUDIT VERIFIED' : 'UNBALANCED'}
            </span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs font-mono">
            <div>
              <span className="opacity-70 block mb-1">Account Balance</span>
              <span className="text-lg font-bold">₹{verification.accountBalance.toFixed(2)}</span>
            </div>
            <div>
              <span className="opacity-70 block mb-1">Calculated Ledger Sum</span>
              <span className="text-lg font-bold">₹{verification.calculatedLedgerBalance.toFixed(2)}</span>
            </div>
            <div>
              <span className="opacity-70 block mb-1">Total Debits</span>
              <span className="text-lg font-bold text-rose-400">₹{verification.totalDebits.toFixed(2)}</span>
            </div>
            <div>
              <span className="opacity-70 block mb-1">Total Credits</span>
              <span className="text-lg font-bold text-emerald-400">₹{verification.totalCredits.toFixed(2)}</span>
            </div>
          </div>
        </div>
      )}

      {/* Ledger Table */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-xl space-y-4">
        <div className="flex justify-between items-center border-b border-slate-800 pb-4">
          <h2 className="text-base font-bold text-white flex items-center gap-2">
            <Layers className="w-4 h-4 text-cyan-400" />
            Immutable Ledger Entries for Account {currentUser.accountNumber}
          </h2>
          <button
            onClick={fetchLedgerData}
            className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:text-white"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 font-semibold uppercase">
                <th className="py-3 px-4">Entry ID</th>
                <th className="py-3 px-4">Transaction ID</th>
                <th className="py-3 px-4">Entry Type</th>
                <th className="py-3 px-4">Amount</th>
                <th className="py-3 px-4">Seq No</th>
                <th className="py-3 px-4">Created At</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {entries.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-500">
                    No ledger entries recorded for this account.
                  </td>
                </tr>
              ) : (
                entries.map((entry) => (
                  <tr key={entry.id} className="hover:bg-slate-800/40">
                    <td className="py-3.5 px-4 text-slate-300">{entry.id.substring(0, 8)}...</td>
                    <td className="py-3.5 px-4 text-cyan-400">{entry.transactionId.substring(0, 8)}...</td>
                    <td className="py-3.5 px-4">
                      <span
                        className={`px-2.5 py-1 rounded-md font-bold text-xs ${
                          entry.entryType === 'DEBIT'
                            ? 'bg-rose-950 text-rose-400 border border-rose-800'
                            : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                        }`}
                      >
                        {entry.entryType}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 font-bold text-white text-sm">₹{entry.amount.toFixed(2)}</td>
                    <td className="py-3.5 px-4 text-slate-400">{entry.sequenceNo}</td>
                    <td className="py-3.5 px-4 text-slate-400">{new Date(entry.createdAt).toLocaleString()}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
