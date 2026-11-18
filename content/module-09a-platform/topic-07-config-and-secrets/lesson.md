## Hook

Think of a bike. The frame is the same in every city. The seat height, the lock code and the city map are set for each rider and each place.

Spoke's jar is the frame. The database address, the pool size and the passwords are the settings. Last year someone welded a setting to the frame: the production password sat in `application.yml`, in git, for a year. Last week staging found the production database and quietly used it. This ticket teaches you to keep frame and settings apart, and to make a wrong setting stop the bike before it leaves the dock.

## Concept

### Config is everything that changes between environments

**Configuration** is every value that differs between dev, staging and prod, while the code stays the same. You met it in [Spring Boot intro](/t/module-08-backend/topic-03-spring-boot-intro).

| Config (outside the code) | Code (inside the jar) |
|---|---|
| database URL, user and password | how a fare is computed |
| connection pool size | which endpoints exist |
| payment provider's API key | the unlock fee rule: 100 cents |
| cache address, log level | how a rental is stored |

```callout jargon
The **twelve-factor app** is a well-known list of twelve rules for apps that run in the cloud. Its third rule says: keep config strictly apart from code, and read it from the environment the app runs in.
```

A quick test from the twelve-factor idea: could you make the repository public today without leaking a single credential? If not, config is hiding in the code. Spoke failed that test for a year.

### One build for every environment

In [Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production) you learned "build once, deploy many". Config is what makes that possible.

The image `ghcr.io/spoke/spoke-api:2.6.0-3f9c2b7a1d4e` is built once. Staging runs it with staging settings. Prod runs the very same image with prod settings. Nothing is rebuilt between them.

The trap is a build that bakes settings in: a `staging` jar and a `prod` jar, or a Dockerfile line like `ENV SPOKE_DB_URL=...`. Then the file you tested is not the file you ship. Settings must arrive when the app **starts**, never when it is **built**.

### Property sources: where Spring Boot looks

Spring Boot does not read one file. It reads many **property sources** and stacks them, the way you stack sheets of tracing paper. A value on a higher sheet hides the same key below it.

```callout jargon
A **property source** is one place settings come from: a file, the environment variables, the command line, a folder of secret files. Spring Boot keeps them in an ordered list and asks them from the top down.
```

The sources Spoke uses, lowest first:

- defaults written in code, such as `@DefaultValue` on a record field
- `application.yml` inside the jar
- `application-staging.yml`, the profile's own file
- operating system environment variables, such as `SPOKE_DB_URL`
- command-line arguments, such as `--spoke.db.pool-size=20`

Two smaller details: a file **outside** the jar beats the same file inside it. And Java system properties (`-Dspoke.db.url=...`) sit just above environment variables.

### Which source wins

When one key comes from several sources, the highest one wins. This is the list from the [Spring Boot intro](/t/module-08-backend/topic-03-spring-boot-intro), with Java system properties added. Highest first:

1. Test settings, such as `@SpringBootTest(properties = ...)`
2. Command-line arguments
3. Java system properties (`-D`)
4. Environment variables
5. The profile's file, such as `application-staging.yml`
6. `application.yml`
7. Defaults in code

Why this order? The more specific and the later decided, the higher it sits. A file is written months ahead. An environment variable is set by the deploy. A command-line flag is typed by a person right now, for one run.

When you wonder "where did this value come from?", Spring tells you. A failed startup prints an `Origin` line, and `/actuator/configprops` shows the origin of each value (with the values masked, as you will see).

### Relaxed binding: SPOKE_DB_URL is spoke.db.url

Environment variable names cannot contain dots or dashes in most shells. So Spring Boot matches names loosely. This is called **relaxed binding**.

```callout jargon
**Relaxed binding** means Spring Boot accepts several spellings of one key. `spoke.db.pool-size`, `spoke.db.poolSize` and `SPOKE_DB_POOLSIZE` all fill the same field.
```

