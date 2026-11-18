## What happened

Friday, 17:00. Spoke ships release 2.6.0 of `spoke-api` to the 30 pods in the `spoke` namespace. Kakashi's dashboard lights up within a minute. Three things went wrong, one after another:

1. **The wrong build ran.** CI pushed the new image as `ghcr.io/spoke/spoke-api:latest`. One node still had Tuesday's `latest` in its cache, and used it. Its pods ran 2.5.0, while the rest ran 2.6.0.
2. **Every pod stopped at once.** The rollout had `maxUnavailable: 100%`. All 30 old pods stopped together, and riders got 503s for four minutes while the new ones started.
3. **Unlocks were cut off.** Each old pod stopped the moment it got SIGTERM. Unlock requests that were half done never answered, and riders saw an error with a bike that might or might not open.

Each part had a guard rail that was missing. Your job is to build all three into Spoke's deploy tooling.

```callout jargon
An **image tag** is a name for one version of an image, such as `2.6.0` or `latest`. A **mutable** tag can be moved to point at a different image later. An **immutable** reference always means the same image.
```

### Why latest ran an old build

`latest` does not mean "newest". It is just a tag, and it points at whatever was pushed last. On Friday it moved from Tuesday's image to 2.6.0.

Spoke's manifest said `imagePullPolicy: IfNotPresent`. That tells a node: "if you already have an image with this name, use it". One node had an old `latest` from Tuesday, so it never pulled the new one. The image name was the same, so nothing looked wrong.

There was a second trap. The manifest's image line never changed between deploys, so `kubectl apply` saw no change at all. The deploy tool ran `kubectl rollout restart` instead, which restarts every pod whatever the image is.

```callout jargon
A **digest** is a fingerprint of an image's exact bytes, written `sha256:` and 64 hex characters. Change one byte, and the digest changes. So a digest can never point at a different image.
```

### Four minutes of 503s

Someone had set `maxUnavailable: 100%` to make restarts "quicker". Kubernetes accepts that value. It means "you may stop every pod at the same time".

```animate
title: Friday's rollout, at 17:00
kind: scene
nodes:
  - { id: lb, label: Load balancer, emoji: "🔀", value: "30 ready pods", row: 0, col: 2 }
  - { id: old, label: "30 pods, 2.5.0", emoji: "🟦", value: ready, row: 1, col: 1 }
  - { id: new, label: "30 pods, 2.6.0", emoji: "🟩", value: starting, row: 1, col: 3 }
  - { id: riders, label: Riders, emoji: "📱", value: "unlocking", row: 0, col: 0 }
steps:
  - say: Riders unlock bikes as usual. The load balancer spreads their requests over 30 ready pods.
    send: [{ from: riders, to: lb, label: "POST /rentals" }, { from: lb, to: old, label: requests }]
  - say: The rollout starts. maxUnavailable 100% lets every old pod stop in the first wave.
    set: { old: stopping, lb: "0 ready pods" }
    focus: [old]
  - say: All 30 new pods start together. Spring takes time to start, and none of them is ready yet.
    show: [new]
    set: { old: gone }
  - say: The load balancer has nowhere to send requests. Every rider gets a 503.
    send: { from: lb, to: riders, label: "503", back: true }
    set: { riders: "503 for 4 minutes" }
  - say: A safe plan never lets the ready pods fall below a floor. Spoke needs at least 24 of 30 serving.
    set: { lb: "floor: 24 ready" }
    focus: [lb]
```

### Cut off mid-unlock

When Kubernetes stops a pod, it sends **SIGTERM** first. That is a polite "please stop". The pod should stop taking new requests, finish the ones it has, then exit. Kubernetes waits up to `terminationGracePeriodSeconds` before it sends **SIGKILL**, which cannot be refused.

Spoke's `RequestGate` stopped the moment SIGTERM arrived. Kubernetes does take a stopping pod out of the Service, but that news takes a moment to spread, as the card "Graceful shutdown" in Kubernetes showed. In this incident, `RequestGate`'s readiness flag stands for that removal. Its 503 covers the requests that still arrive meanwhile, and the old code had neither. It also never waited for requests already running. A rider whose unlock was half done got no answer.

```callout jargon
A request is **in flight** while the server is still working on it: it has arrived, but the answer has not gone back yet. **Draining** a pod means letting its in-flight requests finish while it takes no new ones.
```

## What you need

You learned each idea in an earlier ticket. Here they meet in one incident.

- Why `latest` is a trap, and what a digest is: [Docker basics](/t/module-07-tooling/topic-05-docker-basics) and [Production Dockerfiles](/t/module-09a-platform/topic-01-dockerfiles).
- How CI names each image it pushes: [GitHub Actions](/t/module-09a-platform/topic-03-github-actions).
- `maxSurge`, `maxUnavailable` and how they round: [Kubernetes manifests](/t/module-09a-platform/topic-04-kubernetes-manifests).
- Rolling updates, readiness and graceful shutdown: [Kubernetes](/t/module-09-architecture/topic-10-kubernetes), the cards "Rolling updates" and "Graceful shutdown".
- `CountDownLatch` with a timeout: [Coordinating threads](/t/module-05-advanced-java/topic-15-coordinating-threads).
- `AtomicInteger` and `volatile`: [Concurrent collections](/t/module-05-advanced-java/topic-06-concurrent-collections) and [The Java Memory Model](/t/module-05-advanced-java/topic-14-memory-model).

