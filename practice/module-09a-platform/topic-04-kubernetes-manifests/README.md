# Kubernetes manifests: a check before the cluster

The first hand-written manifests for spoke-api had a Service that matched no Pods and a memory limit that got Pods OOMKilled. Build two tools that CI runs on every manifest before it reaches the cluster: `ManifestCheck`, which reads the YAML and reports broken rules, and `RolloutMath`, which works out what a rolling update will do.

Given to you, finished: the `Rule` enum, the records `Finding(rule, subject)`, `PodCounts(oldPods, newPods)` and `RolloutLimits(maxSurge, maxUnavailable, maxPods, minAvailable)`, and the helpers at the bottom of `ManifestCheck`: `loadDocuments`, `only`, `all`, `autoscalerFor`, `path`, `map`, `maps`, `text` and `number`. `path(doc, "spec", "replicas")` walks nested maps and gives `null` when any step is missing.

## 1. `ManifestCheck.check(String yaml)`

The file holds several YAML documents separated by `---`. Empty documents are skipped. It must hold exactly one `Deployment` (otherwise `only` throws `IllegalArgumentException`), any number of `Service`s, and maybe a `HorizontalPodAutoscaler`.

Return a list of `Finding`s, **each one at most once**, in any order. A file that breaks no rule gives an empty list. The `subject` is the Deployment's name, the container's name or the Service's name, as each rule says.

**Helpers you write first**

- **TODO 1, `selects(selector, podLabels)`:** `false` for an empty selector. Otherwise `true` only when every key of the selector is in `podLabels` with the same value. Extra Pod labels are fine. Compare values as text, `Objects.equals(text(a), text(b))`, because YAML reads `version: 2` as a number.
- **TODO 2, `pinned(image)`:** `false` for `null` or blank. `true` when the image contains `@sha256:` (a digest). Otherwise look only at the part **after the last `/`**: it is pinned when it has a `:` and the tag after it is not `latest`. So `localhost:5000/spoke-api` is **not** pinned: that `:5000` is the registry's port. This is only a first filter: Spoke's real release tags carry the version and the first 12 hex characters of the commit, such as `2.6.0-3f9c2b7a1d4e`, but any tag other than `latest` passes this rule.
- **TODO 3, `checksDependency(path)`:** `false` for `null`. Lower-case the path, drop anything from `?` on, and drop trailing `/`. Then it checks a dependency when it **equals** `/actuator/health` (the full health check, which includes the database), or when any **segment** between `/` characters is `db`, `database`, `redis` or `kafka`. `/actuator/health/db` and `/internal/redis/ping` check one; `/actuator/health/liveness` and `/feedback` do not.

**The rules**

| Rule | Subject | Reported when |
|---|---|---|
| **TODO 4** `SELECTOR_MISMATCH` | Deployment name | `selects(spec.selector.matchLabels, template labels)` is false |
| **TODO 4** `TOO_FEW_REPLICAS` | Deployment name | the replica count is below 2. If `autoscalerFor(documents, name)` finds an HPA, the count is its `spec.minReplicas`; otherwise `spec.replicas`. Missing means 1 |
| **TODO 5** `IMAGE_NOT_PINNED` | container name | `pinned(image)` is false |
| **TODO 5** `MISSING_READINESS_PROBE` | container name | no `readinessProbe` |
| **TODO 5** `MISSING_LIVENESS_PROBE` | container name | no `livenessProbe` |
| **TODO 5** `LIVENESS_CHECKS_DEPENDENCY` | container name | `checksDependency(livenessProbe.httpGet.path)` is true. Readiness is not checked by this rule |
| **TODO 6** `MISSING_REQUESTS` | container name | `resources.requests` lacks `cpu` or `memory` (one finding, even if both are missing) |
| **TODO 6** `MISSING_MEMORY_LIMIT` | container name | no `resources.limits.memory` |
| **TODO 6** `NOT_RUN_AS_NON_ROOT` | container name | the effective `runAsNonRoot` is not `true`. Effective means the container's `securityContext.runAsNonRoot` **if that key is there**, else the Pod's `spec.template.spec.securityContext.runAsNonRoot` |
| **TODO 7** `SERVICE_SELECTS_NOTHING` | Service name | `selects(spec.selector, template labels)` is false, including a Service with no selector |
| **TODO 7** `TARGET_PORT_UNKNOWN` | Service name | some entry in `spec.ports` has a target no container offers. The target is `targetPort`, or `port` when `targetPort` is missing. A number must equal some container's `containerPort`; text must equal some container port's `name` |

Container rules apply to **every** container in `spec.template.spec.containers`, sidecars included.

## 2. `RolloutMath`

`maxSurge` and `maxUnavailable` arrive as text: a whole number such as `"1"`, or a percentage of replicas such as `"25%"`.

- **TODO 8, `resolve(value, replicas, roundUp)`:** trim the text. Throw `IllegalArgumentException` for `null`, empty, negative or non-numeric values. `"4"` gives 4. `"25%"` gives 25 × replicas ÷ 100, **rounded up** when `roundUp` is true and **down** when it is false. Use whole-number maths, not `double`.
- **TODO 9, `limits(replicas, maxSurge, maxUnavailable)`:**
  - Throw `IllegalArgumentException` when `replicas < 1`; when **both** values are written as zero (`0` or `0%`); or when `maxUnavailable` is a percentage above `100%`.
  - `surge = resolve(maxSurge, replicas, true)` (rounds **up**). `unavailable = resolve(maxUnavailable, replicas, false)` (rounds **down**).
  - If both came out 0 after rounding, `unavailable` becomes 1. Then cap `unavailable` at `replicas`.
  - Return `new RolloutLimits(surge, unavailable, replicas + surge, replicas - unavailable)`. Example: 10 replicas at 25% and 25% gives `(3, 2, 13, 8)`.
- **TODO 10, `plan(replicas, maxSurge, maxUnavailable)`:** a simple model where every new Pod is ready as soon as it starts. Use `limits(...)`, then:

```text
old = replicas, new = 0, record (old, new)
until (old, new) is (0, replicas):
    add = min(replicas - new, maxPods - (old + new));        if add > 0:    new += add,    record
    remove = min(old, (old + new) - minAvailable);           if remove > 0: old -= remove, record
```

With 3 replicas, maxSurge `"1"` and maxUnavailable `"0"`, the plan is (3,0), (3,1), (2,1), (2,2), (1,2), (1,3), (0,3).

Press **Submit** in JavaQuest, or just save the file in your IDE. The checks run on their own.
