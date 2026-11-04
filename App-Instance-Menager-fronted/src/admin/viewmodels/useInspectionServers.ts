import { useCallback, useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchInspectionStatuses, forgetHostKey, startInspection } from '../api/inspectionApi';
import type { InspectionStatus } from '../model/Inspection';

const POLL_INTERVAL_MS = 3000;

export const useInspectionServers = () => {
  const [servers, setServers] = useState<InspectionStatus[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    try {
      setServers(await fetchInspectionStatuses());
      setError(null);
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się pobrać serwerów. Sprawdź, czy backend działa.'));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const first = setTimeout(refresh, 0);
    const timer = setInterval(refresh, POLL_INTERVAL_MS);
    return () => {
      clearTimeout(first);
      clearInterval(timer);
    };
  }, [refresh]);

  const inspect = useCallback(async (serverId: number) => {
    try {
      await startInspection(serverId);
      await refresh();
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się uruchomić badania.'));
    }
  }, [refresh]);

  const trustNewKey = useCallback(async (serverId: number) => {
    try {
      await forgetHostKey(serverId);
      await inspect(serverId);
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się usunąć zapisanego klucza.'));
    }
  }, [inspect]);

  const effectiveId = selectedId ?? servers[0]?.serverId ?? null;
  const selected = servers.find((server) => server.serverId === effectiveId) ?? null;

  return { servers, selected, setSelectedId, isLoading, error, refresh, inspect, trustNewKey };
};
