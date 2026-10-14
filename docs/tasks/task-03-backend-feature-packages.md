# Task 03 – Move backend to feature-first packages

**Status: ✅ done 14.10.2026**

| Type | Module | Branch |
|---|---|---|
| ♻️ refactor | backend | `task-03-backend-feature-packages` |

## 🎯 Goal
Apply ADR-004: every class lives in its feature package (`code`, `deploy`, `process`, `shared`,
`legacy`), so a feature can be read, changed and tested in one place.

## 📋 Scope
Mechanical move only — **no behaviour change**. Package declarations and imports change; class
bodies do not, except where a typo fix in the class name is listed below.

Class map (root `com.example.DynamicCode`):

| From | To |
|---|---|
| `controller/code/CodeController` | `code/controller/CodeController` |
| `databaseservice/code/SourceCodeService`, `CompiledCodeService` | `code/service/` |
| `databaseservice/file/SourceFolderInTheDiskService`, `CompileFolderInTheDiskService` | `code/service/` |
| `service/code/compilation/CompilationService` | `code/service/` |
| `service/code/launcher/AppLauncherService` | `code/service/` |
| `service/code/prcoces/LanguageProces` | `code/service/LanguageProcess` |
| `service/code/provider/ProviderCodeService` | `code/service/` |
| `service/file/SavingDataInSystemService` | `code/service/` |
| `fileutil/FileService` | `code/service/` |
| `model/entity/code/*`, `model/entity/file/*` | `code/entity/` |
| `model/dto/file/CodeFileDto` | `code/dto/` |
| `repository/code/*`, `repository/file/*` | `code/repository/` |
| `mapper/code/MapperCode`, `mapper/file/MapperFile` | `code/mapper/` |
| `constants/code/LanguageType` | `code/enums/` |
| `constants/code/CompileConstats` | `code/enums/CompileConstants` |
| `strategy/language/*`, `factory/code/langugage/LanguageHandlerFactory` | `code/strategy/` |
| `strategy/memory/*` | `code/strategy/memory/` |
| `controller/deploy/DeployController` | `deploy/controller/` |
| `databaseservice/deploy/RemoteSerwerService`, `RemoteProgramService` | `deploy/service/` |
| `service/deploy/connection/SshSessionFactory` | `deploy/service/` |
| `service/deploy/execution/ExecutionService` | `deploy/service/` |
| `service/deploy/provider/ProviderDeployService` | `deploy/service/` |
| `service/deploy/serwer/SerwerConfigurationService` | `deploy/service/` |
| `service/deploy/storage/RemoteDirectoryService` | `deploy/service/` |
| `service/deploy/transport/*` | `deploy/service/` |
| `model/entity/deploy/*` | `deploy/entity/` |
| `model/dto/deploy/ExecuteDto`, `TransferTask` | `deploy/dto/` |
| `repository/deploy/*` | `deploy/repository/` |
| `constants/deploy/OperationSystem`, `UploadStrategyType` | `deploy/enums/` |
| `strategy/sshfiiletransfer/*`, `factory/deploy/sshfiiletransfer/FileUploadStrategyFactory` | `deploy/strategy/` |
| `controller/process/ProcessController` | `process/controller/` |
| `databaseservice/procces/RemoteExecutableService` | `process/service/` |
| `service/procces/SavingProccesInSytemService` | `process/service/SavingProcessInSystemService` |
| `model/entity/procces/RemoteExecutable` | `process/entity/` |
| `repository/procces/RemoteExecutableRepository` | `process/repository/` |
| `configuration/WebSocketConfig` | `shared/config/` |
| `notification/FrontendNotificationService` | `shared/notification/` |
| `databaseservice/DataBaseProvider` | `shared/persistence/` (used by code, process and deploy) |
| `controller/test/**`, `service/**/test/**`, `service/test/util/ProcessMenager`, `model/dto/proces/ProcesDefault` | `legacy/` (keep sub-structure flat: `legacy/controller`, `legacy/service`, `legacy/dto`) |

- [ ] Move classes per the map (IDE refactor or `git mv` + package/import fix).
- [ ] Move existing tests to mirror the new packages.
- [ ] Remove the now-empty layer packages.
- [ ] Cross-feature imports allowed after the move: `deploy` → `code.service`, `code.entity`
      (source/compiled code for upload) and `process.service`, `process.entity`
      (`RemoteExecutableUploadStrategy`). Record them in `docs/architecture.md`.
- [ ] Update `docs/architecture.md` section 2 and the paths in `App-Instance-Manager/CLAUDE.md`
      sections 7–8.

## 🚫 Out of scope
Renaming entity classes (would rename tables), changing endpoints, DTO introduction, splitting
large classes, deleting legacy code, fixing other typos than those listed in the map.

## 🔗 Dependencies
Task 01 (build must compile to verify the move).

## ⚠️ Pitfalls
- `deploy` reading `code` and `process` entities directly is allowed only as listed above; do not
  add more cross-feature imports during the move.
- `factory/deploy/provider/ProgramLoaderFactory` has no users — list it in the report; deleting it
  is a separate decision.
- Spring component scan starts at `DynamicCodeApplication` in the root package — keep it there.
- `@Profile("dev")` duplicate `POST /api/deploy/transfer/bulk` stays as is (out of scope), noted.

## ✅ Acceptance criteria
- [ ] No top-level `controller`, `service`, `databaseservice`, `repository`, `model`, `factory`,
      `strategy`, `constants`, `mapper`, `fileutil`, `configuration`, `notification` packages remain.
- [ ] `./mvnw test` result identical to before the move (same tests pass/fail).
- [ ] App starts and the frontend works against it (same endpoints).
- [ ] Existing H2 data still loads (table names unchanged).

## 🧪 Tests
`./mvnw test` before and after; manual smoke test: list projects, compile, launch, list servers.

## 🏁 Definition of Done
- [ ] Acceptance criteria met
- [ ] `docs/architecture.md` updated with the new tree and allowed cross-feature imports
- [ ] "Current state and migration" paragraph in `App-Instance-Manager/CLAUDE.md` section 2 removed