The documented rule for turning a key into an environment variable name:

1. Replace each dot with an underscore.
2. Remove every dash.
3. Write it in capitals.

So `spoke.db.url` becomes `SPOKE_DB_URL`, and `spoke.db.pool-size` becomes `SPOKE_DB_POOLSIZE`.

```text
DbSettings[url=jdbc:postgresql://db.staging.spoke.internal/spoke, username=spoke_api, poolSize=10]
```

That line came from a real run of Spring Boot 4 with `SPOKE_DB_URL`, `SPOKE_DB_USERNAME` and `SPOKE_DB_POOLSIZE` set, and no file at all. Spring's matching is forgiving: in another run, `SPOKE_DB_POOL_SIZE` also filled `poolSize`. Still, write the documented form. It is the one every teammate and every tool expects.

### Profiles: small differences, not separate apps

A **profile** switches on an extra file of settings, as you saw in module 8. `SPRING_PROFILES_ACTIVE=staging` makes Spring read `application-staging.yml` on top of `application.yml`.

YAML can also hold every profile in one file, as separate documents split by `---`:

```yaml
spoke:
  env: dev
  db:
    url: jdbc:postgresql://localhost:5432/spoke
---
spring:
  config:
    activate:
      on-profile: staging
spoke:
  env: staging
  db:
    url: jdbc:postgresql://db.staging.spoke.internal:5432/spoke
```

Use profiles for the few things that differ by environment, such as hosts and pool sizes. Two traps:

- **A profile per city, per laptop, per feature.** Ten profiles combine in ways nobody can predict. Keep three: `dev`, `staging`, `prod`.
- **Secrets in profile files.** `application-prod.yml` is still a file in git. A profile changes *which* file is read, not *who* can read it.

### Placeholders: ${key} and ${key:default}

A value can point at another key with a **placeholder**. Spring fills it in after it has stacked every source.

```yaml
spoke:
  db:
    host: localhost
    url: jdbc:postgresql://${spoke.db.host}:5432/spoke
  cache:
    ttl: ${SPOKE_CACHE_TTL:60s}
```

- `${spoke.db.host}` is replaced by the final value of `spoke.db.host`. Set `SPOKE_DB_HOST` in staging, and the URL follows.
- `${SPOKE_CACHE_TTL:60s}` uses `60s` when nothing sets the key. The default is everything after the first colon.
- A placeholder with no value and no default stops the start. Placeholders that point at each other in a loop are an error too.

```animate
title: Filling in placeholders
kind: code
code: |
  String host = "db.staging.spoke.internal";
  String port = "5432";
  String url = "jdbc:postgresql://${host}:${port}/spoke";
  url = url.replace("${host}", host);
  url = url.replace("${port}", port);
  System.out.println(url);
steps:
  - line: 1
    vars: { host: '"db.staging.spoke.internal"' }
    say: Staging's environment sets the host. It came from SPOKE_DB_HOST, after every source was stacked.
  - line: 2
    vars: { port: '"5432"' }
    say: The port comes from application.yml. No one overrode it.
  - line: 3
    vars: { url: '"jdbc:postgresql://${host}:${port}/spoke"' }
    say: The URL is a template with two holes. The file never names a real host.
  - line: 4
    vars: { url: '"jdbc:postgresql://db.staging.spoke.internal:${port}/spoke"' }
    say: The first hole is filled with the final host value.
  - line: 5
    vars: { url: '"jdbc:postgresql://db.staging.spoke.internal:5432/spoke"' }
    say: Then the port. Change one key, and every value that uses it follows.
  - line: 6
    out: "jdbc:postgresql://db.staging.spoke.internal:5432/spoke"
    say: The finished URL. Spring does the same, with ${...} and an optional default after a colon.
```

### Syntax: @ConfigurationProperties with @Validated

Read a group of settings into a record, and let the validation rules from [Validation](/t/module-08-backend/topic-06-validation) check them.

