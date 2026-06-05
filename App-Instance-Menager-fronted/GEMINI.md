# GEMINI.md — App Instance Manager: Frontend Developer Guide

> This file is the primary context document for AI-assisted development on the **App-Instance-Manager** project.
> It covers frontend architecture, backend API contracts, design patterns, and contribution conventions.
> Read this before making any changes to the codebase.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Repository Structure](#2-repository-structure)
3. [Frontend Architecture](#3-frontend-architecture)
   - [Design Pattern: MVVM](#31-design-pattern-mvvm)
   - [Module Breakdown](#32-module-breakdown)
   - [Routing](#33-routing)
4. [Backend API Contract](#4-backend-api-contract)
   - [Code Module](#41-code-module)
   - [Deploy Module](#42-deploy-module)
   - [Process Module](#43-process-module)
   - [WebSocket Notifications](#44-websocket-notifications)
5. [Data Models](#5-data-models)
6. [State Management](#6-state-management)
7. [How to Add New Features](#7-how-to-add-new-features)
8. [Conventions & Rules](#8-conventions--rules)
9. [Backend Domain Glossary](#9-backend-domain-glossary)

---

## 1. Project Overview

**App Instance Manager** is a full-stack application for:

- Writing, compiling, and running code (Java, Python, C++) dynamically at runtime.
- Deploying compiled programs to remote servers via SSH.
- Managing running processes on remote machines.

The **frontend** (`App-Instance-Menager-fronted/`) is a React + TypeScript SPA built with Vite. It communicates with a **Spring Boot backend** (`App-Instance-Manager/`) over REST and WebSocket.

---

## 2. Repository Structure

```
bartoszkac-app-instance-manager/
├── App-Instance-Manager/               # Spring Boot backend (Java)
│   └── src/main/java/.../DynamicCode/
│       ├── controller/                 # REST endpoints
│       ├── service/                    # Business logic
│       ├── databaseservice/            # DB access layer (not JPA repositories directly)
│       ├── repository/                 # Spring Data JPA repositories
│       ├── model/
│       │   ├── entity/                 # JPA entities (DB tables)
│       │   └── dto/                    # Data transfer objects
│       ├── strategy/                   # Strategy pattern implementations
│       ├── factory/                    # Factory pattern implementations
│       └── notification/              # WebSocket push service
│
└── App-Instance-Menager-fronted/       # React + TypeScript frontend
    └── src/
        ├── editor/                     # Code editor module
        ├── deploy/                     # Deployment module
        ├── execution/                  # Process execution module
        ├── router/                     # Client-side routing
        └── styles/                     # Global CSS
```

---

## 3. Frontend Architecture

### 3.1 Design Pattern: MVVM

The frontend strictly follows the **MVVM (Model–View–ViewModel)** pattern. Every feature module is split into three layers:

```
feature/
├── model/          → TypeScript interfaces/types (pure data shapes, no logic)
├── viewmodel/      → Custom React hooks (useXxxViewModel.ts) — all business logic lives here
└── views/          → React components that only render and delegate events to the ViewModel
    components/     → Sub-components used by the view (atoms, panels, cards)
```

**Rules:**
- **Model**: Only type definitions. No functions, no API calls, no React.
- **ViewModel**: All `fetch`/`axios` calls, state (`useState`, `useReducer`), side effects (`useEffect`), and derived data. Returns data + handlers to the View.
- **View**: Calls the ViewModel hook, destructures the result, renders JSX. Contains zero business logic.
- **Components**: Presentational only. Receive props, emit events via callbacks. They do not call ViewModel hooks.

**Example — correct pattern:**

```tsx
// model/EditorState.ts
export interface SourceFile {
  id: string;
  name: string;
  content: string;
  language: LanguageType;
}

// viewmodels/useEditor.ts
export function useEditor() {
  const [files, setFiles] = useState<SourceFile[]>([]);
  const saveFile = async (file: SourceFile) => {
    await fetch('/api/code/save', { method: 'POST', body: JSON.stringify(file) });
    setFiles(prev => [...prev.filter(f => f.id !== file.id), file]);
  };
  return { files, saveFile };
}

// views/EditorView.tsx
export function EditorView() {
  const { files, saveFile } = useEditor();
  return <CodeView files={files} onSave={saveFile} />;
}
```

---

### 3.2 Module Breakdown

#### `editor/` — Code Editor

Handles source code projects: creating, editing, and compiling code.

| File | Responsibility |
|---|---|
| `views/EditorView.tsx` | Main editor layout (sidebar + code panel + status bar) |
| `views/ProjectsView.tsx` | Project list / dashboard |
| `viewmodels/useEditor.ts` | Active file state, compile trigger, output handling |
| `viewmodels/useProjects.ts` | Project CRUD, language selection |
| `model/EditorState.ts` | `SourceFile`, `Project`, `CompileResult` types |
| `components/CodeView.tsx` | Monaco/textarea code editor component |
| `components/Sidebar.tsx` | File tree navigation |
| `components/TabBar.tsx` | Open file tabs |
| `components/ExecutionPanel.tsx` | Compilation output / console |
| `components/StatusBar.tsx` | Language, cursor position, compile status |
| `components/Topbar.tsx` | Actions toolbar |
| `components/NewProjectModal.tsx` | Create project dialog |
| `components/ProjectCard.tsx` | Card in the projects list |

#### `deploy/` — SSH Deployment

Handles configuring remote servers and deploying compiled code via SSH.

| File | Responsibility |
|---|---|
| `views/DeployView.tsx` | Three-panel deployment workspace |
| `viewmodel/useDeployViewModel.ts` | Server selection, file selection, deploy action, transfer status |
| `model/DeployState.ts` | `RemoteServer`, `DeployTask`, `TransferStatus` types |
| `components/ServersPanel.tsx` | Left panel: list of configured SSH servers |
| `components/FilesPanel.tsx` | Right panel: compiled files available to deploy |
| `components/CenterPanel.tsx` | Central panel: deploy configuration + action button |
| `components/DeployAtoms.tsx` | Shared small UI components (badges, icons, status chips) |

#### `execution/` — Process Management

Monitors and controls running processes on remote machines.

| File | Responsibility |
|---|---|
| `views/ExecutionView.tsx` | Process list + live output viewer |
| `viewmodels/useExecutionViewModel.ts` | Poll/subscribe for process status, kill action |
| `model/useExecutionViewModel.ts` | `RunningProcess`, `ProcessStatus` types |

#### `router/` — Navigation

```ts
// router/useRouter.ts
// Provides navigate(route) and currentRoute
// Routes: '/' → ProjectsView, '/editor' → EditorView,
//         '/deploy' → DeployView, '/execution' → ExecutionView
```

---

### 3.3 Routing

The app uses a **custom lightweight router** (`router/useRouter.ts`) — not React Router. Navigation is done by calling `navigate(path)` from the hook.

When adding a new view, register the route in `router/index.ts` and add a case in `App.tsx`.

---

## 4. Backend API Contract

Base URL: `http://localhost:8080` (configurable in `vite.config.ts` proxy).

All request/response bodies are JSON. All endpoints return standard HTTP status codes.

---

### 4.1 Code Module

**Controller:** `controller/code/CodeController.java`

#### Upload / Save Source Code

```
POST /api/code/source
Content-Type: application/json

Body: {
  "name": string,          // file name, e.g. "Main.java"
  "content": string,       // raw source code
  "language": "JAVA" | "PYTHON" | "CPP"
}

Response 200: {
  "id": string (UUID),
  "name": string,
  "language": string,
  "createdAt": string (ISO-8601)
}
```

#### Get All Source Files

```
GET /api/code/source

Response 200: SourceCode[]
```

#### Compile Code

```
POST /api/code/compile/{sourceCodeId}

Response 200: {
  "id": string (UUID),      // compiled code ID
  "sourceCodeId": string,
  "status": "SUCCESS" | "FAILURE",
  "output": string,         // compiler stdout/stderr
  "compiledAt": string
}
```

**Backend internals (for context):**
The backend uses a **Strategy pattern** (`strategy/language/`) with `LanguageHandler` as the interface. `JavaHandler`, `PythonHandler`, and `CppHandler` implement compile and run logic per language. A `LanguageHandlerFactory` resolves the correct strategy from a `LanguageType` enum.

For Java, compilation is done **in-memory** (no temp files) using `javax.tools.JavaCompiler` via custom `MemoryJavaFileManager`, `MemorySourceFile`, and `MemoryByteCodeFile` classes.

---

#### Launch Compiled App (local run)

```
POST /api/code/launch/{compiledCodeId}

Response 200: {
  "processId": string,
  "status": "RUNNING",
  "startedAt": string
}
```

---

### 4.2 Deploy Module

**Controller:** `controller/deploy/DeployController.java`

#### List Remote Servers

```
GET /api/deploy/servers

Response 200: RemoteSerwerConfiguration[]
{
  "id": string (UUID),
  "host": string,
  "port": number,
  "username": string,
  "operationSystem": "LINUX" | "WINDOWS"
}
```

#### Add Remote Server

```
POST /api/deploy/servers
Body: {
  "host": string,
  "port": number,
  "username": string,
  "password": string,       // sent only on create, never returned
  "operationSystem": "LINUX" | "WINDOWS"
}

Response 201: RemoteSerwerConfiguration (without password)
```

#### List Deployable Programs

```
GET /api/deploy/programs

Response 200: RemoteProgramConfiguration[]
{
  "id": string (UUID),
  "name": string,
  "compiledCodeId": string,
  "uploadStrategy": "COMPILED_CODE" | "SOURCE_CODE" | "REMOTE_EXECUTABLE"
}
```

#### Deploy Program to Server

```
POST /api/deploy/transfer
Body: {
  "serverId": string,
  "programId": string
}

Response 202: {
  "taskId": string,
  "status": "PENDING"
}
```

The actual transfer is **asynchronous**. Progress is pushed via WebSocket (see §4.4).

**Backend internals:**
File transfer uses the **Strategy pattern** (`strategy/sshfiiletransfer/`) via `FileUploadStrategy` interface. Strategies: `CompiledCodeUploadStrategy`, `SourceCodeUploadStrategy`, `RemoteExecutableUploadStrategy`. The strategy is selected by `FileUploadStrategyFactory` based on `UploadStrategyType` enum.

SSH sessions are created by `SshSessionFactory` (JSch/Apache MINA SSHD).

---

### 4.3 Process Module

**Controller:** `controller/process/ProcessController.java`

#### List Running Processes

```
GET /api/process

Response 200: RemoteExecutable[]
{
  "id": string (UUID),
  "name": string,
  "serverId": string,
  "pid": number | null,
  "status": "RUNNING" | "STOPPED" | "ERROR",
  "startedAt": string
}
```

#### Kill Process

```
DELETE /api/process/{processId}

Response 200: { "status": "STOPPED" }
```

#### Get Process Output

```
GET /api/process/{processId}/output

Response 200: {
  "processId": string,
  "output": string    // stdout accumulated so far
}
```

---

### 4.4 WebSocket Notifications

**Config:** `configuration/WebSocketConfig.java`  
**Service:** `notification/FrontendNotificationService.java`

The backend pushes real-time updates over WebSocket (STOMP over SockJS).

**Endpoint:** `ws://localhost:8080/ws`

**Topics to subscribe to from frontend:**

| Topic | Payload | When |
|---|---|---|
| `/topic/deploy/status` | `{ taskId, status, progress, message }` | During SSH file transfer |
| `/topic/process/output` | `{ processId, chunk }` | Live stdout from running process |
| `/topic/process/status` | `{ processId, status }` | Process state change |
| `/topic/compile/result` | `{ sourceCodeId, status, output }` | Compilation finished |

**Frontend usage pattern:**

```ts
// In a ViewModel hook
useEffect(() => {
  const client = new Client({ brokerURL: 'ws://localhost:8080/ws' });
  client.onConnect = () => {
    client.subscribe('/topic/deploy/status', (msg) => {
      const payload = JSON.parse(msg.body);
      setTransferStatus(payload);
    });
  };
  client.activate();
  return () => client.deactivate();
}, []);
```

Use `@stomp/stompjs` library. Subscribe in ViewModel hooks, never in View components.

---

## 5. Data Models

These TypeScript types should mirror the backend DTOs/entities. Define them in the relevant `model/` directory.

```ts
// Shared / global types (can live in a shared types file)

export type LanguageType = 'JAVA' | 'PYTHON' | 'CPP';
export type OperationSystem = 'LINUX' | 'WINDOWS';
export type UploadStrategyType = 'COMPILED_CODE' | 'SOURCE_CODE' | 'REMOTE_EXECUTABLE';
export type ProcessStatus = 'RUNNING' | 'STOPPED' | 'ERROR';
export type CompileStatus = 'SUCCESS' | 'FAILURE';
export type TransferStatus = 'PENDING' | 'IN_PROGRESS' | 'DONE' | 'FAILED';

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

export interface RemoteSerwerConfiguration {
  id: string;
  host: string;
  port: number;
  username: string;
  operationSystem: OperationSystem;
}

export interface RemoteProgramConfiguration {
  id: string;
  name: string;
  compiledCodeId: string;
  uploadStrategy: UploadStrategyType;
}

export interface RemoteExecutable {
  id: string;
  name: string;
  serverId: string;
  pid: number | null;
  status: ProcessStatus;
  startedAt: string;
}

export interface TransferTask {
  taskId: string;
  status: TransferStatus;
  progress?: number;
  message?: string;
}
```

---

## 6. State Management

There is **no global state library** (no Redux, no Zustand). State is local to ViewModel hooks using `useState` and `useReducer`.

**When to use what:**
- `useState` — simple values (selected ID, loading flag, form input).
- `useReducer` — complex state with multiple related fields (e.g. editor state with files + active tab + dirty flag).
- `useEffect` — data fetching on mount, WebSocket subscriptions, cleanup.
- Prop drilling — acceptable up to 2 levels. Beyond that, extract a shared hook or use React Context.

**Do not** introduce a global state library without discussion. The current pattern scales well for this app size.

---

## 7. How to Add New Features

### Adding a new View/Page

1. Create `src/featureName/` directory.
2. Add `model/FeatureState.ts` — TypeScript types only.
3. Add `viewmodels/useFeatureViewModel.ts` — logic, API calls, state.
4. Add `views/FeatureView.tsx` — renders using the ViewModel hook.
5. Add `views/index.ts` — re-export the view.
6. Register the route in `router/index.ts`.
7. Add the case in `App.tsx`.

### Adding a new API call

1. Identify the backend controller (`controller/` directory).
2. Check the HTTP method, path, request body shape, and response shape.
3. Add the fetch call inside the relevant ViewModel hook — never in a View component.
4. Define or extend the TypeScript type in the module's `model/` file.

### Adding a new backend language handler

On the backend, implement `LanguageHandler` interface, add the enum value to `LanguageType.java`, and register in `LanguageHandlerFactory`. On the frontend, add the new value to the `LanguageType` union type and add it to any language selector UI.

### Adding a new SSH upload strategy

On the backend, implement `FileUploadStrategy`, add to `UploadStrategyType.java` enum, register in `FileUploadStrategyFactory`. On the frontend, add the value to `UploadStrategyType` union and update any deployment configuration UI.

---

## 8. Conventions & Rules

### Naming

| Thing | Convention | Example |
|---|---|---|
| ViewModel hooks | `useXxxViewModel` or `useXxx` | `useDeployViewModel` |
| View components | `XxxView` | `DeployView` |
| Model files | `XxxState.ts` | `DeployState.ts` |
| Sub-components | PascalCase, descriptive | `ServersPanel`, `ProjectCard` |
| Types/Interfaces | PascalCase | `RemoteExecutable` |
| Enum-like union types | `'UPPER_CASE'` strings | `'RUNNING' \| 'STOPPED'` |

### File Structure

- One component per file.
- Each module directory has an `index.ts` that re-exports its public surface.
- Do not import from `../otherModule/components/Foo` — import from `../otherModule` (through the index).

### API Calls

- All `fetch` / `axios` calls live in ViewModel hooks.
- Always handle loading and error states.
- Never mutate backend response objects directly — map them to local state types.

### CSS / Styling

- Global styles in `styles/global.css`.
- Component-level styles in `App.css` or co-located `.css` files.
- Use CSS custom properties for any repeated colors/spacing.

### TypeScript

- **No `any`**. Use `unknown` and narrow, or define a proper type.
- Enable strict mode (already configured in `tsconfig.app.json`).
- Prefer `interface` for object shapes, `type` for unions and primitives.

---

## 9. Backend Domain Glossary

Understanding backend naming avoids confusion when reading API responses:

| Backend Term | Meaning |
|---|---|
| `SourceCode` | A source file stored in the DB (Java/Python/C++ text) |
| `CompiledCode` | The result of compiling a `SourceCode` |
| `RemoteSerwerConfiguration` | SSH server credentials + metadata (`Serwer` = Polish for "Server") |
| `RemoteProgramConfiguration` | A deployable program definition — links compiled code to a deployment strategy |
| `RemoteExecutable` | A running (or past) process instance on a remote server |
| `TransferTask` | An async file-transfer job dispatched to a remote server |
| `LanguageType` | Enum: `JAVA`, `PYTHON`, `CPP` |
| `UploadStrategyType` | Enum: `COMPILED_CODE`, `SOURCE_CODE`, `REMOTE_EXECUTABLE` |
| `OperationSystem` | Enum: `LINUX`, `WINDOWS` — affects path separators and run commands on remote |
| `DataBaseProvider` | Backend service layer between repositories and business services |
| `FrontendNotificationService` | Spring service that pushes events to WebSocket topics |

> **Note on spelling:** The codebase uses `Serwer` (Polish) instead of `Server` in several class and variable names (`RemoteSerwerConfiguration`, `SerwerConfigurationService`). Match this spelling exactly when referencing backend class names.

---

*Last updated: generated from repository structure analysis. Keep this file in sync when adding new modules, endpoints, or patterns.*
