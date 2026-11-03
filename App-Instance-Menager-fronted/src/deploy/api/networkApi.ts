import type { NetworkScanResponse, NewBulkServer } from '../model/NetworkScan';
import { apiFetch } from './deployHttp';

const LAN_HELPER_URL = 'http://localhost:8899';

const helperFetch = async <T>(path: string): Promise<T> => {
  const res = await fetch(`${LAN_HELPER_URL}${path}`);
  const body = (await res.json()) as T & { detail?: string };
  if (!res.ok) throw new Error(body.detail ?? `${res.status} ${res.statusText} — ${path}`);
  return body;
};

export const fetchHelperSubnet = async () => (await helperFetch<{ cidr: string }>('/subnet')).cidr;

export const scanWithHelper = () => helperFetch<NetworkScanResponse>('/scan');

export const fetchSubnets = () => apiFetch<string[]>('/api/deploy/network/subnets');

export const scanSubnet = (cidr: string) =>
  apiFetch<NetworkScanResponse>('/api/deploy/network/scan', {
    method: 'POST',
    body: JSON.stringify({ cidr }),
  });

export const registerServers = (servers: NewBulkServer[]) =>
  apiFetch('/api/deploy/server/bulk', {
    method: 'POST',
    body: JSON.stringify(servers),
  });
