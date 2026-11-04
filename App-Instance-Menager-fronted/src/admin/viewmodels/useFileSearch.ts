import { useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { searchFiles } from '../api/inspectionApi';
import type { FilePage } from '../model/Inspection';

const DEBOUNCE_MS = 350;
const MIN_QUERY_LENGTH = 2;

export const useFileSearch = (serverId: number | null) => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<FilePage | null>(null);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const text = query.trim();
  const isActive = text.length >= MIN_QUERY_LENGTH && serverId !== null;

  useEffect(() => {
    if (!isActive || serverId === null) return;
    let cancelled = false;
    const timer = setTimeout(() => {
      setIsSearching(true);
      searchFiles(serverId, text)
        .then((found) => { if (!cancelled) { setResults(found); setError(null); } })
        .catch((err: unknown) => { if (!cancelled) setError(errorMessage(err, 'Nie udało się wyszukać plików.')); })
        .finally(() => { if (!cancelled) setIsSearching(false); });
    }, DEBOUNCE_MS);
    return () => {
      cancelled = true;
      clearTimeout(timer);
    };
  }, [serverId, text, isActive]);

  return { query, setQuery, isActive, results: isActive ? results : null, isSearching, error };
};
