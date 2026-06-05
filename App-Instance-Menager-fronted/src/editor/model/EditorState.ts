// src/editor/model/EditorState.ts

export type Language = 'ts' | 'py' | 'js' | 'rs' | 'css' | 'json';

export type LanguageType = Language;
export type CompileStatus = 'pending' | 'success' | 'error' | 'running';

export interface ProjectFile {
  name: string;
  ext: string;
  content?: string;
}

export interface SourceCode {
  id: string;
  name: string;
  content: string;
  language: LanguageType;
  createdAt: string;
}

export interface CompiledCode {
  id: string;
  sourceCodeId: string;
  status: CompileStatus;
  output: string;
  compiledAt: string;
}

export interface DeployedInstance {
  id: string;
  status: 'online' | 'offline' | 'busy';
  fileName: string;
  serverName: string;
  path: string;
  lang: string;
  logs?: string[];
}

export interface Project {
  id: number;
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
  ts: [
    { name: 'App.tsx',     ext: 'ts'   },
    { name: 'useStore.ts', ext: 'ts'   },
    { name: 'types.ts',    ext: 'ts'   },
    { name: 'styles.css',  ext: 'css'  },
  ],
  py: [
    { name: 'main.py',   ext: 'py' },
    { name: 'config.py', ext: 'py' },
    { name: 'utils.py',  ext: 'py' },
  ],
  js: [
    { name: 'index.js',    ext: 'js'   },
    { name: 'store.js',    ext: 'js'   },
    { name: 'config.json', ext: 'json' },
  ],
  rs: [
    { name: 'main.rs',    ext: 'rs'   },
    { name: 'router.rs',  ext: 'rs'   },
    { name: 'Cargo.toml', ext: 'json' },
  ],
  css:  [{ name: 'styles.css',  ext: 'css'  }],
  json: [{ name: 'config.json', ext: 'json' }],
};

export const SAMPLE_CODE: Record<string, string> = {
  ts: `import { useState } from 'react'

interface User {
  id: number
  name: string
  email: string
}

const fetchUsers = async (): Promise<User[]> => {
  const res = await fetch('/api/users')
  return res.json()
}

export default function App() {
  const [users, setUsers] = useState<User[]>([])
  return <div>{users.map(u => u.name)}</div>
}`,
  py: `from dataclasses import dataclass
from typing import Optional

@dataclass
class Config:
    host: str = "localhost"
    port: int = 8080
    debug: bool = False

def create_server(config: Optional[Config] = None) -> None:
    """Uruchom serwer HTTP."""
    if config is None:
        config = Config()
    print(f"Serwer na {config.host}:{config.port}")`,
  js: `// Moduł zarządzania stanem
const createStore = (reducer, initialState) => {
  let state = initialState
  const listeners = []

  return {
    getState: () => state,
    dispatch: (action) => {
      state = reducer(state, action)
      listeners.forEach(l => l())
    },
    subscribe: (fn) => listeners.push(fn),
  }
}`,
  rs: `use std::collections::HashMap;

#[derive(Debug)]
struct Router {
    routes: HashMap<String, String>,
}

impl Router {
    fn new() -> Self {
        Router { routes: HashMap::new() }
    }

    fn add_route(&mut self, path: &str, handler: &str) {
        self.routes.insert(path.to_string(), handler.to_string());
    }
}`,
  css: `/* Główne style aplikacji */
:root {
  --bg: #0f1117;
  --accent: #4f7ef7;
  --text: #e2e8f0;
}

body {
  font-family: 'DM Sans', sans-serif;
  background: var(--bg);
  color: var(--text);
}`,
  json: `{
  "name": "mój-projekt",
  "version": "1.0.0",
  "dependencies": {},
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build"
  }
}`,
};