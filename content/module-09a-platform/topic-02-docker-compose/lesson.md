## Hook

A new mechanic at the depot gets a 14-step note: "find a wrench, borrow a pump from Harbor, ask someone for the tyre levers". By lunch they have half a toolkit, from three different shops. Another depot hands them one sealed toolbox, the same for everyone, and they fix a bike before their coffee is cold.

Spoke's setup page is the 14-step note. Each new hire installs Postgres, Redis and the API by hand, and each laptop ends up different. **Docker Compose** is the sealed toolbox: one file, one command, the same stack on every laptop.

## Concept

[Docker basics](/t/module-07-tooling/topic-05-docker-basics) showed a first `compose.yaml`, with `up -d`, `logs -f` and `down`. This ticket makes Compose your daily tool. You will see how services find each other, where data lives and how start-up order really works. Then come the traps that eat a new hire's first day.

### Why Compose

Spoke's API needs Postgres and Redis to start. Running each with a long `docker run` line works, but nobody remembers the flags, and the three must share a network.

```callout jargon
**Docker Compose** runs a group of containers that belong together, described in one YAML file. Each container in the file is a **service**. The whole group is a **project**.
```

The file goes into Git next to the code. So the whole stack is versioned with the code it serves: check out last month's commit and you get last month's database version too.

Compose is for your laptop and for CI. Production runs on Kubernetes, which the following tickets cover. The ideas carry over: services, networks, volumes, health checks.

```callout tip
Use `docker compose`, with a space. It is built into Docker. The old `docker-compose`, with a dash, was a separate Python program and is retired. Old files often start with a `version:` line. Today's Compose ignores it and warns that "the attribute `version` is obsolete".
```

### The shape of compose.yaml

```yaml
name: spoke              # the project name; without it, the folder's name is used
services:                # one entry per container
  db: { ... }
  redis: { ... }
  api: { ... }
volumes:                 # named volumes, managed by Docker
  db-data:
```

Compose names everything after the project. Here the containers become `spoke-db-1` and `spoke-redis-1`, the network is `spoke_default` and the volume is `spoke_db-data`. Two projects with different names never clash.

The file is called `compose.yaml` and sits at the top of the repository. Compose looks for that name in the current folder, so `docker compose up` needs no `-f`.

### image or build

Every service needs an image. It can come from a registry, or be built from your code:

```yaml
services:
  db:
    image: postgres:16-alpine          # pulled from a registry if it is not on the laptop
  api:
    build: .                           # built from the Dockerfile in this folder
    image: ghcr.io/spoke/spoke-api:dev # the name to give the built image
```

- `image:` alone pulls the image the first time, then reuses the copy on the laptop.
- `build:` builds from a Dockerfile, using the same cache as `docker build`. The long form takes `context`, `dockerfile`, `target` and `args`.
- With both, Compose builds the image and tags it with the `image:` name.
- `docker compose up` builds only when no image exists yet. After a code change, run `docker compose up --build`, or you keep running yesterday's image.

Pin backing services to a version, as in topic 1. `postgres:16-alpine` matches what production runs. A bare `postgres` gives every new hire whatever is newest that day.

### ports or expose

```yaml
  db:
    ports:
      - "127.0.0.1:55432:5432"   # laptop port 55432 -> container port 5432
  redis:
    expose:
      - "6379"                   # documentation only: nothing is published
```

- **`ports`** publishes a container port on your laptop, as `-p` does. Read it as "host:container".
- The `127.0.0.1:` in front publishes it on your laptop only. Without it, Docker listens on every network card, so anyone on the café Wi-Fi can reach your database. On Linux, Docker's rules can even slip past the firewall.
- **`expose`** publishes nothing. It only records which port the service uses.

Here is the surprise: containers on the same Compose network reach **every** port of each other, with no `ports` or `expose` at all. `ports` is only for you, on the laptop. So publish only what you need to reach from outside, such as the API and maybe the database for your SQL tool.

### The default network and service names

Compose creates one private network per project, `spoke_default`, and puts every service on it. On that network, **each service name is also a host name**. Docker runs a small DNS server that answers "where is `db`?" with the db container's current address.

```callout jargon
**DNS** (Domain Name System) turns a name into a network address. On the internet it turns `spoke.example` into an IP address. On a Compose network, Docker's DNS does the same for service names.
```

So the API's settings name the services, not addresses:

```yaml
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/spoke
      SPRING_DATA_REDIS_HOST: redis
```