```java
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("spoke.db")
record DbSettings(@NotBlank String url, @NotBlank String username, @Min(1) @Max(50) int poolSize) {}
```

`@Validated` makes Spring check every rule while it builds the record, at startup. Without it, a missing username quietly becomes `null`, and a missing pool size becomes `0`.

A typed record is better than `@Value` sprinkled over twenty classes. All the database settings sit in one place, with their rules, and a unit test can build one by hand.

### Fail fast: a bad config stops the start

```callout jargon
**Fail fast** means a program stops at the first sign of a broken setting, with a clear message, instead of starting and failing later at a worse moment.
```

Here is Spring Boot 4 starting with `--spoke.db.pool-size=0` and no username, in a real run:

```text
***************************
APPLICATION FAILED TO START
***************************

Description:

Binding to target spoke.api.DbSettings failed:

    Property: spoke.db.poolSize
    Value: "0"
    Origin: "spoke.db.pool-size" from property source "commandLineArgs"
    Reason: must be greater than or equal to 1

    Property: spoke.db.username
    Value: "null"
    Reason: must not be blank


Action:

Update your application's configuration
```

Notice three gifts. It lists **every** broken property, not just the first. It says **where** the bad value came from: `commandLineArgs`. And it fails **before** the app takes traffic.

During a rolling update, a new Pod that fails to start never becomes ready. Most old Pods keep serving (all of them with `maxUnavailable: 0`), and the rollout stalls. A config bug becomes a red deploy, not a rider-facing outage. Without validation, the same bug starts fine and breaks on the first ride at 8 a.m.

### What counts as a secret

A **secret** is any value that lets someone act as Spoke or read Spoke's data.

- database passwords
- API keys, such as the payment provider's key
- signing keys for tokens (the JWT key from [Spring Security](/t/module-08-backend/topic-11-spring-security))
- private keys for TLS certificates
- cloud credentials, such as an AWS access key
- webhook signing secrets

Not secrets: the database host, the pool size, the city name. They are config, but leaking them hurts nobody. Mixing the two up has costs both ways. Treat everything as secret, and nobody can debug. Treat nothing as secret, and the password ends up in a log.

### Secrets never go in git

[Spring Security](/t/module-08-backend/topic-11-spring-security) already said it: a secret in git is a stolen secret. Here is why that is so hard to undo.

- Git keeps **history**. Deleting the line in a new commit leaves it in every older commit, and in every clone.
- Bots scan public repositories for key patterns within minutes of a push.
- A private repo is read by every engineer, every CI runner and every tool with access.

Two guards stop a secret before it lands:

- **Secret scanning** in CI and on push. GitHub's push protection, or a tool such as gitleaks, rejects a push that contains a known key pattern.
- **A pre-commit hook** that runs the same scanner on your laptop.

If one slips through anyway, the fix is the same every time. **Rotate first**: make a new secret and switch the old one off. Cleaning history comes second, and it is optional.

### Secret managers: Vault and AWS Secrets Manager

```callout jargon
A **secret manager** is a service that stores secrets encrypted, decides who may read each one, and records every read. **HashiCorp Vault** and **AWS Secrets Manager** are two common ones.
```

What a secret manager gives you that a file cannot:

| Need | How the secret manager does it |
|---|---|
| Encrypted storage | secrets are encrypted at rest, with keys it guards |
| Least privilege | a policy says `spoke-api` in `prod` may read `prod/spoke-api/*`, and nothing else |
| An audit trail | every read is logged: who, which secret, when |
| Rotation | it can change a secret on a schedule |

How does the app prove who it is, without a secret to read secrets? The platform vouches for it. On Kubernetes, Vault trusts the Pod's **service account** token. On AWS, the Pod gets an **IAM role**. No password is needed to fetch the password.

Spring can read from a secret manager directly, through a library such as Spring Cloud Vault. Many teams instead sync secrets into Kubernetes with a tool such as the External Secrets Operator, so the app only reads files.

