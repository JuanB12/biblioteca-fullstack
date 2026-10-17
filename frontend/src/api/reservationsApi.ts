import { httpClient } from './httpClient';
import type { Reservation } from './types';

export const reservationsApi = {
  create: (bookId: number) => httpClient.post<Reservation>('/reservations', { bookId }).then((r) => r.data),

  cancel: (id: number) => httpClient.delete(`/reservations/${id}`),
};
