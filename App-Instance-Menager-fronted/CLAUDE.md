# CLAUDE.md – App Instance Manager Frontend

Rules for all code you generate or modify under `App-Instance-Menager-fronted/`. Read the root
`CLAUDE.md` first.

> **Compliance is not limited to new code.** When you modify a file, bring the whole file into
> compliance. Out-of-scope violations elsewhere go into the final report.

## Mandatory rules

- **No comments in code**: no `//`, `/* */`, JSDoc or `{/* */}` in JSX, no commented-out code.
  Rationale goes into `docs/`. Tool directives that change behaviour are allowed;
  `eslint-disable`, `@ts-ignore`, `@ts-expect-error` are not — fix the cause.
- **Functions ≤ 20 lines, files ≤ 200 lines**, max 3 parameters, nesting ≤ 2 (section 5).
- **MVVM per feature** (section 3). Views render, view-models think, models are types.
- **HTTP only through the feature's API module** with the base URL from one place (section 4).
- **DRY.** The second copy of a piece of logic is the signal to extract it.
- Every new behaviour ships with a test once a test runner is set up (section 8).
- **Do not guess. Ask.** Never invent an endpoint or a field — check `docs/api.md` and the
  backend controller.

`GEMINI.md` in this folder is a legacy guide. Its MVVM description is still valid; its API section
(port 8080, several paths) is wrong. This file and `docs/api.md` win.

---

## 1. Stack

- React **19**, TypeScript **~6.0**, Vite **8**, `@vitejs/plugin-react`.
- Editor: `react-simple-code-editor` + `prismjs` (not Monaco, despite the root README).
- Console: SockJS + STOMP loaded at runtime in `terminal/useConsoleConnection.ts` (auto-connects), endpoint
  `/ws-console`, topic `/topic/output`.
- Motion: `gsap` (screen-enter timeline, rail marker, modal open) through `shared/motion.ts`; always
  check `prefersReducedMotion()` and render the final state when it is true.
- Icons: Solar set via `@iconify/react` + `@iconify-icons/solar` (offline, one import per icon).
  Add an icon to the map in `shared/ui/Icon.tsx`; never hand-draw SVG icons.
- ESLint 10 flat config (`eslint.config.js`) with `typescript-eslint`, `react-hooks`,
  `react-refresh`.
- Path alias `@/*` → `src/*` (tsconfig `paths` + Vite config).
- Scripts: `npm run dev`, `npm run build` (`tsc -b && vite build`), `npm run lint`,
  `npm run preview`.

**Build state (14.10.2026):** `npm run build` green, `npm run lint` 0 errors and 1 known warning
(`EditorView.tsx` effect deps — `reset` from `useEditor` is not memoised, so adding it would loop).
A change must not add errors or warnings.

`tsconfig.json` has no `baseUrl` (removed in TS 6); the `@/*` alias works through `paths` alone.
Shared helpers live in `src/shared/` (first one: `errorMessage(err, fallback)` — use it in every
`catch (err: unknown)`).

Do not add dependencies (router, state library, UI kit, axios) as a side effect. A new dependency is
a decision recorded in `docs/decisions.md`.

---

## 2. Folder layout

```
src/
  App.tsx            screen switch based on router state
  main.tsx           bootstrap
  router/            custom state router: useRouter() → { state, navigateTo(screen, project?) }
  editor/            projects list + code editor        (api/, components/, model/, viewmodels/, views/)
  deploy/            servers, files, transfer           (components/, model/, viewmodel/, views/)
  execution/         run programs on remote servers     (model/, viewmodels/, views/)
  process/           upload remote executables          (model/, viewmodel/, views/)
  terminal/          live console over STOMP (types for window.SockJS / window.Stomp in vite-env.d.ts)
  shared/            cross-feature code: errorMessage, lang.ts (language keys/labels), motion.ts (GSAP),
                     ui/ (AppShell, PipelineRail, ScreenHeader, Modal, Icon, atoms)
  styles/            tokens.css (design tokens) + one CSS file per area, imported by global.css
```

- **Organise by feature.** A feature folder owns its views, components, view-models, model types and
  API module.
- **Features never import from each other's internals.** Shared code (HTTP helper, base URL,
  shared types such as `Project`) moves to a `shared/` folder when a task touches it.
- New screen: add the name to the `Screen` union in `router/useRouter.ts`, add a branch in
  `App.tsx`, create the feature folder with the four MVVM layers.
- Folder naming is inconsistent (`viewmodel/` vs `viewmodels/`). New features use `viewmodels/`;
  existing folders are renamed only inside a task.

---

## 3. MVVM

| Layer | Contains | Never contains |
|---|---|---|
| `model/` | `interface` / `type` / enums mirroring backend DTOs | functions, fetch, React |
| `api/` | Typed functions calling the backend (one per endpoint) | React state, JSX |
| `viewmodels/useXxxViewModel.ts` | `useState`/`useReducer`/`useEffect`, calls to `api/`, derived data, handlers | JSX, direct `fetch` |
| `views/` | Call one view-model hook, render components | business logic, fetch |
| `components/` | Presentational, props in / callbacks out | hooks that fetch, view-model calls |

