import { useCallback, useEffect, useState } from 'react';
import { extractErrorMessage } from '../api/httpClient';
import { loansApi } from '../api/loansApi';
import type { Loan } from '../api/types';
import { EmptyState, ErrorState, LoadingState } from '../components/StateViews';

export function MyLoansPage() {
  const [loans, setLoans] = useState<Loan[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingLoanId, setPendingLoanId] = useState<number | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const loadLoans = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setLoans(await loansApi.mine());
    } catch (err) {
      setError(extractErrorMessage(err, 'No se pudieron cargar tus prestamos'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLoans();
  }, [loadLoans]);

  async function handleReturn(loan: Loan) {
    setPendingLoanId(loan.id);
    setActionError(null);
    try {
      await loansApi.returnLoan(loan.id);
      await loadLoans();
    } catch (err) {
      setActionError(extractErrorMessage(err, 'No se pudo registrar la devolucion'));
    } finally {
      setPendingLoanId(null);
    }
  }

  return (
    <div className="page">
      <h1>Mis préstamos</h1>

      {actionError && <ErrorState message={actionError} />}
      {loading && <LoadingState label="Cargando tus préstamos..." />}
      {!loading && error && <ErrorState message={error} />}
      {!loading && !error && loans.length === 0 && <EmptyState label="Todavía no has pedido ningún libro prestado." />}

      {!loading && !error && loans.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Libro</th>
              <th>Fecha de préstamo</th>
              <th>Fecha límite</th>
              <th>Devuelto</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {loans.map((loan) => (
              <tr key={loan.id} className={loan.overdue ? 'row-overdue' : undefined}>
                <td>{loan.bookTitle}</td>
                <td>{loan.loanDate}</td>
                <td>
                  {loan.dueDate}
                  {loan.overdue && <span className="badge badge-overdue">Vencido</span>}
                </td>
                <td>{loan.returnDate ?? '—'}</td>
                <td>
                  {!loan.returnDate && (
                    <button
                      className="btn btn-primary btn-sm"
                      disabled={pendingLoanId === loan.id}
                      onClick={() => handleReturn(loan)}
                    >
                      Devolver
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
