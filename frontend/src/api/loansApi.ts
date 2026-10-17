import { httpClient } from './httpClient';
import type { Loan } from './types';

export const loansApi = {
  create: (bookId: number) => httpClient.post<Loan>('/loans', { bookId }).then((r) => r.data),

  mine: () => httpClient.get<Loan[]>('/loans/mine').then((r) => r.data),

  returnLoan: (id: number) => httpClient.put<Loan>(`/loans/${id}/return`).then((r) => r.data),
};
