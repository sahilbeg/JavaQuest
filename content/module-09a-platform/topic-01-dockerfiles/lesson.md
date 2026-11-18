## Hook

A delivery van that carries the whole workshop to every station is slow and heavy. It holds the lathe, the welding kit and every spare part. And anyone who breaks in finds tools for anything.

The spoke-api image is that van. It weighs 1.1 GB, takes 9 minutes to build after a one-line change, and runs as root with a full JDK and curl inside. A good production image is a light courier bike: only the jar, a Java runtime, and nothing an attacker could use.

## Concept

In [Docker basics](/t/module-07-tooling/topic-05-docker-basics) you learned `FROM`, `COPY`, `RUN`, a first multi-stage build, ports, volumes and Compose. This ticket goes under the hood: how layers and the cache really work, and what makes an image small, fast to build and safe.

### Images are stacks of layers

An image is a list of **layers** plus a little configuration: the start command, the user, the environment. Each layer is a compressed archive of the files one step added, changed or deleted.

```callout jargon
A **digest** is a fingerprint of some content, such as `sha256:4db2...`. Change one byte and the digest changes. Docker names every layer, and every image, by its digest.
```

When a container starts, Docker stacks the layers on top of each other. Upper layers hide files in lower ones. The container gets one thin writable layer on top, which disappears with the container.

```callout jargon
A **union filesystem** shows several stacked folders as one. Docker uses one, called **overlay**, to show an image's layers as one normal-looking disk.
```

Because layers are named by digest, they are shared. Ten images built on the same Java runtime store that runtime once. A server that pulls a new version of spoke-api downloads only the layers it does not already have.

### How the build cache decides

Before each step, Docker asks one question. "Have I built exactly this before, on this exact parent layer?" If yes, it reuses the saved layer and prints `CACHED`. The question is answered differently for different steps:

| Step | What must match for a cache hit |
|---|---|
| `FROM` | the same image |
| `RUN` | the same parent layer and the **same command text** |
| `COPY` and `ADD` | the same parent layer, the same text, and the same **contents** (checksums) of every file copied |
| `ENV`, `WORKDIR`, `USER`, ... | the same parent layer and the same text |

Notice what `RUN` does not check. Docker never looks at what the command downloads. `RUN apt-get update` stays cached for months, and its package list grows stale. That is why `apt-get update` and `apt-get install` belong in one `RUN`.

Once one step misses the cache, every later step misses too. Their parent layer is new, so nothing built on the old one can be reused.

### Cache busting: order your steps

Changing an input so that a step, and everything after it, rebuilds is called **busting the cache**. Sometimes you want it. Usually it is an accident, caused by step order.

```animate
title: A one-line change in two Dockerfiles
kind: scene
nodes:
  - { id: change, label: FareService.java changed, emoji: "✏️", row: 0, col: 0 }
  - { id: pom, label: COPY pom.xml, emoji: "📄", value: "?", row: 0, col: 1 }
  - { id: deps, label: RUN go-offline (7 min), emoji: "📦", value: "?", row: 0, col: 2 }
  - { id: src, label: COPY src, emoji: "📁", value: "?", row: 1, col: 1 }
  - { id: pkg, label: RUN package (1 min), emoji: "🔨", value: "?", row: 1, col: 2 }
steps:
  - say: You change one Java file. Docker checks the steps from the top, one at a time.
    focus: [change]
  - say: COPY pom.xml reads only pom.xml, and its checksum is the same. Cache hit.
    set: { pom: CACHED }
  - say: The download step has the same text and the same parent layer. Cache hit, seven minutes saved.
    set: { deps: CACHED }
  - say: COPY src reads the changed file. Its checksum differs, so this step runs again.
    send: { from: change, to: src, label: new checksum }
    set: { src: REBUILD }
  - say: Package sits on a new parent layer, so it rebuilds too. One minute in all.
    set: { pkg: REBUILD }
  - say: Now picture COPY . . as the first step. It reads every file, so the same change busts it, and the 7-minute download runs again.
    set: { pom: REBUILD, deps: REBUILD, src: REBUILD, pkg: REBUILD }
```