Inside the network, use the service name and the **container** port: `db:5432`. From your laptop, use `localhost` and the **published** port: `localhost:55432`. Same database, two addresses, depending on where you stand.

```animate
title: Who can reach the database, and how
kind: scene
nodes:
  - { id: laptop, label: Your laptop, emoji: "💻", row: 0, col: 0 }
  - { id: api, label: api container, emoji: "🚲", row: 0, col: 2 }
  - { id: db, label: db container, emoji: "🐘", value: "listens on 5432", row: 1, col: 1 }
steps:
  - say: The database listens on port 5432 inside its own container.
    focus: [db]
  - say: Your SQL tool on the laptop uses the published port, localhost:55432. Docker forwards it to db's 5432.
    send: { from: laptop, to: db, label: "localhost:55432" }
  - say: The API container asks Docker's DNS for db, and connects to db:5432 on the shared network.
    send: { from: api, to: db, label: "db:5432" }
  - say: If the API used localhost:5432 instead, it would knock on its own container's door. Nobody listens there.
    focus: [api]
    set: { api: "localhost:5432: no response" }
```

### localhost inside a container

Each container has its own network stack, with its own `localhost`. Inside the API container, `localhost` means the API container itself, never your laptop and never the database.

Here is the test, in a small throwaway container on the `spoke` network. `pg_isready` asks whether a Postgres server answers at an address:

```bash
docker compose run --rm --no-deps db pg_isready -h localhost -p 5432
docker compose run --rm --no-deps db pg_isready -h db -p 5432
```

```text
localhost:5432 - no response
db:5432 - accepting connections
```

This is the most common Compose mistake. The app works when you run it from the IDE, where `localhost:55432` really is the database. Then you move it into a container, and every connection fails.

### Named volumes and bind mounts

A container's own files vanish when the container is removed. Two kinds of mounts keep files outside it:

```yaml
    volumes:
      - db-data:/var/lib/postgresql/data          # named volume
      - ./db/init:/docker-entrypoint-initdb.d:ro  # bind mount, read-only
```

| | Named volume | Bind mount |
|---|---|---|
| Written as | a name, `db-data:` | a path, starting with `./` or `/` |
| Lives | in Docker's own storage | in a folder on your laptop |
| Good for | data the container owns: the database files | files you edit: init scripts, config, sometimes source code |
| Declared | also under the top-level `volumes:` | nowhere else |

`:ro` makes a mount read-only inside the container, so a bug cannot scribble over your scripts. On Mac and Windows, bind mounts are slower than named volumes, because each file access crosses into Docker's hidden Linux VM. Keep busy database files in a named volume.

### down versus down -v

| Command | Containers | Network | Named volumes |
|---|---|---|---|
| `docker compose stop` | stopped, kept | kept | kept |
| `docker compose down` | removed | removed | **kept** |
| `docker compose down -v` | removed | removed | **deleted** |

So `down` and `up` give you back the same rides, riders and stations. That is what you want on a normal day. `down -v` wipes the database, and the next `up` starts from an empty one. Use it on purpose, when you want a clean slate; the stale volume trap below shows when.

### Environment variables and .env

Two different things share the word "environment" in Compose. Keep them apart:

- **`environment:`** (and `env_file:`) under a service sets variables **inside that container**. Spring reads `SPRING_DATASOURCE_URL` from there.
- **`${NAME}`** anywhere in `compose.yaml` is filled in by Compose **before** anything starts. This is **interpolation**. Compose reads the values from your shell, then from a file called `.env` next to `compose.yaml`.

```text
# .env (never committed; Git ignores it)
DB_PASSWORD=local-dev-only
DB_PORT=55432   # only because this laptop already runs Postgres on 5432
```

A variable in `.env` does not reach any container by itself. It only fills in `${...}` in the file. And when your shell has the same variable, the shell wins: `DB_PORT=56000 docker compose up` overrides the `.env` value for that one run.

### Syntax: interpolation defaults and required values

```yaml
      - "127.0.0.1:${DB_PORT:-5432}:5432"                 # default when DB_PORT is unset or empty
      POSTGRES_PASSWORD: ${DB_PASSWORD:?set DB_PASSWORD in .env}   # stop with this message
```

- `${NAME:-default}` uses the default when the variable is unset or empty.
- `${NAME:?message}` refuses to start without it. With no `.env`, `docker compose config` stopped with:

