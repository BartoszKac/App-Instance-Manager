# Kubernetes – App Instance Manager

The whole app runs in one namespace, `aim`. The `inspector` microservice scales with load through an
HPA that KEDA creates (ADR-014): replicas = active and queued inspections / 4, from 1 to 10.

## Setup (once)

1. Docker Desktop → Settings → Kubernetes → Enable, with the **kubeadm** cluster type. It shares Docker's
   image store, so locally built images need no registry. A `kind` cluster does not share it.
2. Install KEDA, pinned to a release that supports your Kubernetes version
   (see https://keda.sh/docs/latest/operate/cluster/#kubernetes-compatibility):

   ```bash
   kubectl apply --server-side -f https://github.com/kedacore/keda/releases/download/v2.16.1/keda-2.16.1.yaml
   ```

## Run

```bash
docker compose build
kubectl apply -k k8s
kubectl -n aim port-forward svc/frontend 5173:80
kubectl -n aim port-forward svc/backend 8888:8888
```

Then open `http://localhost:5173`. Each `port-forward` holds its terminal.

The LAN scan target is `AIM_NETWORK_SCAN_SUBNETS` in `backend.yaml`.

## Watch scaling

```bash
kubectl -n aim get hpa,scaledobject
kubectl -n aim get pods -l app=inspector -w
```

## Security

- Never change the `backend` or `frontend` services to `LoadBalancer` or `NodePort`, and never use
  `kubectl port-forward --address 0.0.0.0`. The API runs arbitrary code and has no login. `port-forward`
  listens on loopback only.
- `network-policy.yaml` limits access to the inspector's gRPC port, through which SSH passwords travel
  unencrypted. A NetworkPolicy works only with a CNI that enforces it. The default Docker Desktop cluster
  is not known to enforce it (assumption, not verified), so there the protection is that the cluster
  runs nothing else and the port is not exposed outside it.

## Stop

```bash
kubectl delete -k k8s                       # also deletes the PVCs, so the H2 DB and saved projects
```