The rule: **put what changes least at the top and what changes most at the bottom**. Base image, then system packages, then the build file and dependencies, then your code. Here is a small test build, run again after a change in `src`. Its `RUN` steps only print, standing in for Maven. Shortened:

```text
#7 [3/6] COPY pom.xml .
#7 CACHED
#8 [4/6] RUN echo "downloading dependencies" && sleep 3
#8 CACHED
#9 [5/6] COPY src ./src
#9 DONE 0.0s
#10 [6/6] RUN echo "compiling" && ls src
#10 DONE 0.2s
```

Watch for quiet cache busters too. An `ARG` whose value changes on every build busts every step that uses it. So does `COPY . .` of a folder full of build output.

### Syntax: .dockerignore

```text
# .dockerignore for spoke-api
.git
target/
*.md
.env
**/*.log
```

The **build context** is every file Docker sends to the builder before the build starts. `.dockerignore` lists what stays out. It does three jobs:

- **Speed.** Without it, `.git` and `target/` travel with every build, often hundreds of megabytes.
- **Cache.** A changed file that is ignored never reaches `COPY . .`, so it cannot bust the cache.
- **Safety.** A `.env` file with passwords, or your Git history, never ends up in a layer by accident.

Patterns start at the top of the context. `*.md` matches only Markdown files at the top; `**/*.log` matches them in every folder. A line starting with `!` makes an exception, such as `!README.md`.

### COPY or ADD

`COPY` copies files from the build context into the image. That is all it does.

`ADD` does the same, plus two surprises. Given a URL, it downloads the file. Given a local file, it checks the contents. Docker unpacks any local file whose contents are a tar archive, whatever its name. So a file that happens to be an archive is silently unpacked.

So the rule is simple: **use `COPY`**. Use `ADD` only when you want one of its tricks on purpose. For downloads, many teams prefer `RUN curl` in a build stage, where they can check the file's checksum.

```dockerfile
COPY --chown=10001:10001 config/ /app/config/
```

`--chown` sets the owner of the copied files in the same step. A separate `RUN chown -R` would write a second copy of every file into a new layer.

### Choosing a base image

The base image is usually the biggest part of your image, and it brings most of the security findings. The choice for a Java service:

| Base image | What is inside | Trade-off |
|---|---|---|
| `eclipse-temurin:21-jdk` | Ubuntu, a full JDK: compiler, `jshell`, `jcmd`, debug tools | Largest. Right for a **build** stage, never for running |
| `eclipse-temurin:21-jre` | Ubuntu and only the Java runtime | Roughly half the JDK's size. A safe default |
| `eclipse-temurin:21-jre-alpine` | Alpine Linux and the runtime | Smaller still, but built on musl (next card) |
| `gcr.io/distroless/java21-debian12` | Only the runtime and the files Java needs. No shell, no package manager | Small, very few findings. Harder to debug |

```callout warning
`eclipse-temurin:21`, with no `-jre` or `-jdk`, is the **JDK**. Always say which one you mean.
```

Fewer packages means fewer **CVEs** to patch, a smaller download on every server, and less for an attacker to use. curl, a shell and a compiler are exactly what someone who breaks in wants to find.

```callout jargon
A **CVE** (Common Vulnerabilities and Exposures) is a publicly listed security bug in a piece of software, with an id such as `CVE-2024-1234` and a severity from LOW to CRITICAL.
```

### Alpine, musl and distroless

**Alpine** is a tiny Linux distribution. It replaces **glibc**, the usual C library that programs use to talk to the operating system, with a smaller one called **musl**. Pure Java code does not care. But some libraries ship native code built for glibc: compression, crypto, fast networking. On musl, they can crash at start or quietly fall back to slower code.

Alpine images also have a different shell and package manager (`apk`, not `apt-get`). Pick Alpine only when you know your dependencies run on musl, and test it.

**Distroless** images have no shell at all. `docker exec -it ... sh` fails, because there is no `sh`. That is the point: an attacker gets no shell either. For debugging, distroless offers `:debug` tags with a small shell. Kubernetes can also attach a temporary debug container, with `kubectl debug`.

