# Task 11 – Make a sent file runnable from screen Uruchom

| Type | Module | Branch |
|---|---|---|
| 🐛 fix | frontend | `task-11-run-sent-file` |

## 🎯 Goal
No active code sets the execute permission, and the commands on screen Uruchom use the folder name instead of the file
name. Afterwards the user can see which file a card is about, and can make it executable and run it deliberately.

## 📋 Scope
Approved by the user on 05.11.2026: `chmod` is a button the user chooses, never automatic after sending.

- [ ] `DeployedInstance.fileName` comes from `idCode` + `uploadStrategyType`, looked up in `GET /api/deploy/deployable-files`. Today `inferFileName(cfg.pathInServer)` returns the folder, for example `test`.
- [ ] `lang` comes from the same lookup instead of `inferLang(path)`, which falls back to `sh`.
- [ ] The card shows the file name next to the path.
- [ ] New presets in group System: `Nadaj prawo uruchamiania` and `Uruchom plik`, hidden when the server's `operationSystem` is `WINDOWS`.
- [ ] Presets keep today's behaviour: a click only fills the prompt, and the user presses Uruchom.
- [ ] A card whose file no longer exists says so, and file-based presets are hidden for it.
- [ ] Split `useExecutionViewModel.ts` or `ExecutionView.tsx` only if touched and over the limits.

## 🚫 Out of scope
Backend changes. Automatic `chmod`. Validating commands in `program/execute`. The behaviour of `cd` on an empty path on
Windows and Mac servers. A `fileName` column in `RemoteProgramConfiguration`.

## 🔗 Dependencies
Task 09 (safe names) and task 10 (`GET /api/deploy/deployable-files`).

## 🧩 Code patterns
A file name reaches a remote shell, so every preset quotes it:
```ts
const shellQuote = (value: string) => `'${value.replace(/'/g, `'\\''`)}'`;

{ label: 'Nadaj prawo uruchamiania', cmd: (i) => `chmod +x -- ${shellQuote(i.fileName)}` },
{ label: 'Uruchom plik', cmd: (i) => `./${shellQuote(i.fileName)}` },
```
Also quote the existing presets that use `fileName` (`javac`, `java`, `python3`, `g++`).

## ⚠️ Pitfalls
- 🔒 `ExecutionService` runs `cd <path> && <command>` in a remote shell. File names were never checked before task 09, so old rows can hold anything. Quote always, and do not rely on validation alone.
- A project maps to its main file only. `Uruchom plik` makes no sense for a Java `.class` file, so offer it only for uploads and show `java Main` from the Java group for projects.
- `base(i.fileName)` assumes one extension: `app.tar.gz` becomes `app.tar`. Existing behaviour, list it in the report.
- `program/execute` returns `200` with the text `BŁĄD WYKONANIA: …` on failure. `chmod` failures appear only in the output. Do not parse it here.

## ✅ Acceptance criteria
- [ ] A card for an uploaded `app.bin` shows `app.bin`, and the preset fills `chmod +x -- 'app.bin'`.
- [ ] The file runs on a Linux test server after `chmod` followed by `Uruchom plik`.
- [ ] A name with a space or `'` works, and a name such as `a;touch x` creates no extra file.
- [ ] Nothing runs without the user pressing Uruchom.
- [ ] A Windows server card has no `chmod` preset.

## 🧪 Tests
No frontend test runner exists (Q6). Verify with `npm run build`, `npm run lint` and a manual check against a throwaway
Linux SSH container, including the hostile file name above. Report what was checked. The "ships with a test" rule applies
only once Vitest exists, so `shellQuote` stays a pure function, ready for a test.

## 🏁 Definition of Done
- [ ] Acceptance criteria met, build and lint green
- [ ] `docs/api.md` frontend table updated (endpoints used by the execution feature)
- [ ] `docs/decisions.md` records the manual `chmod` decision
- [ ] Final report lists changed files, the shell quoting change, and pre-existing violations found

## 📚 References
`docs/architecture.md` → Security surface, Remote execute. `App-Instance-Menager-fronted/CLAUDE.md` sections 3, 5, 9.