### Kubernetes Secrets: base64 is not encryption

[Running on Kubernetes](/t/module-09-architecture/topic-10-kubernetes) showed that a Kubernetes Secret is only **base64**:

```bash
printf 'hunter2' | base64
```

```text
aHVudGVyMg==
```

Anyone who can read the YAML can turn that back into `hunter2`. So:

- Never commit Secret YAML. Commit an `ExternalSecret` that **points at** Vault or AWS instead. Or use **Sealed Secrets**, which encrypts the value so only the cluster can open it.
- Turn on **encryption at rest** for the cluster's database, etcd.
- Limit who may `get secrets` in the `spoke` namespace. Reading a Secret should be as rare as reading a password.

### Environment variables or mounted files?

A Secret reaches the app in one of two ways. Both work. Files are the safer default.

**Environment variables leak.** Every child process inherits them. If spoke-api runs a helper program, that program gets `SPOKE_DB_PASSWORD` too. On Linux, `/proc/<pid>/environ` shows them to the same user and to root. Crash reporters and debug pages often dump the whole environment. And they are read once, at start, so a new value needs a restart.

**Mounted files** sit in a folder only the app can read. They are not inherited. Kubernetes updates a mounted Secret file in place after it changes, after a short delay.

Spring Boot reads a folder of files with `configtree`:

```yaml
spring:
  config:
    import: "optional:configtree:/etc/spoke/secrets/"
```

The file `/etc/spoke/secrets/spoke/db/password` becomes the key `spoke.db.password`, and its content becomes the value. One catch: Spring reads the files at startup. A new value in the file only reaches the app on restart, unless a library watches the file.

### Rotation and short-lived credentials

**Rotating** a secret means replacing it with a new one, on a schedule or after a leak. Without downtime, it takes an overlap:

1. Create the new password. The old one still works.
2. Roll out the app with the new password.
3. Check that nothing uses the old one any more.
4. Switch the old one off.

Better still: credentials that expire on their own. Vault's **database secrets engine** creates a fresh database user for each app instance, with a **lease** of, say, one hour. The app renews the lease while it runs. When the Pod dies, the user expires.

```callout jargon
A **short-lived credential** is a password or token that works only for minutes or hours. A **lease** is the time a credential stays valid before it must be renewed. A stolen short-lived credential is worth very little.
```

AWS offers the same idea for its databases: IAM authentication tokens that are valid for 15 minutes. The fewer long-lived passwords exist, the less there is to steal.

### Masking secrets in logs and actuator

A secret that never touches git can still leak through output. Three common holes:

- **Logging a settings object.** A record's `toString` prints every field, password included. Give it a `toString` that masks, or keep the password out of the record.
- **A password inside a URL**, such as `jdbc:postgresql://spoke_api:hunter2@db/spoke`. Mask the part between `:` and `@`, or keep the password out of the URL.
- **Actuator endpoints.** `/actuator/env` and `/actuator/configprops` list every setting. Since Spring Boot 3, they show every value as `******` by default.

Leave that default alone. `management.endpoint.env.show-values=always` on a public port publishes your passwords. As [Observability](/t/module-09-architecture/topic-08-observability) said, keep Actuator on its own port, with only `health`, `info` and `prometheus` exposed.

The masking rule is simple and safe: mask any key whose name **ends with** `password`, `secret`, `token` or `key`, or **contains** `credentials`. Masking too much costs a little debugging. Masking too little costs a password.

### Feature flags are not config

[Shipping to production](/t/module-07-tooling/topic-07-shipping-to-production) introduced feature flags. They look like config, but they behave differently.

| | Config | Feature flag |
|---|---|---|
| Changes when | at deploy or restart | at runtime, in seconds |
| Differs by | environment | rider, city, percentage of traffic |
| Owned by | the platform team | the product team |
| Lives | as long as the app | until the feature is fully out, then deleted |
| Spoke example | `spoke.db.pool-size` | `night-discount` for 10% of riders |

