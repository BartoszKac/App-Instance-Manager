import { apiFetch } from '@/shared/http';
import type { FilePage, InspectionStatus } from './model';

const BASE = '/api/inspections/servers';

export const fetchInspectionStatuses = () => apiFetch<InspectionStatus[]>(BASE);

export const fetchChildren = (serverId: number, parent: string, page: number) => {
  const query = new URLSearchParams({ parent, page: String(page) });
  return apiFetch<FilePage>(`${BASE}/${serverId}/files?${query}`);
};
