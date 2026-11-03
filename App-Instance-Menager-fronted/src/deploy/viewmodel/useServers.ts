import { useCallback, useMemo, useState } from 'react';
import { apiFetch } from '../api/deployHttp';
import { EMPTY_NEW_SERVER, getServerId } from '../model/DeployState';
import type { DeployServer, NewServerForm } from '../model/DeployState';

const toggledId = (set: Set<number>, id: number) => {
  const next = new Set(set);
  if (next.has(id)) next.delete(id); else next.add(id);
  return next;
};

const useServerList = (onSelectionChange: () => void) => {
  const [allServers, setAllServers] = useState<DeployServer[]>([]);
  const [selectedServers, setSelectedServers] = useState<Set<number>>(new Set());

  const selectedServersList = useMemo(
    () => allServers.filter((s) => selectedServers.has(getServerId(s))),
    [allServers, selectedServers],
  );

  const reloadServers = useCallback(async () => {
    setAllServers(await apiFetch<DeployServer[]>('/api/deploy/servers'));
  }, []);

  const toggleServer = useCallback((id: number) => {
    setSelectedServers((prev) => toggledId(prev, id));
    onSelectionChange();
  }, [onSelectionChange]);

  const deleteServer = useCallback(async (id: number) => {
    if (!id) return;
    try {
      await apiFetch(`/api/deploy/server/${id}`, { method: 'DELETE' });
      setAllServers((prev) => prev.filter((s) => getServerId(s) !== id));
      setSelectedServers((prev) => { const next = new Set(prev); next.delete(id); return next; });
    } catch (err: unknown) {
      console.error('Błąd usuwania serwera:', err);
    }
  }, []);

  return { allServers, setAllServers, selectedServers, selectedServersList, reloadServers, toggleServer, deleteServer };
};

const useAddServer = (reloadServers: () => Promise<void>) => {
  const [showAddServer, setShowAddServer] = useState(false);
  const [newServer, setNewServer] = useState<NewServerForm>(EMPTY_NEW_SERVER);
  const [addingServer, setAddingServer] = useState(false);
  const [addServerError, setAddServerError] = useState<string | null>(null);

  const addServer = useCallback(async () => {
    if (!newServer.name.trim() || !newServer.host.trim()) return;
    setAddingServer(true);
    setAddServerError(null);
    try {
      await apiFetch('/api/deploy/server', {
        method: 'POST',
        body: JSON.stringify({
          name: newServer.name.trim(),
          ip: newServer.host.trim(),
          user: newServer.user.trim() || 'deploy',
          pass: newServer.pass,
        }),
      });
      await reloadServers();
      setNewServer(() => EMPTY_NEW_SERVER);
      setShowAddServer(false);
    } catch (err: unknown) {
      setAddServerError(err instanceof Error ? err.message : 'Błąd dodawania serwera');
    } finally {
      setAddingServer(false);
    }
  }, [newServer, reloadServers]);

  return { showAddServer, setShowAddServer, newServer, setNewServer, addServer, addingServer, addServerError };
};

export const useServers = (onSelectionChange: () => void) => {
  const list = useServerList(onSelectionChange);
  const add = useAddServer(list.reloadServers);
  return { ...list, ...add };
};
