import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Role } from '../api/types';

interface AuthUser {
  token: string;
  name: string;
  email: string;
  role: Role;
}

interface AuthState {
  token: string | null;
  name: string | null;
  email: string | null;
  role: Role | null;
  isAuthenticated: boolean;
  login: (user: AuthUser) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      name: null,
      email: null,
      role: null,
      isAuthenticated: false,
      login: (user) =>
        set({
          token: user.token,
          name: user.name,
          email: user.email,
          role: user.role,
          isAuthenticated: true,
        }),
      logout: () =>
        set({ token: null, name: null, email: null, role: null, isAuthenticated: false }),
    }),
    { name: 'biblioteca-auth' },
  ),
);
