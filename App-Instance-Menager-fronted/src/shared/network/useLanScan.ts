import { useCallback, useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchCurrentScan, fetchSubnets, startScan } from './networkApi';
import type { ScanJob } from './model';

const POLL_INTERVAL_MS = 1000;

export const useLanScan = () => {
  const [subnets, setSubnets] = useState<string[]>([]);
  const [cidr, setCidr] = useState('');
  const [isLoadingSubnets, setIsLoadingSubnets] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [job, setJob] = useState<ScanJob | null>(null);

  const refresh = useCallback(async () => {
    try {
      setJob(await fetchCurrentScan());
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się odczytać postępu skanu.'));
    }
  }, []);

  useEffect(() => {
    if (job?.state !== 'RUNNING') return;
    const timer = setTimeout(() => { void refresh(); }, POLL_INTERVAL_MS);
    return () => clearTimeout(timer);
  }, [job, refresh]);

  const load = useCallback(async () => {
    setError(null);
    setIsLoadingSubnets(true);
    try {
      const found = await fetchSubnets();
      setSubnets(found);
      setCidr((current) => current || (found[0] ?? ''));
      await refresh();
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się pobrać listy podsieci.'));
    } finally {
      setIsLoadingSubnets(false);
    }
  }, [refresh]);

  const scan = useCallback(async () => {
    setError(null);
    try {
      setJob(await startScan(cidr));
    } catch (err: unknown) {
      setError(errorMessage(err, 'Skan się nie udał.'));
      await refresh();
    }
  }, [cidr, refresh]);

  const isScanning = job?.state === 'RUNNING';
  const failure = job?.state === 'FAILED' ? job.error : null;

  return { subnets, cidr, setCidr, isLoadingSubnets, isScanning, error: error ?? failure, setError, job, load, scan };
};