### Syntax: a multi-stage Maven build

```dockerfile
FROM maven:3.9.11-eclipse-temurin-21 AS build
# ... build the jar here ...

FROM eclipse-temurin:21.0.8_9-jre
COPY --from=build /src/target/spoke-api.jar /app/spoke-api.jar
```

You met this shape in Docker basics. Three details matter in production:

- **Only the last stage ships.** Maven, the JDK, the sources and the downloaded dependencies stay in the build stage. They never reach the servers.
- **BuildKit skips stages the final stage does not need**, and runs independent stages in parallel. A test stage that nothing depends on is skipped, so CI builds it on purpose with `--target test`.
- **`--target` builds one stage only**: `docker build --target build .` stops after the build stage. CI uses it to run tests inside the same image recipe.

```callout jargon
**BuildKit** is Docker's builder. It decides what to rebuild, runs stages in parallel, and adds features such as cache mounts and secret mounts. It has been the default since Docker 23.
```

### Dependencies in their own layer

spoke-api has about 150 dependency jars, around 60 MB. Downloading them is the slow part of every build. Give the download its own layer, and only `pom.xml` can bust it:

```dockerfile
WORKDIR /src
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src ./src
RUN ./mvnw -q -B package -DskipTests
```

- The Maven wrapper needs its `.mvn/` folder and the `mvnw` script, so they come first. `-B` is batch mode: no colours or progress bars in the build log.
- In a project with several modules, copy **every** module's `pom.xml` before the download, or Maven cannot see the whole build.
- `go-offline` misses a few plugin jars that are only fetched while a plugin runs. `package` may still download a little. That is fine.

The other approach is a **cache mount**, `RUN --mount=type=cache,target=/root/.m2 ./mvnw package`. BuildKit keeps Maven's download folder between builds, outside any layer. It survives `pom.xml` changes too. But on a brand-new CI machine the cache starts empty, unless the pipeline saves it.

### Spring Boot layered jars

A Spring Boot jar is a **fat jar**: your 1 MB of code plus 60 MB of libraries, in one file. Copy it into the image and one changed class makes a new 61 MB layer. Every server must download all of it again.

Spring Boot can split the jar into four folders, from least to most often changed:

| Folder | Holds | Changes |
|---|---|---|
| `dependencies` | released library jars | when `pom.xml` changes |
| `spring-boot-loader` | the code that starts a Boot app | almost never |
| `snapshot-dependencies` | `-SNAPSHOT` library jars | often |
| `application` | your classes and resources | every commit |

```dockerfile
RUN java -Djarmode=tools -jar target/spoke-api.jar extract --layers --destination /extracted
```

Then copy each folder with its own `COPY --from=build`, in that order. A normal commit now changes only the last layer, about 1 MB. This `tools` jar mode exists since Spring Boot 3.3; older guides use `layertools`.

### Run as a non-root user

Most base images start your program as `root`, user id 0. Containers share the host's kernel. Say an attacker finds a hole in your app, and then a hole in the container's walls. Root inside can then become root on the server.

```dockerfile
RUN groupadd --system --gid 10001 spoke \
 && useradd --system --uid 10001 --gid 10001 spoke
USER 10001
```

- Use a **numeric** user id in `USER`. Kubernetes can only check that a container is not root when the id is a number. With `USER spoke`, a Pod that demands a non-root user refuses to start.
- Files that `COPY` adds belong to root. User 10001 can read them but not change them. That is good: the app cannot rewrite its own code. Give it a writable folder for temporary files, if it needs one, with `--chown`.
- Pick an id above 10000, so it is unlikely to match a real user on the host.

### PID 1 and signals

```callout jargon
A **signal** is a short message the operating system delivers to a process. **SIGTERM** means "please shut down", and a program can catch it and clean up. **SIGKILL** means "die now", and it cannot be caught.
```

`docker stop` sends SIGTERM to the container's first process, **PID 1** (process id 1). It waits, 10 seconds by default. If the container is still running, it sends SIGKILL. Kubernetes does the same, with a 30-second wait.

