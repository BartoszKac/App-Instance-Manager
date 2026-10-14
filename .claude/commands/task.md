---
description: Implement a task file from docs/tasks following the project workflow
argument-hint: <task number or file name>
---

Implement task `$ARGUMENTS` from `docs/tasks/`.

1. Read `CLAUDE.md`, the `CLAUDE.md` of every module the task touches, `docs/architecture.md`,
   `docs/api.md` and the task file in full.
2. List anything crucial that is unknown or contradicts the code. If there is anything, stop and ask.
3. Implement only the 📋 Scope. Respect 🚫 Out of scope.
4. Keep `docs/api.md` and the frontend callers in sync with any endpoint change.
5. Verify: backend `./mvnw test` in `App-Instance-Manager/`; frontend `npm run build` and
   `npm run lint` in `App-Instance-Menager-fronted/`.
6. Report: changed files, verification results (exact failures if any), skipped steps, and
   pre-existing violations found. Do not commit.
