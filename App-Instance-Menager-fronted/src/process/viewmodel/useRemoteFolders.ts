import { useCallback, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchChildren } from '@/shared/inspection/inspectionFilesApi';
import type { InspectedFile } from '@/shared/inspection/model';

const ROOT = '';
const PAGE_SIZE = 200;

export interface FolderNode {
  folders: InspectedFile[];
  page: number;
  hasMore: boolean;
  isLoading: boolean;
}

const EMPTY_NODE: FolderNode = { folders: [], page: 0, hasMore: false, isLoading: true };

export const toSftpPath = (path: string) => (path.includes('\\') ? `/${path.split('\\').join('/')}` : path);

export const useRemoteFolders = (serverId: number, onPick: (path: string) => void) => {
  const [nodes, setNodes] = useState<Record<string, FolderNode>>({});
  const [expanded, setExpanded] = useState<ReadonlySet<string>>(new Set());
  const [selected, setSelected] = useState<string | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (path: string, page: number) => {
    setNodes((prev) => ({ ...prev, [path]: { ...(prev[path] ?? EMPTY_NODE), isLoading: true } }));
    try {
      const found = await fetchChildren(serverId, path, page);
      const folders = found.entries.filter((entry) => entry.type === 'd');
      const hasMore = folders.length === found.entries.length && (page + 1) * PAGE_SIZE < found.total;
      setNodes((prev) => ({ ...prev, [path]: { folders: [...(prev[path]?.folders ?? []), ...folders], page, hasMore, isLoading: false } }));
      return folders;
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się pobrać katalogów serwera.'));
      setNodes((prev) => ({ ...prev, [path]: { ...(prev[path] ?? EMPTY_NODE), isLoading: false } }));
      return [];
    }
  }, [serverId]);

  const expand = useCallback((path: string) => {
    setExpanded((prev) => new Set(prev).add(path));
    void load(path, 0);
  }, [load]);

  const open = useCallback(async () => {
    setIsOpen(true);
    setError(null);
    setNodes({});
    setExpanded(new Set());
    const roots = await load(ROOT, 0);
    if (roots.length === 1) expand(roots[0].path);
  }, [load, expand]);

  const toggle = useCallback((path: string) => {
    if (!expanded.has(path)) {
      if (nodes[path]) setExpanded((prev) => new Set(prev).add(path));
      else expand(path);
      return;
    }
    setExpanded((prev) => {
      const next = new Set(prev);
      next.delete(path);
      return next;
    });
  }, [expanded, nodes, expand]);

  const loadMore = useCallback((path: string) => {
    const node = nodes[path];
    if (node) void load(path, node.page + 1);
  }, [nodes, load]);

  const close = useCallback(() => setIsOpen(false), []);

  const confirm = useCallback(() => {
    if (selected) onPick(toSftpPath(selected));
    setIsOpen(false);
  }, [selected, onPick]);

  return { rootPath: ROOT, nodes, expanded, selected, setSelected, isOpen, error, open, close, toggle, loadMore, confirm };
};

export type RemoteFolders = ReturnType<typeof useRemoteFolders>;
