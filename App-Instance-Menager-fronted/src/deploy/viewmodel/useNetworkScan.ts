import { useCallback, useMemo, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { useLanScan } from '@/shared/network/useLanScan';
import type { DeviceDetails, DiscoveredDevice, ScanJob } from '@/shared/network/model';
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
  job: ScanJob | null;
  isPicked: (device: DiscoveredDevice) => boolean;
  pickedCount: number;
  isPending: (device: DiscoveredDevice) => boolean;
  knownIps: Set<string>;
  credentials: ScanCredentials;
  canAdd: boolean;
  open: () => void;
  close: () => void;
  setCidr: (cidr: string) => void;
  setCredentials: (credentials: ScanCredentials) => void;
  scan: () => Promise<void>;
  togglePicked: (device: DiscoveredDevice) => void;
  addPicked: () => Promise<void>;
}

const toServer = (device: DiscoveredDevice, details: DeviceDetails | undefined, credentials: ScanCredentials): NewBulkServer => ({
  name: device.hostname ?? details?.hostname ?? device.ip,
  ip: device.ip,
  user: credentials.user.trim(),
  pass: credentials.pass,
  operationSystem: details?.osFamily ?? 'LINUX',
});

export const useNetworkScan = (knownServerIps: string[], onAdded: () => Promise<void>): NetworkScanViewModel => {
  const lan = useLanScan();
  const { load, job, setError } = lan;
  const runScan = lan.scan;
  const [isOpen, setIsOpen] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [choices, setChoices] = useState<Record<string, boolean>>({});
  const [credentials, setCredentials] = useState(EMPTY_SCAN_CREDENTIALS);
  const knownIps = useMemo(() => new Set(knownServerIps), [knownServerIps]);

  const isPicked = useCallback((device: DiscoveredDevice) => {
    if (knownIps.has(device.ip)) return false;
    return choices[device.ip] ?? device.sshOpen;
  }, [choices, knownIps]);

  const isPending = useCallback((device: DiscoveredDevice) => {
    return job?.state === 'RUNNING' && (job.stage !== 'DETAILS' || device.openPorts.length > 0);
  }, [job]);

  const picked = useMemo(() => (job?.devices ?? []).filter(isPicked), [job, isPicked]);

  const open = useCallback(() => {
    setIsOpen(true);
    void load();
  }, [load]);

  const close = useCallback(() => {
    setChoices({});
    setCredentials(EMPTY_SCAN_CREDENTIALS);
    setIsOpen(false);
  }, []);

  const scan = useCallback(async () => {
    setChoices({});
    await runScan();
  }, [runScan]);

  const addPicked = useCallback(async () => {
    setIsAdding(true);
    setError(null);
    try {
      await registerServers(picked.map((d) => toServer(d, job?.details[d.ip], credentials)));
      await onAdded();
      close();
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się dodać serwerów.'));
    } finally {
      setIsAdding(false);
    }
  }, [picked, job, credentials, onAdded, close, setError]);

  const togglePicked = useCallback((device: DiscoveredDevice) => {
    setChoices((prev) => ({ ...prev, [device.ip]: !(prev[device.ip] ?? device.sshOpen) }));
  }, []);

  const waiting = job?.state === 'RUNNING' && picked.some((d) => !job.details[d.ip]?.osFamily);
  const canAdd = !isAdding && !waiting && picked.length > 0 && credentials.user.trim() !== '' && credentials.pass !== '';

  return {
    isOpen, isAdding, knownIps, credentials, canAdd, open, close, setCredentials, scan, togglePicked, addPicked,
    job, isPicked, isPending, pickedCount: picked.length,
    isLoadingSubnets: lan.isLoadingSubnets, isScanning: lan.isScanning, error: lan.error,
    subnets: lan.subnets, cidr: lan.cidr, setCidr: lan.setCidr,
  };
};
