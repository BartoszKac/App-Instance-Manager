// src/deploy/viewmodel/useDeployViewModel.ts
import { useState, useMemo, useCallback, useEffect } from 'react';
import {
  MainClass,
  Process,
  DeployServer,
  DeployFile,
  BulkTransferItem,
  UploadStrategyType,
  NewServerForm,
  EMPTY_NEW_SERVER,
  getServerId,
  defaultStrategy,
} from '../model/DeployState';

// ─── API ──────────────────────────────────────────────────────────────────────

const API = 'http://localhost:8888';

async function apiFetch<T = void>(path: string, opts?: RequestInit): Promise<T> {
  const res = await fetch(`${API}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...opts,
  });
  if (!res.ok) throw new Error(`${res.status} ${res.statusText} — ${path}`);
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}

function delay(ms: number) {
  return new Promise<void>((r) => setTimeout(r, ms));
}

// ─── INTERFACE ────────────────────────────────────────────────────────────────

export interface DeployViewModel {
  loading: boolean;
  loadError: string | null;

  files: DeployFile[];
  allServers: DeployServer[];

  selectedFiles: Set<string>;
  selectedServers: Set<number>;

  selectedFilesList: DeployFile[];
  selectedServersList: DeployServer[];
  canDeploy: boolean;

  // per-file strategy
  setFileStrategy: (key: string, strategy: UploadStrategyType) => void;

  toggleFile: (key: string) => void;
  toggleAllFiles: () => void;

  toggleServer: (id: number) => void;
  deleteServer: (id: number) => Promise<void>;

  deploy: () => Promise<void>;
  deploying: boolean;
  deployLog: string[];
  deployDone: boolean;

  showAddServer: boolean;
  setShowAddServer: (v: boolean) => void;
  newServer: NewServerForm;
  setNewServer: (fn: (prev: NewServerForm) => NewServerForm) => void;
  addServer: () => Promise<void>;
  addingServer: boolean;
  addServerError: string | null;
}

// ─── HOOK ─────────────────────────────────────────────────────────────────────

export function useDeployViewModel(): DeployViewModel {
  const [mainClasses, setMainClasses] = useState<MainClass[]>([]);
  const [processes,   setProcesses]   = useState<Process[]>([]);
  const [allServers,  setAllServers]  = useState<DeployServer[]>([]);

  const [loading,   setLoading]   = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [selectedFiles,   setSelectedFiles]   = useState<Set<string>>(new Set());
  const [selectedServers, setSelectedServers] = useState<Set<number>>(new Set());

  // per-file strategy overrides: key → UploadStrategyType
  const [fileStrategies, setFileStrategies] = useState<Record<string, UploadStrategyType>>({});

  const [deploying,  setDeploying]  = useState(false);
  const [deployLog,  setDeployLog]  = useState<string[]>([]);
  const [deployDone, setDeployDone] = useState(false);

  const [showAddServer,  setShowAddServer]  = useState(false);
  const [newServer,      setNewServer]      = useState<NewServerForm>(EMPTY_NEW_SERVER);
  const [addingServer,   setAddingServer]   = useState(false);
  const [addServerError, setAddServerError] = useState<string | null>(null);

  // ── fetch on mount ─────────────────────────────────────────────────────────
  useEffect(() => {
    let cancelled = false;
    const fetchAll = async () => {
      setLoading(true);
      setLoadError(null);
      try {
        const [mc, procs, servers] = await Promise.all([
          apiFetch<MainClass[]>('/api/code/source/main-class/all'),
          apiFetch<Process[]>('/api/process'),
          apiFetch<DeployServer[]>('/api/deploy/servers'),
        ]);
        if (cancelled) return;
        setMainClasses(mc);
        setProcesses(procs);
        setAllServers(servers);
      } catch (err: unknown) {
        if (!cancelled)
          setLoadError(err instanceof Error ? err.message : 'Błąd pobierania danych');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    fetchAll();
    return () => { cancelled = true; };
  }, []);

  // ── derived: file list with per-file strategy ──────────────────────────────
  const files = useMemo<DeployFile[]>(() => {
    const src: DeployFile[] = mainClasses.map((mc) => {
      const key = `source::${mc.idManClass}`;
      return {
        key,
        name:     mc.name,
        language: mc.language,
        source:   'source' as const,
        id:       mc.idManClass,
        strategy: fileStrategies[key] ?? defaultStrategy(mc.language),
      };
    });
    const proc: DeployFile[] = processes.map((p) => {
      const key = `process::${p.id}`;
      return {
        key,
        name:     p.name,
        language: p.language,
        source:   'process' as const,
        id:       p.id,
        strategy: fileStrategies[key] ?? defaultStrategy(p.language),
      };
    });
    return [...src, ...proc];
  }, [mainClasses, processes, fileStrategies]);

  const selectedFilesList = useMemo(
    () => files.filter((f) => selectedFiles.has(f.key)),
    [files, selectedFiles],
  );

  const selectedServersList = useMemo(
    () => allServers.filter((s) => selectedServers.has(getServerId(s))),
    [allServers, selectedServers],
  );

  const canDeploy =
    selectedFilesList.length > 0 && selectedServersList.length > 0 && !deploying;

  // ── helpers ────────────────────────────────────────────────────────────────
  const resetDeployState = useCallback(() => {
    setDeployDone(false);
    setDeployLog([]);
  }, []);

  // ── file actions ───────────────────────────────────────────────────────────
  const setFileStrategy = useCallback((key: string, strategy: UploadStrategyType) => {
    setFileStrategies((prev) => ({ ...prev, [key]: strategy }));
  }, []);

  const toggleFile = useCallback((key: string) => {
    setSelectedFiles((prev) => {
      const next = new Set(prev);
      next.has(key) ? next.delete(key) : next.add(key);
      return next;
    });
    resetDeployState();
  }, [resetDeployState]);

  const toggleAllFiles = useCallback(() => {
    setSelectedFiles((prev) => {
      if (prev.size === files.length && files.length > 0) return new Set();
      return new Set(files.map((f) => f.key));
    });
    resetDeployState();
  }, [files, resetDeployState]);

  // ── server actions ─────────────────────────────────────────────────────────
  const toggleServer = useCallback((id: number) => {
    setSelectedServers((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
    resetDeployState();
  }, [resetDeployState]);

  const deleteServer = useCallback(async (id: number) => {
    if (!id) return;
    try {
      await apiFetch(`/api/deploy/server/${id}`, { method: 'DELETE' });
      setAllServers((prev) => prev.filter((s) => getServerId(s) !== id));
      setSelectedServers((prev) => {
        const next = new Set(prev);
        next.delete(id);
        return next;
      });
    } catch (err: unknown) {
      console.error('Błąd usuwania serwera:', err);
    }
  }, []);

  // ── deploy ─────────────────────────────────────────────────────────────────
  const deploy = useCallback(async () => {
    if (!canDeploy) return;

    setDeploying(true);
    setDeployDone(false);
    const log: string[] = [];
    const push = (msg: string) => { log.push(msg); setDeployLog([...log]); };

    push(`▶ Przygotowuję deploy — ${selectedFilesList.length} plik(ów) → ${selectedServersList.length} serwer(ów)`);
    await delay(300);

    // każdy plik ma własną strategię
    const payload: BulkTransferItem[] = [];
    for (const srv of selectedServersList) {
      for (const file of selectedFilesList) {
        payload.push({
          mainClassId:        file.id,
          configurationId:    getServerId(srv),
          uploadStrategyType: file.strategy,
        });
      }
    }

    push(`⚡ Wysyłam ${payload.length} operacji…`);
    await delay(200);

    try {
      await apiFetch('/api/deploy/transfer/bulk', {
        method: 'POST',
        body:   JSON.stringify(payload),
      });
      for (const srv of selectedServersList) {
        push(`\n→ ${srv.name} (${srv.user}@${srv.ip})`);
        for (const file of selectedFilesList) {
          push(`   ✅ ${file.name} [${file.strategy}]`);
        }
      }
      await delay(200);
      push(`\n✅ Deploy zakończony pomyślnie`);
      setDeployDone(true);
    } catch (err: unknown) {
      push(`\n✗ Błąd deployu: ${err instanceof Error ? err.message : String(err)}`);
    } finally {
      setDeploying(false);
    }
  }, [canDeploy, selectedFilesList, selectedServersList]);

  // ── add server ─────────────────────────────────────────────────────────────
  const addServer = useCallback(async () => {
    if (!newServer.name.trim() || !newServer.host.trim()) return;
    setAddingServer(true);
    setAddServerError(null);
    try {
      await apiFetch('/api/deploy/server', {
        method: 'POST',
        body: JSON.stringify({
          name: newServer.name.trim(),
          ip:   newServer.host.trim(),
          user: newServer.user.trim() || 'deploy',
          pass: newServer.pass,
        }),
      });
      const updated = await apiFetch<DeployServer[]>('/api/deploy/servers');
      setAllServers(updated);
      setNewServer(() => EMPTY_NEW_SERVER);
      setShowAddServer(false);
    } catch (err: unknown) {
      setAddServerError(err instanceof Error ? err.message : 'Błąd dodawania serwera');
    } finally {
      setAddingServer(false);
    }
  }, [newServer]);

  return {
    loading, loadError,
    files, allServers,
    selectedFiles, selectedServers,
    selectedFilesList, selectedServersList,
    canDeploy,
    setFileStrategy,
    toggleFile, toggleAllFiles,
    toggleServer, deleteServer,
    deploy, deploying, deployLog, deployDone,
    showAddServer, setShowAddServer,
    newServer, setNewServer,
    addServer, addingServer, addServerError,
  };
}