# Decisions

Numbered, append-only. A decision is changed by adding a new entry that supersedes the old one,
never by editing history. Claude never invents an entry — a missing referenced decision means this
file is stale; ask.

## Document precedence

1. Source code – for what the system currently does.
2. Module `CLAUDE.md` – coding conventions of that module.
3. Root `CLAUDE.md` – workflow and cross-module rules.
4. This file – design decisions.
5. `architecture.md`, `api.md` – descriptions; fixed when they disagree with 1–4.
6. `App-Instance-Menager-fronted/GEMINI.md` – legacy, lowest priority.

---

## ADR-001 – Claude working rules follow the LoyaltyPrograms projects (14.10.2026)

**Context.** The user's LoyaltyProgramsBackend and LoyaltyProgramsFrontend repositories have a
mature `CLAUDE.md` + `docs/` setup (task files, decisions log, open questions, "do not guess – ask").

**Decision.** This repository adopts the same structure: root and module `CLAUDE.md`,
`docs/architecture.md`, `docs/api.md`, `docs/decisions.md`, `docs/open-questions.md`,
`docs/task-template.md`, `docs/tasks/`. Size limits (20-line functions, 200-line files), no comments
in code, strategy maps instead of branching, DTOs at the API boundary.

**Consequences.** Existing code violates many rules; it is brought into compliance only when a task
touches it, not in one sweep.

## ADR-002 – Keep stack versions; upgrades are separate tasks (14.10.2026)

**Decision.** Backend stays on Java 17 / Spring Boot 3.2.4 / Maven, frontend on React 19 / Vite 8,
until a dedicated task changes it. LoyaltyPrograms uses Java 25 / Boot 4 / Gradle / Angular — that
is an inspiration for conventions, not a version target.

## ADR-003 – Layer-first packages kept until a refactor task (14.10.2026) — superseded by ADR-004

**Decision.** New backend code goes into the existing layer folders under the matching sub-domain
(`code`, `deploy`, `process`). A move to feature-first packages is a planned, separate refactor.

## ADR-004 – Backend uses feature-first packages (FDD) (14.10.2026) — supersedes ADR-003

**Context.** The user decided the backend follows the same feature split as the frontend and as
LoyaltyProgramsBackend (FDD), instead of keeping layer-first packages.

**Decision.** Top-level packages under `com.example.DynamicCode` are features: `code`, `deploy`,
`process`, plus `shared` (cross-feature code) and `legacy` (frozen `@Profile("dev")` experiments).
Each feature owns `controller/ service/ dto/ entity/ repository/ mapper/ enums/ exception/ strategy/`
as needed. Features never import each other's internals. Rules: `App-Instance-Manager/CLAUDE.md`
section 2.

**Consequences.** One mechanical move (task 03) relocates all existing classes; until then new
classes are created directly in the target feature packages. Entity class names stay unchanged so
table names in the H2 file DB do not change.

## ADR-005 – Frontend bundling only in the `frontend` Maven profile (14.10.2026)

**Context.** `pom.xml` always ran `frontend-maven-plugin` against a non-existent folder, so no
Maven goal worked. Running Node during every `./mvnw test` also slows the backend loop.

**Decision.** `frontend-maven-plugin` and the copy of `dist/` into `static/` run only with
`-Pfrontend` (`./mvnw -Pfrontend package`). Paths point at `../App-Instance-Menager-fronted`.
Node pinned to `v22.12.0` (Vite 8 requires Node ≥ 20.19). Plain `./mvnw test` needs no Node.

## ADR-006 – Frontend redesign: "pipeline" shell, GSAP and Solar icons (14.10.2026)

**Context.** The user asked for a much better-looking frontend. Each screen had its own copy of the
navigation, ~100 inline style blocks, leftover Vite template CSS (`index.css` centring `#root`)
and several unstyled classes.

**Decision.**
- One `AppShell` with a vertical `PipelineRail` (Kod → Plik → Wysyłka → Uruchom), a shared
  `ScreenHeader` and the console docked at the bottom. Views no longer render navigation.
- Design tokens in `styles/tokens.css`; teal-graphite base, periwinkle `--signal` accent, Schibsted
  Grotesk + JetBrains Mono.
- New dependencies: `gsap` (single screen-enter timeline, rail marker, modal open; bypassed under
  `prefers-reduced-motion`), `@iconify/react` + `@iconify-icons/solar` (offline icon data).
- No smooth-scroll library and no WebGL: the app is a tool with independently scrolling panes.

**Consequences.** Screens: projects as a list, editor with a Save → Compile → Run strip, deploy plan
as server lanes, a result panel on the run screen (command output was not shown before).

## ADR-007 – Light "paper and machine" colour scheme (14.10.2026) — refines ADR-006

**Context.** The user asked for a colour style inspired by viktor.com. Taken from it, at the level
of traits only: warm off-white page, navy-ink text and dark pill buttons, an electric violet accent,
white rounded cards with soft shadows, a soft violet-to-peach glow. No logo, name, copy, typeface or
layout was reused.

**Decision.** The shell turns light (`--page` #f6f2ee, `--surface` white, `--text` #17142a), primary
actions are navy pills (`--action`), `--signal` becomes violet #6236ff. Everything that shows code
or program output stays dark (`--pane` #15121f) via token re-mapping, so the editor, console and
output read as "the machine" inside the paper UI. Display face: Sora. One glow, at the bottom of
the pipeline rail.

**Consequences.** ADR-006 structure, motion and icons are unchanged; only tokens, radii and the
display face changed.