### The classes

| Class | What it is |
|---|---|
| `ImagePolicy` | bronze: `releaseTag(version, commitSha)` and `requireImmutable(image)` |
| `MutableImageException(image, reason)` | thrown for an image that could change under you |
| `RolloutPlanner` | silver: `plan(replicas, maxSurge, maxUnavailable, minAvailable)`; `resolve(value, replicas, roundUp)` is ready |
| `RolloutPlan(replicas, surge, unavailable, waves)` | the plan; `lowestServing()` and `highestTotal()` |
| `Wave(number, started, stopped, serving, oldPods, newPods)` | one step of the rollout |
| `UnsafeRolloutException` | thrown when a plan would drop below the floor |
| `RequestGate(gracePeriod)` | gold: `handle(work)`, `shutdown()`, `ready()`, `stopped()`, `inFlight()` |
| `Response(status, body)` | `ok(body)` 200, `shuttingDown()` 503, `cutOff()` 502 |
| `ShutdownReport(inFlightAtStart, cutOff)` | what one shutdown did; `clean()` |

### Tags you can trust

A tag is only safe if nobody can move it. Spoke's rule: CI tags each image with the release version **and** the commit it was built from, such as `2.6.0-3f9c2b7a1d4e`. The first 12 characters of the commit SHA say exactly which code is inside.

A bare version such as `2.6.0` is not enough. Someone can rebuild and push `2.6.0` again with different code. Names like `main` and `stable` move by design. A digest, `@sha256:...`, is safest of all, because it is the image's own fingerprint.

```callout jargon
A **commit SHA** is the 40-character hex name git gives a commit, such as `3f9c2b7a1d4e5f60718293a4b5c6d7e8f9012345`. **Hex** means the digits 0 to 9 and the letters a to f.
```

### Where the tag is

An image name can have a colon in two places: in the registry's port, and before the tag. Splitting at the first colon finds the wrong one.

```animate
title: Finding the tag in an image name
kind: code
code: |
  String image = "registry.spoke.local:5000/spoke/spoke-api";
  int firstColon = image.indexOf(':');
  System.out.println(image.substring(firstColon + 1));
  int lastSlash = image.lastIndexOf('/');
  System.out.println(image.indexOf(':', lastSlash + 1));
steps:
  - line: 1
    vars: { image: '"registry.spoke.local:5000/spoke/spoke-api"' }
    say: This image lives in a registry on port 5000. It has no tag at all.
  - line: 2
    vars: { firstColon: 20 }
    say: The first colon is the one before the port.
  - line: 3
    out: "5000/spoke/spoke-api"
    say: Everything after it looks like a "tag". A careless check might even accept it.
  - line: 4
    vars: { lastSlash: 31 }
    say: The tag can only be in the last part of the name, after the last slash.
  - line: 5
    out: "-1"
    say: There is no colon after the last slash. -1 means no tag, so Docker would use latest.
```

### Rounding in a rollout

`maxSurge` and `maxUnavailable` can be numbers of pods or percentages. Kubernetes rounds a percentage in the **safe** direction for each one. Extra pods round **up**: more help is fine. Missing pods round **down**: fewer gone is safer.

So for 30 pods at 25%, which is 7.5, the surge is 8 and the unavailable is 7. `resolve` already does this, as you wrote it in the Kubernetes manifests ticket.

Two zero rules come from Kubernetes. Both limits **written** as 0 is refused, because no pod could ever be replaced. If **rounding** makes both 0, Kubernetes quietly allows one unavailable pod. Spoke's floor check then decides whether that one pod is too many.

```callout jargon
A **safety floor** is the fewest pods that must keep serving during a rollout. Spoke's is 24 of 30. Kubernetes has its own form of this, the PodDisruptionBudget, for planned evictions such as node repairs.
```

### Syntax: a bounded wait

```java nocompile
boolean finished = drained.await(gracePeriod.toMillis(), TimeUnit.MILLISECONDS);
```

`await` with a timeout waits until the latch reaches 0, or until the time runs out, whichever comes first. It returns `true` if the latch reached 0, and `false` if time ran out. It never waits for ever, so a stuck request cannot hold the pod up past its grace period.

### The order of a graceful shutdown

The order is the whole fix. Readiness goes off first, so no new work arrives. Then the pod waits for the work it already has. Only then does it stop.

