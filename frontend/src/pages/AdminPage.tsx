import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../api/adminApi';
import { extractErrorMessage } from '../api/httpClient';
import type { AdminStats, BlockedUser } from '../api/types';
import { EmptyState, ErrorState, LoadingState } from '../components/StateViews';

export function AdminPage() {
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [statsLoading, setStatsLoading] = useState(true);
  const [statsError, setStatsError] = useState<string | null>(null);

  const [blockedUsers, setBlockedUsers] = useState<BlockedUser[]>([]);
  const [usersLoading, setUsersLoading] = useState(true);
  const [usersError, setUsersError] = useState<string | null>(null);
  const [pendingUserId, setPendingUserId] = useState<number | null>(null);

  const loadStats = useCallback(async () => {
    setStatsLoading(true);
    setStatsError(null);
    try {
      setStats(await adminApi.stats());
    } catch (err) {
      setStatsError(extractErrorMessage(err, 'No se pudieron cargar las estadísticas'));
    } finally {
      setStatsLoading(false);
    }
  }, []);

  const loadBlockedUsers = useCallback(async () => {
    setUsersLoading(true);
    setUsersError(null);
    try {
      setBlockedUsers(await adminApi.blockedUsers());
    } catch (err) {
      setUsersError(extractErrorMessage(err, 'No se pudieron cargar las cuentas bloqueadas'));
    } finally {
      setUsersLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStats();
    loadBlockedUsers();
  }, [loadStats, loadBlockedUsers]);

  async function handleUnblock(user: BlockedUser) {
    setPendingUserId(user.id);
    try {
      await adminApi.unblockUser(user.id);
      await Promise.all([loadBlockedUsers(), loadStats()]);
    } catch (err) {
      setUsersError(extractErrorMessage(err, 'No se pudo desbloquear la cuenta'));
    } finally {
      setPendingUserId(null);
    }
  }

  return (
    <div className="page">
      <h1>Panel de administración</h1>

      <section>
        <h2>Estadísticas</h2>
        {statsLoading && <LoadingState label="Cargando estadísticas..." />}
        {!statsLoading && statsError && <ErrorState message={statsError} />}
        {!statsLoading && !statsError && stats && (
          <div className="stats-grid">
            <StatCard label="Préstamos activos" value={stats.activeLoans} />
            <StatCard label="Préstamos vencidos" value={stats.overdueLoans} tone="danger" />
            <StatCard label="Libros disponibles" value={stats.availableBooks} />
            <StatCard label="Libros prestados" value={stats.borrowedBooks} />
            <StatCard label="Libros reservados" value={stats.reservedBooks} />
            <StatCard label="Cuentas bloqueadas" value={stats.blockedUsers} tone="danger" />
          </div>
        )}
      </section>

      <section>
        <h2>Cuentas bloqueadas</h2>
        {usersError && <ErrorState message={usersError} />}
        {usersLoading && <LoadingState label="Cargando cuentas bloqueadas..." />}
        {!usersLoading && blockedUsers.length === 0 && <EmptyState label="No hay cuentas bloqueadas ahora mismo." />}
        {!usersLoading && blockedUsers.length > 0 && (
          <table className="table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Correo</th>
                <th>Bloqueado hasta</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {blockedUsers.map((user) => (
                <tr key={user.id}>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>{user.blockedUntil ?? 'Indefinido (bloqueo manual)'}</td>
                  <td>
                    <button
                      className="btn btn-secondary btn-sm"
                      disabled={pendingUserId === user.id}
                      onClick={() => handleUnblock(user)}
                    >
                      Desbloquear
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}

function StatCard({ label, value, tone }: { label: string; value: number; tone?: 'danger' }) {
  return (
    <div className={`stat-card ${tone ? `stat-card-${tone}` : ''}`}>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}