```text
error while interpolating services.db.environment.POSTGRES_PASSWORD: required variable DB_PASSWORD is missing a value: set DB_PASSWORD in .env
```

Plain `${NAME}` only warns, then uses an empty text, as Docker basics showed. For a password, an empty text is worse than a loud stop. Commit a `.env.example` with safe sample values, so a new hire copies it to `.env` and is done.

### Health checks

```yaml
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U spoke -d spoke"]
      interval: 5s        # ask every 5 seconds
      timeout: 3s         # an answer slower than this counts as a failure
      retries: 10         # 10 failures in a row: unhealthy
      start_period: 10s   # failures in the first 10 s do not count
```

A **health check** is a command Docker runs inside the container, over and over. Exit code 0 means healthy. `CMD` runs a program directly; `CMD-SHELL` runs the text through a shell, so `&&` and `||` work.

Use a tool that is already in the image. The Postgres image has `pg_isready`, and the Redis image has `redis-cli ping`. A health check in `compose.yaml` replaces any `HEALTHCHECK` in the image. That is how spoke-api's production image, which has no curl, can still get a check locally.

`docker compose ps` shows the result in the status column:

```text
SERVICE   STATUS                    PORTS
db        Up 11 seconds (healthy)   127.0.0.1:55432->5432/tcp
redis     Up 20 seconds (healthy)   6379/tcp
```

### depends_on: started is not ready

```yaml
  api:
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_healthy
```

Plain `depends_on: [db, redis]` only sets the **order**: Compose starts db, then starts the API right away. Postgres needs a few seconds before it accepts connections. The API starts in those seconds, fails to connect and stops.

The condition decides how long Compose waits:

| Condition | Compose starts the API when the dependency... |
|---|---|
| `service_started` (the default) | has started, ready or not |
| `service_healthy` | passes its health check |
| `service_completed_successfully` | has run and exited with code 0. For a one-off job, such as a database migration |

Even then, write the app to retry. A database can restart at any time, long after start-up. Health ordering makes the first start smooth; [resilience](/t/module-09-architecture/topic-09-resilience) keeps the app alive after that.

`docker compose up -d --wait` returns only when every service is running and healthy. Scripts and CI use it before they run tests.

### Profiles

Some services are only needed now and then: a database admin web page, a fake email server, a load generator. **Profiles** keep them out of a normal `up`.

```yaml
  adminer:
    image: adminer:5
    profiles: [tools]
    ports:
      - "127.0.0.1:8090:8080"
```

A service with `profiles` starts only when one of its profiles is switched on. A service without `profiles` always starts.

```bash
docker compose up -d                    # db, redis, api
docker compose --profile tools up -d    # the same, plus adminer
```

Setting `COMPOSE_PROFILES=tools` in your shell or `.env` does the same as the flag.

### Override files

Compose reads `compose.yaml`, then automatically merges `compose.override.yaml` on top, if it exists. Teams use the override for personal or laptop-only settings, such as a debug port:

```yaml
# compose.override.yaml
services:
  api:
    ports:
      - "5005:5005"
    environment:
      JAVA_TOOL_OPTIONS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
      SPRING_PROFILES_ACTIVE: dev
```

How merging works, checked with `docker compose config`:

- **Maps merge by key.** The API keeps its four variables and gains two.
- **Lists of ports are added together.** The API now publishes 8080 **and** 5005. An override cannot remove a port by leaving it out.

For other places, name the files yourself, and the automatic override is skipped: `docker compose -f compose.yaml -f compose.ci.yaml up`. Later files win.

```callout tip
`docker compose config` prints the final, merged file, with every variable filled in. It needs no running containers. When Compose does something odd, read this first.
```

### Syntax: the everyday commands

| Command | What it does |
|---|---|
| `docker compose up -d` | creates and starts everything in the background |
| `docker compose up -d --build api` | rebuilds the API image, then starts it |
| `docker compose up -d --wait` | waits until every service is running and healthy |
| `docker compose ps` | lists the project's containers, with health |
| `docker compose logs -f api` | follows one service's output |
| `docker compose exec db psql -U spoke -d spoke` | runs a command in the **running** db container |
| `docker compose run --rm db pg_isready -h db` | runs a command in a **new**, one-off container of that service |
| `docker compose stop` | stops the containers, keeps them |
| `docker compose down` | removes containers and network, keeps volumes |
| `docker compose config` | shows the merged, interpolated file |

`exec` and `run` look alike. `exec` steps into a container that is already running. `run` makes a fresh one, which `--rm` removes when the command ends.

