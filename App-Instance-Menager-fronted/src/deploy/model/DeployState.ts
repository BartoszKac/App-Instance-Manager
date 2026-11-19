import type { LanguageType, UploadStrategyType } from '@/shared/deployable/model';

export type { LanguageType, UploadStrategyType };

export type OperationSystem = 'LINUX' | 'WINDOWS' | 'MAC';

export interface DeployServer {
  idConfiguration?: number;
  IdConfiguration?: number;
  name: string;
  ip: string;
  user: string;
  pass: string;
  operationSystem: OperationSystem | null;
}

export interface BulkTransferItem {
  mainClassId: number;
  configurationId: number;
  uploadStrategyType: UploadStrategyType;
}

export interface TransferResult {
  mainClassId: number;
  configurationId: number;
  success: boolean;
  message: string;
}

export type FileSource = 'source' | 'process';

export interface StrategyOption {
  strategy: UploadStrategyType;
  disabledReason: string | null;
}

export interface DeployFile {
  key: string;
  name: string;
  language: LanguageType;
  source: FileSource;
  id: number;
  strategy: UploadStrategyType;
  options: StrategyOption[];
  runHint: string;
  description: string | null;
  sizeBytes: number | null;
  uploadedAt: string | null;
}

export interface NewServerForm {
  name: string;
  host: string;
  user: string;
  pass: string;
}
