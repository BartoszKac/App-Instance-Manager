import { useCallback, useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchDeployableFiles } from '@/shared/deployable/deployableFilesApi';
import { executeCommand as postCommand, fetchPrograms, fetchServers } from '../api/executionApi';
import type { DeployedInstance, TerminalLine } from '../model/Executionmodel';
import { mapConfigToInstance } from './instances';

let nextLineId = 0;
const mkLine = (text: string, type: TerminalLine['type']): TerminalLine => ({ id: nextLineId++, text, type });

const loadInstances = async (): Promise<DeployedInstance[]> => {
  const [programs, servers, files] = await Promise.all([fetchPrograms(), fetchServers(), fetchDeployableFiles()]);
  return programs.map((cfg) => mapConfigToInstance(cfg, servers, files));
};

const parseOutputLines = (text: string): string[] => {
  try {
    const json: unknown = JSON.parse(text);
    return (typeof json === 'string' ? json : JSON.stringify(json, null, 2)).split('\n');
  } catch {
    return text.split('\n');
  }
};

const useTerminal = () => {
  const [terminalLines, setTerminalLines] = useState<TerminalLine[]>([
    mkLine('Tu pojawi się wynik komend uruchamianych na serwerach.', 'info'),
  ]);
  const pushLine = useCallback((text: string, type: TerminalLine['type']) => {
    setTerminalLines((prev) => [...prev, mkLine(text, type)]);
  }, []);
  const clearTerminal = useCallback(() => setTerminalLines([]), []);
  return { terminalLines, pushLine, clearTerminal };
};

const useInstances = (pushLine: (text: string, type: TerminalLine['type']) => void) => {
  const [instances, setInstances] = useState<DeployedInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const applyLoad = useCallback((load: Promise<DeployedInstance[]>) =>
    load
      .then(setInstances)
      .catch((err: unknown) => {
        const msg = errorMessage(err);
        setError(msg);
        pushLine(`Nie udało się wczytać programów: ${msg}`, 'error');
      })
      .finally(() => setLoading(false)), [pushLine]);

  const fetchInstances = useCallback(() => {
    setLoading(true);
    setError(null);
    pushLine('Wczytywanie programów…', 'info');
    return applyLoad(loadInstances());
  }, [applyLoad, pushLine]);

  useEffect(() => { void applyLoad(loadInstances()); }, [applyLoad]);

  return { instances, loading, error, fetchInstances };
};

export const useExecutionViewModel = () => {
  const { terminalLines, pushLine, clearTerminal } = useTerminal();
  const { instances, loading, error, fetchInstances } = useInstances(pushLine);

  const executeCommand = useCallback(async (idConfiguration: number, command: string) => {
    if (!command.trim()) return;
    const ts = new Date().toLocaleTimeString('pl-PL', { hour12: false });
    pushLine(`[${ts}] $ ${command}`, 'command');
    try {
      parseOutputLines(await postCommand(command, idConfiguration)).forEach((line) => pushLine(line, 'success'));
    } catch (err: unknown) {
      pushLine(errorMessage(err), 'error');
    }
  }, [pushLine]);

  return { instances, loading, error, terminalLines, executeCommand, fetchInstances, clearTerminal };
};