### Compose for integration tests

```callout jargon
An **integration test** checks several real parts working together, such as the API with a real Postgres, instead of one class with fakes.
```

A CI job can start the backing services with Compose, run the tests against them, and tear down:

```bash
docker compose up -d --wait db redis
./mvnw -B verify
docker compose down -v
```

`--wait` makes sure Postgres is healthy before the first test. `down -v` leaves nothing behind for the next job.

[JPA and Hibernate](/t/module-08-backend/topic-09-jpa-hibernate) showed the other way: **Testcontainers**. This Java library starts a fresh container from inside a test class, and removes it afterwards.

| | Compose in CI | Testcontainers |
|---|---|---|
| Who starts the database | a script, before the tests | the test itself |
| Lifetime | the whole test run | one test class, or shared on purpose |
| Free ports | fixed ports in the file | random free ports, found automatically |
| Running one test in the IDE | start Compose first | just press Run |

Many Java teams use Testcontainers for tests and Compose for running the app locally. Spring Boot even has a `spring-boot-docker-compose` module, which runs `docker compose up` when the app starts, then points the app at the services.

### Trap: the port is already in use

The laptop used for this lesson already had its own Postgres on port 5432. Asking Compose for the same port failed:

```text
Error response from daemon: ports are not available: exposing port TCP 127.0.0.1:5432 -> 127.0.0.1:0: listen tcp4 127.0.0.1:5432: bind: address already in use
```

Two fixes. Stop the other program, or move the host port, which is the left-hand number only. That is why Spoke's file says `${DB_PORT:-5432}`. A new hire whose laptop already runs Postgres sets `DB_PORT=55432` in their `.env`. The container side stays 5432, so the API's `db:5432` never changes.

The sneakier version gives no error at all. On some laptops, two servers can share a port number when they listen on different addresses. Your SQL tool then connects to `localhost:5432`, lands in the wrong database, and shows tables you never made. When data looks strange, check which server answered.

### Trap: a stale volume with an old schema

The Postgres image runs the scripts in `/docker-entrypoint-initdb.d` **only when its data folder is empty**, the very first time. Spoke added `02-bikes.sql` next to the stations script, then ran `down` and `up`. The volume still held last week's database:

```text
db-1  | PostgreSQL Database directory appears to contain a database; Skipping initialization
```

```text
ERROR:  relation "bike" does not exist
```

After `docker compose down -v` and `up` again, the scripts ran on an empty volume, and the table was there. The same trap hits when a new hire pulls a branch with a schema change. Switching Postgres from 16 to 17 is worse: old data files and a new version do not mix.

For real schemas, use a migration tool such as **Flyway** or **Liquibase**, run by the app or by a one-off service. It changes an existing database step by step, so nobody needs to wipe anything. A later ticket covers migrations in production.

### Trap: arm64 and amd64 images

```callout jargon
A **CPU architecture** is the instruction set a processor understands. Most servers use **amd64** (also called x86-64). Apple silicon Macs and many cloud machines use **arm64**. A program built for one cannot run directly on the other.
```

Popular images, such as `postgres` and `redis`, are published for both, and Docker picks the right one. Two traps remain:

- **An image published only for amd64**, on an Apple silicon Mac. Docker Desktop can run it through emulation, but slowly, and some programs misbehave. If you must, say so with `platform: linux/amd64` on that service, so it is a visible choice.
- **An image you built on your Mac and pushed** is arm64 only. On an amd64 server it fails at once with `exec format error`. Build release images for both with `docker buildx build --platform linux/amd64,linux/arm64`.

### Trap: secrets in compose files

`compose.yaml` goes into Git, so it must never hold a real secret. Three rules:

- **Values come from `.env` or the shell**, through `${...}`. `.env` is listed in `.gitignore`. Commit `.env.example` with fake values.
- **Local passwords are not real passwords.** `local-dev-only` protects a database that only listens on `127.0.0.1`. Never reuse a staging or production password on a laptop.
- **For files, use `secrets:`.** Compose mounts each secret as a file at `/run/secrets/<name>`. Unlike environment variables, it does not show up in `docker inspect`.

```yaml
services:
  db:
    environment:
      POSTGRES_PASSWORD_FILE: /run/secrets/db_password   # the Postgres image reads the password from this file
    secrets: [db_password]
secrets:
  db_password:
    file: ./secrets/db_password.txt   # git-ignored
```

