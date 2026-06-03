import { useState, useMemo, useCallback } from 'react';
import {
  DeployProject,
  DeployServer,
  SelectedFileEntry,
  NewServerForm,
  MOCK_PROJECTS,
  INITIAL_SERVERS,
  EMPTY_NEW_SERVER,
} from '../model/DeployState';

// ─── HELPERS ──────────────────────────────────────────────────────────────────

const fileKey = (projId: number, fileName: string) => `${projId}::${fileName}`;

function delay(ms: number) {
  return new Promise<void>((r) => setTimeout(r, ms));
}

// ─── VIEW MODEL ───────────────────────────────────────────────────────────────

export interface DeployViewModel {
  // data
  projects: DeployProject[];
  allServers: DeployServer[];

  // selection
  selectedFiles: Set<string>;
  selectedServers: Set<string>;
  collapsedProjects: Set<number>;

  // derived
  selectedFilesList: SelectedFileEntry[];
  selectedServersList: DeployServer[];
  canDeploy: boolean;

  // actions — files
  toggleFile: (projId: number, fileName: string) => void;
  toggleAllProjectFiles: (proj: DeployProject) => void;
  toggleCollapseProject: (projId: number) => void;

  // actions — servers
  toggleServer: (serverId: string) => void;

  // actions — deploy
  deploy: () => Promise<void>;
  deploying: boolean;
  deployLog: string[];
  deployDone: boolean;

  // actions — add server modal
  showAddServer: boolean;
  setShowAddServer: (v: boolean) => void;
  newServer: NewServerForm;
  setNewServer: (fn: (prev: NewServerForm) => NewServerForm) => void;
  addServer: () => void;
}

export function useDeployViewModel(): DeployViewModel {
  const [projects]    = useState<DeployProject[]>(MOCK_PROJECTS);
  const [baseServers] = useState<DeployServer[]>(INITIAL_SERVERS);
  const [extraServers, setExtraServers] = useState<DeployServer[]>([]);

  const allServers = useMemo(
    () => [...baseServers, ...extraServers],
    [baseServers, extraServers],
  );

  // selection state
  const [selectedFiles,      setSelectedFiles]      = useState<Set<string>>(new Set());
  const [selectedServers,    setSelectedServers]    = useState<Set<string>>(new Set());
  const [collapsedProjects,  setCollapsedProjects]  = useState<Set<number>>(new Set());

  // deploy state
  const [deploying,  setDeploying]  = useState(false);
  const [deployLog,  setDeployLog]  = useState<string[]>([]);
  const [deployDone, setDeployDone] = useState(false);

  // add-server modal
  const [showAddServer, setShowAddServer] = useState(false);
  const [newServer, setNewServer] = useState<NewServerForm>(EMPTY_NEW_SERVER);

  // ── derived ────────────────────────────────────────────────────────────────

  const selectedFilesList = useMemo<SelectedFileEntry[]>(() => {
    const result: SelectedFileEntry[] = [];
    for (const proj of projects) {
      for (const file of proj.files) {
        if (selectedFiles.has(fileKey(proj.id, file.name))) {
          result.push({ project: proj, file });
        }
      }
    }
    return result;
  }, [selectedFiles, projects]);

  const selectedServersList = useMemo(
    () => allServers.filter((s) => selectedServers.has(s.id)),
    [selectedServers, allServers],
  );

  const canDeploy =
    selectedFilesList.length > 0 && selectedServersList.length > 0 && !deploying;

  // ── actions — files ────────────────────────────────────────────────────────

  const resetDeployState = () => {
    setDeployDone(false);
    setDeployLog([]);
  };

  const toggleFile = useCallback((projId: number, fileName: string) => {
    const k = fileKey(projId, fileName);
    setSelectedFiles((prev) => {
      const next = new Set(prev);
      next.has(k) ? next.delete(k) : next.add(k);
      return next;
    });
    resetDeployState();
  }, []);

  const toggleAllProjectFiles = useCallback(
    (proj: DeployProject) => {
      const keys = proj.files.map((f) => fileKey(proj.id, f.name));
      const allSelected = keys.every((k) => selectedFiles.has(k));
      setSelectedFiles((prev) => {
        const next = new Set(prev);
        if (allSelected) keys.forEach((k) => next.delete(k));
        else             keys.forEach((k) => next.add(k));
        return next;
      });
      resetDeployState();
    },
    [selectedFiles],
  );

  const toggleCollapseProject = useCallback((projId: number) => {
    setCollapsedProjects((prev) => {
      const next = new Set(prev);
      next.has(projId) ? next.delete(projId) : next.add(projId);
      return next;
    });
  }, []);

  // ── actions — servers ──────────────────────────────────────────────────────

  const toggleServer = useCallback((serverId: string) => {
    setSelectedServers((prev) => {
      const next = new Set(prev);
      next.has(serverId) ? next.delete(serverId) : next.add(serverId);
      return next;
    });
    resetDeployState();
  }, []);

  // ── actions — deploy ───────────────────────────────────────────────────────

  const deploy = useCallback(async () => {
    if (!canDeploy) return;

    setDeploying(true);
    setDeployDone(false);
    const log: string[] = [];

    const push = (msg: string) => {
      log.push(msg);
      setDeployLog([...log]);
    };

    push(
      `▶ Starting deploy — ${selectedFilesList.length} file(s) → ${selectedServersList.length} server(s)`,
    );
    await delay(400);

    for (const srv of selectedServersList) {
      push(`\n⚡ Connecting to ${srv.name} (${srv.user}@${srv.host}:${srv.port})…`);
      await delay(500);

      if (srv.status === 'offline') {
        push(`  ✗ ${srv.name} — connection refused`);
        continue;
      }

      push(`  ✓ Authenticated`);

      for (const { project, file } of selectedFilesList) {
        await delay(250 + Math.random() * 300);
        push(`  → ${project.name}/${file.name}`);
      }

      push(`  ✅ ${srv.name} done`);
    }

    await delay(300);
    push(`\n✅ Deploy complete`);
    setDeploying(false);
    setDeployDone(true);
  }, [canDeploy, selectedFilesList, selectedServersList]);

  // ── actions — add server ───────────────────────────────────────────────────

  const addServer = useCallback(() => {
    if (!newServer.name.trim() || !newServer.host.trim()) return;
    setExtraServers((prev) => [
      ...prev,
      {
        id:     `u-${Date.now()}`,
        name:   newServer.name.trim(),
        host:   newServer.host.trim(),
        port:   Number(newServer.port) || 22,
        user:   newServer.user.trim() || 'deploy',
        status: 'unknown',
      },
    ]);
    setNewServer(() => EMPTY_NEW_SERVER);
    setShowAddServer(false);
  }, [newServer]);

  return {
    projects,
    allServers,
    selectedFiles,
    selectedServers,
    collapsedProjects,
    selectedFilesList,
    selectedServersList,
    canDeploy,
    toggleFile,
    toggleAllProjectFiles,
    toggleCollapseProject,
    toggleServer,
    deploy,
    deploying,
    deployLog,
    deployDone,
    showAddServer,
    setShowAddServer,
    newServer,
    setNewServer,
    addServer,
  };
}
