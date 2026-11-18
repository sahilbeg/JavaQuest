## Hook

A big bike-share company does not have one mechanic who walks the city fixing bikes. It has a depot manager with a list: "300 stations, 2,000 bikes, every dock working". Every hour, the manager compares the list with the streets and sends crews to close the gaps.

Kakashi is the mechanic walking the city at 3 a.m. Spoke grew from six servers to 30 copies of its API, and he restarts, updates and counts them by hand. **Kubernetes** is the depot manager. You write the list; it keeps the city matching it, day and night.

## Concept

### Why an orchestrator

In [Docker basics](/t/module-07-tooling/topic-05-docker-basics) you packed the API into an image and ran it as a container. One container on one machine is easy. Thirty, spread over ten machines, raise five questions:

| Question | What Kubernetes does |
|---|---|
| Which machine runs each copy? | **scheduling**: it places each copy where CPU and memory are free |
| A copy crashed at 3 a.m. Who restarts it? | **self-healing**: it restarts or replaces it, no human needed |
| Rush hour needs 60 copies, nights need 10. | **scaling**: one number to change, or a rule that changes it |
| How do we ship version 2.5.0 without downtime? | **rollouts**: it swaps copies a few at a time, and can undo |
| Copies come and go. How does anyone find them? | **service discovery**: one stable name that always points at the healthy copies |

```callout jargon
A **container orchestrator** is a system that runs many containers across many machines for you: it places, restarts, scales and updates them. **Kubernetes** (often **K8s**, an 8 for the eight middle letters) is the most widely used one.
```

### Cluster, nodes and the control plane

A Kubernetes **cluster** is a group of machines run as one. Each machine is a **node**. A few special parts, together called the **control plane**, make the decisions.

| Part | Where | Its job |
|---|---|---|
| **API server** | control plane | the front door: every command and every change goes through it |
| **etcd** | control plane | a small, reliable database holding the desired and current state |
| **scheduler** | control plane | picks a node for each new Pod, based on free CPU and memory |
| **controllers** | control plane | loops that compare "what should be" with "what is" and fix the gap |
| **kubelet** | every node | starts and stops containers on its node, and runs their health checks |
| **kube-proxy** | every node | routes traffic for a Service to the right containers |

You never log into a node to start something. You tell the API server what you want, and the rest follows. Most teams rent a **managed cluster**, such as Amazon EKS, Google GKE or Azure AKS. The cloud runs the control plane; you bring the nodes and the YAML.

### Desired state and the reconciliation loop

This is the one big idea of Kubernetes. You do not give orders like "start 3 copies". You describe **desired state**: "3 copies of spoke-api 2.5.0 should be running". A **controller** then runs a loop forever:

1. Look at the desired state.
2. Look at the actual state.
3. Act to close the gap, then go back to 1.

```callout jargon
**Desired state** is what you have declared should exist. **Reconciliation** is a controller's endless loop of comparing desired with actual and acting on the difference.
```

```animate
title: A node dies at 3 a.m.
kind: scene
nodes:
  - { id: etcd, label: "Desired state", emoji: "📋", value: "spoke-api × 3" }
  - { id: ctrl, label: Controller, emoji: "🔁", value: "sees 3 of 3", col: 2 }
  - { id: pa, label: "Pod a", emoji: "📦", value: running, row: 1, col: 1 }
  - { id: pb, label: "Pod b", emoji: "📦", value: running, row: 1, col: 2 }
  - { id: pc, label: "Pod c", emoji: "📦", value: running, row: 1, col: 3 }
  - { id: pd, label: "Pod d", emoji: "📦", value: starting, row: 1, col: 4 }
steps:
  - say: The desired state says 3 copies of spoke-api. The controller counts 3 running. Nothing to do.
    focus: [etcd, ctrl]
  - say: At 3 a.m. the node under Pod b loses power. Pod b is gone.
    hide: [pb]
    set: { ctrl: "sees 2 of 3" }
  - say: On its next pass, the controller sees 2 running where 3 are wanted. That gap is its cue.
    focus: [ctrl]
  - say: It asks for a new Pod. The scheduler puts it on a healthy node, and it starts.
    show: [pd]
    send: { from: ctrl, to: pd, label: create }
  - say: Pod d passes its checks. 3 of 3 again, and nobody woke up.
    set: { pd: running, ctrl: "sees 3 of 3" }
  - say: Delete a Pod by hand and the same thing happens. To really have fewer, change the desired state.
    focus: [etcd]
```

