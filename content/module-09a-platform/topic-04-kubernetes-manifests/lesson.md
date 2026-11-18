## Hook

A building plan can look perfect on paper and still put the front door on the wrong wall. Builders catch that with a plan check, before anyone pours concrete.

Spoke's first hand-written Kubernetes manifests had two wrong walls. The Service pointed at Pods that did not exist, and the memory limit was too small for Java. Both were visible in the YAML. Nobody checked it. This ticket teaches you to write manifests that are right, and to build the check that catches the next mistake before it reaches the cluster.

## Concept

### From whiteboard to real files

[Running on Kubernetes](/t/module-09-architecture/topic-10-kubernetes) explained the ideas: Pods, Deployments, Services, probes, limits and rolling updates. This ticket is the hands-on half. You write every field yourself, learn the exact numbers Kubernetes uses, and check the files before they ship.

Spoke keeps its manifests in git, next to the code, in a `k8s/` folder:

```text
spoke-api/
  k8s/
    deployment.yaml      # how to run the Pods
    service.yaml         # one stable address in front of them
    pdb.yaml             # how many may be stopped for maintenance
    hpa.yaml             # how many to run, from 3 to 30
```

```callout jargon
A **manifest** is a YAML file that describes Kubernetes objects. One file can hold several objects, separated by a line with three dashes, `---`. Each part is called a YAML **document**.
```

The [GitHub Actions](/t/module-09a-platform/topic-03-github-actions) workflow builds and pushes the image. These files tell Kubernetes how to run it.

### Syntax: a Deployment manifest

```yaml
apiVersion: apps/v1               # the API group and version for Deployments
kind: Deployment
metadata:
  name: spoke-api
  namespace: spoke
  labels: { app: spoke-api }      # labels on the Deployment object itself
spec:
  replicas: 3                     # how many Pods
  revisionHistoryLimit: 10        # old ReplicaSets kept for rollback (10 is the default)
  selector:
    matchLabels: { app: spoke-api }   # which Pods this Deployment owns
  template:                       # the Pod template: every Pod is made from this
    metadata:
      labels: { app: spoke-api }
    spec:
      containers:
        - name: api
          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
```

Every object has the same four top-level keys: `apiVersion`, `kind`, `metadata` and `spec`. Inside a Deployment's `spec` there is a second, nested `spec`: the Pod's own. Keep the two apart in your head: the outer one is about copies and updates, the inner one about containers.

You rarely start from a blank page. kubectl can write a starting file for you, with no cluster at all. This is a real run:

```bash
kubectl create deployment spoke-api --image=ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e --replicas=3 --port=8080 --dry-run=client -o yaml
```

```text
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: spoke-api
  name: spoke-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spoke-api
  strategy: {}
  template:
    metadata:
      labels:
        app: spoke-api
    spec:
      containers:
      - image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
        name: spoke-api
        ports:
        - containerPort: 8080
        resources: {}
status: {}
```

It is a skeleton. The empty `resources: {}` and the missing probes are exactly what the rest of this page fills in.

### Selector and template labels: why they must match

A Deployment finds its Pods by label, not by name. `spec.selector.matchLabels` says "I own every Pod with these labels". `spec.template.metadata.labels` stamps labels on each new Pod. If the template's Pods do not carry the selector's labels, the Deployment would create Pods it cannot see, then create more, forever.

So the API server refuses a Deployment whose selector does not match its own template. The rule is "subset": every selector label must be on the template, and the template may have extra ones.

```yaml
  selector:
    matchLabels: { app: spoke-api }            # the selector
  template:
    metadata:
      labels: { app: spoke-api, track: stable }   # fine: has app=spoke-api, plus an extra
```

Two more facts that bite:

- **The selector is immutable.** Once the Deployment exists, you cannot change `matchLabels`. To change it, you delete and recreate the Deployment.
- **Keep the selector small.** Put stable labels like `app` in it. Never put a version label there, or every release would need a new Deployment.

```callout jargon
**Immutable** means it cannot be changed after creation. Kubernetes rejects any apply that tries.
```

### Containers, named ports and env

