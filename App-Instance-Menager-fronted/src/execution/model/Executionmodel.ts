// src/execution/model/ExecutionModel.ts

export interface ProgramConfig {
  idConfiguration: number;
  idSerwer: number;
  uploadStrategyType: string;
  pathInServer: string;
  idCode: number;
}

export interface DeployedInstance {
  id: string;
  idConfiguration: number;
  fileName: string;
  serverName: string;
  path: string;
  lang: string;
  status: 'online' | 'offline';
}

export type TerminalLineType = 'info' | 'command' | 'success' | 'error';

export interface TerminalLine {
  id: number;
  text: string;
  type: TerminalLineType;
}

let _lineId = 0;
export function mkLine(text: string, type: TerminalLineType): TerminalLine {
  return { id: _lineId++, text, type };
}

export function inferLang(path: string): string {
  if (path.endsWith('.java') || path.includes('/java/')) return 'java';
  if (path.endsWith('.py'))                               return 'py';
  if (path.endsWith('.ts') || path.endsWith('.js'))       return 'ts';
  return 'sh';
}

export function inferFileName(path: string): string {
  const parts = path.split('/').filter(Boolean);
  return parts[parts.length - 1] ?? path;
}

export function mapConfigToInstance(cfg: ProgramConfig): DeployedInstance {
  return {
    id:              String(cfg.idConfiguration),
    idConfiguration: cfg.idConfiguration,
    fileName:        inferFileName(cfg.pathInServer),
    serverName:      `Server #${cfg.idSerwer}`,
    path:            cfg.pathInServer,
    lang:            inferLang(cfg.pathInServer),
    status:          'online',
  };
}