### In an interview

Compose questions often hide inside "how do you set up a dev environment" or "how do you test against a real database". Classic ones:

- **"My app can't reach the database from inside its container. Why?"** It uses `localhost`, which is the container itself. Use the service name and the container port: `db:5432`.
- **"What is the difference between `ports` and `expose`?"** `ports` publishes to the host. `expose` is documentation. Containers on one network reach each other's ports either way.
- **"Does `depends_on` wait for the database to be ready?"** Not by default. Use a health check with `condition: service_healthy`, and still retry in the app.
- **"Where does the data go when I run `down`?"** It stays in the named volume. Only `down -v` deletes it.
- **"Compose or Testcontainers for integration tests?"** Testcontainers for tests owned by the code. Compose for running the whole stack, locally or in a CI job.

Also say what Compose is not: it runs one machine's worth of containers. For many machines, self-healing and rolling updates, you need an orchestrator such as Kubernetes.

## Worked example

Tsunade's ticket: one file, one command. Here is the project layout and the full file.

```text
spoke-api/
  compose.yaml
  compose.override.yaml   (optional, per laptop)
  .env.example            (committed: DB_PASSWORD=local-dev-only, DB_PORT=5432)
  .env                    (git-ignored, copied from .env.example)
  db/init/01-stations.sql
  Dockerfile              (from the last ticket)
  pom.xml
  src/
```

```yaml
name: spoke

services:
  api:
    build: .
    image: ghcr.io/spoke/spoke-api:dev
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/spoke
      SPRING_DATASOURCE_USERNAME: spoke
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:?set DB_PASSWORD in .env}
      SPRING_DATA_REDIS_HOST: redis
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_healthy

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: spoke
      POSTGRES_USER: spoke
      POSTGRES_PASSWORD: ${DB_PASSWORD:?set DB_PASSWORD in .env}
    ports:
      - "127.0.0.1:${DB_PORT:-5432}:5432"
    volumes:
      - db-data:/var/lib/postgresql/data
      - ./db/init:/docker-entrypoint-initdb.d:ro
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U spoke -d spoke"]
      interval: 5s
      timeout: 3s
      retries: 10
      start_period: 10s

  redis:
    image: redis:7-alpine
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

  adminer:
    image: adminer:5
    profiles: [tools]
    ports:
      - "127.0.0.1:8090:8080"

volumes:
  db-data:
```

Reading it from the top:

1. **`name: spoke`** fixes the project name, so it does not depend on the folder a new hire cloned into.
2. **`api`** is built from the Dockerfile. Its settings use service names, `db` and `redis`, never `localhost`. It waits for both to be healthy.
3. **`db`** is pinned to Postgres 16, like production. The password must come from `.env`, and the host port can move with `DB_PORT`. Only the laptop can reach it.
4. **Two mounts on `db`.** The named volume keeps the data across `down`. The read-only bind mount feeds the init script on the very first start.
5. **`redis`** publishes nothing. Only the API needs it, over the network.
6. **`adminer`** is a small database web page, started only with `--profile tools`.

A new hire's whole setup is now three commands:

```bash
cp .env.example .env
docker compose up -d --wait
docker compose ps
```

Most days, engineers run only the backing services in Compose and the API from the IDE, for fast restarts and the debugger. On the test laptop, which had another Postgres on 5432, `.env` set `DB_PORT=55432`. Then:

```bash
docker compose up -d --wait db redis
docker compose ps --format 'table {{.Service}}\t{{.Status}}\t{{.Ports}}'
docker compose exec db psql -U spoke -d spoke -c 'SELECT id, docks FROM station ORDER BY id;'
```

```text
SERVICE   STATUS                    PORTS
db        Up 11 seconds (healthy)   127.0.0.1:55432->5432/tcp
redis     Up 20 seconds (healthy)   6379/tcp

    id     | docks 
-----------+-------
 depot     |    12
 harbor    |    12
 old-mill  |    12
 riverside |    12
 uptown    |    12
(5 rows)
```

The API in the IDE connects to `jdbc:postgresql://localhost:55432/spoke`, because it runs on the laptop, outside the network. Inside Compose, the same API would use `db:5432`. When the day ends, `docker compose down` keeps the data for tomorrow.

## Try it

Compose fills in `${...}` before anything starts. This program copies its rules: the shell beats `.env`, `:-` gives a default, `:?` stops with a message. Run it.

