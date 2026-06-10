export enum LanguageType {
  JAVA    = 'JAVA',
  PYTHON  = 'PYTHON',
  CPP     = 'CPP',
  BASH    = 'BASH',
  UNKNOWN = 'UNKNOWN',
}

export interface RemoteExecutable {
  id?: number;
  name: string;
  language: LanguageType;
}

export interface UploadState {
  status: 'idle' | 'uploading' | 'success' | 'error';
  progress: number;
  message: string | null;
  uploadedFile: RemoteExecutable | null;
}

export const LANGUAGE_LABELS: Record<LanguageType, string> = {
  [LanguageType.JAVA]:    'Java (.java)',
  [LanguageType.PYTHON]:  'Python (.py)',
  [LanguageType.CPP]:     'C++ (.cpp)',
  [LanguageType.BASH]:    'Bash (.sh)',
  [LanguageType.UNKNOWN]: 'Inny / nieznany',
};