- A view-model returns a plain object `{ state…, actions… }`. Keep it ≤ 200 lines; split by concern
  (`useServers`, `useTransfer`) and compose.
- Loading and error state are explicit in every view-model that fetches (`isLoading`, `error`).
- **Lookup maps instead of `if/else` chains or `switch` on strings/enums**
  (`Record<LanguageType, string>`), with an explicit fallback.

---

## 4. HTTP and WebSocket

- Today the base URL `http://localhost:8888` is hardcoded in four files
  (`editor/api/CodeService.ts`, `deploy/viewmodel/useDeployViewModel.ts`,
  `execution/viewmodels/useExecutionViewModel.ts`, `process/model/remoteExecutableRepository.ts`)
  plus `terminal/PackageTerminal.tsx`. This is known debt.
- New code reads the base URL from one module that uses `import.meta.env.VITE_API_URL` with
  `http://localhost:8888` as fallback. When a task touches one of the files above, migrate it.
- Every call checks `res.ok` and throws a typed error; the view-model turns it into `error` state.
- Request/response types live in the feature `model/` and match `docs/api.md` exactly.
- WebSocket: one connection for the app (the terminal). Do not open extra STOMP clients per view.

---

## 5. Size and complexity

- Hard limits: 20 lines per function (including JSX-returning components — extract
  sub-components), 200 lines per file, 3 parameters (use an object), nesting depth 2.
- Known oversize files: `useDeployViewModel.ts`, `ExecutionView.tsx`, `PackageTerminal.tsx`,
  `EditorView.tsx`. Split them only when a task touches them.

## 6. TypeScript

- No `any`. Unknown data from the network is typed via the model interface.
- `PackageTerminal.tsx` uses untyped `window.SockJS` / `window.Stomp` — known debt; new code declares
  types in `vite-env.d.ts` instead of using `any`.
- `type` imports use `import type` (`verbatimModuleSyntax` is on).
- Named exports only for new modules (no `export default` except `App`).

## 7. Styling

Design direction (ADR-006) — "pipeline": code travels Kod → Plik → Wysyłka → Uruchom. The left
`PipelineRail` is the one memorable element; everything else stays quiet.

- Colours only through tokens in `styles/tokens.css` (ADR-007, "paper and machine"): light warm
  chrome (`--page`, `--surface`, `--raised`, `--line`, `--text*`), navy `--action` for primary
  pill buttons, violet `--signal` for active state, focus and progress, `--ok`/`--warn`/`--error`
  for status, `--lang-*` per language via `.lang--java` etc. Code lives in dark `--pane-*` panels:
  `.code`, `.console`, `.run-output`, `.deploy-log__lines`, `.prompt`, `.statusbar` re-map the text
  and line tokens inside them, so components inside a pane need no special colours. No raw hex in
  components.
- Type: Sora (`--display`) for screen titles, modal titles and row names; Schibsted Grotesk for UI
  text; JetBrains Mono only for code, file names, paths and commands. Buttons, search and
  segmented controls are pills (`--radius-pill`); cards use `--radius-lg` + `--shadow-card`.
  Sentence case everywhere — no all-caps labels, no eyebrow labels above headings.
- Every screen starts with `ScreenHeader` and marks animated parts with `data-enter` (`title`,
  `meta`, `body`); the shell runs one enter timeline per screen. No other ambient animation.
- Shared building blocks: `.btn` (`--primary`, `--run`, `--quiet`, `--ghost`, `--danger`, `--sm`),
  `.input`, `.field`, `.segmented`, `Modal`, `LangChip`, `StatusDot`, `Checkbox`, `Notice`,
  `EmptyState`. Reuse before adding.
- No inline `style={{…}}` except for genuinely dynamic values (progress width, console height).
- Copy is Polish, plain verbs, actions keep one name through the flow (button "Wyślij" → status
  "Wysyłanie…"). Errors say what happened and what to do.
- Every interactive element has a visible focus state (`--focus-ring`) and works with keyboard.

## 8. Testing

- No test runner is configured yet. Adding Vitest + React Testing Library is a separate task
  (see `docs/open-questions.md`). Until then, a change is verified by `npm run build` and
  `npm run lint` passing, plus a manual check of the touched screen against a running backend —
  report which.
- Once Vitest exists: view-models are tested with `renderHook`, `fetch` is mocked, components with
  RTL by role/label.

## 9. Security

- Never store SSH passwords in `localStorage`/`sessionStorage`. Never log request bodies containing
  credentials to the console.
- Never render backend output with `dangerouslySetInnerHTML`; program output is text.

## 10. Do not guess — ask

- Ask before: adding dependencies, changing the router approach, changing API contracts, anything
  security related.
- Routine judgement calls with an obvious default: decide, then mention it in the report.