```yaml
      containers:
        - name: api
          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
          ports:
            - name: http              # a name other objects can use
              containerPort: 8080
            - name: management        # Actuator, from the Observability ticket
              containerPort: 8081
          env:
            - name: SPOKE_CITY
              valueFrom:
                configMapKeyRef: { name: spoke-api-config, key: city }
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef: { name: spoke-db, key: password }
          envFrom:
            - configMapRef: { name: spoke-api-config }   # every key becomes a variable
```

- **`containerPort` is documentation.** The app listens wherever it listens. But naming the port lets probes and Services say `management` instead of `8081`. Change the number once, here, and everything follows.
- A port name has at most 15 characters: lowercase letters, digits and dashes.
- **`valueFrom`** pulls one key from a ConfigMap or a Secret. **`envFrom`** pulls all of them.
- Spring Boot reads `SPRING_DATASOURCE_PASSWORD` as the property `spring.datasource.password`.

Environment variables are read once, when the container starts. Changing the ConfigMap does not change running Pods. `kubectl rollout restart deployment/spoke-api` replaces them, a few at a time. A later ticket, [Configuration and secrets](/t/module-09a-platform/topic-07-config-and-secrets), goes deeper.

### Image tags and digests

An image reference has up to four parts:

```text
registry:    ghcr.io
repository:  spoke/spoke-api
tag:         2.6.0-3f9c2b7a1d4e      (version 2.6.0, commit 3f9c2b7a1d4e)
digest:      sha256:9b1e...c07a
written as:  ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e@sha256:9b1e...c07a
```

- A **tag** is a name a person chose. It can be moved: someone can push a different build under the same tag. Some registries can forbid that, called **tag immutability**.
- A **digest** is the SHA-256 hash of the image's contents. It cannot move. If both are written, the digest wins.
- No tag at all means the tag `latest`, which every new build moves.

```callout jargon
A **digest** is a fingerprint of the exact image bytes, written `sha256:` plus 64 hex characters. Two different builds can never share one.
```

Kubernetes also picks a pull rule from the tag. When `imagePullPolicy` is left out, `latest` or no tag means `Always`: download on every start. Any other tag means `IfNotPresent`: reuse the copy on the node. That is one reason a rollback is quick: the previous image is usually still on the node, so nothing is downloaded.

The rule for Spoke: every release image is tagged with its version and the first 12 hex characters of its git commit, such as `2.6.0-3f9c2b7a1d4e`. Add a digest where you can. A bare `2.6.0` is not a release tag. The `ManifestCheck` you build is only a first filter: it rejects `latest` and missing tags. Watch the colon: in `localhost:5000/spoke-api`, the `:5000` is the registry's port, not a tag. The tag is only ever after the last `/`.

### Probes wired to Actuator

Spoke serves Actuator on port 8081, named `management`. Each probe asks one question:

```yaml
          startupProbe:
            httpGet: { path: /actuator/health/liveness, port: management }
            periodSeconds: 5
            failureThreshold: 24
          livenessProbe:
            httpGet: { path: /actuator/health/liveness, port: management }
            periodSeconds: 10
            timeoutSeconds: 2
            failureThreshold: 3
          readinessProbe:
            httpGet: { path: /actuator/health/readiness, port: management }
            periodSeconds: 5
            failureThreshold: 2
```

- Until the **startup** probe passes once, the other two are switched off.
- **Liveness** checks only the app's own state. The liveness group never includes the database, Redis or Kafka.
- **Readiness** may include things the Pod needs to serve. Spring adds its own state here, `OUT_OF_SERVICE` while starting or stopping.

Never point liveness at plain `/actuator/health`. That endpoint includes the `db` check, so a slow database would restart every Pod at once. The checker you build flags it, along with any path that has a `db`, `database`, `redis` or `kafka` part.

### Probe timing: the arithmetic

Every probe has five timing fields. These are the defaults:

| Field | Default | Meaning |
|---|---|---|
| `initialDelaySeconds` | 0 | wait this long after the container starts |
| `periodSeconds` | 10 | ask this often |
| `timeoutSeconds` | 1 | an answer slower than this counts as a failure |
| `failureThreshold` | 3 | this many failures in a row and the probe has failed |
| `successThreshold` | 1 | this many passes in a row to count as healthy again (must be 1 for liveness and startup) |

Two sums matter. A stuck app is restarted after about `failureThreshold × periodSeconds`. A slow start gets `failureThreshold × periodSeconds` of the startup probe to finish.

