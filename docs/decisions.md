# Decisions

Numbered, append-only. A decision is changed by adding a new entry that supersedes the old one,
never by editing history. Claude never invents an entry — a missing referenced decision means this
file is stale; ask.

## Document precedence

1. Source code – for what the system currently does.
2. Module `CLAUDE.md` – coding conventions of that module.
3. Root `CLAUDE.md` – workflow and cross-module rules.
4. This file – design decisions.
5. `architecture.md`, `api.md` – descriptions; fixed when they disagree with 1–4.
6. `App-Instance-Menager-fronted/GEMINI.md` – legacy, lowest priority.

---

## ADR-001 – Claude working rules follow the LoyaltyPrograms projects (14.10.2026)

**Context.** The user's LoyaltyProgramsBackend and LoyaltyProgramsFrontend repositories have a
mature `CLAUDE.md` + `docs/` setup (task files, decisions log, open questions, "do not guess – ask").

**Decision.** This repository adopts the same structure: root and module `CLAUDE.md`,
`docs/architecture.md`, `docs/api.md`, `docs/decisions.md`, `docs/open-questions.md`,
`docs/task-template.md`, `docs/tasks/`. Size limits (20-line functions, 200-line files), no comments
in code, strategy maps instead of branching, DTOs at the API boundary.

**Consequences.** Existing code violates many rules; it is brought into compliance only when a task
touches it, not in one sweep.

## ADR-002 – Keep stack versions; upgrades are separate tasks (14.10.2026)

**Decision.** Backend stays on Java 17 / Spring Boot 3.2.4 / Maven, frontend on React 19 / Vite 8,
until a dedicated task changes it. LoyaltyPrograms uses Java 25 / Boot 4 / Gradle / Angular — that
is an inspiration for conventions, not a version target.

## ADR-003 – Layer-first packages kept until a refactor task (14.10.2026) — superseded by ADR-004

**Decision.** New backend code goes into the existing layer folders under the matching sub-domain
(`code`, `deploy`, `process`). A move to feature-first packages is a planned, separate refactor.

## ADR-004 – Backend uses feature-first packages (FDD) (14.10.2026) — supersedes ADR-003

**Context.** The user decided the backend follows the same feature split as the frontend and as
LoyaltyProgramsBackend (FDD), instead of keeping layer-first packages.

**Decision.** Top-level packages under `com.example.DynamicCode` are features: `code`, `deploy`,
`process`, plus `shared` (cross-feature code) and `legacy` (frozen `@Profile("dev")` experiments).
Each feature owns `controller/ service/ dto/ entity/ repository/ mapper/ enums/ exception/ strategy/`
as needed. Features never import each other's internals. Rules: `App-Instance-Manager/CLAUDE.md`
section 2.

**Consequences.** One mechanical move (task 03) relocates all existing classes; until then new
classes are created directly in the target feature packages. Entity class names stay unchanged so
table names in the H2 file DB do not change.

## ADR-005 – Frontend bundling only in the `frontend` Maven profile (14.10.2026)

**Context.** `pom.xml` always ran `frontend-maven-plugin` against a non-existent folder, so no
Maven goal worked. Running Node during every `./mvnw test` also slows the backend loop.

**Decision.** `frontend-maven-plugin` and the copy of `dist/` into `static/` run only with
`-Pfrontend` (`./mvnw -Pfrontend package`). Paths point at `../App-Instance-Menager-fronted`.
Node pinned to `v22.12.0` (Vite 8 requires Node ≥ 20.19). Plain `./mvnw test` needs no Node.

## ADR-006 – Frontend redesign: "pipeline" shell, GSAP and Solar icons (14.10.2026)

**Context.** The user asked for a much better-looking frontend. Each screen had its own copy of the
navigation, ~100 inline style blocks, leftover Vite template CSS (`index.css` centring `#root`)
and several unstyled classes.

**Decision.**
- One `AppShell` with a vertical `PipelineRail` (Kod → Plik → Wysyłka → Uruchom), a shared
  `ScreenHeader` and the console docked at the bottom. Views no longer render navigation.
- Design tokens in `styles/tokens.css`; teal-graphite base, periwinkle `--signal` accent, Schibsted
  Grotesk + JetBrains Mono.