Desired state also makes the cluster **declarative**: the YAML files in git say exactly what runs. Rebuilding a cluster means applying the same files again.

### Syntax: YAML and kubectl apply

```bash
kubectl apply -f spoke-api.yaml
```

Every object is a YAML document with the same four parts:

```yaml
apiVersion: apps/v1          # which version of the Kubernetes API defines this kind
kind: Deployment             # what sort of object
metadata:
  name: spoke-api            # its name, unique for its kind in its namespace
spec:                        # the desired state: what you want
  replicas: 3
  # ...
```

`kubectl` is the command-line tool that talks to the API server. `apply` sends your file. If the object is new, it is created. If it exists, Kubernetes works out the difference and changes only that. Apply the same file twice and nothing happens the second time, which makes it safe to run from a CI pipeline.

`kubectl get deployment spoke-api -o yaml` shows the object back, with a `status` part added. `spec` is what you asked for; `status` is what Kubernetes has reached so far.

### Pods

A **Pod** is the smallest thing Kubernetes runs: one or more containers that live and die together. Usually it holds exactly one container, the Spoke API.

- Each Pod gets **its own IP address** inside the cluster.
- Containers in one Pod share that IP, so they reach each other on `localhost`. A helper container in the same Pod is called a **sidecar**, for example one that ships logs.
- Pods are **ephemeral**: short-lived and replaceable. A replaced Pod gets a new name and a new IP. The old one never comes back.

```callout jargon
**Ephemeral** means temporary. Anything a Pod writes to its own disk disappears with it, so keep state in a database, not in the container.
```

That last point drives the rest of this page. Since Pods come and go, nothing may depend on one particular Pod or its IP.

### Deployments, ReplicaSets and Pods

You almost never create Pods yourself. You create a **Deployment**, and it manages the rest.

- A **Deployment** holds the Pod template (image, ports, settings) and the number of copies. It also handles updates.
- For each version of the template, it creates a **ReplicaSet**, whose only job is "keep exactly N of these Pods running".
- The ReplicaSet creates the **Pods**.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spoke-api
spec:
  replicas: 30
  selector:
    matchLabels:
      app: spoke-api
  template:                          # the Pod template: every copy is made from this
    metadata:
      labels:
        app: spoke-api
    spec:
      containers:
        - name: api
          image: registry.spoke.example/spoke-api:2.5.0
          ports:
            - containerPort: 8080
```

Change the image to `2.5.1` and apply. The Deployment makes a second ReplicaSet for 2.5.1, then grows the new one and shrinks the old one. The old ReplicaSet stays around at 0 copies, which is what makes undo fast.

### Labels, selectors and namespaces

A **label** is a key and value stuck on an object, like `app: spoke-api` or `track: canary`. A **selector** picks objects by their labels. That is how the parts find each other: the Deployment above owns every Pod labelled `app: spoke-api`.

```bash
kubectl get pods -l app=spoke-api            # every Pod with that label
kubectl get pods -l app=spoke-api,track=canary
```

A **namespace** is a named folder inside the cluster. Names must be unique only inside it, and access rules and resource **quotas** (caps on total CPU and memory) can be set per namespace. Spoke might use `spoke` for the API and `monitoring` for its dashboards. Many companies use a separate cluster for production, so a mistake in staging cannot touch it.

```bash
kubectl get pods -n spoke                     # -n picks the namespace
```

### Services: one stable name

Pods change IP every time they are replaced. Other services cannot chase them. A **Service** gives a set of Pods one stable name and address, and spreads requests across them.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: spoke-api
spec:
  selector:
    app: spoke-api          # send traffic to Pods with this label...
  ports:
    - port: 80              # ...arriving on port 80 of the Service
      targetPort: 8080      # ...to port 8080 of each Pod
```

The default type is **ClusterIP**: a virtual IP reachable only inside the cluster. Kubernetes also gives it a DNS name. The billing service, in the same namespace, simply calls `http://spoke-api`. From another namespace, it is `spoke-api.spoke.svc.cluster.local`.

```callout jargon
A **Service** is a stable name and virtual IP in front of a changing set of Pods, chosen by a label selector. It load-balances across the Pods that are **ready**, a word the probes card explains.
```

The Service keeps its list of Pods up to date by itself. A new Pod with the label joins once it is ready; a dead one leaves. This is the service discovery from the first card.

