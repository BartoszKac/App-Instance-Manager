// ─── TYPES ────────────────────────────────────────────────────────────────────

export type ServerStatus = 'online' | 'offline' | 'unknown';

export interface DeployFile {
  name: string;
  ext: string;
}

export interface DeployProject {
  id: number;
  name: string;
  lang: string;
  files: DeployFile[];
}

export interface DeployServer {
  id: string;
  name: string;
  host: string;
  port: number;
  user: string;
  status: ServerStatus;
}

export interface SelectedFileEntry {
  project: DeployProject;
  file: DeployFile;
}

export interface NewServerForm {
  name: string;
  host: string;
  port: string;
  user: string;
}

// ─── CONSTANTS ────────────────────────────────────────────────────────────────

export const MOCK_PROJECTS: DeployProject[] = [
  {
    id: 1,
    name: 'frontend-app',
    lang: 'ts',
    files: [
      { name: 'App.tsx',      ext: 'ts'  },
      { name: 'useStore.ts',  ext: 'ts'  },
      { name: 'styles.css',   ext: 'css' },
      { name: 'types.ts',     ext: 'ts'  },
    ],
  },
  {
    id: 2,
    name: 'api-server',
    lang: 'py',
    files: [
      { name: 'main.py',   ext: 'py' },
      { name: 'config.py', ext: 'py' },
      { name: 'utils.py',  ext: 'py' },
    ],
  },
  {
    id: 3,
    name: 'cli-tool',
    lang: 'rs',
    files: [
      { name: 'main.rs',    ext: 'rs'   },
      { name: 'router.rs',  ext: 'rs'   },
      { name: 'Cargo.toml', ext: 'json' },
    ],
  },
];

export const INITIAL_SERVERS: DeployServer[] = [
  { id: 's1', name: 'Production', host: 'prod.example.com',    port: 22,   user: 'deploy', status: 'online'  },
  { id: 's2', name: 'Staging',    host: 'staging.example.com', port: 22,   user: 'ubuntu', status: 'online'  },
  { id: 's3', name: 'Dev Server', host: '192.168.1.10',        port: 2222, user: 'root',   status: 'offline' },
];

export const EXT_COLOR: Record<string, string> = {
  ts:   '#4f7ef7',
  py:   '#22c55e',
  js:   '#f59e0b',
  rs:   '#ef4444',
  css:  '#7c3aed',
  json: '#94a3b8',
};

export const EMPTY_NEW_SERVER: NewServerForm = {
  name: '',
  host: '',
  port: '22',
  user: 'deploy',
};