Java catches SIGTERM: Spring Boot stops taking new requests, finishes the ones in flight and closes its database pool. But only if Java is the process that receives the signal. Here are two small test images. Each starts a server with a script; in one, the script ends with `exec`. A Redis server stands in for Java, because it also shuts down cleanly on SIGTERM.

```bash
docker exec demo-no-exec ps -o pid,args
docker exec demo-exec ps -o pid,args
```

```text
PID   COMMAND
    1 {start.sh} /bin/sh /start.sh
    7 redis-server *:6379

PID   COMMAND
    1 redis-server *:6379
```

(The line for `ps` itself is trimmed.) Without `exec`, PID 1 is the shell, and the server is PID 7. The shell does not pass SIGTERM on. `docker stop -t 10 demo-no-exec` took 10.2 seconds and ended with exit code **137**, which is 128 + 9: killed by SIGKILL. The server never heard a thing. With `exec`, the stop took 0.2 seconds, and the server logged its clean shutdown. Java does the same: on SIGTERM it runs its shutdown hooks and exits with 143, which is 128 + 15.

```animate
title: docker stop when the shell is PID 1
kind: scene
nodes:
  - { id: docker, label: docker stop, emoji: "🐳" }
  - { id: shell, label: "/bin/sh (PID 1)", emoji: "🐚", value: running }
  - { id: java, label: "java (PID 7)", emoji: "☕", value: serving rides }
steps:
  - say: docker stop sends SIGTERM, but only to PID 1.
    send: { from: docker, to: shell, label: SIGTERM }
  - say: PID 1 is the shell. It does not forward the signal, so Java never hears it.
    set: { shell: ignores it }
  - say: Docker waits 10 seconds. Java keeps serving, with no idea it is about to die.
    focus: [java]
  - say: Time is up. Docker sends SIGKILL, and everything stops mid-request. Exit code 137.
    send: { from: docker, to: java, label: SIGKILL }
    set: { java: killed, shell: killed }
  - say: With exec form, Java itself is PID 1. SIGTERM reaches it, and Spring shuts down cleanly.
    set: { java: "PID 1: clean exit" }
```

### Syntax: exec form, and scripts that exec

```dockerfile
ENTRYPOINT ["java", "-jar", "spoke-api.jar"]
CMD ["--spring.profiles.active=prod"]
```

- **Exec form** is a JSON list. Docker starts `java` directly, so Java is PID 1. Use it.
- **Shell form**, `ENTRYPOINT java -jar spoke-api.jar`, runs `/bin/sh -c "java -jar spoke-api.jar"`. Some shells hand over to Java, others stay as PID 1. Do not rely on luck. Shell form also ignores any `CMD` arguments.
- With both in exec form, `CMD` supplies default arguments to `ENTRYPOINT`. `docker run spoke-api --server.port=9090` replaces only the `CMD` part.

If you need a start script, end it with `exec`, so Java replaces the shell:

```bash
#!/bin/sh
echo "starting spoke-api in $SPOKE_ENV"
exec java $JAVA_OPTS -jar /app/spoke-api.jar "$@"
```

```callout tip
PID 1 has one more job: cleaning up finished child processes. Java does not do that. If your app starts other programs, run `docker run --init`, or put the tiny `tini` program first in `ENTRYPOINT`. It forwards signals and cleans up.
```

### The JVM inside the container

[Running on Kubernetes](/t/module-09-architecture/topic-10-kubernetes) showed the rule: the JVM reads the container's memory limit and, by default, gives the heap only 25% of it. Set the share in the image, as an environment variable that operators can still change:

```dockerfile
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
```

Every JVM reads `JAVA_TOOL_OPTIONS` and says so when it starts. With that value set, `java -XX:+PrintFlagsFinal -version` printed, among its many lines:

```text
Picked up JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError
     bool ExitOnOutOfMemoryError                   = true                                      {product} {environment}
   double MaxRAMPercentage                         = 75.000000                                 {product} {environment}
```