### Ingress and Gateway: traffic from outside

A ClusterIP Service is invisible from the internet. The rider app needs a way in. An **Ingress** is a set of HTTP routing rules: "requests for `api.spoke.example` go to the `spoke-api` Service".

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: spoke-api
spec:
  rules:
    - host: api.spoke.example
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: spoke-api
                port:
                  number: 80
```

An Ingress is only rules. An **ingress controller**, a proxy running in the cluster, reads them and does the routing. It usually ends TLS too, like the load balancer in [Networking](/t/module-08-backend/topic-13-networking).

The newer **Gateway API** does the same job with more power, such as sending 5% of traffic to a canary. New clusters increasingly use it instead of Ingress. A Service of type **LoadBalancer** is the simplest way in: the cloud gives that one Service a public address.

### ConfigMaps and Secrets

The same image runs in staging and production, as in [Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production). Settings come from outside. A **ConfigMap** holds plain settings; a **Secret** holds passwords and keys.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: spoke-api-config
data:
  SMS_TIMEOUT_MS: "2000"
  SMS_BULKHEAD_SLOTS: "20"
```

Inside the Deployment's container, pull them in as environment variables:

```yaml
          envFrom:
            - configMapRef:
                name: spoke-api-config
          env:
            - name: PAYMENT_API_KEY
              valueFrom:
                secretKeyRef:
                  name: payment-provider
                  key: api-key
```

Spring Boot maps `SMS_TIMEOUT_MS` to the property `sms.timeout-ms`, so the code from [Resilience](/t/module-09-architecture/topic-09-resilience) reads its timeouts from here.

### Secrets are not encrypted

Here is a Secret made by `kubectl`, in a real run:

```bash
kubectl create secret generic payment-provider --from-literal=api-key=pay-key-live-7 --dry-run=client -o yaml
```

```text
apiVersion: v1
data:
  api-key: cGF5LWtleS1saXZlLTc=
kind: Secret
metadata:
  name: payment-provider
```

That value looks scrambled, but it is only **base64**, an encoding anyone can reverse:

```bash
printf 'cGF5LWtleS1saXZlLTc=' | base64 -d
```

```text
pay-key-live-7
```

Base64 is not encryption. So:

- **Never commit Secret YAML to git.** Anyone who reads the repo has the key.
- Limit who may read Secrets in the cluster, and turn on **encryption at rest** for etcd.
- Many teams keep secrets in a **secret manager**, such as HashiCorp Vault or AWS Secrets Manager, and sync them into the cluster. That is the vault from [Docker basics](/t/module-07-tooling/topic-05-docker-basics), wired into Kubernetes.

**Env vars or files?** Environment variables are read once, at start: a change needs a restart. They also leak easily, into crash reports and debug logs. A Secret can instead be **mounted as a file**, and Kubernetes updates the file when the Secret changes, after a short delay. Files are the safer default for keys.

### Probes: startup, liveness and readiness

[Observability](/t/module-09-architecture/topic-08-observability) taught the two health questions in its card "Liveness vs readiness". Here they get wired to the platform. The kubelet asks each container regular questions, called **probes**. Each has one job:

| Probe | Question | When it fails |
|---|---|---|
| **startup** | "Have you finished starting?" | keep waiting; liveness and readiness are not asked yet. Too many failures restart it |
| **liveness** | "Are you stuck beyond saving?" | the container is **restarted** |
| **readiness** | "Can you take requests right now?" | the Pod is **taken out of the Service**, not restarted. It rejoins when it passes again |

Point them at Actuator's `/actuator/health/liveness` and `/actuator/health/readiness`. Spoke serves Actuator on its own port, 8081, as set up in Observability, so the probes use that port.

```yaml
          startupProbe:
            httpGet: { path: /actuator/health/liveness, port: 8081 }
            periodSeconds: 5
            failureThreshold: 24        # up to 2 minutes to start
          livenessProbe:
            httpGet: { path: /actuator/health/liveness, port: 8081 }
            periodSeconds: 10
            failureThreshold: 3         # 3 failures in a row: restart
          readinessProbe:
            httpGet: { path: /actuator/health/readiness, port: 8081 }
            periodSeconds: 5
            failureThreshold: 2
```

A Spring app on a small CPU share can take 30 s or more to start. Without the startup probe, liveness would kill it before it finished, again and again. Readiness reports `OUT_OF_SERVICE` during startup and shutdown, so the Service only sends traffic in between. Readiness also gates rolling updates: an old Pod leaves only when a new one is ready, as the rolling update card shows.

