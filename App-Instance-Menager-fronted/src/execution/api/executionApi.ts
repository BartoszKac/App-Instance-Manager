import { API_BASE_URL } from '@/shared/apiBase';
import { apiFetch } from '@/shared/http';
import type { ProgramConfig, ServerConfig } from '../model/Executionmodel';

export const fetchPrograms = () => apiFetch<ProgramConfig[]>('/api/deploy/programs');

export const fetchServers = () => apiFetch<ServerConfig[]>('/api/deploy/servers');

export const executeCommand = async (command: string, programConfigId: number): Promise<string> => {
  const res = await fetch(`${API_BASE_URL}/api/deploy/program/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ command, programConfigId }),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`Serwer aplikacji odpowiedział ${res.status}: ${text || res.statusText}`);
  return text;
};
