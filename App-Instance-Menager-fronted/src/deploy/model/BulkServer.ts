import type { OsFamily } from '@/shared/network/model';

export interface ScanCredentials {
  user: string;
  pass: string;
}

export interface NewBulkServer {
  name: string;
  ip: string;
  user: string;
  pass: string;
  operationSystem: OsFamily;
}

export const EMPTY_SCAN_CREDENTIALS: ScanCredentials = { user: 'deploy', pass: '' };
