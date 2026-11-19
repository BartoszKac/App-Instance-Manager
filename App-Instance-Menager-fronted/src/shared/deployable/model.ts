export type UploadStrategyType = 'SOURCE_CODE' | 'COMPILED_CODE' | 'REMOTE_EXECUTABLE';

export type LanguageType = 'JAVA' | 'PYTHON' | 'CPP' | 'BASH' | 'UNKNOWN';

export interface ProjectFile {
  mainClassId: number;
  name: string;
  language: LanguageType;
  compiledFileCount: number;
}

export interface UploadedFile {
  id: number;
  name: string;
  language: LanguageType;
  description: string | null;
  sizeBytes: number | null;
  uploadedAt: string | null;
}

export interface DeployableFiles {
  projects: ProjectFile[];
  uploads: UploadedFile[];
}
