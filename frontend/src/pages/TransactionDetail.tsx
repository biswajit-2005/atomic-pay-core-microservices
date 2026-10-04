import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { transferApi, ledgerApi } from '../services/api';
import { Transaction, LedgerEntry } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { ArrowLeft, CheckCircle2, XCircle, Clock, ShieldCheck, FileText } from 'lucide-react';

export const TransactionDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [transaction, setTransaction] = useState<Transaction | null>(null);
  const [ledgerEntries, setLedgerEntries] = useState<LedgerEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    const fetchDetails = async () => {
      setLoading(true);
      try {
        const txData = await transferApi.getTransaction(id);
        setTransaction(txData);

        const ledgerData = await ledgerApi.getLedgerForTransaction(id);
        setLedgerEntries(ledgerData);
      } catch (err: any) {
        setError(err.message || 'Failed to fetch transaction details');
      } finally {
        setLoading(false);
      }
    };
    fetchDetails();
  }, [id]);

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center text-slate-400">
        <div className="w-8 h-8 border-4 border-cyan-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
        Loading transaction Saga details...
      </div>
    );
  }

  if (error || !transaction) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center space-y-4">
        <div className="p-4 bg-rose-950/80 border border-rose-800 text-rose-300 rounded-xl inline-block">
          {error || 'Transaction not found'}
        </div>
        <div>
          <Link to="/" className="text-cyan-400 hover:underline flex items-center justify-center gap-1">
            <ArrowLeft className="w-4 h-4" /> Back to Dashboard
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Nav Header */}
      <div className="flex items-center justify-between">
        <Link to="/" className="inline-flex items-center gap-2 text-sm text-cyan-400 hover:text-cyan-300 font-medium">
          <ArrowLeft className="w-4 h-4" /> Back to Dashboard
        </Link>
        <StatusBadge status={transaction.status} />
      </div>

      {/* Transaction Overview Card */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-xl space-y-6">
        <div className="border-b border-slate-800 pb-4 flex justify-between items-start">
          <div>
            <span className="text-xs font-mono text-cyan-400 uppercase">Transaction Record</span>
            <h1 className="text-2xl font-bold text-white font-mono mt-1">{transaction.id}</h1>
          </div>
          <div className="text-right">
            <span className="text-xs text-slate-400">Total Amount</span>
            <div className="text-3xl font-extrabold text-white">
              ₹{transaction.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>
          </div>
        </div>

        {/* Metadata Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
          <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
            <span className="text-slate-400 block mb-1">Sender Account</span>
            <span className="font-mono text-slate-200 font-semibold">{transaction.senderAccountId.substring(0, 13)}...</span>
          </div>

          <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
            <span className="text-slate-400 block mb-1">Receiver Account</span>
            <span className="font-mono text-slate-200 font-semibold">{transaction.receiverAccountId.substring(0, 13)}...</span>
          </div>

          <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
            <span className="text-slate-400 block mb-1">Idempotency Key</span>
            <span className="font-mono text-amber-300 font-semibold truncate block">{transaction.idempotencyKey}</span>
          </div>

          <div className="p-3 bg-slate-950/60 rounded-xl border border-slate-800">
            <span className="text-slate-400 block mb-1">Correlation ID</span>
            <span className="font-mono text-slate-300 truncate block">{transaction.correlationId || 'N/A'}</span>
          </div>
        </div>

        {transaction.failureReason && (
          <div className="p-4 bg-rose-950/60 border border-rose-800/80 rounded-xl text-xs text-rose-300 flex items-start gap-2">
            <XCircle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
            <div>
              <strong className="block font-semibold">Failure Details ({transaction.failureCode}):</strong>
              {transaction.failureReason}
            </div>
          </div>
        )}
      </div>

      {/* Saga Execution Steps Section */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-xl space-y-6">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-purple-400" />
            Saga Execution Timeline & State Step Trace
          </h2>
          <p className="text-xs text-slate-400">Order-enforced saga orchestration step log from transactions.transaction_steps</p>
        </div>

        <div className="relative border-l-2 border-slate-800 ml-4 space-y-6 pl-6">
          {transaction.steps && transaction.steps.length > 0 ? (
            transaction.steps.map((step) => (
              <div key={step.id} className="relative group">
                {/* Step Node Marker */}
                <div className={`absolute -left-[31px] top-1 w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold ${
                  step.status === 'SUCCESS' ? 'bg-emerald-500 text-slate-950' : step.status === 'FAILED' ? 'bg-rose-500 text-white' : 'bg-amber-500 text-slate-950 animate-pulse'
                }`}>
                  {step.stepOrder}
                </div>

                <div className="bg-slate-950/70 border border-slate-800 rounded-xl p-4 space-y-2">
                  <div className="flex justify-between items-center">
                    <span className="font-mono font-bold text-sm text-cyan-300">
                      Step {step.stepOrder}: {step.stepType}
                    </span>
                    <span className={`text-xs px-2 py-0.5 rounded font-mono font-semibold ${
                      step.status === 'SUCCESS' ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-rose-950 text-rose-400 border border-rose-800'
                    }`}>
                      {step.status}
                    </span>
                  </div>

                  {step.errorMessage && (
                    <div className="text-xs text-rose-300 bg-rose-950/40 p-2 rounded border border-rose-900">
                      Error ({step.errorCode}): {step.errorMessage}
                    </div>
                  )}

                  <div className="flex justify-between items-center text-[11px] text-slate-400 font-mono pt-1 border-t border-slate-900">
                    <span>Attempts: {step.attempts}</span>
                    <span>Started: {step.startedAt ? new Date(step.startedAt).toLocaleTimeString() : 'N/A'}</span>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <p className="text-xs text-slate-500">No explicit steps recorded for this transaction.</p>
          )}
        </div>
      </div>

      {/* Paired Double-Entry Ledger Entries */}
      <div className="rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-xl space-y-4">
        <div>
          <h2 className="text-lg font-bold text-white flex items-center gap-2">
            <FileText className="w-5 h-5 text-emerald-400" />
            Paired Double-Entry Ledger Entries
          </h2>
          <p className="text-xs text-slate-400">Balanced debit & credit entries recorded in ledger.ledger_entries</p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 font-semibold uppercase">
                <th className="py-2.5 px-4">Seq</th>
                <th className="py-2.5 px-4">Type</th>
                <th className="py-2.5 px-4">Ledger Account ID</th>
                <th className="py-2.5 px-4">Amount</th>
                <th className="py-2.5 px-4">Created At</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 font-mono">
              {ledgerEntries.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-4 text-center text-slate-500">
                    No double-entry ledger entries exist for this transaction state.
                  </td>
                </tr>
              ) : (
                ledgerEntries.map((entry) => (
                  <tr key={entry.id} className="hover:bg-slate-800/40">
                    <td className="py-3 px-4 text-slate-400">{entry.sequenceNo}</td>
                    <td className="py-3 px-4">
                      <span className={`px-2 py-0.5 rounded font-bold ${
                        entry.entryType === 'DEBIT' ? 'bg-rose-950 text-rose-400 border border-rose-800' : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                      }`}>
                        {entry.entryType}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-300">{entry.ledgerAccountId}</td>
                    <td className="py-3 px-4 font-bold text-white">₹{entry.amount.toFixed(2)}</td>
                    <td className="py-3 px-4 text-slate-400">{new Date(entry.createdAt).toLocaleTimeString()}</td>
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
