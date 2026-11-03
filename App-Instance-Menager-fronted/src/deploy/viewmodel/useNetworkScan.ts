import { useCallback, useMemo, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { useLanScan } from '@/shared/network/useLanScan';
import type { DeviceDetails, DiscoveredDevice, NetworkScanResponse } from '@/shared/network/model';
import { registerServers } from '../api/serversApi';
import { EMPTY_SCAN_CREDENTIALS } from '../model/BulkServer';
import type { NewBulkServer, ScanCredentials } from '../model/BulkServer';

export interface NetworkScanViewModel {
  isOpen: boolean;
  isLoadingSubnets: boolean;
  isScanning: boolean;
  isAdding: boolean;
  error: string | null;
  subnets: string[];
  cidr: string;
  result: NetworkScanResponse | null;
  details: Record<string, DeviceDetails>;
  pendingDetails: Set<string>;
  picked: Set<string>;
  knownIps: Set<string>;
  credentials: ScanCredentials;
  canAdd: boolean;
  open: () => void;
  close: () => void;
  setCidr: (cidr: string) => void;
  setCredentials: (credentials: ScanCredentials) => void;
  scan: () => Promise<void>;
  togglePicked: (ip: string) => void;
  addPicked: () => Promise<void>;
}

const defaultPicks = (devices: DiscoveredDevice[], knownIps: Set<string>) =>
  new Set(devices.filter((d) => d.sshOpen && !knownIps.has(d.ip)).map((d) => d.ip));

const toServer = (device: DiscoveredDevice, details: DeviceDetails | undefined, credentials: ScanCredentials): NewBulkServer => ({
  name: details?.hostname ?? device.hostname ?? device.ip,
  ip: device.ip,
  user: credentials.user.trim(),
  pass: credentials.pass,
  operationSystem: details?.osFamily ?? 'LINUX',
});

const toggled = (set: Set<string>, value: string) => {
  const next = new Set(set);
  if (next.has(value)) next.delete(value); else next.add(value);
  return next;
};

export const useNetworkScan = (knownServerIps: string[], onAdded: () => Promise<void>): NetworkScanViewModel => {
  const lan = useLanScan();
  const { reset, loadSubnets, result, details, pendingDetails, setError } = lan;
  const runScan = lan.scan;
  const [isOpen, setIsOpen] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [picked, setPicked] = useState<Set<string>>(new Set());
  const [credentials, setCredentials] = useState(EMPTY_SCAN_CREDENTIALS);
  const knownIps = useMemo(() => new Set(knownServerIps), [knownServerIps]);

  const open = useCallback(() => {
    reset();
    setIsOpen(true);
    void loadSubnets();
  }, [reset, loadSubnets]);

  const close = useCallback(() => {
    reset();
    setPicked(new Set());
    setCredentials(EMPTY_SCAN_CREDENTIALS);
    setIsOpen(false);
  }, [reset]);

  const scan = useCallback(async () => {
    setPicked(new Set());
    const response = await runScan();
    if (response) setPicked(defaultPicks(response.devices, knownIps));
  }, [runScan, knownIps]);

  const addPicked = useCallback(async () => {
    const devices = (result?.devices ?? []).filter((d) => picked.has(d.ip));
    setIsAdding(true);
    setError(null);
    try {
      await registerServers(devices.map((d) => toServer(d, details[d.ip], credentials)));
      await onAdded();
      close();
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się dodać serwerów.'));
    } finally {
      setIsAdding(false);
    }
  }, [result, picked, details, credentials, onAdded, close, setError]);

  const togglePicked = useCallback((ip: string) => setPicked((prev) => toggled(prev, ip)), []);
  const waitingForPicked = [...picked].some((ip) => pendingDetails.has(ip));
  const canAdd = !isAdding && !waitingForPicked && picked.size > 0 && credentials.user.trim() !== '' && credentials.pass !== '';

  return {
    isOpen, isAdding, picked, knownIps, credentials, canAdd, open, close, setCredentials, scan, togglePicked, addPicked,
    result, details, pendingDetails,
    isLoadingSubnets: lan.isLoadingSubnets, isScanning: lan.isScanning, error: lan.error,
    subnets: lan.subnets, cidr: lan.cidr, setCidr: lan.setCidr,
  };
};
