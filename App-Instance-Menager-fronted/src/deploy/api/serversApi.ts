import { apiFetch } from '@/shared/http';
import type { NewBulkServer } from '../model/BulkServer';
import type { ServerInventory } from '../model/ServerInventory';

export const registerServers = (servers: NewBulkServer[]) =>
  apiFetch('/api/deploy/server/bulk', {
    method: 'POST',
    body: JSON.stringify(servers),
  });

export const fetchServerInventory = (id: number) => apiFetch<ServerInventory>(`/api/deploy/server-inventory/${id}`);