```animate
title: How long until Kubernetes acts?
kind: code
code: |
  int periodSeconds = 10;
  int failureThreshold = 3;
  int livenessSeconds = failureThreshold * periodSeconds;
  System.out.println("liveness: restart after about " + livenessSeconds + " s");
  int startupPeriod = 5;
  int startupFailures = 24;
  int startupBudget = startupFailures * startupPeriod;
  System.out.println("startup: up to " + startupBudget + " s to start");
steps:
  - line: 1
    vars: { periodSeconds: 10 }
    say: Liveness asks every 10 seconds.
  - line: 2
    vars: { failureThreshold: 3 }
    say: It needs 3 failures in a row before it gives up on the container.
  - line: 3
    vars: { livenessSeconds: 30 }
    say: So a stuck app is restarted about 30 seconds after it stops answering. Plus up to one timeout.
  - line: 4
    out: "liveness: restart after about 30 s"
    say: Shorter means faster recovery, but more restarts by mistake during a long pause.
  - line: 7
    vars: { startupPeriod: 5, startupFailures: 24, startupBudget: 120 }
    say: The startup probe asks every 5 seconds, up to 24 times. That is a two-minute budget.
  - line: 8
    out: "startup: up to 120 s to start"
    say: Size it from the slowest start you have measured, with room to spare. Liveness waits for it.
```

Measure first. If spoke-api's slowest start in staging was 50 s, a 120 s startup budget is safe. Do not use `initialDelaySeconds` on liveness for this: a startup probe ends early when the app is fast, a delay never does.

### Requests, limits and QoS classes

Requests reserve, limits cap: CPU over its limit is slowed, memory over its limit is killed. From those numbers, Kubernetes puts every Pod in a **Quality of Service class**:

| QoS class | When | Under memory pressure |
|---|---|---|
| **Guaranteed** | every container has CPU **and** memory limits, each equal to its request | evicted last |
| **Burstable** | not Guaranteed, but at least one request or limit is set | in the middle; those using more than they requested go first |
| **BestEffort** | no requests or limits at all | evicted first |

```callout jargon
**Eviction** is the node stopping Pods to free memory or disk when it runs short. The Pods are then recreated somewhere else.
```

Spoke's usual setting is memory request equal to memory limit, and a CPU request with no CPU limit. That avoids CPU throttling. It is **Burstable**, not Guaranteed, because CPU has no limit. That is a fine trade for a web API. See a Pod's class with `kubectl get pod <name> -o jsonpath='{.status.qosClass}'`.

Where do the numbers come from? Not guesses. Run the Pod under a realistic load in staging and read its real usage from the dashboards in [Observability](/t/module-09-architecture/topic-08-observability). Set the memory request a little above the peak, and the CPU request near the normal busy level.

### Memory limit vs MaxRAMPercentage

The JVM's memory is more than its heap. Loaded classes, compiled code, thread stacks and network buffers all live outside it. The container's limit must hold all of it.

```java
long limitMiB = 1024;          // resources.limits.memory: 1Gi
int maxRamPercentage = 75;     // -XX:MaxRAMPercentage=75
long heapMiB = limitMiB * maxRamPercentage / 100;
System.out.println("heap " + heapMiB + " MiB, everything else " + (limitMiB - heapMiB) + " MiB");
```

That prints `heap 768 MiB, everything else 256 MiB`. A Spring Boot API with a few hundred threads often needs 200 MiB or more outside the heap. If the heap is allowed to reach the limit, the rest has nowhere to go, and the kernel kills the container: **OOMKilled**, exit code 137.

Two rules:

- Use `-XX:MaxRAMPercentage`, never a fixed `-Xmx`. It follows the limit when someone changes it.
- When Pods are OOMKilled, check the gap below the limit before you blame a leak. A heap that is healthy but too close to the limit looks exactly like a leak from the outside.

### Rolling update math: surge rounds up, unavailable rounds down

`maxSurge` and `maxUnavailable` take a whole number or a percentage of `replicas`. A percentage is turned into Pods with two different roundings:

- **`maxSurge` rounds up.** 25% of 10 is 2.5, so 3 extra Pods may exist.
- **`maxUnavailable` rounds down.** 25% of 10 is 2.5, so only 2 Pods may be missing.

