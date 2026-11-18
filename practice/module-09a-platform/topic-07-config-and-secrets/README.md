# Config you can trust: resolve, redact, refuse

Staging quietly used the production database last week, and the database password sat in `application.yml` for a year. Build three small pieces of the startup path that stop both from happening again:

1. `ConfigResolver` merges every config source in a fixed order, fills in `${...}` placeholders and checks required keys.
2. `Redactor` hides secrets before the effective config is printed or logged.
3. `SafetyCheck` refuses to start when the environment and the database do not belong together.

These are small versions of what Spring Boot does for you, so you see every rule. Everything works on plain `Map<String, String>`s: keys like `spoke.db.url`, values as text.

Given to you: `ConfigSources` (a record holding the five sources), `ConfigException`, and its two subclasses `MissingConfigException` and `UnsafeConfigException`.

## 1. `ConfigResolver`

### TODO 1, `envNameToKey(String envName)`, returns `Optional<String>`

- Only names that start with `SPOKE_` are Spoke config. Any other name, such as `PATH` or `JAVA_HOME`, gives `Optional.empty()`.
- Otherwise: lower-case the whole name (use `Locale.ROOT`) and replace every `_` with `.`. `SPOKE_DB_URL` gives `spoke.db.url`. `SPOKE_DB_POOLSIZE` gives `spoke.db.poolsize`.

### TODO 2 to 4, `merge(ConfigSources sources)`, returns a sorted `Map<String, String>`

Start from an empty `TreeMap`. Apply the sources from **lowest to highest priority**, each one overwriting keys that are already there:

1. `defaults()`
2. `applicationFile()` (what `application.yml` holds)
3. `profileFile()` (what `application-staging.yml` holds, say)
4. `environment()`: the real environment variable names, such as `SPOKE_DB_URL`
5. `commandLine()`: arguments such as `--spoke.db.pool-size=20`

**Environment variables (TODO 3).** Skip a variable whose `envNameToKey` is empty. For the others, find every key **already in the map** whose *relaxed* form equals the env key. The relaxed form (the helper `relaxed`, given) is the key in lower case with every `-` removed: `spoke.db.pool-size` becomes `spoke.db.poolsize`.

- At least one key matches: overwrite each match's value, and keep its spelling. `SPOKE_DB_POOLSIZE=20` sets `spoke.db.pool-size` to `20`, and does **not** add `spoke.db.poolsize`.
- No key matches: add the env key itself, such as `spoke.cache.ttl` for `SPOKE_CACHE_TTL`.

**Command-line arguments (TODO 4).** Each must look like `--key=value`:

- It must start with `--` and contain an `=` after a key of at least one character. Otherwise throw `IllegalArgumentException`, with the whole argument in the message. So `spoke.env=prod`, `--spoke.env` and `--=prod` are all rejected.
- Split at the **first** `=`. `--spoke.db.url=jdbc:postgresql://db/spoke?ssl=true` has the value `jdbc:postgresql://db/spoke?ssl=true`.
- The value may be empty: `--spoke.banner=` sets `spoke.banner` to `""`.
- The key is used exactly as written.

`merge` leaves `${...}` placeholders alone.

### TODOs 5 and 7 to 9, `resolve(ConfigSources sources)`, returns a sorted `Map<String, String>`

Call `merge` first, **then** resolve placeholders, so a placeholder sees the final merged value. If the file says `spoke.db.url=jdbc:postgresql://${spoke.db.host}/spoke` and `SPOKE_DB_HOST` overrides the host, the URL gets the environment's host.

Resolve the keys **in sorted order** (the merged map is a `TreeMap`). For each key, `resolveKey(key, raw, new ArrayList<>())`. Inside a value:

- `${name}` is replaced by the **resolved** value of `name`. So placeholders may chain: a value can point at a key whose own value has placeholders.
- `${name:default}`: split the text inside the braces at its **first** `:`. If `name` exists, use its value; if not, use the default text exactly, which may be empty. `${spoke.db.url:jdbc:h2:mem:spoke}` has the default `jdbc:h2:mem:spoke`. Defaults are plain text: no placeholders inside them.
- Text around and between placeholders is kept: `jdbc:postgresql://${spoke.db.host}:${spoke.db.port}/spoke`.
- `name` missing and no default: throw `new ConfigException("Could not resolve placeholder ${" + name + "} in " + key)`, where `key` is the key whose value holds the placeholder.
- A `${` with no `}` after it: throw `new ConfigException("Unclosed placeholder in " + key + ": " + text)`.

