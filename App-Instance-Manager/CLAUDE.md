# CLAUDE.md – App Instance Manager Backend

Rules for all code you generate or modify under `App-Instance-Manager/`. Read the root `CLAUDE.md`
first.

> **Compliance is not limited to new code.** When you modify a class for any reason, bring that
> class into compliance with the rules below. Violations elsewhere that are out of scope are listed
> in the final report so they can become a task.

## Mandatory rules

- **Security (section 10) outranks every other rule.** Code execution, SSH and file paths are the
  attack surface of this application.
- Every new behaviour ships with at least one automated test (section 9).
- **No explanatory comments or JavaDoc in Java source.** Naming and structure carry the meaning;
  rationale goes into `docs/`. Section-divider comments (`// --- KOMPILACJA ---`) and commented-out
  code are removed when the class is touched.
- **No `System.out.println` / `printStackTrace`.** Use `@Slf4j` logging (section 11).
- **Controllers never return JPA entities** in new or modified endpoints — map to DTO records
  (section 5).
- **Do not guess. Ask** (section 13).

---

## 1. Stack (as in `pom.xml` — verify there before relying on it)

- Java **17** (`java.version`), Spring Boot **3.2.4**, Maven wrapper (`mvnw`, `mvnw.cmd`).
- `spring-boot-starter-web`, `spring-boot-starter-websocket` (STOMP, simple broker),
  `spring-boot-starter-data-jpa`, H2 (file DB `./data/moja_baza`, `ddl-auto=update`).
- JSch `0.1.55` for SSH/SFTP. springdoc-openapi `2.5.0`. Lombok `1.18.42`.
- Tests: JUnit 5, Mockito, AssertJ (via `spring-boot-starter-test`); H2 in-memory profile in
  `src/test/resources/application.properties`.
- Server port **8888**. Multipart limit 150MB.
- Root package: `com.example.DynamicCode`. Do not rename it without a task.

Do not upgrade Spring Boot, Java or JSch as a side effect of another task. Version changes are their
own task.

---

## 2. Package structure — Feature-Driven Development (FDD)

Code is organised **by feature, not by layer** (ADR-004). Every class belongs to the feature it
serves. There are no horizontal layers at the top level.

- A **feature** is a vertical slice that owns its controllers, services, DTOs, entities,
  repositories, mappers, enums, exceptions and strategies.
- **Features never import each other's internal classes.** Cross-feature use goes through a public
  service of the other feature's `service/` package or through `shared`. A dependency cycle between
  features is a design error — raise it.
- **Never create top-level `controller/`, `service/`, `repository/`, `model/`, `dto/`, `factory/`,
  `strategy/` packages.**
- A class belongs in exactly one package. Code used by two or more features lives in `shared`.

### Features

```
com.example.DynamicCode
  code/       projects of source files: save, folders on disk, compile, launch locally
  deploy/     remote servers, SSH sessions, file transfer, remote programs, remote execution
  process/    uploaded executables (RemoteExecutable)
  shared/     WebSocket config, FrontendNotificationService, GlobalExceptionHandler,
              CORS config, generic file helpers
  legacy/     former controller/test + service/**/test, @Profile("dev") — frozen, see section 3
```

Create a new top-level feature only when a task introduces a domain that fits none of the above.

### Sub-packages inside a feature (create only those that are needed)

```
<feature>/
  controller/   @RestController — HTTP boundary only
  service/      business logic, use-case orchestration
  dto/          request/response records
  entity/       JPA @Entity classes
  repository/   Spring Data JPA interfaces
  mapper/       entity ↔ DTO mapping (pure functions)
  enums/        enums owned by the feature (LanguageType → code, UploadStrategyType → deploy)
  exception/    feature-specific unchecked exceptions
  strategy/     strategy interfaces + implementations + their factory (LanguageHandler, upload strategies)
```

