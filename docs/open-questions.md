# Open questions

Each question has a working assumption. Claude follows the assumption until the question is
answered, and labels code built on it. Answered questions move to `decisions.md`.

| # | Question | Working assumption |
|---|---|---|
| Q1 | Is the app meant to run only on localhost / a trusted network? | Yes. No auth is added unless a task asks; exposing it publicly is out of scope. |
| Q2 | ~~Should `pom.xml` build the frontend into the jar?~~ | Answered by ADR-005: only with `-Pfrontend`. |
| Q3 | Can the legacy endpoints (`legacy/**`) be deleted? | No, not until confirmed — nothing in the frontend calls them, but external tools might. |
| Q4 | Should `app/`, `data/` and `deploy-config.json` be removed from git and added to `.gitignore`? | Yes eventually; not done without confirmation (removal rewrites what others pull). |
| Q5 | Should SSH passwords be stored encrypted / replaced by keys, and hidden from `GET /api/deploy/servers`? | Hide from responses in any task touching that endpoint; storage change is its own task. |
| Q6 | Which test runner for the frontend? | Vitest + React Testing Library (fits Vite), to be added in its own task. |
| Q7 | Upgrade to Java 21+ / Spring Boot 3.5+/4? README claims Java 21 / Boot 4. | Keep current versions (ADR-002); README to be corrected. |
| Q8 | Single CORS config: which origins? | `http://localhost:5173` (Vite default) once a task introduces it. |
| Q9 | Target of the planned "code transformation" – rewrite of this frontend/backend modeled on LoyaltyPrograms? | Open – to be specified by the user before any rewrite task. |
