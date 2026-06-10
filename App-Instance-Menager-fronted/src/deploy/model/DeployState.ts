// src/deploy/model/DeployState.ts

// ─── API RESPONSE TYPES ───────────────────────────────────────────────────────

/** From GET /api/code/source/main-class/all */
export interface MainClass {
  idManClass: number;
  id: number;
  name: string;
  code: string;
  language: LanguageType;
}

/** From GET /api/process */
export interface Process {
  id: number;
  name: string;
  contents: string[];
  language: LanguageType;
}

/** From GET /api/deploy/servers — mirrors RemoteSerwerConfiguration entity */
export interface DeployServer {
  // backend może zwracać idConfiguration lub IdConfiguration — obsługujemy oba
  idConfiguration?: number;
  IdConfiguration?: number;
  name: string;
  ip: string;
  user: string;
  pass: string;
  operationSystem: OperationSystem | null;
}

/** Pomocnicza funkcja — zawsze zwraca id serwera niezależnie od wielkości liter */
export function getServerId(srv: DeployServer): number {
  return (srv.idConfiguration ?? srv.IdConfiguration)!;
}

// ─── TRANSFER TYPES ───────────────────────────────────────────────────────────

export type UploadStrategyType = 'SOURCE_CODE' | 'COMPILED_CODE' | 'REMOTE_EXECUTABLE';

export type LanguageType = 'JAVA' | 'PYTHON' | 'JAVASCRIPT' | string;

export type OperationSystem = 'LINUX' | 'WINDOWS' | string;

export interface BulkTransferItem {
  mainClassId: number;
  configurationId: number;
  uploadStrategyType: UploadStrategyType;
}

// ─── UI TYPES ─────────────────────────────────────────────────────────────────

export type FileSource = 'source' | 'process';

export interface DeployFile {
  key: string;
  name: string;
  language: LanguageType;
  source: FileSource;
  id: number;
  /** Strategia przypisana do tego konkretnego pliku */
  strategy: UploadStrategyType;
}

export interface NewServerForm {
  name: string;
  host: string;
  user: string;
  pass: string;
}

// ─── CONSTANTS ────────────────────────────────────────────────────────────────

export const UPLOAD_STRATEGIES: UploadStrategyType[] = [
  'SOURCE_CODE',
  'COMPILED_CODE',
  'REMOTE_EXECUTABLE',
];

export const STRATEGY_LABEL: Record<UploadStrategyType, string> = {
  SOURCE_CODE:       'Source',
  COMPILED_CODE:     'Compiled',
  REMOTE_EXECUTABLE: 'Executable',
};

/** Domyślna strategia na podstawie języka */
export function defaultStrategy(language: LanguageType): UploadStrategyType {
  if (language === 'JAVA')   return 'COMPILED_CODE';
  if (language === 'PYTHON') return 'SOURCE_CODE';
  return 'SOURCE_CODE';
}

export const LANG_COLOR: Record<string, string> = {
  JAVA:       '#f59e0b',
  PYTHON:     '#22c55e',
  JAVASCRIPT: '#4f7ef7',
  TS:         '#4f7ef7',
  RS:         '#ef4444',
  CSS:        '#7c3aed',
};

export const LANG_EXT: Record<string, string> = {
  JAVA:       'java',
  PYTHON:     'py',
  JAVASCRIPT: 'js',
};

export const EMPTY_NEW_SERVER: NewServerForm = {
  name: '',
  host: '',
  user: 'deploy',
  pass: '',
};

export const EXT_COLOR: Record<string, string> = {
  ts:   '#4f7ef7',
  py:   '#22c55e',
  js:   '#f59e0b',
  rs:   '#ef4444',
  css:  '#7c3aed',
  json: '#94a3b8',
  java: '#f59e0b',
  JAVA:       '#f59e0b',
  PYTHON:     '#22c55e',
  JAVASCRIPT: '#4f7ef7',
};