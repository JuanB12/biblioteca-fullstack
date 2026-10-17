import { useCallback, useEffect, useState } from 'react';
import { booksApi } from '../api/booksApi';
import { extractErrorMessage } from '../api/httpClient';
import { loansApi } from '../api/loansApi';
import { reservationsApi } from '../api/reservationsApi';
import type { Book, BookStatus } from '../api/types';
import { BookForm } from '../components/BookForm';
import { EmptyState, ErrorState, LoadingState } from '../components/StateViews';
import { useAuthStore } from '../store/authStore';

const STATUS_LABEL: Record<BookStatus, string> = {
  DISPONIBLE: 'Disponible',
  PRESTADO: 'Prestado',
  RESERVADO: 'Reservado',
};

export function CatalogPage() {
  const role = useAuthStore((s) => s.role);

  const [books, setBooks] = useState<Book[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [titleFilter, setTitleFilter] = useState('');
  const [authorFilter, setAuthorFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState<BookStatus | ''>('');

  const [pendingBookId, setPendingBookId] = useState<number | null>(null);
  const [actionMessage, setActionMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const loadBooks = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await booksApi.search({
        title: titleFilter || undefined,
        author: authorFilter || undefined,
        status: statusFilter || undefined,
      });
      setBooks(page.content);
    } catch (err) {
      setError(extractErrorMessage(err, 'No se pudo cargar el catalogo'));
    } finally {
      setLoading(false);
    }
  }, [titleFilter, authorFilter, statusFilter]);

  useEffect(() => {
    loadBooks();
  }, [loadBooks]);

  async function handleBorrow(book: Book) {
    setPendingBookId(book.id);
    setActionMessage(null);
    try {
      await loansApi.create(book.id);
      setActionMessage({ type: 'success', text: `Pediste prestado "${book.title}".` });
      await loadBooks();
    } catch (err) {
      setActionMessage({ type: 'error', text: extractErrorMessage(err, 'No se pudo registrar el prestamo') });
    } finally {
      setPendingBookId(null);
    }
  }

  async function handleReserve(book: Book) {
    setPendingBookId(book.id);
    setActionMessage(null);
    try {
      await reservationsApi.create(book.id);
      setActionMessage({ type: 'success', text: `Quedaste en la fila de espera por "${book.title}".` });
    } catch (err) {
      setActionMessage({ type: 'error', text: extractErrorMessage(err, 'No se pudo crear la reserva') });
    } finally {
      setPendingBookId(null);
    }
  }

  async function handleDelete(book: Book) {
    setPendingBookId(book.id);
    setActionMessage(null);
    try {
      await booksApi.remove(book.id);
      setActionMessage({ type: 'success', text: `Eliminaste "${book.title}".` });
      await loadBooks();
    } catch (err) {
      setActionMessage({ type: 'error', text: extractErrorMessage(err, 'No se pudo eliminar el libro') });
    } finally {
      setPendingBookId(null);
    }
  }

  return (
    <div className="page">
      <h1>Catálogo</h1>

      <div className="filters">
        <input
          placeholder="Buscar por titulo"
          value={titleFilter}
          onChange={(e) => setTitleFilter(e.target.value)}
        />
        <input
          placeholder="Buscar por autor"
          value={authorFilter}
          onChange={(e) => setAuthorFilter(e.target.value)}
        />
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as BookStatus | '')}>
          <option value="">Todos los estados</option>
          <option value="DISPONIBLE">Disponible</option>
          <option value="PRESTADO">Prestado</option>
          <option value="RESERVADO">Reservado</option>
        </select>
      </div>

      {actionMessage && (
        <div className={`state ${actionMessage.type === 'success' ? 'state-success' : 'state-error'}`}>
          {actionMessage.type === 'success' ? '✓' : '⚠'} {actionMessage.text}
        </div>
      )}

      {loading && <LoadingState label="Cargando catálogo..." />}
      {!loading && error && <ErrorState message={error} />}
      {!loading && !error && books.length === 0 && <EmptyState label="No se encontraron libros." />}

      {!loading && !error && books.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>Título</th>
              <th>Autor</th>
              <th>ISBN</th>
              <th>Año</th>
              <th>Estado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {books.map((book) => (
              <tr key={book.id}>
                <td>{book.title}</td>
                <td>{book.author}</td>
                <td>{book.isbn}</td>
                <td>{book.publicationYear ?? '—'}</td>
                <td>
                  <span className={`badge badge-${book.status.toLowerCase()}`}>{STATUS_LABEL[book.status]}</span>
                </td>
                <td className="actions">
                  {book.status === 'DISPONIBLE' && (
                    <button
                      className="btn btn-primary btn-sm"
                      disabled={pendingBookId === book.id}
                      onClick={() => handleBorrow(book)}
                    >
                      Pedir prestado
                    </button>
                  )}
                  {book.status !== 'DISPONIBLE' && (
                    <button
                      className="btn btn-secondary btn-sm"
                      disabled={pendingBookId === book.id}
                      onClick={() => handleReserve(book)}
                    >
                      Reservar
                    </button>
                  )}
                  {role === 'ADMIN' && book.status === 'DISPONIBLE' && (
                    <button
                      className="btn btn-danger btn-sm"
                      disabled={pendingBookId === book.id}
                      onClick={() => handleDelete(book)}
                    >
                      Eliminar
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {role === 'ADMIN' && (
        <BookForm
          onCreated={(book) => {
            setActionMessage({ type: 'success', text: `Registraste "${book.title}".` });
            loadBooks();
          }}
        />
      )}
    </div>
  );
}
