import { useState, type FormEvent } from 'react';
import { booksApi } from '../api/booksApi';
import { extractErrorMessage } from '../api/httpClient';
import type { Book } from '../api/types';

export function BookForm({ onCreated }: { onCreated: (book: Book) => void }) {
  const [isbn, setIsbn] = useState('');
  const [title, setTitle] = useState('');
  const [author, setAuthor] = useState('');
  const [publicationYear, setPublicationYear] = useState('');
  const [coverUrl, setCoverUrl] = useState('');
  const [subjects, setSubjects] = useState('');

  const [lookupLoading, setLookupLoading] = useState(false);
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  async function handleAutocomplete() {
    if (!isbn.trim()) {
      setLookupError('Ingresa un ISBN primero');
      return;
    }
    setLookupError(null);
    setLookupLoading(true);
    try {
      const data = await booksApi.lookup(isbn.trim());
      if (data.title) setTitle(data.title);
      if (data.author) setAuthor(data.author);
      if (data.publicationYear) setPublicationYear(String(data.publicationYear));
      if (data.coverUrl) setCoverUrl(data.coverUrl);
      if (data.subjects) setSubjects(data.subjects);
    } catch (err) {
      setLookupError(
        extractErrorMessage(err, 'No se pudo autocompletar desde Open Library. Completa los datos a mano.'),
      );
    } finally {
      setLookupLoading(false);
    }
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitError(null);
    setSubmitLoading(true);
    try {
      const book = await booksApi.create({
        title,
        author,
        isbn,
        publicationYear: publicationYear ? Number(publicationYear) : null,
        coverUrl: coverUrl || null,
        subjects: subjects || null,
      });
      onCreated(book);
      setIsbn('');
      setTitle('');
      setAuthor('');
      setPublicationYear('');
      setCoverUrl('');
      setSubjects('');
    } catch (err) {
      setSubmitError(extractErrorMessage(err, 'No se pudo registrar el libro'));
    } finally {
      setSubmitLoading(false);
    }
  }

  return (
    <form className="book-form" onSubmit={handleSubmit}>
      <h3>Agregar libro</h3>

      <div className="field-row">
        <label className="field">
          ISBN
          <input value={isbn} onChange={(e) => setIsbn(e.target.value)} required />
        </label>
        <button type="button" className="btn btn-secondary" onClick={handleAutocomplete} disabled={lookupLoading}>
          {lookupLoading ? 'Buscando...' : 'Autocompletar desde ISBN'}
        </button>
      </div>
      {lookupError && <div className="state state-error small">⚠ {lookupError}</div>}

      <label className="field">
        Titulo
        <input value={title} onChange={(e) => setTitle(e.target.value)} required />
      </label>

      <label className="field">
        Autor
        <input value={author} onChange={(e) => setAuthor(e.target.value)} required />
      </label>

      <label className="field">
        Año de publicacion
        <input
          type="number"
          value={publicationYear}
          onChange={(e) => setPublicationYear(e.target.value)}
        />
      </label>

      {coverUrl && (
        <div className="cover-preview">
          <img src={coverUrl} alt={`Portada de ${title}`} />
        </div>
      )}

      {submitError && <div className="state state-error">⚠ {submitError}</div>}

      <button type="submit" className="btn btn-primary" disabled={submitLoading}>
        {submitLoading ? 'Guardando...' : 'Registrar libro'}
      </button>
    </form>
  );
}
