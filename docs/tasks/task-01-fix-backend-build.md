# Task 01 – Make `./mvnw test` pass on a clean checkout

**Status: ✅ done 14.10.2026**

| Type | Module | Branch |
|---|---|---|
| 🏗️ infra | backend | `task-01-fix-backend-build` |

## 🎯 Goal
Every workflow in `CLAUDE.md` ends with `./mvnw test`. Today it cannot even compile, so no change can
be verified.

## 📋 Scope
Two blockers, found on 14.10.2026:

1. `frontend-maven-plugin` and `maven-resources-plugin` in `pom.xml` point at
   `../App-Instance-Manager-front`, which does not exist (the folder is `../App-Instance-Menager-fronted`).
   Error: `C:\...\App-Instance-Manager-front doesn't exist`.
2. `src/main/resources/application.properties` and `src/test/resources/application.properties`
   are not UTF-8 (ISO-8859 bytes in Polish comments), while the build reads resources as UTF-8.
   Error: `filtering ... application.properties ... failed with MalformedInputException: Input length = 1`.

- [ ] Fix the frontend path, or move the frontend build into a Maven profile (e.g. `-Pfrontend`)
      so `./mvnw test` does not need Node — decide with the user (see 💬).
- [ ] Re-save both `application.properties` as UTF-8; rewrite the broken comment characters.
- [ ] Run `./mvnw test`, record the result; fix nothing else — failing tests become their own task.

## 🚫 Out of scope
Upgrading Spring Boot/Java, changing any property value, fixing failing tests, renaming folders.

## ✅ Acceptance criteria
- [ ] `./mvnw test` compiles and runs the test suite on a clean checkout without Node installed
      (if the profile option is chosen).
- [ ] `./mvnw spring-boot:run` starts on port 8888 with the same configuration values as before.

## 🧪 Tests
`./mvnw test`; manual start of the app.

## 🏁 Definition of Done
- [ ] Acceptance criteria met
- [ ] Root `CLAUDE.md` "Known build issue" paragraph and `docs/open-questions.md` Q2 updated

## 💬 Open questions
- Should `./mvnw package` bundle the frontend into the jar by default, or only on demand?