Both round toward safety: more room to start new Pods, fewer Pods missing.

```animate
title: 25% of 10 Pods, two ways
kind: code
code: |
  int replicas = 10;
  double exact = replicas * 25 / 100.0;
  int surge = (int) Math.ceil(exact);
  int unavailable = (int) Math.floor(exact);
  System.out.println("maxSurge " + surge + ", maxUnavailable " + unavailable);
  System.out.println("Pods: " + (replicas - unavailable) + " to " + (replicas + surge));
steps:
  - line: 1
    vars: { replicas: 10 }
    say: spoke-api runs 10 Pods, with the default 25% for both settings.
  - line: 2
    vars: { exact: '2.5' }
    say: 25% of 10 is 2.5 Pods. Half a Pod cannot exist, so Kubernetes must round.
  - line: 3
    vars: { surge: 3 }
    say: maxSurge rounds up, to 3. Up to 3 extra Pods may start at once.
  - line: 4
    vars: { unavailable: 2 }
    say: maxUnavailable rounds down, to 2. At most 2 Pods may be missing.
  - line: 5
    out: "maxSurge 3, maxUnavailable 2"
    say: The same 25% gives two different numbers.
  - line: 6
    out: "Pods: 8 to 13"
    say: During the whole rollout, at least 8 Pods are ready and at most 13 exist.
```

The surge Pods need room. If the cluster has no free space for 3 more Pods, the new ones stay `Pending` and the rollout waits. Small Deployments surprise people: 25% of 3 Pods is 0.75, so maxSurge is 1 and maxUnavailable is **0**.

### The zero-zero rule

Set both `maxSurge: 0` and `maxUnavailable: 0`, and no Pod could ever be replaced: no room to add, no permission to remove. The API server refuses that Deployment. `0%` counts as zero too.

Rounding can still produce two zeros. `maxSurge: 0` and `maxUnavailable: 25%` with 3 replicas gives 0 and 0.75, which rounds down to 0. Here Kubernetes quietly lets **1** Pod be unavailable, so the rollout can move. Also, maxUnavailable can never be more than `replicas`, and a percentage above 100% is refused.

```callout tip
Gojo says: for a small Deployment, write whole numbers, such as `maxSurge: 1` and `maxUnavailable: 0`. Then nobody has to work out the rounding at 3 a.m.
```

### PodDisruptionBudget: surviving node maintenance

Nodes get upgraded and replaced. Before that, an operator **drains** the node: its Pods are evicted and rescheduled elsewhere. If all three spoke-api Pods sit on that node, all three go at once.

A **PodDisruptionBudget** (PDB) limits that. This is a real run of kubectl's generator:

```bash
kubectl create poddisruptionbudget spoke-api --selector=app=spoke-api --min-available=2 --dry-run=client -o yaml
```

```text
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: spoke-api
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: spoke-api
status:
  currentHealthy: 0
  desiredHealthy: 0
  disruptionsAllowed: 0
  expectedPods: 0
```

Drain asks before each eviction. If evicting a Pod would leave fewer than 2 healthy, drain waits and retries until a replacement is ready elsewhere. Use `minAvailable` or `maxUnavailable`, never both.

Know its limits. A PDB covers **voluntary** disruptions: drains and evictions. It does not stop a node that crashes, and it does not slow down your own rolling updates. Those follow `maxUnavailable`.

### Service: port, targetPort and named ports

```yaml
apiVersion: v1
kind: Service
metadata:
  name: spoke-api
  namespace: spoke
spec:
  type: ClusterIP             # the default: one virtual IP, inside the cluster only
  selector: { app: spoke-api }
  ports:
    - name: http
      port: 80                # what callers use: http://spoke-api
      targetPort: http        # where it goes on the Pod: the port named http, 8080
```

- **`port`** is the Service's own port. Callers use it.
- **`targetPort`** is the Pod's port. It can be a number or a port **name**. Leave it out and it equals `port`, a classic way to send traffic to port 80 on a Pod that listens on 8080.
- The Service keeps a list of ready Pods that match its selector, in objects called **EndpointSlices**. `kubectl get endpointslices -l kubernetes.io/service-name=spoke-api` shows it. An empty list means the selector matches nothing, or no Pod is ready.

