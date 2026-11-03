import { useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchInspectionStatuses } from '@/shared/inspection/inspectionFilesApi';
import type { InspectionStatus } from '@/shared/inspection/model';

export const useUploadTargets = (isActive: boolean) => {
  const [servers, setServers] = useState<InspectionStatus[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!isActive) return;
    let cancelled = false;
    fetchInspectionStatuses()
      .then((found) => {
        if (cancelled) return;
        setServers(found);
        setError(null);
      })
      .catch((err: unknown) => { if (!cancelled) setError(errorMessage(err, 'Nie udało się pobrać serwerów.')); })
      .finally(() => { if (!cancelled) setIsLoading(false); });
    return () => { cancelled = true; };
  }, [isActive]);

  const selected = servers.find((server) => server.serverId === selectedId) ?? servers[0] ?? null;

  return { servers, selected, setSelectedId, isLoading, error };
};