```animate
title: A pod drains on SIGTERM
kind: scene
nodes:
  - { id: k8s, label: Kubernetes, emoji: "☸️", row: 0, col: 0 }
  - { id: pod, label: "Pod (RequestGate)", emoji: "🟦", value: "ready, 1 in flight", row: 0, col: 2 }
  - { id: lb, label: Load balancer, emoji: "🔀", row: 1, col: 2 }
  - { id: ana, label: Rider Ana, emoji: "📱", value: "unlocking rental-17", row: 0, col: 4 }
  - { id: ben, label: Rider Ben, emoji: "📱", row: 1, col: 4 }
steps:
  - say: Ana's unlock, rental-17, is running on this pod when SIGTERM arrives.
    send: { from: k8s, to: pod, label: SIGTERM }
  - say: Step one, readiness goes off. The probe now fails, so the load balancer stops sending new work here.
    set: { pod: "not ready, 1 in flight" }
  - say: Ben's request was already on its way. The pod answers 503 at once, and the load balancer retries another pod.
    send: [{ from: lb, to: pod, label: "Ben: POST /rentals" }, { from: pod, to: lb, label: "503", back: true }]
  - say: Step two, wait. Ana's unlock finishes, and she gets her 200.
    send: { from: pod, to: ana, label: "200 unlocked", back: true }
    set: { pod: "not ready, 0 in flight", ana: "riding" }
  - say: Step three, nothing is in flight, so the pod stops at once. It never waited the whole grace period.
    set: { pod: stopped }
    focus: [pod]
```

If a request is still running when the grace period ends, the pod stops anyway. That request is cut off, and the report says so.

## The tiers

1. **Bronze** resolves the incident. `releaseTag` makes `<version>-<first 12 characters of the commit SHA>`. `requireImmutable` accepts only a release tag or a `sha256` digest. It refuses `latest`, no tag, `main`, `stable`, a bare version and a bad digest with `MutableImageException`. A registry port is not a tag.
2. **Silver**: `plan` refuses limits written as 0 and 0, and allows one unavailable pod when rounding makes both 0, as Kubernetes does. It refuses a plan below the floor with `UnsafeRolloutException`, and builds waves that never serve fewer than `replicas - unavailable`.
3. **Gold**: `shutdown` turns readiness off first, then lets in-flight requests finish, at most for the grace period, then stops. A new request during shutdown gets a 503 and never runs. A request that was already running gets its 200.

The exact rules, with every boundary and the wave formulas, are in the README.

## Recap

- Deploy an image that can never change: a tag made from the version and the commit SHA, or a `sha256` digest. `latest`, `main` and bare versions can move, and nodes cache them.
- Kubernetes rounds `maxSurge` up and `maxUnavailable` down. Both written as 0 is refused; both rounded to 0 allows one unavailable pod.
- A rollout plan must keep enough pods serving in every wave. Kubernetes allows `maxUnavailable: 100%`, so your own tooling has to refuse it.
- A graceful shutdown has a fixed order: readiness off, drain in-flight requests within the grace period, then stop.
- Wait with a timeout, never for ever. A stuck request must not keep a pod alive past its grace period.

## Common mistakes

**1. Finding the tag at the first colon.**

```java nocompile
String tag = image.substring(image.indexOf(':') + 1);   // the port's colon comes first
```

Symptom: `registry.spoke.local:5000/spoke/spoke-api` gets the "tag" `5000/spoke/spoke-api`. The bronze port check fails. Fix: look for the colon after the last `/`.

**2. Accepting a version tag on its own.** `2.6.0` looks pinned, but anyone can push `2.6.0` again. The bronze check for moveable tags fails. Fix: the tag must be the version, a dash, and 12 lowercase hex characters.

**3. Checking the floor before the zero rule.**

```java nocompile
if (replicas - unavailable < minAvailable) { throw new UnsafeRolloutException("..."); }
if (surge == 0 && unavailable == 0) { unavailable = 1; }   // too late: the floor never saw this pod
```

Symptom: 3 pods with `maxSurge "0"`, `maxUnavailable "10%"` and a floor of 3 pass the check, then plan to stop a pod. The silver check expects `UnsafeRolloutException`. Fix: settle `unavailable` first, then check the floor.

**4. Refusing a plan that only rounds to 0 and 0.** `maxSurge "0"` with `maxUnavailable "10%"` is a valid Deployment. For 3 pods, 10% rounds down to 0, and Kubernetes then allows one unavailable pod. Refuse only limits written as 0 and 0.

**5. Waiting with no limit, or for the whole grace period.**

```java nocompile
drained.await();                    // a stuck request keeps the pod alive for ever
Thread.sleep(gracePeriod.toMillis()); // every shutdown takes the full 30 seconds
```

Symptom: the gold grace-period check times out, or the "waits only as long as needed" check fails. Fix: `await(timeout, unit)`, and count the latch down when the last request finishes.

**6. Turning readiness off after the wait.** New requests keep arriving while the pod drains, so it never drains. The gold check says "ready() still says true". Fix: readiness off is the very first line of `shutdown()`.