Two mistakes come from mixing them. A kill switch kept in an environment variable needs a restart of 30 Pods to flip, which is far too slow in an emergency. A database URL in the flag system can be changed by a click, with no review and no deploy record.

### Guarding against the wrong environment

Last week staging used the production database. No single rule prevents that. Stack several, strongest first:

1. **Separate networks.** Staging cannot reach production's database at all. A wrong URL simply times out.
2. **Separate credentials.** Staging's database user does not exist in production. A wrong URL fails to log in.
3. **No production values as defaults.** `application.yml` points at `localhost`, never at production. A forgotten override then fails safely.
4. **A startup guard.** If `spoke.env` is `staging` but the database host contains `prod`, refuse to start.
5. **Say where you are.** Log `spoke.env` and the database host, not the password, at startup. Show the environment on dashboards.

```animate
title: How staging found the production database
kind: scene
nodes:
  - { id: base, label: application.yml, emoji: "📄", value: "db-prod" }
  - { id: profile, label: application-staging.yml, emoji: "📄", value: "no url" }
  - { id: env, label: SPOKE_DB_URL, emoji: "🌍", value: "renamed" }
  - { id: app, label: spoke-api in staging, emoji: "🖥️", value: "?" }
  - { id: prod, label: Production database, emoji: "🗄️", row: 1, col: 3 }
steps:
  - say: application.yml had the production URL as its default. It was meant to be overridden everywhere else.
    send: { from: base, to: app, label: "db-prod" }
    set: { app: "db-prod" }
  - say: The staging profile file never set a URL. It changed only the pool size.
    focus: [profile]
  - say: The staging deploy used to set SPOKE_DB_URL. Someone renamed the variable, and nothing complained.
    focus: [env]
  - say: So the lowest layer won. Staging connected to production, and staging tests wrote fake rides into it.
    send: { from: app, to: prod, label: "INSERT fake rides" }
    set: { app: "connected to prod" }
  - say: A startup guard sees spoke.env=staging with a prod host and stops. Separate credentials would fail the login too.
    set: { app: "refused to start" }
```

### In an interview

Config and secrets questions test whether you have run a real service. Classic ones:

- **"How do you configure one service for dev, staging and prod?"** One artifact, config from outside it. Name the sources and their order. Profiles for small differences. Validation at startup.
- **"Where do you keep secrets?"** A secret manager, never git. The app proves who it is with its service account or IAM role. Secrets arrive as mounted files.
- **"A key was pushed to GitHub. What now?"** Rotate first, then check the audit log for use, then clean history if you must. Add secret scanning so it cannot happen again.
- **"How do you rotate a database password without downtime?"** Overlap two credentials, or use short-lived ones from Vault.
- **"Environment variables or files for secrets?"** Files: not inherited, not dumped by crash reports, and updated in place, then picked up on restart or by a file-watching library.
- **"How do you stop staging from writing to production?"** Network separation, separate credentials, no prod defaults, and a startup guard.

In a system design round, one sentence is enough. Say: "config comes from outside the image and secrets from a secret manager, all validated at startup".

## Worked example

Here is Spoke's fixed setup for staging, file by file.

**1. `application.yml` in the jar: safe defaults, no secrets.**

```yaml
spoke:
  env: dev
  db:
    url: jdbc:postgresql://localhost:5432/spoke
    username: spoke_api
    pool-size: 10
spring:
  config:
    import: "optional:configtree:/etc/spoke/secrets/"
```

The default URL is `localhost`. A forgotten override now fails on a laptop-style address, never on production. There is no password line at all.

**2. `application-staging.yml`: only what differs.**

```yaml
spoke:
  env: staging
  db:
    url: jdbc:postgresql://db.staging.spoke.internal:5432/spoke
    pool-size: 20
```

**3. The Deployment: the profile, one override, and the secret as a file.**

