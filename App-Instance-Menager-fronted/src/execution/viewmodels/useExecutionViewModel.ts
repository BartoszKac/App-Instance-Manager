// src/execution/viewmodel/useExecutionViewModel.ts
import { useState, useEffect, useRef, useCallback } from 'react';
import {
  DeployedInstance,
  ProgramConfig,
  ServerConfig,
  TerminalLine,
  mkLine,
  mapConfigToInstance,
} from '../model/Executionmodel';

const BASE = 'http://localhost:8888/api/deploy';

export const useExecutionViewModel = () => {
  const [instances, setInstances]         = useState<DeployedInstance[]>([]);
  const [loading, setLoading]             = useState(false);
  const [error, setError]                 = useState<string | null>(null);
  const [terminalLines, setTerminalLines] = useState<TerminalLine[]>([
    mkLine('Ready. Fetch instances to begin.', 'info'),
  ]);

  // stable ref zamiast pushLine w dependencies
  const setLinesRef = useRef(setTerminalLines);

  const pushLine = useCallback((text: string, type: TerminalLine['type']) => {
    setLinesRef.current(prev => [...prev, mkLine(text, type)]);
  }, []);

  const fetchInstances = useCallback(async () => {
    setLoading(true);
    setError(null);
    pushLine('Fetching program configurations...', 'info');
    try {
      const [programsRes, serversRes] = await Promise.all([
        fetch(`${BASE}/programs`),
        fetch(`${BASE}/servers`),
      ]);

      if (!programsRes.ok) throw new Error(`Programs HTTP ${programsRes.status}`);
      if (!serversRes.ok)  throw new Error(`Servers HTTP ${serversRes.status}`);

      const programs: ProgramConfig[] = await programsRes.json();
      const servers:  ServerConfig[]  = await serversRes.json();

      console.log('[DEBUG] programs:', programs);
      console.log('[DEBUG] servers:', servers);
      console.log('[DEBUG] first program idSerwer:', programs[0]?.idSerwer, '| first server idConfiguration:', servers[0]?.idConfiguration);

      setInstances(programs.map(cfg => mapConfigToInstance(cfg, servers)));
      pushLine(`Loaded ${programs.length} instance(s).`, 'success');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setError(msg);
      pushLine(`Error fetching instances: ${msg}`, 'error');
    } finally {
      setLoading(false);
    }
  // pusta tablica — fetchInstances nigdy się nie zmienia, pushLine jest stable
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    fetchInstances();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const executeCommand = useCallback(async (idConfiguration: number, command: string) => {
    if (!command.trim()) return;
    const ts = new Date().toLocaleTimeString('pl-PL', { hour12: false });
    pushLine(`[${ts}] $ ${command}`, 'command');
    try {
      const res = await fetch(`${BASE}/program/execute`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ command, programConfigId: idConfiguration }),
      });
      const text = await res.text();
      if (!res.ok) {
        pushLine(`Error (${res.status}): ${text || res.statusText}`, 'error');
        return;
      }
      try {
        const json   = JSON.parse(text);
        const output = typeof json === 'string' ? json : JSON.stringify(json, null, 2);
        output.split('\n').forEach((line: string) => pushLine(line, 'success'));
      } catch {
        text.split('\n').forEach((line: string) => pushLine(line, 'success'));
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      pushLine(`Network error: ${msg}`, 'error');
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const clearTerminal = useCallback(() => {
    setTerminalLines([mkLine('Terminal cleared.', 'info')]);
  }, []);

  return { instances, loading, error, terminalLines, executeCommand, fetchInstances, clearTerminal };
};