export type Role = 'ADMIN' | 'BIBLIOTECARIO';
export type BookStatus = 'DISPONIBLE' | 'PRESTADO' | 'RESERVADO';
export type ReservationStatus = 'PENDIENTE' | 'NOTIFICADO' | 'CANCELADO' | 'CUMPLIDO';

export interface AuthResponse {
  token: string;
  name: string;
  email: string;
  role: Role;
}

export interface Book {
  id: number;
  title: string;
  author: string;
  isbn: string;
  publicationYear: number | null;
  status: BookStatus;
  coverUrl: string | null;
  subjects: string | null;
}

export interface ExternalBookData {
  title: string | null;
  author: string | null;
  publicationYear: number | null;
  coverUrl: string | null;
  subjects: string | null;
}

export interface BookCreateRequest {
  title: string;
  author: string;
  isbn: string;
  publicationYear: number | null;
  coverUrl: string | null;
  subjects: string | null;
}

export interface Loan {
  id: number;
  bookId: number;
  bookTitle: string;
  borrowerName: string;
  borrowerEmail: string;
  loanDate: string;
  dueDate: string;
  returnDate: string | null;
  overdue: boolean;
}

export interface Reservation {
  id: number;
  bookId: number;
  bookTitle: string;
  requesterEmail: string;
  requestDate: string;
  status: ReservationStatus;
}

export interface AdminStats {
  activeLoans: number;
  overdueLoans: number;
  availableBooks: number;
  borrowedBooks: number;
  reservedBooks: number;
  blockedUsers: number;
}

export interface BlockedUser {
  id: number;
  name: string;
  email: string;
  blockedUntil: string | null;
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