```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: staging
  - name: SPOKE_DB_POOLSIZE
    value: "30"
volumeMounts:
  - name: db-secret
    mountPath: /etc/spoke/secrets/spoke/db
    readOnly: true
```

The volume `db-secret` comes from a Kubernetes Secret with one key, `password`. The External Secrets Operator copies it from Vault's `staging/spoke-api/db` path. Git holds only the pointer.

**4. The effective config.** This model stacks the four sources in order and prints where each value came from, as Spring's `Origin` line does.

```java
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        // Lowest priority first. Each later source beats the earlier ones.
        Map<String, Map<String, String>> sources = new LinkedHashMap<>();
        sources.put("application.yml", Map.of(
                "spoke.env", "dev",
                "spoke.db.url", "jdbc:postgresql://localhost:5432/spoke",
                "spoke.db.username", "spoke_api",
                "spoke.db.pool-size", "10"));
        sources.put("application-staging.yml", Map.of(
                "spoke.env", "staging",
                "spoke.db.url", "jdbc:postgresql://db.staging.spoke.internal:5432/spoke",
                "spoke.db.pool-size", "20"));
        sources.put("secrets folder", Map.of(
                "spoke.db.password", "hunter2"));
        sources.put("SPOKE_DB_POOLSIZE", Map.of(
                "spoke.db.pool-size", "30"));

        Map<String, String> value = new TreeMap<>();
        Map<String, String> origin = new TreeMap<>();
        for (var source : sources.entrySet()) {
            for (var setting : source.getValue().entrySet()) {
                value.put(setting.getKey(), setting.getValue());
                origin.put(setting.getKey(), source.getKey());
            }
        }

        for (String key : value.keySet()) {
            String shown = key.endsWith("password") ? "******" : value.get(key);
            System.out.println(key + "=" + shown + "  <- " + origin.get(key));
        }
    }
}
```

It prints:

```text
spoke.db.password=******  <- secrets folder
spoke.db.pool-size=30  <- SPOKE_DB_POOLSIZE
spoke.db.url=jdbc:postgresql://db.staging.spoke.internal:5432/spoke  <- application-staging.yml
spoke.db.username=spoke_api  <- application.yml
spoke.env=staging  <- application-staging.yml
```

Line by line:

1. **`spoke.db.password`** came only from the secrets folder, and the output masks it.
2. **`spoke.db.pool-size`** was set three times. The environment variable sits highest, so `30` wins.
3. **`spoke.db.url`** comes from the staging profile, which beats `application.yml`.
4. **`spoke.db.username`** is the same everywhere, so only the base file sets it.
5. **`spoke.env`** says `staging`. A startup guard compares it with the database host before the app takes traffic.

**5. Fail fast.** `DbSettings` carries `@Validated`, and a guard checks `spoke.env` against the database host. A broken staging deploy now stops with a clear message. Most old Pods keep serving (all of them with `maxUnavailable: 0`) while someone fixes it.

## Try it

This snippet stacks four sources, checks the required keys and prints the effective config with the password masked.

```java run
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Map<String, String> config = new TreeMap<>();
        config.putAll(Map.of("spoke.env", "dev", "spoke.db.pool-size", "10"));        // application.yml
        config.putAll(Map.of("spoke.env", "staging", "spoke.db.pool-size", "20"));    // application-staging.yml
        config.putAll(Map.of("spoke.db.password", "hunter2"));                        // the secrets folder
        config.putAll(Map.of("spoke.db.pool-size", "30"));                            // SPOKE_DB_POOLSIZE

        List<String> missing = new ArrayList<>();
        for (String key : List.of("spoke.env", "spoke.db.url", "spoke.db.password")) {
            if (config.getOrDefault(key, "").isBlank()) {
                missing.add(key);
            }
        }
        System.out.println("missing: " + missing);

        config.forEach((key, value) ->
                System.out.println(key + "=" + (key.endsWith("password") ? "******" : value)));
    }
}
```

