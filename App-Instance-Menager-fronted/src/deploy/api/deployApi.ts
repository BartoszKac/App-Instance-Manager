import { apiFetch } from '@/shared/http';
import type { BulkTransferItem, DeployServer, TransferResult } from '../model/DeployState';

export const fetchServers = () => apiFetch<DeployServer[]>('/api/deploy/servers');

export const transferBulk = (tasks: BulkTransferItem[]) =>
  apiFetch<TransferResult[]>('/api/deploy/transfer/bulk', { method: 'POST', body: JSON.stringify(tasks) });