- `ExitOnOutOfMemoryError` makes Java exit on the first `OutOfMemoryError`. A half-broken server limping on is worse than a clean restart.
- Never bake a fixed `-Xmx` into the image. When an operator lowers the container's limit, a fixed heap no longer fits.
- The JVM also sizes its thread pools to the CPU limit. A container limited to 1 CPU sees 1 processor.

### HEALTHCHECK or Kubernetes probes

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s CMD curl -fs http://localhost:8081/actuator/health/liveness || exit 1
```

Docker basics added a `HEALTHCHECK` like this one. It works for plain `docker run` and for Compose. But look at the cost: it needs `curl` inside the image, and that is one of the things the scanner flagged.

Kubernetes **ignores** `HEALTHCHECK`. It uses its own probes, which call the app from outside the container, so the image needs no curl at all. So for spoke-api:

- **Production image:** no `HEALTHCHECK`, no curl. Kubernetes probes check `/actuator/health/liveness` and `/readiness` on port 8081.
- **Local runs:** define the health check in `compose.yaml`, the next ticket's subject, using a tool that is already in the image there.

If you must keep a Docker health check in a distroless image, write it as a tiny Java class and run it with `java`.

### Tags, digests and pinning

A **tag** is a name that points at an image, and the owner can move it. `eclipse-temurin:21-jre` points at a new image after every Java update and every Ubuntu patch. So Monday's build and Tuesday's build of the same commit can contain different Java versions.

A **digest** never moves. Pinning both gives you a readable name and an exact image:

```dockerfile
FROM eclipse-temurin:21.0.8_9-jre@sha256:<the 64 hex digits of the exact image>
```

When both are given, Docker uses the digest; the tag is there for humans. Pinned images stop updating on their own. So a bot such as **Dependabot** or **Renovate** opens a pull request when a new base image appears. You review it, CI tests it, and you merge it. Updates become visible, tested changes instead of surprises.

Tag your own images the same way. Spoke's release tag is the version plus the first 12 hex characters of the Git commit, such as `ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e`. A bare `2.6.0` is not a release tag. Never push two different builds under one tag. Many registries can be set to refuse it.

### Linting and scanning: hadolint and Trivy

Two kinds of tools guard the image, and both run in CI:

- A **linter** reads the Dockerfile and checks rules, before anything is built. **hadolint** is the common one. Its rule DL3007, for example, warns about `latest`, and DL3020 asks for `COPY` instead of `ADD`. You build a small linter like it in this ticket's fix.
- A **scanner** looks inside the built image. It lists every package and library, and compares their versions with public CVE lists. **Trivy** is a popular free one; Docker Scout and Grype do the same job.

```bash
trivy image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e
```

Trivy prints a table for each part of the image. Each row shows a package, its CVE id, the severity, the installed version and the fixed version. `--exit-code 1` fails the CI step when anything is found. `--ignore-unfixed` skips CVEs with no fix yet, so the build is never blocked by something you cannot act on.

New CVEs are published every day. An image that was clean on Monday can fail on Friday without any change. So scan images on a schedule too, not only when you build them.

### Secrets never go in a layer

Every `ARG` and `ENV` value is saved in the image's history, for anyone who can pull it. Here a test image used an `ARG` to pass a repository token to one `RUN`:

```bash
docker build --build-arg REPO_TOKEN=tok-9f2c41 -t jq-secret-demo .
docker history --no-trunc --format '{{.CreatedBy}}' jq-secret-demo
```

```text
RUN |1 REPO_TOKEN=tok-9f2c41 /bin/sh -c echo "machine repo.spoke.example password $REPO_TOKEN" > /tmp/netrc && echo downloaded && rm /tmp/netrc # buildkit
ARG REPO_TOKEN=tok-9f2c41
```

The token is right there, twice. The `rm` at the end did not help either. A file copied in one step and deleted in a later step is still inside the earlier layer. Anyone can unpack that layer and read it. Deleting never takes anything out of an image.

### Syntax: BuildKit secret mounts

```dockerfile
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
    ./mvnw -q -B dependency:go-offline