**Cycles.** `path` holds the keys being resolved right now, in order.

- Before resolving `key`: if `path` already contains it, add it once more and throw `new ConfigException("Placeholder cycle: " + String.join(" -> ", path))`. With `spoke.a=${spoke.b}`, `spoke.b=${spoke.c}` and `spoke.c=${spoke.a}`, the message is `Placeholder cycle: spoke.a -> spoke.b -> spoke.c -> spoke.a`. A key that uses itself gives `Placeholder cycle: spoke.city -> spoke.city`.
- Otherwise add `key` to the end of `path`, resolve its value, then **remove it from the end** before returning. A key used twice, as in `${spoke.env}/${spoke.env}`, is not a cycle.

Time: O(total length of all values × depth of the deepest chain). Space: O(depth) for `path`. A key reached from several values is resolved again each time; that is fine for a few hundred keys.

### TODO 6, `requireKeys(Map<String, String> config, List<String> required)`

A required key is missing when it is absent **or blank** (empty, or only spaces). Collect **every** missing key, in the order of `required`, and throw `new MissingConfigException(thatList)` if there is at least one. Its message reads `Missing required config: spoke.db.url, spoke.db.password`. If nothing is missing, return normally.

Report them all at once. Fixing one key, restarting, and finding the next is how a deploy turns into an hour.

## 2. `Redactor`

### TODO 10, `isSecretKey(String key)`

Lower-case the key. It is secret when it **ends with** `password`, `secret`, `token` or `key`, or when it **contains** `credentials` anywhere.

- Secret: `spoke.db.password`, `SPOKE_DB_PASSWORD`, `spoke.jwt.signing-secret`, `spoke.github.token`, `spoke.payment.api-key`, `spoke.maps.apiKey`, `spoke.aws.credentials.id`.
- Not secret: `spoke.db.url`, `spoke.db.username`, `spoke.cache.key-prefix` (does not *end* with key), `spoke.rate.tokens-per-minute`.

### TODO 11, `maskUrlPassword(String value)`

A URL can hide a password: `jdbc:postgresql://spoke_api:hunter2@db.staging.spoke.internal:5432/spoke`. Use the given pattern `URL_PASSWORD`, whose group 2 is the password, and replace only that group with `MASK`, keeping groups 1 and 3: the result is `jdbc:postgresql://spoke_api:******@db.staging.spoke.internal:5432/spoke`. A value without `user:password@` is returned unchanged, so `redis://cache.staging.spoke.internal:6379` keeps its port.

### TODO 12, `redact(Map<String, String> config)`

Return a **new** `TreeMap` with every key. A secret key's value becomes `MASK`, which is exactly six stars `******`, whatever the real length. Every other value goes through `maskUrlPassword`. Never change the map you were given: the app still needs the real password.

## 3. `SafetyCheck`

### TODO 13, `hostOf(String url)`

- No `://` in the URL, as in `jdbc:h2:mem:spoke`: return `""`.
- Otherwise take the text after `://` and cut it at the first `/` or `?`.
- Drop everything up to and including the **last** `@`: the user name and password.
- Cut at the first `:`: the port.
- Return the host in lower case (`Locale.ROOT`).

`jdbc:postgresql://spoke_api:hunter2@DB-PROD.spoke.internal:5432/spoke` gives `db-prod.spoke.internal`. Assume passwords contain no `/`, `?` or `@`.

### TODO 14 and 15, `check(Map<String, String> config)`

Throw `UnsafeConfigException` when:

1. `spoke.env` is missing, or not exactly one of `dev`, `staging`, `prod`. The message quotes the value it got, such as `production`.
2. `spoke.db.url` is missing or blank.
3. `spoke.env` is **not** `prod`, but the host **contains** `prod`. Staging or dev must never touch a production database, replicas included.
4. `spoke.env` **is** `prod`, but the host does **not** contain `prod`. Production must not quietly run on a staging or in-memory database either.

The message for rules 3 and 4 names `spoke.env` and the host. It must **never** contain the URL or the password: error messages end up in logs.

Otherwise return normally. Spoke names every production host with `prod` in it, which is what makes this check possible.

## How the checks work

The checks build `ConfigSources` by hand, so nothing reads your real environment. Twenty-one checks in three classes: `ConfigResolverTest`, `RedactorTest` and `SafetyCheckTest`. The cycle checks give up after 2 seconds, in case a cycle loops forever.

Press **Submit** in JavaQuest, or just save the file in your IDE. The tests run on their own.