Kubernetes ignores the Dockerfile's `HEALTHCHECK` from module 7. Probes replace it.

### Probe mistakes that cause outages

**A liveness probe that checks the database.** This is the restart storm from Observability, now at full size. The database slows down for a minute, and all 30 Pods fail liveness at once. Kubernetes restarts all 30, and they reconnect to the struggling database together, cold. Never point liveness at the plain `/actuator/health`, which includes the `db` check. Use `/actuator/health/liveness`.

**No readiness probe.** A new Pod gets traffic the moment its container starts, while Spring is still loading. Riders get connection errors on every deploy. Readiness holds traffic back until the app can serve.

**Readiness that depends on a shared dependency.** If readiness checks the payment provider, a provider outage takes every Pod out of the Service. Nothing serves anything, not even ride history. Keep shared dependencies out of probes; handle them with timeouts and breakers instead.

**Probes with tight timeouts.** The default probe timeout is 1 s. A Pod in a long garbage-collection pause can miss it. Give liveness generous limits: restarting a busy Pod only adds load.

### Requests and limits

Each container states the CPU and memory it needs:

```yaml
          resources:
            requests:
              cpu: "500m"         # half a CPU core, reserved
              memory: "1Gi"
            limits:
              memory: "1Gi"       # the hard ceiling
```

- A **request** is a reservation. The scheduler places a Pod only on a node with that much unreserved. If no node has room, the Pod stays **Pending**.
- A **limit** is a ceiling, and the two resources behave differently above it.
- **CPU over the limit**: the container is **throttled**, made to wait. It slows down but lives. Throttling shows up as mysterious slow requests.
- **Memory over the limit**: the kernel kills the container at once. Kubernetes reports **OOMKilled**, exit code **137**, the same 128 + 9 you met in Docker.

```callout jargon
`m` means millicores: `500m` is half of one CPU core. `Gi` means gibibytes: `1Gi` is 1,024 MiB.
```

Common practice: set the memory limit equal to the memory request, so a Pod never depends on memory that might not be there. Many teams set CPU requests but no CPU limit, to avoid throttling. Without requests, the scheduler packs Pods blindly, and the autoscaler on a later card cannot measure usage.

### The JVM in a container

Since Java 10, the JVM reads the container's memory limit instead of the whole machine's memory. By default it gives the heap 25% of that limit. With a 1 GiB limit, that is 256 MiB, and the rest sits unused.

`-XX:MaxRAMPercentage=75` raises it to 768 MiB. The other quarter is not waste: thread stacks, loaded classes, compiled code and network buffers live outside the heap. Set it with an environment variable, so the image stays the same:

```yaml
          env:
            - name: JAVA_TOOL_OPTIONS
              value: "-XX:MaxRAMPercentage=75"
```

The classic trap is a fixed `-Xmx`. Someone sets `-Xmx1g` in the image, then ops sets a 1 GiB limit. The heap alone may now fill the whole limit, and the non-heap memory pushes the container over. The kernel kills it with no `OutOfMemoryError`, no stack trace, just 137. Never set `-Xmx` at or above the limit; prefer `MaxRAMPercentage`, which follows the limit when ops changes it.

The JVM also sizes itself to the CPU limit: `Runtime.getRuntime().availableProcessors()` follows it, and so does the number of garbage-collection threads. A pod limited to 1 CPU sees 1 processor, even on a 64-core node.

### Rolling updates

The Deployment's default update strategy is a **rolling update**: replace Pods a few at a time. Two settings control the pace:

- **`maxSurge`**: how many extra Pods may exist during the update, above `replicas`. Default 25%, rounded up.
- **`maxUnavailable`**: how many Pods may be missing during the update. Default 25%, rounded down.

```yaml
spec:
  replicas: 30
  minReadySeconds: 10            # a new Pod must stay ready this long to count
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%              # up to 8 extra Pods (7.5 rounded up)
      maxUnavailable: 0          # never drop below 30 serving Pods
```