The selector rule is the same subset rule as the Deployment's. A Service selecting `app: spoke` never sends traffic to Pods labelled `app: spoke-api`. Kubernetes does not warn you, because a Service with no Pods yet is allowed.

### Syntax: HorizontalPodAutoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: spoke-api
  namespace: spoke
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: spoke-api }
  minReplicas: 3
  maxReplicas: 30
  metrics:
    - type: Resource
      resource:
        name: cpu
        target: { type: Utilization, averageUtilization: 60 }
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300    # the default: look back 5 minutes before shrinking
      policies:
        - { type: Percent, value: 10, periodSeconds: 60 }   # remove at most 10% a minute
```

- `averageUtilization` is a percentage of each Pod's **CPU request**. No request, no autoscaling.
- `behavior` slows scaling down, so a quiet minute does not throw away Pods needed the next minute. Scaling up has no wait by default.
- Do not scale a Java API on memory. The heap grows and rarely shrinks, so memory usage says little about load.
- The HPA owns the replica count. Leave `replicas` out of the Deployment, or every apply resets it. Then `minReplicas` is your real floor, so keep it at 2 or more.
- Resource metrics need the **metrics-server** add-on in the cluster. Managed clusters usually include it.

### securityContext: run as nobody

If an attacker breaks into the app, the `securityContext` decides how much they can do next:

```yaml
    spec:
      securityContext:                 # Pod level: applies to every container
        runAsNonRoot: true
        runAsUser: 10001
        seccompProfile: { type: RuntimeDefault }
      containers:
        - name: api
          securityContext:             # container level: overrides the Pod's settings
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities: { drop: ["ALL"] }
          volumeMounts:
            - { name: tmp, mountPath: /tmp }
      volumes:
        - { name: tmp, emptyDir: {} }
```

- **`runAsNonRoot: true`**: the kubelet refuses to start a container whose user is root. The Pod shows `CreateContainerConfigError`. To check, it needs a numeric user id, from `runAsUser` or the image's `USER` line, as in [Production Dockerfiles](/t/module-09a-platform/topic-01-dockerfiles).
- **`readOnlyRootFilesystem: true`**: nobody can change files inside the container. Spring Boot's Tomcat writes temp files, so mount an empty, writable `emptyDir` at `/tmp`.
- **`allowPrivilegeEscalation: false`**: a process cannot gain more rights than its parent.
- **`capabilities: drop ALL`**: removes Linux's special powers, such as changing network settings.
- **`seccompProfile: RuntimeDefault`**: a filter from the container runtime that blocks rare, risky requests from the app to the Linux kernel.

A container's setting wins over the Pod's. `runAsNonRoot`, `allowPrivilegeEscalation: false`, dropping all capabilities and `seccompProfile` are what the Kubernetes **restricted** Pod Security Standard requires. A read-only root filesystem is extra hardening on top. A namespace can enforce the standard with the label `pod-security.kubernetes.io/enforce: restricted`.

### Spreading Pods across nodes and zones

Three replicas on one node is one point of failure. Two tools spread them out:

```yaml
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: topology.kubernetes.io/zone
          whenUnsatisfiable: ScheduleAnyway
          labelSelector: { matchLabels: { app: spoke-api } }
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                topologyKey: kubernetes.io/hostname
                labelSelector: { matchLabels: { app: spoke-api } }
