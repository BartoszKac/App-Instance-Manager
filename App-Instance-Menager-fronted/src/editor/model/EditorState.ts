// src/editor/model/EditorState.ts

export type LanguageType = 'JAVA' | 'PYTHON' | 'CPP' | 'BASH' | 'UNKNOWN';
export type Language = 'java' | 'py' | 'cpp' | 'sh';
export type CompileStatus = 'pending' | 'success' | 'error' | 'running';

export const EXT_TO_LANGUAGE: Record<string, LanguageType> = {
  java: 'JAVA',
  py:   'PYTHON',
  cpp:  'CPP',
  sh:   'BASH',
};

export const LANGUAGE_TO_EXT: Record<LanguageType, string> = {
  JAVA:    'java',
  PYTHON:  'py',
  CPP:     'cpp',
  BASH:    'sh',
  UNKNOWN: 'txt',
};

// Domyślna nazwa pliku dla języka
export const LANG_DEFAULT_FILENAME: Record<Language, string> = {
  java: 'Main.java',
  py:   'main.py',
  cpp:  'main.cpp',
  sh:   'main.sh',
};

export interface ProjectFile {
  id: number;           // 0 = nowy, >0 = zapisany na backendzie
  name: string;
  ext: string;
  content: string;
  language: LanguageType;
}

// Kształt JSON który przyjmuje backend
export interface SourceCode {
  id: number;
  idManClass: number;   // literówka z backendu — Man nie Main
  name: string;
  code: string;         // backend używa "code" nie "content"
  language: LanguageType;
}

export interface CompiledCode {
  id: number;
  idManClass: number;
  sourceCodeId: string;
  status: CompileStatus;
  output: string;
  compiledAt: string;
}

export interface Project {
  id: number;           // = idManClass na backendzie
  name: string;
  desc: string;
  lang: Language;
  files: ProjectFile[];
  created: string;
}

export interface EditorState {
  activeFile: ProjectFile | null;
  openTabs: ProjectFile[];
}

export interface ProjectsState {
  projects: Project[];
  filter: Language | 'all';
  searchQuery: string;
}

export const INITIAL_EDITOR_STATE: EditorState = {
  activeFile: null,
  openTabs: [],
};

export const INITIAL_PROJECTS_STATE: ProjectsState = {
  projects: [],
  filter: 'all',
  searchQuery: '',
};

export const DEFAULT_FILES: Record<Language, ProjectFile[]> = {
  java: [],
  py:   [],
  cpp:  [],
  sh:   [],
};