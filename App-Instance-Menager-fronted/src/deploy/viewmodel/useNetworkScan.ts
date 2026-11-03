import { useCallback, useMemo, useRef, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchHelperSubnet, fetchSubnets, registerServers, scanSubnet, scanWithHelper } from '../api/networkApi';
import { EMPTY_SCAN_CREDENTIALS } from '../model/NetworkScan';
import type { DiscoveredDevice, NetworkScanResponse, NewBulkServer, ScanCredentials } from '../model/NetworkScan';

export interface NetworkScanViewModel {
  isOpen: boolean;
  isLoadingSubnets: boolean;
  isScanning: boolean;
  isAdding: boolean;
  usesHelper: boolean;
  error: string | null;
  subnets: string[];
  cidr: string;
  result: NetworkScanResponse | null;
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

const toServer = (device: DiscoveredDevice, credentials: ScanCredentials): NewBulkServer => ({
  name: device.hostname ?? device.ip,
  ip: device.ip,
  user: credentials.user.trim(),
  pass: credentials.pass,
  operationSystem: 'LINUX',
});

const toggled = (set: Set<string>, value: string) => {
  const next = new Set(set);
  if (next.has(value)) next.delete(value); else next.add(value);
  return next;
};

const loadAvailableSubnets = async () => {
  try {
    return { subnets: [await fetchHelperSubnet()], usesHelper: true };
  } catch {
    return { subnets: await fetchSubnets(), usesHelper: false };
  }
};

const useScanDialog = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [usesHelper, setUsesHelper] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [subnets, setSubnets] = useState<string[]>([]);
  const [cidr, setCidr] = useState('');
  const [isLoadingSubnets, setIsLoadingSubnets] = useState(false);
  const [result, setResult] = useState<NetworkScanResponse | null>(null);
  const [credentials, setCredentials] = useState(EMPTY_SCAN_CREDENTIALS);
  const session = useRef(0);

  const loadSubnets = useCallback(async (current: number) => {
    setIsLoadingSubnets(true);
    try {
      const found = await loadAvailableSubnets();
      if (session.current !== current) return;
      setSubnets(found.subnets);
      setUsesHelper(found.usesHelper);
      setCidr(found.subnets[0] ?? '');
    } catch (err: unknown) {
      if (session.current === current) setError(errorMessage(err, 'Nie udało się pobrać listy podsieci.'));
    } finally {
      if (session.current === current) setIsLoadingSubnets(false);
    }
  }, []);

  const reset = useCallback(() => {
    session.current += 1;
    setError(null);
    setResult(null);
    setSubnets([]);
    setCidr('');
    setCredentials(EMPTY_SCAN_CREDENTIALS);
    return session.current;
  }, []);

  const open = useCallback(() => {
    setIsOpen(true);
    void loadSubnets(reset());
  }, [loadSubnets, reset]);

  const close = useCallback(() => {
    reset();
    setIsOpen(false);
  }, [reset]);

  return {
    isOpen, open, close, error, setError, subnets, cidr, setCidr, isLoadingSubnets, usesHelper,
    result, setResult, credentials, setCredentials, session,
  };
};

export const useNetworkScan = (knownServerIps: string[], onAdded: () => Promise<void>): NetworkScanViewModel => {
  const dialog = useScanDialog();
  const { cidr, credentials, result, session, setError, setResult, usesHelper } = dialog;
  const closeDialog = dialog.close;
  const [isScanning, setIsScanning] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [picked, setPicked] = useState<Set<string>>(new Set());
  const knownIps = useMemo(() => new Set(knownServerIps), [knownServerIps]);

  const close = useCallback(() => {
    setPicked(new Set());
    closeDialog();
  }, [closeDialog]);

  const scan = useCallback(async () => {
    const current = session.current;
    setIsScanning(true);
    setError(null);
    try {
      const response = await (usesHelper ? scanWithHelper() : scanSubnet(cidr));
      if (session.current !== current) return;
      setResult(response);
      setPicked(defaultPicks(response.devices, knownIps));
    } catch (err: unknown) {
      if (session.current === current) setError(errorMessage(err, 'Skan się nie udał.'));
    } finally {
      setIsScanning(false);
    }
  }, [cidr, knownIps, session, setError, setResult, usesHelper]);

  const addPicked = useCallback(async () => {
    const devices = (result?.devices ?? []).filter((d) => picked.has(d.ip));
    setIsAdding(true);
    setError(null);
    try {
      await registerServers(devices.map((d) => toServer(d, credentials)));
      await onAdded();
      close();
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się dodać serwerów.'));
    } finally {
      setIsAdding(false);
    }
  }, [result, picked, credentials, onAdded, close, setError]);

  const togglePicked = useCallback((ip: string) => setPicked((prev) => toggled(prev, ip)), []);
  const canAdd = !isAdding && picked.size > 0 && credentials.user.trim() !== '' && credentials.pass !== '';

  return {
    isOpen: dialog.isOpen, isLoadingSubnets: dialog.isLoadingSubnets, error: dialog.error, usesHelper,
    subnets: dialog.subnets, cidr, result, credentials,
    open: dialog.open, close, setCidr: dialog.setCidr, setCredentials: dialog.setCredentials,
    isScanning, isAdding, picked, knownIps, canAdd, scan, togglePicked, addPicked,
  };
};
