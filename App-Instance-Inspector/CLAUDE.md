# CLAUDE.md – App Instance Manager Inspector

Rules for code under `App-Instance-Inspector/`. Read the root `CLAUDE.md` first. Java conventions are the same as in
`App-Instance-Manager/CLAUDE.md`: no comments, size limits, Lombok, JUnit 5 + Mockito + AssertJ, no real SSH in tests.

## What it is

Stateless Java 17 / Spring Boot 3.2.4 service without a web server or a database. It exposes the gRPC service from
`../proto/inspection.proto` on port 9090, logs in to a server over SSH and streams what it finds. Only the backend
calls it. See ADR-014.

## Security (outranks everything)

- Read-only on the target. Scripts in `resources/scripts` are fixed, take no input and never use `sudo`.
- No file contents: file listing is metadata only. Facts must not include process arguments, password hashes or
  full cron commands, because those hold secrets.
- Never log `InspectRequest` or the password. Its generated `toString` prints the password.
- The password is only in memory for the time of the call.
- Host keys are pinned through `PinnedHostKeys`. Do not use `StrictHostKeyChecking=no`.
- Remote output is hostile data: keep record, path and label limits.

## Limits

`inspector.max-concurrent` per instance (above it: `RESOURCE_EXHAUSTED`), `inspector.max-entries`, `inspector.job-timeout`.
`k8s/inspector-scaling.yaml` `targetValue` must equal `max-concurrent`.

## Run

```bash
./mvnw test
./mvnw spring-boot:run
```
