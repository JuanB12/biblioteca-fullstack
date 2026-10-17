import { httpClient } from './httpClient';
import type { AuthResponse } from './types';

export const authApi = {
  register: (data: { name: string; email: string; password: string }) =>
    httpClient.post<AuthResponse>('/auth/register', data).then((r) => r.data),

  login: (data: { email: string; password: string }) =>
    httpClient.post<AuthResponse>('/auth/login', data).then((r) => r.data),
};
