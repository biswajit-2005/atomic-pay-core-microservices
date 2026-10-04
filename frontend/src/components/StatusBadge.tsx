import React from 'react';
import { TransactionStatus } from '../types';

interface StatusBadgeProps {
  status: TransactionStatus | string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status }) => {
  let colorStyle = 'bg-slate-800 text-slate-300 border-slate-700';

  switch (status) {
    case 'COMPLETED':
    case 'SUCCESS':
      colorStyle = 'bg-emerald-950/80 text-emerald-400 border-emerald-500/30 shadow-emerald-950/50 shadow-sm';
      break;
    case 'PENDING':
    case 'PROCESSING':
    case 'VALIDATING':
    case 'INITIATED':
      colorStyle = 'bg-amber-950/80 text-amber-400 border-amber-500/30 animate-pulse';
      break;
    case 'COMPENSATING':
      colorStyle = 'bg-purple-950/80 text-purple-400 border-purple-500/30 animate-pulse';
      break;
    case 'COMPENSATED':
      colorStyle = 'bg-indigo-950/80 text-indigo-400 border-indigo-500/30';
      break;
    case 'FAILED':
      colorStyle = 'bg-rose-950/80 text-rose-400 border-rose-500/30';
      break;
    case 'RECONCILIATION_REQUIRED':
      colorStyle = 'bg-orange-950/80 text-orange-400 border-orange-500/40 animate-bounce';
      break;
    case 'DEBITED':
    case 'CREDITED':
      colorStyle = 'bg-sky-950/80 text-sky-400 border-sky-500/30';
      break;
  }

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold border ${colorStyle}`}
    >
      <span className="w-1.5 h-1.5 rounded-full bg-current"></span>
      {status}
    </span>
  );
};
