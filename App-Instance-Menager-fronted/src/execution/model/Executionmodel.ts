import type { UploadStrategyType } from '@/shared/deployable/model';

export interface ProgramConfig {
  idConfiguration: number;
  idSerwer: number;
  uploadStrategyType: UploadStrategyType;
  pathInServer: string;
  idCode: number;
}

export interface ServerConfig {
  idConfiguration: number;
  name: string;
  ip: string;
  user: string;
  pass: string;
  operationSystem: string | null;
}

export interface DeployedInstance {
  id: string;
  idConfiguration: number;
  fileName: string | null;
  isUpload: boolean;
  serverName: string;
  operationSystem: string | null;
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
