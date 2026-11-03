import { useEffect, useMemo, useState } from 'react';
import { apiFetch } from '@/shared/http';
import type {
  DeployFile,
  DeployServer,
  MainClass,
  NewServerForm,
  Process,
  UploadStrategyType,
} from '../model/DeployState';
import { useDeploy, useDeployProgress } from './useDeployRun';
import { useDeployFiles } from './useDeployFiles';
import { useNetworkScan } from './useNetworkScan';
import type { NetworkScanViewModel } from './useNetworkScan';
import { useServers } from './useServers';

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
  networkScan: NetworkScanViewModel;
}

interface InitialData {
  setMainClasses: (value: MainClass[]) => void;
  setProcesses: (value: Process[]) => void;
  setAllServers: (value: DeployServer[]) => void;
}

const useInitialLoad = ({ setMainClasses, setProcesses, setAllServers }: InitialData) => {
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([
      apiFetch<MainClass[]>('/api/code/source/main-class/all'),
      apiFetch<Process[]>('/api/process'),
      apiFetch<DeployServer[]>('/api/deploy/servers'),
    ])
      .then(([mainClasses, processes, servers]) => {
        if (cancelled) return;
        setMainClasses(mainClasses);
        setProcesses(processes);
        setAllServers(servers);
      })
      .catch((err: unknown) => { if (!cancelled) setLoadError(err instanceof Error ? err.message : 'Błąd pobierania danych'); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [setMainClasses, setProcesses, setAllServers]);

  return { loading, loadError };
};

export function useDeployViewModel(): DeployViewModel {
  const progress = useDeployProgress();
  const files = useDeployFiles(progress.resetDeployState);
  const servers = useServers(progress.resetDeployState);
  const { canDeploy, deploy } = useDeploy(progress, files.selectedFilesList, servers.selectedServersList);
  const { loading, loadError } = useInitialLoad({ setMainClasses: files.setMainClasses, setProcesses: files.setProcesses, setAllServers: servers.setAllServers });
  const knownIps = useMemo(() => servers.allServers.map((s) => s.ip), [servers.allServers]);
  const networkScan = useNetworkScan(knownIps, servers.reloadServers);

  return {
    loading, loadError, canDeploy, deploy, networkScan,
    files: files.files, selectedFiles: files.selectedFiles, selectedFilesList: files.selectedFilesList,
    setFileStrategy: files.setFileStrategy, toggleFile: files.toggleFile, toggleAllFiles: files.toggleAllFiles,
    allServers: servers.allServers, selectedServers: servers.selectedServers, selectedServersList: servers.selectedServersList,
    toggleServer: servers.toggleServer, deleteServer: servers.deleteServer,
    deploying: progress.deploying, deployLog: progress.deployLog, deployDone: progress.deployDone,
    showAddServer: servers.showAddServer, setShowAddServer: servers.setShowAddServer,
    newServer: servers.newServer, setNewServer: servers.setNewServer,
    addServer: servers.addServer, addingServer: servers.addingServer, addServerError: servers.addServerError,
  };
}
