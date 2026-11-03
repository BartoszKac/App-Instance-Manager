import { useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchInspectionDetails } from '../api/inspectionApi';
import type { InspectionDetails, InspectionStatus } from '../model/Inspection';

export const useInspectionDetails = (server: InspectionStatus | null) => {
  const [details, setDetails] = useState<InspectionDetails | null>(null);
  const [error, setError] = useState<string | null>(null);
  const serverId = server?.serverId ?? null;
  const dataFrom = server?.dataFrom ?? null;

  useEffect(() => {
    if (serverId === null || dataFrom === null) return;
    let cancelled = false;
    fetchInspectionDetails(serverId)
      .then((found) => {
        if (cancelled) return;
        setDetails(found);
        setError(null);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(errorMessage(err, 'Nie udało się pobrać wyników badania.'));
      });
    return () => { cancelled = true; };
  }, [serverId, dataFrom]);

  const isCurrent = details?.status.serverId === serverId && dataFrom !== null;
  return { details: isCurrent ? details : null, error };
};
