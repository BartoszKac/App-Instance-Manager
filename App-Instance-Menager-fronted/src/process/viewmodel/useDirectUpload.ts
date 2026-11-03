import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchUploadHistory } from '../api/transferApi';
import type { DirectUploadPage, UploadItem } from '../model/DirectUpload';
import { useRemoteFolders } from './useRemoteFolders';
import { startUploadQueue } from './uploadQueue';
import type { UploadQueueHandle } from './uploadQueue';

const toItem = (file: File): UploadItem => ({
  relativePath: file.webkitRelativePath || file.name,
  file,
  status: 'waiting',
  percent: 0,
  error: null,
});

export const useDirectUpload = (serverId: number) => {
  const [destination, setDestination] = useState('');
  const [overwrite, setOverwrite] = useState(false);
  const [items, setItems] = useState<UploadItem[]>([]);
  const [isSending, setIsSending] = useState(false);
  const [history, setHistory] = useState<DirectUploadPage | null>(null);
  const [error, setError] = useState<string | null>(null);
  const queue = useRef<UploadQueueHandle | null>(null);
  const folders = useRemoteFolders(serverId, setDestination);

  const reloadHistory = useCallback(async () => {
    try {
      setHistory(await fetchUploadHistory(serverId));
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się pobrać historii wysyłek.'));
    }
  }, [serverId]);

  useEffect(() => {
    const first = setTimeout(reloadHistory, 0);
    return () => {
      clearTimeout(first);
      queue.current?.stop();
    };
  }, [reloadHistory]);

  useEffect(() => {
    if (!isSending) return;
    const warn = (e: BeforeUnloadEvent) => e.preventDefault();
    window.addEventListener('beforeunload', warn);
    return () => window.removeEventListener('beforeunload', warn);
  }, [isSending]);

  const choose = useCallback((files: FileList | null) => {
    setItems(Array.from(files ?? []).map(toItem));
  }, []);

  const send = useCallback(async () => {
    setIsSending(true);
    setError(null);
    const pending = items.map((item) => ({ ...item, status: 'waiting' as const, percent: 0, error: null }));
    setItems(pending);
    const onChange = (index: number, patch: Partial<UploadItem>) =>
      setItems((prev) => prev.map((item, i) => (i === index ? { ...item, ...patch } : item)));
    queue.current = startUploadQueue({ items: pending, target: { serverId, destination: destination.trim(), overwrite }, onChange });
    await queue.current.done;
    queue.current = null;
    setIsSending(false);
    await reloadHistory();
  }, [items, serverId, destination, overwrite, reloadHistory]);

  const stop = useCallback(() => queue.current?.stop(), []);

  const summary = useMemo(() => ({
    total: items.length,
    done: items.filter((item) => item.status === 'done').length,
    failed: items.filter((item) => item.status === 'failed').length,
    bytes: items.reduce((sum, item) => sum + item.file.size, 0),
  }), [items]);

  const canSend = !isSending && items.length > 0 && destination.trim().startsWith('/');

  return { folders, destination, setDestination, overwrite, setOverwrite, items, summary, isSending, canSend, history, error, choose, send, stop };
};

export type DirectUploadViewModel = ReturnType<typeof useDirectUpload>;
