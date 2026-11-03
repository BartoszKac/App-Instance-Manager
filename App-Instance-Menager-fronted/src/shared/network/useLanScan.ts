import { useCallback, useRef, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchDeviceDetails, fetchSubnets, scanSubnet } from './networkApi';
import type { DeviceDetails, NetworkScanResponse } from './model';

const PARALLEL_DETAIL_REQUESTS = 2;

const removed = (set: Set<string>, value: string) => {
  const next = new Set(set);
  next.delete(value);
  return next;
};

export const useLanScan = () => {
  const [subnets, setSubnets] = useState<string[]>([]);
  const [cidr, setCidr] = useState('');
  const [isLoadingSubnets, setIsLoadingSubnets] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<NetworkScanResponse | null>(null);
  const [details, setDetails] = useState<Record<string, DeviceDetails>>({});
  const [pendingDetails, setPendingDetails] = useState<Set<string>>(new Set());
  const session = useRef(0);

  const reset = useCallback(() => {
    session.current += 1;
    setError(null);
    setResult(null);
    setDetails({});
    setPendingDetails(new Set());
    setIsScanning(false);
  }, []);

  const loadSubnets = useCallback(async () => {
    const current = session.current;
    setIsLoadingSubnets(true);
    try {
      const found = await fetchSubnets();
      if (session.current !== current) return;
      setSubnets(found);
      setCidr(found[0] ?? '');
    } catch (err: unknown) {
      if (session.current === current) setError(errorMessage(err, 'Nie udało się pobrać listy podsieci.'));
    } finally {
      setIsLoadingSubnets(false);
    }
  }, []);

  const loadOneDetail = useCallback(async (ip: string, current: number) => {
    try {
      const found = await fetchDeviceDetails(ip);
      if (session.current === current) setDetails((prev) => ({ ...prev, [ip]: found }));
    } catch (err: unknown) {
      if (session.current === current) setError(errorMessage(err, `Nie udało się doczytać szczegółów ${ip}.`));
    } finally {
      if (session.current === current) setPendingDetails((prev) => removed(prev, ip));
    }
  }, []);

  const loadDetails = useCallback(async (ips: string[], current: number) => {
    const queue = [...ips];
    const worker = async () => {
      for (let ip = queue.shift(); ip && session.current === current; ip = queue.shift()) {
        await loadOneDetail(ip, current);
      }
    };
    await Promise.all(Array.from({ length: PARALLEL_DETAIL_REQUESTS }, worker));
  }, [loadOneDetail]);

  const scan = useCallback(async (): Promise<NetworkScanResponse | null> => {
    reset();
    const current = session.current;
    setIsScanning(true);
    try {
      const response = await scanSubnet(cidr);
      if (session.current !== current) return null;
      setResult(response);
      const withPorts = response.devices.filter((d) => d.openPorts.length > 0).map((d) => d.ip);
      setPendingDetails(new Set(withPorts));
      void loadDetails(withPorts, current);
      return response;
    } catch (err: unknown) {
      if (session.current === current) setError(errorMessage(err, 'Skan się nie udał.'));
      return null;
    } finally {
      if (session.current === current) setIsScanning(false);
    }
  }, [cidr, loadDetails, reset]);

  return {
    subnets, cidr, setCidr, isLoadingSubnets, isScanning, error, setError,
    result, details, pendingDetails, loadSubnets, scan, reset,
  };
};
