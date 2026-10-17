import { httpClient } from './httpClient';
import type { Book, BookCreateRequest, BookStatus, ExternalBookData, Page } from './types';

export const booksApi = {
  search: (params: { title?: string; author?: string; status?: BookStatus | '' }) =>
    httpClient
      .get<Page<Book>>('/books', { params: { ...params, size: 50 } })
      .then((r) => r.data),

  lookup: (isbn: string) =>
    httpClient.get<ExternalBookData>(`/books/lookup/${encodeURIComponent(isbn)}`).then((r) => r.data),

  create: (data: BookCreateRequest) => httpClient.post<Book>('/books', data).then((r) => r.data),

  remove: (id: number) => httpClient.delete(`/books/${id}`),
};
