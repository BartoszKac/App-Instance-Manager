# Task 09 – Send the right file and report what really happened

| Type | Module | Branch |
|---|---|---|
| 🐛 fix | backend + frontend | `task-09-reliable-file-transfer` |

## 🎯 Goal
Screen Wysyłka can send another project's code, damages Polish characters, and shows ✅ even when nothing was sent.
Afterwards the file sent is the one chosen, its bytes arrive unchanged, and every failure is visible.

## 📋 Scope
All findings below come from reading the code. None was reproduced on a running system yet.

- [ ] Uploaded files always go as `REMOTE_EXECUTABLE`. `useDeployFiles.ts:16` uses `defaultStrategy(language)`, so a `RemoteExecutable.id` is looked up as a project's `idManClass`.
- [ ] Projects no longer offer `REMOTE_EXECUTABLE` (same id collision in the other direction).
- [ ] `FileUploadStrategy.resolveFiles` returns `Map<String, byte[]>`. Source is encoded as UTF-8, compiled and uploaded files stay raw. Remove the Base64 and ISO-8859-1 conversions from the strategies and from `SshFileTransferService.resolveFileBytes`.
- [ ] No files to send throws a new `NoFilesToTransferException` instead of `warn` + `return` (`SshFileTransferService.java:51-54`).
- [ ] `EnviromentTransferService.processSingleTask` stops swallowing failures. `POST /api/deploy/transfer/bulk` returns one result per task.
- [ ] `RemoteProgramConfiguration` is saved only after a successful upload, so a failed transfer creates no card on screen Uruchom.
- [ ] `useDeployRun.ts` shows ✅ or ✗ per file and server from the response, not from the HTTP status alone.
- [ ] Names of uploaded files are validated (see 🧩). An upload with an unsafe name gets `400`, and an existing row with an unsafe name fails its transfer.
- [ ] Remove `System.out.println`, comments and JavaDoc from touched classes (backend `CLAUDE.md`).

## 🚫 Out of scope
Listing metadata and the new file list (task 10). Run commands and `chmod` (task 11). C++ binaries in `compiled_codes`.
Hiding passwords, host key checking, `@CrossOrigin` on `ProcessController`.

## 🔗 Dependencies
None.

## 🧩 Code patterns
Response of the bulk endpoint, a new record in `deploy/dto`:
```java
public record TransferResult(Long mainClassId, Long configurationId, boolean success, String message) {}
```
`200` with a per-task list, because one bulk request can partly succeed. `message` is a fixed Polish text chosen by failure
type, never `e.getMessage()`: SSH exceptions can carry hosts and paths.

File name rule in `process/service`, applied in `SavingProcessInSystemService` so `/upload`, `/bulk` and `PUT /{id}` all pass it:
reject null, blank, `.`, `..`, any `/` or `\`, control characters, and a leading `-`. Spaces and Polish letters stay
allowed, so `FileService.SAFE_FILE_NAME` is not reused (`process` may not import it either).

## ⚠️ Pitfalls
- 🔒 `SshFileTransferService` builds `normalizedDir + fileName`, and `ProcessController` takes the name from `getOriginalFilename()` unchecked. That is a remote path traversal today.
- `.class`/`.jar` decoding is a guess by extension. A raw uploaded `.jar` only works because Base64 decoding fails on it. Do not keep this behaviour.
- `CompiledCodeUploadStrategy` has a merge function for duplicate names, the two other strategies do not. Keep `toMap` from throwing on duplicates.
- `legacy/**` has its own `POST /api/deploy/transfer/bulk`. Do not change it. Check that it does not use the changed interface.

## ✅ Acceptance criteria
- [ ] An uploaded file with the same id as a project sends the uploaded file, not the project.
- [ ] A source file containing `zażółć` arrives with identical UTF-8 bytes.
- [ ] A binary with bytes above `0x7F` and a `.jar` arrive byte for byte.
- [ ] `Skompilowany` for a project with no compiled files gives ✗ in the log and adds no card on Uruchom.
- [ ] One failed server does not stop the others, and the log shows which one failed.
- [ ] Upload with the name `../../x` gets `400`.

## 🧪 Tests
Backend `./mvnw test`, with mocked SSH:
`SshFileTransferServiceTest` (bytes unchanged, no files throws), `SourceCodeUploadStrategyTest` (UTF-8),
`RemoteExecutableUploadStrategyTest` (raw bytes), `EnviromentTransferServiceTest` (partial failure, no configuration saved
on failure), `SavingProcessInSystemServiceTest` (name rule), `ProcessControllerTest` (`400`).
Frontend: `npm run build`, `npm run lint`, manual check of Wysyłka. Manual transfer against a throwaway SSH container.

## 🏁 Definition of Done
- [ ] Acceptance criteria met, tests green
- [ ] `docs/api.md` updated (bulk response, `400` on upload), frontend caller updated
- [ ] `docs/architecture.md` security table updated
- [ ] Final report lists changed files, the security changes, and pre-existing violations found

## 📚 References
`docs/architecture.md` → Deploy flow and Security surface. `App-Instance-Manager/CLAUDE.md` sections 5, 8, 10.

## 💬 Open questions
- `RemoteProgramService.saveAllConfigurationsToDb` matches on `idCode` + strategy, without `idSerwer`. Sending one file to two servers appears to leave only the last server on Uruchom. Working assumption: not fixed here, gets its own task.
