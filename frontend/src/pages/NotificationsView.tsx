import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { notificationApi } from '../services/api';
import { NotificationItem } from '../types';
import { Bell, Check, RefreshCw, Info, CheckCircle2, AlertTriangle } from 'lucide-react';

export const NotificationsView: React.FC = () => {
  const { currentUser } = useAuth();
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const data = await notificationApi.getNotifications(currentUser.accountId);
      setNotifications(data);
    } catch (err) {
      console.warn('Backend notification API fallback', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, [currentUser.accountId]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await notificationApi.markAsRead(id);
      fetchNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      <div className="flex justify-between items-center border-b border-slate-800 pb-4">
        <div>
          <h1 className="text-2xl font-extrabold text-white flex items-center gap-2">
            <Bell className="w-6 h-6 text-cyan-400" />
            Kafka Transaction Alert Notifications
          </h1>
          <p className="text-xs text-slate-400">
            Real-time event feed consumed asynchronously from Kafka topic transaction.events
          </p>
        </div>

        <button
          onClick={fetchNotifications}
          className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      <div className="space-y-3">
        {notifications.length === 0 ? (
          <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-2xl text-slate-500">
            No notification alerts in inbox.
          </div>
        ) : (
          notifications.map((item) => (
            <div
              key={item.id}
              className={`p-4 rounded-xl border transition-all flex items-start justify-between gap-4 ${
                item.readAt
                  ? 'bg-slate-950/40 border-slate-800/80 text-slate-400 opacity-75'
                  : 'bg-slate-900 border-slate-700/80 text-slate-100 shadow-lg'
              }`}
            >
              <div className="flex items-start gap-3">
                {item.type === 'TRANSACTION_COMPLETED' ? (
                  <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0 mt-0.5" />
                ) : item.type === 'TRANSACTION_COMPENSATED' ? (
                  <AlertTriangle className="w-5 h-5 text-purple-400 shrink-0 mt-0.5" />
                ) : (
                  <Info className="w-5 h-5 text-cyan-400 shrink-0 mt-0.5" />
                )}

                <div>
                  <h4 className="text-sm font-bold text-white">{item.title}</h4>
                  <p className="text-xs text-slate-300 mt-0.5">{item.message}</p>
                  <span className="text-[10px] text-slate-500 font-mono block mt-2">
                    {new Date(item.createdAt).toLocaleString()}
                  </span>
                </div>
              </div>

              {!item.readAt && (
                <button
                  onClick={() => handleMarkAsRead(item.id)}
                  className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 rounded-lg flex items-center gap-1 shrink-0"
                >
                  <Check className="w-3.5 h-3.5 text-cyan-400" />
                  Mark Read
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};