```java run
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    // ${NAME}, ${NAME:-default} or ${NAME:?error message}
    static final Pattern VARIABLE = Pattern.compile("\\$\\{(\\w+)(?::([-?])([^}]*))?}");

    static String interpolate(String line, Map<String, String> shell, Map<String, String> dotEnv) {
        Matcher m = VARIABLE.matcher(line);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String name = m.group(1);
            String value = shell.containsKey(name) ? shell.get(name) : dotEnv.get(name);   // the shell wins over .env
            if (value == null || value.isEmpty()) {
                if ("-".equals(m.group(2))) {
                    value = m.group(3);                                                   // :- gives a default
                } else if ("?".equals(m.group(2))) {
                    throw new IllegalStateException("required variable " + name + " is missing a value: " + m.group(3));
                } else {
                    value = "";                                                           // plain ${NAME}: empty text
                }
            }
            m.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        m.appendTail(out);
        return out.toString();
    }

    public static void main(String[] args) {
        Map<String, String> dotEnv = Map.of("DB_PASSWORD", "local-dev-only", "DB_PORT", "55432");
        Map<String, String> shell = Map.of();   // try Map.of("DB_PORT", "56000")

        System.out.println(interpolate("- \"127.0.0.1:${DB_PORT:-5432}:5432\"", shell, dotEnv));
        System.out.println(interpolate("POSTGRES_PASSWORD: ${DB_PASSWORD:?set DB_PASSWORD in .env}", shell, dotEnv));
        System.out.println(interpolate("SPRING_PROFILES_ACTIVE: ${SPOKE_PROFILE:-dev}", shell, dotEnv));
        System.out.println(interpolate("REDIS_PASSWORD: ${REDIS_PASSWORD}", shell, dotEnv));
    }
}
```

1. Set `shell` to `Map.of("DB_PORT", "56000")`. Which port wins, and why is that handy for one quick run?
2. Remove `DB_PASSWORD` from `dotEnv`. The program stops, the way `docker compose up` would. Which line made it stop?
3. The last line prints an empty password, silently. Change it to `${REDIS_PASSWORD:?set REDIS_PASSWORD}`. Which behaviour would you rather have on a new hire's first day?

## Recap

- Compose runs a project of services from one `compose.yaml` in Git. Each service comes from `image:` or `build:`; after code changes, `up --build`.
- Services reach each other by service name and container port, such as `db:5432`. `localhost` inside a container is that container. `ports` publishes to the laptop (bind to `127.0.0.1`); `expose` publishes nothing.
- Named volumes keep data across `down`; only `down -v` deletes them. Init scripts run only on an empty volume, so a stale volume hides schema changes.
- `${NAME}` comes from the shell, then `.env`. Use `:?` for required values, keep `.env` out of Git, and never put real secrets in the file.
- Plain `depends_on` is start order only; add a health check and `service_healthy`. Profiles hold optional tools, override files layer per-laptop settings, and `docker compose config` shows the result.

## Common mistakes

**1. localhost in the app's settings.**

```yaml
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/spoke
```

What you see: the API works from the IDE, but in Compose it cannot connect. Inside a container, `pg_isready -h localhost -p 5432` printed `localhost:5432 - no response`.

Fix: use the service name and the container port, `jdbc:postgresql://db:5432/spoke`.

**2. depends_on without a health check.**

```yaml
    depends_on: [db, redis]
```

What you see: on a fresh laptop, the API starts while Postgres is still setting up, fails its first connection, and stops. A second `up` works, so the bug looks random.

Fix: give `db` a health check and write `depends_on: { db: { condition: service_healthy } }`. Keep retries in the app too.

**3. Expecting a new init script to run on an old volume.**

```bash
docker compose down
docker compose up -d
```

What you see: Postgres logs `Skipping initialization`, and queries fail with `relation "bike" does not exist`.

Fix: `docker compose down -v` when you want a fresh database. For real schema changes, use migrations, which update the existing database.

**4. Publishing the database to the whole network.**

```yaml
    ports:
      - "5432:5432"
```

What you see: nothing, until someone on the same Wi-Fi finds a Postgres with the password `local-dev-only`.

Fix: `"127.0.0.1:5432:5432"`, or no `ports` at all if only other containers need the database.

**5. A forgotten --build.**

```bash
docker compose up -d
```

What you see: you fixed a bug in the API, but Compose keeps serving the old behaviour. It reused the image built yesterday.

Fix: `docker compose up -d --build api`, or run the API from the IDE while you change it.
