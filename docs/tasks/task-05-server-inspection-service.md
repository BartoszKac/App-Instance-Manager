# Task 05 – Server inspection microservice over gRPC

| Type | Module | Branch |
|---|---|---|
| ✨ feature | backend + new `App-Instance-Inspector` | `task-05-server-inspection` |

## 🎯 Goal
Once the password of a server is known, log in and learn as much as possible about it, without blocking the
main application. The SSH work moves to a separate Java service that can run as many instances.

## 📋 Scope
- [x] `proto/inspection.proto`: one server-streaming RPC, `Inspect`.
- [x] `App-Instance-Inspector`: gRPC server, read-only scripts over SSH, per-instance capacity limit.
- [x] Backend feature `inspection`: queue, async gRPC client with flow control, retries, H2 storage, REST API.
- [x] Automatic start when a server with a password is saved; manual restart through the API.
- [x] Data removed with the server. Runs left active by a restart are marked as failed.

## 🚫 Out of scope
Encrypting stored passwords (Q5), TLS on gRPC (ADR-014), reading file contents, `sudo`, moving `ServerInventoryService`
or the blocking `uname` at registration to the microservice.

## ⚠️ Pitfalls
- Never log `InspectRequest`: its generated `toString` prints the password.
- Remote output is hostile data: bounded record size, path and label lengths, shown only as text.
- A dropped SSH session ends the stream with EOF, so `RemoteCommand` must fail it, not report a short listing as complete.
- `StrictHostKeyChecking=no` must not spread: the inspector pins keys on first use.

## ✅ Acceptance criteria
- [x] `POST /api/inspections/servers/{id}` answers `202` at once; the work runs in the microservice.
- [x] Wrong password, unreachable host and a changed host key give distinct failures, and older data stays.
- [x] A full instance or a lost instance puts the run back in the queue, and it finishes without duplicate rows.
- [x] No password in any response or log.

## 🧪 Tests
Inspector: `./mvnw test`. Backend: `./mvnw test`. Manual end to end against throwaway Debian and Alpine SSH containers.

## 🏁 Definition of Done
- [x] Acceptance criteria met, tests green
- [x] `docs/api.md` updated
- [x] `docs/decisions.md` updated (ADR-014)
- [x] Final report lists changed files and pre-existing violations found
