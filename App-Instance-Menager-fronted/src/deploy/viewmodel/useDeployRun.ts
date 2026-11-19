import { useCallback, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { transferBulk } from '../api/deployApi';
import type { BulkTransferItem, DeployFile, DeployServer, TransferResult } from '../model/DeployState';
import { STRATEGY_LABEL, getServerId } from './deployRules';

interface PlannedTask {
  file: DeployFile;
  server: DeployServer;
  item: BulkTransferItem;
}

const planTasks = (files: DeployFile[], servers: DeployServer[]): PlannedTask[] =>
  servers.flatMap((server) => files.map((file) => ({
    file,
    server,
    item: { mainClassId: file.id, configurationId: getServerId(server), uploadStrategyType: file.strategy },
  })));

const resultLine = (task: PlannedTask, result: TransferResult | undefined) => {
  const label = `${task.file.name} [${STRATEGY_LABEL[task.file.strategy]}]`;
  if (!result) return `   ✗ ${label} — brak odpowiedzi serwera aplikacji`;
  return result.success ? `   ✅ ${label}` : `   ✗ ${label} — ${result.message}`;
};

const resultLines = (tasks: PlannedTask[], results: TransferResult[]): string[] =>
  tasks.flatMap((task, i) => {
    const header = i === 0 || tasks[i - 1].server !== task.server ? [`\n→ ${task.server.name} (${task.server.user}@${task.server.ip})`] : [];
    return [...header, resultLine(task, results[i])];
  });

const summaryLine = (tasks: PlannedTask[], results: TransferResult[]) => {
  const failed = tasks.length - results.filter((r) => r.success).length;
  return failed === 0 ? `\n✅ Wysłano wszystko (${tasks.length})` : `\n✗ Nie udało się wysłać ${failed} z ${tasks.length}. Szczegóły powyżej.`;
};

export const useDeployProgress = () => {
  const [deploying, setDeploying] = useState(false);
  const [deployLog, setDeployLog] = useState<string[]>([]);
  const resetDeployState = useCallback(() => setDeployLog([]), []);
  return { deploying, setDeploying, deployLog, setDeployLog, resetDeployState };
};

type DeployProgress = ReturnType<typeof useDeployProgress>;

export const useDeploy = (progress: DeployProgress, files: DeployFile[], servers: DeployServer[]) => {
  const { deploying, setDeploying, setDeployLog } = progress;
  const canDeploy = files.length > 0 && servers.length > 0 && !deploying;

  const deploy = useCallback(async () => {
    if (!canDeploy) return;
    const tasks = planTasks(files, servers);
    setDeploying(true);
    setDeployLog([`▶ Wysyłanie — ${files.length} plik(ów) na ${servers.length} serwer(ów), ${tasks.length} operacji…`]);
    try {
      const results = await transferBulk(tasks.map((t) => t.item));
      setDeployLog((prev) => [...prev, ...resultLines(tasks, results), summaryLine(tasks, results)]);
    } catch (err: unknown) {
      setDeployLog((prev) => [...prev, `\n✗ Wysyłka nie powiodła się: ${errorMessage(err, 'brak połączenia z aplikacją.')}`]);
    } finally {
      setDeploying(false);
    }
  }, [canDeploy, files, servers, setDeploying, setDeployLog]);

  return { canDeploy, deploy };
};
