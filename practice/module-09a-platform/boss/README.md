# INC-11: The deploy that took Spoke down

Friday's deploy went wrong three times over. Production ran an old build, because the image was tagged `latest` and a node still had an old copy. The rollout then replaced every pod at once, so riders got 503s for four minutes. And as old pods were killed, unlock requests still running were cut off half-way.

Fix Spoke's deploy tooling in package `jq.m11.incident`. Change `ImagePolicy`, `RolloutPlanner` and `RequestGate`. `MutableImageException`, `UnsafeRolloutException`, `Wave`, `RolloutPlan`, `Response` and `ShutdownReport` are ready. Every check fails with a message that says what went wrong.

## Bronze: one exact image (`ImagePolicy`, TODOs 1 and 2)

**`static String releaseTag(String version, String commitSha)`** returns the tag CI gives each image: the version, a dash, and the **first 12 characters** of the commit SHA.

- `releaseTag("2.5.1", "3f9c2b7a1d4e5f60718293a4b5c6d7e8f9012345")` returns `"2.5.1-3f9c2b7a1d4e"`.
- `version` must be three whole numbers with dots, like `2.5.1` or `10.0.12`: no `v`, no `-rc1`, not two numbers.
- `commitSha` must be the full commit SHA: exactly 40 characters, each `0`–`9` or `a`–`f` (lowercase).
- Anything else, including `null`, throws `IllegalArgumentException`.

**`static String requireImmutable(String image)`** returns `image` unchanged when it names one exact build. Otherwise it throws `new MutableImageException(image, reason)`. The exception's message contains the image. An image passes when:

- **It has a digest:** `@` followed by `sha256:` and exactly 64 lowercase hex characters, as in `ghcr.io/spoke/spoke-api@sha256:4b1d…`. A digest decides the image on its own, so `name:anytag@sha256:…` passes too. Any other digest (too short, uppercase, `md5:`, empty) is refused.
- **Or its tag is a release tag:** `<version>-<12 lowercase hex characters>`, exactly the shape `releaseTag` makes, as in `ghcr.io/spoke/spoke-api:2.5.1-3f9c2b7a1d4e`.

Everything else is refused. That includes no tag (Docker would use `latest`), `latest`, `main` and `stable`. It also includes a bare version such as `2.5.1`, a bare SHA, an uppercase SHA, a SHA of the wrong length, `null` and blank text. These tags can all be moved to another build later.

**Where the tag is.** The tag is the text after a colon in the **last** part of the name, after the last `/`. A colon before the last `/` belongs to the registry's port. So `registry.spoke.local:5000/spoke/spoke-api` has **no** tag, and `registry.spoke.local:5000/spoke/spoke-api:2.5.1-3f9c2b7a1d4e` has the tag `2.5.1-3f9c2b7a1d4e`.

## Silver: never drop below the floor (`RolloutPlanner`, TODOs 3 and 4)

**`static int resolve(String value, int replicas, boolean roundUp)`** is ready, as you wrote it in [Kubernetes manifests](/t/module-09a-platform/topic-04-kubernetes-manifests). It turns `"2"` into 2 pods, and `"25%"` into 25% of `replicas`, rounded **up** for `maxSurge` and **down** for `maxUnavailable`. 25% of 30 is 7.5: maxSurge 8, maxUnavailable 7. Anything else throws `IllegalArgumentException`.

**`static RolloutPlan plan(int replicas, String maxSurge, String maxUnavailable, int minAvailable)`**. `minAvailable` is Spoke's safety floor: the fewest pods that must keep serving riders. Check in this order:

1. `replicas` below 1, or `minAvailable` below 0 or above `replicas`: `IllegalArgumentException`.
2. `maxSurge` and `maxUnavailable` both **written** as 0 (`"0"` or `"0%"`): `IllegalArgumentException`. Kubernetes refuses this Deployment too, because no pod could ever be replaced.
3. A `maxUnavailable` percentage above 100%: `IllegalArgumentException`.
4. `surge = resolve(maxSurge, replicas, true)` and `unavailable = resolve(maxUnavailable, replicas, false)`.
5. If rounding made **both** 0, set `unavailable` to 1. Kubernetes does the same. For 3 pods, `maxSurge "0"` with `maxUnavailable "10%"` gives surge 0 and unavailable 1.
6. Cap `unavailable` at `replicas`.
7. `replicas - unavailable < minAvailable`: `UnsafeRolloutException`. Exactly at the floor is allowed. Kubernetes would happily stop that many pods; Spoke's planner refuses, so a person decides.

Then build the waves. Start with `oldPods = replicas` and `newPods = 0`. Each wave works out, in this order:

```text
stopped = min(oldPods, oldPods + newPods - (replicas - unavailable)), and at least 0
started = min(replicas - newPods, replicas + surge - (oldPods - stopped) - newPods), and at least 0
serving = oldPods - stopped + newPods
```

Then `oldPods -= stopped` and `newPods += started`, and the new pods count as ready from the next wave on. Add `new Wave(number, started, stopped, serving, oldPods, newPods)`, numbered from 1. Stop when `oldPods` is 0 and `newPods` is `replicas`. Return `new RolloutPlan(replicas, surge, unavailable, waves)`.

The skeleton already has this loop, with two ready-made guards. Keep them. If a wave would stop **and** start no pod, the loop throws `IllegalStateException` ("the rollout makes no progress"): that happens when step 5 is skipped. After `2 × replicas + 2` waves it throws too, so a wrong formula fails with a message instead of looping for ever. A correct plan never reaches either guard.

The plan for 30 pods, `maxSurge 25%`, `maxUnavailable 0`, so surge 8 and unavailable 0:

| Wave | started | stopped | serving | old after | new after |
|---|---|---|---|---|---|
| 1 | 8 | 0 | 30 | 30 | 8 |
| 2 | 8 | 8 | 30 | 22 | 16 |
| 3 | 8 | 8 | 30 | 14 | 24 |
| 4 | 6 | 8 | 30 | 6 | 30 |
| 5 | 0 | 6 | 30 | 0 | 30 |

With these rules no wave serves fewer than `replicas - unavailable` pods, and none runs more than `replicas + surge`.

## Gold: shut down gracefully (`RequestGate`, TODOs 5 to 7)

`new RequestGate(gracePeriod)` sits in front of every request one pod handles. `handle(work)` runs one request's work, a `Supplier<Response>`. `shutdown()` is what SIGTERM starts.

- **`ready()`** is `true` until `shutdown()` begins, then `false` for good. It is what the readiness probe reports.
- **`handle(work)`**: once shutdown has begun, return `Response.shuttingDown()` (503) **without running `work`**, and without counting it in `inFlight()`. Before that, count the request in `inFlight()` while `work` runs, and return its response. If the server has stopped by the time `work` returns, return `Response.cutOff()` (502) instead: in real life that answer never reaches the rider.
- **`shutdown()`**, in this order:
  1. Turn readiness off, first, before any waiting.
  2. Note how many requests are in flight: that is `inFlightAtStart`.
  3. Wait until every in-flight request has finished, but **at most `gracePeriod`**. Return as soon as the last one finishes; with nothing in flight, do not wait at all. Use a bounded wait, such as `CountDownLatch.await(timeout, unit)`, never one that can wait for ever, and never `Thread.sleep`.
  4. Stop: `stopped()` becomes `true`.
  5. Return `new ShutdownReport(inFlightAtStart, cutOff)`, where `cutOff` is the number of requests still running when the wait ended: 0 when they all finished.

The checks run requests and `shutdown()` on separate threads. They hold a request inside its work with a latch, so it is "in flight" for as long as the check needs. No check sleeps.