```animate
title: Rolling out 2.5.1, one Pod at a time
kind: scene
nodes:
  - { id: svc, label: Service, emoji: "🔀", value: "3 ready Pods", col: 2 }
  - { id: o1, label: "Pod 2.5.0", emoji: "🟦", value: ready, row: 1, col: 0 }
  - { id: o2, label: "Pod 2.5.0", emoji: "🟦", value: ready, row: 1, col: 1 }
  - { id: o3, label: "Pod 2.5.0", emoji: "🟦", value: ready, row: 1, col: 2 }
  - { id: n1, label: "Pod 2.5.1", emoji: "🟩", value: starting, row: 2, col: 1 }
  - { id: n2, label: "Pod 2.5.1", emoji: "🟩", value: starting, row: 2, col: 2 }
  - { id: n3, label: "Pod 2.5.1", emoji: "🟩", value: starting, row: 2, col: 3 }
steps:
  - say: Three Pods run 2.5.0 behind the Service. Settings for this demo are maxSurge 1 and maxUnavailable 0.
    send: [{ from: svc, to: o1, label: requests }, { from: svc, to: o2, label: requests }, { from: svc, to: o3, label: requests }]
  - say: kubectl apply with image 2.5.1. maxSurge 1 allows one extra Pod, so one new Pod starts. The old three keep serving.
    show: [n1]
    set: { svc: "3 ready Pods, 1 starting" }
  - say: The new Pod's readiness probe passes. Only now does the Service send it traffic.
    set: { n1: ready, svc: "4 ready Pods" }
    send: { from: svc, to: n1, label: requests }
  - say: maxUnavailable 0 means an old Pod may stop only once a new one is ready. One 2.5.0 Pod drains and stops.
    hide: [o1]
    set: { svc: "3 ready Pods" }
  - say: The same again. A second new Pod starts, gets ready, and a second old Pod leaves.
    show: [n2]
    hide: [o2]
    set: { n2: ready }
  - say: The last swap. All three Pods now run 2.5.1, and riders never lost a server.
    show: [n3]
    hide: [o3]
    set: { n3: ready }
  - say: Had the new Pods never passed readiness, the rollout would simply stop. The old Pods would keep serving everyone.
    focus: [svc]
```

That last caption is the safety net. **Readiness gates the rollout.** A broken version never gets past its first surge of new Pods, and with `maxUnavailable: 0` no old Pod leaves.

### Syntax: kubectl rollout

```bash
kubectl set image deployment/spoke-api api=registry.spoke.example/spoke-api:2.5.1
kubectl rollout status deployment/spoke-api        # wait, and fail if the rollout gets stuck
kubectl rollout history deployment/spoke-api       # the revisions Kubernetes remembers
kubectl rollout undo deployment/spoke-api          # back to the previous revision
kubectl rollout undo deployment/spoke-api --to-revision=3
```

`rollout status` waits until every Pod is updated and ready. A rollout that makes no progress for 10 minutes, the default `progressDeadlineSeconds`, is marked failed, and `status` exits with an error. That lets a CI pipeline stop and alert someone.

`undo` scales the previous ReplicaSet back up, so it takes seconds, not a rebuild. This is "redeploy first, revert second" from [Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production), built in. In real teams the image tag in git is the source of truth, so after an undo, fix git too.

### Canary and blue-green on Kubernetes

The deploy strategies from module 7 map onto these objects:

- **Canary**: run two Deployments. Each selects `app: spoke-api` plus its own track label: `track: stable` or `track: canary`. Two Deployments with the same selector would fight over the same Pods. The Service selects only `app: spoke-api`, so it spreads traffic across the ready Pods of both. 1 canary Pod next to 29 stable ones gets about 3%. For exact percentages, use the Gateway API's weights, or a tool such as Argo Rollouts that automates the steps.
- **Blue-green**: run two full Deployments, labelled `version: blue` and `version: green`. The Service selects one of them. Switching is one edit to the Service's selector, and switching back is just as fast.

Both need the metrics from [Observability](/t/module-09-architecture/topic-08-observability) to decide whether the new version is healthy.

### Graceful shutdown

Every rollout, scale-down and node repair stops Pods. Stopping one well takes care, because two things start **at the same time** when Kubernetes deletes a Pod:

1. The Pod is taken out of the Service. Every node and the ingress controller must hear about it, which takes a moment.
2. The kubelet runs the container's **preStop** hook, if any, then sends **SIGTERM** to the main process: "please stop".

The **race**: if Spring stops at once, some load balancers have not yet heard the news. They keep sending requests to a Pod that refuses them. Riders see errors on every deploy.

```yaml
    spec:
      terminationGracePeriodSeconds: 45
      containers:
        - name: api
          lifecycle:
            preStop:
              exec:
                command: ["sh", "-c", "sleep 10"]
```

```properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=25s
```

- **`preStop: sleep 10`** keeps the Pod serving for 10 s while the removal spreads. Then SIGTERM arrives.
- **`server.shutdown=graceful`**: on SIGTERM, Spring stops taking new requests and finishes the ones in flight, for up to 25 s here. Recent Spring Boot versions do this by default; writing it down makes it explicit.
- **`terminationGracePeriodSeconds`**: the whole budget, 30 s by default, counted from the start of preStop. When it runs out, Kubernetes sends SIGKILL. So 10 + 25 must fit inside it: 45 leaves room.

```callout warning
SIGTERM goes to the container's main process. Use the exec form `ENTRYPOINT ["java", "-jar", "app.jar"]` from module 7, so java is that process. With a shell in front, the shell may not pass SIGTERM on: Java never hears it, and is killed after the grace period.
```

### Horizontal Pod Autoscaler

Rush hour needs more copies than 3 a.m. A **Horizontal Pod Autoscaler** (HPA) changes `replicas` for you, based on a measurement such as CPU.

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: spoke-api
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: spoke-api
  minReplicas: 10
  maxReplicas: 60
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 60     # aim for 60% of each Pod's CPU request
```

The rule it follows:

```text
desired replicas = ceil(current replicas × current value ÷ target value)
```

At 08:00, 10 Pods average 90% CPU, and the target is 60%. ceil(10 × 90 ÷ 60) = **15** Pods. "Utilization" is measured against each Pod's **CPU request**, which is one more reason to set requests.

It is not instant. New Pods must be scheduled, start the JVM and pass readiness: often a minute or more. To avoid flapping, it also waits 5 minutes by default before scaling down. For a rush hour you can predict, many teams raise `minReplicas` before 08:00 on a schedule. "Horizontal" means more copies; "vertical" would mean bigger Pods.

### StatefulSets, volumes and managed databases

Deployments suit **stateless** apps, like the Spoke API: any copy can serve any request, and a lost copy loses nothing. A database is **stateful**. Its data must survive restarts, and each copy has its own role.

- A **PersistentVolume** is storage that outlives Pods: a network disk the cluster attaches to whichever node the Pod runs on. A Pod asks for one with a **PersistentVolumeClaim**.
- A **StatefulSet** is like a Deployment for stateful apps. Its Pods get stable names, `db-0`, `db-1`, `db-2`, each with its own volume, and they start in order.

You can run PostgreSQL this way, but backups, failover, upgrades and tuning are a full-time job. So most teams use a **managed database** from their cloud, such as Amazon RDS or Google Cloud SQL. The cluster keeps only the stateless apps.

### On call: reading a sick cluster

The first commands to run, in order, like the shell skills from [On call](/t/module-07-tooling/topic-08-shell-on-call):

```bash
kubectl get pods -n spoke                                # STATUS, READY and RESTARTS for every Pod
kubectl describe pod spoke-api-7d9f8-x2k4q -n spoke      # its settings, last state and recent events
kubectl logs spoke-api-7d9f8-x2k4q -n spoke --previous   # logs of the container that crashed
kubectl get events -n spoke --sort-by=.lastTimestamp     # what the cluster did recently
```

`--previous` is the one people forget. After a crash, plain `kubectl logs` shows the **new** container, which has only just started. The crash is in the previous one's logs.

| You see | It means | Look at |
|---|---|---|
| `CrashLoopBackOff` | the container keeps crashing; Kubernetes waits longer between restarts, up to 5 minutes | `logs --previous`: usually a startup exception or missing config |
| `OOMKilled`, exit code 137 | over its memory limit | the limit and `MaxRAMPercentage`; a leak, if usage only grows |
| `ImagePullBackOff` or `ErrImagePull` | the node cannot download the image | a typo in the tag, a missing image, or no access to the registry |
| `Pending` | no node can take it | `describe`: not enough CPU or memory for its requests, or a volume that cannot attach |
| `Running`, but `READY 0/1` | readiness keeps failing, so it gets no traffic | the readiness endpoint, and the app's logs |
| `RESTARTS` climbing on a Running Pod | liveness keeps failing | is liveness checking something it should not? |

`kubectl exec -it <pod> -- sh` opens a shell inside a running container. `kubectl port-forward <pod> 8080:8080` lets you call one Pod from your laptop. Both are for looking, not fixing: a change made by hand inside a Pod vanishes with it. Fix the YAML and apply it.

### In an interview

Kubernetes questions test whether you have run services in production. Classic ones:

- **"What is the difference between liveness and readiness?"** Restart versus remove from traffic. Add the restart storm story: liveness must never check the database.
- **"How do you deploy with zero downtime?"** Rolling update with `maxUnavailable: 0` and readiness gating. Graceful shutdown with preStop and SIGTERM handling. `rollout undo` as the escape hatch.
- **"A Pod keeps restarting. How do you debug it?"** `get pods`, `describe` for the reason and events, `logs --previous`, then check probes and the memory limit.
- **"Why does my Java app get OOMKilled though the heap looks fine?"** Non-heap memory, `-Xmx` too close to the limit, `MaxRAMPercentage` as the fix.
- **"Requests versus limits?"** Scheduling versus ceiling; CPU throttles, memory kills.
- **"How does a Service find its Pods?"** Labels and selectors, ready Pods only, stable DNS name.

In system design rounds, name the pieces when you draw boxes. Say "the API runs as a Deployment behind a Service, with an HPA on CPU". Add "the database is managed, outside the cluster". It shows you know where state lives.

## Worked example

Here is the whole spoke-api Deployment, with every lesson from this page in one file. Read it top to bottom.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spoke-api
  namespace: spoke
spec:
  replicas: 30
  minReadySeconds: 10
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 0
  selector:
    matchLabels:
      app: spoke-api
  template:
    metadata:
      labels:
        app: spoke-api
    spec:
      terminationGracePeriodSeconds: 45
      containers:
        - name: api
          image: registry.spoke.example/spoke-api:2.5.1
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: spoke-api-config
          env:
            - name: JAVA_TOOL_OPTIONS
              value: "-XX:MaxRAMPercentage=75"
          resources:
            requests:
              cpu: "500m"
              memory: "1Gi"
            limits:
              memory: "1Gi"
          startupProbe:
            httpGet: { path: /actuator/health/liveness, port: 8081 }
            periodSeconds: 5
            failureThreshold: 24
          livenessProbe:
            httpGet: { path: /actuator/health/liveness, port: 8081 }
            periodSeconds: 10
            failureThreshold: 3
          readinessProbe:
            httpGet: { path: /actuator/health/readiness, port: 8081 }
            periodSeconds: 5
            failureThreshold: 2
          lifecycle:
            preStop:
              exec:
                command: ["sh", "-c", "sleep 10"]
```

