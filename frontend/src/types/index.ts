export type TransactionStatus =
  | 'INITIATED'
  | 'VALIDATING'
  | 'PROCESSING'
  | 'DEBITED'
  | 'CREDITED'
  | 'COMPLETED'
  | 'PENDING'
  | 'COMPENSATING'
  | 'COMPENSATED'
  | 'FAILED'
  | 'RECONCILIATION_REQUIRED';

export type StepType =
  | 'VALIDATE'
  | 'DEBIT'
  | 'CREDIT'
  | 'LEDGER'
  | 'COMPLETE'
  | 'COMPENSATE_DEBIT';

export type StepStatus = 'IN_PROGRESS' | 'SUCCESS' | 'FAILED';

export type EntryType = 'DEBIT' | 'CREDIT';

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: 'USER' | 'ADMIN' | 'AUDITOR';
  status: string;
}

export interface Account {
  id: string;
  userId: string;
  accountNumber: string;
  currency: string;
  accountType: string;
  status: string;
  currentBalance: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionStep {
  id: string;
  stepOrder: number;
  stepType: StepType;
  status: StepStatus;
  attempts: number;
  startedAt?: string;
  completedAt?: string;
  errorCode?: string;
  errorMessage?: string;
}

export interface Transaction {
  id: string;
  idempotencyKey: string;
  senderAccountId: string;
  receiverAccountId: string;
  amount: number;
  currency: string;
  status: TransactionStatus;
  failureCode?: string;
  failureReason?: string;
  correlationId?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  steps?: TransactionStep[];
}

export interface LedgerEntry {
  id: string;
  transactionId: string;
  ledgerAccountId: string;
  entryType: EntryType;
  amount: number;
  currency: string;
  sequenceNo: number;
  createdAt: string;
}

export interface BalanceVerification {
  accountId: string;
  accountBalance: number;
  calculatedLedgerBalance: number;
  totalDebits: number;
  totalCredits: number;
  isBalanced: boolean;
  message: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  transactionId?: string;
  type: string;
  title: string;
  message: string;
  readAt?: string;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  eventId?: string;
  actorUserId?: string;
  transactionId?: string;
  action: string;
  entityType?: string;
  entityId?: string;
  metadata?: string;
  ipAddress?: string;
  userAgent?: string;
  correlationId?: string;
  createdAt: string;
}

export interface ReconciliationRun {
  id: string;
  startedAt: string;
  completedAt?: string;
  status: string;
  totalTransactions: number;
  mismatches: number;
}

export interface ReconciliationIssue {
  id: string;
  runId: string;
  transactionId?: string;
  accountId?: string;
  issueType: string;
  expectedValue?: number;
  actualValue?: number;
  status: string;
  resolution?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error?: {
    code?: string;
    message?: string;
    details?: any;
  };
  timestamp: string;
  requestId: string;
}
