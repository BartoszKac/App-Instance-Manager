# Task 04 – LAN device scan

| Type | Module | Branch |
|---|---|---|
| ✨ feature | both | `task-04-lan-device-scan` |

## 🎯 Goal
Show how many devices are on a local network and let the user add them as servers without typing
each one by hand.

## 📋 Scope
The backend detects its own private subnets and scans a chosen one with nmap. The Wysyłka screen
shows the device count and list, and adds the chosen devices with shared SSH credentials.

- [x] `GET /api/deploy/network/subnets`, `POST /api/deploy/network/scan`
- [x] `LanguageProcess.captureOutput` with timeout and bounded output
- [x] `NetworkScanModal` and `useNetworkScan`, bulk add through `POST /api/deploy/server/bulk`
- [x] nmap in the Docker image, frontend container, configured scan subnet (ADR-009)

## 🚫 Out of scope
Free-form CIDR input, scheduled scans, encrypted password storage (Q5), CORS for existing
controllers, detecting the OS of devices added in bulk.

## ⚠️ Pitfalls
- nmap XML has a `DOCTYPE`, so the parser cannot forbid it: it disables external entities instead.
- A container cannot detect the host's LAN, only reach it: the subnet is configured (ADR-009).

## ✅ Acceptance criteria
- [x] A subnet outside the detected list is rejected with `400` and no process starts
- [x] Missing nmap answers `503` with a message saying what to do
- [x] A second scan during a running one answers `409`
- [x] Already registered IPs cannot be added again from the list

## 🧪 Tests
Backend: `NmapXmlParserTest`, `NetworkScanServiceTest`, `LocalSubnetServiceTest`,
`NetworkDiscoveryControllerTest`, `LanguageProcessTest` (`@Tag("integration")`). Frontend:
`npm run build`, `npm run lint`.

## 🏁 Definition of Done
- [x] Acceptance criteria met, tests green
- [x] `docs/api.md` updated, frontend caller added
- [x] `docs/decisions.md` updated (ADR-008)
- [x] Manual scan against a real network through the Docker nmap scan
- [x] Windows helper removed; two-stage Docker scan and the Serwisy screen added (ADR-012)
- [ ] Manual check of the scan modal and the Serwisy screen in a browser

## 📚 References
ADR-008, Q5, Q8.