Line by line, in groups:

1. **`replicas: 30` and `selector`**: keep 30 Pods labelled `app: spoke-api`. If one dies at 3 a.m., the ReplicaSet replaces it. That alone ends Kakashi's night shifts.
2. **`strategy`**: during a deploy, up to 8 extra Pods start, and never fewer than 30 serve. `minReadySeconds: 10` makes each new Pod prove itself for 10 s before the next swap.
3. **`image` with an exact tag**: `2.5.1`, never `latest`, so every Pod runs the same build and `rollout undo` knows what "previous" means.
4. **`envFrom` and `env`**: settings come from the ConfigMap. `MaxRAMPercentage=75` gives a 768 MiB heap inside the 1 GiB limit.
5. **`resources`**: each Pod reserves half a core and 1 GiB. Memory request equals limit, so the Pod never relies on memory it might not get.
6. **`startupProbe`**: up to 24 × 5 s = 2 minutes for Spring to start before liveness begins.
7. **`livenessProbe`**: restart only after 3 failures, 10 s apart, of the app's own liveness: never the database.
8. **`readinessProbe`**: out of the Service after 2 failures; back in when it passes. This also gates every rollout.
9. **`preStop` and `terminationGracePeriodSeconds`**: 10 s to leave the Service, up to 25 s for Spring to finish requests, all inside 45 s.

Add the Service, the Ingress, the ConfigMap and an HPA with `minReplicas: 10` and `maxReplicas: 60`, and the HPA takes over the replica count. One more detail: when an HPA manages a Deployment, leave `replicas` out of the file you apply. Otherwise every apply resets the count the HPA chose.

## Try it

