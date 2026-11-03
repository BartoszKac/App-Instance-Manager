export type OsFamily = 'LINUX' | 'WINDOWS' | 'MAC';

export interface DiscoveredDevice {
  ip: string;
  hostname: string | null;
  macAddress: string | null;
  vendor: string | null;
  sshOpen: boolean;
  openPorts: number[];
}

export interface NetworkScanResponse {
  cidr: string;
  deviceCount: number;
  devices: DiscoveredDevice[];
}

export interface DiscoveredService {
  port: number;
  name: string | null;
  product: string | null;
  version: string | null;
  extraInfo: string | null;
  title: string | null;
  tlsSubject: string | null;
}

export interface DeviceDetails {
  ip: string;
  hostname: string | null;
  osName: string | null;
  osFamily: OsFamily | null;
  deviceType: string | null;
  services: DiscoveredService[];
}

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
  3389: 'RDP',
  5900: 'VNC',
  8080: 'HTTP 8080',
  8443: 'HTTPS 8443',
  9100: 'Druk 9100',
  62078: 'iOS',
};
