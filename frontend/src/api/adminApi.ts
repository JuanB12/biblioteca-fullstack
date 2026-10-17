import { httpClient } from './httpClient';
import type { AdminStats, BlockedUser } from './types';

export const adminApi = {
  stats: () => httpClient.get<AdminStats>('/admin/stats').then((r) => r.data),

  blockedUsers: () => httpClient.get<BlockedUser[]>('/admin/users/blocked').then((r) => r.data),

  unblockUser: (id: number) => httpClient.put(`/admin/users/${id}/unblock`),
};