```

- **Topology spread**: keep the count per zone within `maxSkew` of each other. With 3 zones and 6 Pods, that is 2, 2, 2.
- **Pod anti-affinity**: prefer not to put two spoke-api Pods on the same node.
- **Preferred or required**: `ScheduleAnyway` and `preferred...` are wishes. `DoNotSchedule` and `required...` are rules, and a rule nobody can meet leaves Pods `Pending`.

```callout jargon
A **zone** is one data centre building in a cloud region. Zones fail separately, so spreading across them survives a building losing power.
```

### Validating before the cluster

Each check catches different mistakes. Run the cheap ones first.

| Command | Needs a cluster? | Catches |
|---|---|---|
| YAML parse (any YAML library) | no | bad indentation, tabs, broken quotes |
| schema validators such as **kubeconform** | no | unknown fields, wrong types, such as `replicas: "3"` |
| `kubectl apply --dry-run=client -f` | yes, to read the schema | the same, using the live cluster's schema |
| `kubectl apply --dry-run=server -f` | yes | everything the API server checks: selector mismatch, immutable fields, admission policies. Nothing is saved |
| `kubectl diff -f` | yes | shows exactly what would change in the live objects |

"Client" sounds offline, but it is not. This is a real run on a laptop with no cluster, `KUBECONFIG=/dev/null`:

```text
error: error validating "deploy.yaml": error validating data: failed to download openapi: Get "http://localhost:8080/openapi/v2?timeout=32s": dial tcp [::1]:8080: connect: connection refused; if you choose to ignore these errors, turn validation off with --validate=false
```

kubectl wanted to download the schema from the API server. `kubectl diff` exits with 0 when nothing would change, 1 when something would, and above 1 on an error. That makes it easy to use in a pipeline.

### Checks in CI: policy as code

Schema tools check that a file is **valid**. They cannot know Spoke's rules: two replicas at least, pinned images, probes, no root. That is **policy as code**: team rules written as a program, run on every pull request.

```callout jargon
**Policy as code** means writing rules like "every container must have a memory limit" as code that a pipeline runs, instead of as a wiki page people forget.
```

Real teams use tools like **kube-linter**, **Polaris**, **Conftest** (rules written in OPA's Rego language) or **Kyverno**. Kyverno can also enforce the rules inside the cluster, rejecting a bad apply. In this ticket you write a small one in Java, `ManifestCheck`, and the rollout calculator `RolloutMath`. The rules are the knowledge: once you can write them, you can review any manifest.

The pipeline order: parse, schema, policy, then `--dry-run=server` and `kubectl diff` against staging, then apply.

### In an interview

Kubernetes questions at SDE 2 level go past the vocabulary. Expect to write and reason about real YAML.

- **"Write a Deployment for a stateless API."** Cover the four top-level keys, the selector and labels, and probes on a management port. Add requests, limits and a version tag.
- **"Liveness, readiness and startup: what are the numbers?"** Restart after about `failureThreshold × periodSeconds`; startup budget the same sum; liveness never checks dependencies.
- **"10 replicas, 25% and 25%. What happens?"** 3 surge, 2 unavailable, 8 to 13 Pods. Say which way each rounds and why.
- **"Our Java Pods get OOMKilled. Why?"** Non-heap memory, heap too close to the limit, `MaxRAMPercentage`. Mention QoS classes for eviction.
- **"How do you keep a service up during node upgrades?"** PDB, spreading across zones, at least 2 replicas.
- **"How do you stop bad manifests reaching production?"** Schema validation, policy as code, server dry-run and diff in CI.

## Worked example

Here is the ticket's staging file, trimmed to the two bugs:

```yaml
# Deployment (part)
  template:
    metadata:
      labels: { app: spoke-api }
    spec:
      containers:
        - name: api
          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
          env:
            - { name: JAVA_TOOL_OPTIONS, value: "-Xmx512m" }
          resources:
            limits: { memory: 512Mi }
---
# Service (part)
spec:
  selector: { app: spoke }
  ports:
    - { port: 80, targetPort: 8080 }
```

**Bug 1: the Service selects nothing.** It asks for `app: spoke`, and the Pods say `app: spoke-api`. Watch a request try to get through:

```animate
title: A Service with no Pods behind it
kind: scene
nodes:
  - { id: billing, label: Billing service, emoji: "🧾", col: 0 }
  - { id: svc, label: "Service spoke-api", emoji: "🔀", value: "selector app=spoke", col: 2 }
  - { id: p1, label: "Pod 1", emoji: "📦", value: "app=spoke-api", row: 1, col: 1 }
  - { id: p2, label: "Pod 2", emoji: "📦", value: "app=spoke-api", row: 1, col: 2 }
  - { id: p3, label: "Pod 3", emoji: "📦", value: "app=spoke-api", row: 1, col: 3 }