```

```bash
docker build --secret id=maven_settings,src=$HOME/.m2/settings.xml -t ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e .
```

A **secret mount** lends a file to one `RUN` step only. It is never written into a layer, and it does not change the cache key. A test image tried it with two steps:

```dockerfile
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml grep -c token /root/.m2/settings.xml
RUN ls /root/.m2 2>&1 || true
```

The first step could read the file: `grep` printed `1`. The second step, one layer later, printed `ls: /root/.m2: No such file or directory`. And `docker history` shows only the commands, with no file and no value:

```text
RUN /bin/sh -c ls /root/.m2 2>&1 || true # buildkit
RUN /bin/sh -c grep -c token /root/.m2/settings.xml # buildkit
```

Secrets the app needs **at run time**, such as the database password, never go in the image at all. The platform hands them in when the container starts: environment variables in Compose, Secrets in Kubernetes. A later ticket covers secret managers.

### Image size arithmetic

An image's size is the sum of its layers. Here is roughly where the 1.1 GB went:

| Layer | About |
|---|---|
| Ubuntu and the full JDK | 450 MB |
| `apt-get install curl git maven`, package lists included | 250 MB |
| Maven's downloaded jars in `/root/.m2` | 250 MB |
| `COPY . .`: sources, `target/` and `.git` | 150 MB |
| **Total** | **1,100 MB** |

The production image keeps only a JRE base, the dependency jars and the app. That is around 270 + 60 + 1 MB, about a third. And a normal deploy now pulls just the 1 MB application layer.

Deleting files in a later step never shrinks an image. Two test images both made a 50 MB file and removed it. One did both in a single `RUN`; the other used two `RUN` steps:

```bash
docker image ls --format '{{.Repository}} {{.Size}}'
```

```text
jq-size-one-step 39.5MB
jq-size-two-steps 89.5MB
```

The base was 39.5 MB. In two steps, the file lives on in the first step's layer, hidden but downloaded by every server. Clean up in the **same** `RUN` that made the mess, as in `apt-get install ... && rm -rf /var/lib/apt/lists/*`.

### Reproducible builds

A build is **reproducible** when the same commit gives the same image, today and next year. Then you can rebuild an old version to debug it, and trust that the image you tested is the one you ship.

Things that break it, and their fixes:

- **Moving tags.** Pin the base image by digest.
- **Floating versions.** No version ranges and no `-SNAPSHOT` dependencies in a release. Pin Maven plugin versions in `pom.xml`.
- **Unpinned packages.** `apt-get install curl` gets whatever is newest today. Install as few packages as you can, in the build stage.
- **Timestamps.** Every build stamps the time into the jar and the layers. Maven's `project.build.outputTimestamp` property fixes the jar's times. BuildKit reads a `SOURCE_DATE_EPOCH` build argument for the image's times.

Byte-for-byte identical images take extra work, and many teams stop short of it. But "same inputs, same behaviour" is the bar every production image should meet.

### In an interview

Dockerfile questions show whether you have shipped services, not just run them. Classic ones:

- **"How do you make a Docker build faster?"** Order steps from least to most changing. Then: a dependency layer, `.dockerignore`, cache mounts, layered jars.
- **"How do you make an image smaller?"** Multi-stage build, a JRE or distroless base, cleanup in the same `RUN`. Explain why deleting later does not help.
- **"How do you make an image secure?"** A numeric non-root `USER`, a minimal pinned base, Trivy scans, and secret mounts instead of `ARG` or `ENV`.
- **"Why does my container take 10 seconds to stop?"** Shell form, or a script without `exec`. The app is not PID 1, so it never gets SIGTERM.
- **"`COPY` or `ADD`? `CMD` or `ENTRYPOINT`?"** `COPY` unless you need `ADD`'s tricks. `ENTRYPOINT` is the program, `CMD` its default arguments.

In a system design round, one sentence covers it. "Each service ships as a small, non-root image, built by CI, scanned, and deployed by digest."

## Worked example

Here is the Dockerfile behind Kakashi's ticket. Every line works, and almost every line costs something.

```dockerfile
# spoke-api image, version 1
FROM eclipse-temurin:21-jdk
RUN apt-get update && \
    apt-get install -y curl git maven
ARG GITHUB_TOKEN
ENV SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=changeme
WORKDIR /app
COPY . .
RUN mvn -q dependency:go-offline && mvn -q package -DskipTests
ADD config/ /app/config/
EXPOSE 8080
CMD java -jar target/spoke-api.jar
```

What is wrong, line by line:

1. **Line 2.** A full JDK in production, and no `USER` anywhere, so the app runs as root.
2. **Lines 3 and 4.** curl, git and Maven stay in the image, with the package lists.
3. **Lines 5 and 6.** `GITHUB_TOKEN` and `DB_PASSWORD` are saved in the image history.
4. **Line 8.** `COPY . .` before the download, so any change downloads every dependency again. That is the 9 minutes.
5. **Line 10.** `ADD` where `COPY` is meant.
6. **Line 12.** Shell form: `docker stop` waits 10 seconds, then kills the app mid-request.

And here is the production version. It needs no secrets to build, because spoke-api's dependencies come from the public Maven Central.

```dockerfile
# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /src
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY src ./src
RUN ./mvnw -q -B package -DskipTests \
 && java -Djarmode=tools -jar target/spoke-api.jar extract --layers --destination /extracted

FROM eclipse-temurin:21.0.8_9-jre
WORKDIR /app
RUN groupadd --system --gid 10001 spoke \
 && useradd --system --uid 10001 --gid 10001 spoke
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
USER 10001
EXPOSE 8080 8081
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "spoke-api.jar"]
```

Both `FROM` lines are version-pinned here, to keep them readable. The real file also pins each one by digest, `@sha256:...`, as the pinning card shows, and Renovate keeps the digests fresh.

Step by step:

1. **`# syntax=docker/dockerfile:1`** asks BuildKit for the newest stable Dockerfile features. It must be the very first line.
2. **The build stage** starts from a version-pinned Maven image, with a full JDK. That is fine here: this stage never ships.
3. **Wrapper and `pom.xml` first, then `go-offline`.** The 7-minute download now has its own layer, busted only when `pom.xml` changes.
4. **`COPY src`, then package and extract.** A code change starts here. The jar is split into its four layers in `/extracted`.
5. **The runtime stage** starts fresh from a version-pinned JRE. It has no Maven, no compiler and no sources, and it installs nothing extra.
6. **`groupadd` and `useradd`** make user 10001, in one `RUN`, so it is one small layer.
7. **Four `COPY --from=build` lines**, from least to most often changed. A normal commit changes only the last one.
8. **`USER 10001`** comes after the copies, so the app's files stay owned by root and read-only to the app.
9. **`EXPOSE 8080 8081`** documents the app port and the Actuator port that the probes use.
10. **`JAVA_TOOL_OPTIONS`** sizes the heap to the container. `ENTRYPOINT` in exec form makes Java PID 1, so SIGTERM arrives.

Next to it sits the `.dockerignore`:

```text
.git
target/
.idea/
*.md
.env
**/*.log
```

The results, roughly: about 330 MB instead of 1.1 GB. About a minute for a code change instead of 9, and a 1 MB layer to push instead of a 61 MB jar. The scanner no longer finds Maven, git, curl from our `apt-get` line, a JDK or a root user. And the linter you write in the fix finds nothing to complain about.

## Try it

This program models the build cache for two Dockerfiles. Each step lists the project path it reads. Run it.

```java run
import java.util.List;

