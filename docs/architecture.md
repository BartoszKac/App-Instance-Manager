# Architecture – App Instance Manager

State of the code as of 14.10.2026 (after tasks 01–03). Describes what **is**, not what is planned; plans live in
`decisions.md` and `tasks/`.

## 1. Purpose

A web IDE and deployment console:

1. **Code** – write multi-file projects (Java, Python, C++, Bash), save them on the server, compile
   and run them, watch live output.
2. **Deploy** – register remote servers (SSH), transfer source/compiled code or uploaded
   executables to them.
3. **Execute** – run a command for a deployed program on a remote server.
4. **Process** – upload ready executables/scripts (multipart) and keep them in the DB.

## 2. Modules

```
Browser (React SPA, Vite dev server)
   │  REST  http://localhost:8888/api/**
   │  STOMP over SockJS  /ws-console  →  subscribe /topic/output
   ▼
Spring Boot (port 8888)
   ├─ code/      controller, service, entity, repository, mapper, enums, strategy (LanguageHandler + memory javac)
   ├─ deploy/    controller, service (SshSessionFactory → JSch), entity, repository, dto, enums, strategy (uploads)
   ├─ process/   controller, service, entity, repository
   ├─ shared/    config/WebSocketConfig, notification/FrontendNotificationService, persistence/DataBaseProvider
   ├─ legacy/    former controller/test + service/**/test, @Profile("dev") only
   └─ H2 file DB ./data/moja_baza; local runner code/service/LanguageProcess → /topic/output
Disk: ./app/<uuid>/   one folder per saved project (sources + compiled output)
```

## 3. Domain model (JPA entities)

