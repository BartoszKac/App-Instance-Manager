import { useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchInspectionStatuses } from '@/shared/inspection/inspectionFilesApi';
import type { InspectionStatus } from '@/shared/inspection/model';

export const useUploadTargets = () => {
  const [servers, setServers] = useState<InspectionStatus[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    fetchInspectionStatuses()
      .then((found) => { if (!cancelled) setServers(found); })
      .catch((err: unknown) => { if (!cancelled) setError(errorMessage(err, 'Nie udało się pobrać serwerów.')); })
      .finally(() => { if (!cancelled) setIsLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const selected = servers.find((server) => server.serverId === selectedId) ?? servers[0] ?? null;

  return { servers, selected, setSelectedId, isLoading, error };
};
