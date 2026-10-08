import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { UserDashboard } from './pages/UserDashboard';
import { TransactionDetail } from './pages/TransactionDetail';
import { LedgerView } from './pages/LedgerView';
import { NotificationsView } from './pages/NotificationsView';
import { AdminDashboard } from './pages/AdminDashboard';

export function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-slate-950 text-slate-100 font-sans selection:bg-cyan-500 selection:text-slate-950">
          <Navbar />
          <main>
            <Routes>
              <Route path="/" element={<UserDashboard />} />
              <Route path="/transactions/:id" element={<TransactionDetail />} />
              <Route path="/ledger" element={<LedgerView />} />
              <Route path="/notifications" element={<NotificationsView />} />
              <Route path="/admin" element={<AdminDashboard />} />
            </Routes>
          </main>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