1. `spoke.db.url` is missing. Add it to the staging line, and watch `missing` become empty.
2. Swap the staging line and the `application.yml` line. Which `spoke.env` wins now, and why?
3. Add `"spoke.payment.api-key", "pay-key-live-7"` to the secrets line. It prints in plain text. Fix the masking rule so any key ending in `key`, `secret` or `token` is masked too.

## Recap

- Config is everything that differs between environments. Keep it out of the code and out of the build, so one image runs in dev, staging and prod.
- Spring Boot stacks property sources: defaults, `application.yml`, the profile file, environment variables, then the command line, each beating the one before. `SPOKE_DB_URL` binds to `spoke.db.url` by relaxed binding.
- Read settings into `@ConfigurationProperties` records with `@Validated`, so a bad config fails fast at startup and lists every problem at once.
- Secrets live in a secret manager such as Vault or AWS Secrets Manager, never in git. Kubernetes Secrets are only base64. Prefer mounted files over environment variables, rotate often, and prefer short-lived credentials.
- Mask secrets in logs and Actuator, and keep feature flags apart from config. Guard against the wrong environment with separate networks and credentials, no prod defaults and a startup check.

## Common mistakes

**1. A production URL as the default.**

```yaml
# application.yml
spoke:
  db:
    url: jdbc:postgresql://db-prod.spoke.internal:5432/spoke   # "every environment overrides it anyway"
```

Symptom: one environment forgets its override, or a variable gets renamed, and it quietly connects to production. Nothing fails, so nobody notices for days. Fix: defaults point at `localhost` or nothing at all, and a startup guard compares `spoke.env` with the database host.

**2. Logging the settings record.**

```java
public class Main {
    record DbSettings(String url, String username, String password) {}

    public static void main(String[] args) {
        DbSettings db = new DbSettings("jdbc:postgresql://db.staging.spoke.internal/spoke", "spoke_api", "hunter2");
        System.out.println("Connecting with " + db);
    }
}
```

It prints, in a real run:

```text
Connecting with DbSettings[url=jdbc:postgresql://db.staging.spoke.internal/spoke, username=spoke_api, password=hunter2]
```

A record's `toString` prints every field. Fix: override `toString` to print `password=******`, or keep the password out of the record that gets logged.

**3. Forgetting @Validated.**

```java nocompile
@ConfigurationProperties("spoke.db")
record DbSettings(@NotBlank String url, @NotBlank String username, @Min(1) int poolSize) {}
```

Symptom: the rules are there, but nobody checks them. In a real run with only the URL set, Spring started happily with `username=null, poolSize=0`. The app then breaks at its first database call, in front of riders. Fix: add `@Validated`, and the same config stops at startup with `APPLICATION FAILED TO START`.

**4. A dash in an environment variable name.**

```bash
export SPOKE_DB_POOL-SIZE=20
```

The shell refuses, in a real run:

```text
bash: line 0: export: `SPOKE_DB_POOL-SIZE=20': not a valid identifier
```

Fix: follow the rule. Dots become underscores, dashes disappear, capitals throughout: `SPOKE_DB_POOLSIZE`.

**5. Deleting a leaked secret and moving on.**

```bash
git rm config/prod-secrets.yml
git commit -m "remove secrets"
```

Symptom: the password is still in every older commit and every clone, and bots may have copied it already. Fix: rotate first. Make a new password, roll it out, switch the old one off, and check the audit log. Then add secret scanning so the next push is blocked.

**6. A kill switch in an environment variable.**

```yaml
env:
  - name: SPOKE_FEATURES_NIGHTDISCOUNT
    value: "true"
```

Symptom: the night discount misbehaves at 11 p.m. Turning it off means editing the manifest and restarting every Pod, which takes many minutes. Fix: runtime switches belong in a feature flag service that flips in seconds. Environment variables are for config that changes with a deploy.
