export type OsFamily = 'LINUX' | 'WINDOWS' | 'MAC';
export type ScanStage = 'SWEEP' | 'PORTS' | 'NAMES' | 'DETAILS';
export type ScanState = 'RUNNING' | 'DONE' | 'FAILED';

export interface DiscoveredDevice {
  ip: string;
  hostname: string | null;
  macAddress: string | null;
  vendor: string | null;
  sshOpen: boolean;
  openPorts: number[];
  deviceType: string | null;
}

export interface ScriptResult {
  id: string;
  output: string;
}

export interface DiscoveredService {
  port: number;
  name: string | null;
  product: string | null;
  version: string | null;
  extraInfo: string | null;
  title: string | null;
  tlsSubject: string | null;
  cpe: string[];
  scripts: ScriptResult[];
}

export interface DeviceDetails {
  ip: string;
  hostname: string | null;
  macAddress: string | null;
  osName: string | null;
  osFamily: OsFamily | null;
  deviceType: string | null;
  services: DiscoveredService[];
  hostScripts: ScriptResult[];
}

export interface ScanJob {
  id: string;
  cidr: string;
  state: ScanState;
  stage: ScanStage;
  percent: number;
  addressesTotal: number;
  addressesScanned: number;
  portsScanned: number;
  detailsTotal: number;
  detailsDone: number;
  deviceCount: number;
  devices: DiscoveredDevice[];
  details: Record<string, DeviceDetails>;
  error: string | null;
}

export const STAGE_LABEL: Record<ScanStage, string> = {
  SWEEP: 'Szukanie urządzeń',
  PORTS: 'Sprawdzanie portów',
  NAMES: 'Pobieranie nazw',
  DETAILS: 'Rozpoznawanie usług i systemów',
};

export const PORT_LABEL: Record<number, string> = {
  22: 'SSH',
  53: 'DNS',
  80: 'HTTP',
  135: 'RPC',
  139: 'NetBIOS',
  443: 'HTTPS',
  445: 'SMB',
  554: 'RTSP',
  631: 'IPP',
  1883: 'MQTT',
  3306: 'MySQL',
  3389: 'RDP',
  5432: 'PostgreSQL',
  5900: 'VNC',
  8080: 'HTTP 8080',
  8443: 'HTTPS 8443',
  9100: 'Druk 9100',
  62078: 'iOS',
};