steps:
  - say: Three healthy Pods run, each labelled app=spoke-api. Their readiness probes pass.
    focus: [p1, p2, p3]
  - say: The Service looks for Pods labelled app=spoke. None has that label, so its list of endpoints is empty.
    set: { svc: "selector app=spoke, 0 endpoints" }
  - say: Billing calls http://spoke-api. The Service has nowhere to send the request, and the call fails.
    send: { from: billing, to: svc, label: "GET /rentals/rental-17" }
  - say: The fix is one word. The selector becomes app=spoke-api, and all three Pods join the list.
    set: { svc: "selector app=spoke-api, 3 endpoints" }
  - say: The same call now reaches a ready Pod.
    send: [{ from: billing, to: svc, label: "GET /rentals/rental-17" }, { from: svc, to: p2, label: forward }]
```

**Bug 2: the heap fills the limit.** `-Xmx512m` lets the heap alone reach 512 MiB. The limit is also 512 MiB. Under load the heap grew toward its maximum, the non-heap memory pushed past the limit, and the kernel killed the container. No Java error was logged, because Java never got the chance.

Here is the fixed set, written as Spoke's checker expects:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spoke-api
  namespace: spoke
spec:
  selector:
    matchLabels: { app: spoke-api }
  strategy:
    type: RollingUpdate
    rollingUpdate: { maxSurge: 1, maxUnavailable: 0 }
  template:
    metadata:
      labels: { app: spoke-api }
    spec:
      securityContext: { runAsNonRoot: true, runAsUser: 10001 }
      containers:
        - name: api
          image: ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
          ports:
            - { name: http, containerPort: 8080 }
            - { name: management, containerPort: 8081 }
          env:
            - { name: JAVA_TOOL_OPTIONS, value: "-XX:MaxRAMPercentage=75" }
          resources:
            requests: { cpu: 500m, memory: 1Gi }
            limits: { memory: 1Gi }
          startupProbe:
            httpGet: { path: /actuator/health/liveness, port: management }
            periodSeconds: 5
            failureThreshold: 24
          livenessProbe:
            httpGet: { path: /actuator/health/liveness, port: management }
            periodSeconds: 10
            failureThreshold: 3
          readinessProbe:
            httpGet: { path: /actuator/health/readiness, port: management }
            periodSeconds: 5
            failureThreshold: 2
---
apiVersion: v1
kind: Service
metadata:
  name: spoke-api
  namespace: spoke
spec:
  selector: { app: spoke-api }
  ports:
    - { name: http, port: 80, targetPort: http }
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: spoke-api
  namespace: spoke
spec:
  scaleTargetRef: { apiVersion: apps/v1, kind: Deployment, name: spoke-api }
  minReplicas: 3
  maxReplicas: 30
  metrics:
    - type: Resource
      resource: { name: cpu, target: { type: Utilization, averageUtilization: 60 } }
```

Line by line, in groups:

1. **Selector, template labels, Service selector**: all `app: spoke-api`. One label, three places, checked by `SELECTOR_MISMATCH` and `SERVICE_SELECTS_NOTHING`.
2. **No `replicas`**: the HPA owns the count, and its `minReplicas: 3` is the floor.
3. **`targetPort: http`**: a name, so the Service follows the container port if it ever changes.
4. **Memory**: 1 GiB limit, heap capped at 75%, so 256 MiB stays free for the rest of the JVM.
5. **Probes**: on the named `management` port; liveness never touches the database.
6. **`maxSurge: 1, maxUnavailable: 0`**: one extra Pod at a time, never fewer than the current count serving.

## Try it

This program computes a rollout's limits and a simple step-by-step plan. It assumes new Pods become ready at once.

```java run
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int replicas = 10;
        int surgePercent = 25;          // maxSurge: 25%
        int unavailablePercent = 25;    // maxUnavailable: 25%

        int surge = (int) Math.ceil(replicas * surgePercent / 100.0);              // surge rounds UP
        int unavailable = (int) Math.floor(replicas * unavailablePercent / 100.0); // unavailable rounds DOWN
        if (surge == 0 && unavailable == 0) {
            unavailable = 1;            // Kubernetes never lets a rollout get stuck at 0 and 0
        }
        int maxPods = replicas + surge;
        int minAvailable = replicas - unavailable;
        System.out.println("maxSurge " + surge + ", maxUnavailable " + unavailable);
        System.out.println("between " + minAvailable + " and " + maxPods + " Pods at every moment");

        int oldPods = replicas;
        int newPods = 0;
        List<String> steps = new ArrayList<>();
        steps.add(oldPods + " old + " + newPods + " new");
        while (oldPods > 0 || newPods < replicas) {
            int add = Math.min(replicas - newPods, maxPods - (oldPods + newPods));
            if (add > 0) {
                newPods += add;
                steps.add(oldPods + " old + " + newPods + " new   (started " + add + ")");
            }
            int remove = Math.min(oldPods, (oldPods + newPods) - minAvailable);
            if (remove > 0) {
                oldPods -= remove;
                steps.add(oldPods + " old + " + newPods + " new   (stopped " + remove + ")");
            }
        }
        steps.forEach(System.out::println);
    }
}
```