The former `databaseservice/` wrappers (`SourceCodeService`, `RemoteSerwerService`, …) live in
their feature's `service/`; their generic interface `DataBaseProvider<T>` is in
`shared/persistence`.

### Allowed cross-feature imports (state after task 03)

| From | May import |
|---|---|
| any feature | `shared.**` |
| `deploy` | `code.entity.{SourceCode,CompiledCode}`, `code.enums.LanguageType`, `code.service.{SourceCodeService,CompiledCodeService}`, `process.entity.RemoteExecutable`, `process.service.RemoteExecutableService` (upload strategies need the files to send) |
| `process` | `code.enums.LanguageType` |
| `legacy` | anything (frozen) |
| `code` | nothing from other features |

Any new cross-feature import not in this table needs a decision in `docs/decisions.md`. Nothing
outside `legacy` may import from `legacy`.

Existing misspellings in class names (`Serwer`, `Enviroment`, `Aplication`, `Menager`, `Controler`)
are fixed only by a rename task. New identifiers use correct English.

---

## 3. Legacy package `legacy/`

`legacy/**` (formerly `controller/test/**` and `service/**/test/**`) is **not tests** — it is older
experimental endpoints. All legacy controllers carry `@Profile("dev")`, so they are only registered with the
`dev` profile. Some share prefixes with production controllers; `POST /api/deploy/transfer/bulk`
is an exact duplicate, so the `dev` profile currently fails to start.

- Do not add new code to them.
- Do not call them from the frontend.
- Removing or migrating them is its own task; before deleting, grep the frontend for every path.

---

## 4. Design rules

- **Constructor injection only** via `@RequiredArgsConstructor` and `private final` fields. No field
  `@Autowired`.
- **Controller → service delegation.** A controller method: validate input, call one service
  method, wrap the result. No business logic, no repository access in controllers.
- **Branching on a type uses a strategy map, not an if/else or switch chain.** Follow the existing
  pattern: Spring injects `Map<String, LanguageHandler>`; the factory resolves by key. Adding a
  language = one new `@Component` implementing `LanguageHandler` + one `LanguageType` value.
- **Methods ≤ 20 lines, classes ≤ 200 lines, max 3 parameters, nesting depth ≤ 2.** Use guard
  clauses and extracted private methods. Existing oversize classes (`DeployService`,
  `AplicationService`) are refactored only when touched by a task.
- **Enums carry their own data** (as `LanguageType` carries its extension) instead of
  `private static final` constant fields scattered across classes.
- **Immutability for DTOs:** new DTOs are Java `record`s.
- **No `@CrossOrigin(origins = "*")` on new controllers.** CORS belongs in one `WebMvcConfigurer`
  (planned, see `docs/open-questions.md`).

---

## 5. API shape and errors

- New/changed endpoints accept and return DTO records, never `@Entity` classes.
- Use proper status codes: `201` for create, `204` for delete, `404` for missing id, `400` for
  invalid input. Do not return `200` with an error string in the body.
- Exceptions: throw specific domain exceptions (`ProjectNotFoundException`, `SshConnectionException`,
  …) and map them in a single `@RestControllerAdvice` `GlobalExceptionHandler` returning
  `ProblemDetail`. The handler does not exist yet — creating it is its own task; until then, do not
  add ad-hoc `try/catch` returning strings in controllers.
- Every endpoint change is reflected in `docs/api.md` and in the frontend caller in the same change.

---

## 6. Persistence

- Entities live in `<feature>/entity/`, repositories in `<feature>/repository/` and extend
  `JpaRepository`. An entity used by two features stays in its owning feature; the other feature
  reads it through the owner's service.
- Moving an entity to another package does not change its table name (Hibernate uses the simple
  class name) — keep class names unchanged during the package move.
- Field names in Java camelCase. Existing `Long Id` (capital I) in `SourceCode` is a known defect —
  do not copy it.