A controller's whole job fits in a loop. Run this model of a ReplicaSet controller.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int desired = 3;                               // spec.replicas in the Deployment
        List<String> running = new ArrayList<>(List.of("spoke-api-a", "spoke-api-b", "spoke-api-c"));
        char nextName = 'd';

        for (int tick = 1; tick <= 4; tick++) {
            if (tick == 2) {
                running.remove("spoke-api-b");         // a node dies and takes this Pod with it
                System.out.println("tick 2: spoke-api-b died");
            }
            if (tick == 3) {
                desired = 5;                           // someone applies replicas: 5
                System.out.println("tick 3: desired is now 5");
            }
            // The controller's whole job: compare what is with what should be, then close the gap.
            while (running.size() < desired) {
                String pod = "spoke-api-" + nextName++;
                running.add(pod);
                System.out.println("tick " + tick + ": created " + pod);
            }
            while (running.size() > desired) {
                System.out.println("tick " + tick + ": deleted " + running.remove(running.size() - 1));
            }
            System.out.println("tick " + tick + ": running " + running.size() + " " + running);
        }
    }
}
```

1. Add a change at tick 4 that sets `desired` to 2. Which Pods go?
2. At tick 2, remove two Pods instead of one. Does the controller care how the gap appeared?
3. The replacement for `spoke-api-b` is called `spoke-api-d`. What does that tell you about relying on a Pod's name or IP?

## Recap

- Kubernetes runs containers across a cluster of nodes. You declare desired state in YAML with `kubectl apply`, and controllers reconcile actual state to match: scheduling, self-healing, scaling and rollouts.
- A Deployment manages ReplicaSets, which keep N Pods running. Pods are ephemeral, each with its own IP. A Service gives them one stable DNS name, and an Ingress or Gateway lets outside traffic in. Labels and selectors tie it all together.
- ConfigMaps hold settings and Secrets hold keys, but Secrets are only base64: keep them out of git and prefer a secret manager.
- Startup, liveness and readiness probes have different jobs. Liveness must never check the database; readiness gates traffic and rollouts.
- Requests decide placement, limits are ceilings: CPU throttles, memory kills with exit code 137. Use `MaxRAMPercentage` for the JVM. Roll out with `maxSurge` and `maxUnavailable`, shut down gracefully after SIGTERM, autoscale with an HPA, and debug with `describe` and `logs --previous`.

## Common mistakes

**1. A liveness probe that checks the database.**

```yaml
livenessProbe:
  httpGet: { path: /actuator/health, port: 8081 }     # includes the database check
```

Symptom: the database slows down for a minute, and every Pod restarts at once. The cold restarts pile even more load on the database: a restart storm. Fix: point liveness at `/actuator/health/liveness`, which checks only the app itself. Keep dependencies out of liveness.

**2. Setting -Xmx at the memory limit.**

```yaml
env:
  - name: JAVA_TOOL_OPTIONS
    value: "-Xmx1g"          # the container's limit is also 1Gi
```

Symptom: Pods die under load with `OOMKilled` and exit code 137. There is no `OutOfMemoryError` and no stack trace, because the kernel killed the whole container. Fix: `-XX:MaxRAMPercentage=75`, which leaves a quarter of the limit for memory outside the heap.

**3. Committing a Secret to git.**

```yaml
kind: Secret
data:
  api-key: cGF5LWtleS1saXZlLTc=     # "encrypted"?
```

`printf 'cGF5LWtleS1saXZlLTc=' | base64 -d` prints `pay-key-live-7`. Anyone with the repo has the live payment key. Fix: keep Secrets out of git, rotate the leaked key at once, and load secrets from a secret manager.

**4. Forgetting --previous.**

```bash
kubectl logs spoke-api-7d9f8-x2k4q
```

Symptom: the Pod is in `CrashLoopBackOff`, but its logs show a normal startup and nothing else. You are reading the fresh container, which has not crashed yet. Fix: `kubectl logs <pod> --previous` shows the container that crashed, with its exception.

**5. Shutting down without a pause.**

```yaml
terminationGracePeriodSeconds: 30
# no preStop hook
```

Symptom: every deploy, a few riders get connection errors for a second or two. Spring stopped at SIGTERM while some load balancers still sent it traffic. Fix: a `preStop` hook that sleeps about 10 s, `server.shutdown=graceful`, and a grace period longer than the two together.

**6. Deploying the `latest` tag.**

```yaml
image: registry.spoke.example/spoke-api:latest
```

Symptom: Pods started at different times run different code, and `rollout undo` goes back to... `latest` again. Applying the same file changes nothing, so a new build is never rolled out. Fix: an exact version tag for every build, such as `2.5.1`, as "build once, deploy many" in module 7 taught.
