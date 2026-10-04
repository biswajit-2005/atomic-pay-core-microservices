import axios from 'axios';
import {
  ApiResponse,
  Account,
  Transaction,
  LedgerEntry,
  BalanceVerification,
  NotificationItem,
  AuditLog,
  ReconciliationRun,
  ReconciliationIssue,
} from '../types';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers['X-Correlation-ID'] = `corr-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
  return config;
});

export default api;

// Auth Services
export const authApi = {
  login: async (email: string, passwordHash: string) => {
    const res = await api.post<ApiResponse<any>>('/auth/login', { email, passwordHash });
    return res.data;
  },
  register: async (email: string, passwordHash: string, fullName: string, phone?: string) => {
    const res = await api.post<ApiResponse<any>>('/auth/register', { email, passwordHash, fullName, phone });
    return res.data;
  },
};

// Account Services
export const accountApi = {
  getAccounts: async () => {
    const res = await api.get<ApiResponse<Account[]>>('/accounts');
    return res.data.data || [];
  },
  getAccountById: async (accountId: string) => {
    const res = await api.get<ApiResponse<Account>>(`/accounts/${accountId}`);
    return res.data.data;
  },
  getBalance: async (accountId: string) => {
    const res = await api.get<ApiResponse<{ accountId: string; balance: number; currency: string }>>(`/accounts/${accountId}/balance`);
    return res.data.data;
  },
  createAccount: async (userId: string, accountType: string = 'SAVINGS') => {
    const res = await api.post<ApiResponse<Account>>('/accounts', { userId, accountType, currency: 'INR' });
    return res.data.data;
  },
};

// Transfer / Transaction Services
export const transferApi = {
  createTransfer: async (
    idempotencyKey: string,
    senderAccountId: string,
    receiverAccountId: string,
    amount: number,
    simulateFailure?: string
  ) => {
    const headers: Record<string, string> = {
      'Idempotency-Key': idempotencyKey,
      'X-User-Id': senderAccountId,
    };
    if (simulateFailure) {
      headers['X-Simulate-Failure'] = simulateFailure;
    }

    const res = await api.post<ApiResponse<Transaction>>(
      '/transfers',
      { senderAccountId, receiverAccountId, amount, currency: 'INR' },
      { headers }
    );
    return res.data.data;
  },

  getTransaction: async (transactionId: string) => {
    const res = await api.get<ApiResponse<Transaction>>(`/transfers/${transactionId}`);
    return res.data.data;
  },

  getTransactionsForAccount: async (accountId: string) => {
    const res = await api.get<ApiResponse<Transaction[]>>(`/transfers?accountId=${accountId}`);
    return res.data.data || [];
  },
};

// Ledger Services
export const ledgerApi = {
  getLedgerForAccount: async (accountId: string) => {
    const res = await api.get<ApiResponse<LedgerEntry[]>>(`/accounts/${accountId}/ledger`);
    return res.data.data || [];
  },
  getLedgerForTransaction: async (transactionId: string) => {
    const res = await api.get<ApiResponse<LedgerEntry[]>>(`/transactions/${transactionId}/ledger`);
    return res.data.data || [];
  },
  verifyAccountBalance: async (accountId: string) => {
    const res = await api.get<ApiResponse<BalanceVerification>>(`/accounts/${accountId}/ledger/verify`);
    return res.data.data;
  },
};

// Notification Services
export const notificationApi = {
  getNotifications: async (userId: string) => {
    const res = await api.get<ApiResponse<NotificationItem[]>>(`/notifications?userId=${userId}`);
    return res.data.data || [];
  },
  markAsRead: async (notificationId: string) => {
    const res = await api.patch<ApiResponse<NotificationItem>>(`/notifications/${notificationId}/read`);
    return res.data.data;
  },
};

// Admin Services
export const adminApi = {
  getAuditLogs: async (page = 0, size = 50) => {
    const res = await api.get<ApiResponse<AuditLog[]>>(`/admin/audit-logs?page=${page}&size=${size}`);
    return res.data.data || [];
  },
  runReconciliation: async () => {
    const res = await api.post<ApiResponse<ReconciliationRun>>('/admin/reconciliation/run');
    return res.data.data;
  },
  getReconciliationIssues: async (status?: string) => {
    const url = status ? `/admin/reconciliation/issues?status=${status}` : '/admin/reconciliation/issues';
    const res = await api.get<ApiResponse<ReconciliationIssue[]>>(url);
    return res.data.data || [];
  },
  resolveIssue: async (issueId: string, resolution: string) => {
    const res = await api.patch<ApiResponse<ReconciliationIssue>>(`/admin/reconciliation/issues/${issueId}/resolve`, { resolution });
    return res.data.data;
  },
};
