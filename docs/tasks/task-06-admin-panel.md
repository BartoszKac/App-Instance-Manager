# Task 06 – Admin panel with inspection results

| Type | Module | Branch |
|---|---|---|
| ✨ feature | frontend | `task-05-server-inspection` (shared, depends on the task 05 API) |

## 🎯 Goal
Make the results of an inspection easy to browse.

## 📋 Scope
- [x] New screen `admin`, rail station "Admin", feature folder `src/admin` (MVVM).
- [x] Server list with inspection state, polled every 3 s.
- [x] Folder tree loaded on demand with paging, search in paths, marking of folders without access.
- [x] Editing the stored SSH user and password, which starts a new inspection.
- [x] Tabs for facts by category. Re-inspect button, and "Zaufaj nowemu kluczowi" after a host key change.

## 🚫 Out of scope
Viewing file contents, editing or deleting anything on a server, frontend test runner (Q6).

## ⚠️ Pitfalls
A folder can hold over 100 000 entries, so the tree must page and must not load a whole subtree.

## ✅ Acceptance criteria
- [x] Tree, search, facts and failure states work against a running backend.
- [x] No password is requested from or returned by these endpoints.

## 🧪 Tests
`npm run build`, `npm run lint`, manual check with a headless browser against the test backend.

## 🏁 Definition of Done
- [x] Acceptance criteria met, build and lint green
- [x] `docs/api.md` frontend callers updated
- [x] Final report lists changed files and pre-existing violations found
