import { useEffect, useMemo, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchDeployableFiles } from '@/shared/deployable/deployableFilesApi';
import type { DeployableFiles } from '@/shared/deployable/model';
import { fetchServers } from '../api/deployApi';
import type { DeployFile, DeployServer, NewServerForm, UploadStrategyType } from '../model/DeployState';
import { useDeploy, useDeployProgress } from './useDeployRun';
import { useDeployFiles } from './useDeployFiles';
import { useNetworkScan } from './useNetworkScan';
import type { NetworkScanViewModel } from './useNetworkScan';
import { useServerPreview } from './useServerPreview';
import type { ServerPreviewViewModel } from './useServerPreview';
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
  showAddServer: boolean;
  setShowAddServer: (v: boolean) => void;
  newServer: NewServerForm;
  setNewServer: (fn: (prev: NewServerForm) => NewServerForm) => void;
  addServer: () => Promise<void>;
  addingServer: boolean;
  addServerError: string | null;
  networkScan: NetworkScanViewModel;
  serverPreview: ServerPreviewViewModel;
}

interface InitialData {
  setDeployable: (value: DeployableFiles) => void;
  setAllServers: (value: DeployServer[]) => void;
}

const useInitialLoad = ({ setDeployable, setAllServers }: InitialData) => {
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    Promise.all([fetchDeployableFiles(), fetchServers()])
      .then(([deployable, servers]) => {
        if (cancelled) return;
        setDeployable(deployable);
        setAllServers(servers);
      })
      .catch((err: unknown) => { if (!cancelled) setLoadError(errorMessage(err, 'Nie udało się wczytać plików i serwerów.')); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [setDeployable, setAllServers]);

  return { loading, loadError };
};

export function useDeployViewModel(): DeployViewModel {
  const progress = useDeployProgress();
  const files = useDeployFiles(progress.resetDeployState);
  const servers = useServers(progress.resetDeployState);
  const { canDeploy, deploy } = useDeploy(progress, files.selectedFilesList, servers.selectedServersList);
  const { loading, loadError } = useInitialLoad({ setDeployable: files.setDeployable, setAllServers: servers.setAllServers });
  const knownIps = useMemo(() => servers.allServers.map((s) => s.ip), [servers.allServers]);
  const networkScan = useNetworkScan(knownIps, servers.reloadServers);
  const serverPreview = useServerPreview();

  return {
    loading, loadError, canDeploy, deploy, networkScan, serverPreview,
    files: files.files, selectedFiles: files.selectedFiles, selectedFilesList: files.selectedFilesList,
    setFileStrategy: files.setFileStrategy, toggleFile: files.toggleFile, toggleAllFiles: files.toggleAllFiles,
    allServers: servers.allServers, selectedServers: servers.selectedServers, selectedServersList: servers.selectedServersList,
    toggleServer: servers.toggleServer, deleteServer: servers.deleteServer,
    deploying: progress.deploying, deployLog: progress.deployLog,
    showAddServer: servers.showAddServer, setShowAddServer: servers.setShowAddServer,
    newServer: servers.newServer, setNewServer: servers.setNewServer,
    addServer: servers.addServer, addingServer: servers.addingServer, addServerError: servers.addServerError,
  };
}
