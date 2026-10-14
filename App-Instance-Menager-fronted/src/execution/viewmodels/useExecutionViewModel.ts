import { useState, useEffect, useCallback } from 'react';
import type { DeployedInstance, ProgramConfig, ServerConfig, TerminalLine } from '../model/Executionmodel';
import { mkLine, mapConfigToInstance } from '../model/Executionmodel';
import { errorMessage } from '@/shared/errorMessage';

const BASE = 'http://localhost:8888/api/deploy';

const fetchJson = async <T,>(path: string): Promise<T> => {
  const res = await fetch(`${BASE}${path}`);
  if (!res.ok) throw new Error(`${path} HTTP ${res.status}`);
  return res.json() as Promise<T>;
};

const loadInstances = async (): Promise<DeployedInstance[]> => {
  const [programs, servers] = await Promise.all([
    fetchJson<ProgramConfig[]>('/programs'),
    fetchJson<ServerConfig[]>('/servers'),
  ]);
  return programs.map(cfg => mapConfigToInstance(cfg, servers));
};

const parseOutputLines = (text: string): string[] => {
  try {
    const json: unknown = JSON.parse(text);
    return (typeof json === 'string' ? json : JSON.stringify(json, null, 2)).split('\n');
  } catch {
    return text.split('\n');
  }
};

const postCommand = async (command: string, programConfigId: number): Promise<string[]> => {
  const res = await fetch(`${BASE}/program/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ command, programConfigId }),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`Error (${res.status}): ${text || res.statusText}`);
  return parseOutputLines(text);
};

export const useExecutionViewModel = () => {
  const [instances, setInstances]         = useState<DeployedInstance[]>([]);
  const [loading, setLoading]             = useState(true);
  const [error, setError]                 = useState<string | null>(null);
  const [terminalLines, setTerminalLines] = useState<TerminalLine[]>([
    mkLine('Tu pojawi się wynik komend uruchamianych na serwerach.', 'info'),
  ]);

  const pushLine = useCallback((text: string, type: TerminalLine['type']) => {
    setTerminalLines(prev => [...prev, mkLine(text, type)]);
  }, []);

  const applyLoad = useCallback((load: Promise<DeployedInstance[]>) =>
    load
      .then(list => {
        setInstances(list);
      })
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

  useEffect(() => {
    void applyLoad(loadInstances());
  }, [applyLoad]);

  const executeCommand = useCallback(async (idConfiguration: number, command: string) => {
    if (!command.trim()) return;
    const ts = new Date().toLocaleTimeString('pl-PL', { hour12: false });
    pushLine(`[${ts}] $ ${command}`, 'command');
    try {
      const lines = await postCommand(command, idConfiguration);
      lines.forEach(line => pushLine(line, 'success'));
    } catch (err: unknown) {
      pushLine(errorMessage(err), 'error');
    }
  }, [pushLine]);

  const clearTerminal = useCallback(() => {
    setTerminalLines([]);
  }, []);

  return { instances, loading, error, terminalLines, executeCommand, fetchInstances, clearTerminal };
};
