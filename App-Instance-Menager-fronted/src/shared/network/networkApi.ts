import { apiFetch } from '@/shared/http';
import type { DeviceDetails, ScanJob } from './model';

export const fetchSubnets = () => apiFetch<string[]>('/api/deploy/network/subnets');

export const startScan = (cidr: string) =>
  apiFetch<ScanJob>('/api/deploy/network/scan', {
    method: 'POST',
    body: JSON.stringify({ cidr }),
  });

export const fetchCurrentScan = async () => (await apiFetch<ScanJob | undefined>('/api/deploy/network/scan/current')) ?? null;

export const fetchDeviceDetails = (ip: string) =>
  apiFetch<DeviceDetails>('/api/deploy/network/device-details', {
    method: 'POST',
    body: JSON.stringify({ ip }),
  });
