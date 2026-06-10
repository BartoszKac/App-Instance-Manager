// src/execution/viewmodel/useExecutionViewModel.ts
import { useState, useEffect, useCallback } from 'react';
import {
  DeployedInstance,
  ProgramConfig,
  TerminalLine,
  mkLine,
  mapConfigToInstance,
} from '../model/ExecutionModel';

const BASE = 'http://localhost:8888/api/deploy';

export const useExecutionViewModel = () => {
  const [instances, setInstances]         = useState<DeployedInstance[]>([]);
  const [loading, setLoading]             = useState(false);
  const [error, setError]                 = useState<string | null>(null);
  const [terminalLines, setTerminalLines] = useState<TerminalLine[]>([
    mkLine('Ready. Fetch instances to begin.', 'info'),
  ]);

  const pushLine = useCallback((text: string, type: TerminalLine['type']) => {
    setTerminalLines(prev => [...prev, mkLine(text, type)]);
  }, []);

  const fetchInstances = useCallback(async () => {
    setLoading(true);
    setError(null);
    pushLine('Fetching program configurations...', 'info');
    try {
      const res = await fetch(`${BASE}/programs`);
      if (!res.ok) throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      const data: ProgramConfig[] = await res.json();
      setInstances(data.map(mapConfigToInstance));
      pushLine(`Loaded ${data.length} instance(s).`, 'success');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setError(msg);
      pushLine(`Error fetching instances: ${msg}`, 'error');
    } finally {
      setLoading(false);
    }
  }, [pushLine]);

  useEffect(() => {
    fetchInstances();
  }, [fetchInstances]);

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
  }, [pushLine]);

  const clearTerminal = useCallback(() => {
    setTerminalLines([mkLine('Terminal cleared.', 'info')]);
  }, []);

  return { instances, loading, error, terminalLines, executeCommand, fetchInstances, clearTerminal };
};