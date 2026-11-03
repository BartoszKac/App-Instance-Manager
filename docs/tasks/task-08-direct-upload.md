# Task 08 – Direct upload of files and folders to a server

| Type | Module | Branch |
|---|---|---|
| ✨ feature | backend + inspector + frontend | `task-05-server-inspection` (shared, depends on task 05) |

## 🎯 Goal
Send a file or a whole folder straight to a registered server through the microservice, without keeping the content
in the main application.

## 📋 Scope
- [x] gRPC `ServerFileTransfer.Upload`, bidirectional stream with flow control.
- [x] Inspector writes over SFTP to a temporary name, checks the size, then renames.
- [x] Backend feature `transfer`: raw request body streamed to gRPC, no multipart and no temporary file.
- [x] Only metadata stored: paths, size, SHA-256, state, error, times.
- [x] Screen "Plik", mode "Prosto na serwer": server choice, files or a folder, target folder picker, overwrite option, progress, history.

## 🚫 Out of scope
Empty folders (browsers do not report them), download, resuming an interrupted file, file permissions.

## ⚠️ Pitfalls
- Spring multipart buffers to disk, so the endpoint takes `application/octet-stream`.
- `relativePath` and `destination` come from the client: rejected with `..`, empty segments or control characters.
  They reach SFTP calls only, never a shell.
- Existing files stay unless the user chooses overwrite.
- Never log `UploadHeader`: its generated `toString` prints the password.

## ✅ Acceptance criteria
- [x] 300 MB file arrives with a matching SHA-256; no file over 1 MB in `/tmp` or `/opt/aim/app` of the backend afterwards.
- [x] Folder keeps its structure when sent from the browser.
- [x] Aborted upload leaves no partial file. Traversal gives `400`, an existing file `409`.

## 🧪 Tests
Inspector and backend: `./mvnw test`. Frontend: `npm run build`, `npm run lint`. Manual end to end against a throwaway SSH container.

## 🏁 Definition of Done
- [x] Acceptance criteria met, tests green
- [x] `docs/api.md` and `docs/decisions.md` updated (ADR-015)
- [x] Final report lists changed files and pre-existing violations found
