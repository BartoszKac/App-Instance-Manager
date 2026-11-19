# Task 10 – Readable file list on screen Wysyłka

| Type | Module | Branch |
|---|---|---|
| ✨ feature | backend + frontend | `task-10-readable-deploy-files` |

## 🎯 Goal
Today the user must remember what each file is and pick one of three categories for it. Afterwards the list says what a
file is, offers only choices that work, and shows how it will be run.

## 📋 Scope
Approved by the user on 05.11.2026: three new nullable columns in `remote_executable`.

- [ ] `RemoteExecutable` gets `description`, `sizeBytes`, `uploadedAt`. Set on `POST /api/process/upload`.
- [ ] `POST /api/process/upload` takes an optional `description`, at most 200 characters, otherwise `400`.
- [ ] Upload form gets the field "Do czego służy". "Rodzaj pliku" is preselected from the file extension and stays editable.
- [ ] New `GET /api/deploy/deployable-files` returns projects and uploads without file contents.
- [ ] `FilesPanel`: uploaded files have no strategy choice and show description, size and date.
- [ ] `FilesPanel`: projects show `Kod` / `Program`. `Program` appears only for a language that compiles here (Java), and is disabled with "Najpierw skompiluj" while there are no compiled files.
- [ ] A Java project with compiled files defaults to `Program`, every other project to `Kod`.
- [ ] Each row shows the run command as a hint, from a lookup map on language, strategy and extension.
- [ ] `DeployState.ts` and the touched `process/` files follow the frontend `CLAUDE.md` (no comments, base URL from `shared/`).

## 🚫 Out of scope
Renaming `UploadStrategyType` values (only labels change). Removing or changing `GET /api/process`. Editing a description
after upload. C++ binaries in `compiled_codes`, so C++ offers only `Kod`. The Uruchom screen (task 11).

## 🔗 Dependencies
Task 09, which removes `REMOTE_EXECUTABLE` from projects and fixes the strategy of uploaded files.

## 🧩 Code patterns
```java
public record DeployableFilesResponse(List<ProjectFile> projects, List<UploadedFile> uploads) {
    public record ProjectFile(Long mainClassId, String name, LanguageType language, long compiledFileCount) {}
    public record UploadedFile(Long id, String name, LanguageType language, String description, Long sizeBytes, Instant uploadedAt) {}
}
```
Read uploads with a JPQL constructor projection in `RemoteExecutableRepository`, and compiled counts with one
`group by idManClass` query. Loading entities would read every blob into memory and turn counts into N+1 queries.

Labels stay a lookup map: `SOURCE_CODE` → `Kod`, `COMPILED_CODE` → `Program`.

## ⚠️ Pitfalls
- Existing rows have `null` in all three columns. The UI hides what is missing and shows no placeholder such as "0 B".
- `ddl-auto=update` adds nullable columns. Use `Long` and `Instant`, never `long`. Back up `data/` before the first run on real data.
- The new projection records are a new cross-feature import for `deploy`. Add them to the table in backend `CLAUDE.md` section 2 and record it in `docs/decisions.md`.
- `sizeBytes` and `uploadedAt` are set by the server. `/bulk` and `PUT /{id}` take the entity, so they must not be able to overwrite these values on an update.
- Verify whether `SourceCode.name` contains the extension before building hints: `FileService` writes it as the file name, while `LanguageHandler.getRunCommand` appends an extension.
- Selected strategies are stored by file key. A strategy that is no longer valid must fall back to the default.
- `FilesPanel.tsx` and the view-models keep the 20-line and 200-line limits.

## ✅ Acceptance criteria
- [ ] An uploaded file shows its description, size and date, and has no category choice.
- [ ] A row uploaded before this change shows its name and language, without errors.
- [ ] A Java project without compiled files cannot select `Program`, and the reason is visible.
- [ ] After compiling, returning to Wysyłka makes `Program` selectable.
- [ ] A Python or Bash project shows only `Kod`.
- [ ] The `GET /api/deploy/deployable-files` response contains no file contents.

## 🧪 Tests
Backend `./mvnw test`: `@DataJpaTest` for both queries, `@WebMvcTest` for the endpoint (no contents in JSON) and for the
description limit, a service test for server-set fields. Frontend: `npm run build`, `npm run lint`, manual check of screens
Plik and Wysyłka against a running backend, with a database that already contains uploads.

## 🏁 Definition of Done
- [ ] Acceptance criteria met, tests green
- [ ] `docs/api.md` updated, frontend caller updated
- [ ] `docs/architecture.md` entity table and `docs/decisions.md` updated
- [ ] Final report lists changed files and pre-existing violations found

## 📚 References
`App-Instance-Manager/CLAUDE.md` sections 2, 5, 6. `App-Instance-Menager-fronted/CLAUDE.md` sections 3, 5, 7.
