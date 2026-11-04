# Task 02 – Make `npm run build` and `npm run lint` pass

**Status: ✅ done 14.10.2026**

| Type | Module | Branch |
|---|---|---|
| 🏗️ infra | frontend | `task-02-fix-frontend-build-and-lint` |

## 🎯 Goal
The frontend verification step in `CLAUDE.md` (`npm run build`, `npm run lint`) fails on a clean
`npm ci`, so no frontend change can be verified.

## 📋 Scope
State on 14.10.2026 after `npm ci`:

- `npm run build` fails in `tsc -b`:
  `tsconfig.json(26,5): error TS5101: Option 'baseUrl' is deprecated and will stop functioning in TypeScript 7.0.`
- `npm run lint`: 13 problems (11 errors, 2 warnings) — `no-explicit-any` ×4, `no-unused-vars` ×4,
  `no-unused-expressions` ×2, `react-hooks/exhaustive-deps` ×2, `react-hooks/set-state-in-effect`.

- [ ] Remove `baseUrl` from `tsconfig.json` / `tsconfig.app.json`; keep the `@/*` alias working via
      `paths` (TS 6 resolves `paths` relative to the tsconfig without `baseUrl`).
- [ ] Fix every lint error at its cause — no `eslint-disable`, no `any`.
- [ ] Remove existing `eslint-disable` comments where the cause is fixed.

## 🚫 Out of scope
Splitting oversize files, moving base URLs, adding a test runner, visual changes.

## ✅ Acceptance criteria
- [ ] `npm ci && npm run build && npm run lint` exits 0.
- [ ] Every screen still renders against a running backend (manual check).

## 🧪 Tests
`npm run build`, `npm run lint`, manual check of the 5 screens.

## 🏁 Definition of Done
- [ ] Acceptance criteria met
- [ ] Frontend `CLAUDE.md` "Known build state" note removed
