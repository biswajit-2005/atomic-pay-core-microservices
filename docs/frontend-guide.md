# Complete Frontend Development & Architecture Guide — Atomic-Pay

This document provides a comprehensive guide for building, running, and understanding the **Atomic-Pay** frontend web application. The frontend is a modern, high-performance React application built with **Vite**, **TypeScript**, and **TailwindCSS**, supporting both **User Wallet Workflows** (Send/Receive Money, Saga Step Tracing, Immutable Ledger Verification, Real-Time Kafka Alerts) and **Admin Workflows** (Automated Financial Reconciliation, Audit Trail Search, Mismatch Desk Resolution).

---

## 🏗️ Tech Stack & Key Libraries

- **Framework**: React 18 with Vite 5 (`type: module`, JSX transform `react-jsx`)
- **Language**: TypeScript 5 (Strict Mode enabled)
- **Styling**: Vanilla CSS + TailwindCSS 3 (Dark Mode palette: `slate-950`, `cyan-400`, `purple-500`, `emerald-400`, `rose-500`)
- **Icons**: Lucide React (`lucide-react`)
- **Routing**: React Router DOM 6 (`BrowserRouter`, `Routes`, `Route`, `Link`, `useParams`, `useLocation`)
- **HTTP Client**: Axios 1.6 with custom interceptors for `Authorization: Bearer <token>`, `X-Correlation-ID`, and `Idempotency-Key`
- **State Management**: React Context API (`AuthContext`) + Local Component State

---

## 📁 Project Directory Structure

```text
frontend/
├── index.html                  # HTML5 Entry point with Google Fonts (Inter/Outfit)
├── package.json                # Project dependencies & build scripts
├── tsconfig.json               # TypeScript compiler options (strict mode)
├── vite.config.ts              # Vite server & API proxy config (port 3000 -> 8080)
└── src/
    ├── main.tsx                # Application root DOM mount point
    ├── App.tsx                 # App router setup & AuthProvider wrapper
    ├── index.css               # TailwindCSS directives & global styles
    ├── types/
    │   └── index.ts            # TypeScript interfaces (Account, Transaction, LedgerEntry, etc.)
    ├── context/
    │   └── AuthContext.tsx     # Demo user switcher & JWT authentication state
    ├── services/
    │   └── api.ts              # Centralized Axios instance & API method calls
    ├── components/
    │   ├── Navbar.tsx          # Top navigation header & Demo User switcher
    │   └── StatusBadge.tsx     # Visual status badge for transaction states
    └── pages/
        ├── UserDashboard.tsx   # User Wallet, Send Money Modal & History
        ├── TransactionDetail.tsx # Saga step execution timeline & paired ledger
        ├── LedgerView.tsx      # Immutable double-entry engine & math audit widget
        ├── NotificationsView.tsx # Real-time Kafka transaction alerts inbox
        └── AdminDashboard.tsx  # Reconciliation runner, Audit logs & Mismatch desk
```

---

## 👤 1. User Pages & Features

### 1.1 User Wallet Dashboard (`src/pages/UserDashboard.tsx`)

- **Purpose**: Serves as the primary dashboard for wallet holders to view available balances, initiate transfer transactions, and view recent transaction history.
- **Key Components & Features**:
  1. **Real-time Balance Card**: Displays active account balance formatted in Indian Rupees (e.g., `₹1,00,000.00`).
  2. **Send Money Modal Popup**:
     - Allows selecting a receiver account (`Bob Smith`, `Charlie Brown`, `System Admin`).
     - Amount input with numeric validation.
     - **Auto-generated Idempotency Key**: Generates a unique UUID key (e.g., `idemp-1791234000-abcd`) for every transfer attempt to prevent duplicate debiting on network retries.
     - **Failure Simulation Selector**: Dropdown to simulate failure scenarios (`FAIL_CREDIT` to test backward Saga compensation, `DELAY_CREDIT` to test reconciliation engine).
  3. **Transaction History Table**:
     - Lists all incoming and outgoing transfers for the active user.
     - Color-coded badges for statuses (`COMPLETED`, `PENDING`, `FAILED`, `COMPENSATING`, `COMPENSATED`).
     - Clickable rows linking to full Saga execution trace.

### 1.2 Transaction Detail & Saga Execution Step Trace (`src/pages/TransactionDetail.tsx`)

- **Purpose**: Provides deep observability into distributed Saga transaction execution.
- **Key Components & Features**:
  1. **Transaction Overview Header**: Displays Transaction ID, Amount, Idempotency Key, Correlation ID, and Failure Reason (if any).
  2. **Saga Execution Timeline**:
     - Step-by-step visual trace of the Saga workflow:
       - **Step 1: DEBIT_SENDER** (Locks & debits sender balance)
       - **Step 2: CREDIT_RECEIVER** (Credits receiver balance)
       - **Step 3: RECORD_LEDGER** (Posts balanced debit & credit entries)
     - Shows step status (`SUCCESS`, `FAILED`), execution timestamp, attempt count, and error codes.
  3. **Paired Double-Entry Ledger Entries Table**: Displays exact debit and credit entries associated with the transaction ID.

