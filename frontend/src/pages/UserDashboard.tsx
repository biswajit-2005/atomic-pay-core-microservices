import React, { useState, useEffect } from 'react';
import { useAuth, DEMO_USERS } from '../context/AuthContext';
import { accountApi, transferApi } from '../services/api';
import { Transaction } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { Link } from 'react-router-dom';
import { Wallet, ArrowUpRight, RefreshCw, AlertTriangle, CheckCircle2, ShieldAlert, Cpu } from 'lucide-react';

export const UserDashboard: React.FC = () => {
  const { currentUser } = useAuth();
  const [balance, setBalance] = useState<number>(currentUser.initialBalance);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [transferModalOpen, setTransferModalOpen] = useState<boolean>(false);

  // Form State
  const [receiverId, setReceiverId] = useState<string>(DEMO_USERS[1].accountId);
  const [amount, setAmount] = useState<string>('1000.00');
  const [idempotencyKey, setIdempotencyKey] = useState<string>(`idemp-${Date.now()}`);
  const [simulateFailure, setSimulateFailure] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [notificationMsg, setNotificationMsg] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const balData = await accountApi.getBalance(currentUser.accountId);
      if (balData && typeof balData.balance === 'number') {
        setBalance(balData.balance);
      }
      const txs = await transferApi.getTransactionsForAccount(currentUser.accountId);
      setTransactions(txs);
    } catch (err) {
      console.warn('Backend API connection fallback to local state', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [currentUser.accountId]);

  const generateNewIdempotencyKey = () => {
    setIdempotencyKey(`idemp-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`);
  };

  const handleTransferSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setNotificationMsg(null);

    const numericAmount = parseFloat(amount);
    if (isNaN(numericAmount) || numericAmount <= 0) {
      setNotificationMsg({ type: 'error', text: 'Invalid transfer amount' });
      setSubmitting(false);
      return;
    }

    try {
      const result = await transferApi.createTransfer(
        idempotencyKey,
        currentUser.accountId,
        receiverId,
        numericAmount,
        simulateFailure || undefined
      );

      setNotificationMsg({
        type: 'success',
        text: `Transaction executed successfully! Status: ${result.status}`,
      });
      setTransferModalOpen(false);
      fetchData();
    } catch (err: any) {
      const errorResponse = err.response?.data?.error;
      const errorMsg = errorResponse?.message || err.message || 'Transfer failed';
      setNotificationMsg({
        type: 'error',
        text: `Transaction Error: ${errorMsg}`,
      });
      fetchData();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Alert Banner */}
      {notificationMsg && (
        <div
          className={`p-4 rounded-xl border flex items-center justify-between shadow-lg backdrop-blur-md ${
            notificationMsg.type === 'success'
              ? 'bg-emerald-950/80 border-emerald-500/40 text-emerald-200'
              : 'bg-rose-950/80 border-rose-500/40 text-rose-200'
          }`}
        >
          <div className="flex items-center gap-3">
            {notificationMsg.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            ) : (
              <AlertTriangle className="w-5 h-5 text-rose-400" />
            )}
            <span className="text-sm font-medium">{notificationMsg.text}</span>
          </div>
          <button
            onClick={() => setNotificationMsg(null)}
            className="text-xs opacity-70 hover:opacity-100 underline"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Header & Balance Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Wallet Balance Card */}
        <div className="md:col-span-2 relative overflow-hidden rounded-2xl bg-gradient-to-br from-slate-900 via-slate-900 to-indigo-950/60 border border-slate-800 p-6 shadow-2xl">
          <div className="absolute -right-10 -bottom-10 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none"></div>
          <div className="flex justify-between items-start">
            <div>
              <span className="text-xs font-mono uppercase tracking-wider text-cyan-400 font-semibold">
                Available Wallet Balance
              </span>
              <h1 className="text-4xl sm:text-5xl font-extrabold text-white mt-2 tracking-tight">
                ₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </h1>
              <p className="text-xs text-slate-400 mt-2 font-mono">
                Account No: <span className="text-slate-200">{currentUser.accountNumber}</span> | Currency: INR (NUMERIC 19,4)
              </p>
            </div>

            <button
              onClick={() => setTransferModalOpen(true)}
              className="px-5 py-3 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-600 hover:from-cyan-400 hover:to-indigo-500 text-white font-semibold text-sm shadow-lg shadow-cyan-500/25 flex items-center gap-2 transition-all duration-200 transform hover:-translate-y-0.5"
            >
              <ArrowUpRight className="w-4 h-4" />
              Transfer Funds
            </button>
          </div>

          <div className="mt-8 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
              PostgreSQL Row-Locking Concurrency Active
            </span>
            <button
              onClick={fetchData}
              className="flex items-center gap-1 text-slate-400 hover:text-cyan-400 transition-colors"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              Sync State
            </button>
          </div>
        </div>

        {/* Distributed Guarantees Status Card */}
        <div className="rounded-2xl bg-slate-900/90 border border-slate-800 p-6 shadow-xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
                <Cpu className="w-4 h-4 text-purple-400" />
                Reliability Guarantees
              </h3>
              <span className="text-[10px] bg-purple-950 text-purple-300 border border-purple-800 px-2 py-0.5 rounded-full font-mono">
                ACTIVE
              </span>
            </div>

            <ul className="space-y-2.5 text-xs text-slate-300">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Strict Idempotency via DB Unique Constraint</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Double-Entry Zero-Sum Ledger Integrity</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Orchestrated Saga & Compensation Engine</span>
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                <span>Transactional Outbox & Deduplicated Kafka</span>
              </li>
            </ul>
          </div>

          <div className="mt-4 pt-3 border-t border-slate-800 text-[11px] text-slate-400 font-mono">
            Active Role: <span className="text-cyan-400 font-bold">{currentUser.role}</span>
          </div>
        </div>
      </div>

      {/* Transaction History Table */}
      <div className="rounded-2xl bg-slate-900/90 border border-slate-800 p-6 shadow-xl">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h2 className="text-lg font-bold text-white flex items-center gap-2">
              <Wallet className="w-5 h-5 text-cyan-400" />
              Transaction Ledger History
            </h2>
            <p className="text-xs text-slate-400">Real-time state machine transitions and financial events</p>
          </div>
          <button
            onClick={fetchData}
            className="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase tracking-wider">
                <th className="py-3 px-4">Transaction ID</th>
                <th className="py-3 px-4">Amount</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Idempotency Key</th>
                <th className="py-3 px-4">Created At</th>
                <th className="py-3 px-4 text-right">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-xs">
              {transactions.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-8 text-center text-slate-500">
                    No transactions recorded yet. Click "Transfer Funds" above to initiate a financial request.
                  </td>
                </tr>
              ) : (
                transactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="py-3.5 px-4 font-mono font-medium text-slate-200">
                      {tx.id.substring(0, 8)}...
                    </td>
                    <td className="py-3.5 px-4 font-bold text-white">
                      ₹{tx.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={tx.status} />
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400 text-[11px]">
                      {tx.idempotencyKey}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">
                      {new Date(tx.createdAt).toLocaleTimeString()}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <Link
                        to={`/transactions/${tx.id}`}
                        className="inline-flex items-center gap-1 text-cyan-400 hover:text-cyan-300 font-medium hover:underline"
                      >
                        Inspect Saga
                      </Link>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Transfer Funds Modal */}
      {transferModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-md bg-slate-950/80">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 max-w-lg w-full shadow-2xl space-y-6 relative">
            <div className="flex justify-between items-center border-b border-slate-800 pb-4">
              <h3 className="text-lg font-bold text-white flex items-center gap-2">
                <ArrowUpRight className="w-5 h-5 text-cyan-400" />
                Initiate Idempotent Money Transfer
              </h3>
              <button
                onClick={() => setTransferModalOpen(false)}
                className="text-slate-400 hover:text-white text-lg font-bold"
              >
                &times;
              </button>
            </div>

            <form onSubmit={handleTransferSubmit} className="space-y-4 text-xs">
              {/* Receiver Selector */}
              <div>
                <label className="block text-slate-300 font-semibold mb-1">Select Beneficiary Account</label>
                <select
                  value={receiverId}
                  onChange={(e) => setReceiverId(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2.5 text-slate-200 focus:outline-none focus:border-cyan-500 font-mono"
                >
                  {DEMO_USERS.filter((u) => u.accountId !== currentUser.accountId).map((u) => (
                    <option key={u.id} value={u.accountId}>
                      {u.fullName} ({u.accountNumber})
                    </option>
                  ))}
                </select>
              </div>

              {/* Transfer Amount */}
              <div>
                <label className="block text-slate-300 font-semibold mb-1">Transfer Amount (INR ₹)</label>
                <input
                  type="number"
                  step="0.01"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2.5 text-white font-bold text-base focus:outline-none focus:border-cyan-500"
                  required
                />
              </div>

              {/* Idempotency Key Control */}
              <div>
                <div className="flex justify-between items-center mb-1">
                  <label className="text-slate-300 font-semibold flex items-center gap-1">
                    Idempotency-Key
                    <span className="text-[10px] text-amber-400 font-mono">(Prevents Duplicates)</span>
                  </label>
                  <button
                    type="button"
                    onClick={generateNewIdempotencyKey}
                    className="text-[11px] text-cyan-400 hover:underline flex items-center gap-1"
                  >
                    <RefreshCw className="w-3 h-3" />
                    New Key
                  </button>
                </div>
                <input
                  type="text"
                  value={idempotencyKey}
                  onChange={(e) => setIdempotencyKey(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-300 font-mono text-xs focus:outline-none focus:border-cyan-500"
                  required
                />
              </div>

              {/* Failure Simulation Selector (Section 35) */}
              <div className="p-3 bg-purple-950/30 border border-purple-900/50 rounded-xl space-y-1">
                <label className="block text-purple-300 font-bold flex items-center gap-1.5">
                  <ShieldAlert className="w-4 h-4 text-purple-400" />
                  Dev Failure Simulation Control (Header: X-Simulate-Failure)
                </label>
                <select
                  value={simulateFailure}
                  onChange={(e) => setSimulateFailure(e.target.value)}
                  className="w-full bg-slate-950 border border-purple-800/80 rounded-lg px-2.5 py-2 text-purple-200 font-mono text-xs focus:outline-none"
                >
                  <option value="">NORMAL (No failure simulation)</option>
                  <option value="FAIL_DEBIT">FAIL_DEBIT (Debit fails, no balance mutation)</option>
                  <option value="FAIL_CREDIT">FAIL_CREDIT (Credit fails -&gt; Triggers Saga Compensation!)</option>
                  <option value="FAIL_LEDGER">FAIL_LEDGER (Ledger fails -&gt; Triggers Full Compensation!)</option>
                  <option value="DELAY_CREDIT">DELAY_CREDIT (Timeout -&gt; Marks RECONCILIATION_REQUIRED)</option>
                </select>
              </div>

              {/* Submit Buttons */}
              <div className="flex gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setTransferModalOpen(false)}
                  className="flex-1 py-2.5 rounded-xl border border-slate-800 hover:bg-slate-800 text-slate-300 font-semibold"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="flex-1 py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 to-indigo-600 hover:from-cyan-400 hover:to-indigo-500 text-white font-bold shadow-lg shadow-cyan-500/25 flex items-center justify-center gap-2 disabled:opacity-50"
                >
                  {submitting ? <RefreshCw className="w-4 h-4 animate-spin" /> : 'Confirm Transfer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
