# CLAUDE.md – App Instance Manager (monorepo)

Entry point for every session in this repository. Read this file first, then the `CLAUDE.md` of the
module you are touching. The module file wins on module-specific conventions; this file wins on
workflow and cross-module rules.

## Mandatory rules

- **Security first.** This app compiles and executes arbitrary code and opens SSH sessions to
  remote machines. Anything touching process execution, SSH, file paths or credentials outranks every
  other rule (see `docs/architecture.md` → "Security surface"). When in doubt, stop and ask.
- **Do not guess. Ask.** Never invent an endpoint, field, entity, route or config key. If the code
  or `docs/api.md` does not have it, say so.
- **Never present an assumption as a fact.** Label it where it is made.
- **Read before writing.** Read the module `CLAUDE.md`, the relevant part of `docs/`, and the task
  file in `docs/tasks/` (if one exists) before changing code.
- **Every behaviour change ships with a test** (see the module `CLAUDE.md`).
- **No commits, no pushes** unless the user explicitly asks in the current conversation. Never run
  `Git.bat` — it commits everything as `"update"` and pushes to `main`.
- **Scope discipline.** Do only what the task asks. Pre-existing violations outside scope are listed
  in the final report, not fixed silently.

---

## Repository map

| Path | What it is | Rules |
|---|---|---|
| `App-Instance-Manager/` | Backend: Spring Boot, Java, Maven, H2 file DB, JSch (SSH), STOMP WebSocket | `App-Instance-Manager/CLAUDE.md` |
| `App-Instance-Menager-fronted/` | Frontend: React 19 + TypeScript + Vite, MVVM per feature | `App-Instance-Menager-fronted/CLAUDE.md` |
| `docs/` | Project documentation for humans and Claude | see table below |
| `.github/workflows/deploy.yml` | Mintlify docs → GitHub Pages | do not touch without a task |
| `Git.bat` | Personal "add-all, commit, push" script | never run |

The folder name `App-Instance-Menager-fronted` is misspelled historically — do not rename it without a
task; build configuration and documentation depend on the path.

## Project documents — read before implementing

| File | What it is | When to read it |
|---|---|---|
| `docs/architecture.md` | Current architecture of both modules, data flow, security surface, known debt | Before any implementation task |
| `docs/api.md` | REST + WebSocket contract as it exists in the controllers | Anything touching HTTP/WS on either side |
| `docs/decisions.md` | Decisions log (ADR-style, numbered) and document precedence | When two documents disagree |
| `docs/open-questions.md` | Unanswered questions with working assumptions | When a requirement looks underspecified |
| `docs/task-template.md` | Format of every task file | Before writing or splitting a task |
| `docs/tasks/task-NN-*.md` | The task you are implementing | Always, in full |

**Document precedence:** module `CLAUDE.md` > root `CLAUDE.md` on code conventions;
`docs/decisions.md` > `docs/architecture.md` on design decisions; the controllers' source code >
`docs/api.md` > `App-Instance-Menager-fronted/GEMINI.md` on the API contract. `GEMINI.md` is legacy
and partially wrong (port, several endpoints) — never trust it over code.

**Never invent an ADR.** A referenced decision that is not in `docs/decisions.md` means the document
is stale — say so and stop.

---

## Running locally

Backend (port **8888**, H2 file DB in `App-Instance-Manager/data/`):

```bash
cd App-Instance-Manager
./mvnw spring-boot:run          # Windows PowerShell: .\mvnw.cmd spring-boot:run
./mvnw test
```

Frontend (Vite dev server, talks to `http://localhost:8888`):

```bash
cd App-Instance-Menager-fronted
npm install
npm run dev
npm run build                   # tsc -b && vite build
npm run lint
```

Swagger UI (springdoc): `http://localhost:8888/swagger-ui.html`.

Backend in Docker (JDK 17 + python3 + g++ inside, so Java/Python/C++/Bash projects compile and run
in the container; port bound to `127.0.0.1:8888` only):

```bash
docker compose up -d --build     # build + start
docker compose logs -f backend   # logs, including PROCES LOG lines
docker compose down              # stop, keep data
docker compose down -v           # stop and DELETE volumes (H2 DB + saved projects)
```

Data lives in named volumes `aim-data` (`/opt/aim/data`, H2) and `aim-projects` (`/opt/aim/app`).
The container runs as non-root user `runner`. The frontend dev server on the host works unchanged
against `http://localhost:8888`.

Single jar with the frontend bundled into `static/` (needs no local Node — the plugin downloads it):

```bash
cd App-Instance-Manager
./mvnw -Pfrontend package
```

If `./mvnw test` reports failures that look like encoding garbage (`Ä…`, `Ĺ‚`), run
`./mvnw clean test` — stale classes compiled by an IDE with the Windows code page are the cause.

Baseline on 14.10.2026: backend 51/51 tests green; frontend build green, lint 0 errors / 1 warning.

---

## Workflow for every task

1. Read: root `CLAUDE.md` → module `CLAUDE.md` → `docs/architecture.md` → task file.
2. Locate the code (Grep/Glob, or the `Explore` agent for broad sweeps). Do not rely on `GEMINI.md`
   for locations.
3. If anything crucial is unknown, ask before writing code.
4. Implement in small steps. Keep backend and frontend contracts in sync — a changed endpoint means
   updating `docs/api.md` and the frontend caller in the same change.
5. Verify: backend `./mvnw test`; frontend `npm run build` and `npm run lint`.
6. Report: what changed (file list), what was verified and how, what was skipped, and pre-existing
   violations found on the way.

Project slash commands (`.claude/commands/`): `/new-task <description>` drafts a task file,
`/task NN` runs this workflow for a task file. `.claude/settings.json` pre-allows build/test
commands and denies `Git.bat`, `git push` and reading `deploy-config.json`.

## Git

- Branch per task: `task-NN-short-slug`. Conventional commits: `feat(scope): …`, `fix(scope): …`,
  `refactor(scope): …`, `docs: …`, `test(scope): …`.
- Never commit runtime output: `App-Instance-Manager/app/**` (generated project folders),
  `App-Instance-Manager/data/*.db`, `target/`, `dist/`, `node_modules/`.
- Never commit credentials. `App-Instance-Manager/deploy-config.json` holds SSH login data.
