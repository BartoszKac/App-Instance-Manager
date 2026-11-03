import { apiFetch } from '@/shared/http';
import type { ManagedService } from '../model/ManagedService';

export const fetchServices = () => apiFetch<ManagedService[]>('/api/services');

export const createService = (service: Omit<ManagedService, 'id'>) =>
  apiFetch<ManagedService>('/api/services', {
    method: 'POST',
    body: JSON.stringify(service),
  });

export const deleteService = (id: number) => apiFetch(`/api/services/${id}`, { method: 'DELETE' });
