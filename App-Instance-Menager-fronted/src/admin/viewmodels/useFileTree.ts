import { useCallback, useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchChildren } from '../api/inspectionApi';
import type { InspectedFile } from '../model/Inspection';

export interface FolderContent {
  entries: InspectedFile[];
  total: number;
  page: number;
  isLoading: boolean;
}

type Folders = Record<string, FolderContent>;

const ROOT = '';

const merge = (folder: FolderContent | undefined, entries: InspectedFile[], total: number, page: number): FolderContent => ({
  entries: [...(folder?.entries ?? []), ...entries],
  total,
  page,
  isLoading: false,
});

export const useFileTree = (serverId: number | null, dataFrom: string | null) => {
  const [folders, setFolders] = useState<Folders>({});
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(new Set());
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (id: number, path: string, page: number) => {
    setFolders((prev) => ({ ...prev, [path]: { ...(prev[path] ?? { entries: [], total: 0, page: 0 }), isLoading: true } }));
    try {
      const found = await fetchChildren(id, path, page);
      setFolders((prev) => ({ ...prev, [path]: merge(prev[path], found.entries, found.total, page) }));
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się pobrać zawartości katalogu.'));
      setFolders((prev) => ({ ...prev, [path]: { ...prev[path], isLoading: false } }));
    }
  }, []);

  useEffect(() => {
    if (serverId === null || dataFrom === null) return;
    const start = setTimeout(() => {
      setFolders({});
      setExpanded(new Set());
      setError(null);
      void load(serverId, ROOT, 0);
    }, 0);
    return () => clearTimeout(start);
  }, [serverId, dataFrom, load]);

  const toggle = useCallback((path: string) => {
    if (serverId === null) return;
    const isOpen = expanded.has(path);
    if (!isOpen && !folders[path]) void load(serverId, path, 0);
    setExpanded((prev) => {
      const next = new Set(prev);
      if (isOpen) next.delete(path);
      else next.add(path);
      return next;
    });
  }, [serverId, expanded, folders, load]);

  const loadMore = useCallback((path: string) => {
    const folder = folders[path];
    if (serverId !== null && folder) void load(serverId, path, folder.page + 1);
  }, [serverId, folders, load]);

  return { folders, expanded, rootPath: ROOT, error, toggle, loadMore };
};