| Entity | Sub-domain | Meaning |
|---|---|---|
| `SourceCode` | code | One source file; `idManClass` groups files of one project (the main file's id) |
| `CompiledCode` | code | Compiled artefact of a project |
| `SourceFolderInTheDisk` / `CompiledFolderInTheDisk` | file | Mapping project → folder under `app/` |
| `RemoteSerwerConfiguration` | deploy | SSH server: name, ip, user, **pass (plain text)**, OS |
| `RemoteProgramConfiguration` | deploy | A program deployed to a server |
| `RemoteExecutable` | process | Uploaded binary/script bytes + language |
| `ManagedService` | catalog | Service kept by the user: name, host, port, description |

"Project" is not an entity: it is the set of `SourceCode` rows sharing `idManClass`.

## 4. Key flows

**Save → compile → launch (local)**
`POST /api/code/source` (list of `SourceCode`) → files written to `app/<uuid>/` →
`POST /api/code/compile/all/main-class/{id}` → `LanguageHandler.getCompileCommand` →
`LanguageProcess` → output lines pushed to `/topic/output` →
`POST /api/code/launch/main-class/{id}` → `getRunCommand` → same runner.

**Deploy**
`POST /api/deploy/server` registers a server → `POST /api/deploy/transfer/bulk` with
`TransferTask{mainClassId, configurationId, uploadStrategyType}` → `FileUploadStrategyFactory`
picks `SourceCode`/`CompiledCode`/`RemoteExecutable` upload strategy → SFTP via JSch.

**Remote execute**
`POST /api/deploy/program/execute` with `{command, programConfigId}` → `ExecutionService` runs the
command over SSH and returns output as a string.

**LAN discovery**
`GET /api/deploy/network/subnets` → `LocalSubnetService` lists private subnets of the host's cards →
`POST /api/deploy/network/scan` → `NetworkScanService` starts a background `ScanJob`: nmap sweep, ports,
reverse DNS, service details, through `NmapRunner` → `LanguageProcess.captureOutput` → `useLanScan` polls
`GET /api/deploy/network/scan/current` → shown in `NetworkScanModal` (chosen ones saved through
`POST /api/deploy/server/bulk`) and in the Serwisy screen (chosen ones saved through `POST /api/services`).

**Server preview**
`ServerPreviewModal` → `GET /api/deploy/server-inventory/{id}` → `ServerInventoryService` →
`SshSessionFactory.executeBounded` runs `resources/inventory/*` → `InventoryOutputParser`; next to it
`POST /api/deploy/network/device-details` shows what the server exposes on the network.

**Server inspection** (ADR-014)
Saving a server with a password publishes `ServerCredentialsSavedEvent` → `InspectionService` saves a queued run →
`InspectionDispatcher` calls `App-Instance-Inspector` over gRPC (async, one instance per call, round robin) → the inspector
logs in over SSH and streams facts and file batches → `InspectionStreamObserver` → `InspectionStore` writes to H2 →
`admin/` screen polls `/api/inspections/**`. Replicas scale on `GET /api/inspections/load` through KEDA and an HPA (`k8s/`).

**Direct upload** (ADR-015)
Admin tab "Wyślij" → one `POST /api/transfers/servers/{id}/files` per file with a raw body → `DirectUploadService` streams it over
gRPC → inspector writes over SFTP. The backend stores only `DirectUploadRecord` metadata.

## 5. Frontend structure

Custom state router (`router/useRouter.ts`, screens: `projects`, `editor`, `deploy`, `execution`,
`upload`), `App.tsx` switches screens, `PackageTerminal` is always mounted at the bottom.
Each feature folder follows MVVM: `model/`, `viewmodel(s)/`, `views/`, `components/`, optional
`api/`.

## 6. Security surface

| Area | Current state | Risk |
|---|---|---|
| Local code execution | Any client can save and run arbitrary code on the server host | Full host compromise; intended feature, so it must never be exposed beyond localhost/trusted network |
| Process limits | `waitFor()` without timeout, no memory/CPU limit | Infinite loop blocks a thread forever |
| Remote execute | `program/execute` sends a client-supplied command over SSH | Arbitrary remote command execution by design |
| SSH host keys | `StrictHostKeyChecking=no` in `SshSessionFactory` and legacy code | MITM |
| Credentials | Passwords stored plain in H2, returned by `GET /api/deploy/servers`, also in `deploy-config.json` | Leak via API, DB file, git |
| CORS / WS | `@CrossOrigin("*")` per controller, `setAllowedOriginPatterns("*")` on `/ws-console` | Any website the user visits can drive the API |
| Auth | None | Anyone reaching port 8888 controls the app |
| Network scan | `POST /api/deploy/network/scan` starts nmap on a subnet from the backend's own cards or server config only (ADR-008, ADR-009, ADR-012); timeouts, limited concurrency, CORS limited to `localhost:5173`; nmap has `cap_net_raw` in the image | Reveals devices and service versions on the LAN to anyone reaching port 8888; non-browser clients are not limited by CORS; code run in the container can use nmap's raw sockets |
| Inspection | Logs in automatically to every server saved with a password; fixed read-only scripts, no `sudo`, no file contents; host keys pinned on first use (ADR-014) | Full listing of file names, accounts and packages of each server is readable by anyone reaching port 8888; passwords cross the internal gRPC network unencrypted |
| Direct upload | Writes client files to any path the stored SSH account can write, SFTP only, paths validated in the inspector, no overwrite by default (ADR-015) | Anyone reaching port 8888 can place or, with `overwrite`, replace files on registered servers |
| File names | Validated in `FileService` (`[A-Za-z0-9_-][A-Za-z0-9_.-]*`, resolved path must stay in the project folder); invalid → `400` | Mitigated for `POST /api/code/source`; legacy and upload endpoints not reviewed |

## 7. Known technical debt

- `legacy/**` (`@Profile("dev")`) still compiled; `dev` profile has a
  duplicate `POST /api/deploy/transfer/bulk` mapping.
- Entities returned directly from controllers; no DTO layer for most endpoints; no global exception
  handler; errors returned as `200` + string in places.
- `System.out.println` / `printStackTrace` in production code (8 files).
- Runtime folders `app/` and `data/` are tracked in git.
- Frontend: base URL hardcoded in 5 files; several files > 200 lines; no test runner;
  `styles/useExecutionViewModel.ts` misplaced; `GEMINI.md` API section outdated.
- Root `README.md` mentions Monaco Editor and Spring Boot 4 / Java 21 — the code uses
  `react-simple-code-editor` and Spring Boot 3.2.4 / Java 17.
- `idManClass` contract: compile/launch look up the **main file by its `SourceCode.id`**, so a
  project's `idManClass` must equal the id its main file receives on save. The frontend guesses it
  as `max(id) + 1` (`useProjects.ts`) — breaks under concurrent saves or deleted rows.
