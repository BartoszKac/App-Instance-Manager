import { apiFetch } from '@/shared/http';
import type { FilePage, InspectionDetails, InspectionStatus } from '../model/Inspection';

const BASE = '/api/inspections/servers';

export const fetchInspectionStatuses = () => apiFetch<InspectionStatus[]>(BASE);

export const startInspection = (serverId: number) =>
  apiFetch<InspectionStatus>(`${BASE}/${serverId}`, { method: 'POST' });

export const fetchInspectionDetails = (serverId: number) => apiFetch<InspectionDetails>(`${BASE}/${serverId}`);

export const fetchChildren = (serverId: number, parent: string, page: number) => {
  const query = new URLSearchParams({ parent, page: String(page) });
  return apiFetch<FilePage>(`${BASE}/${serverId}/files?${query}`);
};

export const searchFiles = (serverId: number, text: string) => {
  const query = new URLSearchParams({ q: text });
  return apiFetch<FilePage>(`${BASE}/${serverId}/files/search?${query}`);
};

export const updateCredentials = (serverId: number, credentials: { user: string; password: string }) =>
  apiFetch(`/api/deploy/server-credentials/${serverId}`, { method: 'PUT', body: JSON.stringify(credentials) });

export const forgetHostKey =(serverId: number) => apiFetch(`${BASE}/${serverId}/host-key`, { method: 'DELETE' });
