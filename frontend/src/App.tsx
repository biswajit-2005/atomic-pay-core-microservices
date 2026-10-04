import React from 'react';

export function App() {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col items-center justify-center p-6">
      <div className="max-w-2xl text-center space-y-6 bg-slate-900/60 p-8 rounded-2xl border border-slate-800 shadow-2xl backdrop-blur-md">
        <h1 className="text-4xl font-bold tracking-tight bg-gradient-to-r from-blue-400 via-indigo-400 to-purple-400 bg-clip-text text-transparent">
          Atomic-Pay Financial System
        </h1>
        <p className="text-slate-400 text-lg leading-relaxed">
          High-Reliability Distributed Digital Banking Platform with Idempotent Transfers, Double-Entry Ledger, Saga Orchestration, and Outbox Event Streams.
        </p>
        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 font-medium text-sm">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
          Phase 1 Infrastructure Ready
        </div>
      </div>
    </div>
  );
}

export default App;