- Schema is generated by Hibernate (`ddl-auto=update`). Renaming a field drops data silently in
  the file DB — any entity field rename needs a note in the task and the user's confirmation.
- Never commit `data/*.db`.

---

## 7. Code execution (`code` feature)

- Flow: `CodeController` → `ProviderCodeService` → `SavingDataInSystemService` (writes files under
  `app/<uuid>/`) → `CompilationService` (`LanguageHandler.getCompileCommand`) /
  `AppLauncherService` (`getRunCommand`) → `code/service/LanguageProcess.runProcessInDirectory` (the only
  `ProcessBuilder`, streams each line via `FrontendNotificationService` to `/topic/output`).
- Current state: `process.waitFor()` has **no timeout** — known debt. New code must not add another
  process runner; extend `LanguageProcess`.
- Process creation uses `ProcessBuilder` with an argument **list**. Never build a shell command
  string, never pass user input through `sh -c` / `cmd /c`.
- Every spawned process has a timeout and is destroyed on timeout; output is streamed, not buffered
  unbounded.
- File names from the client are validated: no path separators, no `..`, extension must match a
  known `LanguageType`. Resolved paths must stay inside the project's `app/<uuid>/` folder
  (`path.normalize().startsWith(base)`).

## 8. SSH / `deploy` feature

- All SSH sessions come from `deploy/service/SshSessionFactory`. Sessions and channels are closed in
  `try-with-resources` or `finally` — no leaks.
- Upload strategies are resolved through `deploy/strategy/FileUploadStrategyFactory` by `UploadStrategyType`; add a
  strategy, do not add a branch.
- Remote commands are built from validated values only; never interpolate raw user strings into a
  remote shell command.
- Passwords are never logged, never returned in API responses (DTO without the field), never
  written to `deploy-config.json` in new code.

---

## 9. Testing

- JUnit 5 + Mockito + AssertJ. Unit tests with `@ExtendWith(MockitoExtension.class)`;
  `@DataJpaTest` for repository behaviour; `@WebMvcTest` + `MockMvc` for controllers.
- Test class mirrors the production feature package (`code/service/SourceCodeService` →
  `src/test/java/.../code/service/SourceCodeServiceTest`).
- Existing tests still carry `// GIVEN` / `// WHEN` comments; remove them when you touch a test
  class.
- Method names: `shouldXWhenY`. Structure: given / when / then — without comments, separated by
  blank lines.
- No real SSH, no real network, no real compilers in unit tests — mock `SshSessionFactory` and
  process launching. Tests that really compile code are tagged `@Tag("integration")`.
- Run: `./mvnw test`. A change is not done until this passes; if it fails for reasons outside the
  task, report the exact failing test and message.

---

## 10. Security (outranks everything)

- Treat every request body as hostile: source code, file names, server addresses, user names.
- Path traversal, command injection, unbounded process time/memory, SSH credential leaks and
  `setAllowedOriginPatterns("*")` on the WebSocket are the known risk areas — any change there needs
  an explicit note in the final report.
- JSch: `StrictHostKeyChecking=no` (if present) is a known weakness; do not spread it to new code.
- Never log or return credentials. Never commit real IPs/passwords.

## 11. Logging

- `@Slf4j`, parameterised messages: `log.info("Compiled project {}", id)`. No string concatenation.
- `info` for state changes, `debug` for detail, `warn`/`error` with the exception as last argument.
- Never log source code bodies, passwords or full SSH commands containing secrets.

## 12. Style

- 4-space indentation, one top-level class per file, imports without wildcards in new code.
- User-facing messages (exception messages returned to the UI) stay in Polish, as the rest of the
  app; identifiers are English.

## 13. Do not guess — ask

- Ask before acting on: anything security related, entity/schema changes, removing legacy
  endpoints, changing ports or the WebSocket topic, anything outward-facing (SSH to real hosts).
- Routine judgement calls with an obvious default do not need a question — make the call and
  mention it in the report.
