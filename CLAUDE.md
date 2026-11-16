# JavaQuest: build guide for Claude

JavaQuest teaches Java from zero to professional through a story: the learner is an intern at Spoke, a bike-sharing startup. A Spring Boot app (`app/`) renders lessons and quizzes from `content/` and grades code in `practice/` with real JUnit tests.

The app is being built milestone by milestone. To continue the build, run `/continue-build`, or follow "Resuming the build" below.

## Where the build stands

These files are the tracker. Read them before doing anything:

| File | What it holds |
|---|---|
| `build_state.json` | The source of truth: milestone, `nextTask`, `resumeHere`, every module and topic with its status and verification. |
| `BUILD_STATE.md` | A readable mirror of the JSON, rebuilt from it. The JSON wins if they disagree. |
| `docs/ROADMAP.md` | Milestones: M0–M4 platform (done), C00–C10 one content module each, M5 capstone, M6 release hardening. |
| `docs/CURRICULUM.md` | Every unit to build: topic ids, titles, quiz or code, prerequisites. |

Topic statuses, in order: `planned`, `content_written`, `practice_written`, `tests_verified`, `ui_wired`, `done`. Claude takes a unit to `ui_wired`. Only the user's browser read-through moves it to `done`.

## Resuming the build

1. Read `build_state.json` (`milestone`, `nextTask`, `resumeHere`) and check `git status` and `git log --oneline -5`.
2. Tell the user where work stopped and what comes next, in a few lines.
3. Skip any unit whose files already exist and whose status is past `planned`.
4. Build the next milestone, following "How a content module is built".
5. When it is finished, update both state files, then commit (see "Finishing a milestone").

## How a content module is built (C00–C10)

1. Read `docs/CONTENT_SPEC.md`, `docs/STORY.md`, `PRODUCT.md`, the module's section in `docs/CURRICULUM.md`, and the previous module's content as the style reference.
2. Write `content/<module>/module.yaml`.
3. For each topic, write `meta.yaml`, `lesson.md` and `quiz.yaml`. For a code topic, also write `practice/<module>/<topic>/` with `README.md`, `skeleton/`, `src/` (an identical copy of `skeleton/`), `test/` and `solution/`.
4. Write the incident (`boss`, INC-NN, tiers from test class names ending `BronzeTest`, `SilverTest`, `GoldTest`) and the feature (`mini-project`, FEAT-NN). Neither has a `quiz.yaml`.
5. Add each practice unit's `src` and `test` folders to the two lists in `practice/pom.xml`.
6. Check each practice unit quickly with `.claude/scripts/check-topic.sh` (see Commands).
7. Run the three full checks (see Commands). All must pass.
8. Get an independent review of the whole module: facts, quiz answers, whether the brief states everything the tests need, tier separation, and consistency. Fix every finding, then run the full checks again.
9. Finish the milestone.

Large modules can be written in parallel by subagents, one or two topics each. Give them one shared brief with the module's conventions, and keep cross-topic facts consistent (fares, ids, URLs, error types).

## Rules learned while building

- **Voice:** explain like the reader is 15. Sentences under 25 words. Define every jargon word the first time it appears (`callout jargon`). Teammates send tickets but never teach.
- **Fares:** whole cents (`long`), 100 cents to unlock, plus per minute: CLASSIC 15, EBIKE 30, CARGO 25. Bike B-1042 is an EBIKE. Stations: Riverside, Old Mill, Harbor, Uptown, Depot. Rental ids look like `rental-17`.
- **Lesson code is executed:** the content checker compiles every ```` ```java ```` and ```` ```java run ```` block. It also runs any `main` it finds, with a 10 s, 256 MB limit. A block that starts a web server or waits must be ```` ```java nocompile ````. Run-block output must not depend on the clock or the machine.
- **Error texts and outputs** in lessons must come from real runs, not memory.
- **Skeletons:** they compile and carry numbered `// TODO n:` lines. They fail the tests with assertion messages, not compile errors, and a Spring skeleton's context still starts. They include every import the solution uses, because the in-app editor cannot add imports.
- **Tests:** `@DisplayName` on every test, a beginner-readable message on every assertion. No network, no real ports (use MockMvc), files only in `@TempDir`. They pass on any machine, at any time of day.
- **Quizzes:** `drawCount` 6, `passPercent` 80, 10–12 questions. Exactly one defensible answer per question. A fill-blank prompt allows only one answer. Every meta concept is covered, and every `reteach` names a real heading.
- **Spring topics** (`runner: spring`): every library is on one shared runner classpath (`practice/target/runner-classpath.txt`). Spring Security is there without Boot's auto-configuration, so nothing is secured until a topic writes `@EnableWebSecurity`. Practice code has no resource files (no `application.properties`, no `schema.sql`), so configure in code or in `@SpringBootTest(properties = ...)`. No `mainClass` for web apps. The runner compiles with `-parameters`. Spring Boot 4 test annotations moved packages, and Jackson is version 3 (`tools.jackson`), so compile against the real jars rather than guessing.
- **An incident must not be given away:** the topic that teaches its ideas uses different examples.

## Commands

```bash
./run.sh                                                             # start the app at http://localhost:2026
./mvnw -q install                                                    # build and run all platform tests
./mvnw -q -pl tools dependency:properties exec:java@check-content    # check every lesson and quiz (about 2 min)
./mvnw -q -pl tools dependency:properties exec:java@verify-practice  # solution passes, skeleton fails, for every unit (about 3 min); writes build_state.json
.claude/scripts/check-topic.sh <practice unit dir> [solution|skeleton|src]   # one unit, fast, same JVM flags as the runner
```

Do not run `./mvnw install` (or `package`) while the app is running: it rewrites `app/target/javaquest.jar` under the running server, which then hangs. Stop the app first, or use `./mvnw -q test` for the platform tests. `run.sh` rebuilds the jar when `app/`, `runner/` or the poms changed.

After a change to `practice/pom.xml`, refresh the runner classpath:

```bash
./mvnw -q -f practice/pom.xml dependency:build-classpath -Dmdep.outputFile="$PWD/practice/target/runner-classpath.txt"
```

## Finishing a milestone

1. Set every unit of the module to `ui_wired` in `build_state.json`. Code units must already have `solutionPasses: true` and `skeletonFails: true`. Set the module to `ui_wired`, set the milestone to `ui_wired`, and move `milestone`, `nextTask` and `resumeHere` on. Append one line to `completedTasks`.
2. Rebuild `BUILD_STATE.md` from the JSON, keeping its Commands and Session protocol sections.
3. Commit content as `CNN: module N <title>, incident INC-NN and feature FEAT-NN`. Commit platform changes separately, before the content.
4. Do not push unless the user asks.

## Open items for the user

- The browser read-through of finished modules, which moves them from `ui_wired` to `done`.
- A real VoiceOver pass.
