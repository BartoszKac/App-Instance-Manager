import { useCallback, useState } from 'react';
import { apiFetch } from '../api/deployHttp';
import { getServerId } from '../model/DeployState';
import type { BulkTransferItem, DeployFile, DeployServer } from '../model/DeployState';

const delay = (ms: number) => new Promise<void>((r) => setTimeout(r, ms));

const buildPayload = (files: DeployFile[], servers: DeployServer[]): BulkTransferItem[] =>
  servers.flatMap((srv) => files.map((file) => ({
    mainClassId: file.id,
    configurationId: getServerId(srv),
    uploadStrategyType: file.strategy,
  })));

const pushServerResults = (push: (msg: string) => void, files: DeployFile[], servers: DeployServer[]) => {
  for (const srv of servers) {
    push(`\n→ ${srv.name} (${srv.user}@${srv.ip})`);
    for (const file of files) push(`   ✅ ${file.name} [${file.strategy}]`);
  }
};

export const useDeployProgress = () => {
  const [deploying, setDeploying] = useState(false);
  const [deployLog, setDeployLog] = useState<string[]>([]);
  const [deployDone, setDeployDone] = useState(false);

  const resetDeployState = useCallback(() => {
    setDeployDone(false);
    setDeployLog([]);
  }, []);

  return { deploying, setDeploying, deployLog, setDeployLog, deployDone, setDeployDone, resetDeployState };
};

type DeployProgress = ReturnType<typeof useDeployProgress>;

export const useDeploy = (progress: DeployProgress, files: DeployFile[], servers: DeployServer[]) => {
  const { deploying, setDeploying, setDeployLog, setDeployDone } = progress;
  const canDeploy = files.length > 0 && servers.length > 0 && !deploying;

  const deploy = useCallback(async () => {
    if (!canDeploy) return;
    setDeploying(true);
    setDeployDone(false);
    const log: string[] = [];
    const push = (msg: string) => { log.push(msg); setDeployLog([...log]); };

    push(`▶ Przygotowuję deploy — ${files.length} plik(ów) → ${servers.length} serwer(ów)`);
    await delay(300);
    const payload = buildPayload(files, servers);
    push(`⚡ Wysyłam ${payload.length} operacji…`);
    await delay(200);

    try {
      await apiFetch('/api/deploy/transfer/bulk', { method: 'POST', body: JSON.stringify(payload) });
      pushServerResults(push, files, servers);
      await delay(200);
      push(`\n✅ Deploy zakończony pomyślnie`);
      setDeployDone(true);
    } catch (err: unknown) {
      push(`\n✗ Błąd deployu: ${err instanceof Error ? err.message : String(err)}`);
    } finally {
      setDeploying(false);
    }
  }, [canDeploy, files, servers, setDeploying, setDeployDone, setDeployLog]);

  return { canDeploy, deploy };
};
