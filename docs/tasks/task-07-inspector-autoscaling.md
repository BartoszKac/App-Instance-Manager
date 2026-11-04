# Task 07 – Automatic scaling of the inspector on Kubernetes

| Type | Module | Branch |
|---|---|---|
| 🏗️ infra | `k8s/`, `docker-compose.yml`, Dockerfiles | `task-05-server-inspection` (shared) |

## 🎯 Goal
The inspector grows under load instead of staying a single instance.

## 📋 Scope
- [x] Manifests for the whole app in `k8s/`, with services reachable only through `kubectl port-forward`.
- [x] Headless service plus `round_robin` in the gRPC client, so requests reach every replica.
- [x] KEDA `ScaledObject` (creates the HPA), driven by `GET /api/inspections/load`, 1–10 replicas.
- [x] Compose keeps working, with an isolated `inspection` network and manual `--scale`.

## 🚫 Out of scope
Scaling the backend (H2 file DB allows one instance), a public ingress, TLS.

## ⚠️ Pitfalls
- Inspections wait on SSH, so CPU stays low under load. A CPU-based HPA would not react, hence the queue metric.
- gRPC keeps connections, so the server limits connection age to make clients discover new replicas.
- `LoadBalancer` or `NodePort` on the backend would expose code execution without login.

## ✅ Acceptance criteria
- [x] `docker compose config` and `kubectl kustomize k8s` are valid.
- [x] Two replicas share work when each has a limit of 1 (tested with Docker containers).
- [ ] **Not verified:** `kubectl apply -k k8s` on a cluster, and the HPA adding and removing replicas. Kubernetes is
      not enabled on the development machine.

## 🧪 Tests
Steps in `k8s/README.md`.

## 🏁 Definition of Done
- [ ] Scaling observed on a real cluster
- [x] `docs/decisions.md` updated (ADR-014)
