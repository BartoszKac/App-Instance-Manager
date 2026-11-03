import { apiFetch } from '@/shared/http';
import type { DeviceDetails, NetworkScanResponse } from './model';

export const fetchSubnets = () => apiFetch<string[]>('/api/deploy/network/subnets');

export const scanSubnet = (cidr: string) =>
  apiFetch<NetworkScanResponse>('/api/deploy/network/scan', {
    method: 'POST',
    body: JSON.stringify({ cidr }),
  });

export const fetchDeviceDetails = (ip: string) =>
  apiFetch<DeviceDetails>('/api/deploy/network/device-details', {
    method: 'POST',
    body: JSON.stringify({ ip }),
  });
