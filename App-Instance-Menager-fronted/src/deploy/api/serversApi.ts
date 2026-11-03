import { apiFetch } from '@/shared/http';
import type { NewBulkServer } from '../model/BulkServer';

export const registerServers = (servers: NewBulkServer[]) =>
  apiFetch('/api/deploy/server/bulk', {
    method: 'POST',
    body: JSON.stringify(servers),
  });