1. Change `replicas` to 3. Why does no old Pod stop until a new one has started?
2. Set `surgePercent` to 0 with 3 replicas. Which line saves the rollout from never ending?
3. Set both percentages to 50 with 10 replicas. How many steps does the rollout take now, and what is the price?

## Recap

- A Deployment's selector must be a subset of its template labels, and a Service's selector must match the Pod labels too. Name ports, and point `targetPort` and probes at the names.
- Pin images to a unique tag or a digest; the tag is only after the last `/`. Wire startup, liveness and readiness to Actuator, and size them with `failureThreshold × periodSeconds`. Liveness never checks a dependency.
- Requests and limits set the QoS class: Guaranteed, Burstable or BestEffort. Keep the heap well below the memory limit with `MaxRAMPercentage`.
- In a rolling update, maxSurge rounds up and maxUnavailable rounds down. Both zero is refused, and two zeros after rounding become one unavailable Pod.
- Add a PDB, an HPA with `minReplicas` of 2 or more, a restricted `securityContext` and zone spreading. Check every manifest in CI: parse, schema, policy, server dry-run and diff.

## Common mistakes

**1. A Service selector that uses a different name.**

```yaml
# Pods: labels { app: spoke-api }
spec:
  selector: { app: spoke }
```

Symptom: the Pods are Running and ready, but every call through the Service fails. The Service's EndpointSlice lists no endpoints. Fix: copy the selector from the Pod template's labels, exactly. A check like `SERVICE_SELECTS_NOTHING` catches it in CI.

**2. Giving the heap the whole memory limit.**

```yaml
env:
  - { name: JAVA_TOOL_OPTIONS, value: "-XX:MaxRAMPercentage=100" }
resources:
  limits: { memory: 1Gi }
```

Symptom: Pods are OOMKilled, exit code 137, under load, with no Java error in the logs. The heap was allowed to fill the limit, leaving nothing for threads and classes. Fix: 75% is a good start. Raise the limit if the heap really needs more.

**3. Trusting a percentage on a tiny Deployment.**

```yaml
replicas: 3
strategy:
  rollingUpdate: { maxSurge: 25%, maxUnavailable: 25% }
```

Symptom: on a full cluster, the rollout makes no progress. 25% of 3 rounds down to 0 unavailable, so an old Pod may only stop after a new one is ready. The 1 surge Pod has no room and stays `Pending`. Fix: leave room in the cluster for surge Pods, or write `maxUnavailable: 1` if one missing Pod is acceptable.

**4. A PodDisruptionBudget that allows no disruption.**

```yaml
# replicas: 3
spec:
  minAvailable: 3
```

Symptom: a node drain never finishes. Every eviction would drop below 3, so drain keeps retrying. Fix: allow at least one disruption, with `maxUnavailable: 1` or `minAvailable` below the replica count.

**5. A read-only root filesystem with no place for temp files.**

```yaml
securityContext:
  readOnlyRootFilesystem: true
# no volume at /tmp
```

Symptom: the Pod goes into `CrashLoopBackOff`. `kubectl logs --previous` shows Spring failing at startup, because Tomcat cannot create its temp folder. Fix: mount an `emptyDir` volume at `/tmp`, as in the securityContext card.

**6. Editing the selector of a live Deployment.**

```yaml
selector:
  matchLabels: { app: spoke-api, tier: web }   # tier added later
```

Symptom: `kubectl apply` is rejected, saying the field is immutable. Fix: put new labels on the Pod template only. If the selector truly must change, create a Deployment with a new name, move traffic, then delete the old one.
