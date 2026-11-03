export interface DiscoveredDevice {
  ip: string;
  hostname: string | null;
  macAddress: string | null;
  vendor: string | null;
  sshOpen: boolean;
}

export interface NetworkScanResponse {
  cidr: string;
  deviceCount: number;
  devices: DiscoveredDevice[];
}

export interface ScanCredentials {
  user: string;
  pass: string;
}

export interface NewBulkServer {
  name: string;
  ip: string;
  user: string;
  pass: string;
  operationSystem: 'LINUX';
}

export const EMPTY_SCAN_CREDENTIALS: ScanCredentials = { user: 'deploy', pass: '' };