### 1.3 Double-Entry Ledger Engine (`src/pages/LedgerView.tsx`)

- **Purpose**: Exposes the immutable financial ledger audit log.
- **Key Components & Features**:
  1. **Mathematical Balance Audit Widget**:
     - Calls `/api/v1/accounts/{id}/ledger/verify`.
     - Compares Current Account Balance with `Sum(Credits) - Sum(Debits)`.
     - Displays `isBalanced: true` badge and discrepancy counter (`₹0.00`).
  2. **Immutable Entry Stream**: Displays sequence number, entry type (`DEBIT` vs `CREDIT`), ledger account ID, amount, and timestamp.

### 1.4 Real-Time Notification Feed (`src/pages/NotificationsView.tsx`)

- **Purpose**: Displays user alerts published via Kafka (`transaction.events` consumer).
- **Key Components & Features**:
  - Alert cards for `TRANSACTION_COMPLETED`, `TRANSACTION_FAILED`, and `SAGA_COMPENSATED`.
  - Mark notification as read interaction.
  - Navbar unread count badge.

---

## 🛡️ 2. Admin Pages & Features

### 2.1 Admin System & Financial Reconciliation Portal (`src/pages/AdminDashboard.tsx`)

- **Purpose**: Provides compliance officers and platform administrators with financial control tools.
- **Key Components & Features**:
  1. **Automated Reconciliation Runner**:
     - "Run Automated Reconciliation Batch" button.
     - Triggers `POST /api/v1/admin/reconciliation/run` to evaluate database integrity across services.
     - Displays batch results banner (Evaluated transaction count & detected mismatch count).
  2. **Financial Mismatch Resolution Desk**:
     - Lists open reconciliation issues (`UNBALANCED_LEDGER`, `STUCK_TRANSACTION`, `SAGA_INCOMPLETE`).
     - Details affected transaction ID, sender/receiver IDs, discrepancy amount, and error description.
     - **Resolution Modal**: Allows admin to input resolution notes and trigger `PATCH /api/v1/admin/reconciliation/issues/{id}/resolve`.
  3. **System Audit Trail Explorer**:
     - Searchable audit log table powered by `/api/v1/admin/audit-logs`.
     - Displays action type (`TRANSFER_INITIATED`, `RECONCILIATION_RUN`, `ISSUE_RESOLVED`), actor ID, client IP address, and creation timestamp.

---

## ⚙️ 3. Authentication & API Integration Layer

### 3.1 Demo User Switcher (`src/context/AuthContext.tsx`)

The application includes pre-configured demo users to test multi-user transfers and admin features seamlessly:

| User ID                                | Full Name         | Account Number     | Role    | Initial Balance |
| :------------------------------------- | :---------------- | :----------------- | :------ | :-------------- |
| `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11` | **Alice Vance**   | `ACC-1001-ALICE`   | `USER`  | ₹1,00,000.00    |
| `b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22` | **Bob Smith**     | `ACC-1002-BOB`     | `USER`  | ₹20,000.00      |
| `c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33` | **Charlie Brown** | `ACC-1003-CHARLIE` | `USER`  | ₹5,000.00       |
| `d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44` | **System Admin**  | `ACC-9999-ADMIN`   | `ADMIN` | ₹5,00,000.00    |

Selecting a user from the Navbar dropdown instantly switches `currentUser`, active account ID, and stored session state.

### 3.2 Axios Centralized Client (`src/services/api.ts`)

All outgoing HTTP requests use an Axios instance configured with request interceptors:

```typescript
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers["X-Correlation-ID"] =
    `corr-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
  return config;
});
```

---

## 🚀 4. How to Build & Run locally

### Prerequisites

- **Node.js**: `v18.0.0` or higher
- **npm**: `v9.0.0` or higher

### Step 1: Install Dependencies

Open terminal in the `frontend` directory and run:

```bash
cd frontend
npm install
```

### Step 2: Start Development Server

To launch the Vite development server with hot-module replacement (HMR):

```bash
npm run dev
```

The application will be accessible at **`http://localhost:3000/`**.

### Step 3: Run Type-Checking & Production Build

To run TypeScript compiler verification and build static bundle:

```bash
npm run build
```

Build output files will be generated in `frontend/dist/`.

### Step 4: Preview Production Build Locally

To preview the optimized production build:

```bash
npm run preview
```
