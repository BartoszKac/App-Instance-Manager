# Task template

Copy everything below `--- TEMPLATE START ---` into `docs/tasks/task-NN-slug.md` (zero-padded,
next free number, numbers never reused). Delete italic hints and optional sections you do not need.
If the task does not fit on two screens, split it. A task is 0.5–2 days of work.

Emoji are fixed navigation markers:

| Emoji | Section | Required |
|---|---|---|
| 🎯 | Goal | yes |
| 📋 | Scope | yes |
| 🚫 | Out of scope | yes |
| 🔗 | Dependencies | no |
| 🧩 | Code patterns | no |
| ⚠️ | Pitfalls | no |
| ✅ | Acceptance criteria | yes |
| 🧪 | Tests | yes |
| 🏁 | Definition of Done | yes |
| 📚 | References | no |
| 💬 | Open questions | no |

Types: ✨ feature · ♻️ refactor · 🐛 fix · 🔒 security · 🏗️ infra · 📝 docs

--- TEMPLATE START ---

# Task NN – <title>

| Type | Module | Branch |
|---|---|---|
| ✨ feature | backend / frontend / both | `task-NN-slug` |

## 🎯 Goal
*One or two sentences: why this exists, what is better afterwards.*

## 📋 Scope
*What to build. Up to ~10 sentences plus a checklist.*

- [ ] …

## 🚫 Out of scope
*What belongs to other tasks — do not touch.*

## 🔗 Dependencies
*Tasks that must be done first.*

## 🧩 Code patterns
*Do this, not that — short snippets, verified against current code.*

## ⚠️ Pitfalls

## ✅ Acceptance criteria
- [ ] …

## 🧪 Tests
*Which tests prove the acceptance criteria. Backend: `./mvnw test`. Frontend: `npm run build`,
`npm run lint` (+ Vitest once added).*

## 🏁 Definition of Done
- [ ] Acceptance criteria met, tests green
- [ ] `docs/api.md` updated if any endpoint changed, frontend caller updated
- [ ] `docs/decisions.md` / `docs/open-questions.md` updated if a decision was made
- [ ] Final report lists changed files and pre-existing violations found

## 📚 References

## 💬 Open questions