- New dependencies: `gsap` (single screen-enter timeline, rail marker, modal open; bypassed under
  `prefers-reduced-motion`), `@iconify/react` + `@iconify-icons/solar` (offline icon data).
- No smooth-scroll library and no WebGL: the app is a tool with independently scrolling panes.

**Consequences.** Screens: projects as a list, editor with a Save → Compile → Run strip, deploy plan
as server lanes, a result panel on the run screen (command output was not shown before).

## ADR-007 – Light "paper and machine" colour scheme (14.10.2026) — refines ADR-006

**Context.** The user asked for a colour style inspired by viktor.com. Taken from it, at the level
of traits only: warm off-white page, navy-ink text and dark pill buttons, an electric violet accent,
white rounded cards with soft shadows, a soft violet-to-peach glow. No logo, name, copy, typeface or
layout was reused.

**Decision.** The shell turns light (`--page` #f6f2ee, `--surface` white, `--text` #17142a), primary
actions are navy pills (`--action`), `--signal` becomes violet #6236ff. Everything that shows code
or program output stays dark (`--pane` #15121f) via token re-mapping, so the editor, console and
output read as "the machine" inside the paper UI. Display face: Sora. One glow, at the bottom of
the pipeline rail.

**Consequences.** ADR-006 structure, motion and icons are unchanged; only tokens, radii and the
display face changed.

## ADR-008 – LAN device discovery through nmap, local subnets only (02.11.2026)

**Context.** The user wants to see how many devices are on a local network and add them as servers
without typing each one. Offered a pure-Java probe or nmap, the user chose nmap for its accuracy
(ARP, MAC, vendor), accepting that it must be installed on the backend host.

**Decision.**
- The backend runs the `nmap` binary from `PATH` and parses its XML output. nmap is an external
  runtime requirement, not a Maven dependency.
- The scan target is never free-form: the client's `cidr` must exactly match a subnet the backend
  detected on its own network cards (private IPv4, `/24`–`/30`). This stops the unauthenticated API
  from being used as a port scanner and rules out nmap argument injection. One scan runs at a time.
- nmap is started by a new `LanguageProcess.captureOutput` (timeout, bounded output), so `deploy` may
  import `code.service.LanguageProcess`.
- `shared/config/CorsConfig` is the first `WebMvcConfigurer`. It covers only the new
  `/api/deploy/network/**` paths and allows `http://localhost:5173` (working assumption of Q8).
- Found devices are added through the existing `POST /api/deploy/server/bulk` with shared SSH
  credentials and `operationSystem = LINUX`, the same fallback as single registration.

**Consequences.** Without nmap the scan answers `503`. Accuracy depends on privileges: without raw
socket access nmap cannot use ARP and finds fewer devices. A Docker container on the default bridge
network cannot see the host's LAN. Passwords added in bulk are stored as plain text like every other
server password (Q5).

## ADR-009 – Whole app in Docker, LAN scan by TCP connect (02.11.2026) — refines ADR-008

**Context.** The user wants to start everything from Docker with nothing else installed. ADR-008
assumed a bridge container cannot see the host's LAN. Tested on Docker Desktop for Windows, that is
wrong: outgoing connections reach the LAN. The container only cannot *detect* the LAN, because its
own card is `172.17.0.0/16`. nmap's default discovery in the container missed a live host and
reported the broadcast address as up. `-Pn -sT --open` over common ports found every host the default
discovery did. Adding `--host-timeout` made nmap list about 100 hosts that had simply timed out as up
with no ports, so it is not used.

**Decision.**
- nmap runs as `-Pn -sT -p <10 common ports> --open -T4 --max-retries 1`, with a 4 minute limit. A
  device counts only when the parser sees an open port. This replaces the `-p 22` command of ADR-008
  and works without raw sockets.
- Allowed subnets are the detected ones plus `aim.network.scan-subnets` (env
  `AIM_NETWORK_SCAN_SUBNETS`). Configured entries pass the same private `/24`–`/30` rule. The client
  still cannot supply its own range.
- `docker-compose.yml` sets that value from `AIM_SCAN_SUBNETS`, default `192.168.1.0/24`.
- The frontend gets its own nginx container on `127.0.0.1:5173`, the origin CORS already allows.

**Consequences.** A /24 takes about 2.5 minutes in the container, and repeated scans can differ by a
device. A device with none of those ports open is not counted. Through Docker's NAT there
are no MAC addresses or vendors and usually no hostnames. Another LAN needs `AIM_SCAN_SUBNETS` set
before `docker compose up`.

## ADR-010 – Fast LAN scan through a Windows helper (03.11.2026) — refines ADR-009

**Context.** The user found the Docker scan too slow (about 2.5 minutes). From a container there is no
ARP, so it also misses devices without an open common port. Offered a host script, a backend outside
Docker or a faster Docker scan, the user chose the host script.

**Decision.**
- `lan-helper/lan-helper.ps1` runs on Windows with no installation: ping sweep, ARP table, TCP 22
  probe, reverse DNS. `Start.bat` starts Docker and the helper.
- It listens on `localhost:8899` only, serves only `GET /subnet` and `GET /scan`, takes no parameters
  and scans only the private `/24`–`/30` network of the adapter with the default gateway. Requests
  whose `Origin` is not `http://localhost:5173` get `403`.
- The browser calls it directly. If it is unreachable, the UI warns and uses the nmap scan of ADR-009.

**Consequences.** The helper must be started again after a reboot, through `Start.bat`. Windows-only.
There is no automated test for the script. `Start.bat` uses `-ExecutionPolicy Bypass` for that one
process only.

## ADR-011 – More device details from the LAN helper (03.11.2026) — refines ADR-010

**Context.** The user wants more and more accurate information about each device.

**Decision.**
- The helper also collects open ports (17 common ones), the SSH banner, the NetBIOS name, ping
  latency and TTL, and pings twice before reading the ARP table.
- MAC vendors come from nmap's `nmap-mac-prefixes`. `Start.bat` copies it out of the backend image to
  `%LOCALAPPDATA%\AppInstanceManager`, so it is neither committed nor downloaded.
- The OS is derived in order: SSH banner, Apple vendor, Windows-only ports, TTL. Every result carries
  `osConfidence`, and the UI marks a `low` one as "szacunek". Bulk add uses `osFamily`, falling back
  to `LINUX`.
- Pure logic lives in `lan-helper/LanDeviceInfo.ps1`, tested by `LanDeviceInfo.Tests.ps1` without
  network access. The network code of `lan-helper.ps1` has no automated test. The helper stops older
  instances of itself at start, so `Start.bat` picks up a new version.

**Consequences.** A scan takes several seconds longer. TTL guesses can be wrong, and a phone with a
randomized MAC has no vendor. The vendor list dates from the image's nmap package.

## ADR-012 – LAN scan only in Docker, two stages; services catalog (03.11.2026) — supersedes ADR-010 and ADR-011, refines ADR-009

**Context.** The user wants everything to start from Docker and asked for more device information and
a services tab. Tested from a container on Docker Desktop: an ICMP sweep of a /24 takes about 4 s and
gave the same result three times, a TCP scan of live hosts about 4 s, and `nmap -sV` with scripts 8–60 s
per device. TTL is rewritten by Docker's NAT (always 63), and ARP is not available. Chosen by the
user: Docker only with the Windows helper removed, and a tab with detected and own services.

**Decision.**
- `lan-helper/` and `Start.bat` are removed. While the helper ran, HTTP.sys listened on all interfaces,
  so ADR-010's "localhost only" did not hold at the socket level; a request needed a `localhost` Host
  header and the allowed `Origin`, both of which a LAN client can send.
- `POST /scan` is a sweep (`nmap --privileged -sn -PE`) followed by a TCP connect scan of live hosts,
  replacing ADR-009's scan. A device counts when it answers ping. The image grants `cap_net_raw` to
  the nmap binary, so the backend still runs as non-root `runner`.
- `POST /device-details` runs `-sV --version-light` with the scripts `nbstat`, `http-title`,
  `smb-os-discovery` and `ssl-cert` for one host from an allowed subnet. At most 3 run at once. The UI
  loads them after the list, 2 at a time.
- OS comes only from evidence in service data or SMB, with no TTL guess, and is empty otherwise.
- New feature `catalog` with entity `ManagedService` (new table, no other schema change) and
  `/api/services`: name, host, port, description. Adding a service does not connect to it. Found
  services are kept in memory on the screen until saved.

**Consequences.** No MAC or vendor. A device that blocks ping is not listed even with open ports.
Any code run in the backend container can use nmap's raw socket capability, including NSE scripts.
