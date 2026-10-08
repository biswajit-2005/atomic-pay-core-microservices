import React, { createContext, useContext, useState } from 'react';

export interface DemoUser {
  id: string;
  email: string;
  fullName: string;
  role: 'USER' | 'ADMIN' | 'AUDITOR';
  accountId: string;
  accountNumber: string;
  initialBalance: number;
}

export const DEMO_USERS: DemoUser[] = [
  {
    id: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    email: 'alice@atomicpay.com',
    fullName: 'Alice Vance',
    role: 'USER',
    accountId: 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    accountNumber: 'ACC-1001-ALICE',
    initialBalance: 100000.0,
  },
  {
    id: 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22',
    email: 'bob@atomicpay.com',
    fullName: 'Bob Smith',
    role: 'USER',
    accountId: 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22',
    accountNumber: 'ACC-1002-BOB',
    initialBalance: 20000.0,
  },
  {
    id: 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33',
    email: 'charlie@atomicpay.com',
    fullName: 'Charlie Brown',
    role: 'USER',
    accountId: 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33',
    accountNumber: 'ACC-1003-CHARLIE',
    initialBalance: 5000.0,
  },
  {
    id: 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44',
    email: 'admin@atomicpay.com',
    fullName: 'System Admin',
    role: 'ADMIN',
    accountId: 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44',
    accountNumber: 'ACC-9999-ADMIN',
    initialBalance: 500000.0,
  },
];

interface AuthContextType {
  currentUser: DemoUser;
  switchUser: (userId: string) => void;
  token: string | null;
  setToken: (token: string | null) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentUser, setCurrentUser] = useState<DemoUser>(() => {
    const saved = localStorage.getItem('active_demo_user');
    if (saved) {
      const found = DEMO_USERS.find((u) => u.id === saved);
      if (found) return found;
    }
    return DEMO_USERS[0]; // Default to Alice
  });

  const [token, setTokenState] = useState<string | null>(() => localStorage.getItem('token') || 'demo-jwt-token-123');

  const setToken = (newToken: string | null) => {
    setTokenState(newToken);
    if (newToken) {
      localStorage.setItem('token', newToken);
    } else {
      localStorage.removeItem('token');
    }
  };

  const switchUser = (userId: string) => {
    const found = DEMO_USERS.find((u) => u.id === userId);
    if (found) {
      setCurrentUser(found);
      localStorage.setItem('active_demo_user', found.id);
    }
  };

  const logout = () => {
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ currentUser, switchUser, token, setToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return ctx;
};
