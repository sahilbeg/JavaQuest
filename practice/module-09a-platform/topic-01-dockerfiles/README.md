# A Dockerfile linter and a layer cache

Kakashi's security scan flagged the spoke-api image: 1.1 GB, root user, a full JDK and curl inside, and a 9-minute rebuild on every commit. Before anyone fixes it by hand, you build two tools that catch these problems for good: a **linter** that reads a Dockerfile and reports what is wrong, and a **layer cache** model that shows which build steps run again after a change.

No Docker is needed. Your code only reads text.

Given to you, finished: `Instruction` (one parsed instruction), `Finding` (one problem: `rule`, `line`, `message`), the `Rule` enum, `Step` (one build step for the cache model), and a few helpers in `DockerfileLinter` marked "given" or with no TODO: `stage(...)`, `earlierStage(...)`, `baseImage(...)`, `tokens(...)`, `withoutFlags(...)`, `hasFlag(...)`, `sources(...)`, `copiesSources(...)`, `tagOf(...)` and `repositoryOf(...)`.

Do the parts in order. The linter uses your parser, so its checks only pass once part 1 works.

## 1. `DockerfileParser.parse(String text)` → `List<Instruction>`

- **TODO 1:** split the text into lines (given). A line that is blank, or whose first non-space character is `#`, is skipped. That includes comment lines in the middle of a continued instruction, and parser directives such as `# syntax=docker/dockerfile:1`.
- **TODO 2:** a line that ends with a backslash `\` (after trimming) continues on the next line. Remove the backslash, trim each piece, and join the pieces with **one space**. The instruction's `line` is the **1-based** number of the line where it **starts**. Line numbers count every line of the file: blank lines, comments and continuation lines too.
- **TODO 3:** if the file ends while an instruction is still continued, keep that instruction too.
- The given `toInstruction(line, text)` splits off the keyword and turns it into capitals (`from` becomes `FROM`). `args` is the rest, trimmed. JSON forms such as `["java", "-jar", "app.jar"]` stay exactly as written.
- An empty text, or one with only comments and blank lines, gives an empty list.

## 2. `DockerfileLinter.lint(String text)` → `List<Finding>`

A **stage** starts at each `FROM` and holds the instructions up to the next `FROM`. The **final stage** is the last one. `FROM <image> [AS <name>]`: flags that start with `--` (such as `--platform=...`) are skipped, and stage names are compared ignoring case. Instructions before the first `FROM` belong to no stage. A file with no `FROM` has no stages, so the stage rules (base image, JDK, root, shell form, dependency order) report nothing for it.

- **TODO 4:** sort the findings by `line`, then by `rule` in the order of the `Rule` enum. Two findings with the same line and rule keep the order you found them in.
- **TODO 5, `stages(...)`:** split the instructions into stages with the given `stage(from, body)`.

The rules, one TODO each. Every finding needs a non-blank `message` for a human; the checks do not compare its words.

| TODO | Rule | Reported when | Line |
|---|---|---|---|
| 6 | `BASE_IMAGE_NOT_PINNED` | a `FROM` image has no tag, or the tag `latest`. **Fine:** `scratch`, the name of an **earlier** stage, and any image with a digest (it contains `@`). The tag is the text after the last `:` that comes after the last `/`, so `localhost:5000/spoke/base` has **no** tag (`:5000` is a registry port). `tagOf` does this for you. | that `FROM` |
| 7 | `JDK_IN_FINAL_STAGE` | the final stage's real base image, in lower case, contains `jdk`, or the last part of its repository name (after the last `/`, without tag or digest) is `maven` or `gradle`. When the final stage starts `FROM <earlier stage name>`, follow the names back to a real image: `baseImage(stages, last)` does it. | the final `FROM` |
| 8 | `RUNS_AS_ROOT` | the final stage has no `USER` (unless its real base image contains `nonroot`, as in `gcr.io/distroless/java21-debian12:nonroot`), **or** its **last** `USER` is `root` or `0`. Only the part before a `:` counts, so `USER 0:0` is root. A `USER` in an earlier stage does not count. | the final `FROM` when there is no `USER`; otherwise that last `USER` |
| 9 | `SHELL_FORM_COMMAND` | an `ENTRYPOINT` or `CMD` **in the final stage** whose args do not start with `[`. | that instruction |
| 10 | `ADD_FOR_LOCAL_FILE` | an `ADD`, in any stage, with at least one source that does not start with `http://` or `https://`. The sources are every word except flags (`--chown=...`) and the last word, the destination: `sources(args)` gives them. **One** finding per `ADD`. | that `ADD` |
| 11 | `SECRET_IN_ENV_OR_ARG` | an `ENV` or `ARG`, anywhere (also before the first `FROM`), declares a name that, in capitals, contains any of `PASSWORD`, `PASSWD`, `SECRET`, `TOKEN`, `API_KEY`, `APIKEY`, `PRIVATE_KEY` or `CREDENTIAL` (`SECRET_WORDS`). Split the args with `tokens(args)`, which keeps `"quoted text"` together as one word. Each word's name is the part before its first `=`. **Except** the old form `ENV NAME value`: when an `ENV`'s first word has no `=`, only that first word is a name. **One finding per matching name**, so `ENV DB_PASSWORD=x API_KEY=y` gives two. | that `ENV` or `ARG` |
| 12 | `APT_LISTS_NOT_CLEANED` | a `RUN`, in any stage, whose args contain `apt-get install` or `apt install`, but not `rm -rf /var/lib/apt/lists`. | that `RUN` |
| 13 | `SOURCES_BEFORE_DEPENDENCIES` | only in a stage where some `RUN` contains `mvn` (that includes `./mvnw`) and **no** `RUN` contains `--mount=type=cache`. Go through the stage in order, and stop at the first `RUN` that contains `dependency:go-offline` or `dependency:resolve`. Before that point, report every `COPY` without a `--from` flag whose sources copy the code: `.`, `./`, `src`, or anything starting with `src/` (`copiesSources` checks this). If no such `RUN` exists, check every `COPY` in the stage. | that `COPY` |

Example: Kakashi's Dockerfile, from the checks, gives exactly this list (rule@line): `JDK_IN_FINAL_STAGE@2, RUNS_AS_ROOT@2, APT_LISTS_NOT_CLEANED@3, SECRET_IN_ENV_OR_ARG@5, SECRET_IN_ENV_OR_ARG@6, SOURCES_BEFORE_DEPENDENCIES@8, ADD_FOR_LOCAL_FILE@10, SHELL_FORM_COMMAND@12`.

The linter reads the file once and looks at each instruction a fixed number of times, so it runs in O(n) time for n lines (resolving stage names adds O(s²) for s stages, which is tiny), and O(n) space for the parsed instructions.

## 3. `LayerCache(List<String> dockerignore)`

A model of Docker's build cache for one stage. A step runs again unless **every** step before it came from the cache, its instruction text is the same as last build's step at the same position, and none of the files it reads changed.

- **TODO 14, the constructor:** each `.dockerignore` line is stripped. Blank lines and lines starting with `#` are skipped. The rest are plain paths from the top of the build context, **no wildcards**. Clean each with the given `trimSlashes`, so `target/` means `target`.
- **TODO 15, `covers(path, file)`:** `true` when the path is `.` (or empty after cleaning), when `file` equals the path, or when `file` starts with path + `/`. So `src` covers `src/main/A.java` but not `srcold/A.java`, and `README.md` covers only the top-level `README.md`, not `docs/README.md`.
- **TODO 16, `isIgnored(file)`:** `true` when any ignored path covers the file.
- **TODO 17, `readsChange(step, changedFiles)`:** `true` when some changed file that is **not** ignored is covered by one of `step.reads()`. Ignored files never reach the build, so they never bust the cache.
- **TODO 18, `rebuilds(previous, current, changedFiles)`:** go through `current` in order. The first step that is invalidated is the first one where there is no step at the same position in `previous`, or that step's `instruction` text differs, or `readsChange` is `true`. That step **and every step after it** rebuild. Return their **1-based** numbers in order. Nothing invalidated gives an empty list. An empty `previous` (the first build) rebuilds every step.
- **TODO 19, `rebuildSeconds(...)`:** the sum of `seconds()` of the steps `rebuilds(...)` returns.

`rebuilds` is O(s × r × c) time for s steps, r reads per step and c changed files, and O(s) extra space for the answer.

Press **Submit** in JavaQuest, or just save the file in your IDE. The checks run on their own.