public class Main {
    /** One build step: its text, the path it reads from your project (null for RUN), and its seconds. */
    record Step(String text, String reads, int seconds) { }

    static boolean reads(Step step, String file) {
        String path = step.reads();
        return path != null && (path.equals(".") || file.equals(path) || file.startsWith(path + "/"));
    }

    static void build(String title, List<Step> steps, String changedFile) {
        System.out.println(title + ", after changing " + changedFile);
        boolean busted = false;
        int total = 0;
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            busted = busted || reads(step, changedFile);   // once busted, every later step rebuilds
            if (busted) {
                total += step.seconds();
            }
            System.out.printf("  %d %-8s %s%n", i + 1, busted ? "REBUILD" : "CACHED", step.text());
        }
        System.out.println("  build time: " + total + " s");
    }

    public static void main(String[] args) {
        List<Step> good = List.of(
                new Step("COPY pom.xml .", "pom.xml", 1),
                new Step("RUN mvn -q -B dependency:go-offline", null, 420),
                new Step("COPY src ./src", "src", 1),
                new Step("RUN mvn -q -B package -DskipTests", null, 60));
        List<Step> bad = List.of(
                new Step("COPY . .", ".", 2),
                new Step("RUN mvn -q -B dependency:go-offline", null, 420),
                new Step("RUN mvn -q -B package -DskipTests", null, 60));

        String changed = "src/main/java/spoke/api/FareService.java";   // try "pom.xml" or "README.md"
        build("Dependencies first", good, changed);
        build("COPY . . first", bad, changed);
    }
}
```

1. The good order rebuilds in 61 s and the bad one in 482 s. Find the line that makes the difference.
2. Set `changed` to `"pom.xml"`. Both orders now pay for the download. Why can no order avoid that?
3. Set `changed` to `"README.md"`. The good order is fully cached, but `COPY . .` still rebuilds. Which file from the lesson would fix that?

## Recap

- An image is a stack of layers named by digest. The cache reuses a step only when its parent, its text and, for `COPY`, its files' checksums match. One miss rebuilds everything below it.
- Order steps from least to most changing. Give dependencies their own layer, trim the context with `.dockerignore`, and layer Spring Boot jars so a commit changes 1 MB.
- Build in a JDK stage, run on a pinned JRE or distroless image. Alpine uses musl, which can break native libraries.
- Run as a numeric non-root `USER`, and start Java in exec form, so it is PID 1 and gets SIGTERM. Size the heap with `MaxRAMPercentage`.
- Pin base images by digest, lint with hadolint and scan with Trivy. Keep secrets out of layers: secret mounts at build time, the platform at run time.

## Common mistakes

**1. apt-get update in its own RUN.**

```dockerfile
RUN apt-get update
RUN apt-get install -y tzdata
```

What you see: months later someone adds a package to the second line. The first line is still cached, with months-old package lists. The install then asks for package versions the mirror no longer has, and fails with "404 Not Found" errors.

Fix: one `RUN` that updates, installs and cleans: `RUN apt-get update && apt-get install -y --no-install-recommends tzdata && rm -rf /var/lib/apt/lists/*`.

**2. Deleting a secret in a later step.**

```dockerfile
COPY settings.xml /root/.m2/settings.xml
RUN ./mvnw -q -B dependency:go-offline
RUN rm /root/.m2/settings.xml
```

What you see: the file is gone from the running container. But `docker save` and any layer tool still show it in the `COPY` layer, password included.

Fix: never copy secrets in. Use `RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml` and `docker build --secret`. If a secret ever reached a pushed image, rotate it.

**3. A start script without exec.**

```bash
#!/bin/sh
echo "starting spoke"
java -jar /app/spoke-api.jar
```

What you see: `docker stop` hangs for the full timeout, the exit code is 137, and Spring never logs its graceful shutdown. Riders whose requests were in flight get errors on every deploy.

Fix: `exec java -jar /app/spoke-api.jar`, or no script at all: `ENTRYPOINT ["java", "-jar", "spoke-api.jar"]`.

**4. A user name instead of a user id.**

```dockerfile
USER spoke
```

What you see: it runs fine in Docker. On Kubernetes, a Pod that requires `runAsNonRoot` never starts. The container stays in `CreateContainerConfigError`, because Kubernetes cannot tell from a name whether the user is root.

Fix: `USER 10001`, the number you gave the user in `useradd --uid 10001`.

**5. Picking Alpine to save space, then crashing on musl.**

```dockerfile
FROM eclipse-temurin:21-jre-alpine
```

What you see: the image is smaller, but a library with native code fails at start. Often it is an `UnsatisfiedLinkError` about a missing `.so` file. Or the library quietly falls back to slower pure-Java code.

Fix: test every dependency on Alpine before you switch, or choose a glibc-based small image such as the Temurin JRE or distroless.
